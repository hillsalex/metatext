package com.hillsalex.metatext.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.hillsalex.metatext.senders.SmsSender;

/**
 * Created by alex on 11/5/2014.
 */
public class SendReceiveServices extends Service{


    public final static String RECEIVE_SMS_ACTION = "com.hillsalex.metatext.services.SendReceiveServices.RECEIVE_SMS_ACTION";
    public final static String SEND_SINGLE_SMS_ACTION = "com.hillsalex.metatext.services.SendReceiveServices.SEND_SINGLE_SMS_ACTION";
    public final static String SEND_GROUP_SMS_ACTION = "com.hillsalex.metatext.services.SendReceiveServices.SEND_GROUP_SMS_ACTION";


    private SmsReceiver      smsReceiver;
    private SmsSender        smsSender;

    @Override
    public void onCreate(){
        createProcessors();
    }

    @Override
    public void onStart(Intent intent, int startId) {

        handleStart(intent);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        handleStart(intent);
        return START_STICKY;
    }

    public void handleStart(Intent intent){
        if (intent == null) return;
        String action = intent.getAction();
        if (action == RECEIVE_SMS_ACTION){
            smsReceiver.process(intent);
        }
        if (action == SEND_SINGLE_SMS_ACTION){
            smsSender.handleSendMessageIntent(intent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createProcessors(){
        smsSender = new SmsSender(this);
        smsReceiver = new SmsReceiver(this);
    }

}
