package com.iam360.iam360.bus;

import com.iam360.iam360.model.Person;

/**
 * @author Nilan Marktanner
 * @date 2015-12-09
 */
public class PersonReceivedEvent {
    Person person;

    public PersonReceivedEvent(Person person) {
        this.person = person;
    }

    public Person getPerson() {
        return person;
    }
}
