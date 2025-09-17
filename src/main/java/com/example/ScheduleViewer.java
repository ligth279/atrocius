package com.example;

import java.util.List;

public class ScheduleViewer {
    public void printWeeklySchedule(Timetable timetable) {
        Activity[][] slots = timetable.getSlots();
        for (int day = 0; day < 7; day++) {
            System.out.println("Day " + (day + 1) + ":");
            for (int slot = 0; slot < 48; slot++) {
                String name = (slots[day][slot] != null) ? slots[day][slot].getName() : "Free time";
                System.out.println("  Slot " + slot + ": " + name);
            }
        }
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
