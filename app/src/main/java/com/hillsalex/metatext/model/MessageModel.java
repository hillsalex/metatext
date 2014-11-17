package com.hillsalex.metatext.model;

import android.content.Context;
import android.support.v4.util.Pair;

import com.hillsalex.metatext.database.ActiveDatabases;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by alex on 11/10/2014.
 */
public abstract class MessageModel extends Model{

    public static final String FROM_ME_SENDER = "FROM ME";

    public static final int STATUS_SENDING = 0;
    public static final int STATUS_SENT = 0;
    public static final int STATUS_RECEIVED = 0;
    public static final int STATUS_FAILED = 0;
    public static final int STATUS_DOWNLOADING = 0;

    public long id;
    public Contact senderContact;
    public String[] recipients;
    public String body="";
    public long threadId;
    public long date;
    public ContactList contactList;
    public boolean fromMe;
    public boolean read;
    public int status;
    public boolean isFake=false;
    private boolean threadReadGotten;
    private Pair<Integer,Integer> threadRead;


    public MessageModel(long id, String[] recipients, long date, Long threadId,boolean fromMe, boolean read){
        this.id=id;
        this.recipients=recipients;
        this.threadId=threadId;
        this.date=date;
        this.read=read;
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

    public Pair<Integer,Integer> forceRefreshUnreadCount(Context context){
        threadRead = ActiveDatabases.getMmsSmsDatabase(context).getUnreadAndTotalInThread(threadId);
        threadReadGotten=true;
        return threadRead;
    }
    public Pair<Integer,Integer> getCachedUnreadCount(Context context){
        if (!threadReadGotten){
            threadRead = ActiveDatabases.getMmsSmsDatabase(context).getUnreadAndTotalInThread(threadId);
            threadReadGotten=true;
        }
        return threadRead;
    }

    @Override
    public abstract String toString();
}
