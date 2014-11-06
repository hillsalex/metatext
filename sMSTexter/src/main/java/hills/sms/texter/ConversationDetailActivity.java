package hills.sms.texter;

import java.util.ArrayList;
import java.util.List;

import hills.sms.texter.MainActivity.ConversationAdapter;
import hills.sms.texter.MainActivity.MyAdapter;
import hills.sms.texter.MainActivity.ConversationAdapter.ViewHolder;
import hills.sms.texter.texts.Conversation;
import hills.sms.texter.texts.Messages.Message;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * An activity representing a single Conversation detail screen. This activity
 * is only used on handset devices. On tablet-size devices, item details are
 * presented side-by-side with a list of items in a
 * {@link ConversationListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link ConversationDetailFragment}.
 */
public class ConversationDetailActivity extends ActionBarActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conversation_detail);

		// Show the Up button in the action bar.
		if (getActionBar()!=null)
			getActionBar().setDisplayHomeAsUpEnabled(true);

		
		// savedInstanceState is non-null when there is fragment state
		// saved from previous configurations of this activity
		// (e.g. when rotating the screen from portrait to landscape).
		// In this case, the fragment will automatically be re-added
		// to its container so we don't need to manually add it.
		// For more information, see the Fragments API guide at:
		//
		// http://developer.android.com/guide/components/fragments.html
		//
		if (savedInstanceState == null) {
			// Create the detail fragment and add it to the activity
			// using a fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(
					ConversationDetailFragment.ARG_ITEM_ID,
					getIntent().getStringExtra(
							ConversationDetailFragment.ARG_ITEM_ID));
			ConversationDetailFragment fragment = new ConversationDetailFragment();
			fragment.setArguments(arguments);
			getFragmentManager().beginTransaction()
					.add(R.id.conversation_detail_container, fragment).commit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			navigateUpTo(new Intent(this, ConversationListActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	

}
