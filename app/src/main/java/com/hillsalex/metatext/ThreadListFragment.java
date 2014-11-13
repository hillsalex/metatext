package com.hillsalex.metatext;

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.hillsalex.metatext.database.ActiveDatabases;
import com.hillsalex.metatext.database.SortMultipleCursor;
import com.hillsalex.metatext.model.MessageModel;
import com.hillsalex.metatext.model.MmsMessageModel;
import com.hillsalex.metatext.model.SmsMessageModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A list fragment representing a list of Threads. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link ThreadDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ThreadListFragment extends ListFragment {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    public static List<MessageModel> mMessages = new ArrayList<>();
    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ThreadListFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LoadConvos();
        // TODO: replace with a real list adapter.
        setListAdapter(new ArrayAdapter<MessageModel>(
                getActivity(),
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                mMessages));


    }

    private void LoadConvos(){
        Set<Long> threadIds = new HashSet<>();
        try{
            SortMultipleCursor cursor = ActiveDatabases.getMmsSmsDatabase(getActivity()).getSortedThreads();
            while (!cursor.isAfterLast()){
                Pair<Cursor, Integer> nextItem = cursor.next();
                long threadId;
                if (nextItem.second==0){
                    threadId = nextItem.first.getLong(0);
                    if (!threadIds.contains(threadId)) {
                        Long date = nextItem.first.getLong(1);
                        MmsMessageModel mms = new MmsMessageModel(nextItem.first.getLong(3),date,threadId,false);
                        /*
                        Messages.MMSMessage message = new Messages.MMSMessage(nextItem.first.getInt(3),(int)threadId);
                        TexterHelper.populateMMS(getActivity().getContentResolver(), message);
                        message.resolvePublicFields();
                        convos.add(new Conversation(message));
                        */
                        mMessages.add(mms);
                        threadIds.add(threadId);
                    }
                }
                else{
                    threadId = nextItem.first.getInt(0);
                    if (!threadIds.contains(threadId)) {
                        Long id = nextItem.first.getLong(2);
                        String address = nextItem.first.getString(3);
                        String body = nextItem.first.getString(5);
                        Long date=nextItem.first.getLong(1);
                        boolean isMe = false;
                        if (nextItem.first.getInt(4)==2)
                        {
                            isMe = true;
                        }
                        SmsMessageModel sms = new SmsMessageModel(id,address,date,threadId,body,isMe);
                        mMessages.add(sms);
                        /*
                        ContactsHelper.Contact c =ContactsHelper.getContactInfo(getActivity().getContentResolver(),nextItem.first.getString(3));
                        boolean isMe = false;
                        if (nextItem.first.getInt(4)==2)
                        {
                            isMe = true;
                        }
                        Messages.SMSMessage message = new Messages.SMSMessage(
                                nextItem.first.getString(5),
                                c,
                                nextItem.first.getInt(0),
                                nextItem.first.getInt(2),
                                isMe);
                        convos.add(new Conversation(message));
                        */
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

        int si=0;
        si++;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(mMessages.get(position).threadId +"");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */

    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }


    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }


}
