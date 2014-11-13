package com.hillsalex.metatext.database;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.provider.Telephony;
import android.support.v4.util.Pair;
import android.util.Log;

import java.io.UnsupportedEncodingException;

import com.google.android.mms.pdu_alt.CharacterSets;
import com.google.android.mms.pdu_alt.DeliveryInd;
import com.google.android.mms.pdu_alt.EncodedStringValue;
import com.google.android.mms.pdu_alt.GenericPdu;
import com.google.android.mms.pdu_alt.NotificationInd;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.pdu_alt.ReadOrigInd;
import com.hillsalex.metatext.model.MessageModel;
import com.hillsalex.metatext.model.MmsMessageModel;

/**
 * Created by alex on 11/6/2014.
 */
public class MmsDatabase extends Database {

    private static final String TAG = "metatext.database.MmsDatabase";

    public MmsDatabase(Context context) {
        super(context);
    }

    public Cursor getThreadsAndDates(){
        final String[] projection = new String[]{"DISTINCT thread_id","date * 1000", "m_id", "_id", Telephony.Mms.MESSAGE_BOX};
        //final String selection = "thread_id IS NOT NULL) GROUP BY (thread_id";
        final String selection = null;
        return mContext.getContentResolver().query(Telephony.Mms.CONTENT_URI,projection,selection,null,null);
    }

    public Cursor getEntireThread(long threadID) {

        final String[] projection = new String[]{
                Telephony.Mms.THREAD_ID,
                Telephony.Mms._ID,
                Telephony.Mms.DATE + " * 1000",
                Telephony.Mms.MESSAGE_ID,
                Telephony.Mms.STATUS,
                Telephony.Mms.MESSAGE_SIZE,
                Telephony.Mms.MESSAGE_TYPE,
                Telephony.Mms.CONTENT_LOCATION,
                Telephony.Mms.MESSAGE_BOX,
                Telephony.Mms.EXPIRY
        };
        final String selection = "thread_id = " + threadID;
        return mContext.getContentResolver().query(Telephony.Mms.CONTENT_URI,projection,selection,null,null);
    }

    public Cursor getThreadPastDate(long threadID, long date){
        final String[] projection = new String[]{
                Telephony.Mms.THREAD_ID,
                Telephony.Mms._ID,
                Telephony.Mms.DATE + " * 1000",
                Telephony.Mms.MESSAGE_ID,
                Telephony.Mms.STATUS,
                Telephony.Mms.MESSAGE_SIZE,
                Telephony.Mms.MESSAGE_TYPE,
                Telephony.Mms.CONTENT_LOCATION,
                Telephony.Mms.MESSAGE_BOX,
                Telephony.Mms.EXPIRY
        };

        final String selection = "thread_id = " + threadID + " AND " + Telephony.Mms.DATE  + " > " + date;
        return mContext.getContentResolver().query(Telephony.Mms.CONTENT_URI,projection,selection,null,null);
    }
    public Cursor getThreadsAndDatesPastDate(long date){
        final String[] projection = new String[]{"DISTINCT thread_id","date * 1000", "m_id", "_id", Telephony.Mms.MESSAGE_BOX};
        final String selection = Telephony.Mms.DATE + " * 1000 > "+date;
        //final String selection = null;
        return mContext.getContentResolver().query(Telephony.Mms.CONTENT_URI,projection,selection,null,null);
    }

    public MmsMessageModel getMessageForThreadView(Uri uri){
        final String[] projection = new String[]{"thread_id","date * 1000", "m_id", "_id", Telephony.Mms.MESSAGE_BOX};
        Cursor query = mContext.getContentResolver().query(uri,projection,null,null,null);
        if (query!=null && query.moveToFirst()) {
            MmsMessageModel model = new MmsMessageModel(query.getLong(3),query.getLong(1),query.getLong(0),query.getInt(4)== Telephony.Mms.MESSAGE_BOX_SENT);
            query.close();
            return model;
        }
        else return null;
    }
/*
    public void populateMmsMessageModel(MmsMessageModel model){
        String selectionPart = "mid="+model.id;
        Uri uri = Uri.parse("content://mms/part");
        Cursor cursor = mContext.getContentResolver().query(uri,projection,selectionPart,null,null);
    }
*/

    private long getThreadIdFor(NotificationInd notification){
        try{
            EncodedStringValue encodedString = notification.getFrom();
            String fromString = new String(encodedString.getTextString(), CharacterSets.MIMENAME_ISO_8859_1);
            int bab=0;
            bab++;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        SqliteWrapper wrapper;
        return 0;
    }

    private static long findThreadId(Context context,GenericPdu pdu,int type){
        String messageId;
        if (type == PduHeaders.MESSAGE_TYPE_DELIVERY_IND) {
            messageId=new String(((DeliveryInd)pdu).getMessageId());
        }
        else {
            messageId=new String(((ReadOrigInd)pdu).getMessageId());
        }
        StringBuilder sb=new StringBuilder('(');
        sb.append(Telephony.Mms.MESSAGE_ID);
        sb.append('=');
        sb.append(DatabaseUtils.sqlEscapeString(messageId));
        sb.append(" AND ");
        sb.append(Telephony.Mms.MESSAGE_TYPE);
        sb.append('=');
        sb.append(PduHeaders.MESSAGE_TYPE_SEND_REQ);
        Cursor cursor= SqliteWrapper.query(context, context.getContentResolver(), Telephony.Mms.CONTENT_URI, new String[]{Telephony.Mms.THREAD_ID}, sb.toString(), null, null);
        if (cursor != null) {
            try {
                if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                    return cursor.getLong(0);
                }
            }
            finally {
                cursor.close();
            }
        }
        return -1;
    }
}
