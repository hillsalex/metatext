package com.hillsalex.metatext.persons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by alex on 11/5/2014.
 */
public class Persons {

    private List<Person> mPersons;

    public Persons(Person person){
        mPersons = new ArrayList<Person>(1);
        mPersons.add(person);
    }
    public Persons(List<Person> persons){
        mPersons = new ArrayList<Person>(persons);
    }
    public Persons() {mPersons = new ArrayList<Person>();}
    public boolean isSinglePerson(){
        return mPersons.size() == 1;
    }
    public List<Person> getPersons(){
        return mPersons;
    }
    public void addPerson(Person p) { mPersons.add(p);}

}
