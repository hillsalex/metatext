package com.hillsalex.metatext.services;

import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.android.mms.util_alt.SqliteWrapper;
import com.hillsalex.metatext.StaticMessageStrings;
import com.hillsalex.metatext.database.ActiveDatabases;
import com.hillsalex.metatext.notifications.NotificationFactory;
import com.hillsalex.metatext.receivers.SmsReceiver;
import com.klinker.android.send_message.Utils;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static android.provider.Telephony.Sms.Intents.SMS_DELIVER_ACTION;

/**
 * Created by alex on 11/10/2014.
 */
public class SmsReceiverService extends Service {

    private String TAG = "SMSReceiverService";

    private ServiceHandler mServiceHandler;
    private Looper mServiceLooper;
    private boolean mSending;
    public static final String MESSAGE_SENT_ACTION =
            "com.android.mms.transaction.MESSAGE_SENT";

    // Indicates next message can be picked up and sent out.
    public static final String EXTRA_MESSAGE_SENT_SEND_NEXT = "SendNextMsg";

    public static final String ACTION_SEND_MESSAGE =
            "com.android.mms.transaction.SEND_MESSAGE";
    public static final String ACTION_SEND_INACTIVE_MESSAGE =
            "com.android.mms.transaction.SEND_INACTIVE_MESSAGE";

    // This must match the column IDs below.
    private static final String[] SEND_PROJECTION = new String[]{
            Telephony.Sms._ID,        //0
            Telephony.Sms.THREAD_ID,  //1
            Telephony.Sms.ADDRESS,    //2
            Telephony.Sms.BODY,       //3
            Telephony.Sms.STATUS,     //4

    };

    public Handler mToastHandler = new Handler();

    // This must match SEND_PROJECTION.
    private static final int SEND_COLUMN_ID = 0;
    private static final int SEND_COLUMN_THREAD_ID = 1;
    private static final int SEND_COLUMN_ADDRESS = 2;
    private static final int SEND_COLUMN_BODY = 3;
    private static final int SEND_COLUMN_STATUS = 4;

    private int mResultCode;


