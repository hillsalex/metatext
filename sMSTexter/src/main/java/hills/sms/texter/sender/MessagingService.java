package hills.sms.texter.sender;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class MessagingService extends IntentService{
	private static final String TAG = "MessagingService";

    // These actions are for this app only and are used by MessagingReceiver to start this service
    public static final String ACTION_MY_RECEIVE_SMS = "com.example.android.smssample.RECEIVE_SMS";
    public static final String ACTION_MY_RECEIVE_MMS = "com.example.android.smssample.RECEIVE_MMS";
	
    public MessagingService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String intentAction = intent.getAction();
            if (ACTION_MY_RECEIVE_SMS.equals(intentAction)) {
            	// Retrieves a map of extended data from the intent.
            	final Bundle bundle = intent.getExtras();
            	
            	try {
            	
	            	if (bundle != null) {
		            	
		            	final Object[] pdusObj = (Object[]) bundle.get("pdus");
		            	
		            	for (int i = 0; i < pdusObj.length; i++) {
		            	
							SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
							String phoneNumber = currentMessage.getDisplayOriginatingAddress();
							
							String senderNum = phoneNumber;
							String message = currentMessage.getDisplayMessageBody();
							
							Log.i("SmsReceiver", "senderNum: "+ senderNum + "; message: " + message);
							ContentResolver resolver = getContentResolver();
							Uri smsUri = Telephony.Sms.CONTENT_URI;
							ContentValues values = new ContentValues();
							values.put(Telephony.Sms.ADDRESS, phoneNumber);
							values.put(Telephony.Sms.BODY, message);
							Uri uri = resolver.insert(smsUri, values);
							// Show alert
							//int duration = Toast.LENGTH_LONG;
							//Toast toast = Toast.makeText(context, "senderNum: "+ senderNum + ", message: " + message, duration);
							//toast.show();
		            	} // end for loop
					} // bundle is null
				} catch (Exception e) {
					Log.e("SmsReceiver", "Exception smsReceiver" +e);
				}

                // Ensure wakelock is released that was created by the WakefulBroadcastReceiver
                MessagingReceiver.completeWakefulIntent(intent);
            } else if (ACTION_MY_RECEIVE_MMS.equals(intentAction)) {
                

                // Ensure wakelock is released that was created by the WakefulBroadcastReceiver
                MessagingReceiver.completeWakefulIntent(intent);
            }
        }
    }

}
