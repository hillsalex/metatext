package com.hillsalex.metatext.database;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.support.v4.util.Pair;
import android.util.Log;

import com.hillsalex.metatext.model.MessageModel;
import com.hillsalex.metatext.model.MmsMessageModel;
import com.hillsalex.metatext.model.SmsMessageModel;

import java.util.ArrayList;

/**
 * Created by alex on 11/6/2014.
 */
public class SmsDatabase extends Database {

    public SmsDatabase(Context context){super(context);}

    public Cursor getThreadsAndDates(){
        final String[] projection = new String[]{"DISTINCT thread_id","date", "_id","address","type","body","read"};
        //final String selection = "thread_id IS NOT NULL) GROUP BY (thread_id";
        final String selection = null;
        return mContext.getContentResolver().query(Telephony.Sms.CONTENT_URI,projection,selection,null,null);
    }

    public void deleteMessage(MessageModel model){
        if (!(model instanceof SmsMessageModel) || model.isFake){
            return;
        }
        mContext.getContentResolver().delete(Telephony.Sms.CONTENT_URI, "_id="+model.id,null);
    }

    public void markThreadAsRead(long threadID){
        ContentValues values = new ContentValues();
        values.put("read",1);
        mContext.getContentResolver().update(Telephony.Sms.CONTENT_URI,values,"read=0 AND thread_id = "+threadID,null);
    }

    public Pair<Integer, Integer> getUnreadAndTotalInThread(long threadId){

        final String[] projection = new String[]{"count(*) AS count"};
        String selection = "thread_id = " + threadId;

        Cursor countAllCursor = mContext.getContentResolver().query(Telephony.Sms.CONTENT_URI,
                projection,
                selection,
                null,
                null);
        countAllCursor.moveToFirst();

        String unreadSelection = "read = 0 AND thread_id = " + threadId;

        Cursor countUnreadCursor = mContext.getContentResolver().query(Telephony.Sms.CONTENT_URI,
                projection,
                unreadSelection,
                null,
                null);
        countUnreadCursor.moveToFirst();

        Pair<Integer,Integer> toReturn = new Pair<>(countUnreadCursor.getInt(0),countAllCursor.getInt(0));
        countAllCursor.close();
        countUnreadCursor.close();

        return toReturn;
    }

    public Cursor getEntireThread(long threadID) {
        final String[] projection = new String[]{
                Telephony.Sms.THREAD_ID,
                Telephony.Sms._ID,
                Telephony.Sms.DATE,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.STATUS,
                Telephony.Sms.TYPE,
                Telephony.Sms.SUBJECT,
                Telephony.Sms.READ};
        final String selection = "thread_id = " + threadID;
        return mContext.getContentResolver().query(Telephony.Sms.CONTENT_URI,projection,selection,null,null);
    }

    public Cursor getThreadPastDate(long threadID, long date){
        final String[] projection = new String[]{
                Telephony.Sms.THREAD_ID,
                Telephony.Sms._ID,
                Telephony.Sms.DATE,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.STATUS,
                Telephony.Sms.TYPE,
                Telephony.Sms.SUBJECT,
                Telephony.Sms.READ};
        final String selection = "thread_id = " + threadID + " AND " + Telephony.Sms.DATE  + " > " + date;
        return mContext.getContentResolver().query(Telephony.Mms.CONTENT_URI,projection,selection,null,null);
    }

    public Cursor getThreadsAndDatesPastDate(long date){
        final String[] projection = new String[]{"DISTINCT thread_id","date", "_id","address","type","body","read"};
        String selection = Telephony.Sms.DATE + ">" +date;
        //final String selection = null;
        return mContext.getContentResolver().query(Telephony.Sms.CONTENT_URI,projection,selection,null,null);
    }



    public SmsMessageModel getMessageForThreadView(Uri uri) {
        final String[] projection = new String[]{"thread_id","date", "_id","address","type","body","read"};
        try {
            Cursor query = mContext.getContentResolver().query(uri, projection, null, null, null);
            if (query != null && query.moveToFirst()) {

                SmsMessageModel model = new SmsMessageModel(query.getLong(2),
                        query.getString(3),
                        query.getLong(1),
                        query.getLong(0),
                        query.getString(5),
                        SmsMessageModel.isFromMe(query.getInt(4)),
                        query.getInt(6)==1);
                query.close();
                return model;

            }
            else return null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.e("SmsDatabase","Bad things happened. Tried to get a message with URI: " + uri.toString());
            return null;
        }
    }
}
