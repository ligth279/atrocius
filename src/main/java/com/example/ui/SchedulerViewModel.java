package com.example.ui;

import com.example.Activity;
import com.example.ActivityRepository;
import com.example.Event;
import com.example.ScheduleResult;
import com.example.SchedulerService;
import com.example.Task;
import com.example.Timetable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Central state holder for the Scheduler UI.  Keeps the application data in
 * {@link javafx.beans.property.Property} and {@link javafx.collections.ObservableList}
 * instances so the UI can react automatically when data changes.
 */
public class SchedulerViewModel {
    private final ActivityRepository repository = new ActivityRepository();

    private final BooleanProperty darkMode = new SimpleBooleanProperty(false);
    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private final ObservableList<Event> events = FXCollections.observableArrayList();
    private final ObservableList<String> timetableDates = FXCollections.observableArrayList();
    private final ObservableList<ActivityRepository.TimetableEntry> timetableEntries = FXCollections.observableArrayList();
    private final StringProperty selectedDate = new SimpleStringProperty();

    public SchedulerViewModel() {
        refreshTimetableDates();
        selectedDate.addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                timetableEntries.setAll(repository.getTimetableForDate(newDate));
            } else {
                timetableEntries.clear();
            }
        });
    }

    public BooleanProperty darkModeProperty() {
        return darkMode;
    }

    public boolean isDarkMode() {
        return darkMode.get();
    }

    public void toggleDarkMode() {
        darkMode.set(!darkMode.get());
    }

    public ObservableList<Task> getTasks() {
        return tasks;
    }

    public ObservableList<Event> getEvents() {
        return events;
    }

    public ObservableList<String> getTimetableDates() {
        return timetableDates;
    }

    public ObservableList<ActivityRepository.TimetableEntry> getTimetableEntries() {
        return timetableEntries;
    }

    public StringProperty selectedDateProperty() {
        return selectedDate;
    }

    public void refreshTimetableDates() {
        timetableDates.setAll(repository.getAllTimetableDates());
    }

    /**
     * Clears transient UI data after a successful generation.
     */
    public void resetComposerState() {
        tasks.clear();
        events.clear();
    }

    /**
     * Generates a timetable using the {@link SchedulerService} and persists the
     * outcome via {@link ActivityRepository}. The resulting dates are reloaded so
     * the calendar view picks up new entries automatically.
     */
    public boolean generateAndPersistSchedule(List<Integer> workdays,
                                              int workStartSlot,
                                              int workDurationSlots,
                                              int sleepDurationSlots,
                                              LocalDate startDate,
                                              LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return false;
        }

        SchedulerService scheduler = new SchedulerService();
        List<Task> taskSnapshot = new ArrayList<>(tasks);
        List<Event> eventSnapshot = new ArrayList<>(events);

        ScheduleResult result = scheduler.generateTimetable(
                workdays,
                workStartSlot,
                workDurationSlots,
                sleepDurationSlots,
                taskSnapshot,
                eventSnapshot,
                startDate,
                endDate
        );

        ActivityRepository repo = new ActivityRepository();
        repo.deleteTimetableForDateRange("0000-01-01", "9999-12-31");

        List<Activity> allActivities = new ArrayList<>();
        allActivities.add(new com.example.FixedActivity("Sleep", sleepDurationSlots));
        allActivities.add(new com.example.FixedActivity("Work", workDurationSlots));
        allActivities.addAll(taskSnapshot);
        allActivities.addAll(eventSnapshot);

        for (Activity activity : allActivities) {
            repo.addActivity(activity);
        }

        Map<String, Integer> nameToId = repo.getActivityNameToIdMap();
        Timetable timetable = result.getTimetable();
        repo.saveTimetable(timetable, startDate, endDate, nameToId);

        refreshTimetableDates();
        selectedDate.set(endDate != null ? endDate.toString() : null);
        timetableEntries.setAll(repository.getTimetableForDate(selectedDate.get()));
        return true;
    }
}
