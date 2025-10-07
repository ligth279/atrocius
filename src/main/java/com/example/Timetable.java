package com.example;

public class Timetable {
    private final int days;
    private static final int SLOTS_PER_DAY = 48; // 24 hours * 2
    private Activity[][] slots;

    public Timetable(int days) {
        this.days = days;
        this.slots = new Activity[days][SLOTS_PER_DAY];
    }

    public boolean placeActivity(Activity activity) {
        // Dummy implementation: always returns true
        // Real implementation would place activity at its fixed time
        return true;
    }

    public boolean placeTaskIfPossible(Task task) {
        int needed = task.getDurationInSlots();
        for (int day = 0; day < days; day++) {
            for (int start = 0; start <= SLOTS_PER_DAY - needed; start++) {
                boolean fits = true;
                for (int i = 0; i < needed; i++) {
                    if (slots[day][start + i] != null) {
                        fits = false;
                        break;
                    }
                }
                if (fits) {
                    for (int i = 0; i < needed; i++) {
                        slots[day][start + i] = task;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public void placeAt(Activity activity, int day, int startSlot) {
        placeAt(activity, day, startSlot, false);
    }

    // Overloaded: force=true allows overwriting existing activities (for events)
    public void placeAt(Activity activity, int day, int startSlot, boolean force) {
        int duration = activity.getDurationInSlots();
        int d = day;
        int s = startSlot;
        for (int i = 0; i < duration; i++) {
            if (d < 0 || d >= days || s < 0 || s >= SLOTS_PER_DAY) {
                // Wrap to next day if slot overflows
                d = (d + 1) % days;
                s = 0;
            }
            if (!force && slots[d][s] != null) {
                throw new IllegalStateException("Slot already occupied at day " + d + ", slot " + s);
            }
            slots[d][s] = activity;
            s++;
        }
    }

    public Activity[][] getSlots() {
        return slots;
    }
}
