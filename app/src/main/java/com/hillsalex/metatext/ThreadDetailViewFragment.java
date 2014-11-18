package com.hillsalex.metatext;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hillsalex.metatext.database.ActiveDatabases;
import com.hillsalex.metatext.database.SortMultipleCursor;
import com.hillsalex.metatext.model.Contact;
import com.hillsalex.metatext.model.MessageModel;
import com.hillsalex.metatext.model.MmsMessageModel;
import com.hillsalex.metatext.model.SmsMessageModel;
import com.hillsalex.metatext.util.Logger;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;
import com.klinker.android.send_message.Utils;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 * Created by alex on 11/11/2014.
 */
public class ThreadDetailViewFragment extends Fragment {

    List<MessageModel> mMessages = new ArrayList<>();
    ThreadDetailViewAdapter adapter;
    long threadId;
    boolean titleSet = false;
    View mRootView;
    LinearLayoutManager layoutManager;
    Random mRandom = new Random(Calendar.getInstance().getTimeInMillis());

    RecyclerView recyclerView;

    ContentObserver smsSentByOtherProcessHandler;


    private final String TAG = "ThreadDetailViewFragment";

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(StaticMessageStrings.NOTIFY_MESSAGE_RECEIVED);
        filter.addAction(StaticMessageStrings.NOTIFY_SENDING_MESSAGE_PROGRESS);
        filter.addAction(StaticMessageStrings.NOTIFY_SENDING_MESSAGE_FAILED);
        filter.addAction(StaticMessageStrings.NOTIFY_SENDING_MESSAGE_SENT);


