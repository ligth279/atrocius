// ...existing code...
package com.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SchedulerFX extends Application {
    // Helper to update the task list UI in the generator panel
    private void updateTaskListBox(VBox taskListBox) {
        taskListBox.getChildren().clear();
        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i);
            String daysStr = (task.getTargetDay() != null) ? " on day " + (task.getTargetDay() + 1) : " (any day)";
            String label = task.getName() + ", " + task.getDurationInSlots() + " slots" + daysStr;
            Button removeBtn = new Button("Remove");
            int idx = i;
            removeBtn.setOnAction(rm -> {
                taskList.remove(idx);
                updateTaskListBox(taskListBox);
            });
            HBox row = new HBox(8, new Label(label), removeBtn);
            row.setAlignment(Pos.CENTER_LEFT);
            taskListBox.getChildren().add(row);
        }
    }
    
    // Helper to update the event list UI in the generator panel
    private void updateEventListBox(VBox eventListBox) {
        eventListBox.getChildren().clear();
        for (int i = 0; i < eventList.size(); i++) {
            Event ev = eventList.get(i);
            String label = ev.getName() + ", " + ev.getEventDate() + ", " + (ev.getStartSlot()/2) + ":" + ((ev.getStartSlot()%2)*30 == 0 ? "00" : "30") + ", " + (ev.getDurationInSlots()/2) + "h";
            Button removeBtn = new Button("Remove");
            int idx = i;
            removeBtn.setOnAction(rm -> {
                eventList.remove(idx);
                updateEventListBox(eventListBox);
            });
            HBox row = new HBox(8, new Label(label), removeBtn);
            row.setAlignment(Pos.CENTER_LEFT);
            eventListBox.getChildren().add(row);
        }
    }
    // UI fields for generator panel
    private TextField workdaysField;
    private Spinner<Integer> workHourSpinner;
    private ComboBox<String> workMinuteBox;
    private Spinner<Integer> workDurationSpinner;
    private Spinner<Integer> sleepDurationSpinner;
    private TextArea tasksArea;
    private TextArea eventsArea;
    private DatePicker endDatePicker;
    private TextArea outputArea;
    // Calendar panel fields
    private Label calendarInfoLabel;
    private VBox calendarGridBox;
    private java.util.List<String> timetableDates;
    // Main root
    private BorderPane root;
    // Store events and tasks added in the generator panel
    private java.util.List<Event> eventList = new java.util.ArrayList<>();
    private java.util.List<Task> taskList = new java.util.ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Smart Weekly Scheduler");
        root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #667eea 0%, #764ba2 100%);");
        showLandingPanel();
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Landing panel with navigation
    private void showLandingPanel() {
        VBox landing = new VBox(30);
        landing.setAlignment(Pos.CENTER);
        landing.setPadding(new Insets(60));
        landing.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 20;");

        Label title = new Label("🗓️ Smart Scheduler");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        title.setTextFill(Color.rgb(102, 126, 234));

        Label subtitle = new Label("Plan your week, your way.");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 18));
        subtitle.setTextFill(Color.rgb(118, 75, 162));

        Button seeBtn = new Button("See Timetable");
        seeBtn.setStyle("-fx-background-color: #48bb78; -fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold; -fx-padding: 16 40; -fx-background-radius: 30;");
        seeBtn.setOnAction(e -> showCalendarPanel());

        Button genBtn = new Button("Generate New Timetable");
        genBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold; -fx-padding: 16 40; -fx-background-radius: 30;");
        genBtn.setOnAction(e -> showGeneratorPanel());

        landing.getChildren().addAll(title, subtitle, seeBtn, genBtn);
        root.setCenter(landing);
        root.setLeft(null);
        root.setRight(null);
    }

    // Calendar panel for viewing timetable by date
    private void showCalendarPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(40));
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setStyle("-fx-background-color: rgba(255,255,255,0.97); -fx-background-radius: 18;");

        Label header = new Label("📅 View Your Timetable");
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        header.setTextFill(Color.rgb(102, 126, 234));

        calendarInfoLabel = new Label("Select a date to view your schedule.");
        calendarInfoLabel.setFont(Font.font("Segoe UI", 14));

        // Fetch timetable dates
        ActivityRepository repo = new ActivityRepository();
        timetableDates = repo.getAllTimetableDates();

        calendarGridBox = new VBox(10);
        calendarGridBox.setAlignment(Pos.CENTER);
        if (timetableDates.isEmpty()) {
            calendarGridBox.getChildren().add(new Label("No timetables found. Generate one first!"));
        } else {
            calendarGridBox.getChildren().add(createCalendarGrid());
        }

        Button backBtn = new Button("⬅ Back");
        backBtn.setOnAction(e -> showLandingPanel());
        backBtn.setStyle("-fx-background-color: #e2e8f0; -fx-font-size: 14; -fx-background-radius: 20;");

        panel.getChildren().addAll(header, calendarInfoLabel, calendarGridBox, backBtn);
        root.setCenter(panel);
        root.setLeft(null);
        root.setRight(null);
    }

    // Generator panel for new timetable
    private void showGeneratorPanel() {
        BorderPane genRoot = new BorderPane();
        genRoot.setStyle("-fx-background-color: rgba(255,255,255,0.97); -fx-background-radius: 18;");

        // Left nav panel

    VBox navPanel = new VBox(20);
    navPanel.setPadding(new Insets(30, 10, 30, 30));
    navPanel.setAlignment(Pos.TOP_LEFT);
    navPanel.setStyle("-fx-background-color: #f7fafc; -fx-background-radius: 16; -fx-border-width: 0 2 0 0; -fx-border-color: #cbd5e0;");

    Button sleepBtn = new Button("😴 Sleep");
    Button workBtn = new Button("🏫 Work/School");
    Button tasksBtn = new Button("📝 Tasks");
    Button eventsBtn = new Button("🎉 Events");
    Button generateBtn = new Button("🚀 Generate Timetable");
    sleepBtn.setMaxWidth(Double.MAX_VALUE);
    workBtn.setMaxWidth(Double.MAX_VALUE);
    tasksBtn.setMaxWidth(Double.MAX_VALUE);
    eventsBtn.setMaxWidth(Double.MAX_VALUE);
    generateBtn.setMaxWidth(Double.MAX_VALUE);
    String navDefault = "-fx-background-color: #e2e8f0; -fx-font-size: 16; -fx-background-radius: 12;";
    String navSelected = "-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 16; -fx-background-radius: 12; -fx-font-weight: bold;";
    sleepBtn.setStyle(navSelected);
    workBtn.setStyle(navDefault);
    tasksBtn.setStyle(navDefault);
    eventsBtn.setStyle(navDefault);
    generateBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold; -fx-background-radius: 18; -fx-padding: 12 0;");

    navPanel.getChildren().addAll(sleepBtn, workBtn, tasksBtn, eventsBtn);
    navPanel.getChildren().add(new Label("") ); // Spacer
    navPanel.getChildren().add(generateBtn);


        // Right content panel (category details)
        StackPane contentPane = new StackPane();

        // Sleep content
        VBox sleepContent = new VBox(18);
        sleepContent.setAlignment(Pos.TOP_LEFT);
        sleepContent.setPadding(new Insets(40, 40, 40, 40));
        Label sleepHeader = new Label("😴 Sleep Settings");
        sleepHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        Label sleepLabel = new Label("Sleep Duration (hours):");
        sleepDurationSpinner = new Spinner<>(1, 24, 8);
        sleepDurationSpinner.setEditable(true);
        sleepContent.getChildren().addAll(sleepHeader, sleepLabel, sleepDurationSpinner);

        // Work/School content
        VBox workContent = new VBox(18);
        workContent.setAlignment(Pos.TOP_LEFT);
        workContent.setPadding(new Insets(40, 40, 40, 40));
        Label workHeader = new Label("🏫 Work/School Settings");
        workHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        Label workdaysLabel = new Label("Workdays (comma-separated, e.g. 1,2,3,4,5):");
        workdaysField = new TextField("1,2,3,4,5");
        workdaysField.setPrefWidth(180);
        Label workStartLabel = new Label("Work Start Time:");
        workHourSpinner = new Spinner<>(0, 23, 9);
        workHourSpinner.setEditable(true);
        workMinuteBox = new ComboBox<>();
        workMinuteBox.getItems().addAll("00", "30");
        workMinuteBox.setValue("00");
        HBox workStartBox = new HBox(8, workHourSpinner, new Label(":"), workMinuteBox);
        Label workDurationLabel = new Label("Work Duration (hours):");
        workDurationSpinner = new Spinner<>(1, 12, 8);
        workDurationSpinner.setEditable(true);
        workContent.getChildren().addAll(workHeader, workdaysLabel, workdaysField, workStartLabel, workStartBox, workDurationLabel, workDurationSpinner);

        // Tasks content
        VBox tasksContent = new VBox(18);
        tasksContent.setAlignment(Pos.TOP_LEFT);
        tasksContent.setPadding(new Insets(40, 40, 40, 40));
        Label tasksHeader = new Label("📝 Tasks");
        tasksHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));

        // Form fields for tasks
        HBox taskNameBox = new HBox(8, new Label("Name:"), new TextField());
        Spinner<Integer> taskDurationSpinner = new Spinner<>(1, 24, 1);
        HBox taskDurationBox = new HBox(8, new Label("Duration (hours):"), taskDurationSpinner);
        
        // Day selection with checkboxes
        Label daySelectLabel = new Label("Select Days (or none for any day):");
        daySelectLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");
        CheckBox monCheckBox = new CheckBox("Mon");
        CheckBox tueCheckBox = new CheckBox("Tue");
        CheckBox wedCheckBox = new CheckBox("Wed");
        CheckBox thuCheckBox = new CheckBox("Thu");
        CheckBox friCheckBox = new CheckBox("Fri");
        CheckBox satCheckBox = new CheckBox("Sat");
        CheckBox sunCheckBox = new CheckBox("Sun");
        HBox dayCheckBoxes = new HBox(10, monCheckBox, tueCheckBox, wedCheckBox, thuCheckBox, friCheckBox, satCheckBox, sunCheckBox);
        dayCheckBoxes.setAlignment(Pos.CENTER_LEFT);
        VBox daySelectionBox = new VBox(6, daySelectLabel, dayCheckBoxes);
        
        Button addTaskBtn = new Button("Add Task");
        Label addTaskMsg = new Label("");
        addTaskMsg.setStyle("-fx-text-fill: green; -fx-font-size: 12;");

        // List of added tasks
        VBox taskListBox = new VBox(6);
        taskListBox.setPadding(new Insets(8, 0, 0, 0));
        taskListBox.setStyle("-fx-background-color: #f7fafc; -fx-background-radius: 8;");

        addTaskBtn.setOnAction(e -> {
            String name = ((TextField)taskNameBox.getChildren().get(1)).getText().trim();
            int duration = taskDurationSpinner.getValue();
            
            // Collect selected days
            java.util.List<Integer> selectedDays = new java.util.ArrayList<>();
            if (monCheckBox.isSelected()) selectedDays.add(0);
            if (tueCheckBox.isSelected()) selectedDays.add(1);
            if (wedCheckBox.isSelected()) selectedDays.add(2);
            if (thuCheckBox.isSelected()) selectedDays.add(3);
            if (friCheckBox.isSelected()) selectedDays.add(4);
            if (satCheckBox.isSelected()) selectedDays.add(5);
            if (sunCheckBox.isSelected()) selectedDays.add(6);
            
            System.out.println("[DEBUG] Add Task clicked: name=" + name + ", duration=" + duration + ", selectedDays=" + selectedDays);
            if (name.isEmpty()) {
                addTaskMsg.setText("Please enter task name.");
                addTaskMsg.setStyle("-fx-text-fill: red; -fx-font-size: 12;");
                System.out.println("[DEBUG] Add Task validation failed: name is empty");
                return;
            }
            
            if (selectedDays.isEmpty()) {
                // No specific days selected, add as any day
                Task task = new Task(name, duration * 2);
                taskList.add(task);
            } else {
                // Add one task for each selected day
                for (Integer dayIdx : selectedDays) {
                    Task task = new Task(name, duration * 2, dayIdx);
                    taskList.add(task);
                }
            }
            
            System.out.println("[DEBUG] Task(s) added to taskList. Current size: " + taskList.size());
            addTaskMsg.setText("Task added!");
            addTaskMsg.setStyle("-fx-text-fill: green; -fx-font-size: 12;");
            // Clear fields
            ((TextField)taskNameBox.getChildren().get(1)).setText("");
            taskDurationSpinner.getValueFactory().setValue(1);
            monCheckBox.setSelected(false);
            tueCheckBox.setSelected(false);
            wedCheckBox.setSelected(false);
            thuCheckBox.setSelected(false);
            friCheckBox.setSelected(false);
            satCheckBox.setSelected(false);
            sunCheckBox.setSelected(false);
            updateTaskListBox(taskListBox);
        });

        // On panel show, always update task list display to reflect current persistent taskList
        updateTaskListBox(taskListBox);

        tasksContent.getChildren().addAll(tasksHeader, taskNameBox, taskDurationBox, daySelectionBox, addTaskBtn, addTaskMsg, new Label("Tasks:"), taskListBox);


        // Events content
        VBox eventsContent = new VBox(18);
        eventsContent.setAlignment(Pos.TOP_LEFT);
        eventsContent.setPadding(new Insets(40, 40, 40, 40));
        Label eventsHeader = new Label("🎉 Events");
        eventsHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));

        // Form fields
        HBox nameBox = new HBox(8, new Label("Name:"), new TextField());
        DatePicker eventDatePicker = new DatePicker();
        HBox dateBox = new HBox(8, new Label("Date:"), eventDatePicker);
        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, 18);
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, 0, 1);
        Spinner<Integer> durationSpinner = new Spinner<>(1, 24, 1);
        HBox timeBox = new HBox(8, new Label("Hour:"), hourSpinner, new Label("Minute:"), minuteSpinner, new Label("Duration (h):"), durationSpinner);
        Button addEventBtn = new Button("Add Event");
        Label addEventMsg = new Label("");
        addEventMsg.setStyle("-fx-text-fill: green; -fx-font-size: 12;");



        // List of added events (uses persistent class field eventList)
        VBox eventListBox = new VBox(6);
        eventListBox.setPadding(new Insets(8, 0, 0, 0));
        eventListBox.setStyle("-fx-background-color: #f7fafc; -fx-background-radius: 8;");

        addEventBtn.setOnAction(e -> {
            String name = ((TextField)nameBox.getChildren().get(1)).getText().trim();
            java.time.LocalDate date = eventDatePicker.getValue();
            int hour = hourSpinner.getValue();
            int minute = minuteSpinner.getValue();
            int duration = durationSpinner.getValue();
            System.out.println("[DEBUG] Add Event clicked: name=" + name + ", date=" + date + ", hour=" + hour + ", minute=" + minute + ", duration=" + duration);
            if (name.isEmpty() || date == null) {
                addEventMsg.setText("Please enter name and date.");
                addEventMsg.setStyle("-fx-text-fill: red; -fx-font-size: 12;");
                System.out.println("[DEBUG] Add Event validation failed: name or date is empty");
                return;
            }
            int slot = hour * 2 + (minute >= 30 ? 1 : 0);
            Event event = new Event(name, duration * 2, date, slot);
            eventList.add(event);
            System.out.println("[DEBUG] Event added to eventList. Current size: " + eventList.size());
            addEventMsg.setText("Event added!");
            addEventMsg.setStyle("-fx-text-fill: green; -fx-font-size: 12;");
            // Clear fields
            ((TextField)nameBox.getChildren().get(1)).setText("");
            eventDatePicker.setValue(null);
            hourSpinner.getValueFactory().setValue(18);
            minuteSpinner.getValueFactory().setValue(0);
            durationSpinner.getValueFactory().setValue(1);
            updateEventListBox(eventListBox);
        });

        // On panel show, always update event list display to reflect current persistent eventList
        updateEventListBox(eventListBox);

        eventsContent.getChildren().addAll(eventsHeader, nameBox, dateBox, timeBox, addEventBtn, addEventMsg, new Label("Events:"), eventListBox);

        // Show sleep by default
        contentPane.getChildren().setAll(sleepContent);

        // Button actions with highlight
        sleepBtn.setOnAction(e -> {
            contentPane.getChildren().setAll(sleepContent);
            sleepBtn.setStyle(navSelected);
            workBtn.setStyle(navDefault);
            tasksBtn.setStyle(navDefault);
            eventsBtn.setStyle(navDefault);
        });
        workBtn.setOnAction(e -> {
            contentPane.getChildren().setAll(workContent);
            sleepBtn.setStyle(navDefault);
            workBtn.setStyle(navSelected);
            tasksBtn.setStyle(navDefault);
            eventsBtn.setStyle(navDefault);
        });
        tasksBtn.setOnAction(e -> {
            contentPane.getChildren().setAll(tasksContent);
            sleepBtn.setStyle(navDefault);
            workBtn.setStyle(navDefault);
            tasksBtn.setStyle(navSelected);
            eventsBtn.setStyle(navDefault);
        });
        eventsBtn.setOnAction(e -> {
            contentPane.getChildren().setAll(eventsContent);
            sleepBtn.setStyle(navDefault);
            workBtn.setStyle(navDefault);
            tasksBtn.setStyle(navDefault);
            eventsBtn.setStyle(navSelected);
        });

        // End date picker and output area (now in sidebar)
        HBox endDateBox = new HBox(10);
        endDateBox.setAlignment(Pos.CENTER_LEFT);
        Label endDateLabel = new Label("End Date:");
        endDatePicker = new DatePicker();
        endDateBox.getChildren().addAll(endDateLabel, endDatePicker);

    outputArea = new TextArea();
    outputArea.setEditable(false);
    outputArea.setFocusTraversable(false);
    outputArea.setPrefRowCount(2);
    outputArea.setWrapText(true);
    outputArea.setStyle("-fx-font-family: 'Consolas'; -fx-background-radius: 8; -fx-background-color: #f7fafc; -fx-border-color: #cbd5e0; -fx-border-width: 1; -fx-text-fill: #2d3748; -fx-opacity: 1;");

        // Generate button
        generateBtn.setOnAction(e -> {
            if (endDatePicker.getValue() == null) {
                outputArea.setText("❗ Please choose an end date before generating the timetable.");
                return;
            }
            // Run generation in background
            new Thread(() -> {
                boolean success = generateSchedule();
                javafx.application.Platform.runLater(() -> {
                    if (success) {
                        showCalendarPanel();
                    } else {
                        outputArea.setText("❌ Failed to generate timetable. Check your input.");
                    }
                });
            }).start();
        });

        // Back button
        Button backBtn = new Button("⬅ Back");
        backBtn.setOnAction(e -> showLandingPanel());
        backBtn.setStyle("-fx-background-color: #e2e8f0; -fx-font-size: 14; -fx-background-radius: 20;");

        // Add end date, generate, output, and back to the bottom of the sidebar
        VBox navBottom = new VBox(14, endDateBox, generateBtn, outputArea, backBtn);
        navBottom.setAlignment(Pos.BOTTOM_LEFT);
        navBottom.setPadding(new Insets(30, 10, 30, 10));

        // Remove previous navPanel children after generateBtn, then add navBottom
        navPanel.getChildren().remove(navPanel.getChildren().size() - 1); // remove old spacer
        navPanel.getChildren().remove(generateBtn); // remove from previous location if present
        navPanel.getChildren().add(navBottom);

        VBox rightPanel = new VBox(10, contentPane);
        rightPanel.setAlignment(Pos.TOP_CENTER);
        rightPanel.setPadding(new Insets(0, 0, 0, 0));

        genRoot.setLeft(navPanel);
        genRoot.setCenter(rightPanel);
        root.setCenter(genRoot);
        root.setLeft(null);
        root.setRight(null);
    }

    // Generator panel input UI


    // Create a visually styled calendar grid for timetable dates
    private GridPane createCalendarGrid() {
        // Only show timetable dates from today onward
        java.time.LocalDate today = java.time.LocalDate.now();
        // Filter timetableDates to only those >= today
        java.util.List<String> futureDates = timetableDates.stream()
            .filter(d -> !java.time.LocalDate.parse(d).isBefore(today))
            .toList();
        if (futureDates.isEmpty()) {
            GridPane grid = new GridPane();
            grid.add(new Label("No future timetables found."), 0, 0);
            return grid;
        }
        java.time.LocalDate minDate = java.time.LocalDate.parse(futureDates.get(0));
        java.time.LocalDate maxDate = java.time.LocalDate.parse(futureDates.get(futureDates.size()-1));
        java.time.LocalDate firstOfMonth = minDate.withDayOfMonth(1);
        java.time.LocalDate lastOfMonth = maxDate.withDayOfMonth(maxDate.lengthOfMonth());

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setAlignment(Pos.CENTER);

        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            dayLabel.setTextFill(Color.rgb(102, 126, 234));
            grid.add(dayLabel, i, 0);
        }

        // Fill calendar
        java.time.LocalDate date = firstOfMonth;
        int row = 1;
        int col = (date.getDayOfWeek().getValue() + 6) % 7; // Monday=0
        while (!date.isAfter(lastOfMonth)) {
            String dateStr = date.toString();
            if (!java.time.LocalDate.parse(dateStr).isBefore(today)) {
                Button dayBtn = new Button(String.valueOf(date.getDayOfMonth()));
                dayBtn.setMinSize(40, 40);
                dayBtn.setMaxSize(40, 40);
                if (futureDates.contains(dateStr)) {
                    dayBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-border-color: #764ba2; -fx-border-width: 2;");
                    dayBtn.setOnAction(e -> showTimetableDayPanel(dateStr));
                } else {
                    dayBtn.setDisable(true);
                    dayBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #aaa; -fx-background-radius: 20;");
                }
                grid.add(dayBtn, col, row);
            }
            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
            date = date.plusDays(1);
        }
        return grid;
    }

    // Show timetable for a specific day in a new panel
    private void showTimetableDayPanel(String dateStr) {
        StackPane bgPane = new StackPane();
        bgPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8fafc 0%, #e9d8fd 100%);");

    VBox centerBox = new VBox(30);
    centerBox.setAlignment(Pos.CENTER);
    centerBox.setPadding(new Insets(30));

    // Back to Calendar button
    Button backBtn = new Button("⬅ Back to Calendar");
    backBtn.setStyle("-fx-background-color: #e2e8f0; -fx-font-size: 14; -fx-background-radius: 20;");
    backBtn.setOnAction(e -> showCalendarPanel());


        java.time.LocalDate dateObj = java.time.LocalDate.parse(dateStr);
        String dayOfWeek = dateObj.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault());
        String dateDisplay = dayOfWeek + ", " + dateObj.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault()) + " " + dateObj.getDayOfMonth() + ", " + dateObj.getYear();
        Label dateHeader = new Label(dateDisplay);
        dateHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        dateHeader.setTextFill(Color.rgb(102, 126, 234));
        dateHeader.setStyle("-fx-effect: dropshadow(gaussian, #b794f4, 8, 0.2, 0, 2);");

        // Card for timetable with vertical progress bar
        HBox cardRow = new HBox(0);
        cardRow.setAlignment(Pos.CENTER);

        // --- Vertical progress bar as border ---
        ActivityRepository repo = new ActivityRepository();
        java.util.List<ActivityRepository.TimetableEntry> entries = repo.getTimetableForDate(dateStr);
        boolean isToday = dateStr.equals(java.time.LocalDate.now().toString());
        VBox progressBarVBox = new VBox();
        progressBarVBox.setPrefWidth(18);
        progressBarVBox.setMinWidth(18);
        progressBarVBox.setMaxWidth(18);
        progressBarVBox.setPrefHeight(340);
        progressBarVBox.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 8;");
        if (isToday && !entries.isEmpty()) {
            int totalSlots = 48;
            java.time.LocalTime now = java.time.LocalTime.now();
            int currentSlot = now.getHour() * 2 + (now.getMinute() >= 30 ? 1 : 0);
            double progress = (currentSlot + 1) / (double) totalSlots;
            Region fill = new Region();
            fill.setPrefWidth(18);
            fill.setMinWidth(18);
            fill.setMaxWidth(18);
            fill.setPrefHeight(progressBarVBox.getPrefHeight() * progress);
            fill.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #48bb78 100%); -fx-background-radius: 8 8 8 8;");
            progressBarVBox.getChildren().add(fill);
        }
        // --- End vertical progress bar ---

        VBox card = new VBox(18);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(30, 30, 30, 30));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 24; -fx-effect: dropshadow(gaussian, #a0aec0, 16, 0.2, 0, 4);");

        VBox timetableBox = new VBox(10);
        timetableBox.setAlignment(Pos.TOP_CENTER);
        if (entries.isEmpty()) {
            timetableBox.getChildren().add(new Label("❌ No timetable found for " + dateStr));
        } else {
            int n = entries.size();
            int i = 0;
            java.time.LocalTime now = java.time.LocalTime.now();
            int currentSlot = isToday ? (now.getHour() * 2 + (now.getMinute() >= 30 ? 1 : 0)) : -1;
            int highlightIdx = -1, nextIdx = -1;
            int[] groupStart = new int[n];
            int[] groupEnd = new int[n];
            String[] groupActivity = new String[n];
            int groupCount = 0;
            double rowHeight = 38;
            while (i < n) {
                String activity = entries.get(i).activityName;
                int startSlot = entries.get(i).slot;
                int j = i + 1;
                while (j < n && entries.get(j).activityName.equals(activity) && entries.get(j).slot == entries.get(j-1).slot + 1) {
                    j++;
                }
                int endSlot = entries.get(j-1).slot;
                groupStart[groupCount] = startSlot;
                groupEnd[groupCount] = endSlot;
                groupActivity[groupCount] = activity;
                if (isToday && currentSlot >= startSlot && currentSlot <= endSlot) highlightIdx = groupCount;
                if (isToday && nextIdx == -1 && currentSlot < startSlot) nextIdx = groupCount;
                groupCount++;
                i = j;
            }
            for (int g = 0; g < groupCount; g++) {
                int startSlot = groupStart[g];
                int endSlot = groupEnd[g];
                String activity = groupActivity[g];
                int startHour = startSlot / 2;
                int startMin = (startSlot % 2) * 30;
                int endHour = (endSlot + 1) / 2;
                int endMin = ((endSlot + 1) % 2) * 30;
                String timeStr = String.format("%02d:%02d - %02d:%02d", startHour, startMin, endHour, endMin);
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPrefHeight(rowHeight);
                // Highlight current task row with a strong background and connect to progress bar
                if (isToday && g == highlightIdx) {
                    row.setStyle("-fx-background-color: linear-gradient(to right, #c6f6d5 0%, #e6fffa 100%); -fx-background-radius: 12; -fx-border-color: #48bb78; -fx-border-width: 0 0 0 6; -fx-border-radius: 12 0 0 12;");
                } else {
                    row.setStyle("");
                }
                Label timeLabel = new Label(timeStr);
                timeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
                Label actLabel = new Label(activity);
                actLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
                actLabel.setPadding(new Insets(6, 18, 6, 18));
                actLabel.setStyle("-fx-background-radius: 20; -fx-font-size: 15;");
                if (isToday && g == highlightIdx) {
                    actLabel.setStyle(actLabel.getStyle() + "-fx-background-color: #48bb78; -fx-text-fill: white; border: 2px solid #22543d;");
                } else if (isToday && g == nextIdx) {
                    actLabel.setStyle(actLabel.getStyle() + "-fx-background-color: #b2f5ea; -fx-text-fill: #22543d; border: 2px solid #81e6d9;");
                } else {
                    actLabel.setStyle(actLabel.getStyle() + "-fx-background-color: #e2e8f0; -fx-text-fill: #2d3748; border: 2px solid #cbd5e0;");
                }
                row.getChildren().addAll(new Label("⏰"), timeLabel, actLabel);
                timetableBox.getChildren().add(row);
            }
        }

    card.getChildren().addAll(dateHeader, timetableBox);
        cardRow.getChildren().addAll(progressBarVBox, card);
    centerBox.getChildren().addAll(cardRow, backBtn);
        bgPane.getChildren().add(centerBox);
        root.setCenter(bgPane);
    }


    private VBox createSection(String title, javafx.scene.Node... nodes) {
        VBox section = new VBox(8);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: #f7fafc; -fx-background-radius: 8;");
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        titleLabel.setTextFill(Color.rgb(45, 55, 72));
        
        section.getChildren().add(titleLabel);
        section.getChildren().addAll(nodes);
        return section;
    }

    /**
     * Returns true if generation and save succeeded, false otherwise.
     */
    private boolean generateSchedule() {
        // Delete all existing timetable entries before generating a new one
        ActivityRepository repo = new ActivityRepository();
        repo.deleteTimetableForDateRange("0000-01-01", "9999-12-31");

        // Collect user input
        int sleepHours = sleepDurationSpinner.getValue();
        String workdaysText = workdaysField.getText();
        int workHour = workHourSpinner.getValue();
        String workMinute = workMinuteBox.getValue();
        int workStartSlot = workHour * 2 + ("30".equals(workMinute) ? 1 : 0);
        int workDuration = workDurationSpinner.getValue();
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate end = endDatePicker.getValue();
        if (end == null || today.isAfter(end)) return false;

        // Parse workdays
        java.util.List<Integer> workdays = new java.util.ArrayList<>();
        for (String s : workdaysText.split(",")) {
            try { workdays.add(Integer.parseInt(s.trim()) - 1); } catch (Exception ex) {}
        }

        // Use taskList field directly
        java.util.List<Task> tasks = new java.util.ArrayList<>(taskList);
        // Use eventList field directly
        java.util.List<Event> events = new java.util.ArrayList<>(eventList);

        // Call backend SchedulerService to generate timetable
        SchedulerService scheduler = new SchedulerService();
        ScheduleResult result = scheduler.generateTimetable(
            workdays, workStartSlot, workDuration * 2, sleepHours * 2, tasks, events, today, end
        );

        // Save timetable to backend for chosen date range
        java.util.List<Activity> allActs = new java.util.ArrayList<>();
        allActs.add(new FixedActivity("Sleep", sleepHours * 2));
        allActs.add(new FixedActivity("Work/School", workDuration * 2));
        allActs.addAll(tasks);
        allActs.addAll(events);
        for (Activity act : allActs) repo.addActivity(act);
        java.util.Map<String, Integer> nameToId = repo.getActivityNameToIdMap();
    repo.saveTimetable(result.getTimetable(), today, end, nameToId);
        // UI update is done on JavaFX thread
        return true;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
