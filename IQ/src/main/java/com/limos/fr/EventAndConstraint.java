package com.limos.fr;

import org.w3c.dom.events.Event;

public class EventAndConstraint<E extends TimestampedEvent> {
    private E event;
    private String constraint;

    public EventAndConstraint(E event, String constraint) {
        this.event = event;
        this.constraint = constraint;
    }

    public EventAndConstraint(E event) {
        this.event = event;
    }

    public void setConstraint(String constraint) {
        this.constraint = constraint;
    }

    public E getEvent() {
        return event;
    }

    public String getConstraint() {
        return constraint;
    }
}
