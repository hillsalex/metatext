package com.hillsalex.metatext.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SqliteWrapper;
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

    public static final String DELETE_NOTIFICAITON_MSS = "com.hillsalex.metatext.DELETE_NOTIFICATION";
    public static final String DELETE_URI = "com.hillsalex.metatext.DELETE_URI";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Uri uri;
        switch(action) {
            case MMS_RECEIVED_AND_DOWNLOADED:
                uri= intent.getParcelableExtra(StaticMessageStrings.MESSAGE_RECEIVED_URI);
                boolean hasSms = intent.hasExtra(StaticMessageStrings.MESSAGE_IS_SMS);
                if (uri == null || hasSms == false) return Service.START_STICKY;
                boolean isSms = intent.getBooleanExtra(StaticMessageStrings.MESSAGE_IS_SMS, false);
                if (isSms) {
                    NotificationFactory.makeNotificationForSms(this, ActiveDatabases.getSmsDatabase(this).getMessageForThreadView(uri));
                } else {
                    NotificationFactory.makeNotificationForMms(this, ActiveDatabases.getMmsDatabase(this).getMessageForThreadView(uri));
                }
                break;
            case DELETE_NOTIFICAITON_MSS:

                break;
        }
        return Service.START_STICKY;
    }

    public static void startDeleteTask(Context context, Uri uri) {

        try {

            AlarmManager alarms = (AlarmManager) context
                    .getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(context,
                    MmsReceivedService.class);
            intent.putExtra(MmsReceivedService.DELETE_NOTIFICAITON_MSS,
                    MmsReceivedService.DELETE_NOTIFICAITON_MSS);
            intent.putExtra(DELETE_URI,uri);

            PendingIntent pIntent = PendingIntent.getBroadcast(context,
                    1234567, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarms.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+10000,pIntent);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
