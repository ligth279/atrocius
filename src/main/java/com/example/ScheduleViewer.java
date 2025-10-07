package com.example;

import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ScheduleViewer {

    /**
     * Returns a schedule string with actual dates for each day, starting from the given startDate (Monday).
     * Date format: dd-MM-yy. Keeps code open for future custom week logic.
     */
    public String getScheduleString(Timetable timetable, LocalDate startDate) {
        StringBuilder sb = new StringBuilder();
        Activity[][] slots = timetable.getSlots();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yy");
        LocalDate today = LocalDate.now();
        int startDayIdx = -1;
        for (int day = 0; day < 7; day++) {
            LocalDate date = startDate.plusDays(day);
            if (date.isAfter(today)) {
                startDayIdx = day;
                break;
            }
        }
        if (startDayIdx == -1) return "No future days in this week.";
        for (int day = startDayIdx; day < 7; day++) {
            LocalDate date = startDate.plusDays(day);
            sb.append(date.format(fmt)).append(" (").append(getDayName(day)).append("):").append("\n");
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

    /**
     * Overload: only show slots after displayStartDateTime for today, all slots for other days
     */
    public String getScheduleString(Timetable timetable, LocalDate weekStartDate, java.time.LocalDateTime displayStartDateTime) {
        StringBuilder sb = new StringBuilder();
        Activity[][] slots = timetable.getSlots();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yy");
        for (int day = 0; day < 7; day++) {
            LocalDate date = weekStartDate.plusDays(day);
            sb.append(date.format(fmt)).append(" (").append(getDayName(day)).append("):").append("\n");
            int start = 0;
            // Only show slots after displayStartDateTime for today
            if (date.equals(displayStartDateTime.toLocalDate())) {
                int slot = displayStartDateTime.getHour() * 2 + (displayStartDateTime.getMinute() >= 30 ? 1 : 0);
                start = Math.min(slot, 47);
            }
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



    // Helper to get day name (Mon, Tue, ...)
    private String getDayName(int dayIdx) {
        String[] names = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        return names[dayIdx % 7];
    }

    public void printWeeklySchedule(Timetable timetable, LocalDate startDate) {
        Activity[][] slots = timetable.getSlots();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yy");
        for (int day = 0; day < 7; day++) {
            LocalDate date = startDate.plusDays(day);
            System.out.println(date.format(fmt) + " (" + getDayName(day) + "):");
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
