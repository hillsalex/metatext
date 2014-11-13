package com.hillsalex.metatext;

import android.app.Fragment;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.hillsalex.metatext.database.ActiveDatabases;
import com.hillsalex.metatext.database.SortMultipleCursor;
import com.hillsalex.metatext.model.ContactList;
import com.hillsalex.metatext.model.MessageModel;
import com.hillsalex.metatext.model.MmsMessageModel;
import com.hillsalex.metatext.model.SmsMessageModel;
import com.klinker.android.logger.Log;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by alex on 11/10/2014.
 */
public class ThreadListViewFragment extends Fragment {


    List<MessageModel> mMessages = new ArrayList<>();
    MyAdapter adapter;
    ThreadClickedListener listener;
    LinearLayoutManager layoutManager;

    public ThreadListViewFragment() {

    }

    public void setListener(ThreadClickedListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_thread_list_view, container, false);


        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.thread_list_view_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(rootView.getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MyAdapter(new ArrayList<MessageModel>());
        recyclerView.setAdapter(adapter);
        adapter.setOnClickedListener(listener);
        LoadConvos();


        //adapter.setElements(mMessages);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        RefreshConversations();
    }

    public void RefreshConversations() {
        if (adapter.isEmpty()) {
            LoadConvos();
            return;
        }
        MessageModel model = adapter.getLastMessage();

        Set<Long> threadIds = new HashSet<>();
        List<MessageModel> newMessages = new ArrayList<>();
        try {
            SortMultipleCursor cursor = ActiveDatabases.getMmsSmsDatabase(getActivity()).getThreadsPastDate(model.date);

            if (cursor.getCount() > 20) {
                adapter.clear();
                mMessages.clear();
                LoadConvos();
                adapter.setElements(mMessages);
                return;
            }
            while (!cursor.isAfterLast()) {
                Pair<Cursor, Integer> nextItem = cursor.next();
                long threadId;
                if (nextItem.second == 0) {
                    threadId = nextItem.first.getLong(0);
                    if (!threadIds.contains(threadId)) {
                        Long date = nextItem.first.getLong(1);

                        MmsMessageModel mms = new MmsMessageModel(nextItem.first.getLong(3), date, threadId, nextItem.first.getInt(4) == Telephony.Mms.MESSAGE_BOX_SENT);
                        newMessages.add(mms);
                        threadIds.add(threadId);
                    }
                } else {
                    threadId = nextItem.first.getInt(0);
                    if (!threadIds.contains(threadId)) {
                        Long id = nextItem.first.getLong(2);
                        String address = nextItem.first.getString(3);
                        String body = nextItem.first.getString(5);
                        Long date = nextItem.first.getLong(1);
                        boolean isMe = false;
                        if (nextItem.first.getInt(4) == 2) {
                            isMe = true;
                        }
                        SmsMessageModel sms = new SmsMessageModel(id, address, date, threadId, body, isMe);
                        newMessages.add(sms);
                        threadIds.add(threadId);
                    }
                }
            }
            cursor.closeAll();
        } catch (SortMultipleCursor.InvalidMultiCursorArgumentsException e) {
            e.printStackTrace();
        } catch (SortMultipleCursor.MultiCursorAfterLastException e) {
            e.printStackTrace();
        }

        adapter.refreshElements(newMessages);
    }

    Set<Long> seenThreadIds = new HashSet<>();

