package com.example;

import java.util.*;
class RecurringTask {
    String name;
    int slots;
    List<Integer> days; // days to repeat on (0=Mon, 6=Sun)
    RecurringTask(String name, int slots, List<Integer> days) {
        this.name = name;
        this.slots = slots;
        this.days = days;
    }
}

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
        List<RecurringTask> recurringTasks = new ArrayList<>();
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
            System.out.println("Should this task repeat on specific days? (yes/no): ");
            String repeat = scanner.nextLine().trim().toLowerCase();
            List<Integer> days = new ArrayList<>();
            if (repeat.equals("yes")) {
                System.out.println("Enter days as comma-separated numbers (0=Mon, 6=Sun), e.g., 0,2,4: ");
                String daysInput = scanner.nextLine();
                String[] dayTokens = daysInput.split(",");
                for (String token : dayTokens) {
                    try {
                        days.add(Integer.parseInt(token.trim()));
                    } catch (NumberFormatException e) {}
                }
            } else {
                // If not recurring, schedule on any day
                for (int d = 0; d < 7; d++) days.add(d);
            }
            recurringTasks.add(new RecurringTask(name, slots, days));
        }


    // Flatten recurringTasks into a list of Task objects for each day
    List<Task> tasks = new ArrayList<>();
    for (RecurringTask rt : recurringTasks) {
        for (int day : rt.days) {
            tasks.add(new Task(rt.name, rt.slots, day));
        }
    }

    SchedulerService scheduler = new SchedulerService();
    ScheduleResult scheduleResult = scheduler.generateTimetable(
        workdays, workStartSlot, workDurationSlots,
        sleepDurationSlots, tasks, new ArrayList<Event>());

    ScheduleViewer viewer = new ScheduleViewer();
    // Find next Monday from today
    java.time.LocalDate today = java.time.LocalDate.now();
    java.time.DayOfWeek dow = today.getDayOfWeek();
    int daysUntilMonday = (java.time.DayOfWeek.MONDAY.getValue() - dow.getValue() + 7) % 7;
    if (daysUntilMonday == 0) daysUntilMonday = 7; // always next Monday
    java.time.LocalDate nextMonday = today.plusDays(daysUntilMonday);
    viewer.printWeeklySchedule(scheduleResult.timetable(), nextMonday);
    if (!scheduleResult.unscheduledTasks().isEmpty()) {
        viewer.printUnscheduledTasks(scheduleResult.unscheduledTasks());
    }
    }
}
