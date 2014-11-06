package hills.sms.texter.sender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class IncomingSmsBroadcastReceiver extends BroadcastReceiver {

	private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(final Context context, final Intent intent) {

        if (intent != null && SMS_RECEIVED.equals(intent.getAction())) {
            final SmsMessage smsMessage = extractSmsMessage(intent);
            processMessage(context, smsMessage);
        }

    }

    private SmsMessage extractSmsMessage(final Intent intent) {

        final Bundle pudsBundle = intent.getExtras();
        final Object[] pdus = (Object[]) pudsBundle.get("pdus");
        final SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[0]);

        return smsMessage;

    }

    private void processMessage(final Context context, final SmsMessage smsMessage) {
        // Do something interesting here
    }


}