    @Override
    public void onCreate() {
        // Temporarily removed for this duplicate message track down.
//        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE) || LogTag.DEBUG_SEND) {
//            Log.v(TAG, "onCreate");
//        }

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        HandlerThread thread = new HandlerThread(TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mResultCode = intent != null ? intent.getIntExtra("result", 0) : 0;

        if (mResultCode != 0) {
            Log.v(TAG, "onStart: #" + startId + " mResultCode: " + mResultCode +
                    " = " + translateResultCode(mResultCode));
        }

        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
        return Service.START_STICKY;
    }

    private static String translateResultCode(int resultCode) {
        switch (resultCode) {
            case Activity.RESULT_OK:
                return "Activity.RESULT_OK";
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                return "SmsManager.RESULT_ERROR_GENERIC_FAILURE";
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                return "SmsManager.RESULT_ERROR_RADIO_OFF";
            case SmsManager.RESULT_ERROR_NULL_PDU:
                return "SmsManager.RESULT_ERROR_NULL_PDU";
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                return "SmsManager.RESULT_ERROR_NO_SERVICE";
            default:
                return "Unknown error code";
        }
    }

    @Override
    public void onDestroy() {
        // Temporarily removed for this duplicate message track down.
//        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE) || LogTag.DEBUG_SEND) {
//            Log.v(TAG, "onDestroy");
//        }
        mServiceLooper.quit();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        /**
         * Handle incoming transaction requests.
         * The incoming requests are initiated by the MMSC Server or by the MMS Client itself.
         */
        @Override
        public void handleMessage(Message msg) {
            int serviceId = msg.arg1;
            Intent intent = (Intent) msg.obj;
            if (intent != null) {// && MmsConfig.isSmsEnabled(getApplicationContext())) {
                String action = intent.getAction();

                int error = intent.getIntExtra("errorCode", 0);


                if (MESSAGE_SENT_ACTION.equals(intent.getAction())) {
                    //handleSmsSent(intent, error);
                } else if (SMS_DELIVER_ACTION.equals(action)) {
                    handleSmsReceived(intent, error);
                } else if (ACTION_BOOT_COMPLETED.equals(action)) {
                    //handleBootCompleted();
                    //} else if (TelephonyIntents.ACTION_SERVICE_STATE_CHANGED.equals(action)) {
                    //handleServiceStateChanged(intent);
                } else if (ACTION_SEND_MESSAGE.endsWith(action)) {
                    //  handleSendMessage();
                } else if (ACTION_SEND_INACTIVE_MESSAGE.equals(action)) {
                    // handleSendInactiveMessage();
                }
            }
            // NOTE: We MUST not call stopSelf() directly, since we need to
            // make sure the wake lock acquired by AlertReceiver is released.
            SmsReceiver.finishStartingService(SmsReceiverService.this, serviceId);
        }
    }

    private void handleSmsReceived(Intent intent, int error) {
        SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        String format = intent.getStringExtra("format");
        Uri messageUri = insertMessage(this, msgs, error, format);

        if (messageUri != null) {

            //NotificationFactory.makeNotification(this, "New SMS Received","TODO shit with it");
            //long threadId = MessagingNotification.getSmsThreadId(this, messageUri);
            // Called off of the UI thread so ok to block.
            //Log.d(TAG, "handleSmsReceived messageUri: " + messageUri + " threadId: " + threadId);
            //MessagingNotification.blockingUpdateNewMessageIndicator(this, threadId, false);




            Log.v("SmsReceiver","Uri is:"+(messageUri==null ? null : messageUri.toString()));
            if (messageUri!=null && messageUri.toString().startsWith("content://")) {
                NotificationFactory.makeNotificationForSms(this, ActiveDatabases.getSmsDatabase(this).getMessageForThreadView(messageUri));

                Intent threadUpdatedIntent = new Intent(StaticMessageStrings.NOTIFY_MESSAGE_RECEIVED);
                threadUpdatedIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                threadUpdatedIntent.putExtra(StaticMessageStrings.MESSAGE_RECEIVED_URI, messageUri);
                threadUpdatedIntent.putExtra(StaticMessageStrings.MESSAGE_IS_SMS, true);
                this.sendBroadcast(threadUpdatedIntent);
            }
        }
    }

    /**
     * If the message is a class-zero message, display it immediately
     * and return null.  Otherwise, store it using the
     * <code>ContentResolver</code> and return the
     * <code>Uri</code> of the thread containing this message
     * so that we can use it for notification.
     */
    private Uri insertMessage(Context context, SmsMessage[] msgs, int error, String format) {
        // Build the helper classes to parse the messages.
        SmsMessage sms = msgs[0];

        if (sms.getMessageClass() == SmsMessage.MessageClass.CLASS_0) {
            //displayClassZeroMessage(context, sms, format);
            return null;
        } else if (sms.isReplace()) {
            return replaceMessage(context, msgs, error);
        } else {
            return storeMessage(context, msgs, error);
        }
    }


    // This must match the column IDs below.
    private final static String[] REPLACE_PROJECTION = new String[]{
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.PROTOCOL
    };

    // This must match REPLACE_PROJECTION.
    private static final int REPLACE_COLUMN_ID = 0;

    /**
     * This method is used if this is a "replace short message" SMS.
     * We find any existing message that matches the incoming
     * message's originating address and protocol identifier.  If
     * there is one, we replace its fields with those of the new
     * message.  Otherwise, we store the new message as usual.
     * <p/>
     * See TS 23.040 9.2.3.9.
     */
    private Uri replaceMessage(Context context, SmsMessage[] msgs, int error) {
        SmsMessage sms = msgs[0];
        ContentValues values = extractContentValues(sms);
        values.put(Telephony.Sms.ERROR_CODE, error);
        int pduCount = msgs.length;

        if (pduCount == 1) {
            // There is only one part, so grab the body directly.
            values.put(Telephony.Sms.Inbox.BODY, replaceFormFeeds(sms.getDisplayMessageBody()));
        } else {
            // Build up the body from the parts.
            StringBuilder body = new StringBuilder();
            for (int i = 0; i < pduCount; i++) {
                sms = msgs[i];
                //if (sms.mWrappedSmsMessage != null) {
                body.append(sms.getDisplayMessageBody());
                //}
            }
            values.put(Telephony.Sms.Inbox.BODY, replaceFormFeeds(body.toString()));
        }

        ContentResolver resolver = context.getContentResolver();
        String originatingAddress = sms.getOriginatingAddress();
        int protocolIdentifier = sms.getProtocolIdentifier();
        String selection =
                Telephony.Sms.ADDRESS + " = ? AND " +
                        Telephony.Sms.PROTOCOL + " = ?";
        String[] selectionArgs = new String[]{
                originatingAddress, Integer.toString(protocolIdentifier)
        };

        Cursor cursor = SqliteWrapper.query(context, resolver, Telephony.Sms.Inbox.CONTENT_URI,
                REPLACE_PROJECTION, selection, selectionArgs, null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    long messageId = cursor.getLong(REPLACE_COLUMN_ID);
                    Uri messageUri = ContentUris.withAppendedId(
                            Telephony.Sms.CONTENT_URI, messageId);

                    SqliteWrapper.update(context, resolver, messageUri,
                            values, null, null);
                    return messageUri;
                }
            } finally {
                cursor.close();
            }
        }
        return storeMessage(context, msgs, error);
    }

    public static String replaceFormFeeds(String s) {
        // Some providers send formfeeds in their messages. Convert those formfeeds to newlines.
        return s == null ? "" : s.replace('\f', '\n');
    }

