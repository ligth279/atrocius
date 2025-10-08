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
import java.util.function.BiConsumer;

public class SchedulerFX extends Application {
    
    // ========== HELPER METHODS FOR DARK MODE & UI COMPONENTS ==========
    
    /**
     * Apply dark mode text color to a label.
     * @param label The label to style
     * @param isHeader True for header color (brighter), false for regular text
     */
    private void applyDarkModeTextColor(Label label, boolean isHeader) {
        if (isHeader) {
            label.setTextFill(darkMode ? Color.rgb(147, 197, 253) : Color.rgb(45, 55, 72));
        } else {
            label.setTextFill(darkMode ? Color.rgb(203, 213, 225) : Color.rgb(45, 55, 72));
        }
    }
    
    /**
     * Create a section header with icon and title.
     */
    private HBox createSectionHeader(String icon, String title) {
        HBox headerBox = new HBox(10, new Label(icon), new Label(title));
        ((Label)headerBox.getChildren().get(1)).setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        applyDarkModeTextColor((Label)headerBox.getChildren().get(1), true);
        return headerBox;
    }
    
    /**
     * Create a styled content VBox with consistent padding and dark mode support.
     */
    private VBox createContentBox() {
        VBox content = new VBox(18);
        content.setAlignment(Pos.TOP_LEFT);
        content.setPadding(new Insets(40, 40, 40, 40));
        if (darkMode) {
            content.setStyle("-fx-background-color: #2d3250; -fx-background-radius: 18; -fx-border-color: #475569; -fx-border-width: 1;");
        } else {
            content.setStyle("-fx-background-color: #f7fafc; -fx-background-radius: 18; -fx-border-color: #cbd5e0; -fx-border-width: 1;");
        }
        return content;
    }
    
    /**
     * Apply hover effects to a button.
     */
    private void applyButtonHoverEffect(Button button, String baseStyle, String hoverStyle) {
        button.setStyle(baseStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
    }
    
    /**
     * Create a list of day checkboxes (Mon-Sun).
     */
    private java.util.List<CheckBox> createDayCheckBoxes(String style) {
        java.util.List<CheckBox> boxes = new java.util.ArrayList<>();
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String day : days) {
            CheckBox cb = new CheckBox(day);
            cb.setStyle(style);
            boxes.add(cb);
        }
        return boxes;
    }
    
    /**
     * Update navigation button styles with one selected and others default.
     */
    private void updateNavButtonStyles(String navDefault, String navSelected, Button selectedBtn, Button... otherButtons) {
        selectedBtn.setStyle(navSelected);
        for (Button btn : otherButtons) {
            btn.setStyle(navDefault);
        }
    }
    
    // ========== END HELPER METHODS ==========
    
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
    // private TextField workdaysField; // replaced by checkboxes
    // Workday checkboxes (Mon–Sun) for work section
    private CheckBox monWorkBox;
    private CheckBox tueWorkBox;
    private CheckBox wedWorkBox;
    private CheckBox thuWorkBox;
    private CheckBox friWorkBox;
    private CheckBox satWorkBox;
    private CheckBox sunWorkBox;
    private Spinner<Integer> workHourSpinner;
    private ComboBox<String> workMinuteBox;
    private Spinner<Integer> workDurationSpinner;
    private Spinner<Integer> sleepDurationSpinner;
    private DatePicker endDatePicker;
    private TextArea outputArea;
    // Calendar panel fields
    private java.util.List<String> timetableDates;
    // Main root
    private BorderPane root;
    // Store events and tasks added in the generator panel
    private java.util.List<Event> eventList = new java.util.ArrayList<>();
    private java.util.List<Task> taskList = new java.util.ArrayList<>();
    
    // Dark mode state
    private boolean darkMode = false;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Smart Weekly Scheduler");
        root = new BorderPane();
        applyRootBackground();
        showLandingPanel();
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    // Apply background based on dark mode
    private void applyRootBackground() {
        if (darkMode) {
            root.setStyle("-fx-background-color: linear-gradient(to bottom right, #1a1a2e 0%, #16213e 100%);");
        } else {
            root.setStyle("-fx-background-color: linear-gradient(to bottom right, #667eea 0%, #764ba2 100%);");
        }
    }
    
    // Toggle dark mode and refresh current panel
    private void toggleDarkMode() {
        darkMode = !darkMode;
        applyRootBackground();
        // Refresh the current panel by re-showing it
        // We'll track the current panel state
        showLandingPanel(); // For now, always return to landing when toggling
    }

