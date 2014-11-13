package com.hillsalex.metatext.model;

/**
 * Created by alex on 11/10/2014.
 */
public class SmsMessageModel extends MessageModel {



    public SmsMessageModel(Long id, String recipient,long date,Long threadId, String body, boolean fromMe){
        super(id, new String[]{recipient},date, threadId,fromMe);
        this.body = body;
        this.date=date;
        if (!fromMe)
            this.senderContact = Contact.get(recipient,true);
    }

    @Override
    public String toString() {
        return body;
    }
}
