package com.hillsalex.metatext.database.models;

import com.hillsalex.metatext.database.models.MessageModel;
import com.hillsalex.metatext.messages.sms.OutgoingTextMessage;
import com.hillsalex.metatext.persons.Persons;

/**
 * Created by alex on 11/5/2014.
 */
public class SmsMessageModel extends MessageModel {

    public SmsMessageModel(String body, Persons persons){
        super(body,persons);
    }
    public SmsMessageModel(OutgoingTextMessage message){
        super(message.getMessageBody(),message.getPersons());
    }
    @Override
    public boolean isSMS(){ return true;}
}
