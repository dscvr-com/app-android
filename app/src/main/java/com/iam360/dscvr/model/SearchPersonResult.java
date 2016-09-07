package com.iam360.dscvr.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mariel on 6/8/2016.
 */
public class SearchPersonResult {

    List<Person> persons;

    public SearchPersonResult() {
        persons = new ArrayList<>();
    }

    public List<Person> getPersons() {
        return persons;
    }
}
