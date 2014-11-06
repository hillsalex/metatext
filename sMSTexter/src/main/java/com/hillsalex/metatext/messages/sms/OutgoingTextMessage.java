package com.hillsalex.metatext.messages.sms;

import android.os.Parcel;
import android.os.Parcelable;

import com.hillsalex.metatext.persons.Person;
import com.hillsalex.metatext.persons.Persons;

/**
 * Created by alex on 11/6/2014.
 */
public class OutgoingTextMessage implements Parcelable {

    private final Persons persons;
    private final String message;

    public String getMessageBody(){
        return message;
    }

    public boolean isGroup(){
        return !persons.isSinglePerson();
    }

    public Persons getPersons(){
        return persons;
    }

    public String getIndividualAddress(){
        if (!isGroup()){ return persons.getPersons().get(0).getPhoneNumber(); }
        return null;
    }


    /* everything below here is for implementing Parcelable */

    // 99.9% of the time you can just ignore this
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(message);
        out.writeInt(persons.getPersons().size());
        for (Person p : persons.getPersons()){
            out.writeString(p.getPhoneNumber());
        }
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<OutgoingTextMessage> CREATOR = new Parcelable.Creator<OutgoingTextMessage>() {
        public OutgoingTextMessage createFromParcel(Parcel in) {
            return new OutgoingTextMessage(in);
        }

        public OutgoingTextMessage[] newArray(int size) {
            return new OutgoingTextMessage[size];
        }
    };

    public OutgoingTextMessage(Persons persons, String body){
        this.persons = persons;
        this.message = body;
    }
    public OutgoingTextMessage(Person person, String body){
        persons = new Persons(person);
        message = body;
    }

    // example constructor that takes a Parcel and gives you an object populated with it's values
    public OutgoingTextMessage(Parcel in) {
        String body = in.readString();
        int count = in.readInt();
        Persons ps = new Persons();
        Person p;
        for (int i=0;i<count;i++){
            p = new Person(0,in.readString(),null,null,"");
            ps.addPerson(p);
        }
        this.persons = ps;
        this.message = body;
    }
}