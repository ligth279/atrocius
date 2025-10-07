package com.example;

import java.util.*;

public class SchedulerService {
    public ScheduleResult generateTimetable(
            List<Integer> workdays, int workStartSlot, int workDurationSlots,
            int sleepDurationSlots,
            List<Task> tasks,
            List<Event> events) {
        Timetable timetable = new Timetable();
        List<Task> unscheduledTasks = new ArrayList<>();

        // 0. Place events first, with forced placement
        for (Event event : events) {
            int day = event.getTargetDay();
            int startSlot = event.getStartSlot();
            timetable.placeAt(event, day, startSlot, true); // force overwrite
        }

        // 1. Place work at user-specified time on all workdays
        for (int workDay : workdays) {
            FixedActivity work = new FixedActivity("Work", workDurationSlots);
            timetable.placeAt(work, workDay, workStartSlot);
        }

        // 2. Place sleep: fill available night slots (20:00-08:00) with 'Sleep' until quota is met, even if split
        for (int day = 0; day < 7; day++) {
            int sleepPlaced = 0;
            Activity[] slots = timetable.getSlots()[day];
            // Night period: 20:00 (slot 40) to 48, then 0 to 16 (08:00)
            int[] nightSlots = new int[24];
            int idx = 0;
            for (int s = 40; s < 48; s++) nightSlots[idx++] = s;
            for (int s = 0; s < 16; s++) nightSlots[idx++] = s;
            // Count already filled sleep slots in night
            for (int s : nightSlots) {
                if (slots[s] != null && "Sleep".equals(slots[s].getName())) {
                    sleepPlaced++;
                }
            }
            // Fill remaining sleep slots in available night slots
            for (int s : nightSlots) {
                if (sleepPlaced >= sleepDurationSlots) break;
                if (slots[s] == null) {
                    slots[s] = new FixedActivity("Sleep", 1);
                    sleepPlaced++;
                }
            }
        }

        // 3. Distribute tasks as evenly as possible, filling any available block
        for (Task task : tasks) {
            boolean scheduled = false;
            int bestDay = -1;
            int bestStart = -1;
            int maxBlock = -1;
            // If task has a targetDay, only search that day
            List<Integer> daysToSearch = new ArrayList<>();
            if (task.getTargetDay() != null) {
                daysToSearch.add(task.getTargetDay());
            } else {
                for (int d = 0; d < 7; d++) daysToSearch.add(d);
            }
            for (int day : daysToSearch) {
                int currentBlock = 0;
                int blockStart = -1;
                for (int slot = 0; slot < 48; slot++) {
                    if (timetable.getSlots()[day][slot] == null) {
                        if (currentBlock == 0) blockStart = slot;
                        currentBlock++;
                    } else {
                        if (currentBlock >= task.getDurationInSlots() && currentBlock > maxBlock) {
                            bestDay = day;
                            bestStart = blockStart;
                            maxBlock = currentBlock;
                        }
                        currentBlock = 0;
                        blockStart = -1;
                    }
                }
                // Check at end of day
                if (currentBlock >= task.getDurationInSlots() && currentBlock > maxBlock) {
                    bestDay = day;
                    bestStart = blockStart;
                    maxBlock = currentBlock;
                }
            }
            if (bestDay != -1 && bestStart != -1) {
                for (int i = 0; i < task.getDurationInSlots(); i++) {
                    timetable.getSlots()[bestDay][bestStart + i] = task;
                }
                scheduled = true;
            }
            if (!scheduled) {
                unscheduledTasks.add(task);
            }
        }

        return new ScheduleResult(timetable, unscheduledTasks);
    }
}
