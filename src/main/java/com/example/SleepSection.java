package com.example;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SleepSection {
    private final Spinner<Integer> sleepDurationSpinner;
    private final boolean darkMode;

    public SleepSection(Spinner<Integer> sleepDurationSpinner, boolean darkMode) {
        this.sleepDurationSpinner = sleepDurationSpinner;
        this.darkMode = darkMode;
    }

    public VBox getContent() {
        VBox sleepContent = new VBox(18);
        sleepContent.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        sleepContent.setPadding(new Insets(40, 40, 40, 40));
        if (darkMode) {
            sleepContent.setStyle("-fx-background-color: #2d3250; -fx-background-radius: 18; -fx-border-color: #475569; -fx-border-width: 1;");
        } else {
            sleepContent.setStyle("-fx-background-color: #f7fafc; -fx-background-radius: 18; -fx-border-color: #cbd5e0; -fx-border-width: 1;");
        }
        HBox sleepHeaderBox = new HBox(10, new Label("😴"), new Label("Sleep Settings"));
        Label headerLabel = (Label) sleepHeaderBox.getChildren().get(1);
        headerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        headerLabel.setTextFill(darkMode ? Color.rgb(147, 197, 253) : Color.rgb(45, 55, 72));
        Label sleepLabel = new Label("Sleep Duration (hours):");
        sleepLabel.setTextFill(darkMode ? Color.rgb(203, 213, 225) : Color.rgb(45, 55, 72));
        sleepDurationSpinner.setEditable(true);
        sleepDurationSpinner.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #a0aec0; -fx-background-color: #fff; -fx-padding: 4 12; -fx-font-size: 16;");
        Label sleepTip = new Label("Recommended: 7–9 hours per night");
        sleepTip.setStyle(darkMode ? "-fx-font-size: 12; -fx-text-fill: #94a3b8;" : "-fx-font-size: 12; -fx-text-fill: #888;");
        sleepContent.getChildren().addAll(sleepHeaderBox, sleepLabel, sleepDurationSpinner, sleepTip);
        return sleepContent;
    }
}