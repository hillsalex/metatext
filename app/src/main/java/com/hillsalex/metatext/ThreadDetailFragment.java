package com.hillsalex.metatext;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import com.hillsalex.metatext.database.ActiveDatabases;
import com.hillsalex.metatext.database.SortMultipleCursor;
import com.hillsalex.metatext.model.MessageModel;
import com.hillsalex.metatext.model.MmsMessageModel;
import com.hillsalex.metatext.model.SmsMessageModel;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a single Thread detail screen.
 * This fragment is either contained in a {@link ThreadListActivity}
 * in two-pane mode (on tablets) or a {@link ThreadDetailActivity}
 * on handsets.
 */
public class ThreadDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private String threadId;

    List<MessageModel> messages = new ArrayList<MessageModel>();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ThreadDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            threadId = getArguments().getString(ARG_ITEM_ID);
        }
    }

    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /*View rootView = inflater.inflate(R.layout.fragment_thread_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.thread_detail)).setText(mItem.content);
        }

        return rootView;*/
        View rootView = inflater.inflate(R.layout.fragment_thread_detail,
                container, false);

        // Show the dummy content as text in a TextView.


        //mRecyclerView = (RecyclerView) rootView.findViewById(R.id.conversation_recycler_view);
        //mRecyclerView.setHasFixedSize(false);
        //mLayoutManager = new LinearLayoutManager(rootView.getContext());
        //((LinearLayoutManager)mLayoutManager).setReverseLayout(true);

        //mRecyclerView.setLayoutManager(mLayoutManager);
        loadMessages();
        ArrayAdapter<MessageModel> adapter = new ReverseAdapater<MessageModel>(
                getActivity(),
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                messages);
        ((ListView) rootView.findViewById(R.id.conversation_recycler_view)).setAdapter(adapter);
        ((ImageButton) rootView.findViewById(R.id.send_button)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendTapped(v);
            }
        });

        return rootView;
    }

    private void sendTapped(View v) {
        /*if (mAdapter.mDataset.get(0) instanceof SMSMessage)
        {
            SmsMessageSender sender = new SmsSingleRecipientSender(this.getActivity(),
                    mAdapter.mDataset.get(0).from.phoneNumber,
                    ((TextView)rootView.findViewById(R.id.send_text)).getText().toString(),MessageUtil.getOrCreateThreadId(this.getActivity(),mAdapter.mDataset.get(0).from.phoneNumber),false,null);
            try {
                sender.sendMessage(0);
            } catch (MmsException e) {
                e.printStackTrace();
            }

            /*
            Intent intent = new Intent(getActivity().getApplicationContext(),SendReceiveServices.class);
            intent.setAction(SendReceiveServices.SEND_SINGLE_SMS_ACTION);
            Person p = new Person(0,mAdapter.mDataset.get(0).from.phoneNumber,null,null,"");
            OutgoingTextMessage message = new OutgoingTextMessage(p,((TextView)rootView.findViewById(R.id.send_text)).getText().toString());
            intent.putExtra("text_message",message);
            getActivity().getApplicationContext().startService(intent);

            */
        /*
            ((TextView)rootView.findViewById(R.id.send_text)).setText("");
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

            if(imm.isAcceptingText()) { // verify if the soft keyboard is open
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            }
        }*/
    }

    public class ReverseAdapater<T> extends ArrayAdapter<T>{

        public ReverseAdapater(Context context, int resource) {
            super(context, resource);
        }

        public ReverseAdapater(Context context, int resource, int textViewResourceId) {
            super(context, resource, textViewResourceId);
        }

        public ReverseAdapater(Context context, int resource, T[] objects) {
            super(context, resource, objects);
        }

        public ReverseAdapater(Context context, int resource, int textViewResourceId, T[] objects) {
            super(context, resource, textViewResourceId, objects);
        }

        public ReverseAdapater(Context context, int resource, List<T> objects) {
            super(context, resource, objects);
        }

        public ReverseAdapater(Context context, int resource, int textViewResourceId, List<T> objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public T getItem(int position) {
            return super.getItem(getCount() - 1 - position);
        }

        @Override
        public int getPosition(T item) {
            return getCount() - 1 - super.getPosition(item);
        }

        @Override
        public long getItemId(int position) {
            return super.getItemId(getCount() - 1 - position);
        }

    }

    private void loadMessages() {

        try {
            SortMultipleCursor cursor = ActiveDatabases.getMmsSmsDatabase(getActivity()).getConversation(Long.parseLong(threadId));
            while (!cursor.isAfterLast()) {
                Pair<Cursor, Integer> nextItem = cursor.next();

                if (nextItem.second == 0) {
                    Long date = nextItem.first.getLong(2);
                    MmsMessageModel mms = new MmsMessageModel(nextItem.first.getLong(1), date,Long.parseLong(threadId),false);
                    messages.add(mms);
                } else {
                    Long id = nextItem.first.getLong(1);
                    String address = nextItem.first.getString(3);
                    String body = nextItem.first.getString(4);
                    Long date = nextItem.first.getLong(2);
                    boolean isMe = false;
                    if (nextItem.first.getInt(6) == 2) {
                        isMe = true;
                    }
                    SmsMessageModel sms = new SmsMessageModel(id, address,date, Long.parseLong(threadId), body, isMe);
                    messages.add(sms);
                }
            }
            cursor.closeAll();

        } catch (SortMultipleCursor.InvalidMultiCursorArgumentsException e) {
            e.printStackTrace();
        } catch (SortMultipleCursor.MultiCursorAfterLastException e) {
            e.printStackTrace();
        }
    }
}
