package co.optonaut.optonaut.bus;

import co.optonaut.optonaut.model.Person;

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
