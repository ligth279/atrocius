package com.example;

import com.example.ui.ActivityCard;
import com.example.ui.SchedulerViewModel;
import com.example.ui.TimetableDayView;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.LinkedHashSet;
import java.time.temporal.ChronoUnit;

/**
 * Primary JavaFX entry point presenting the playful, themeable scheduler UI.
 */
public class SchedulerFX extends Application {

    private final SchedulerViewModel viewModel = new SchedulerViewModel();

    private BorderPane root;

    // Generator controls
    private Spinner<Integer> sleepDurationSpinner;
    private CheckBox[] workdayBoxes;
    private Spinner<Integer> workHourSpinner;
    private ComboBox<String> workMinuteBox;
    private Spinner<Integer> workDurationSpinner;
    private DatePicker endDatePicker;
    private Label generatorStatusLabel;

    // Task inputs
    private TextField taskNameField;
    private Spinner<Integer> taskHourSpinner;
    private ComboBox<String> taskMinuteBox;
    private CheckBox[] taskDayBoxes;
    private ToggleGroup taskTimeGroup;
    private Label taskStatusLabel;
    private ListView<Task> taskListView;

    // Event inputs
    private TextField eventNameField;
    private DatePicker eventDatePicker;
    private Spinner<Integer> eventHourSpinner;
    private ComboBox<String> eventMinuteBox;
    private Spinner<Integer> eventDurationSpinner;
    private Label eventStatusLabel;
    private ListView<Event> eventListView;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Smart Weekly Scheduler");
        Image icon = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/icons/calendar-blue.png")));
        stage.getIcons().add(icon);

        root = new BorderPane();
        root.getStyleClass().add("app-root");
        bindDarkModeClass(root);

        Scene scene = new Scene(root, 1120, 760);
        scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/styles/theme.css")).toExternalForm());
        stage.setScene(scene);
        stage.show();

