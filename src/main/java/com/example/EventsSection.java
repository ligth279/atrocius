package com.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.function.Consumer;

public class EventsSection {
    private final TextField eventNameField;
    private final DatePicker eventDatePicker;
    private final Spinner<Integer> hourSpinner;
    private final Spinner<Integer> minuteSpinner;
    private final Spinner<Integer> durationSpinner;
    private final Button addEventBtn;
    private final Label addEventMsg;
    private final VBox eventListBox;
    private final boolean darkMode;
    private final Consumer<EventData> onAddEvent;
    private final Runnable onClearFields;

    public static class EventData {
        public final String name;
        public final java.time.LocalDate date;
        public final int hour;
        public final int minute;
        public final int duration;
        public EventData(String name, java.time.LocalDate date, int hour, int minute, int duration) {
            this.name = name;
            this.date = date;
            this.hour = hour;
            this.minute = minute;
            this.duration = duration;
        }
    }

    public EventsSection(TextField eventNameField, DatePicker eventDatePicker, Spinner<Integer> hourSpinner,
                        Spinner<Integer> minuteSpinner, Spinner<Integer> durationSpinner, Button addEventBtn,
                        Label addEventMsg, VBox eventListBox, boolean darkMode,
                        Consumer<EventData> onAddEvent, Runnable onClearFields) {
        this.eventNameField = eventNameField;
        this.eventDatePicker = eventDatePicker;
        this.hourSpinner = hourSpinner;
        this.minuteSpinner = minuteSpinner;
        this.durationSpinner = durationSpinner;
        this.addEventBtn = addEventBtn;
        this.addEventMsg = addEventMsg;
        this.eventListBox = eventListBox;
        this.darkMode = darkMode;
        this.onAddEvent = onAddEvent;
        this.onClearFields = onClearFields;
    }

    public VBox getContent() {
        VBox eventsContent = new VBox(18);
        eventsContent.setAlignment(Pos.TOP_LEFT);
        eventsContent.setPadding(new Insets(40, 40, 40, 40));
        if (darkMode) {
            eventsContent.setStyle("-fx-background-color: #2d3250; -fx-background-radius: 18; -fx-border-color: #475569; -fx-border-width: 1;");
        } else {
            eventsContent.setStyle("-fx-background-color: #f7fafc; -fx-background-radius: 18; -fx-border-color: #cbd5e0; -fx-border-width: 1;");
        }
        HBox eventsHeaderBox = new HBox(10, new Label("\uD83C\uDF89"), new Label("Events"));
        Label headerLabel = (Label) eventsHeaderBox.getChildren().get(1);
        headerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        headerLabel.setTextFill(darkMode ? Color.rgb(147, 197, 253) : Color.rgb(45, 55, 72));

        Label eventNameLabel = new Label("Event Name:");
        eventNameLabel.setTextFill(darkMode ? Color.rgb(203, 213, 225) : Color.rgb(45, 55, 72));
        eventNameField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #a0aec0; -fx-background-color: #fff; -fx-padding: 4 12; -fx-font-size: 15;");

        Label eventDateLabel = new Label("Event Date:");
        eventDateLabel.setTextFill(darkMode ? Color.rgb(203, 213, 225) : Color.rgb(45, 55, 72));
        eventDatePicker.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #a0aec0; -fx-background-color: #fff; -fx-padding: 4 12; -fx-font-size: 15;");

        Label eventTimeLabel = new Label("Event Time & Duration:");
        eventTimeLabel.setTextFill(darkMode ? Color.rgb(203, 213, 225) : Color.rgb(45, 55, 72));
        eventTimeLabel.setPadding(new Insets(0, 0, 8, 0));
        hourSpinner.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #a0aec0; -fx-background-color: #fff; -fx-padding: 4 12; -fx-font-size: 15;");
        minuteSpinner.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #a0aec0; -fx-background-color: #fff; -fx-padding: 4 12; -fx-font-size: 15;");
        durationSpinner.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #a0aec0; -fx-background-color: #fff; -fx-padding: 4 12; -fx-font-size: 15;");
        HBox timeBox = new HBox(8, new Label("Hour:"), hourSpinner, new Label("Minute:"), minuteSpinner, new Label("Duration (h):"), durationSpinner);
        timeBox.setAlignment(Pos.CENTER_LEFT);
        VBox eventTimeBox = new VBox(10, eventTimeLabel, timeBox);
        eventTimeBox.setPadding(new Insets(14, 14, 14, 14));
        eventTimeBox.setStyle("-fx-background-color: #fff; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #cbd5e0; -fx-border-width: 1;");
        Label eventTimeTip = new Label("Events are fixed at specific times.");
        eventTimeTip.setStyle(darkMode ? "-fx-font-size: 12; -fx-text-fill: #94a3b8;" : "-fx-font-size: 12; -fx-text-fill: #888;");

        addEventBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold; -fx-background-radius: 12; -fx-padding: 8 24; -fx-cursor: hand;");
        addEventMsg.setStyle("-fx-text-fill: green; -fx-font-size: 12;");

        Label addedEventsLabel = new Label("Added Events:");
        addedEventsLabel.setTextFill(darkMode ? Color.rgb(203, 213, 225) : Color.rgb(45, 55, 72));

        addEventBtn.setOnAction(e -> {
            String name = eventNameField.getText().trim();
            java.time.LocalDate date = eventDatePicker.getValue();
            int hour = hourSpinner.getValue();
            int minute = minuteSpinner.getValue();
            int duration = durationSpinner.getValue();
            if (name.isEmpty() || date == null) {
                addEventMsg.setText("Please enter name and date.");
                addEventMsg.setStyle("-fx-text-fill: red; -fx-font-size: 12;");
                return;
            }
            onAddEvent.accept(new EventData(name, date, hour, minute, duration));
            addEventMsg.setText("Event added!");
            addEventMsg.setStyle("-fx-text-fill: green; -fx-font-size: 12;");
            onClearFields.run();
        });

        eventsContent.getChildren().addAll(eventsHeaderBox, eventNameLabel, eventNameField, eventDateLabel, eventDatePicker, eventTimeBox, eventTimeTip, addEventBtn, addEventMsg, addedEventsLabel, eventListBox);
        return eventsContent;
    }
}
