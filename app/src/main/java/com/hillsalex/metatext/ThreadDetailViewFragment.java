package com.hillsalex.metatext;

import android.app.Fragment;
import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.mms.transaction.TransactionSettings;
import com.hillsalex.metatext.database.ActiveDatabases;
import com.hillsalex.metatext.database.SortMultipleCursor;
import com.hillsalex.metatext.model.Contact;
import com.hillsalex.metatext.model.MessageModel;
import com.hillsalex.metatext.model.MmsMessageModel;
import com.hillsalex.metatext.model.SmsMessageModel;
import com.klinker.android.logger.Log;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;
import com.klinker.android.send_message.Utils;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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

    public ThreadDetailViewFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_thread_detail_view, container, false);
        mRootView = rootView;
        threadId = getArguments().getLong("threadId");
        if (getArguments().containsKey("titleSet")){
            titleSet = getArguments().getBoolean("titleSet");
        }
        if (getArguments().getBoolean("cameFromNotification")){
            NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel((int)threadId);
        }
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.thread_detail_view_recycler_view);
        recyclerView.setHasFixedSize(false);
        layoutManager= new LinearLayoutManager(rootView.getContext(), LinearLayoutManager.VERTICAL, true);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ThreadDetailViewAdapter(new ArrayList<MessageModel>());
        recyclerView.setAdapter(adapter);

        LoadConvo();

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);

        ((ImageView) rootView.findViewById(R.id.send_message_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText textView = (EditText)mRootView.findViewById(R.id.message_to_send_text);
                if (textView != null) {
                    String toSend = textView.getText().toString();
                    textView.setText("");

                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm.isAcceptingText()) { // verify if the soft keyboard is open
                        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                    }
                    Log.v("TreadDetailViewFrag",toSend);

                    String[] addresses =adapter.getAddresses();
                    if (addresses!=null && addresses.length>0) {
                        Settings s = Utils.getDefaultSendSettings(getActivity());
                        Transaction t = new Transaction(getActivity(),s);
                        Message message;
                        if (addresses.length>1)
                            message = new Message(toSend,addresses);
                        else{
                            message = new Message(toSend,addresses[0]);
                        }
                        t.sendNewMessage(message,adapter.getThreadId());
                    }
                }
            }

        });

        //adapter.setElements(mMessages);
        return rootView;
    }


    private class LoaderTask extends AsyncTask<SortMultipleCursor, SortMultipleCursor, Pair<List<MessageModel>, SortMultipleCursor>> {
        @Override
        protected Pair<List<MessageModel>, SortMultipleCursor> doInBackground(SortMultipleCursor... params) {
            List<MessageModel> models = new ArrayList<>();
            SortMultipleCursor cursor = params[0];
            int count = 0;
            while (!cursor.isAfterLast() && count < 10) {

                try {
                    Pair<Cursor, Integer> nextItem = cursor.next();

                    if (nextItem.second == 0) {
                        Long date = nextItem.first.getLong(2);
                        MmsMessageModel mms = new MmsMessageModel(nextItem.first.getLong(1), date, threadId, nextItem.first.getInt(8) == Telephony.Mms.MESSAGE_BOX_SENT);
                        models.add(mms);
                    } else {
                        Long id = nextItem.first.getLong(1);
                        String address = nextItem.first.getString(3);
                        String body = nextItem.first.getString(4);
                        Long date = nextItem.first.getLong(2);
                        boolean isMe = false;
                        if (nextItem.first.getInt(6) == 2) {
                            isMe = true;
                        }
                        SmsMessageModel sms = new SmsMessageModel(id, address, date, threadId, body, isMe);
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

    public void notifyNewMessage(Uri uri, boolean isSms){
        if (!isSms) {
            MmsMessageModel model = ActiveDatabases.getMmsDatabase(getActivity()).getMessageForThreadView(uri);
            if (model.threadId == threadId) {
                adapter.updateThread(model);
            }
        } else {
            SmsMessageModel model = ActiveDatabases.getSmsDatabase(getActivity()).getMessageForThreadView(uri);
            if (model.threadId == threadId) {
                adapter.updateThread(model);
            }
        }
    }

    public void LoadConvo() {

        try {
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
                    MmsMessageModel mms = new MmsMessageModel(nextItem.first.getLong(1), date, threadId, nextItem.first.getInt(8) == Telephony.Mms.MESSAGE_BOX_SENT);
                    mMessages.add(mms);
                } else {
                    Long id = nextItem.first.getLong(1);
                    String address = nextItem.first.getString(3);
                    String body = nextItem.first.getString(4);
                    Long date = nextItem.first.getLong(2);
                    boolean isMe = false;
                    if (nextItem.first.getInt(6) == 2) {
                        isMe = true;
                    }
                    SmsMessageModel sms = new SmsMessageModel(id, address, date, threadId, body, isMe);
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

        public String[] getAddresses(){
            if (mDataset.size()>0){
                return mDataset.get(0).contactList.getNumbers(true);
            }
            return null;
        }
        public long getThreadId(){
            if (mDataset.size()>0){
                return mDataset.get(0).threadId;
            }
            return 0;
        }

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public View mRootView;


            public ViewHolder(View v) {
                super(v);
                mRootView = v;
            }

        }

        public void updateThread(MessageModel model) {
            int oldIndex = -1;
            int i = 0;
            for (MessageModel m : mDataset) {
                if (m.threadId == model.threadId)
                    oldIndex = i;
                i++;
            }
            boolean isScrolled = layoutManager.findFirstVisibleItemPosition()==0;
            if (oldIndex == -1) {
                mDataset.add(0, model);
                notifyItemInserted(0);
            } else {
                mDataset.remove(oldIndex);
                mDataset.add(0, model);
                notifyItemMoved(oldIndex, 0);
                notifyItemChanged(0);
                if (!isScrolled) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            layoutManager.scrollToPosition(0);
                        }
                    });
                }
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
            text = model.senderContact.getName();
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


