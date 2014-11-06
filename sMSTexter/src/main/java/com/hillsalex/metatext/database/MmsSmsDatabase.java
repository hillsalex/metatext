package com.hillsalex.metatext.database;

import android.content.Context;
import android.database.Cursor;

/**
 * Created by alex on 11/6/2014.
 */
public class MmsSmsDatabase extends Database{

    public MmsSmsDatabase(Context context) {
        super(context);
    }

    public SortMultipleCursor getSortedThreads() throws SortMultipleCursor.InvalidMultiCursorArgumentsException {
        Cursor mmsCursor = ActiveDatabases.getMmsDatabase(mContext).getThreadsAndDates();
        Cursor smsCursor = ActiveDatabases.getSmsDatabase(mContext).getThreadsAndDates();
        int[] args = new int[]{1,1};

        return new SortMultipleCursor(
                new Cursor[]{mmsCursor,smsCursor},
                args,
                long.class,
                true
        );
    }
}
