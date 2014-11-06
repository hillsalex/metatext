package com.hillsalex.metatext.database;

import android.content.Context;

/**
 * Created by alex on 11/6/2014.
 */
public class ActiveDatabases {
    private static final Object lock             = new Object();
    private static ActiveDatabases instance;


    /*
    DATABASES
     */
    private SmsDatabase mSmsDatabase;
    private MmsDatabase mMmsDatabase;
    private MmsSmsDatabase mMmsSmsDatabase;


    public static ActiveDatabases getInstance(Context context){
        synchronized (lock){
            if (instance==null)
                instance = new ActiveDatabases(context);
            return instance;
        }
    }

    public static SmsDatabase getSmsDatabase(Context context){
        return getInstance(context).mSmsDatabase;
    }
    public static MmsDatabase getMmsDatabase(Context context){
        return getInstance(context).mMmsDatabase;
    }
    public static MmsSmsDatabase getMmsSmsDatabase(Context context){
        return getInstance(context).mMmsSmsDatabase;
    }


    private ActiveDatabases(Context context){
        this.mSmsDatabase = new SmsDatabase(context);
        this.mMmsDatabase = new MmsDatabase(context);
        this.mMmsSmsDatabase = new MmsSmsDatabase(context);
    }
}
