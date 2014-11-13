package com.hillsalex.metatext;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.view.Menu;
import android.view.MenuItem;

import com.android.mms.MmsConfig;
import com.hillsalex.metatext.database.ActiveDatabases;
import com.hillsalex.metatext.model.Contact;
import com.hillsalex.metatext.model.ContactList;
import com.hillsalex.metatext.model.MessageModel;
import com.hillsalex.metatext.model.MmsMessageModel;


public class ThreadListViewActivity extends Activity {

    MmsSentHandler mmsSentHandler;
    SmsSentHandler smsSentHandler;

    ThreadListViewFragment fragment = null;
    public class ThreadClicked extends ThreadListViewFragment.ThreadClickedListener{

        Activity mActivity;
        public ThreadClicked(Activity activity){
            mActivity = activity;
        }
        @Override
        public void onClick(long threadId, ContactList contacts) {
            Intent detailIntent = new Intent(mActivity, ThreadDetailViewActivity.class);
            detailIntent.putExtra("threadId", threadId);
            detailIntent.putExtra("title",contacts.formatNames(";"));
            startActivity(detailIntent);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_list_view);
        fragment = new ThreadListViewFragment();
        fragment.setListener(new ThreadClicked(this));
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
        Contact.init(this);
        MmsConfig.init(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO: reload messages since new date
        registerReceiver(threadUpdatedReceiver,new IntentFilter(StaticMessageStrings.NOTIFY_MESSAGE_RECEIVED));
        mmsSentHandler = new MmsSentHandler(null,this);
        smsSentHandler = new SmsSentHandler(null,this);
        getContentResolver().registerContentObserver(Telephony.Mms.CONTENT_URI,true,mmsSentHandler);
        getContentResolver().registerContentObserver(Telephony.Sms.CONTENT_URI,true,smsSentHandler);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(threadUpdatedReceiver);
        getContentResolver().unregisterContentObserver(mmsSentHandler);
        getContentResolver().unregisterContentObserver(smsSentHandler);
        mmsSentHandler = null;
        smsSentHandler = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_thread_list_view, menu);
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


    private class SmsSentHandler extends ContentObserver{
        Context mContext;
        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public SmsSentHandler(Handler handler, Context context) {
            super(handler);
            mContext=context;
        }


        
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            fragment.RefreshConversations();
            /*
            MessageModel model = ActiveDatabases.getSmsDatabase(mContext).getMessageForThreadView(uri);
            if (model.fromMe) fragment.notifyNewMessage(uri,true);*/
        }
    }
    private class MmsSentHandler extends ContentObserver{
        Context mContext;
        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public MmsSentHandler(Handler handler, Context context) {
            super(handler);
            mContext=context;
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            fragment.RefreshConversations();
            /*MessageModel model = ActiveDatabases.getMmsDatabase(mContext).getMessageForThreadView(uri);
            if (model.fromMe) fragment.notifyNewMessage(uri,false);*/
        }
    }


}
