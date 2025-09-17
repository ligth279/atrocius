package com.example;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 1. Get work info
        System.out.println("Enter workdays as comma-separated numbers (0=Mon, 6=Sun), e.g., 0,1,2,3,4 for Mon-Fri:");
        String workdaysInput = scanner.next();
        String[] workdayTokens = workdaysInput.split(",");
        List<Integer> workdays = new ArrayList<>();
        for (String token : workdayTokens) {
            try {
                workdays.add(Integer.parseInt(token.trim()));
            } catch (NumberFormatException e) {
                // skip invalid
            }
        }
        System.out.println("Enter work start hour (0-23): ");
        int workHour = scanner.nextInt();
        System.out.println("Enter work start minute (0 or 30): ");
        int workMinute = scanner.nextInt();
        System.out.println("Enter work duration in hours: ");
        int workDurationHours = scanner.nextInt();

        int workStartSlot = workHour * 2 + (workMinute == 30 ? 1 : 0);
        int workDurationSlots = workDurationHours * 2;

        // 2. Get sleep info
        System.out.println("Enter sleep duration in hours (e.g., 6 or 8): ");
        int sleepDurationHours = scanner.nextInt();
        int sleepDurationSlots = sleepDurationHours * 2;

        // 3. Get tasks
        List<Task> tasks = new ArrayList<>();
        System.out.println("Enter number of tasks: ");
        int numTasks = scanner.nextInt();
        scanner.nextLine(); // consume newline
        for (int i = 0; i < numTasks; i++) {
            System.out.println("Task " + (i + 1) + " name: ");
            String name = scanner.nextLine();
            System.out.println("Task duration in hours (e.g., 0.5, 1, 1.5): ");
            double dur = scanner.nextDouble();
            scanner.nextLine(); // consume newline
            int slots = (int) Math.round(dur * 2);
            tasks.add(new Task(name, slots));
        }

    SchedulerService scheduler = new SchedulerService();
    ScheduleResult scheduleResult = scheduler.generateTimetable(
        workdays, workStartSlot, workDurationSlots,
        sleepDurationSlots, tasks);

        ScheduleViewer viewer = new ScheduleViewer();
        viewer.printWeeklySchedule(scheduleResult.timetable());
        if (!scheduleResult.unscheduledTasks().isEmpty()) {
            viewer.printUnscheduledTasks(scheduleResult.unscheduledTasks());
        }
    }
}
