package hills.sms.texter;

import hills.sms.texter.ContactsHelper.Contact;
import hills.sms.texter.texts.Conversation;
import hills.sms.texter.texts.Conversation.PaginatedConversationList;
import hills.sms.texter.texts.Conversation.PaginatedMessageList;
import hills.sms.texter.texts.Messages.MMSMessage;
import hills.sms.texter.texts.Messages.Message;
import hills.sms.texter.texts.Messages.SMSMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.util.Log;
import android.util.TypedValue;

public class TexterHelper {
	
	
	
	public static int ConvertToDP(int pixels, Resources resources){
		return (int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels, resources.getDisplayMetrics())+0.5);
	}
	
	public static Cursor getALLSMSQuery(ContentResolver resolver){
		final String[] projection = new String[]{"DISTINCT thread_id", "body","address", "_id","date"};
		String selection =  "thread_id IS NOT NULL) GROUP BY (thread_id";
		selection = null;
		Cursor query = resolver.query(Telephony.Sms.CONTENT_URI, projection,selection, null,null);
		return query;
	}
		
	public static Cursor getALLMMSQuery(ContentResolver resolver){
		final String[] mmsProjection = new String[]{"thread_id","date","m_id","_id"};
		Cursor query = resolver.query(Telephony.Mms.CONTENT_URI, mmsProjection,null,null,null);
		return query;
	}
	
	
	public static PaginatedConversationList getAllThreads(ContentResolver resolver){
		return new SMSMMSPaginatedConversation(resolver);
	}
	
	private static class SMSMMSPaginatedConversation implements PaginatedConversationList{

		List<Conversation> threads = new ArrayList<Conversation>();
		Set<Integer> foundThreadIDs = new HashSet<Integer>();
		Cursor mSMSQuery;
		Cursor mMMSQuery;
		ContentResolver mResolver;
		private int perPageCount = 20;
		
		public SMSMMSPaginatedConversation(ContentResolver resolver){
			mResolver = resolver;
			mSMSQuery = TexterHelper.getALLSMSQuery(resolver);
			mMMSQuery = TexterHelper.getALLMMSQuery(resolver);
			if (mSMSQuery!=null) mSMSQuery.moveToFirst();
			if (mMMSQuery!=null) mMMSQuery.moveToFirst();
		}
		@Override
		public List<Conversation> getNextPage() {
			Conversation convo = moveToNextThread();
			if (convo == null) return null;
			ArrayList<Conversation> nextPage = new ArrayList<Conversation>(perPageCount);
			nextPage.add(convo);
			for(int i=0;i<perPageCount-1;i++){
				convo = moveToNextThread();
				if (convo == null) break;
				nextPage.add(convo);
			}
			if (convo == null){
				mMMSQuery.close();
				mMMSQuery = null;
				mSMSQuery.close();
				mSMSQuery = null;
			}
			return nextPage;
		}
		
		private Conversation moveToNextThread(){
			long mostRecentDate = -999;
			long tempLong=-999;
			boolean isSMS=false;
			boolean isMMS=false;
			int threadID=-1000;
			while (true)
			{
				isSMS=false;
				isMMS=false;
				threadID=-1000;
				if (mSMSQuery!=null&&!mSMSQuery.isAfterLast()){
					mostRecentDate = mSMSQuery.getLong(mSMSQuery.getColumnIndex(Telephony.Sms.DATE));
					isSMS = true;
				}
				if (mMMSQuery!=null&&!mMMSQuery.isAfterLast()){
					tempLong = 1000*mMMSQuery.getLong(mMMSQuery.getColumnIndex(Telephony.Mms.DATE));
					if (tempLong>mostRecentDate) 
						{	
							mostRecentDate = tempLong;
							isMMS = true;
							isSMS = false;
						}
				}
				if (mostRecentDate == -999) break;

				if (isSMS){
					threadID = mSMSQuery.getInt(mSMSQuery.getColumnIndex("thread_id"));
				}
				if (isMMS){
					threadID = mMMSQuery.getInt(mMMSQuery.getColumnIndex("thread_id"));
				}
				if (!foundThreadIDs.contains(threadID)){
					break;
				}
				else{
					if (isSMS) mSMSQuery.moveToNext();
					if (isMMS) mMMSQuery.moveToNext();
				}
			}
			if (mostRecentDate==-999) return null;
			foundThreadIDs.add(threadID);
			if (isSMS) 
				return new Conversation(
					new SMSMessage(
						mSMSQuery.getString(1), 
						ContactsHelper.getContactInfo(mResolver,mSMSQuery.getString(2)), 
						mSMSQuery.getInt(0), 
						mSMSQuery.getInt(3),false)
					);
			if (isMMS)
			{
				MMSMessage message = new MMSMessage(mMMSQuery.getInt(
						mMMSQuery.getColumnIndex(Telephony.Mms._ID)),
						mMMSQuery.getInt(mMMSQuery.getColumnIndex("thread_id"))
		    			);
		    	TexterHelper.populateMMS(mResolver, message);
			    message.resolvePublicFields();
			    return new Conversation(message);
			}
			return null;
		}
	}
	
	
	public static PaginatedMessageList getAllMessages(ContentResolver resolver, int threadID){
		return new SMSMMSPaginatedMessages(resolver, threadID);
	}
	
	private static class SMSMMSPaginatedMessages implements PaginatedMessageList{

		ContentResolver mResolver;
		private int perPageCount = 20;
		private Cursor mSMSQuery = null;
		private Cursor mMMSQuery = null;
		private int mThreadID = -10;
		
		public SMSMMSPaginatedMessages(ContentResolver resolver, int threadID){
			mResolver = resolver;
			String selection = "thread_id = " + threadID;
			mThreadID = threadID;
			final String[] projection = new String[]{"*"};
			mSMSQuery = resolver.query(Telephony.Sms.CONTENT_URI, projection, selection, null, null);
			mMMSQuery = resolver.query(Telephony.Mms.CONTENT_URI, projection, selection, null, null);
			if (mSMSQuery!=null) mSMSQuery.moveToFirst();
			if (mMMSQuery!=null) mMMSQuery.moveToFirst();
			if (mSMSQuery.isAfterLast()) {
				mSMSQuery.close();
				mSMSQuery = null;
			}if (mMMSQuery.isAfterLast()) {
				mMMSQuery.close();
				mMMSQuery = null;
			}
		}
		
		@Override
		public List<Message> getNextPage() {
			ArrayList<Message> toReturn = null;
			Message m = getNextMessage();
			if (m!=null){
				toReturn = new ArrayList<Message>(perPageCount);
				toReturn.add(m);
				for (int i=0;i<perPageCount;i++){
					m = getNextMessage();
					if (m == null) break;
					toReturn.add(m);
				}
			}
			return toReturn;
		}
		private Message getNextMessage(){
			long lastDate = -999;
			long tempDate = -999;
			boolean isSMS = false;
			boolean isMMS = false;
			if (mSMSQuery != null){
				lastDate = mSMSQuery.getLong(mSMSQuery.getColumnIndex(Telephony.Sms.DATE));
				isSMS = true;
			}
			if (mMMSQuery != null){
				long tempLong = 1000*mMMSQuery.getLong(mMMSQuery.getColumnIndex(Telephony.Mms.DATE));
				if (tempLong>lastDate) 
				{	
					lastDate = tempLong;
						isMMS = true;
						isSMS = false;
				}
			}
			if (lastDate == -999) return null;
			if (isSMS){
				
				int type = mSMSQuery.getInt(mSMSQuery.getColumnIndex("type"));
				String person = mSMSQuery.getString(mSMSQuery.getColumnIndex("address"));
				Contact contact = ContactsHelper.getContactInfo(mResolver, person);
				boolean isMe = false;
				if (type==2){
					isMe = true;
				}
				Message m = new SMSMessage(
						mSMSQuery.getString(mSMSQuery.getColumnIndex("body")),
		    			contact,
		    			mThreadID,
		    			mSMSQuery.getInt(mSMSQuery.getColumnIndex("_id")),isMe);
				mSMSQuery.moveToNext();
				if (mSMSQuery.isAfterLast()){
					mSMSQuery.close();
					mSMSQuery = null;
				}
				return m;
			}
			if (isMMS){
				
				MMSMessage message = new MMSMessage(mMMSQuery.getInt(
						mMMSQuery.getColumnIndex(Telephony.Mms._ID)),
						mMMSQuery.getInt(mMMSQuery.getColumnIndex("thread_id"))
		    			);
		    	TexterHelper.populateMMS(mResolver, message);
		    	message.resolvePublicFields();
		    	
		    	mMMSQuery.moveToNext();
				if (mMMSQuery.isAfterLast()){
					mMMSQuery.close();
					mMMSQuery = null;
				}
				
		    	return message;
			}
			return null;
		}
		
	}
	
	public static Conversation getConversation(ContentResolver resolver, int threadID){
		String[] projection = new String[]{"DISTINCT thread_id", "body","address", "_id","date"};
		Cursor query = resolver.query(Telephony.Sms.CONTENT_URI, projection, "thread_id = " + threadID, null, null);
		Conversation toReturn = null;
		long smsDate = -1;
		long mmsDate = -1;
		Conversation smsConvo = null;
		Conversation mmsConvo = null;
		if (query != null && query.moveToFirst()) {
			smsDate = query.getLong(query.getColumnIndex("date"));
		    	smsConvo = new Conversation(
		    			new SMSMessage(
		    					query.getString(1), 
		    					ContactsHelper.getContactInfo(resolver,query.getString(2)), 
		    					query.getInt(0), 
		    					query.getInt(3),false)
		    			);
		}
		query.close();
		
		projection = new String[]{"thread_id", "_id","date"};
		query = resolver.query(Telephony.Mms.CONTENT_URI, projection, "thread_id = " + threadID, null, null);
		if (query != null && query.moveToFirst()) {
			mmsDate = query.getLong(query.getColumnIndex("date"))*1000;
			MMSMessage message = new MMSMessage(query.getInt(
					query.getColumnIndex(Telephony.Mms._ID)),
					query.getInt(query.getColumnIndex("thread_id"))
	    			);
	    	TexterHelper.populateMMS(resolver, message);
	    	
	    	message.resolvePublicFields();
	    	mmsConvo = new Conversation(message);
		}
		if (smsDate>mmsDate){
			return smsConvo;
		}
		else{
			return mmsConvo;
		}
	}
	
	
	public static List<Message> getSMSThread(ContentResolver resolver, Conversation thread){
		Map<String, Contact> lookupMap = new HashMap<String, Contact>();

		ArrayList<Message> messages = new ArrayList<Message>();
		
		String selection = "thread_id = " + thread.threadID;
		final String[] projection = new String[]{"*"};
		Cursor query = resolver.query(Telephony.Sms.CONTENT_URI, projection, selection, null, null);
		Cursor mmsQuery = resolver.query(Telephony.Mms.CONTENT_URI, projection, selection, null, null);
		
		
		if (query != null && mmsQuery!=null){
			query.moveToFirst();
			mmsQuery.moveToFirst();
			while (true){
				long smsDate = -9999;
				if (!query.isAfterLast())
				{
					//1417490143
					//1412977628
					smsDate = query.getLong(query.getColumnIndex(Telephony.Sms.DATE));
				}
				long mmsInDate = -9999;
				if(!mmsQuery.isAfterLast())
				{
					mmsInDate = mmsQuery.getLong(mmsQuery.getColumnIndex("date"))*1000;
					if (mmsInDate == 0)
						mmsInDate = mmsQuery.getLong(mmsQuery.getColumnIndex(Telephony.Mms.DATE));	
				}
				if (mmsInDate == -9999 && smsDate == -9999)
					break;
				/*
				Calendar calendar = Calendar.getInstance();

				calendar.setTimeInMillis(smsDate);
				Date finaldate = calendar.getTime();
				String smsTime = finaldate.toString();
				
				calendar.setTimeInMillis(mmsInDate*1000);
				Date mmsfinaldate = calendar.getTime();
				String mmsTime = mmsfinaldate.toString();
				*/
				
				if (smsDate>mmsInDate){
					int type = query.getInt(query.getColumnIndex("type"));
					String person = query.getString(query.getColumnIndex("address"));
					Contact contact = ContactsHelper.getContactInfo(resolver, person);
					boolean isMe = false;
					if (type==2){
						isMe = true;
					}
			    	messages.add(new SMSMessage(
			    			query.getString(query.getColumnIndex("body")),
			    			contact,
			    			thread.threadID,
			    			query.getInt(query.getColumnIndex("_id")),isMe));
			    	query.moveToNext();
				}
				else{
			    	MMSMessage message = new MMSMessage(mmsQuery.getInt(
			    			mmsQuery.getColumnIndex(Telephony.Mms._ID)),
			    			mmsQuery.getInt(mmsQuery.getColumnIndex("thread_id"))
			    			);
			    	TexterHelper.populateMMS(resolver, message);
			    	
			    	message.resolvePublicFields();
			    	messages.add(message);
			    	
			    	mmsQuery.moveToNext();
				}
		    	
		    }
		}
		query.close();
		mmsQuery.close();
		return messages;
	}
	
	public static List<Message> getMMSThread(ContentResolver resolver, Conversation thread){
		Map<String, Contact> lookupMap = new HashMap<String, Contact>();

		ArrayList<Message> messages = new ArrayList<Message>();
		String selection = "thread_id = " + thread.threadID;
		final String[] projection = new String[]{"*"};
		Cursor query = resolver.query(Telephony.Mms.CONTENT_URI, projection, selection, null, null);
		if (query != null && query.moveToFirst()) {
		    do {
		    	int type = query.getInt(query.getColumnIndex("type"));
		    	Contact contact = new Contact("Me", "", null, null);
		    	if (type!=2)
		    	{
		    		String person =query.getString(query.getColumnIndex("address"));
		    		if (lookupMap.containsKey(person)) contact = lookupMap.get(person);
		    		else{
		    			contact = ContactsHelper.getContactInfo(resolver, person);
		    			lookupMap.put(person, contact);
		    		}
		    	}
		    	//messages.add(new SMSMessage(query.getString(query.getColumnIndex("body")),contact));
		    } while (query.moveToNext());
		}
		return messages;
	}
	
	public static List<Conversation> getMMSThreads(ContentResolver resolver){
		/*
		Column: _id
		Column: thread_id
		Column: date
		Column: date_sent
		Column: msg_box
		Column: read
		Column: m_id
		Column: sub
		Column: sub_cs
		Column: ct_t
		Column: ct_l
		Column: exp
		Column: m_cls
		Column: m_type
		Column: v
		Column: m_size
		Column: pri
		Column: rr
		Column: rpt_a
		Column: resp_st
		Column: st
		Column: tr_id
		Column: retr_st
		Column: retr_txt
		Column: retr_txt_cs
		Column: read_status
		Column: ct_cls
		Column: resp_txt
		Column: d_tm
		Column: d_rpt
		Column: locked
		Column: htc_category
		Column: cs_timestamp
		Column: cs_id
		Column: cs_synced
		Column: seen
		Column: extra
		Column: phone_type
		Column: date2
		Column: smilext
		Column: text_only
		Column: sim_slot
		 */
		List<Conversation> threadIDs = new ArrayList<Conversation>();
		/*
		final String[] projection = new String[]{"DISTINCT thread_id"};
		
		Cursor query = resolver.query(Telephony.Mms.CONTENT_URI, projection, null, null, null);
		
		if (query != null && query.moveToFirst()) {
		    do {
		    	threadIDs.add(new TextThread(
		    			query.getInt(0),"MMS Message","MMS number"
		    			));
		    } while (query.moveToNext());
		}*/
		return threadIDs;
		
	}
	
	public static boolean partExists(ContentResolver resolver, MMSMessage message){
		int mmsId = message.mMessageID;
		String selectionPart = "mid=" + mmsId;
		Uri uri = Uri.parse("content://mms/part");
		Cursor cursor = resolver.query(uri, null,
			    selectionPart, null, null);
		if (cursor.moveToFirst()) {
			return true;
		}
		return false;
	}
	
	public static boolean populateMMS(ContentResolver resolver, MMSMessage message){
		//if (!partExists(resolver, message)) 
			//return false;
		getAllMMSInfo(resolver, message);
		insertAddressMMSInfo(resolver, message);
		return true;
	}
	
	public static void getAllMMSInfo(ContentResolver resolver, MMSMessage message){
		int mmsId = message.mMessageID;
		String selectionPart = "mid=" + mmsId;
		Uri uri = Uri.parse("content://mms/part");
		Cursor cursor = resolver.query(uri, null,
		    selectionPart, null, null);
		if (cursor.moveToFirst()) {
		    do {
		        String partId = cursor.getString(cursor.getColumnIndex("_id"));
		        String type = cursor.getString(cursor.getColumnIndex("ct"));
		        if ("text/plain".equals(type)) {
		            String data = cursor.getString(cursor.getColumnIndex("_data"));
		            String body="";
		            if (data != null) {
		                // implementation of this method below
		                body = getMmsText(resolver,partId);
		            } else {
		                body = cursor.getString(cursor.getColumnIndex("text"));
		            }
		            message.mTextContent.add(body);
		        }
		        if ("image/jpeg".equals(type) || "image/bmp".equals(type) ||
		                "image/gif".equals(type) || "image/jpg".equals(type) ||
		                "image/png".equals(type)) {
		        	message.mImageUris.add(Uri.parse("content://mms/part/" + partId));
		        }
		    } while (cursor.moveToNext());
		}
	}
	public static void insertAddressMMSInfo(ContentResolver resolver, MMSMessage message) {
		int id = message.mMessageID;
	    String selectionAdd = new String("msg_id=" + id);
	    String uriStr = MessageFormat.format("content://mms/{0}/addr", id);
	    Uri uriAddress = Uri.parse(uriStr);
	    Cursor cAdd = resolver.query(uriAddress, null,
	        selectionAdd, null, null);
	    String name = null;
	    Contact from = null;
	    Map<String,Contact> toContacts = new HashMap<String,Contact>();
	    List<String> names = new ArrayList<String>();
	    if (cAdd.moveToFirst()) {
	        do {
	            String number = cAdd.getString(cAdd.getColumnIndex("address"));
	            names.add(number);
	            try {
                    Long.parseLong(number.replaceAll("\\D+",""));
                    name = number;
                } catch (NumberFormatException nfe) {
                    if (name == null) {
                        name = number;
                    }
                }
	            if (name != null && !name.equals("insert-address-token")) {
	                int type = cAdd.getInt(cAdd.getColumnIndex("type"));
	                Contact c = ContactsHelper.getContactInfo(resolver, name);
	                if (c.displayName.equals("Gillian")){
	                	int ba=0;
	                	ba++;
	                }
	                if (type!=151)
	                {
	                	from = c;
	                }
	                //else
	                {
	                	toContacts.put(c.phoneNumber, c);
	                }
	            }
	        } while (cAdd.moveToNext());
	    }
	    if (cAdd != null) {
	        cAdd.close();
	    }
	    message.isMe = true;
	    List<Contact> toContactsList = new ArrayList<Contact>(toContacts.values());
	    if (from!=null)
		{
	    	if (toContactsList.size() == 1)
	    	{
				for (int i=0;i<toContactsList.size();i++){
					if (from.phoneNumber.equals(toContactsList.get(i).phoneNumber))
					{
						message.isMe=false;
						break;
					}
				}
	    	}
	    	else
	    	{
	    		message.isMe = false;
	    	}
		}
	    
	    message.mContacts = toContactsList;
	    message.from = from;
	}
	
	public static String getMmsText(ContentResolver resolver, String id) {
	    Uri partURI = Uri.parse("content://mms/part/" + id);
	    InputStream is = null;
	    StringBuilder sb = new StringBuilder();
	    try {
	        is = resolver.openInputStream(partURI);
	        if (is != null) {
	            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
	            BufferedReader reader = new BufferedReader(isr);
	            String temp = reader.readLine();
	            while (temp != null) {
	                sb.append(temp);
	                temp = reader.readLine();
	            }
	        }
	    } catch (IOException e) {}
	    finally {
	        if (is != null) {
	            try {
	                is.close();
	            } catch (IOException e) {}
	        }
	    }
	    return sb.toString();
	}
	
}


