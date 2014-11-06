package hills.sms.texter;

import java.util.ArrayList;
import java.util.List;

import com.hillsalex.metatext.messages.sms.OutgoingTextMessage;
import com.hillsalex.metatext.persons.Person;
import com.hillsalex.metatext.services.SendReceiveServices;
import com.squareup.picasso.Picasso;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import hills.sms.texter.MainActivity.MyAdapter;
import hills.sms.texter.dummy.DummyContent;
import hills.sms.texter.sender.Sender;
import hills.sms.texter.texts.Conversation;
import hills.sms.texter.texts.Conversation.PaginatedMessageList;
import hills.sms.texter.texts.Messages.MMSMessage;
import hills.sms.texter.texts.Messages.Message;
import hills.sms.texter.texts.Messages.SMSMessage;

/**
 * A fragment representing a single Conversation detail screen. This fragment is
 * either contained in a {@link ConversationListActivity} in two-pane mode (on
 * tablets) or a {@link ConversationDetailActivity} on handsets.
 */
public class ConversationDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The dummy content this fragment is presenting.
	 */
	private DummyContent.DummyItem mItem;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ConversationDetailFragment() {
	}

	 private RecyclerView mRecyclerView;
	private ConversationAdapter mAdapter;
	private RecyclerView.LayoutManager mLayoutManager;
	
	private int mThreadID;
	private View rootView;
	private PaginatedMessageList mMessageList = null;
   
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		
		
		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			mItem = DummyContent.ITEM_MAP.get(getArguments().getString(
					ARG_ITEM_ID));
			mThreadID = Integer.parseInt(getArguments().getString(ARG_ITEM_ID));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_conversation_detail,
				container, false);

		// Show the dummy content as text in a TextView.
		
		
		mRecyclerView = (RecyclerView) rootView.findViewById(R.id.conversation_recycler_view);
		mRecyclerView.setHasFixedSize(false);
		mLayoutManager = new LinearLayoutManager(rootView.getContext());
		((LinearLayoutManager)mLayoutManager).setReverseLayout(true);
		mRecyclerView.setLayoutManager(mLayoutManager);
		
		ConversationAdapter adapter = new ConversationAdapter(new ArrayList<Message>());
		mAdapter = adapter;
		mRecyclerView.setAdapter(adapter);
		((ImageButton)rootView.findViewById(R.id.send_button)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendTapped(v);
			}
		});
		onRefresh();
		
		return rootView;
	}

	public void sendTapped(View v){
		if (mAdapter.mDataset.get(0) instanceof SMSMessage)
		{
            Intent intent = new Intent(getActivity().getApplicationContext(),SendReceiveServices.class);
            intent.setAction(SendReceiveServices.SEND_SINGLE_SMS_ACTION);
            Person p = new Person(0,mAdapter.mDataset.get(0).from.phoneNumber,null,null,"");
            OutgoingTextMessage message = new OutgoingTextMessage(p,((TextView)rootView.findViewById(R.id.send_text)).getText().toString());
            intent.putExtra("text_message",message);
            getActivity().getApplicationContext().startService(intent);


            ((TextView)rootView.findViewById(R.id.send_text)).setText("");
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

		    if(imm.isAcceptingText()) { // verify if the soft keyboard is open                      
		        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
		    }
		}
		

	}
	
	public void onRefresh(){
		ContentResolver resolver = getActivity().getContentResolver();
		mMessageList = TexterHelper.getAllMessages(resolver, mThreadID);
		mAdapter.mDataset.clear();
		mAdapter.notifyDataSetChanged();
		startNewPage();
	}
	
	private void startNewPage(){
		(new GetNextPageTask()).execute(1);
	}
	
	private class GetNextPageTask extends AsyncTask<Integer, Integer, List<Message>>{

		@Override
		protected List<Message> doInBackground(Integer... params) {
			List<Message> toReturn = null;
			if (mMessageList!=null){
				toReturn = mMessageList.getNextPage();
			}
			return toReturn;
		}
		
		protected void onPostExecute(List<Message> result){
			if (result != null)
			{
				int start = mAdapter.mDataset.size();
				mAdapter.mDataset.addAll(result);
				mAdapter.notifyItemRangeInserted(start, result.size());
				startNewPage();
			}
			else
			{
				//mSwipeLayout.setRefreshing(false);
			}
		}
		
	}

	public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {
	    public List<Message> mDataset;

	    // Provide a reference to the views for each data item
	    // Complex data items may need more than one view per item, and
	    // you provide access to all the views for a data item in a view holder
	    public class ViewHolder extends RecyclerView.ViewHolder {
	        // each data item is just a string in this case
	        public View mTextView;
	        public ViewHolder(View v) {
	            super(v);
	            mTextView = v;
	        }
	    }

	    // Provide a suitable constructor (depends on the kind of dataset)
	    public ConversationAdapter(List<Message> myDataset) {
	        mDataset = myDataset;
	    }

	    // Create new views (invoked by the layout manager)
	    @Override
	    public ConversationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
	                                                   int viewType) {
	        // create a new view
	        View v = LayoutInflater.from(parent.getContext())
	                               .inflate(R.layout.conversation_entry_view, parent, false);
	        // set the view's size, margins, paddings and layout parameters
	 
	        ViewHolder vh = new ViewHolder(v);
	        return vh;
	    }

	    // Replace the contents of a view (invoked by the layout manager)
	    @Override
	    public void onBindViewHolder(ViewHolder holder, int position) {
	    	ContentResolver resolver = getActivity().getContentResolver();
	        // - get element from your dataset at this position
	        // - replace the contents of the view with that element
	    	Message message = mDataset.get(position);
	    	Message belowMessage = null;
	    	Message aboveMessage = null;
	    	if (position>0) belowMessage = mDataset.get(position-1);
	    	if (position<mDataset.size()-1) aboveMessage = mDataset.get(position+1);
	    	
	        ((TextView)holder.mTextView.findViewById(R.id.names)).setText(message.threadName);
	        ((TextView)holder.mTextView.findViewById(R.id.text_content)).setText(message.displayMessage);
	        ((TextView)holder.mTextView.findViewById(R.id.text_content)).setVisibility(View.VISIBLE);
	        if (message.displayMessage==null)
	        	((TextView)holder.mTextView.findViewById(R.id.text_content)).setVisibility(View.GONE);

    		holder.mTextView.findViewById(R.id.right_divider).setAlpha(0.0f);
    		holder.mTextView.findViewById(R.id.left_divider).setAlpha(0.0f);
    		
    		if (message.imageUris!=null && message.imageUris.size()>0)
    		{
    			ImageView img0 = ((ImageView)holder.mTextView.findViewById(R.id.message_thread_pic_0));
    			Picasso.with(holder.mTextView.getContext()).load(message.imageUris.get(0)).noFade().into(img0);
    			//img0.setImageURI(message.imageUris.get(0));
    			img0.setVisibility(View.VISIBLE);
    			/*try {
            	    String nullString = img0.getDrawable().toString();
            	} catch (java.lang.NullPointerException ex) {
            		img0.setImageResource(R.drawable.teal500);
            	}*/
    			if (message.imageUris.size()>1)
    			{
    				ImageView img1 = ((ImageView)holder.mTextView.findViewById(R.id.message_thread_pic_1));
    				Picasso.with(holder.mTextView.getContext()).load(message.imageUris.get(1)).into(img1);
    				//img1.setImageURI(message.imageUris.get(1));
        			img1.setVisibility(View.VISIBLE);
        			/*try {
                	    String nullString = img1.getDrawable().toString();
                	} catch (java.lang.NullPointerException ex) {
                		img1.setImageResource(R.drawable.teal500);
                	}*/
    			}
    			else
    			{
    				((ImageView)holder.mTextView.findViewById(R.id.message_thread_pic_1)).setImageResource(R.drawable.teal500);
    				((ImageView)holder.mTextView.findViewById(R.id.message_thread_pic_1)).setVisibility(View.GONE);    			
    			}
    		}
    		else{
    			((ImageView)holder.mTextView.findViewById(R.id.message_thread_pic_0)).setImageResource(R.drawable.teal500);
    			((ImageView)holder.mTextView.findViewById(R.id.message_thread_pic_0)).setVisibility(View.GONE);
    			((ImageView)holder.mTextView.findViewById(R.id.message_thread_pic_1)).setImageResource(R.drawable.teal500);
    			((ImageView)holder.mTextView.findViewById(R.id.message_thread_pic_1)).setVisibility(View.GONE);
    		}
    		
	        if (message.isMe) 
	    	{
	        	RelativeLayout layout = ((RelativeLayout)holder.mTextView.findViewById(R.id.conversation_entry_holder));

		        ((TextView)holder.mTextView.findViewById(R.id.text_content)).setGravity(Gravity.RIGHT);
	        	
	        	android.support.v7.widget.RecyclerView.LayoutParams params = (android.support.v7.widget.RecyclerView.LayoutParams)layout.getLayoutParams();
	        	int lm = params.leftMargin;
	        	int tm = params.topMargin;
	        	int bm = params.bottomMargin;
	        	int rm = params.rightMargin;
	        	Resources res = getResources();
	        	lm=TexterHelper.ConvertToDP(48,res);
	        	tm=TexterHelper.ConvertToDP(0,res);
	        	bm=TexterHelper.ConvertToDP(0,res);
	        	rm=TexterHelper.ConvertToDP(0,res);
	        	if (belowMessage!=null && !belowMessage.isMe)
	        	{
	        		bm=TexterHelper.ConvertToDP(16,res);
	        		holder.mTextView.findViewById(R.id.right_divider).setAlpha(0.0f);
	        		holder.mTextView.findViewById(R.id.conversation_entry_holder).setBackgroundResource(R.drawable.container_dropshadow_lrb);
	        	}
	        	if(belowMessage!=null && belowMessage.isMe){
	        		holder.mTextView.findViewById(R.id.right_divider).setAlpha(0.1f);
	        		holder.mTextView.findViewById(R.id.conversation_entry_holder).setBackgroundResource(R.drawable.container_dropshadow_lr);
	        	}
	        	if (belowMessage==null)
	        	{
	        		holder.mTextView.findViewById(R.id.conversation_entry_holder).setBackgroundResource(R.drawable.container_dropshadow_lr);
	        	}
	        	params.setMargins(lm,tm,rm,bm);
	        	layout.setLayoutParams(params);
	        	
	        	RelativeLayout.LayoutParams textLayoutParams = (RelativeLayout.LayoutParams)((TextView)holder.mTextView.findViewById(R.id.names)).getLayoutParams();
        		textLayoutParams.height = 0;
        		lm=textLayoutParams.leftMargin;
        		tm=textLayoutParams.topMargin;
        		rm=textLayoutParams.rightMargin;
        		bm=textLayoutParams.bottomMargin;
	        	if (aboveMessage!=null && aboveMessage.isMe){
	        		//tm=TexterHelper.ConvertToDP(0, res);
	        	}
	        	if(aboveMessage!=null && !aboveMessage.isMe){
	        		//tm=TexterHelper.ConvertToDP(8, res);
	        		//textLayoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
	        	}
        		textLayoutParams.setMargins(lm, tm, rm, bm);
	    	}
	        else
	        {
	        	RelativeLayout layout = ((RelativeLayout)holder.mTextView.findViewById(R.id.conversation_entry_holder));
	        	
		        ((TextView)holder.mTextView.findViewById(R.id.text_content)).setGravity(Gravity.LEFT);
		        
	        	android.support.v7.widget.RecyclerView.LayoutParams params = (android.support.v7.widget.RecyclerView.LayoutParams)layout.getLayoutParams();
	        	int lm = params.leftMargin;
	        	int tm = params.topMargin;
	        	int bm = params.bottomMargin;
	        	int rm = params.rightMargin;
	        	Resources res = getResources();
	        	lm=TexterHelper.ConvertToDP(0,res);
	        	tm=TexterHelper.ConvertToDP(0,res);
	        	bm=TexterHelper.ConvertToDP(0,res);
	        	rm=TexterHelper.ConvertToDP(48,res);
	        	holder.mTextView.findViewById(R.id.conversation_entry_holder).setBackgroundResource(R.drawable.colorWhite);
	        	
	        	if (belowMessage!=null && belowMessage.isMe)
	        	{
	        		bm=TexterHelper.ConvertToDP(16,res);
	        		holder.mTextView.findViewById(R.id.left_divider).setAlpha(0.0f);
	        		holder.mTextView.findViewById(R.id.conversation_entry_holder).setBackgroundResource(R.drawable.container_dropshadow_lrb);
	        	}
	        	if(belowMessage!=null && !belowMessage.isMe)
	        	{
	        		holder.mTextView.findViewById(R.id.left_divider).setAlpha(0.1f);
	        		holder.mTextView.findViewById(R.id.conversation_entry_holder).setBackgroundResource(R.drawable.container_dropshadow_lr);
	        	}
	        	if (belowMessage==null)
	        	{
	        		holder.mTextView.findViewById(R.id.conversation_entry_holder).setBackgroundResource(R.drawable.container_dropshadow_lr);
	        	}
	        	params.setMargins(lm,tm,rm,bm);
	        	layout.setLayoutParams(params);
	        	
	        	RelativeLayout.LayoutParams textLayoutParams = (RelativeLayout.LayoutParams)((TextView)holder.mTextView.findViewById(R.id.names)).getLayoutParams();
        		if (message.from!=null && message instanceof MMSMessage && ((MMSMessage)message).mContacts.size()>1)
        		{
        			textLayoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        	        ((TextView)holder.mTextView.findViewById(R.id.names)).setText(message.from.displayName);
        		}
        		else{
        			textLayoutParams.height = 0;
        		}
        		lm=textLayoutParams.leftMargin;
        		tm=textLayoutParams.topMargin;
        		rm=textLayoutParams.rightMargin;
        		bm=textLayoutParams.bottomMargin;
	        	if (aboveMessage!=null && !aboveMessage.isMe){
	        		//tm=TexterHelper.ConvertToDP(0, res);
	        	}
	        	if(aboveMessage!=null && aboveMessage.isMe){
	        		//tm=TexterHelper.ConvertToDP(8, res);
	        		//textLayoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
	        	}
        		textLayoutParams.setMargins(lm, tm, rm, bm);
	        }
	        if (message.imageUris != null && message.imageUris.size()>0)
	        {
	        	holder.mTextView.findViewById(R.id.right_divider).setAlpha(0.0f);
	        	holder.mTextView.findViewById(R.id.left_divider).setAlpha(0.0f);
	        }
	        if (belowMessage!=null && belowMessage.imageUris!=null && belowMessage.imageUris.size()>0)
	        {
	        	holder.mTextView.findViewById(R.id.right_divider).setAlpha(0.0f);
	        	holder.mTextView.findViewById(R.id.left_divider).setAlpha(0.0f);
	        }
	        
	        /*if (message.thumbnailUri == null){
	        	((ImageView)holder.mTextView.findViewById(R.id.prof_pic)).setImageResource(R.drawable.teal500);
	        	((TextView)holder.mTextView.findViewById(R.id.image_replacement)).setText(message.imageReplacement);
	        }
	        else{
	        	((TextView)holder.mTextView.findViewById(R.id.image_replacement)).setText("");
	        	ImageView contactimage = ((ImageView)holder.mTextView.findViewById(R.id.prof_pic));
	        	contactimage.setImageURI(message.thumbnailUri);
	        	try {
	        	    String nullString = contactimage.getDrawable().toString();
	        	} catch (java.lang.NullPointerException ex) {
	        	    contactimage.setImageResource(R.drawable.teal500);
	        	    ((TextView)holder.mTextView.findViewById(R.id.image_replacement)).setText(message.imageReplacement);
	        	}
	        }*/
	    }

	    // Return the size of your dataset (invoked by the layout manager)
	    @Override
	    public int getItemCount() {
	        return mDataset.size();
	    }
	}
}
