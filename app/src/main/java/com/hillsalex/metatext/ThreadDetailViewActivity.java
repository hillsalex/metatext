package com.hillsalex.metatext;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.hillsalex.metatext.database.ActiveDatabases;
import com.hillsalex.metatext.model.SmsMessageModel;
import com.klinker.android.send_message.Transaction;


public class ThreadDetailViewActivity extends Activity {

    ThreadDetailViewActivity mInstance;
    ThreadDetailViewFragment fragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInstance = this;
        setContentView(R.layout.activity_thread_detail_view);

        Bundle arguments = new Bundle();


        String titleString = getIntent().getStringExtra("title");
        if (titleString != null) {
            setTitle(titleString);
            arguments.putBoolean("titleSet", true);
        } else {
            setTitle("");
            arguments.putBoolean("titleSet", false);
        }
        fragment = new ThreadDetailViewFragment();

        arguments.putLong("threadId", getIntent().getLongExtra("threadId", 0));
        arguments.putBoolean("cameFromNotification", getIntent().getBooleanExtra("cameFromNotification", false));


        fragment.setArguments(arguments);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_thread_detail_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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


    /*

    TODO create new actions for sentMessage/progressMessage/failedMessage
    TODO edit DB and delete two failure threads, put a breakpoint at the null cursor in getcontact or something like that

    */

}
