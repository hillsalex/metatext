package hills.sms.texter;

import hills.sms.texter.ContactsHelper.Contact;
import hills.sms.texter.texts.Conversation;
import hills.sms.texter.texts.Messages.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.R.integer;
import android.R.string;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	 private RecyclerView mRecyclerView;
	    private MyAdapter mAdapter;
	    private RecyclerView.LayoutManager mLayoutManager;
	    private List<Message> myDataset = new ArrayList<Message>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
		
		mRecyclerView.setHasFixedSize(false);
		
		
		mLayoutManager = new LinearLayoutManager(this);
		((LinearLayoutManager)mLayoutManager).setReverseLayout(true);
		mRecyclerView.setLayoutManager(mLayoutManager);

		android.os.Handler threadHandler = new android.os.Handler(){};
		int currentNotify=0;
		mAdapter = new MyAdapter(new ArrayList<Conversation>());
		//mRecyclerView.setAdapter(mAdapter);
		
		
		ContentResolver resolver = getContentResolver();
		List<Conversation> smsThreads = null;// TexterHelper.getSMSThreads(resolver);
		
		ConversationAdapter adapter = new ConversationAdapter(smsThreads.get(1));
		mRecyclerView.setAdapter(adapter);
		
		Thread threadGetter = new Thread(new Runnable(){
			public void run(){
				try{
					/*
					ContentResolver resolver = getContentResolver();
					List<Conversation> smsThreads = TexterHelper.getSMSThreads(resolver);
					List<Conversation> mmsThreads = TexterHelper.getMMSThreads(resolver);
					for (int i=0;i<smsThreads.size();i++)
					{
						mAdapter.mDataset.add(smsThreads.get(i));
					}
					
					mAdapter.notifyItemRangeInserted(0, smsThreads.size());*/
				}
				catch (Exception e){
					Log.e("SMSTexter", "Shit, exception in getting stuff", e);
				}
			}
		});
		threadGetter.start();
		
		/*for (int i=0;i<mmsThreads.size();i++){
			TextThread mms = mmsThreads.get(i);
			TextThread sms;
			for (int j=0;j<smsThreads.size();j++){
				sms = smsThreads.get(j);
				if (mms.threadID == sms.threadID){
					sms.lastMessage="THIS WAS MMS";
				}
			}
		}*/
		//myDataset = TexterHelper.getSMSThread(resolver,smsThreads.get(1));
		
		
		
	}
	
	public void fetchThreads(){
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
/*
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private List<Message> mDataset;

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
    public MessageAdapter(List<Message> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
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
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        ((TextView)holder.mTextView.findViewById(R.id.names)).setText(mDataset.get(position).contact.displayName);
        ((TextView)holder.mTextView.findViewById(R.id.text_content)).setText(mDataset.get(position).message);
        if (mDataset.get(position).contact.thumbnailURI == null){
        	((ImageView)holder.mTextView.findViewById(R.id.prof_pic)).setImageURI(null);
        }
        else{
        	((ImageView)holder.mTextView.findViewById(R.id.prof_pic)).setImageURI(mDataset.get(position).contact.thumbnailURI);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
	*/
	
	
public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {
    public Conversation mDataset;

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
    public ConversationAdapter(Conversation myDataset) {
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
    	ContentResolver resolver = getContentResolver();
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
    	Message message = mDataset.get(resolver,position);
    	Message belowMessage = null;
    	Message aboveMessage = null;
    	if (position>0) belowMessage = mDataset.get(resolver, position-1);
    	if (position<mDataset.size(resolver)-1) aboveMessage = mDataset.get(resolver, position+1);
    	
        ((TextView)holder.mTextView.findViewById(R.id.names)).setText(message.threadName);
        ((TextView)holder.mTextView.findViewById(R.id.text_content)).setText(message.displayMessage);
        if (message.isMe) 
    	{
        	RelativeLayout layout = ((RelativeLayout)holder.mTextView.findViewById(R.id.conversation_entry_holder));
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
        	}
        	params.setMargins(lm,tm,rm,bm);
        	layout.setLayoutParams(params);
    	}
        else
        {
        	RelativeLayout layout = ((RelativeLayout)holder.mTextView.findViewById(R.id.conversation_entry_holder));
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
        	
        	if (belowMessage!=null && belowMessage.isMe)
        	{
        		bm=TexterHelper.ConvertToDP(16,res);
        	}
        	
        	params.setMargins(lm,tm,rm,bm);
        	layout.setLayoutParams(params);
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
        return mDataset.size(getContentResolver());
    }
}

	
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    public List<Conversation> mDataset;

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
    	ContentResolver resolver = getContentResolver();
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        ((TextView)holder.mTextView.findViewById(R.id.names)).setText(mDataset.get(position).lastMessage.threadName);
        ((TextView)holder.mTextView.findViewById(R.id.text_content)).setText(mDataset.get(position).lastMessage.displayMessage);
        if (mDataset.get(position).lastMessage.thumbnailUri == null){
        	((ImageView)holder.mTextView.findViewById(R.id.prof_pic)).setImageResource(R.drawable.teal500);
        	((TextView)holder.mTextView.findViewById(R.id.image_replacement)).setText(mDataset.get(position).lastMessage.imageReplacement);
        }
        else{
        	((TextView)holder.mTextView.findViewById(R.id.image_replacement)).setText("");
        	ImageView contactimage = ((ImageView)holder.mTextView.findViewById(R.id.prof_pic));
        	contactimage.setImageURI(mDataset.get(position).lastMessage.thumbnailUri);
        	try {
        	    String nullString = contactimage.getDrawable().toString();
        	} catch (java.lang.NullPointerException ex) {
        	    contactimage.setImageResource(R.drawable.teal500);
        	    ((TextView)holder.mTextView.findViewById(R.id.image_replacement)).setText(mDataset.get(position).lastMessage.imageReplacement);
        	}
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

}
