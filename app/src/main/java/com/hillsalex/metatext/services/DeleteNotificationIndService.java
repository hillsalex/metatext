package com.hillsalex.metatext.services;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;

import com.hillsalex.metatext.database.Database;

/**
 * Created by alex on 11/18/2014.
 */
public class DeleteNotificationIndService extends IntentService {

    public final static String DELETE_NOTIFICATION_IND_ACTION = "metatext.DELETE_NOTIFICATION_IND_ACTION";
    public final static String DELETE_NOTIFICATION_IND_URI = "metatext.DELETE_NOTIFICATION_IND_URI";

    public DeleteNotificationIndService() {
        super("DeleteNotificationIndThread");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        Uri toDeleteUri;
        switch (action){
            case DELETE_NOTIFICATION_IND_ACTION:
                toDeleteUri = intent.getParcelableExtra(DELETE_NOTIFICATION_IND_URI);
                Database.deleteUriInBackground(getApplicationContext(), toDeleteUri);
                break;
            default:
                break;
        }

    }
}
