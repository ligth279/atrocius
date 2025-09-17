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
        int[] dayLoads = new int[7];
        for (Task task : tasks) {
            boolean scheduled = false;
            // Try to place on day with least load
            int bestDay = -1;
            int minLoad = Integer.MAX_VALUE;
            for (int day = 0; day < 7; day++) {
                int load = dayLoads[day];
                if (load < minLoad) {
                    minLoad = load;
                    bestDay = day;
                }
            }
            // Try to place on bestDay, searching all possible slots
            for (int start = 0; start <= 48 - task.getDurationInSlots(); start++) {
                boolean fits = true;
                for (int i = 0; i < task.getDurationInSlots(); i++) {
                    if (timetable.getSlots()[bestDay][start + i] != null) {
                        fits = false;
                        break;
                    }
                }
                if (fits) {
                    for (int i = 0; i < task.getDurationInSlots(); i++) {
                        timetable.getSlots()[bestDay][start + i] = task;
                    }
                    dayLoads[bestDay] += task.getDurationInSlots();
                    scheduled = true;
                    break;
                }
            }
            if (!scheduled) {
                // Try to place anywhere, searching all possible slots
                outer:
                for (int day = 0; day < 7; day++) {
                    for (int start = 0; start <= 48 - task.getDurationInSlots(); start++) {
                        boolean fits = true;
                        for (int i = 0; i < task.getDurationInSlots(); i++) {
                            if (timetable.getSlots()[day][start + i] != null) {
                                fits = false;
                                break;
                            }
                        }
                        if (fits) {
                            for (int i = 0; i < task.getDurationInSlots(); i++) {
                                timetable.getSlots()[day][start + i] = task;
                            }
                            dayLoads[day] += task.getDurationInSlots();
                            scheduled = true;
                            break outer;
                        }
                    }
                }
            }
            if (!scheduled) {
                unscheduledTasks.add(task);
            }
        }

        return new ScheduleResult(timetable, unscheduledTasks);
    }
}
