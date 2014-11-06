package com.hillsalex.metatext.persons;

import android.net.Uri;

/**
 * Created by alex on 11/5/2014.
 */
public class Person {

    private Uri fullsizeUri;
    private Uri thumbUri;
    private String displayName;
    private int contactID;
    private String phoneNumber;

    public int getContactID() {
        return contactID;
    }

    public void setContactID(int contactID) {
        this.contactID = contactID;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Uri getFullsizeUri() {
        return fullsizeUri;
    }

    public void setFullsizeUri(Uri fullsizeUri) {
        this.fullsizeUri = fullsizeUri;
    }

    public Uri getThumbUri() {
        return thumbUri;
    }

    public void setThumbUri(Uri thumbUri) {
        this.thumbUri = thumbUri;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Person(int contactID, String phoneNumber, Uri fullsizeUri, Uri thumbUri, String displayName){
        this.contactID=contactID;
        this.phoneNumber=phoneNumber;
        this.fullsizeUri=fullsizeUri;
        this.thumbUri=thumbUri;
        this.displayName=displayName;
    }

}
