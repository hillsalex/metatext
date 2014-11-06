package com.hillsalex.metatext.database;

import android.content.Context;
import android.database.Cursor;
import android.provider.Telephony;

/**
 * Created by alex on 11/6/2014.
 */
public class MmsDatabase extends Database {


    public MmsDatabase(Context context) {
        super(context);
    }

    public Cursor getThreadsAndDates(){
        final String[] projection = new String[]{"DISTINCT thread_id","date * 1000", "m_id", "_id"};
        final String selection = "thread_id IS NOT NULL) GROUP BY (thread_id";
        return mContext.getContentResolver().query(Telephony.Mms.CONTENT_URI,projection,selection,null,null);
    }
}
