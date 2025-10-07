package com.example;

public class Event extends Activity {
    private int startDay;
    private int startSlot;

    public Event(String name, int durationInSlots, int startDay, int startSlot) {
        super(name, durationInSlots);
        this.startDay = startDay;
        this.startSlot = startSlot;
    }

    public int getTargetDay() {
        return startDay;
    }

    public int getStartSlot() {
        return startSlot;
    }
}