    private class LoaderTask extends AsyncTask<SortMultipleCursor, SortMultipleCursor, Pair<List<MessageModel>,SortMultipleCursor>> {
        @Override
        protected Pair<List<MessageModel>,SortMultipleCursor> doInBackground(SortMultipleCursor... params) {
            List<MessageModel> models = new ArrayList<>();
            SortMultipleCursor cursor = params[0];
            int count = 0;
                while (!cursor.isAfterLast() && count < 10) {

                    try {
                        Pair<Cursor, Integer> nextItem = cursor.next();
                        long threadId;
                        if (nextItem.second == 0) {
                            threadId = nextItem.first.getLong(0);
                            if (!seenThreadIds.contains(threadId)) {
                                count++;
                                Long date = nextItem.first.getLong(1);
                                MmsMessageModel mms = new MmsMessageModel(nextItem.first.getLong(3), date, threadId, nextItem.first.getInt(4) == Telephony.Mms.MESSAGE_BOX_SENT);
                                models.add(mms);
                                seenThreadIds.add(threadId);
                            }
                        } else {
                            threadId = nextItem.first.getInt(0);
                            if (!seenThreadIds.contains(threadId)) {
                                count++;
                                Long id = nextItem.first.getLong(2);
                                String address = nextItem.first.getString(3);
                                String body = nextItem.first.getString(5);
                                Long date = nextItem.first.getLong(1);
                                boolean isMe = false;
                                if (nextItem.first.getInt(4) == 2) {
                                    isMe = true;
                                }
                                SmsMessageModel sms = new SmsMessageModel(id, address, date, threadId, body, isMe);
                                models.add(sms);
                                seenThreadIds.add(threadId);
                            }
                        }
                    } catch (SortMultipleCursor.MultiCursorAfterLastException e) {
                        e.printStackTrace();
                    }

            }
            return new Pair<>(models,cursor);
        }

        @Override
        protected void onPostExecute(Pair<List<MessageModel>,SortMultipleCursor> models) {
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

    private void LoadConvos() {
        try {
            seenThreadIds = new HashSet<>();
            LoaderTask task = new LoaderTask();
            SortMultipleCursor cursor = ActiveDatabases.getMmsSmsDatabase(getActivity()).getSortedThreads();
            task.execute(cursor);
        } catch (SortMultipleCursor.InvalidMultiCursorArgumentsException e) {
            e.printStackTrace();
            return;
        }
        if (true)
            return;




        Set<Long> threadIds = new HashSet<>();
        try {
            SortMultipleCursor cursor = ActiveDatabases.getMmsSmsDatabase(getActivity()).getSortedThreads();
            while (!cursor.isAfterLast()) {
                Pair<Cursor, Integer> nextItem = cursor.next();
                long threadId;
                if (nextItem.second == 0) {
                    threadId = nextItem.first.getLong(0);
                    if (!threadIds.contains(threadId)) {
                        Long date = nextItem.first.getLong(1);

                        MmsMessageModel mms = new MmsMessageModel(nextItem.first.getLong(3), date, threadId, nextItem.first.getInt(4) == Telephony.Mms.MESSAGE_BOX_SENT);
                        mMessages.add(mms);
                        threadIds.add(threadId);
                    }
                } else {
                    threadId = nextItem.first.getInt(0);
                    if (!threadIds.contains(threadId)) {
                        Long id = nextItem.first.getLong(2);
                        String address = nextItem.first.getString(3);
                        String body = nextItem.first.getString(5);
                        Long date = nextItem.first.getLong(1);
                        boolean isMe = false;
                        if (nextItem.first.getInt(4) == 2) {
                            isMe = true;
                        }
                        SmsMessageModel sms = new SmsMessageModel(id, address, date, threadId, body, isMe);
                        mMessages.add(sms);
                        threadIds.add(threadId);
                    }
                }
            }
            cursor.closeAll();
        } catch (SortMultipleCursor.InvalidMultiCursorArgumentsException e) {
            e.printStackTrace();
        } catch (SortMultipleCursor.MultiCursorAfterLastException e) {
            e.printStackTrace();
        }
    }

    public void notifyNewMessage(Uri uri, boolean isSms) {
        if (!isSms) {
            MmsMessageModel model = ActiveDatabases.getMmsDatabase(getActivity()).getMessageForThreadView(uri);
            adapter.updateThread(model);
        } else {
            SmsMessageModel model = ActiveDatabases.getSmsDatabase(getActivity()).getMessageForThreadView(uri);
            adapter.updateThread(model);
        }
    }


    public static abstract class ThreadClickedListener {
        public abstract void onClick(long threadId, ContactList contacts);
    }

    public static class AddElementsRunnable implements Runnable{
        List<MessageModel> mModels;
        MyAdapter self;
        public AddElementsRunnable(MyAdapter adapter, List<MessageModel> models){
            self = adapter;
            mModels = models;
        }

        @Override
        public void run() {
            self.addElements(mModels);
        }
    }
    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private List<MessageModel> mDataset;
        private ThreadClickedListener listener;


        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            // each data item is just a string in this case
            public View mRootView;

            public View contactView;

            public ViewHolder(View v, View cView) {
                super(v);
                mRootView = v;
                v.setOnClickListener(this);
                contactView = cView;
            }

            @Override
            public void onClick(View v) {
                if (listener != null) listener.onClick(mDataset.get(getPosition()).threadId,mDataset.get(getPosition()).contactList);
            }
        }

