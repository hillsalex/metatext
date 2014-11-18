package com.hillsalex.metatext.database;

import android.content.ContentProviderClient;
import android.content.Context;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by alex on 11/10/2014.
 */
public class Database {
    protected Context mContext;

    public Database(Context context){
        mContext=context;
    }

    public static void deleteUriInBackground(Context context, Uri uri){
        new Thread(new DeleteTask(context,uri)).start();
    }

    public static class DeleteTask implements Runnable{

        private Context context;
        private Uri uri;
        public DeleteTask(Context context, Uri uri){
            this.context=context;
            this.uri=uri;
        }
        @Override
        public void run() {
            ContentProviderClient client = context.getContentResolver().acquireContentProviderClient(uri);
            try {
                client.delete(uri, null, null);
                Log.d("Database", "Finished delete of: " + uri.toString());
            } catch (RemoteException e) {
                Log.e("Database", "Ending delete with EXCEPTION ");
                e.printStackTrace();
            }
            if (client!=null)
                client.release();
        }
    }

    public static class DeleteFromDatabaseUsingUriTask extends AsyncTask{

        Context context;
        Uri uri;
        public DeleteFromDatabaseUsingUriTask(Context context, Uri uri){
            this.context=context;
            this.uri=uri;
        }

        @Override
        protected Object doInBackground(Object[] params) {

            android.util.Log.d("DATABASE","Start delete time: "+ DateFormat.getDateTimeInstance().format(new Date()));
            SqliteWrapper.delete(context, context.getContentResolver(),
                    uri, null, null);
            android.util.Log.d("DATABASE","End delete time: "+ DateFormat.getDateTimeInstance().format(new Date()));
            return null;
        }
    }
}
