package com.hillsalex.metatext.services;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.hillsalex.metatext.StaticMessageStrings;
import com.hillsalex.metatext.database.ActiveDatabases;
import com.hillsalex.metatext.notifications.NotificationFactory;

/**
 * Created by alex on 11/11/2014.
 */
public class MmsReceivedService extends Service {

    public static final String MMS_RECEIVED_AND_DOWNLOADED = "com.mmsreceived.mms_received_and_downloaded";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Uri uri = intent.getParcelableExtra(StaticMessageStrings.MESSAGE_RECEIVED_URI);
        boolean hasSms = intent.hasExtra(StaticMessageStrings.MESSAGE_RECEIVED_IS_SMS);
        if (uri==null || hasSms == false) return Service.START_NOT_STICKY;
        boolean isSms = intent.getBooleanExtra(StaticMessageStrings.MESSAGE_RECEIVED_IS_SMS,false);
        if (isSms) {
            NotificationFactory.makeNotificationForSms(this, ActiveDatabases.getSmsDatabase(this).getMessageForThreadView(uri));
        }
        else{
            NotificationFactory.makeNotificationForMms(this, ActiveDatabases.getMmsDatabase(this).getMessageForThreadView(uri));
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