    // Landing panel with navigation
    private void showLandingPanel() {
        VBox landing = new VBox(30);
        landing.setAlignment(Pos.CENTER);
        landing.setPadding(new Insets(60));
        
        // Dynamic styling based on dark mode
        if (darkMode) {
            landing.setStyle("-fx-background-color: rgba(30,32,48,0.95); -fx-background-radius: 20;");
        } else {
            landing.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 20;");
        }

        // Theme toggle button at top right
        Button themeToggle = new Button(darkMode ? "☀️" : "🌙");
        themeToggle.setStyle("-fx-background-color: transparent; -fx-font-size: 24; -fx-cursor: hand;");
        themeToggle.setOnAction(e -> toggleDarkMode());
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.TOP_RIGHT);
        topBar.getChildren().add(themeToggle);

        Label title = new Label("🗓️ Smart Scheduler");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        title.setTextFill(darkMode ? Color.rgb(147, 197, 253) : Color.rgb(102, 126, 234));

        Label subtitle = new Label("Plan your week, your way.");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 18));
        subtitle.setTextFill(darkMode ? Color.rgb(203, 213, 225) : Color.rgb(118, 75, 162));

        Button seeBtn = new Button("See Timetable");
        seeBtn.setStyle("-fx-background-color: #48bb78; -fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold; -fx-padding: 16 40; -fx-background-radius: 30;");
        seeBtn.setOnAction(e -> showCalendarPanel());

        Button genBtn = new Button("Generate New Timetable");
        genBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold; -fx-padding: 16 40; -fx-background-radius: 30;");
        genBtn.setOnAction(e -> showGeneratorPanel());

        landing.getChildren().addAll(topBar, title, subtitle, seeBtn, genBtn);
        root.setCenter(landing);
        root.setLeft(null);
        root.setRight(null);
    }

    // Calendar panel for viewing timetable by date
    private void showCalendarPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(40));
        panel.setAlignment(Pos.TOP_CENTER);
        
        // Dynamic styling based on dark mode
        if (darkMode) {
            panel.setStyle("-fx-background-color: rgba(30,32,48,0.97); -fx-background-radius: 18;");
        } else {
            panel.setStyle("-fx-background-color: rgba(255,255,255,0.97); -fx-background-radius: 18;");
        }

        Label header = new Label("📅 View Your Timetable");
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        header.setTextFill(darkMode ? Color.rgb(147, 197, 253) : Color.rgb(102, 126, 234));

        Label infoLabel = new Label("Select a date to view your schedule.");
        infoLabel.setFont(Font.font("Segoe UI", 14));
        infoLabel.setTextFill(darkMode ? Color.rgb(203, 213, 225) : Color.rgb(45, 55, 72));

        // Fetch timetable dates
        ActivityRepository repo = new ActivityRepository();
        timetableDates = repo.getAllTimetableDates();

        VBox gridBox = new VBox(10);
        gridBox.setAlignment(Pos.CENTER);
        if (timetableDates.isEmpty()) {
            Label emptyLabel = new Label("No timetables found. Generate one first!");
            emptyLabel.setTextFill(darkMode ? Color.rgb(203, 213, 225) : Color.rgb(45, 55, 72));
            gridBox.getChildren().add(emptyLabel);
        } else {
            gridBox.getChildren().add(createCalendarGrid());
        }

        Button backBtn = new Button("⬅ Back");
        backBtn.setOnAction(e -> showLandingPanel());
        backBtn.setStyle("-fx-background-color: #e2e8f0; -fx-font-size: 14; -fx-background-radius: 20;");

        panel.getChildren().addAll(header, infoLabel, gridBox, backBtn);
        root.setCenter(panel);
        root.setLeft(null);
        root.setRight(null);
    }

    // Generator panel for new timetable
    private void showGeneratorPanel() {
        BorderPane genRoot = new BorderPane();
        // Dynamic styling based on dark mode
        if (darkMode) {
            genRoot.setStyle("-fx-background-color: rgba(30,32,48,0.97); -fx-background-radius: 18;");
        } else {
            genRoot.setStyle("-fx-background-color: rgba(255,255,255,0.97); -fx-background-radius: 18;");
        }

        // Left nav panel
    VBox navPanel = new VBox(20);
    navPanel.setPadding(new Insets(30, 10, 30, 30));
    navPanel.setAlignment(Pos.TOP_LEFT);
    // Dynamic nav panel styling
    if (darkMode) {
        navPanel.setStyle("-fx-background-color: #1e2030; -fx-background-radius: 16; -fx-border-width: 0 2 0 0; -fx-border-color: #3a3f5c;");
    } else {
        navPanel.setStyle("-fx-background-color: #f7fafc; -fx-background-radius: 16; -fx-border-width: 0 2 0 0; -fx-border-color: #cbd5e0;");
    }

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
    
    // Dynamic button styling
    String navDefault = darkMode 
        ? "-fx-background-color: #2d3250; -fx-text-fill: #cbd5e0; -fx-font-size: 16; -fx-background-radius: 12;" 
        : "-fx-background-color: #e2e8f0; -fx-font-size: 16; -fx-background-radius: 12;";
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

    // Sleep content (modernized UI with dark mode)
    VBox sleepContent = createContentBox();
    HBox sleepHeaderBox = createSectionHeader("😴", "Sleep Settings");
    Label sleepLabel = new Label("Sleep Duration (hours):");
    applyDarkModeTextColor(sleepLabel, false);
    sleepDurationSpinner = new Spinner<>(1, 24, 8);
    sleepDurationSpinner.setEditable(true);
    sleepDurationSpinner.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #a0aec0; -fx-background-color: #fff; -fx-padding: 4 12; -fx-font-size: 16;");
    Label sleepTip = new Label("Recommended: 7–9 hours per night");
    sleepTip.setStyle(darkMode ? "-fx-font-size: 12; -fx-text-fill: #94a3b8;" : "-fx-font-size: 12; -fx-text-fill: #888;");
    sleepContent.getChildren().addAll(sleepHeaderBox, sleepLabel, sleepDurationSpinner, sleepTip);

    // Work/School content (modernized UI with dark mode)
    // Modular WorkSection usage
    String checkBoxStyle = "-fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 6 16; -fx-font-size: 15; -fx-border-color: #a0aec0; -fx-background-color: #fff;";
    java.util.List<CheckBox> workCheckBoxes = createDayCheckBoxes(checkBoxStyle);
    monWorkBox = workCheckBoxes.get(0);
    tueWorkBox = workCheckBoxes.get(1);
    wedWorkBox = workCheckBoxes.get(2);
    thuWorkBox = workCheckBoxes.get(3);
    friWorkBox = workCheckBoxes.get(4);
    satWorkBox = workCheckBoxes.get(5);
    sunWorkBox = workCheckBoxes.get(6);
    // Default Mon–Fri selected
    monWorkBox.setSelected(true);
    tueWorkBox.setSelected(true);
    wedWorkBox.setSelected(true);
    thuWorkBox.setSelected(true);
    friWorkBox.setSelected(true);
    workHourSpinner = new Spinner<>(0, 23, 9);
    workMinuteBox = new ComboBox<>();
    workMinuteBox.getItems().addAll("00", "30");
    workMinuteBox.setValue("00");
    workDurationSpinner = new Spinner<>(1, 12, 8);
    VBox workContent = new WorkSection(workHourSpinner, workMinuteBox, workDurationSpinner, workCheckBoxes, darkMode).getContent();

        // Tasks content
        // Modular TasksSection usage
        String taskCheckBoxStyle = "-fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 6 16; -fx-font-size: 15; -fx-border-color: #a0aec0; -fx-background-color: #fff;";
        java.util.List<CheckBox> taskDayBoxes = createDayCheckBoxes(taskCheckBoxStyle);
        TextField taskNameField = new TextField();
        Spinner<Integer> taskDurationSpinner = new Spinner<>(1, 24, 1);
        Button addTaskBtn = new Button("Add Task");
        Label addTaskMsg = new Label("");
        VBox taskListBox = new VBox(6);
        // On panel show, always update task list display to reflect current persistent taskList
        taskListBox.setPadding(new Insets(8, 0, 0, 0));
        taskListBox.setStyle("-fx-background-color: #fff; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #cbd5e0; -fx-border-width: 1;");
        CheckBox morningBox = new CheckBox();
        CheckBox eveningBox = new CheckBox();
        Runnable onClearTaskFields = () -> {
            taskNameField.setText("");
            taskDurationSpinner.getValueFactory().setValue(1);
            for (CheckBox cb : taskDayBoxes) cb.setSelected(false);
            morningBox.setSelected(false);
            eveningBox.setSelected(false);
            updateTaskListBox(taskListBox);
        };
        BiConsumer<com.example.TasksSection.TaskInput, Integer> onAddTask = (input, duration) -> {
            if (input.days.isEmpty()) {
                Task task = new Task(input.name, duration * 2, null, input.preferredTime);
                taskList.add(task);
            } else {
                for (Integer dayIdx : input.days) {
                    Task task = new Task(input.name, duration * 2, dayIdx, input.preferredTime);
                    taskList.add(task);
                }
            }
        };
        updateTaskListBox(taskListBox);
        VBox tasksContent = new TasksSection(
            taskNameField, taskDurationSpinner, taskDayBoxes, addTaskBtn, addTaskMsg, taskListBox, darkMode,
            onAddTask, onClearTaskFields, morningBox, eveningBox
        ).getContent();


        // Events content
        // Modular EventsSection usage
        TextField eventNameField = new TextField();
        DatePicker eventDatePicker = new DatePicker();
        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, 18);
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, 0, 1);
        Spinner<Integer> durationSpinner = new Spinner<>(1, 24, 1);
        Button addEventBtn = new Button("Add Event");
        Label addEventMsg = new Label("");
        VBox eventListBox = new VBox(6);
        eventListBox.setPadding(new Insets(8, 0, 0, 0));
        eventListBox.setStyle("-fx-background-color: #fff; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #cbd5e0; -fx-border-width: 1;");
        Runnable onClearEventFields = () -> {
            eventNameField.setText("");
            eventDatePicker.setValue(null);
            hourSpinner.getValueFactory().setValue(18);
            minuteSpinner.getValueFactory().setValue(0);
            durationSpinner.getValueFactory().setValue(1);
            updateEventListBox(eventListBox);
        };
        java.util.function.Consumer<com.example.EventsSection.EventData> onAddEvent = (data) -> {
            int slot = data.hour * 2 + (data.minute >= 30 ? 1 : 0);
            Event event = new Event(data.name, data.duration * 2, data.date, slot);
            eventList.add(event);
        };
        updateEventListBox(eventListBox);
        VBox eventsContent = new EventsSection(
            eventNameField, eventDatePicker, hourSpinner, minuteSpinner, durationSpinner, addEventBtn, addEventMsg, eventListBox, darkMode,
            onAddEvent, onClearEventFields
        ).getContent();

        // Show sleep by default
        contentPane.getChildren().setAll(sleepContent);

        // Button actions with highlight using helper method
        sleepBtn.setOnAction(e -> {
            contentPane.getChildren().setAll(sleepContent);
            updateNavButtonStyles(navDefault, navSelected, sleepBtn, workBtn, tasksBtn, eventsBtn);
        });
        workBtn.setOnAction(e -> {
            contentPane.getChildren().setAll(workContent);
            updateNavButtonStyles(navDefault, navSelected, workBtn, sleepBtn, tasksBtn, eventsBtn);
        });
        tasksBtn.setOnAction(e -> {
            contentPane.getChildren().setAll(tasksContent);
            updateNavButtonStyles(navDefault, navSelected, tasksBtn, sleepBtn, workBtn, eventsBtn);
        });
        eventsBtn.setOnAction(e -> {
            contentPane.getChildren().setAll(eventsContent);
            updateNavButtonStyles(navDefault, navSelected, eventsBtn, sleepBtn, workBtn, tasksBtn);
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


    /**
     * Returns true if generation and save succeeded, false otherwise.
     */
    private boolean generateSchedule() {
        // Delete all existing timetable entries before generating a new one
        ActivityRepository repo = new ActivityRepository();
        repo.deleteTimetableForDateRange("0000-01-01", "9999-12-31");

        // Collect user input
        int sleepHours = sleepDurationSpinner.getValue();
    // Build workdaysText from checkboxes (Mon=1, ..., Sun=7)
    StringBuilder workdaysTextBuilder = new StringBuilder();
    if (monWorkBox.isSelected()) workdaysTextBuilder.append("1,");
    if (tueWorkBox.isSelected()) workdaysTextBuilder.append("2,");
    if (wedWorkBox.isSelected()) workdaysTextBuilder.append("3,");
    if (thuWorkBox.isSelected()) workdaysTextBuilder.append("4,");
    if (friWorkBox.isSelected()) workdaysTextBuilder.append("5,");
    if (satWorkBox.isSelected()) workdaysTextBuilder.append("6,");
    if (sunWorkBox.isSelected()) workdaysTextBuilder.append("7,");
    String workdaysText = workdaysTextBuilder.length() > 0 ? workdaysTextBuilder.substring(0, workdaysTextBuilder.length()-1) : "";
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
    allActs.add(new FixedActivity("Work", workDuration * 2));
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
