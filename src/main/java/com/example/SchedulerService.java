package com.example;

import java.util.*;

public class SchedulerService {
    public ScheduleResult generateTimetable(
            List<Integer> workdays, int workStartSlot, int workDurationSlots,
            int sleepDurationSlots,
            List<Task> tasks) {
        Timetable timetable = new Timetable();
        List<Task> unscheduledTasks = new ArrayList<>();

        // 1. Place work at user-specified time on all workdays
        for (int workDay : workdays) {
            FixedActivity work = new FixedActivity("Work", workDurationSlots);
            timetable.placeAt(work, workDay, workStartSlot);
        }

        // 2. Place sleep: for each day, half at end of previous day, half at start of current day
        for (int day = 0; day < 7; day++) {
            int half = sleepDurationSlots / 2;
            int otherHalf = sleepDurationSlots - half;
            int prevDay = (day + 6) % 7; // previous day, wrap around
            // Place second half at start of current day if not already filled
            if (otherHalf > 0) {
                boolean canPlace = true;
                for (int i = 0; i < otherHalf; i++) {
                    if (timetable.getSlots()[day][i] != null) {
                        canPlace = false;
                        break;
                    }
                }
                if (canPlace) {
                    FixedActivity sleepStart = new FixedActivity("Sleep", otherHalf);
                    timetable.placeAt(sleepStart, day, 0);
                }
            }
            // Place first half at end of previous day if not already filled
            if (half > 0) {
                boolean canPlace = true;
                for (int i = 0; i < half; i++) {
                    if (timetable.getSlots()[prevDay][48 - half + i] != null) {
                        canPlace = false;
                        break;
                    }
                }
                if (canPlace) {
                    FixedActivity sleepEnd = new FixedActivity("Sleep", half);
                    timetable.placeAt(sleepEnd, prevDay, 48 - half);
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
