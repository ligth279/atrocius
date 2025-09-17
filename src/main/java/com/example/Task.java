package com.example;

public class Task extends Activity {
    private Integer targetDay; // null means any day

    public Task(String name, int durationInSlots) {
        super(name, durationInSlots);
        this.targetDay = null;
    }

    public Task(String name, int durationInSlots, Integer targetDay) {
        super(name, durationInSlots);
        this.targetDay = targetDay;
    }

    public Integer getTargetDay() {
        return targetDay;
    }
}
