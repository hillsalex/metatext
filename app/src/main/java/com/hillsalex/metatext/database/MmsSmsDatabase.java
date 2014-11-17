package com.hillsalex.metatext.database;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.util.Pair;

/**
 * Created by alex on 11/6/2014.
 */
public class MmsSmsDatabase extends Database {

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

    public Pair<Integer, Integer> getUnreadAndTotalInThread(long threaId){
        Pair<Integer,Integer> p1 = ActiveDatabases.getMmsDatabase(mContext).getUnreadAndTotalInThread(threaId);
        Pair<Integer,Integer> p2 = ActiveDatabases.getSmsDatabase(mContext).getUnreadAndTotalInThread(threaId);
        return new Pair<>(p1.first+p2.first,p1.second+p2.second);
    }

    public SortMultipleCursor getThreadsPastDate(long date) throws SortMultipleCursor.InvalidMultiCursorArgumentsException{
        Cursor mmsCursor = ActiveDatabases.getMmsDatabase(mContext).getThreadsAndDatesPastDate(date);
        Cursor smsCursor = ActiveDatabases.getSmsDatabase(mContext).getThreadsAndDatesPastDate(date);
        int[] args = new int[]{1,1};

        return new SortMultipleCursor(
                new Cursor[]{mmsCursor,smsCursor},
                args,
                long.class,
                true
        );
    }

    public SortMultipleCursor getConversation(long threadID) throws SortMultipleCursor.InvalidMultiCursorArgumentsException {
        Cursor mmsCursor = ActiveDatabases.getMmsDatabase(mContext).getEntireThread(threadID);
        Cursor smsCursor = ActiveDatabases.getSmsDatabase(mContext).getEntireThread(threadID);
        int[] args = new int[]{2,2};
        return new SortMultipleCursor(
                new Cursor[]{mmsCursor,smsCursor},
                args,
                long.class,
                true
        );
    }

    public void markThreadAsRead(long threadID){
        ActiveDatabases.getMmsDatabase(mContext).markThreadAsRead(threadID);
        ActiveDatabases.getSmsDatabase(mContext).markThreadAsRead(threadID);
    }

    public SortMultipleCursor getConversationPastDate(long threadID, long date) throws SortMultipleCursor.InvalidMultiCursorArgumentsException{
        Cursor mmsCursor = ActiveDatabases.getMmsDatabase(mContext).getThreadPastDate(threadID,date);
        Cursor smsCursor = ActiveDatabases.getSmsDatabase(mContext).getThreadPastDate(threadID,date);

        return null;
    }
}
