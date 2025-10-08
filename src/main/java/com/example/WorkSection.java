package com.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;

public class WorkSection {
    private final Spinner<Integer> workHourSpinner;
    private final ComboBox<String> workMinuteBox;
    private final Spinner<Integer> workDurationSpinner;
    private final List<CheckBox> workCheckBoxes;
    private final boolean darkMode;

    public WorkSection(Spinner<Integer> workHourSpinner, ComboBox<String> workMinuteBox, Spinner<Integer> workDurationSpinner, List<CheckBox> workCheckBoxes, boolean darkMode) {
        this.workHourSpinner = workHourSpinner;
        this.workMinuteBox = workMinuteBox;
        this.workDurationSpinner = workDurationSpinner;
        this.workCheckBoxes = workCheckBoxes;
        this.darkMode = darkMode;
    }

    public VBox getContent() {
        VBox workContent = new VBox(18);
        workContent.setAlignment(Pos.TOP_LEFT);
        workContent.setPadding(new Insets(40, 40, 40, 40));
        if (darkMode) {
            workContent.setStyle("-fx-background-color: #2d3250; -fx-background-radius: 18; -fx-border-color: #475569; -fx-border-width: 1;");
        } else {
            workContent.setStyle("-fx-background-color: #f7fafc; -fx-background-radius: 18; -fx-border-color: #cbd5e0; -fx-border-width: 1;");
        }
        HBox workHeaderBox = new HBox(10, new Label("\uD83C\uDFEB"), new Label("Work/School Settings"));
        Label headerLabel = (Label) workHeaderBox.getChildren().get(1);
        headerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        headerLabel.setTextFill(darkMode ? Color.rgb(147, 197, 253) : Color.rgb(45, 55, 72));

        Label workdaysLabel = new Label("Select Workdays:");
        workdaysLabel.setTextFill(darkMode ? Color.rgb(203, 213, 225) : Color.rgb(45, 55, 72));

        HBox workdayCheckBoxes = new HBox(10);
        workdayCheckBoxes.getChildren().addAll(workCheckBoxes);
        workdayCheckBoxes.setAlignment(Pos.CENTER_LEFT);
        Label workdaysLabel2 = new Label("Select Workdays:");
        workdaysLabel2.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        workdaysLabel2.setTextFill(darkMode ? Color.rgb(203, 213, 225) : Color.rgb(45, 55, 72));
        workdaysLabel2.setPadding(new Insets(0, 0, 8, 0));
        VBox workdayBox = new VBox(10, workdaysLabel2, workdayCheckBoxes);
        workdayBox.setPadding(new Insets(14, 14, 14, 14));
        workdayBox.setStyle("-fx-background-color: #fff; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #cbd5e0; -fx-border-width: 1;");
        Label workdaysTip = new Label("Workdays repeat each week. Leave unchecked for days off.");
        workdaysTip.setStyle(darkMode ? "-fx-font-size: 12; -fx-text-fill: #94a3b8;" : "-fx-font-size: 12; -fx-text-fill: #888;");

        Label workStartLabel = new Label("Work Start Time:");
        workStartLabel.setTextFill(darkMode ? Color.rgb(203, 213, 225) : Color.rgb(45, 55, 72));
        workHourSpinner.setEditable(true);
        workHourSpinner.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #a0aec0; -fx-background-color: #fff; -fx-padding: 4 12; -fx-font-size: 16;");
        workMinuteBox.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #a0aec0; -fx-background-color: #fff; -fx-padding: 4 12; -fx-font-size: 16;");
        HBox workStartBox = new HBox(8, workHourSpinner, new Label(":"), workMinuteBox);

        Label workDurationLabel = new Label("Work Duration (hours):");
        workDurationLabel.setTextFill(darkMode ? Color.rgb(203, 213, 225) : Color.rgb(45, 55, 72));
        workDurationSpinner.setEditable(true);
        workDurationSpinner.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #a0aec0; -fx-background-color: #fff; -fx-padding: 4 12; -fx-font-size: 16;");

        workContent.getChildren().setAll(workHeaderBox, workdaysLabel, workdayBox, workdaysTip, workStartLabel, workStartBox, workDurationLabel, workDurationSpinner);
        return workContent;
    }
}