        showLandingView();
    }

    private void showLandingView() {
        Node landing = buildLandingPane();
        root.setCenter(landing);
        animateIn(landing);
    }


    private void showCalendarView() {
        Node calendar = buildCalendarPane();
        root.setCenter(calendar);
        animateIn(calendar);
    }

    private void showGeneratorView() {
        Node generator = buildGeneratorPane();
        root.setCenter(generator);
        animateIn(generator);
    }

    private void showDayView(String date) {
        Node day = buildDayPane(date);
        root.setCenter(day);
        animateIn(day);
    }

    // ------------------------------------------------------------------
    // View builders
    // ------------------------------------------------------------------

    private Node buildLandingPane() {
        BorderPane container = new BorderPane();
        container.setPadding(new Insets(64));
        bindDarkModeClass(container);

        Button themeToggle = createThemeToggle();
        BorderPane.setAlignment(themeToggle, Pos.TOP_RIGHT);
        container.setTop(themeToggle);

        VBox hero = new VBox(24);
        hero.setAlignment(Pos.CENTER);
        hero.setSpacing(18);
        hero.setFillWidth(false);
        styleSurface(hero);

        Label title = new Label("🗓️ Smart Scheduler");
        styleHeading(title, "heading-title");

        Label subtitle = new Label("Plan your week, your way.");
        subtitle.getStyleClass().add("heading-subtitle");
        bindDarkModeClass(subtitle);

        Label tagline = new Label("Bright, playful, and productive scheduling for every week.");
        tagline.getStyleClass().add("body-label");
        bindDarkModeClass(tagline);

        Button calendarBtn = createPrimaryButton("See Timetable");
        calendarBtn.setOnAction(e -> showCalendarView());

        Button generatorBtn = createPrimaryButton("Generate New Timetable");
        generatorBtn.setOnAction(e -> showGeneratorView());

        hero.getChildren().addAll(title, subtitle, tagline, calendarBtn, generatorBtn);
        container.setCenter(hero);
        return container;
    }

    private Node buildCalendarPane() {
        viewModel.refreshTimetableDates();

        BorderPane container = new BorderPane();
        container.setPadding(new Insets(48));
        bindDarkModeClass(container);

        Button themeToggle = createThemeToggle();
        Button backBtn = createSecondaryButton("⬅ Back");
        backBtn.setOnAction(e -> showLandingView());

        Region spacer = new Region();
        HBox topBar = new HBox(16, backBtn, spacer, themeToggle);
        topBar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bindDarkModeClass(topBar);
        BorderPane.setMargin(topBar, new Insets(0, 0, 24, 0));
        container.setTop(topBar);

        VBox content = new VBox(24);
        content.setAlignment(Pos.TOP_CENTER);
        styleSurface(content);

        Label header = new Label("📅 View Your Timetable");
        styleHeading(header, "heading-title");

        Label info = new Label("Pick a day to explore its schedule, or generate a new plan.");
        info.getStyleClass().add("body-label");
        bindDarkModeClass(info);

    Node datesView = viewModel.getTimetableDates().isEmpty()
        ? createEmptyState("No timetables yet. Generate one to begin!")
        : createTimetableCalendar();

    Node scrollableDates = wrapInScrollPane(datesView);
    VBox.setVgrow(scrollableDates, Priority.ALWAYS);

        content.getChildren().addAll(header, info, scrollableDates);
        container.setCenter(content);
        return container;
    }

    private Node buildGeneratorPane() {
        BorderPane container = new BorderPane();
        container.setPadding(new Insets(48));
        bindDarkModeClass(container);

    Button themeToggle = createThemeToggle();
    BorderPane.setAlignment(themeToggle, Pos.TOP_RIGHT);
    container.setTop(themeToggle);

        HBox layout = new HBox(32);
        layout.setAlignment(Pos.TOP_LEFT);

        VBox nav = new VBox(16);
        nav.setPrefWidth(220);
        styleSurface(nav);

        Button sleepBtn = createNavButton("😴 Sleep");
        Button workBtn = createNavButton("🏫 Work/School");
        Button tasksBtn = createNavButton("📝 Tasks");
        Button eventsBtn = createNavButton("🎉 Events");

        StackPane contentStack = new StackPane();
        contentStack.setPrefWidth(720);

        Node sleepView = buildSleepSection();
        Node workView = buildWorkSection();
        Node tasksView = buildTasksSection();
        Node eventsView = buildEventsSection();

        contentStack.getChildren().addAll(sleepView, workView, tasksView, eventsView);
        showContent(contentStack, sleepView);
        selectNavButton(sleepBtn);

        sleepBtn.setOnAction(e -> {
            showContent(contentStack, sleepView);
            selectNavButton(sleepBtn, workBtn, tasksBtn, eventsBtn);
        });
        workBtn.setOnAction(e -> {
            showContent(contentStack, workView);
            selectNavButton(workBtn, sleepBtn, tasksBtn, eventsBtn);
        });
        tasksBtn.setOnAction(e -> {
            showContent(contentStack, tasksView);
            selectNavButton(tasksBtn, sleepBtn, workBtn, eventsBtn);
        });
        eventsBtn.setOnAction(e -> {
            showContent(contentStack, eventsView);
            selectNavButton(eventsBtn, sleepBtn, workBtn, tasksBtn);
        });

        generatorStatusLabel = new Label();
        generatorStatusLabel.getStyleClass().add("status-label");
        bindDarkModeClass(generatorStatusLabel);

        endDatePicker = new DatePicker();
        bindDarkModeClass(endDatePicker);

        Label endDateLabel = new Label("End Date");
        endDateLabel.getStyleClass().add("body-label");
        bindDarkModeClass(endDateLabel);

        Button generateBtn = createPrimaryButton("🚀 Generate Timetable");
        generateBtn.setOnAction(e -> handleGenerateTimetable());

    Button landingBackBtn = createSecondaryButton("⬅ Back");
    landingBackBtn.setOnAction(e -> showLandingView());

    VBox bottomSection = new VBox(12, endDateLabel, endDatePicker, generateBtn, generatorStatusLabel, landingBackBtn);
        bottomSection.setAlignment(Pos.BOTTOM_LEFT);
        bottomSection.getChildren().forEach(this::bindDarkModeClass);

    Region navSpacer = new Region();
    VBox.setVgrow(navSpacer, Priority.ALWAYS);
    nav.getChildren().addAll(sleepBtn, workBtn, tasksBtn, eventsBtn, navSpacer, bottomSection);

        layout.getChildren().addAll(nav, contentStack);
        container.setCenter(layout);
        return container;
    }

    private Node buildDayPane(String date) {
        viewModel.selectedDateProperty().set(date);

        BorderPane container = new BorderPane();
        container.setPadding(new Insets(48));
        bindDarkModeClass(container);

    Button themeToggle = createThemeToggle();
    Button backToCalendarBtn = createSecondaryButton("⬅ Back to Calendar");
    backToCalendarBtn.setOnAction(e -> showCalendarView());

    Region dayTopSpacer = new Region();
    HBox topBar = new HBox(16, backToCalendarBtn, dayTopSpacer, themeToggle);
    topBar.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(dayTopSpacer, Priority.ALWAYS);
    bindDarkModeClass(topBar);
    BorderPane.setMargin(topBar, new Insets(0, 0, 24, 0));
    container.setTop(topBar);

        VBox content = new VBox(24);
        content.setAlignment(Pos.TOP_CENTER);
        styleSurface(content);

        LocalDate localDate = LocalDate.parse(date);
        String headingText = localDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault())
                + ", "
                + localDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())
                + " " + localDate.getDayOfMonth() + ", " + localDate.getYear();

        Label header = new Label(headingText);
        styleHeading(header, "heading-title");

    TimetableDayView timetableDayView = new TimetableDayView(viewModel.getTimetableEntries());
    timetableDayView.bindDarkMode(viewModel.darkModeProperty());
    Node dayScroll = wrapInScrollPane(timetableDayView);
    VBox.setVgrow(dayScroll, Priority.ALWAYS);

        content.getChildren().addAll(header, dayScroll);
        container.setCenter(content);
        return container;
    }

    // ------------------------------------------------------------------
    // Section builders
    // ------------------------------------------------------------------

    private Node buildSleepSection() {
        VBox box = new VBox(18);
        styleSurface(box);

        Label header = new Label("😴 Sleep Settings");
        styleHeading(header, "heading-title");

        Label prompt = new Label("How many hours of sleep do you want each night?");
        prompt.getStyleClass().add("body-label");
        bindDarkModeClass(prompt);

        sleepDurationSpinner = new Spinner<>(4, 12, 8);
        sleepDurationSpinner.setEditable(true);
        bindDarkModeClass(sleepDurationSpinner);

        box.getChildren().addAll(header, prompt, sleepDurationSpinner);
        return box;
    }

    private Node buildWorkSection() {
        VBox box = new VBox(18);
        styleSurface(box);

        Label header = new Label("🏫 Work & School");
        styleHeading(header, "heading-title");

        Label daysLabel = new Label("Select your work or school days:");
        daysLabel.getStyleClass().add("body-label");
        bindDarkModeClass(daysLabel);

        workdayBoxes = createDayCheckboxArray();
        FlowPane daysPane = new FlowPane(12, 12);
        daysPane.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < workdayBoxes.length; i++) {
            if (i < 5) {
                workdayBoxes[i].setSelected(true);
            }
            bindDarkModeClass(workdayBoxes[i]);
            daysPane.getChildren().add(workdayBoxes[i]);
        }
        bindDarkModeClass(daysPane);

        workHourSpinner = new Spinner<>(0, 23, 9);
        workHourSpinner.setEditable(true);
        bindDarkModeClass(workHourSpinner);

        workMinuteBox = new ComboBox<>();
        workMinuteBox.getItems().addAll("00", "30");
        workMinuteBox.setValue("00");
        bindDarkModeClass(workMinuteBox);

        HBox startTimeBox = new HBox(8, workHourSpinner, new Label(":"), workMinuteBox);
        startTimeBox.setAlignment(Pos.CENTER_LEFT);
        bindDarkModeClass(startTimeBox);

        workDurationSpinner = new Spinner<>(1, 12, 8);
        workDurationSpinner.setEditable(true);
        bindDarkModeClass(workDurationSpinner);

        box.getChildren().addAll(
                header,
                daysLabel,
                daysPane,
                new Label("Start time:"),
                startTimeBox,
                new Label("Duration (hours):"),
                workDurationSpinner
        );

        box.getChildren().stream()
                .filter(node -> node instanceof Label)
                .forEach(this::bindDarkModeClass);
        return box;
    }

    private Node buildTasksSection() {
        VBox box = new VBox(18);
        styleSurface(box);

        Label header = new Label("📝 Tasks");
        styleHeading(header, "heading-title");

        taskNameField = new TextField();
        taskNameField.setPromptText("Task name");
        bindDarkModeClass(taskNameField);

        taskHourSpinner = new Spinner<>(0, 12, 0);
        taskHourSpinner.setEditable(true);
        bindDarkModeClass(taskHourSpinner);

        taskMinuteBox = new ComboBox<>();
        taskMinuteBox.getItems().addAll("00", "30");
        taskMinuteBox.setValue("30");
        bindDarkModeClass(taskMinuteBox);

        HBox durationBox = new HBox(8,
                new Label("Hour:"), taskHourSpinner,
                new Label("Minute:"), taskMinuteBox);
        durationBox.setAlignment(Pos.CENTER_LEFT);
        bindDarkModeClass(durationBox);

        taskDayBoxes = createDayCheckboxArray();
        FlowPane daysPane = new FlowPane(12, 12);
        daysPane.setAlignment(Pos.CENTER_LEFT);
        for (CheckBox cb : taskDayBoxes) {
            bindDarkModeClass(cb);
            daysPane.getChildren().add(cb);
        }
        bindDarkModeClass(daysPane);

        taskTimeGroup = new ToggleGroup();
        RadioButton any = createTimeToggle("Any", "any");
        RadioButton morning = createTimeToggle("Morning", "morning");
        RadioButton evening = createTimeToggle("Evening", "evening");
        any.setSelected(true);

        HBox preferredTimeBox = new HBox(12, any, morning, evening);
        preferredTimeBox.setAlignment(Pos.CENTER_LEFT);
        bindDarkModeClass(preferredTimeBox);

    Button addTaskBtn = createPrimaryButton("Add Task");
    addTaskBtn.setOnAction(e -> handleAddTask());

        taskStatusLabel = new Label();
        taskStatusLabel.getStyleClass().add("status-label");
        bindDarkModeClass(taskStatusLabel);

        taskListView = createTaskListView();
        taskListView.setPrefHeight(220);

    box.getChildren().addAll(
        header,
        new Label("Task name"),
        taskNameField,
        new Label("Duration"),
        durationBox,
        new Label("Preferred days (optional)"),
        daysPane,
        new Label("Preferred time"),
        preferredTimeBox,
        addTaskBtn,
        taskStatusLabel,
        taskListView
    );

        box.getChildren().stream()
                .filter(node -> node instanceof Label)
                .forEach(this::bindDarkModeClass);
        return box;
    }

    private Node buildEventsSection() {
        VBox box = new VBox(18);
        styleSurface(box);

        Label header = new Label("🎉 Events");
        styleHeading(header, "heading-title");

        eventNameField = new TextField();
        eventNameField.setPromptText("Event name");
        bindDarkModeClass(eventNameField);

        eventDatePicker = new DatePicker();
        bindDarkModeClass(eventDatePicker);

        eventHourSpinner = new Spinner<>(0, 23, 18);
        eventHourSpinner.setEditable(true);
        bindDarkModeClass(eventHourSpinner);

        eventMinuteBox = new ComboBox<>();
        eventMinuteBox.getItems().addAll("00", "30");
        eventMinuteBox.setValue("00");
        bindDarkModeClass(eventMinuteBox);

        HBox startTimeBox = new HBox(8, eventHourSpinner, new Label(":"), eventMinuteBox);
        startTimeBox.setAlignment(Pos.CENTER_LEFT);
        bindDarkModeClass(startTimeBox);

        eventDurationSpinner = new Spinner<>(1, 12, 1);
        eventDurationSpinner.setEditable(true);
        bindDarkModeClass(eventDurationSpinner);

        Button addEventBtn = createPrimaryButton("Add Event");
        addEventBtn.setOnAction(e -> handleAddEvent());

        eventStatusLabel = new Label();
        eventStatusLabel.getStyleClass().add("status-label");
        bindDarkModeClass(eventStatusLabel);

        eventListView = createEventListView();
        eventListView.setPrefHeight(220);

        box.getChildren().addAll(
                header,
                new Label("Event name"),
                eventNameField,
                new Label("Event date"),
                eventDatePicker,
                new Label("Start time"),
                startTimeBox,
                new Label("Duration (hours)"),
                eventDurationSpinner,
                addEventBtn,
                eventStatusLabel,
                eventListView
        );

        box.getChildren().stream()
                .filter(node -> node instanceof Label)
                .forEach(this::bindDarkModeClass);
        return box;
    }

    // ------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------

    private void handleAddTask() {
        String name = taskNameField.getText() == null ? "" : taskNameField.getText().trim();
        if (name.isEmpty()) {
            taskStatusLabel.setText("Please enter a task name.");
            return;
        }

        int durationHours = taskHourSpinner.getValue();
        int durationMinutes = Integer.parseInt(taskMinuteBox.getValue());
        int durationSlots = durationHours * 2 + (durationMinutes == 30 ? 1 : 0);
        if (durationSlots == 0) {
            durationSlots = 1; // enforce minimum 30 minutes
        }

        List<Integer> selectedDays = new ArrayList<>();
        for (int i = 0; i < taskDayBoxes.length; i++) {
            if (taskDayBoxes[i].isSelected()) {
                selectedDays.add(i);
            }
        }

        Toggle selectedToggle = taskTimeGroup.getSelectedToggle();
        String preferredTime = selectedToggle != null
                ? Objects.toString(selectedToggle.getUserData(), "any")
                : "any";

        if (selectedDays.isEmpty()) {
            viewModel.getTasks().add(new Task(name, durationSlots, null, preferredTime));
        } else {
            for (Integer day : selectedDays) {
                viewModel.getTasks().add(new Task(name, durationSlots, day, preferredTime));
            }
        }

        taskStatusLabel.setText("✅ Task added");
        taskNameField.clear();
        taskHourSpinner.getValueFactory().setValue(0);
        taskMinuteBox.setValue("30");
        for (CheckBox cb : taskDayBoxes) {
            cb.setSelected(false);
        }
        taskTimeGroup.getToggles().forEach(toggle -> {
            if ("any".equals(toggle.getUserData())) {
                toggle.setSelected(true);
            }
        });
    }

    private void handleAddEvent() {
        String name = eventNameField.getText() == null ? "" : eventNameField.getText().trim();
        LocalDate date = eventDatePicker.getValue();
        if (name.isEmpty() || date == null) {
            eventStatusLabel.setText("Please provide an event name and date.");
            return;
        }

        int hour = eventHourSpinner.getValue();
        String minute = eventMinuteBox.getValue();
        int slot = hour * 2 + ("30".equals(minute) ? 1 : 0);
        int durationSlots = eventDurationSpinner.getValue() * 2;

        viewModel.getEvents().add(new Event(name, durationSlots, date, slot));
        eventStatusLabel.setText("🎉 Event added");

        eventNameField.clear();
        eventDatePicker.setValue(null);
        eventHourSpinner.getValueFactory().setValue(18);
        eventMinuteBox.setValue("00");
        eventDurationSpinner.getValueFactory().setValue(1);
    }

    private void handleGenerateTimetable() {
        generatorStatusLabel.setText("Generating timetable...");

        LocalDate today = LocalDate.now();
        LocalDate end = endDatePicker.getValue();
        if (end == null || end.isBefore(today)) {
            generatorStatusLabel.setText("Please choose an end date after today.");
            return;
        }

        List<Integer> workdays = new ArrayList<>();
        for (int i = 0; i < workdayBoxes.length; i++) {
            if (workdayBoxes[i].isSelected()) {
                workdays.add(i);
            }
        }

        int workStartSlot = workHourSpinner.getValue() * 2
                + ("30".equals(workMinuteBox.getValue()) ? 1 : 0);
        int workDurationSlots = workDurationSpinner.getValue() * 2;
        int sleepDurationSlots = sleepDurationSpinner.getValue() * 2;

        new Thread(() -> {
            boolean success = viewModel.generateAndPersistSchedule(
                    workdays,
                    workStartSlot,
                    workDurationSlots,
                    sleepDurationSlots,
                    today,
                    end
            );

            Platform.runLater(() -> {
                if (success) {
                    generatorStatusLabel.setText("✅ Timetable created!");
                    viewModel.resetComposerState();
                    taskStatusLabel.setText("");
                    eventStatusLabel.setText("");
                    showCalendarView();
                } else {
                    generatorStatusLabel.setText("❌ Could not generate timetable. Check your inputs.");
                }
            });
        }, "scheduler-generator").start();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("primary-button");
        bindDarkModeClass(button);
        return button;
    }

    private Button createSecondaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("secondary-button");
        bindDarkModeClass(button);
        return button;
    }

    private Button createNavButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        bindDarkModeClass(button);
        return button;
    }

    private Button createThemeToggle() {
        Button toggle = new Button();
        toggle.getStyleClass().add("theme-toggle");
        toggle.textProperty().bind(Bindings.when(viewModel.darkModeProperty())
                .then("☀️")
                .otherwise("🌙"));
        toggle.setOnAction(e -> viewModel.toggleDarkMode());
        bindDarkModeClass(toggle);
        return toggle;
    }

    private void selectNavButton(Button selected, Button... others) {
        for (Button other : others) {
            other.getStyleClass().remove("nav-button--selected");
        }
        if (!selected.getStyleClass().contains("nav-button--selected")) {
            selected.getStyleClass().add("nav-button--selected");
        }
    }

    private void showContent(StackPane container, Node content) {
        if (!container.getChildren().contains(content)) {
            container.getChildren().add(content);
        }
        content.toFront();
    }

    private Node createTimetableCalendar() {
        ObservableList<String> rawDates = viewModel.getTimetableDates();
        if (rawDates.isEmpty()) {
            return createEmptyState("No timetables yet.");
        }

        List<LocalDate> sortedDates = rawDates.stream()
                .map(LocalDate::parse)
                .distinct()
                .sorted()
                .toList();

        LocalDate firstDate = sortedDates.get(0);
        LocalDate lastDate = sortedDates.get(sortedDates.size() - 1);
        LocalDate calendarStart = firstDate.with(DayOfWeek.MONDAY);
        LocalDate calendarEnd = lastDate.with(DayOfWeek.SUNDAY);
        Set<LocalDate> activeDates = new LinkedHashSet<>(sortedDates);

        GridPane grid = new GridPane();
        grid.getStyleClass().add("timetable-grid");
        bindDarkModeClass(grid);
        grid.setHgap(16);
        grid.setVgap(16);
        grid.setMaxWidth(Double.MAX_VALUE);

        for (int i = 0; i < 7; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100d / 7d);
            grid.getColumnConstraints().add(column);

            DayOfWeek dayOfWeek = DayOfWeek.MONDAY.plus(i);
            Label header = new Label(dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()));
            header.getStyleClass().add("calendar-header");
            bindDarkModeClass(header);
            GridPane.setConstraints(header, i, 0);
            GridPane.setFillWidth(header, true);
            grid.getChildren().add(header);
        }

        int totalDays = (int) ChronoUnit.DAYS.between(calendarStart, calendarEnd) + 1;
        for (int offset = 0; offset < totalDays; offset++) {
            LocalDate date = calendarStart.plusDays(offset);
            int column = offset % 7;
            int row = offset / 7 + 1;

            StackPane cell = new StackPane();
            cell.getStyleClass().add("calendar-cell");
            bindDarkModeClass(cell);

            boolean isActive = activeDates.contains(date);
            if (isActive) {
                cell.getStyleClass().add("calendar-cell--active");
            } else if (date.isBefore(firstDate) || date.isAfter(lastDate)) {
                cell.getStyleClass().add("calendar-cell--outside");
            } else {
                cell.getStyleClass().add("calendar-cell--idle");
            }

            VBox content = new VBox(6);
            content.getStyleClass().add("calendar-cell__content");
            content.setAlignment(Pos.TOP_LEFT);
            bindDarkModeClass(content);

            if (date.getDayOfMonth() == 1) {
                Label month = new Label(date.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()).toUpperCase());
                month.getStyleClass().add("calendar-day-month");
                bindDarkModeClass(month);
                content.getChildren().add(month);
            }

            Label dayNumber = new Label(String.valueOf(date.getDayOfMonth()));
            dayNumber.getStyleClass().add("calendar-day-num");
            bindDarkModeClass(dayNumber);
            content.getChildren().add(dayNumber);

            Label hint = new Label(isActive ? "View timetable" : "No entries");
            hint.getStyleClass().add("calendar-day-hint");
            bindDarkModeClass(hint);
            content.getChildren().add(hint);

            cell.getChildren().add(content);
            GridPane.setConstraints(cell, column, row);
            GridPane.setFillWidth(cell, true);
            GridPane.setVgrow(cell, Priority.ALWAYS);

            if (isActive) {
                cell.setOnMouseClicked(e -> showDayView(date.toString()));
                cell.setCursor(Cursor.HAND);
            }

            grid.getChildren().add(cell);
        }

        return grid;
    }

    private Node wrapInScrollPane(Node node) {
        ScrollPane scrollPane = new ScrollPane(node);
        scrollPane.getStyleClass().add("frost-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setMaxWidth(Double.MAX_VALUE);
        bindDarkModeClass(scrollPane);
        if (node instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }
        return scrollPane;
    }

    private Node createEmptyState(String message) {
        Label label = new Label(message);
        label.getStyleClass().add("body-label");
        bindDarkModeClass(label);
        return label;
    }

    private CheckBox[] createDayCheckboxArray() {
        String[] labels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        CheckBox[] boxes = new CheckBox[labels.length];
        for (int i = 0; i < labels.length; i++) {
            CheckBox cb = new CheckBox(labels[i]);
            cb.getStyleClass().add("chip");
            boxes[i] = cb;
        }
        return boxes;
    }

    private RadioButton createTimeToggle(String label, String value) {
        RadioButton button = new RadioButton(label);
        button.setUserData(value);
        button.setToggleGroup(taskTimeGroup);
        bindDarkModeClass(button);
        return button;
    }

    private ListView<Task> createTaskListView() {
        ListView<Task> listView = new ListView<>(viewModel.getTasks());
        listView.setPlaceholder(createEmptyState("No tasks yet."));
        bindDarkModeClass(listView);
        listView.setCellFactory(lv -> new ListCell<>() {
            private final Label title = new Label();
            private final Button removeButton = createSecondaryButton("Remove");
            private final HBox container = new HBox(12, title, removeButton);

            {
                container.setAlignment(Pos.CENTER_LEFT);
                bindDarkModeClass(container);
                bindDarkModeClass(title);
            }

            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    int slots = Math.max(1, item.getDurationInSlots());
                    int totalMinutes = slots * 30;
                    int baseMinutes = 30;
                    int endTotalMinutes = baseMinutes + totalMinutes;
                    String startLabel = "00:30";
                    String endLabel = String.format("%02d:%02d", endTotalMinutes / 60, endTotalMinutes % 60);
                    title.setText(item.getName() + "  (" + startLabel + " - " + endLabel + ")");
                    removeButton.setOnAction(e -> viewModel.getTasks().remove(item));
                    setGraphic(container);
                }
            }
        });
        return listView;
    }

    private ListView<Event> createEventListView() {
        ListView<Event> listView = new ListView<>(viewModel.getEvents());
        listView.setPlaceholder(createEmptyState("No events yet."));
        bindDarkModeClass(listView);
        listView.setCellFactory(lv -> new ListCell<>() {
            private final ActivityCard card = new ActivityCard();
            private final Button removeButton = createSecondaryButton("Remove");
            private final HBox container = new HBox(12, card, removeButton);

            {
                container.setAlignment(Pos.CENTER_LEFT);
                bindDarkModeClass(container);
                card.bindDarkMode(viewModel.darkModeProperty());
            }

            @Override
            protected void updateItem(Event item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    card.setActivity(item);
                    removeButton.setOnAction(e -> viewModel.getEvents().remove(item));
                    setGraphic(container);
                }
            }
        });
        return listView;
    }

    private void bindDarkModeClass(Node node) {
        BooleanProperty dark = viewModel.darkModeProperty();
        if (dark.get() && !node.getStyleClass().contains("dark")) {
            node.getStyleClass().add("dark");
        }
        dark.addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                if (!node.getStyleClass().contains("dark")) {
                    node.getStyleClass().add("dark");
                }
            } else {
                node.getStyleClass().remove("dark");
            }
        });
    }

    private void styleSurface(Region region) {
        if (!region.getStyleClass().contains("surface-card")) {
            region.getStyleClass().add("surface-card");
        }
        bindDarkModeClass(region);
    }

    private void styleHeading(Label label, String cssClass) {
        if (!label.getStyleClass().contains(cssClass)) {
            label.getStyleClass().add(cssClass);
        }
        bindDarkModeClass(label);
    }

    private void animateIn(Node node) {
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(280), node);
        ft.setToValue(1);
        ft.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
