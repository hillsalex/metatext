package com.hillsalex.metatext;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.hillsalex.metatext.database.ActiveDatabases;
import com.hillsalex.metatext.model.MessageModel;
import com.hillsalex.metatext.model.MmsMessageModel;
import com.hillsalex.metatext.model.SmsMessageModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class ThreadDetailViewActivity extends Activity {

//    MmsSentHandler mmsSentHandler;
//    SmsSentHandler smsSentHandler;
    ThreadDetailViewActivity mInstance;
    ThreadDetailViewFragment fragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInstance = this;
        setContentView(R.layout.activity_thread_detail_view);

        Bundle arguments = new Bundle();


        String titleString = getIntent().getStringExtra("title");
        if (titleString!=null){
            setTitle(titleString);
            arguments.putBoolean("titleSet",true);
        }
        else{
            setTitle("");
            arguments.putBoolean("titleSet",false);
        }
        fragment  = new ThreadDetailViewFragment();

        arguments.putLong("threadId",getIntent().getLongExtra("threadId", 0));
        arguments.putBoolean("cameFromNotification",getIntent().getBooleanExtra("cameFromNotification",false));


        fragment.setArguments(arguments);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(threadUpdatedReceiver,new IntentFilter(StaticMessageStrings.NOTIFY_MESSAGE_RECEIVED));
//        mmsSentHandler = new MmsSentHandler(null,this);
//        smsSentHandler = new SmsSentHandler(null,this);
//        getContentResolver().registerContentObserver(Telephony.Mms.CONTENT_URI,true,mmsSentHandler);
//        getContentResolver().registerContentObserver(Telephony.Sms.CONTENT_URI,true,smsSentHandler);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(threadUpdatedReceiver);
//        getContentResolver().unregisterContentObserver(mmsSentHandler);
//        getContentResolver().unregisterContentObserver(smsSentHandler);
//        mmsSentHandler = null;
//        smsSentHandler = null;
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
    private BroadcastReceiver threadUpdatedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Uri uri = intent.getParcelableExtra(StaticMessageStrings.MESSAGE_RECEIVED_URI);
            boolean hasSms = intent.hasExtra(StaticMessageStrings.MESSAGE_RECEIVED_IS_SMS);
            if (uri==null || hasSms == false) return;
            boolean isSms = intent.getBooleanExtra(StaticMessageStrings.MESSAGE_RECEIVED_IS_SMS,false);
            fragment.notifyNewMessage(uri,isSms);
        }
    };
}