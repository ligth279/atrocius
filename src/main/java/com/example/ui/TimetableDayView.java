package com.example.ui;

import com.example.ActivityRepository;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.scene.paint.Color;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.DropShadow;

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
        int currentSlot = now.getHour() * 4 + now.getMinute() / 15;

        // Merge consecutive slots with the same activity
        int n = entries.size();
        int i = 0;
        while (i < n) {
            ActivityRepository.TimetableEntry startEntry = entries.get(i);
            int startSlot = startEntry.slot;
            String activityName = startEntry.activityName;
            String activityType = startEntry.activityType;
            SlotCategory category = classifySlot(activityName, activityType);
            int j = i + 1;
            while (j < n && entries.get(j).activityName.equals(activityName) && entries.get(j).slot == entries.get(j - 1).slot + 1) {
                j++;
            }
            int endSlotInclusive = entries.get(j - 1).slot;
            int endSlotExclusive = endSlotInclusive + 1;

            VBox slot = new VBox(6);
            slot.getStyleClass().add("schedule-slot");
            applyCategoryStyles(slot, category, activityName);
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

            Label startTime = new Label(formatSlot(startSlot));
            startTime.getStyleClass().add("title");
            Label iconLabel = new Label(getIconFor(category, activityName));
            iconLabel.getStyleClass().add("slot-icon");
            Label activity = new Label(activityName);
            activity.getStyleClass().add("meta");
            Label endTime = new Label(formatSlot(endSlotExclusive));
            endTime.getStyleClass().add("title");
            if (darkMode) {
                for (Label lbl : new Label[]{startTime, activity, iconLabel, endTime}) {
                    if (!lbl.getStyleClass().contains("dark")) {
                        lbl.getStyleClass().add("dark");
                    }
                }
            }
            // Use spacers to spread out content
            javafx.scene.layout.Region leftSpacer = new javafx.scene.layout.Region();
            javafx.scene.layout.Region rightSpacer = new javafx.scene.layout.Region();
            HBox.setHgrow(leftSpacer, javafx.scene.layout.Priority.ALWAYS);
            HBox.setHgrow(rightSpacer, javafx.scene.layout.Priority.ALWAYS);
            HBox row = new HBox(18, startTime, leftSpacer, iconLabel, activity, rightSpacer, endTime);
            row.setAlignment(Pos.CENTER);
            slot.getChildren().add(row);
            getChildren().add(slot);

            i = j;
        }
    }

    private void applyCategoryStyles(VBox slot, SlotCategory category, String activityName) {
        if (category == SlotCategory.WORK && activityName != null && !activityName.equalsIgnoreCase("Work")) {
            // Respect custom name while keeping work palette
            slot.getStyleClass().add("slot-work");
            return;
        }

        switch (category) {
            case EVENT -> slot.getStyleClass().add("slot-event");
            case TASK -> slot.getStyleClass().add("slot-task");
            case WORK -> slot.getStyleClass().add("slot-work");
            case SLEEP -> slot.getStyleClass().add("slot-sleep");
            case FIXED -> slot.getStyleClass().add("slot-fixed");
            default -> {
                // Leave default styling for uncategorised entries
            }
        }
    }

    private SlotCategory classifySlot(String activityName, String activityType) {
        if (activityType == null) {
            return SlotCategory.GENERIC;
        }

        String normalized = activityType.trim().toLowerCase();
        String lowerName = activityName == null ? "" : activityName.toLowerCase();
        return switch (normalized) {
            case "event" -> SlotCategory.EVENT;
            case "task" -> SlotCategory.TASK;
            case "fixedactivity" -> {
                if (lowerName.contains("sleep")) {
                    yield SlotCategory.SLEEP;
                }
                if (lowerName.contains("work") || lowerName.contains("school")) {
                    yield SlotCategory.WORK;
                }
                yield SlotCategory.FIXED;
            }
            default -> SlotCategory.GENERIC;
        };
    }

    private String getIconFor(SlotCategory category, String activityName) {
        return switch (category) {
            case EVENT -> "🎉";
            case TASK -> "📝";
            case WORK -> "💼";
            case SLEEP -> "😴";
            case FIXED -> "📌";
            default -> activityName != null && !activityName.isBlank() ? "📋" : "⬜";
        };
    }

    private static String formatSlot(int slot) {
        int hour = slot / 4;
        int minute = (slot % 4) * 15;
        return String.format("%02d:%02d", hour, minute);
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

    private enum SlotCategory {
        EVENT,
        TASK,
        WORK,
        SLEEP,
        FIXED,
        GENERIC
    }
}
