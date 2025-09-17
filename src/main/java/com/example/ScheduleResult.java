package com.example;

// Holds the result of the scheduling operation: the timetable and any unscheduled tasks.
import java.util.List;

public record ScheduleResult(Timetable timetable, List<Task> unscheduledTasks) {}
