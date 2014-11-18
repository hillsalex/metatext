package com.hillsalex.metatext;

import android.app.Activity;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.text.SimpleDateFormat;
import java.util.Date;


public class TestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test, menu);
        return true;
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy, MM, dd - HH:mm:ss");

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        ContentResolver resolver = this.getContentResolver();
        long startTime = SystemClock.uptimeMillis();
        Log.d("TEST", "Starting delete");

        Log.d("TEST", "Starting tests at: " + sdf.format(new Date()));

        Uri toDelete = Uri.parse("content://mms/1005");
        ContentProviderClient client = resolver.acquireContentProviderClient(toDelete);
        int count = 0;

        Log.d("TEST", "Starting delete at: " + sdf.format(new Date()));
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentProviderClient client = getContentResolver().acquireContentProviderClient(Uri.parse("content://mms/1005"));
                int count = 0;
                try {
                    count = client.delete(Uri.parse("content://mms/1007"), null, null);
                } catch (RemoteException e) {
                    Log.e("TEST", "Ending delete with EXCEPTION delete at: " + sdf.format(new Date()));
                    e.printStackTrace();
                }
                Log.d("TEST", "Ending delete at: " + sdf.format(new Date()) + ", deleted: " + count);
                client.release();
            }
        }).start();

        try {
            Log.d("TEST", "Starting query at: " + sdf.format(new Date()));
            Cursor cursor = client.query(Uri.parse("content://mms"), new String[]{"thread_id", "_id"}, "thread_id > 100", null, null);

            if (cursor != null && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    Log.d("TEST", "thread_id: " + cursor.getLong(0) + " id: " + cursor.getLong(1));
                    cursor.moveToNext();
                }
            } else {
                Log.d("TEST", "cursor is totally fucked, dude.");
            }
            if (cursor != null)
                cursor.close();

            Log.d("TEST", "Ending query at: " + sdf.format(new Date()));

        } catch (RemoteException e) {
            Log.e("TEST", "Ending query WITH EXCEPTION at: " + sdf.format(new Date()));
            e.printStackTrace();
        }
        client.release();
        Log.d("TEST", "Ending tests at: " + sdf.format(new Date()));

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