//    private static int count = 0;

    private Uri storeMessage(Context context, SmsMessage[] msgs, int error) {
        SmsMessage sms = msgs[0];

        // Store the message in the content provider.
        ContentValues values = extractContentValues(sms);
        values.put(Telephony.Sms.ERROR_CODE, error);
        int pduCount = msgs.length;

        if (pduCount == 1) {
            // There is only one part, so grab the body directly.
            values.put(Telephony.Sms.Inbox.BODY, replaceFormFeeds(sms.getDisplayMessageBody()));
        } else {
            // Build up the body from the parts.
            StringBuilder body = new StringBuilder();
            for (int i = 0; i < pduCount; i++) {
                sms = msgs[i];
                //if (sms.mWrappedSmsMessage != null) {
                body.append(sms.getDisplayMessageBody());
                //}
            }
            values.put(Telephony.Sms.Inbox.BODY, replaceFormFeeds(body.toString()));
        }

        // Make sure we've got a thread id so after the insert we'll be able to delete
        // excess messages.
        Long threadId = values.getAsLong(Telephony.Sms.THREAD_ID);
        String address = values.getAsString(Telephony.Sms.ADDRESS);

        // Code for debugging and easy injection of short codes, non email addresses, etc.
        // See Contact.isAlphaNumber() for further comments and results.
//        switch (count++ % 8) {
//            case 0: address = "AB12"; break;
//            case 1: address = "12"; break;
//            case 2: address = "Jello123"; break;
//            case 3: address = "T-Mobile"; break;
//            case 4: address = "Mobile1"; break;
//            case 5: address = "Dogs77"; break;
//            case 6: address = "****1"; break;
//            case 7: address = "#4#5#6#"; break;
//        }
        /*
        if (!TextUtils.isEmpty(address)) {
            Contact cacheContact = Contact.get(address,true);
            if (cacheContact != null) {
                address = cacheContact.getNumber();
            }
        } else {
            address = getString(R.string.unknown_sender);
            values.put(Telephony.Sms.ADDRESS, address);
        }*/

        if (((threadId == null) || (threadId == 0)) && (address != null)) {
            threadId = Utils.getOrCreateThreadId(context, address);
            values.put(Telephony.Sms.THREAD_ID, threadId);
        }

        ContentResolver resolver = context.getContentResolver();

        Uri insertedUri = SqliteWrapper.insert(context, resolver, Telephony.Sms.Inbox.CONTENT_URI, values);

        // Now make sure we're not over the limit in stored messages

        return insertedUri;
    }

    /**
     * Extract all the content values except the body from an SMS
     * message.
     */
    private ContentValues extractContentValues(SmsMessage sms) {
        // Store the message in the content provider.
        ContentValues values = new ContentValues();

        values.put(Telephony.Sms.Inbox.ADDRESS, sms.getDisplayOriginatingAddress());

        // Use now for the timestamp to avoid confusion with clock
        // drift between the handset and the SMSC.
        // Check to make sure the system is giving us a non-bogus time.
        Calendar buildDate = new GregorianCalendar(2011, 8, 18);    // 18 Sep 2011
        Calendar nowDate = new GregorianCalendar();
        long now = System.currentTimeMillis();
        nowDate.setTimeInMillis(now);

        if (nowDate.before(buildDate)) {
            // It looks like our system clock isn't set yet because the current time right now
            // is before an arbitrary time we made this build. Instead of inserting a bogus
            // receive time in this case, use the timestamp of when the message was sent.
            now = sms.getTimestampMillis();
        }

        values.put(Telephony.Sms.Inbox.DATE, new Long(now));
        values.put(Telephony.Sms.Inbox.DATE_SENT, Long.valueOf(sms.getTimestampMillis()));
        values.put(Telephony.Sms.Inbox.PROTOCOL, sms.getProtocolIdentifier());
        values.put(Telephony.Sms.Inbox.READ, 0);
        values.put(Telephony.Sms.Inbox.SEEN, 0);
        if (sms.getPseudoSubject().length() > 0) {
            values.put(Telephony.Sms.Inbox.SUBJECT, sms.getPseudoSubject());
        }
        values.put(Telephony.Sms.Inbox.REPLY_PATH_PRESENT, sms.isReplyPathPresent() ? 1 : 0);
        values.put(Telephony.Sms.Inbox.SERVICE_CENTER, sms.getServiceCenterAddress());
        return values;
    }

}
