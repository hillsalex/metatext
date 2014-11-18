package com.hillsalex.metatext.receivers;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.provider.Telephony;

import com.hillsalex.metatext.services.SmsReceiverService;

/**
 * Created by alex on 11/10/2014.
 */
public class SmsReceiver extends BroadcastReceiver {
    static final Object mStartingServiceSync = new Object();
    static PowerManager.WakeLock mStartingService;
    private static SmsReceiver sInstance;

    public static SmsReceiver getInstance() {
        if (sInstance == null) {
            sInstance = new SmsReceiver();
        }
        return sInstance;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        onReceiveWithPrivilege(context, intent, false);
    }
    protected void onReceiveWithPrivilege(Context context, Intent intent, boolean privileged) {
        // If 'privileged' is false, it means that the intent was delivered to the base
        // no-permissions receiver class.  If we get an SMS_RECEIVED message that way, it
        // means someone has tried to spoof the message by delivering it outside the normal
        // permission-checked route, so we just ignore it.
        if (!privileged && intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            return;
        }

        intent.setClass(context, SmsReceiverService.class);
        intent.putExtra("result", getResultCode());
        beginStartingService(context, intent);
    }

    /**
     * Start the service to process the current event notifications, acquiring
     * the wake lock before returning to ensure that the service will run.
     */
    public static void beginStartingService(Context context, Intent intent) {
        synchronized (mStartingServiceSync) {
            if (mStartingService == null) {
                PowerManager pm =
                        (PowerManager)context.getSystemService(Context.POWER_SERVICE);
                mStartingService = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "StartingAlertService");
                mStartingService.setReferenceCounted(false);
            }
            mStartingService.acquire();
            context.startService(intent);
        }
    }

    /**
     * Called back by the service when it has finished processing notifications,
     * releasing the wake lock if the service is now stopping.
     */
    public static void finishStartingService(Service service, int startId) {
        synchronized (mStartingServiceSync) {
            if (mStartingService != null) {
                if (service.stopSelfResult(startId)) {
                    mStartingService.release();
                }
            }
        }
    }
}
