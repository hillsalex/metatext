package com.hillsalex.metatext.database.models;

import com.hillsalex.metatext.persons.Person;
import com.hillsalex.metatext.persons.Persons;

/**
 * Created by alex on 11/5/2014.
 */
public abstract class MessageModel {

    private String body;
    private Persons addresses;

    protected MessageModel(String body, Persons addresses){
        this.body=body;
        this.addresses=addresses;
    }

    public boolean isSMS(){return false;}
    public String getBody(){return body;}
    public Person getSolePerson(){
        return addresses.getPersons().get(0);
    }

}
