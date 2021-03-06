package com.hillsalex.metatext.database.apnutils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.util.Set;

/**
 * Created by alex on 11/5/2014.
 */
public abstract class ApnDatabaseSuper {

    protected final Context context;
    protected       SQLiteOpenHelper databaseHelper;

    protected static final String ID_WHERE            = "_id = ?";
    private static final String CONVERSATION_URI      = "content://textsecure/thread/";
    private static final String CONVERSATION_LIST_URI = "content://textsecure/conversation-list";

    public ApnDatabaseSuper(Context context, SQLiteOpenHelper databaseHelper) {
        this.context        = context;
        this.databaseHelper = databaseHelper;
    }


    protected void notifyConversationListeners(Set<Long> threadIds) {
        for (long threadId : threadIds)
            notifyConversationListeners(threadId);
    }

    protected void notifyConversationListeners(long threadId) {
        context.getContentResolver().notifyChange(Uri.parse(CONVERSATION_URI + threadId), null);
    }

    protected void notifyConversationListListeners() {
        context.getContentResolver().notifyChange(Uri.parse(CONVERSATION_LIST_URI), null);
    }

    protected void setNotifyConverationListeners(Cursor cursor, long threadId) {
        cursor.setNotificationUri(context.getContentResolver(), Uri.parse(CONVERSATION_URI + threadId));
    }

    protected void setNotifyConverationListListeners(Cursor cursor) {
        cursor.setNotificationUri(context.getContentResolver(), Uri.parse(CONVERSATION_LIST_URI));
    }
}
