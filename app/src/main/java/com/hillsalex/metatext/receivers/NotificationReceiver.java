package com.hillsalex.metatext.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.hillsalex.metatext.StaticMessageStrings;
import com.hillsalex.metatext.database.ActiveDatabases;
import com.hillsalex.metatext.model.SmsMessageModel;
import com.hillsalex.metatext.notifications.NotificationFactory;

/**
 * Created by alex on 11/12/2014.
 */
public class NotificationReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action){
            case StaticMessageStrings.NOTIFY_MESSAGE_RECEIVED:
                Uri uri = intent.getParcelableExtra(StaticMessageStrings.MESSAGE_RECEIVED_URI);
                boolean hasSms = intent.hasExtra(StaticMessageStrings.MESSAGE_RECEIVED_IS_SMS);
                if (uri==null || hasSms == false) return;
                boolean isSms = intent.getBooleanExtra(StaticMessageStrings.MESSAGE_RECEIVED_IS_SMS,false);
                if (isSms) {
                    NotificationFactory.makeNotificationForSms(context, ActiveDatabases.getSmsDatabase(context).getMessageForThreadView(uri));
                }
                else{
                    NotificationFactory.makeNotificationForMms(context, ActiveDatabases.getMmsDatabase(context).getMessageForThreadView(uri));
                }
                break;
        }
    }
}
