package com.hillsalex.metatext.senders;

import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

import com.hillsalex.metatext.messages.sms.OutgoingTextMessage;
import com.hillsalex.metatext.database.models.SmsMessageModel;

import java.util.ArrayList;

/**
 * Created by alex on 11/5/2014.
 */
public class SmsSender {

    private String TAG = "SmsSender";

    private final Context context;

    public SmsSender(Context context) {
        this.context = context;
    }

    public void handleSendMessageIntent(Intent intent) {
        if (intent==null) return;
        OutgoingTextMessage message = intent.getParcelableExtra("text_message");
        handleSendMessage(new SmsMessageModel(message));
    }

    public void handleSendMessage(SmsMessageModel message) {

        ArrayList<String> messages = null;
        String number = message.getSolePerson().getPhoneNumber();
        messages = SmsManager.getDefault().divideMessage(message.getBody());
        //TODO:Intents

        try{
            SmsManager.getDefault().sendMultipartTextMessage(number,null,messages,null,null);
        }
        catch(NullPointerException e){
            Log.e(TAG,e.getMessage(),e);
            Log.e(TAG,"Recipient: " + number);
            Log.e(TAG,"Message Parts: " + messages.size());
            //TODO: Try sending as individual messages
        }

    }


}