        smsSentByOtherProcessHandler = new SmsSentByOtherProcessHandler(null,getActivity());
        getActivity().getContentResolver().registerContentObserver(Telephony.Sms.CONTENT_URI,true,smsSentByOtherProcessHandler);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(threadUpdatedReceiver, filter);
        //mmsSentHandler = new MmsSentHandler(null,this);
        //smsSentHandler = new SmsSentHandler(null,this);
        //getContentResolver().registerContentObserver(Telephony.Mms.Outbox.CONTENT_URI,true,mmsSentHandler);
        //getContentResolver().registerContentObserver(Telephony.Sms.CONTENT_URI,true,smsSentHandler);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(threadUpdatedReceiver);
            getActivity().getContentResolver().unregisterContentObserver(smsSentByOtherProcessHandler);
            smsSentByOtherProcessHandler = null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //getContentResolver().unregisterContentObserver(mmsSentHandler);
        //getContentResolver().unregisterContentObserver(smsSentHandler);
        //mmsSentHandler = null;
        //smsSentHandler = null;
    }


    public ThreadDetailViewFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_thread_detail_view, container, false);
        mRootView = rootView;
        threadId = getArguments().getLong("threadId");
        if (getArguments().containsKey("titleSet")) {
            titleSet = getArguments().getBoolean("titleSet");
        }
        NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel((int) threadId);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.thread_detail_view_recycler_view);
        recyclerView.setHasFixedSize(false);
        layoutManager = new LinearLayoutManager(rootView.getContext(), LinearLayoutManager.VERTICAL, true);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ThreadDetailViewAdapter(new ArrayList<MessageModel>());
        recyclerView.setAdapter(adapter);

        ActiveDatabases.getMmsSmsDatabase(getActivity()).markThreadAsRead(threadId);

        LoadConvo();

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);

        ((ImageView) rootView.findViewById(R.id.attach_message_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Settings s = Utils.getDefaultSendSettings(getActivity());
                Transaction t = new Transaction(getActivity(), s);
                Message message;

                message = new Message("Test","4242424419", BitmapFactory.decodeResource(getResources(),R.drawable.abs__ic_search));
                Log.v("ThreadDetailView", "New message, tempId: " + t.getTempMessageId());
                t.sendNewMessage(message,0);
            }
        });

        ((ImageView) rootView.findViewById(R.id.send_message_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText textView = (EditText) mRootView.findViewById(R.id.message_to_send_text);
                if (textView != null) {
                    String toSend = textView.getText().toString();
                    textView.setText("");
                    if (toSend.equals("")) return;

                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm.isAcceptingText()) { // verify if the soft keyboard is open
                        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                    }
                    Log.v("TreadDetailViewFrag", toSend);

                    String[] addresses = adapter.getAddresses();
                    if (addresses != null && addresses.length > 0) {
                        Settings s = Utils.getDefaultSendSettings(getActivity());
                        Transaction t = new Transaction(getActivity(), s);
                        Message message;
                        int tempId = mRandom.nextInt(Integer.MAX_VALUE / 2);
                        if (addresses.length > 1) {
                            message = new Message(toSend, addresses);
                            message.setTempId(tempId);
                        } else {
                            message = new Message(toSend, addresses[0]);
                            message.setTempId(tempId);
                        }
                        message.setType(Message.TYPE_SMSMMS);
                        t.sendNewMessage(message, adapter.getThreadId());
                    }
                }
            }

        });

        //adapter.setElements(mMessages);
        return rootView;
    }


    private class LoaderTask extends AsyncTask<SortMultipleCursor, SortMultipleCursor, Pair<List<MessageModel>, SortMultipleCursor>> {

        private long startMils=0;

        @Override
        protected Pair<List<MessageModel>, SortMultipleCursor> doInBackground(SortMultipleCursor... params) {
            if (Logger.PERF_ENABLED){
                startMils = System.currentTimeMillis();
            }
            List<MessageModel> models = new ArrayList<>();
            SortMultipleCursor cursor = params[0];
            int count = 0;
            while (!cursor.isAfterLast() && count < 10) {

                try {
                    Pair<Cursor, Integer> nextItem = cursor.next();

                    if (nextItem.second == 0) {
                        Long date = nextItem.first.getLong(2);
                        MmsMessageModel mms = new MmsMessageModel(nextItem.first.getLong(1), date, threadId, nextItem.first.getInt(8) == Telephony.Mms.MESSAGE_BOX_SENT, nextItem.first.getInt(10) == 1);
                        models.add(mms);
                    } else {
                        Long id = nextItem.first.getLong(1);
                        String address = nextItem.first.getString(3);
                        String body = nextItem.first.getString(4);
                        Long date = nextItem.first.getLong(2);
                        boolean isMe = false;
                        if (SmsMessageModel.isFromMe(nextItem.first.getInt(6))) {
                            isMe = true;
                        }
                        SmsMessageModel sms = new SmsMessageModel(id, address, date, threadId, body, isMe, nextItem.first.getInt(8) == 1);
                        models.add(sms);
                    }
                    count++;
                } catch (SortMultipleCursor.MultiCursorAfterLastException e) {
                    e.printStackTrace();
                }

            }

            return new Pair<>(models, cursor);
        }

        @Override
        protected void onPostExecute(Pair<List<MessageModel>, SortMultipleCursor> models) {
            super.onPostExecute(models);

            if (Logger.PERF_ENABLED){
                Logger.LogPerf(TAG,"loading 10 messages from thread:" + threadId,System.currentTimeMillis() - startMils);
            }
            try {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new AddElementsRunnable(adapter, models.first));
                    if (!(models.first.size() < 10)) {
                        LoaderTask task = new LoaderTask();
                        task.execute(models.second);
                    } else {
                        models.second.closeAll();
                    }
                }
            } catch (NullPointerException e) {
                Log.d("DetailViewFragment", "Activity stopped before we could continue");
            }
        }
    }


    private class SmsSentByOtherProcessHandler extends ContentObserver {
        Context mContext;
        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public SmsSentByOtherProcessHandler(Handler handler, Context context) {
            super(handler);
            mContext=context;
        }



        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (uri!=null && !uri.getPath().equals("/raw")){
                notifyNewMessageFromOther(uri, true);
            }
        }
    }
    private BroadcastReceiver threadUpdatedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Uri uri;
            boolean isSms;

            switch (intent.getAction()) {
                case StaticMessageStrings.NOTIFY_MESSAGE_RECEIVED:
                    uri = intent.getParcelableExtra(StaticMessageStrings.MESSAGE_RECEIVED_URI);
                    boolean hasSms = intent.hasExtra(StaticMessageStrings.MESSAGE_IS_SMS);
                    if (uri == null || hasSms == false || uri.toString().equals("/raw")) return;
                    isSms = intent.getBooleanExtra(StaticMessageStrings.MESSAGE_IS_SMS, false);
                    notifyNewMessage(uri, isSms);
                    break;
                case StaticMessageStrings.NOTIFY_SENDING_MESSAGE_SENT:
                    int id = intent.getIntExtra(StaticMessageStrings.MESSAGE_INTERNAL_ID,-1);
                    isSms = intent.getBooleanExtra(StaticMessageStrings.MESSAGE_IS_SMS, false);
                    uri = intent.getParcelableExtra(StaticMessageStrings.MESSAGE_URI);
                    if (isSms && uri!=null && !uri.toString().equals("/raw")){
                        //notifyNewMessage(uri,true);
                    }
                    /*

                        Intent newIntent = new Intent(StaticMessageStrings.NOTIFY_SENDING_MESSAGE_SENT);
                        newIntent.putExtra(StaticMessageStrings.MESSAGE_INTERNAL_ID,intent.getIntExtra(StaticMessageStrings.MESSAGE_INTERNAL_ID,-10));
                        newIntent.putExtra(StaticMessageStrings.MESSAGE_URI,uri);
                        newIntent.putExtra(StaticMessageStrings.MESSAGE_IS_SMS,true);

                        LocalBroadcastManager.getInstance(context).sendBroadcast(newIntent);
                     */
                    //SmsMessageModel model = ActiveDatabases.getSmsDatabase(mInstance).getMessageForThreadView(uri);
                    break;
                case StaticMessageStrings.NOTIFY_SENDING_MESSAGE_FAILED:

                    break;
                case StaticMessageStrings.NOTIFY_SENDING_MESSAGE_PROGRESS:

                    break;
            }
        }
    };


    public class AddElementsToFrontAndScrollRunnable implements Runnable {
        MessageModel mModel;
        ThreadDetailViewAdapter self;

        public AddElementsToFrontAndScrollRunnable(ThreadDetailViewAdapter adapter, MessageModel model){
            mModel=model;
            self = adapter;
        }

        @Override
        public void run() {
            self.updateThread(mModel);
        }
    }

    public class AddElementsRunnable implements Runnable {
        List<MessageModel> mModels;
        ThreadDetailViewAdapter self;

        public AddElementsRunnable(ThreadDetailViewAdapter adapter, List<MessageModel> models) {
            self = adapter;
            mModels = models;
        }

        @Override
        public void run() {
            if (!titleSet) {
                titleSet = true;
                getActivity().setTitle(mModels.get(0).contactList.formatNames(";"));
            }
            self.addElements(mModels);
        }
    }

    public void notifyNewMessageFromOther(Uri uri, boolean isSms){
        if (!isSms) {
            MmsMessageModel model = ActiveDatabases.getMmsDatabase(getActivity()).getMessageForThreadView(uri);
            if (model.threadId == threadId && !model.fromMe) {
                ActiveDatabases.getMmsSmsDatabase(getActivity()).markThreadAsRead(threadId);
                getActivity().runOnUiThread(new AddElementsToFrontAndScrollRunnable(adapter,model));
            }
        } else {
            SmsMessageModel model = ActiveDatabases.getSmsDatabase(getActivity()).getMessageForThreadView(uri);
            if (model.threadId == threadId && !model.fromMe) {
                ActiveDatabases.getMmsSmsDatabase(getActivity()).markThreadAsRead(threadId);
                getActivity().runOnUiThread(new AddElementsToFrontAndScrollRunnable(adapter,model));
            }
        }
    }

    public void notifyNewMessage(Uri uri, boolean isSms) {
        if (!isSms) {
            MmsMessageModel model = ActiveDatabases.getMmsDatabase(getActivity()).getMessageForThreadView(uri);
            if (model!=null && model.threadId == threadId) {
                ActiveDatabases.getMmsSmsDatabase(getActivity()).markThreadAsRead(threadId);
                getActivity().runOnUiThread(new AddElementsToFrontAndScrollRunnable(adapter,model));
            }
        } else {
            SmsMessageModel model = ActiveDatabases.getSmsDatabase(getActivity()).getMessageForThreadView(uri);
            if (model != null && model.threadId == threadId) {
                ActiveDatabases.getMmsSmsDatabase(getActivity()).markThreadAsRead(threadId);
                getActivity().runOnUiThread(new AddElementsToFrontAndScrollRunnable(adapter,model));
            }
        }
    }

    public void LoadConvo() {

        try {
            Log.v("ThreadDetailVewFrag","New loader task created");
            LoaderTask task = new LoaderTask();
            SortMultipleCursor cursor = ActiveDatabases.getMmsSmsDatabase(getActivity()).getConversation(threadId);
            task.execute(cursor);
        } catch (SortMultipleCursor.InvalidMultiCursorArgumentsException e) {
            e.printStackTrace();
            return;
        }
        if (true)
            return;

        try {
            SortMultipleCursor cursor = ActiveDatabases.getMmsSmsDatabase(getActivity()).getConversation(threadId);
            while (!cursor.isAfterLast()) {
                Pair<Cursor, Integer> nextItem = cursor.next();
                if (nextItem.second == 0) {
                    Long date = nextItem.first.getLong(2);
                    MmsMessageModel mms = new MmsMessageModel(nextItem.first.getLong(1), date, threadId, nextItem.first.getInt(8) == Telephony.Mms.MESSAGE_BOX_SENT, nextItem.first.getInt(10) == 1);
                    mMessages.add(mms);
                } else {
                    Long id = nextItem.first.getLong(1);
                    String address = nextItem.first.getString(3);
                    String body = nextItem.first.getString(4);
                    Long date = nextItem.first.getLong(2);
                    boolean isMe = false;
                    if (SmsMessageModel.isFromMe(nextItem.first.getInt(6))) {
                        isMe = true;
                    }
                    SmsMessageModel sms = new SmsMessageModel(id, address, date, threadId, body, isMe, nextItem.first.getInt(10) == 8);
                    mMessages.add(sms);
                }
            }
            cursor.closeAll();
        } catch (SortMultipleCursor.InvalidMultiCursorArgumentsException e) {
            e.printStackTrace();
        } catch (SortMultipleCursor.MultiCursorAfterLastException e) {
            e.printStackTrace();
        }
    }

    public class ThreadDetailViewAdapter extends RecyclerView.Adapter<ThreadDetailViewAdapter.ViewHolder> {
        private List<MessageModel> mDataset;



        public String[] getAddresses() {
            if (mDataset.size() > 0) {
                return mDataset.get(0).contactList.getNumbers(true);
            }
            return null;
        }

        public long getThreadId() {
            if (mDataset.size() > 0) {
                return mDataset.get(0).threadId;
            }
            return 0;
        }

        private void deleteMessage(MessageModel model){
            if (model instanceof MmsMessageModel){
                ActiveDatabases.getMmsDatabase(getActivity()).deleteMessage(model);
            }
            else{
                ActiveDatabases.getSmsDatabase(getActivity()).deleteMessage(model);
            }
        }

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
            // each data item is just a string in this case
            public View mRootView;


            public ViewHolder(View v) {
                super(v);
                mRootView = v;
                mRootView.setOnLongClickListener(this);
            }

            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Delete entry")
                        .setMessage("Are you sure you want to delete this entry?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                deleteMessage(mDataset.get(getPosition()));
                                notifyItemRemoved(getPosition());
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                return true;
            }
        }


        public void updateThread(MessageModel model) {
            int i = 0;
            for (MessageModel m : mDataset) {
                if (m.id == model.id)
                    return;
                if (m.date < model.date)
                    break;
                i++;
            }

            boolean isScrolled = !(layoutManager.findFirstVisibleItemPosition() == 0);
            mDataset.add(i, model);
            notifyItemInserted(i);

            if (!isScrolled) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        layoutManager.scrollToPosition(0);
                    }
                });
            }
        }

        public void setElements(List<MessageModel> toSet) {
            mDataset = toSet;
            notifyDataSetChanged();
        }

        public void addElements(List<MessageModel> toAdd) {
            int totalSize = mDataset.size();
            mDataset.addAll(toAdd);
            notifyItemRangeInserted(totalSize, toAdd.size());
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public ThreadDetailViewAdapter(List<MessageModel> myDataset) {
            mDataset = myDataset;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public ThreadDetailViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                     int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.thread_detail_view_holder, parent, false);
            // set the view's size, margins, paddings and layout parameters


            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            MessageModel model = mDataset.get(position);

            //SET NAMES
            String text = "";
            //text = model.contactList.formatNames(";");
            if (model.senderContact!=null) {
                text = model.senderContact.getName();
            }
            //if (model.fromMe == true) text = "Me";
            ((TextView) holder.mRootView.findViewById(R.id.message_from_text)).setText(text);


            //SET BODY
            String body = "";
            if (model instanceof SmsMessageModel) {
                body = model.body;
            } else if (model.body.equals("")) {
                body = "MMS no text";
            } else {
                body = model.body;
            }
            ((TextView) holder.mRootView.findViewById(R.id.message_body_text)).setText(body);

            //SET IMAGE
            ImageView messageContentImageView = (ImageView) holder.mRootView.findViewById(R.id.message_image);
            if (model instanceof MmsMessageModel) {
                MmsMessageModel mmsModel = (MmsMessageModel) model;

                if (mmsModel.mImageUris.size() > 0) {
                    Picasso.with(getActivity()).load(mmsModel.mImageUris.get(0)).into(messageContentImageView);
                    messageContentImageView.setVisibility(View.VISIBLE);
                } else {
                    Picasso.with(getActivity()).cancelRequest(messageContentImageView);
                    messageContentImageView.setImageURI(null);
                    messageContentImageView.setImageBitmap(null);
                    messageContentImageView.setImageDrawable(null);
                    messageContentImageView.setVisibility(View.GONE);
                }
            } else {
                Picasso.with(getActivity()).cancelRequest(messageContentImageView);
                messageContentImageView.setImageURI(null);
                messageContentImageView.setImageBitmap(null);
                messageContentImageView.setImageDrawable(null);
                messageContentImageView.setVisibility(View.GONE);
            }

            //SET DATE
            String date = "";
            long msgTime = model.date;
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat tf = android.text.format.DateFormat.getTimeFormat(getActivity());
            date = df.format(msgTime) + " - " + tf.format(msgTime);
            ((TextView) holder.mRootView.findViewById(R.id.message_date_text)).setText(date);

            View rightImage = holder.mRootView.findViewById(R.id.contact_pic_right);
            View leftImage = holder.mRootView.findViewById(R.id.contact_pic_left);

            ImageView img = ((ImageView) rightImage);
            ImageView otherImg = ((ImageView) leftImage);
            int height = img.getLayoutParams().height;
            if (height == 0) height = otherImg.getLayoutParams().height;

            //SET PICS
            if (model.fromMe) {
                holder.mRootView.findViewById(R.id.message_content_holder).setBackground(getActivity().getResources().getDrawable(R.color.amber500));
                Contact me = Contact.getMe(false);
                img.setVisibility(View.VISIBLE);
                img.setImageDrawable(me.getAvatar(getActivity(), getActivity().getResources().getDrawable(R.color.teal500)));
                img.getLayoutParams().height = height;
                otherImg.setVisibility(View.INVISIBLE);
                otherImg.getLayoutParams().height = 0;
            } else {
                img = ((ImageView) leftImage);
                otherImg = ((ImageView) rightImage);
                holder.mRootView.findViewById(R.id.message_content_holder).setBackground(getActivity().getResources().getDrawable(R.color.teal500));
                if (model.contactList.size() >= 1) {
                    img.setVisibility(View.VISIBLE);
                    img.setImageDrawable(model.contactList.get(0).getAvatar(getActivity(), getActivity().getResources().getDrawable(R.color.teal500)));
                }
                img.getLayoutParams().height = height;
                otherImg.setVisibility(View.INVISIBLE);
                otherImg.getLayoutParams().height = 0;
            }

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }

}


