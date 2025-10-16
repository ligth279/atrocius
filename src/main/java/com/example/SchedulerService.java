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
            // Before midnight: slots 80-95 (20:00-00:00) - 15-min slots
            int sleepPlaced = 0;
            for (int s = 80; s < 96 && sleepPlaced < halfSleep; s++) {
                if (slots[s] == null) {
                    slots[s] = new FixedActivity("Sleep", 1);
                    sleepPlaced++;
                }
            }
            // After midnight: slots 0-15 (00:00-04:00) - 15-min slots
            for (int s = 0; s < 16 && sleepPlaced < sleepDurationSlots; s++) {
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
                    if (slotIdx >= 96 || slots[slotIdx] != null) {
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

        // 3. Place tasks (with intelligent fallback and conflict resolution)
        for (Task task : tasks) {
            boolean taskPlaced = false;
            
            // Try placing task on every applicable day (respecting targetDay and recurring weekly)
            for (int day = 0; day < days && !taskPlaced; day++) {
                java.time.LocalDate date = startDate.plusDays(day);
                int weekday = date.getDayOfWeek().getValue(); // 1=Mon, 7=Sun
                if (task.getTargetDay() != null && weekday - 1 != task.getTargetDay()) continue;

                // Preferred time window slots (15-minute intervals, 96 slots per day)
                int morningStart = 0, morningEnd = 47;   // 00:00 to 12:00 (slot 0-47)
                int eveningStart = 64, eveningEnd = 95;  // 16:00 to 24:00 (slot 64-95)

                // Try preferred window first
                String pref = task.getPreferredTime();
                if ("morning".equals(pref)) {
                    taskPlaced = tryPlaceTaskInWindow(timetable.getSlots()[day], task, morningStart, morningEnd);
                } else if ("evening".equals(pref)) {
                    taskPlaced = tryPlaceTaskInWindow(timetable.getSlots()[day], task, eveningStart, eveningEnd);
                }

                // If not placed, try anywhere on the same day
                if (!taskPlaced) {
                    taskPlaced = tryPlaceTaskInWindow(timetable.getSlots()[day], task, 0, 95);
                }
            }

            // If task was not placed after scanning applicable days, apply fallback logic
            if (!taskPlaced) {
                // Strategy: look for next 3 days, then nearest non-work day
                taskPlaced = tryPlaceTaskWithFallback(timetable, task, days, startDate, workdays);
            }

            // Track unscheduled tasks
            if (!taskPlaced) {
                unscheduledTasks.add(task);
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

    /**
     * Fallback strategy for unplaced tasks:
     * 1. Search next 3 days for any free slot
     * 2. If still not placed, find nearest non-work day with no events
     */
    private boolean tryPlaceTaskWithFallback(Timetable timetable, Task task, int totalDays, 
                                              java.time.LocalDate startDate, List<Integer> workdays) {
        Activity[][] allSlots = timetable.getSlots();

        // Step 1: Try next 3 days (from day 0 to min(totalDays, 3))
        int lookAheadDays = Math.min(totalDays, 3);
        for (int day = 0; day < lookAheadDays; day++) {
            if (tryPlaceTaskInWindow(allSlots[day], task, 0, 95)) {
                return true;
            }
        }

        // Step 2: Find nearest non-work day with no events
        for (int day = 0; day < totalDays; day++) {
            java.time.LocalDate date = startDate.plusDays(day);
            int weekday = date.getDayOfWeek().getValue(); // 1=Mon, 7=Sun
            
            // Check if this is a non-work day
            if (!workdays.contains(weekday - 1)) {
                // Check if this day has no events (scan for Event instances)
                Activity[] daySlots = allSlots[day];
                boolean hasEvent = false;
                for (Activity act : daySlots) {
                    if (act instanceof Event) {
                        hasEvent = true;
                        break;
                    }
                }
                
                // If no events on this non-work day, try to place task
                if (!hasEvent) {
                    if (tryPlaceTaskInWindow(daySlots, task, 0, 95)) {
                        return true;
                    }
                }
            }
        }

        return false; // Could not place task anywhere
    }
}
