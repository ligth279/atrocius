package com.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;
import java.util.function.BiConsumer;

public class TasksSection {
    private final TextField taskNameField;
    private final Spinner<Integer> taskDurationSpinner;
    private final List<CheckBox> taskDayBoxes;
    private final Button addTaskBtn;
    private final Label addTaskMsg;
    private final VBox taskListBox;
    private final boolean darkMode;
    private final BiConsumer<TaskInput, Integer> onAddTask;
    private final Runnable onClearFields;
    private final CheckBox morningBox;
    private final CheckBox eveningBox;

    public static class TaskInput {
        public final String name;
        public final List<Integer> days;
        public final String preferredTime; // "morning", "evening", or "any"
        public TaskInput(String name, List<Integer> days, String preferredTime) {
            this.name = name;
            this.days = days;
            this.preferredTime = preferredTime;
        }
    }

    public TasksSection(TextField taskNameField, Spinner<Integer> taskDurationSpinner, List<CheckBox> taskDayBoxes,
                        Button addTaskBtn, Label addTaskMsg, VBox taskListBox, boolean darkMode,
                        BiConsumer<TaskInput, Integer> onAddTask, Runnable onClearFields,
                        CheckBox morningBox, CheckBox eveningBox) {
        this.taskNameField = taskNameField;
        this.taskDurationSpinner = taskDurationSpinner;
        this.taskDayBoxes = taskDayBoxes;
        this.addTaskBtn = addTaskBtn;
        this.addTaskMsg = addTaskMsg;
        this.taskListBox = taskListBox;
        this.darkMode = darkMode;
        this.onAddTask = onAddTask;
        this.onClearFields = onClearFields;
        this.morningBox = morningBox;
        this.eveningBox = eveningBox;
    }

    public VBox getContent() {
        VBox tasksContent = new VBox(18);
        tasksContent.setAlignment(Pos.TOP_LEFT);
        tasksContent.setPadding(new Insets(40, 40, 40, 40));
        if (darkMode) {
            tasksContent.setStyle("-fx-background-color: #2d3250; -fx-background-radius: 18; -fx-border-color: #475569; -fx-border-width: 1;");
        } else {
            tasksContent.setStyle("-fx-background-color: #f7fafc; -fx-background-radius: 18; -fx-border-color: #cbd5e0; -fx-border-width: 1;");
        }
        HBox tasksHeaderBox = new HBox(10, new Label("\uD83D\uDCDD"), new Label("Tasks"));
        Label headerLabel = (Label) tasksHeaderBox.getChildren().get(1);
        headerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        headerLabel.setTextFill(darkMode ? Color.rgb(147, 197, 253) : Color.rgb(45, 55, 72));

        Label taskNameLabel = new Label("Task Name:");
        taskNameLabel.setTextFill(darkMode ? Color.rgb(203, 213, 225) : Color.rgb(45, 55, 72));
        taskNameField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #a0aec0; -fx-background-color: #fff; -fx-padding: 4 12; -fx-font-size: 15;");

        Label taskDurationLabel = new Label("Duration (hours):");
        taskDurationLabel.setTextFill(darkMode ? Color.rgb(203, 213, 225) : Color.rgb(45, 55, 72));
        taskDurationSpinner.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #a0aec0; -fx-background-color: #fff; -fx-padding: 4 12; -fx-font-size: 15;");

        Label daySelectLabel = new Label("Select Days (or none for any day):");
        daySelectLabel.setStyle(darkMode ? "-fx-font-size: 15; -fx-text-fill: #cbd5e0;" : "-fx-font-size: 15; -fx-text-fill: #2d3748;");
        daySelectLabel.setPadding(new Insets(0, 0, 8, 0));
        HBox dayCheckBoxes = new HBox(10);
        dayCheckBoxes.getChildren().addAll(taskDayBoxes);
        dayCheckBoxes.setAlignment(Pos.CENTER_LEFT);
        VBox daySelectionBox = new VBox(10, daySelectLabel, dayCheckBoxes);
        daySelectionBox.setPadding(new Insets(14, 14, 14, 14));
        daySelectionBox.setStyle("-fx-background-color: #fff; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #cbd5e0; -fx-border-width: 1;");
        Label daySelectTip = new Label("Tasks are scheduled during free time slots.");
        daySelectTip.setStyle(darkMode ? "-fx-font-size: 12; -fx-text-fill: #94a3b8;" : "-fx-font-size: 12; -fx-text-fill: #888;");

        // Preferred time checkboxes
        HBox preferredTimeBox = new HBox(12);
        Label prefLabel = new Label("Preferred Time:");
        prefLabel.setTextFill(darkMode ? Color.rgb(203, 213, 225) : Color.rgb(45, 55, 72));
        morningBox.setText("Morning");
        eveningBox.setText("Evening");
        // Make checkboxes mutually exclusive
        morningBox.setOnAction(e -> {
            if (morningBox.isSelected()) {
                eveningBox.setSelected(false);
            }
        });
        eveningBox.setOnAction(e -> {
            if (eveningBox.isSelected()) {
                morningBox.setSelected(false);
            }
        });
        preferredTimeBox.getChildren().addAll(prefLabel, morningBox, eveningBox);
        preferredTimeBox.setAlignment(Pos.CENTER_LEFT);

        addTaskBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 8 24; -fx-cursor: hand;");
        addTaskMsg.setStyle("-fx-text-fill: green; -fx-font-size: 12;");

        Label addedTasksLabel = new Label("Added Tasks:");
        addedTasksLabel.setTextFill(darkMode ? Color.rgb(203, 213, 225) : Color.rgb(45, 55, 72));

        addTaskBtn.setOnAction(e -> {
            String name = taskNameField.getText().trim();
            int duration = taskDurationSpinner.getValue();
            if (name.isEmpty()) {
                addTaskMsg.setText("Please enter task name.");
                addTaskMsg.setStyle("-fx-text-fill: red; -fx-font-size: 12;");
                return;
            }
            java.util.List<Integer> selectedDays = new java.util.ArrayList<>();
            for (int i = 0; i < taskDayBoxes.size(); i++) {
                if (taskDayBoxes.get(i).isSelected()) selectedDays.add(i);
            }
            String preferredTime = "any";
            if (morningBox.isSelected() && !eveningBox.isSelected()) preferredTime = "morning";
            else if (!morningBox.isSelected() && eveningBox.isSelected()) preferredTime = "evening";
            else if (morningBox.isSelected() && eveningBox.isSelected()) preferredTime = "any";
            onAddTask.accept(new TaskInput(name, selectedDays, preferredTime), duration);
            addTaskMsg.setText("Task added!");
            addTaskMsg.setStyle("-fx-text-fill: green; -fx-font-size: 12;");
            onClearFields.run();
        });

        tasksContent.getChildren().addAll(tasksHeaderBox, taskNameLabel, taskNameField, taskDurationLabel, taskDurationSpinner, daySelectionBox, preferredTimeBox, daySelectTip, addTaskBtn, addTaskMsg, addedTasksLabel, taskListBox);
        return tasksContent;
    }
}
