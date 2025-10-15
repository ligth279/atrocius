package com.example.ui;

import com.example.ActivityRepository;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Animation;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.scene.paint.Color;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Visualises a day's worth of timetable entries using animated slot cards.
 */
public class TimetableDayView extends VBox {
    private final ObservableList<ActivityRepository.TimetableEntry> entries;
    private boolean darkMode;

    public TimetableDayView(ObservableList<ActivityRepository.TimetableEntry> entries) {
        this.entries = entries;
        getStyleClass().add("schedule-day");
        setSpacing(12);
        setPadding(new Insets(24));
        setAlignment(Pos.TOP_LEFT);
        rebuild();
        entries.addListener((ListChangeListener<ActivityRepository.TimetableEntry>) change -> rebuild());
    }

    public void bindDarkMode(BooleanProperty darkProperty) {
        darkMode = darkProperty.get();
        applyDarkModeClass();
        rebuild();
        darkProperty.addListener((obs, oldVal, newVal) -> {
            darkMode = newVal;
            applyDarkModeClass();
            rebuild();
        });
    }

    private void rebuild() {
        getChildren().clear();
        if (entries.isEmpty()) {
            Label empty = new Label("No entries for this date yet. Generate a schedule first!");
            empty.getStyleClass().add("meta");
            if (darkMode && !empty.getStyleClass().contains("dark")) {
                empty.getStyleClass().add("dark");
            }
            getChildren().add(empty);
            return;
        }
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        int currentSlot = now.getHour() * 2 + (now.getMinute() >= 30 ? 1 : 0);

        // Merge consecutive slots with the same activity
        int n = entries.size();
        int i = 0;
        while (i < n) {
            ActivityRepository.TimetableEntry startEntry = entries.get(i);
            int startSlot = startEntry.slot;
            String activityName = startEntry.activityName;
            int j = i + 1;
            while (j < n && entries.get(j).activityName.equals(activityName) && entries.get(j).slot == entries.get(j - 1).slot + 1) {
                j++;
            }
            int endSlotInclusive = entries.get(j - 1).slot;
            int endSlotExclusive = endSlotInclusive + 1;

            VBox slot = new VBox(4);
            slot.getStyleClass().add("schedule-slot");
            if (darkMode && !slot.getStyleClass().contains("dark")) {
                slot.getStyleClass().add("dark");
            }

            LocalDate entryDate = LocalDate.parse(startEntry.date);
            boolean isToday = entryDate.equals(today);
            if (isToday) {
                if (currentSlot >= startSlot && currentSlot < endSlotExclusive) {
                    slot.getStyleClass().add("schedule-slot--current");
                    applyPulseAnimation(slot);
                } else if (startSlot >= currentSlot) {
                    slot.getStyleClass().add("schedule-slot--upcoming");
                }
            }

            Label time = new Label(formatTimeRange(startSlot, endSlotExclusive));
            time.getStyleClass().add("title");
            Label activity = new Label(activityName);
            activity.getStyleClass().add("meta");
            if (darkMode) {
                if (!time.getStyleClass().contains("dark")) {
                    time.getStyleClass().add("dark");
                }
                if (!activity.getStyleClass().contains("dark")) {
                    activity.getStyleClass().add("dark");
                }
            }
            slot.getChildren().addAll(time, activity);
            getChildren().add(slot);

            i = j;
        }
    }

    private static String formatTimeRange(int startSlot, int endSlot) {
        int startHour = startSlot / 2;
        int startMinute = (startSlot % 2) * 30;
        int endHour = endSlot / 2;
        int endMinute = (endSlot % 2) * 30;
        return String.format("%02d:%02d - %02d:%02d", startHour, startMinute, endHour, endMinute);
    }

    private void applyPulseAnimation(VBox slot) {
        // Subtle glassy sweep with red border for current activity
        Object existing = slot.getProperties().get("gradientTimeline");
        if (existing instanceof Timeline timeline) {
            timeline.stop();
        }

        // Subtle cyan glow effect
        GaussianBlur blur = new GaussianBlur(2);
        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(6, 182, 212, 0.5));
        glow.setRadius(15);
        glow.setSpread(0.3);
        glow.setInput(blur);
        slot.setEffect(glow);

        // Animate gradient position from left to right only
        DoubleProperty offset = new SimpleDoubleProperty(0);
        offset.addListener((obs, oldV, newV) -> {
            double pos = newV.doubleValue();
            // Subtle cyan-to-blue gradient with red border
            String css = String.format(
                "-fx-background-color: linear-gradient(from 0%% 0%% to 100%% 0%%, " +
                "rgba(34, 211, 238, 0.95) %.0f%%, " +
                "rgba(14, 165, 233, 1.0) %.0f%%, " +
                "rgba(34, 211, 238, 0.95) %.0f%%); " +
                "-fx-background-radius: 18; " +
                "-fx-border-color: rgba(239, 68, 68, 0.8); " +  // red border
                "-fx-border-width: 2.5; " +
                "-fx-border-radius: 18; " +
                "-fx-padding: 12 16; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: white;",
                Math.max(0, pos - 25), pos, Math.min(100, pos + 25)
            );
            slot.setStyle(css);
        });

        // Slower, more subtle sweep (3 seconds)
        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(offset, 0)),
                new KeyFrame(Duration.seconds(3.0), new KeyValue(offset, 100))
        );
        tl.setAutoReverse(false);
        tl.setCycleCount(Animation.INDEFINITE);
        tl.setOnFinished(e -> offset.set(0));
        slot.getProperties().put("gradientTimeline", tl);
        tl.play();
    }

    private void applyDarkModeClass() {
        if (darkMode) {
            if (!getStyleClass().contains("dark")) {
                getStyleClass().add("dark");
            }
        } else {
            getStyleClass().remove("dark");
        }
    }
}
