package hills.sms.texter.texts;

import java.util.List;

import android.content.ContentResolver;
import hills.sms.texter.TexterHelper;
import hills.sms.texter.texts.Messages.Message;


public class Conversation {
	public int threadID;
	public Message lastMessage;
	private List<Message> mMessages=null;
	
	public Conversation(Message lastMessage){
		this.lastMessage = lastMessage;
		this.threadID = lastMessage.threadID;
	}
	public Message get(ContentResolver resolver, int position){
		if (mMessages==null){
			mMessages = TexterHelper.getSMSThread(resolver, this);
		}
		return mMessages.get(position);
	}
	public int size(ContentResolver resolver){
		if (mMessages==null){
			mMessages = TexterHelper.getSMSThread(resolver, this);
		}
		return mMessages.size();
	}
	
	public interface PaginatedMessageList{
		public List<Message> getNextPage();
	}
	
	public interface PaginatedConversationList{
		
		public List<Conversation> getNextPage();
		
	}
}
