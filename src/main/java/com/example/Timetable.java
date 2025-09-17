package com.example;

public class Timetable {
    private static final int DAYS = 7;
    private static final int SLOTS_PER_DAY = 48; // 24 hours * 2
    private Activity[][] slots = new Activity[DAYS][SLOTS_PER_DAY];

    public boolean placeActivity(Activity activity) {
        // Dummy implementation: always returns true
        // Real implementation would place activity at its fixed time
        return true;
    }

    public boolean placeTaskIfPossible(Task task) {
        int needed = task.getDurationInSlots();
        for (int day = 0; day < DAYS; day++) {
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
        int duration = activity.getDurationInSlots();
        for (int i = 0; i < duration; i++) {
            if (day < 0 || day >= DAYS || startSlot + i < 0 || startSlot + i >= SLOTS_PER_DAY) {
                throw new IllegalArgumentException("Invalid day or slot for activity placement");
            }
            if (slots[day][startSlot + i] != null) {
                throw new IllegalStateException("Slot already occupied at day " + day + ", slot " + (startSlot + i));
            }
            slots[day][startSlot + i] = activity;
        }
    }

    public Activity[][] getSlots() {
        return slots;
    }
}
