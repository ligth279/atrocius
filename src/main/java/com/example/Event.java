package com.example;

import java.time.LocalDate;

public class Event extends Activity {
    private final LocalDate eventDate;
    private final int startSlot;

    public Event(String name, int durationInSlots, LocalDate eventDate, int startSlot) {
        super(name, durationInSlots);
        this.eventDate = eventDate;
        this.startSlot = startSlot;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public int getStartSlot() {
        return startSlot;
    }
}
