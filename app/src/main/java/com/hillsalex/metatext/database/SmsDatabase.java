package com.hillsalex.metatext.database;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.util.Log;

import com.hillsalex.metatext.model.MessageModel;
import com.hillsalex.metatext.model.SmsMessageModel;

/**
 * Created by alex on 11/6/2014.
 */
public class SmsDatabase extends Database {

    public SmsDatabase(Context context){super(context);}

    public Cursor getThreadsAndDates(){
        final String[] projection = new String[]{"DISTINCT thread_id","date", "_id","address","type","body"};
        //final String selection = "thread_id IS NOT NULL) GROUP BY (thread_id";
        final String selection = null;
        return mContext.getContentResolver().query(Telephony.Sms.CONTENT_URI,projection,selection,null,null);
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
                Telephony.Sms.SUBJECT};
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
                Telephony.Sms.SUBJECT};
        final String selection = "thread_id = " + threadID + " AND " + Telephony.Sms.DATE  + " > " + date;
        return mContext.getContentResolver().query(Telephony.Mms.CONTENT_URI,projection,selection,null,null);
    }

    public Cursor getThreadsAndDatesPastDate(long date){
        final String[] projection = new String[]{"DISTINCT thread_id","date", "_id","address","type","body"};
        String selection = Telephony.Sms.DATE + ">" +date;
        //final String selection = null;
        return mContext.getContentResolver().query(Telephony.Sms.CONTENT_URI,projection,selection,null,null);
    }


    public SmsMessageModel getMessageForThreadView(Uri uri) {
        final String[] projection = new String[]{"thread_id","date", "_id","address","type","body"};
        try {
            Cursor query = mContext.getContentResolver().query(uri, projection, null, null, null);
            if (query != null && query.moveToFirst()) {
                SmsMessageModel model = new SmsMessageModel(query.getLong(2),
                        query.getString(3),
                        query.getLong(1),
                        query.getLong(0),
                        query.getString(5),
                        query.getInt(4) == 2);
                query.close();
                return model;

            }
            else return null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.e("SmsDatabase","Bad things happened. Tried to get a message with URI: " + uri.getPath());
            return null;
        }

    }
}
