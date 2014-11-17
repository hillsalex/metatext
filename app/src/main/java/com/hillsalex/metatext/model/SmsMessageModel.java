package com.hillsalex.metatext.model;

import android.provider.Telephony;

/**
 * Created by alex on 11/10/2014.
 */
public class SmsMessageModel extends MessageModel {



    public SmsMessageModel(Long id, String recipient,long date,Long threadId, String body, boolean fromMe, boolean read){
        super(id, new String[]{recipient},date, threadId,fromMe, read);
        this.body = body;
        this.date=date;
        if (!fromMe)
            this.senderContact = Contact.get(recipient,true);
    }

    @Override
    public String toString() {
        return body;
    }


    public static boolean isFromMe(int type){
        return (type != Telephony.Sms.MESSAGE_TYPE_INBOX);
    }
}
