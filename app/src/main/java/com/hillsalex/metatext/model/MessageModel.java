package com.hillsalex.metatext.model;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by alex on 11/10/2014.
 */
public abstract class MessageModel extends Model{

    public static final String FROM_ME_SENDER = "FROM ME";

    public long id;
    public Contact senderContact;
    public String[] recipients;
    public String body="";
    public long threadId;
    public long date;
    public ContactList contactList;
    public boolean fromMe;

    public MessageModel(long id, String[] recipients, long date, Long threadId,boolean fromMe){
        this.id=id;
        this.recipients=recipients;
        this.threadId=threadId;
        this.date=date;
        this.contactList = ContactList.getByNumbers(new ArrayList(Arrays.asList(recipients)), false);
        if (fromMe)
        {
            this.senderContact = Contact.getMe(false);
            this.fromMe = true;
        }
        else{
            this.fromMe = false;
        }
    }

    @Override
    public abstract String toString();
}
