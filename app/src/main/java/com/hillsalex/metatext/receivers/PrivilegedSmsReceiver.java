package com.hillsalex.metatext.receivers;

/**
 * Created by alex on 11/10/2014.
 */

import android.content.Context;
import android.content.Intent;

import com.android.mms.MmsConfig;

/**
 * This class exists specifically to allow us to require permissions checks on SMS_RECEIVED
 * broadcasts that are not applicable to other kinds of broadcast messages handled by the
 * SmsReceiver base class.
 */
public class PrivilegedSmsReceiver extends SmsReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        MmsConfig.init(context);
        // Pass the message to the base class implementation, noting that it
        // was permission-checked on the way in.
        onReceiveWithPrivilege(context, intent, true);
    }
}
