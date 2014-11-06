package hills.sms.texter.texts;

import java.util.ArrayList;
import java.util.List;

import hills.sms.texter.ContactsHelper.Contact;
import android.net.Uri;

public class Messages {
	public static abstract class Message {
		public String displayMessage;
		public String displayName;
		public String threadName;
		
		public Contact from=null;
		
		public String imageReplacement;
		public Uri thumbnailUri;
		public Uri fullsizeUri;
		
		public List<Uri> imageUris;
		
		public int threadID;
		public int ID;
		
		public boolean isMe = false;
	}
	
	public static class SMSMessage extends Message{
		
		public SMSMessage(String message, Contact contact, int threadID, int ID, boolean isMe){
			this.from = contact;
			this.isMe = isMe;
			this.displayMessage = message;
			if (contact.displayName.equals("")){
				this.displayName = contact.phoneNumber;
				this.threadName = contact.phoneNumber;
			}
			else
			{
				this.displayName = contact.displayName;
				this.threadName = contact.displayName;
			}
			this.thumbnailUri = contact.thumbnailURI;
			this.fullsizeUri = contact.photoURI;
			if (contact.displayName.equals("")){
				this.imageReplacement = "#";
			}
			else{
				String[] parts = contact.displayName.split(" ");
				if (parts.length>1) this.imageReplacement = parts[0].substring(0, 1) + parts[1].substring(0, 1);
				else this.imageReplacement = parts[0].substring(0,1);
			}
			this.threadID = threadID;
			this.ID = ID;
		}
	}
	
	public static class MMSMessage extends Message{
		public int mMessageID;
		public List<Uri> mImageUris = new ArrayList<Uri>();
		public List<String> mTextContent = new ArrayList<String>();
		public List<Contact> mContacts = new ArrayList<Contact>();
		public MMSMessage(int messageID, int threadID){
			mMessageID = messageID;
			this.threadID = threadID;
		}
		public void resolvePublicFields(){
			if (!mTextContent.isEmpty())
			{
			    StringBuilder sb = new StringBuilder();
				for (int i=0;i<mTextContent.size();i++){
					sb.append(mTextContent.get(i));
					if (i!=mTextContent.size()-1){
						sb.append("\n");
					}
				}
				this.displayMessage = sb.toString();
			}
			else{
				this.displayMessage = null;
			}
			if (mImageUris.size()>0) imageUris = mImageUris;
			if (mContacts.size()>0){
				this.thumbnailUri = mContacts.get(0).thumbnailURI;
				this.fullsizeUri = mContacts.get(0).photoURI;
				
			
				if (mContacts.get(0).displayName.equals("")) 
					this.imageReplacement = "#";
				else{
					String[] parts = mContacts.get(0).displayName.split(" ");
					if (parts.length>1) this.imageReplacement = parts[0].substring(0, 1) + parts[1].substring(0, 1);
					else this.imageReplacement = parts[0].substring(0,1);
				}
			}
			StringBuilder sb = new StringBuilder();
			
			for (int i=0;i<mContacts.size();i++){
				Contact contact = mContacts.get(i);
				if (!contact.displayName.equals("")){
					sb.append(contact.displayName);
				}
				else{
					sb.append(contact.phoneNumber);
				}
				if (i!=mContacts.size()-1) sb.append("; ");
				
			}
			this.displayName = sb.toString();
			this.threadName = this.displayName;
			//if (this.displayMessage==null)//toAdd = "\n"+this.displayMessage;
			//this.displayMessage = "To: " + this.threadName + toAdd;
			if (this.from!=null)
				this.displayName = from.displayName; 
		}
		
	}
}
