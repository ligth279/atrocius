package com.example;

public abstract class Activity {
    private String name;
    private int durationInSlots; // 1 slot = 15 minutes

    public Activity(String name, int durationInSlots) {
        this.name = name;
        this.durationInSlots = durationInSlots;
    }

    public String getName() {
        return name;
    }

    public int getDurationInSlots() {
        return durationInSlots;
    }
}
