package com.example;

// Holds the result of the scheduling operation: the timetable and any unscheduled tasks.
import java.util.List;

public class ScheduleResult {
	private final Timetable timetable;
	private final List<Task> unscheduledTasks;

	public ScheduleResult(Timetable timetable, List<Task> unscheduledTasks) {
		this.timetable = timetable;
		this.unscheduledTasks = unscheduledTasks;
	}

	public Timetable getTimetable() {
		return timetable;
	}

	public List<Task> getUnscheduledTasks() {
		return unscheduledTasks;
	}
}
