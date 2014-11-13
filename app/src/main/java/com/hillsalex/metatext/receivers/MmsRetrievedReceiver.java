package com.hillsalex.metatext.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hillsalex.metatext.notifications.NotificationFactory;

/**
 * Created by alex on 11/11/2014.
 */
public class MmsRetrievedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationFactory.makeNotification(context, "New MMS Received", "TODO some shit with it");
    }
}
