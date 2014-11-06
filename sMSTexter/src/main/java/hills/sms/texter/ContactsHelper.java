package hills.sms.texter;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.text.format.Time;

public class ContactsHelper {

	private static Map<String, Contact> cache = new HashMap<String, Contact>();
	private static long lastClear = 0;
	private static int cacheCount = 0;
	private static int nonCacheCount = 0;
	private static void clearCache(){
		cache.clear();
	}
	
	
	public static class Contact{
		public String displayName;
		public String phoneNumber;
		public Uri photoURI;
		public Uri thumbnailURI;
		public Contact(String displayName, String phoneNumber, Uri photoURI, Uri thumbnailURI){
			this.displayName = displayName;
			this.phoneNumber = phoneNumber;
			this.photoURI = photoURI;
			this.thumbnailURI = thumbnailURI;
		}
	}
	
	public static Contact getContactInfo(ContentResolver resolver, String phoneNumber){
		Time now = new Time();
		now.setToNow();
		long nowMils = now.toMillis(false);
		if (nowMils - lastClear > 10000) 
		{
			clearCache();
			lastClear = nowMils;
		}
		
		if (cache.containsKey(phoneNumber)) 
		{
			cacheCount++;
			return cache.get(phoneNumber);
		}
		nonCacheCount++;
		//new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME }
		Contact toReturn = new Contact("",phoneNumber,null,null);
		
		Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber));
        Cursor phoneCursor = resolver.query(uri,
        		new String[] {
        		PhoneLookup.DISPLAY_NAME,
        		PhoneLookup.NUMBER,
        		PhoneLookup.PHOTO_URI,
        		PhoneLookup.PHOTO_THUMBNAIL_URI,
        		}
        , null, null, null);
        for (phoneCursor.moveToFirst(); !phoneCursor.isAfterLast(); phoneCursor.moveToNext()) {
        	String photouri = phoneCursor.getString(2);

        	Uri photoUriObject = null; 
        	Cursor photoQ=null;
        	if (photouri!=null)
        	{
        		photoUriObject = Uri.parse(photouri);
	        	
        	}
        	
        	Uri thumbUriObject = null;
        	String thumbUri = phoneCursor.getString(3);
        	if (thumbUri!=null)
        	{
	        	thumbUriObject = Uri.parse(thumbUri);
        	}
        	toReturn = new Contact(
        			(phoneCursor.getString(0).equals(phoneCursor.getString(1)) ? "" : phoneCursor.getString(0)),
        			phoneCursor.getString(1), 
        			photoUriObject, 
        			thumbUriObject);
        }
        if (phoneCursor != null && !phoneCursor.isClosed()) {
    		phoneCursor.close();
        }
        cache.put(phoneNumber, toReturn);
        return toReturn;
	}
	
}