        public boolean isEmpty() {
            return mDataset.isEmpty();
        }

        public void clear() {
            mDataset.clear();
            notifyDataSetChanged();
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

        public MessageModel getLastMessage() {
            return mDataset.get(0);
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

        public void refreshElements(List<MessageModel> models) {
            for (int i = models.size() - 1; i >= 0; i--) {
                updateThread(models.get(i));
            }
        }
        /*
        public void updateThread(SmsMessageModel model){

        }*/

        // Provide a suitable constructor (depends on the kind of dataset)
        public MyAdapter(List<MessageModel> myDataset) {
            mDataset = myDataset;
        }

        public void setOnClickedListener(ThreadClickedListener listener) {
            this.listener = listener;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.thread_list_view_holder, parent, false);
            // set the view's size, margins, paddings and layout parameters

            View picView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.multi_contact_picture_view, (FrameLayout) v.findViewById(R.id.contact_pic_holder), true);

            ViewHolder vh = new ViewHolder(v, picView);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            MessageModel model = mDataset.get(position);

            //SET NAMES
            String text = "";
            text = model.contactList.formatNames(";");
            ((TextView) holder.mRootView.findViewById(R.id.list_view_address_name)).setText(text);


            //SET BODY
            String body = "";
            if (mDataset.get(position) instanceof SmsMessageModel) {
                body = model.body;
            } else if (model.body.equals("")) {
                body = "MMS no text";
            } else {
                body = "Mms: " + model.body;
            }
            ((TextView) holder.mRootView.findViewById(R.id.list_view_last_message_text)).setText(body);


            //SET DATE
            String date = "";
            long msgTime = model.date;
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat tf = android.text.format.DateFormat.getTimeFormat(getActivity());
            date = df.format(msgTime) + " - " + tf.format(msgTime);
            ((TextView) holder.mRootView.findViewById(R.id.list_view_date_text)).setText(date);

            //SET CONTACT
            if (model.contactList.size() == 1) {
                holder.contactView.findViewById(R.id.contact_pic_0_0).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_1_0).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_1_1).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_2_0).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_2_1).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_2_2).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_3_0).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_3_1).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_3_2).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_3_3).setVisibility(View.GONE);

                holder.contactView.findViewById(R.id.contact_pic_0_0).setVisibility(View.VISIBLE);
                ((ImageView) holder.contactView.findViewById(R.id.contact_pic_0_0)).setImageDrawable(model.contactList.get(0).getAvatar(getActivity(), getActivity().getResources().getDrawable(R.color.teal500)));
            } else if (model.contactList.size() == 2) {
                holder.contactView.findViewById(R.id.contact_pic_0_0).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_1_0).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_1_1).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_2_0).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_2_1).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_2_2).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_3_0).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_3_1).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_3_2).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_3_3).setVisibility(View.GONE);

                holder.contactView.findViewById(R.id.contact_pic_1_0).setVisibility(View.VISIBLE);
                ((ImageView) holder.contactView.findViewById(R.id.contact_pic_1_0)).setImageDrawable(model.contactList.get(0).getAvatar(getActivity(), getActivity().getResources().getDrawable(R.color.teal500)));
                holder.contactView.findViewById(R.id.contact_pic_1_1).setVisibility(View.VISIBLE);
                ((ImageView) holder.contactView.findViewById(R.id.contact_pic_1_1)).setImageDrawable(model.contactList.get(1).getAvatar(getActivity(), getActivity().getResources().getDrawable(R.color.teal500)));
            } else if (model.contactList.size() == 3) {
                holder.contactView.findViewById(R.id.contact_pic_0_0).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_1_0).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_1_1).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_2_0).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_2_1).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_2_2).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_3_0).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_3_1).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_3_2).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_3_3).setVisibility(View.GONE);

                holder.contactView.findViewById(R.id.contact_pic_2_0).setVisibility(View.VISIBLE);
                ((ImageView) holder.contactView.findViewById(R.id.contact_pic_2_0)).setImageDrawable(model.contactList.get(0).getAvatar(getActivity(), getActivity().getResources().getDrawable(R.color.teal500)));
                holder.contactView.findViewById(R.id.contact_pic_2_1).setVisibility(View.VISIBLE);
                ((ImageView) holder.contactView.findViewById(R.id.contact_pic_2_1)).setImageDrawable(model.contactList.get(1).getAvatar(getActivity(), getActivity().getResources().getDrawable(R.color.teal500)));
                holder.contactView.findViewById(R.id.contact_pic_2_2).setVisibility(View.VISIBLE);
                ((ImageView) holder.contactView.findViewById(R.id.contact_pic_2_2)).setImageDrawable(model.contactList.get(2).getAvatar(getActivity(), getActivity().getResources().getDrawable(R.color.teal500)));
            } else if (model.contactList.size() == 4) {
                holder.contactView.findViewById(R.id.contact_pic_0_0).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_1_0).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_1_1).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_2_0).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_2_1).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_2_2).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_3_0).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_3_1).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_3_2).setVisibility(View.GONE);
                holder.contactView.findViewById(R.id.contact_pic_3_3).setVisibility(View.GONE);

                holder.contactView.findViewById(R.id.contact_pic_3_0).setVisibility(View.VISIBLE);
                ((ImageView) holder.contactView.findViewById(R.id.contact_pic_3_0)).setImageDrawable(model.contactList.get(0).getAvatar(getActivity(), getActivity().getResources().getDrawable(R.color.teal500)));
                holder.contactView.findViewById(R.id.contact_pic_3_1).setVisibility(View.VISIBLE);
                ((ImageView) holder.contactView.findViewById(R.id.contact_pic_3_1)).setImageDrawable(model.contactList.get(1).getAvatar(getActivity(), getActivity().getResources().getDrawable(R.color.teal500)));
                holder.contactView.findViewById(R.id.contact_pic_3_2).setVisibility(View.VISIBLE);
                ((ImageView) holder.contactView.findViewById(R.id.contact_pic_3_2)).setImageDrawable(model.contactList.get(2).getAvatar(getActivity(), getActivity().getResources().getDrawable(R.color.teal500)));
                holder.contactView.findViewById(R.id.contact_pic_3_3).setVisibility(View.VISIBLE);
                ((ImageView) holder.contactView.findViewById(R.id.contact_pic_3_3)).setImageDrawable(model.contactList.get(3).getAvatar(getActivity(), getActivity().getResources().getDrawable(R.color.teal500)));
            }

            //SET IMAGE PICS
            if (model instanceof MmsMessageModel) {
                MmsMessageModel mmsmodel = (MmsMessageModel) model;
                if (mmsmodel.mImageUris.size() > 0) {
                    Picasso.with(getActivity()).load(mmsmodel.mImageUris.get(0)).into(((ImageView) holder.mRootView.findViewById(R.id.thread_view_image_preview)));

                } else {
                    Picasso.with(getActivity()).cancelRequest(((ImageView) holder.mRootView.findViewById(R.id.thread_view_image_preview)));
                    ((ImageView) holder.mRootView.findViewById(R.id.thread_view_image_preview)).setImageURI(null);
                    ((ImageView) holder.mRootView.findViewById(R.id.thread_view_image_preview)).setImageBitmap(null);
                    ((ImageView) holder.mRootView.findViewById(R.id.thread_view_image_preview)).setImageDrawable(null);
                }
            } else {
                Picasso.with(getActivity()).cancelRequest(((ImageView) holder.mRootView.findViewById(R.id.thread_view_image_preview)));
                ((ImageView) holder.mRootView.findViewById(R.id.thread_view_image_preview)).setImageURI(null);
                ((ImageView) holder.mRootView.findViewById(R.id.thread_view_image_preview)).setImageBitmap(null);
                ((ImageView) holder.mRootView.findViewById(R.id.thread_view_image_preview)).setImageDrawable(null);
            }

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }


}

