package com.example;

import java.util.List;

public class ScheduleViewer {
        public String getScheduleString(Timetable timetable) {
            StringBuilder sb = new StringBuilder();
            Activity[][] slots = timetable.getSlots();
            for (int day = 0; day < 7; day++) {
                sb.append("Day ").append(day + 1).append(":\n");
                int start = 0;
                while (start < 48) {
                    String name = (slots[day][start] != null) ? slots[day][start].getName() : "Free time";
                    int end = start;
                    while (end + 1 < 48) {
                        String nextName = (slots[day][end + 1] != null) ? slots[day][end + 1].getName() : "Free time";
                        if (!nextName.equals(name)) break;
                        end++;
                    }
                    String startTime = slotToTime(start);
                    String endTime = slotToTime(end + 1); // end is inclusive, so add 1
                    sb.append("  ").append(startTime).append(" - ").append(endTime).append(": ").append(name).append("\n");
                    start = end + 1;
                }
            }
            return sb.toString();
        }
    public void printWeeklySchedule(Timetable timetable) {
        Activity[][] slots = timetable.getSlots();
        for (int day = 0; day < 7; day++) {
            System.out.println("Day " + (day + 1) + ":");
            int start = 0;
            while (start < 48) {
                String name = (slots[day][start] != null) ? slots[day][start].getName() : "Free time";
                int end = start;
                while (end + 1 < 48) {
                    String nextName = (slots[day][end + 1] != null) ? slots[day][end + 1].getName() : "Free time";
                    if (!nextName.equals(name)) break;
                    end++;
                }
                String startTime = slotToTime(start);
                String endTime = slotToTime(end + 1); // end is inclusive, so add 1
                System.out.println("  " + startTime + " - " + endTime + ": " + name);
                start = end + 1;
            }
        }
    }

    private String slotToTime(int slot) {
        int hour = (slot / 2) % 24;
        int minute = (slot % 2) * 30;
        return String.format("%02d:%02d", hour, minute);
    }

    public void printUnscheduledTasks(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) return;
        System.out.println("--- WARNING: NOT ALL TASKS COULD BE SCHEDULED ---");
        System.out.println("The following tasks could not fit in your schedule:");
        for (Task task : tasks) {
            double hours = task.getDurationInSlots() / 2.0;
            System.out.printf("- %s (Required: %.1f hours)%n", task.getName(), hours);
        }
    }
}
