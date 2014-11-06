package hills.sms.texter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hillsalex.metatext.database.ActiveDatabases;
import com.hillsalex.metatext.database.SmsDatabase;
import com.hillsalex.metatext.database.SortMultipleCursor;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.ListFragment;
import android.content.ContentResolver;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import hills.sms.texter.MainActivity.MyAdapter;
import hills.sms.texter.MainActivity.MyAdapter.ViewHolder;
import hills.sms.texter.dummy.DummyContent;
import hills.sms.texter.texts.Conversation;
import hills.sms.texter.texts.Conversation.PaginatedConversationList;
import hills.sms.texter.texts.Messages.Message;

/**
 * A list fragment representing a list of Threads. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link ConversationDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ConversationListFragment extends Fragment implements OnRefreshListener {

	private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Message> myDataset = new ArrayList<Message>();
	private SwipeRefreshLayout mSwipeLayout;
	PaginatedConversationList mConvoList = null;
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
		public Map<Integer,Conversation> getConversations();
	}

	
	public void testClick(View v) {
		int i=0;
		i++;
		i--;
	}
	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) {
		}
		@Override
		public Map<Integer,Conversation> getConversations(){
			return null;
		}
	};

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ConversationListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// TODO: replace with a real list adapter.
		
	}
	
	 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.conversation_list_view_holder, container);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			//setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
		}
		
		
		mSwipeLayout = (SwipeRefreshLayout) getView().findViewById(R.id.refresh_layout);
		mSwipeLayout.setOnRefreshListener(this);
		
		
		
		mRecyclerView = (RecyclerView) getView().findViewById(R.id.threads_recycler_view);
		
		mRecyclerView.setHasFixedSize(true);
		mLayoutManager = new LinearLayoutManager(getActivity());
		mRecyclerView.setLayoutManager(mLayoutManager);
		
		
		//List<Conversation> smsThreads = TexterHelper.getSMSThreads(resolver);
		mAdapter = new MyAdapter(new ArrayList<Conversation>());
		mRecyclerView.setAdapter(mAdapter);

        SmsDatabase smsdb = ActiveDatabases.getSmsDatabase(getActivity());
        Cursor cursor = smsdb.getThreadsAndDates();
        if (cursor!=null){
            cursor.moveToFirst();
            while (!cursor.isAfterLast())
            {
                long tid = cursor.getInt(0);
                long date = cursor.getLong(1);
                String body = cursor.getString(2);
                int i=0;
                i++;
            }
        }



		onRefresh();
	}
	
	private void startNewPage(){
		(new GetNextPageTask()).execute(1);
	}
	
	private class GetNextPageTask extends AsyncTask<Integer, Integer, List<Conversation>>{

		@Override
		protected List<Conversation> doInBackground(Integer... params) {
			List<Conversation> toReturn = null;
			if (mConvoList!=null){
				toReturn = mConvoList.getNextPage();
			}
			return toReturn;
		}
		
		protected void onPostExecute(List<Conversation> result){
			mSwipeLayout.setRefreshing(false);
			if (result != null)
			{
				int start = mAdapter.mDataset.size();
				mAdapter.mDataset.addAll(result);
				mAdapter.notifyItemRangeInserted(start, result.size());
				startNewPage();
			}
		}
		
	}
	
	
	@Override
	public void onRefresh(){
		ContentResolver resolver = getActivity().getContentResolver();
		mConvoList = TexterHelper.getAllThreads(resolver);
		mAdapter.mDataset.clear();
		mAdapter.notifyDataSetChanged();
		startNewPage();
	}
	/*
	public void fetchAllThreads(){
		ContentResolver resolver = getActivity().getContentResolver();
		List<Conversation> smsThreads = TexterHelper.getSMSThreads(resolver);
		mAdapter.mDataset = smsThreads;
	}
	
	public void fetchThreadsRefreshView(){
			(new FetchTreadsTask()).execute(1);
	}
	
	public class FetchTreadsTask extends AsyncTask<Integer, Float, Integer>{

		@Override
		protected Integer doInBackground(Integer... params) {
			fetchAllThreads();
			return null;
		}
		
		protected void onPostExecute(Integer result){
			mAdapter.notifyDataSetChanged();
		}
	}*/

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}
	/*
	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		mCallbacks.onItemSelected(DummyContent.ITEMS.get(position).id);
	}
	*/
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
	/*
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
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
	
	*/
	
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> implements View.OnClickListener{
    public List<Conversation> mDataset;

    private final OnClickListener clickListener = this;
    

	@Override
	public void onClick(View v) {
		int itemPosition = mRecyclerView.getChildPosition(v);
		mCallbacks.onItemSelected(""+mDataset.get(itemPosition).threadID);
	}
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder  {
        // each data item is just a string in this case
        public View mRootView;
        public Conversation mConversation=null;
        public ViewHolder(View v) {
            super(v);
            mRootView = v;
            mRootView.setOnClickListener(clickListener);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(List<Conversation> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.thread_view, parent, false);
        // set the view's size, margins, paddings and layout parameters
        
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
    	Conversation conversation = mDataset.get(position);
    	holder.mConversation = conversation;
    	ContentResolver resolver = getActivity().getContentResolver();
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        ((TextView)holder.mRootView.findViewById(R.id.names)).setText(conversation.lastMessage.threadName);
        if (conversation.lastMessage.displayMessage==null || conversation.lastMessage.displayMessage.equals("")){
        	
        	((TextView)holder.mRootView.findViewById(R.id.text_content)).setText((conversation.lastMessage.isMe ? "You sent an " : "They sent an ") + "Image");
        }
        else ((TextView)holder.mRootView.findViewById(R.id.text_content)).setText(conversation.lastMessage.displayMessage);
        if (conversation.lastMessage.thumbnailUri == null){
        	((ImageView)holder.mRootView.findViewById(R.id.prof_pic)).setImageResource(R.drawable.teal500);
        	((TextView)holder.mRootView.findViewById(R.id.image_replacement)).setText(conversation.lastMessage.imageReplacement);
        }
        else{
        	((TextView)holder.mRootView.findViewById(R.id.image_replacement)).setText("");
        	ImageView contactimage = ((ImageView)holder.mRootView.findViewById(R.id.prof_pic));
        	Picasso.with(holder.mRootView.getContext()).load(conversation.lastMessage.thumbnailUri).placeholder(R.drawable.teal500).noFade().into(contactimage);
        	/*try {
        	    String nullString = contactimage.getDrawable().toString();
        	} catch (java.lang.NullPointerException ex) {
        	    contactimage.setImageResource(R.drawable.teal500);
        	    ((TextView)holder.mRootView.findViewById(R.id.image_replacement)).setText(conversation.lastMessage.imageReplacement);
        	}*/
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
}
