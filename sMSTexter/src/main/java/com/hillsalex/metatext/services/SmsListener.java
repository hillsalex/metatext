package com.hillsalex.metatext.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;


import com.hillsalex.metatext.messages.sms.IncomingTextMessage;
import com.hillsalex.metatext.messages.sms.OutgoingTextMessage;
import com.hillsalex.metatext.persons.Person;
import com.hillsalex.metatext.persons.Persons;

import java.util.ArrayList;

import hills.sms.texter.preferences.TexterPreferences;
import hills.sms.texter.util.Util;

/**
 * Created by alex on 11/6/2014.
 */

public class SmsListener extends BroadcastReceiver {
    private static final String TAG = "SmsListener";
    private static final String SMS_RECEIVED_ACTION  = Telephony.Sms.Intents.SMS_RECEIVED_ACTION;
    private static final String SMS_DELIVERED_ACTION = Telephony.Sms.Intents.SMS_DELIVER_ACTION;
    private static final String MMS_SMS_SEND_ACTION = Intent.ACTION_SENDTO;


    private boolean isExemption(SmsMessage message, String messageBody) {

        // ignore CLASS0 ("flash") messages
        if (message.getMessageClass() == SmsMessage.MessageClass.CLASS_0)
            return true;

        // ignore OTP messages from Sparebank1 (Norwegian bank)
        if (messageBody.startsWith("Sparebank1://otp?")) {
            return true;
        }

        return
                message.getOriginatingAddress().length() < 7 &&
                        (messageBody.toUpperCase().startsWith("//ANDROID:") || // Sprint Visual Voicemail
                                messageBody.startsWith("//BREW:")); //BREW stands for “Binary Runtime Environment for Wireless"
    }

    private SmsMessage getSmsMessageFromIntent(Intent intent) {
        Bundle bundle             = intent.getExtras();
        Object[] pdus             = (Object[])bundle.get("pdus");

        if (pdus == null || pdus.length == 0)
            return null;

        return SmsMessage.createFromPdu((byte[])pdus[0]);
    }

    private String getSmsMessageBodyFromIntent(Intent intent) {
        Bundle bundle             = intent.getExtras();
        Object[] pdus             = (Object[])bundle.get("pdus");
        StringBuilder bodyBuilder = new StringBuilder();

        if (pdus == null)
            return null;

        for (Object pdu : pdus)
            bodyBuilder.append(SmsMessage.createFromPdu((byte[])pdu).getDisplayMessageBody());

        return bodyBuilder.toString();
    }

    private OutgoingTextMessage getToSendAsTextMessage(Intent intent){
        Uri content = intent.getData();
        String body = intent.getStringExtra("sms_body");
        String address = content.getLastPathSegment();
        Person p = new Person(0,address,null,null,"");
        return new OutgoingTextMessage(p,body);
    }

    private ArrayList<IncomingTextMessage> getAsTextMessages(Intent intent) {
        Object[] pdus                   = (Object[])intent.getExtras().get("pdus");
        ArrayList<IncomingTextMessage> messages = new ArrayList<IncomingTextMessage>(pdus.length);

        for (int i=0;i<pdus.length;i++)
            messages.add(new IncomingTextMessage(SmsMessage.createFromPdu((byte[])pdus[i])));

        return messages;
    }
/*
    private boolean isRelevant(Context context, Intent intent) {
        SmsMessage message = getSmsMessageFromIntent(intent);
        String messageBody = getSmsMessageBodyFromIntent(intent);

        if (message == null && messageBody == null)
            return false;

        if (isExemption(message, messageBody))
            return false;

        if (!ApplicationMigrationService.isDatabaseImported(context))
            return false;

        if (isChallenge(context, intent))
            return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                SMS_RECEIVED_ACTION.equals(intent.getAction()) &&
                Util.isDefaultSmsProvider(context))
        {
            return false;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT &&
                TexterPreferences.isInterceptAllSmsEnabled(context))
        {
            return true;
        }

        return WirePrefix.isEncryptedMessage(messageBody) || WirePrefix.isKeyExchange(messageBody);
    }

    private boolean isChallenge(Context context, Intent intent) {
        String messageBody = getSmsMessageBodyFromIntent(intent);

        if (messageBody == null)
            return false;

        if (messageBody.matches("Your TextSecure verification code: [0-9]{3,4}-[0-9]{3,4}") &&
                TextSecurePreferences.isVerifying(context))
        {
            return true;
        }

        return false;
    }

    private String parseChallenge(Context context, Intent intent) {
        String messageBody    = getSmsMessageBodyFromIntent(intent);
        String[] messageParts = messageBody.split(":");
        String[] codeParts    = messageParts[1].trim().split("-");

        return codeParts[0] + codeParts[1];
    }
*/
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w("SMSListener", "Got SMS broadcast...");

        if (false){//SMS_RECEIVED_ACTION.equals(intent.getAction()) && isChallenge(context, intent)) {
            Log.w("SmsListener", "Got challenge!");
            //Intent challengeIntent = new Intent(RegistrationService.CHALLENGE_EVENT);
            //challengeIntent.putExtra(RegistrationService.CHALLENGE_EXTRA, parseChallenge(context, intent));
            //context.sendBroadcast(challengeIntent);

            abortBroadcast();
        } else if (//(intent.getAction().equals(SMS_DELIVERED_ACTION)) ||
                (intent.getAction().equals(SMS_DELIVERED_ACTION)))// && isRelevant(context, intent))
        {
            Log.i(TAG,"Got request to write SMS to database");
            Intent receivedIntent = new Intent(context, SendReceiveServices.class);
            receivedIntent.setAction(SendReceiveServices.RECEIVE_SMS_ACTION);
            receivedIntent.putExtra("ResultCode", this.getResultCode());
            receivedIntent.putParcelableArrayListExtra("text_messages",getAsTextMessages(intent));
            context.startService(receivedIntent);
            abortBroadcast();
        }
        else if((intent.getAction().equals(SMS_RECEIVED_ACTION))){
            Log.i(TAG,"Got notified that a different app wrote SMS to database");
        }
        else if((intent.getAction().equals(MMS_SMS_SEND_ACTION))){

            Intent receivedIntent = new Intent(context, SendReceiveServices.class);
            receivedIntent.putExtra("ResultCode", this.getResultCode());
            OutgoingTextMessage message = getToSendAsTextMessage(intent);
            if (message.isGroup()){
                return;
            }
            else
            {
                receivedIntent.setAction(SendReceiveServices.SEND_SINGLE_SMS_ACTION);
                receivedIntent.putExtra("text_message",message);
            }
            context.startService(receivedIntent);
            abortBroadcast();


        }
    }
}
