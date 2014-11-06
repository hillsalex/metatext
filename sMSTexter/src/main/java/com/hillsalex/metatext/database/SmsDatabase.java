package com.hillsalex.metatext.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorJoiner;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.Telephony;
import android.support.v4.util.Pair;
import android.util.Log;

import com.hillsalex.metatext.messages.sms.IncomingTextMessage;
import com.hillsalex.metatext.persons.Person;
import com.hillsalex.metatext.persons.Persons;

import hills.sms.texter.util.Util;

/**
 * Created by alex on 11/6/2014.
 */
public class SmsDatabase extends Database {

    public SmsDatabase(Context context){super(context);}

    public Cursor getThreadsAndDates(){
        final String[] projection = new String[]{"DISTINCT thread_id","date", "_id"};
        final String selection = "thread_id IS NOT NULL) GROUP BY (thread_id";
        return mContext.getContentResolver().query(Telephony.Sms.CONTENT_URI,projection,selection,null,null);
    }

}
