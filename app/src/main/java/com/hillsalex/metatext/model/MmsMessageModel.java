package com.hillsalex.metatext.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.support.v4.util.Pair;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;

import com.hillsalex.metatext.database.ActiveDatabases;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 11/10/2014.
 */
public class MmsMessageModel extends MessageModel{

    public ArrayList<Uri> mImageUris = new ArrayList<>();


    public MmsMessageModel(long id, long date, long threadId,boolean fromMe,boolean read){
        super(id, new String[]{},date, threadId,fromMe,read);
        try {
            resolveMessage();
            /*if (!fromMe && this.contactList.size()>0) {
                this.senderContact = this.contactList.get(0);
            }*/
        } catch (ActiveDatabases.NeedActiveContextException e) {
            e.printStackTrace();
        }
    }

    private MmsMessageModel(long id, String[] recipients, long date, long threadId, boolean fromMe){
        super(-1,recipients,date,threadId,fromMe,true);
    }

    public static MmsMessageModel getFakeMmsMessageModel(String[] recipients, long date, long threadId, boolean fromMe, ArrayList<Uri> imageUris){
        MmsMessageModel m = new MmsMessageModel(-1l,recipients,date,threadId,fromMe);
        m.isFake=true;
        m.status=STATUS_SENDING;
        m.mImageUris = imageUris;
        return m;
    }

    private void resolveMessage() throws ActiveDatabases.NeedActiveContextException {
        ContentResolver resolver = ActiveDatabases.getInstance().mContext.getContentResolver();
        String selectionPart = "mid=" + id;
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
                    this.body+= body;
                }
                if ("image/jpeg".equals(type) || "image/bmp".equals(type) ||
                        "image/gif".equals(type) || "image/jpg".equals(type) ||
                        "image/png".equals(type)) {
                    mImageUris.add(Uri.parse("content://mms/part/" + partId));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        Pair<Boolean,List<String>> addresses = getAddresses(resolver);
        this.contactList = ContactList.getByNumbers(addresses.second,true);
        //this.fromMe = addresses.first;
        //if (addresses.first) this.contactList.add(0,Contact.getMe(false));
    }

    public Pair<Boolean,List<String>> getAddresses(ContentResolver resolver){
        String myNumber="";
        try {
            myNumber= ((TelephonyManager)ActiveDatabases.getInstance().mContext
                    .getSystemService(Context.TELEPHONY_SERVICE))
                    .getLine1Number();
        } catch (ActiveDatabases.NeedActiveContextException e) {
            e.printStackTrace();
        }

        final String[] projection = new String[]{"address","type"};
        String selectionAdd = new String("msg_id="+id);
        String uriStr = "content://mms/"+id+"/addr";
        Uri uriAddress = Uri.parse(uriStr);
        Cursor cAdd = resolver.query(uriAddress,projection,selectionAdd,null,null);
        List<String> toReturn = new ArrayList<>();
        boolean me = false;
        if (cAdd!= null && cAdd.moveToFirst()){
            do {
                String number = cAdd.getString(0);
                if (number.equals("insert-address-token")){
                    me=true;
                }
                else if(PhoneNumberUtils.compare(number,myNumber))
                {
                }
                else {
                    if (cAdd.getInt(1)==137){
                        senderContact = Contact.get(number,true);
                    }
                    toReturn.add(number);
                }
            } while (cAdd.moveToNext());
        }
        if (cAdd != null) {
            cAdd.close();
        }
        return new Pair<Boolean, List<String>>(me,toReturn);
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


    @Override
    public String toString() {
        if (body!="")
            return body;
        else if (mImageUris.size()>0)
            return "Last message is an image";
        else return "Non-text, non-image content";
    }
}
