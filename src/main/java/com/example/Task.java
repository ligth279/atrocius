package com.example;

public class Task extends Activity {
    private Integer targetDay; // null means any day
    private String preferredTime; // "morning", "evening", or "any"

    public Task(String name, int durationInSlots) {
        super(name, durationInSlots);
        this.targetDay = null;
        this.preferredTime = "any";
    }

    public Task(String name, int durationInSlots, Integer targetDay) {
        super(name, durationInSlots);
        this.targetDay = targetDay;
        this.preferredTime = "any";
    }

    public Task(String name, int durationInSlots, Integer targetDay, String preferredTime) {
        super(name, durationInSlots);
        this.targetDay = targetDay;
        this.preferredTime = preferredTime == null ? "any" : preferredTime;
    }

    public Integer getTargetDay() {
        return targetDay;
    }

    public String getPreferredTime() {
        return preferredTime;
    }
}
