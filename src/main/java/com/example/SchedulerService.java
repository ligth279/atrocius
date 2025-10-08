package com.example;

import java.util.*;

public class SchedulerService {
    public ScheduleResult generateTimetable(
            List<Integer> workdays, int workStartSlot, int workDurationSlots,
            int sleepDurationSlots,
            List<Task> tasks,
            List<Event> events,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate) {
        int days = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        Timetable timetable = new Timetable(days);
        List<Task> unscheduledTasks = new ArrayList<>();


        // 0. Place events (highest priority, always overwrite)
        for (Event event : events) {
            java.time.LocalDate eventDate = event.getEventDate();
            if (!eventDate.isBefore(startDate) && !eventDate.isAfter(endDate)) {
                int dayIdx = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, eventDate);
                timetable.placeAt(event, dayIdx, event.getStartSlot(), true); // force overwrite
            }
        }

        // 1. Place sleep (next priority, only if not occupied by event)
        for (int day = 0; day < days; day++) {
            Activity[] slots = timetable.getSlots()[day];
            int halfSleep = sleepDurationSlots / 2;
            // Before midnight: slots 40-47 (20:00-00:00)
            int sleepPlaced = 0;
            for (int s = 40; s < 48 && sleepPlaced < halfSleep; s++) {
                if (slots[s] == null) {
                    slots[s] = new FixedActivity("Sleep", 1);
                    sleepPlaced++;
                }
            }
            // After midnight: slots 0-7 (00:00-04:00)
            for (int s = 0; s < 8 && sleepPlaced < sleepDurationSlots; s++) {
                if (slots[s] == null) {
                    slots[s] = new FixedActivity("Sleep", 1);
                    sleepPlaced++;
                }
            }
        }

        // 2. Place work (repeat every week, only if not occupied by event or sleep)
        for (int day = 0; day < days; day++) {
            java.time.LocalDate date = startDate.plusDays(day);
            int weekday = date.getDayOfWeek().getValue(); // 1=Mon, 7=Sun
            if (workdays.contains(weekday - 1)) {
                Activity[] slots = timetable.getSlots()[day];
                boolean canPlace = true;
                for (int i = 0; i < workDurationSlots; i++) {
                    int slotIdx = workStartSlot + i;
                    if (slotIdx >= 48 || slots[slotIdx] != null) {
                        canPlace = false;
                        break;
                    }
                }
                if (canPlace) {
                    for (int i = 0; i < workDurationSlots; i++) {
                        slots[workStartSlot + i] = new FixedActivity("Work", workDurationSlots);
                    }
                }
            }
        }

        // 3. Place tasks (repeat every week, only if not occupied by event, sleep, or work)
        for (Task task : tasks) {
            for (int day = 0; day < days; day++) {
                java.time.LocalDate date = startDate.plusDays(day);
                int weekday = date.getDayOfWeek().getValue(); // 1=Mon, 7=Sun
                if (task.getTargetDay() != null && weekday - 1 != task.getTargetDay()) continue;
                Activity[] slots = timetable.getSlots()[day];

                // Preferred time window slots
                int morningStart = 0, morningEnd = 23;   // 00:00 to 12:00 (slot 0-23)
                int eveningStart = 32, eveningEnd = 47;  // 16:00 to 24:00 (slot 32-47)
                boolean placed = false;

                // Try preferred window first
                String pref = task.getPreferredTime();
                if ("morning".equals(pref)) {
                    placed = tryPlaceTaskInWindow(slots, task, morningStart, morningEnd);
                } else if ("evening".equals(pref)) {
                    placed = tryPlaceTaskInWindow(slots, task, eveningStart, eveningEnd);
                }

                // If not placed, try anywhere
                if (!placed) {
                    placed = tryPlaceTaskInWindow(slots, task, 0, 47);
                }
            }
        }
        return new ScheduleResult(timetable, unscheduledTasks);
    }

    // Helper: Try to place task in a window of slots, returns true if placed
    private boolean tryPlaceTaskInWindow(Activity[] slots, Task task, int start, int end) {
        int currentBlock = 0;
        int blockStart = -1;
        for (int slot = start; slot <= end; slot++) {
            if (slots[slot] == null) {
                if (currentBlock == 0) blockStart = slot;
                currentBlock++;
            } else {
                if (currentBlock >= task.getDurationInSlots()) {
                    for (int i = 0; i < task.getDurationInSlots(); i++) {
                        slots[blockStart + i] = task;
                    }
                    return true;
                }
                currentBlock = 0;
                blockStart = -1;
            }
        }
        if (currentBlock >= task.getDurationInSlots()) {
            for (int i = 0; i < task.getDurationInSlots(); i++) {
                slots[blockStart + i] = task;
            }
            return true;
        }
        return false;
    }
}
