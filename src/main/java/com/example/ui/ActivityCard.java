package com.example.ui;

import com.example.Activity;
import com.example.Event;
import com.example.Task;
import javafx.animation.ScaleTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Animated card representation for {@link Activity}. The control adapts its
 * content based on the runtime type (Task, Event, Fixed activity) and exposes a
 * fluid hover animation to provide immediate feedback to the user.
 */
public class ActivityCard extends VBox {
    private final Label title = new Label();
    private final Label meta = new Label();
    private final Label chip = new Label();

    public ActivityCard() {
        getStyleClass().add("activity-card");
        setSpacing(12);
        setPadding(new Insets(18));

        title.getStyleClass().add("title");
        meta.getStyleClass().add("meta");
        chip.getStyleClass().add("chip");

        HBox header = new HBox(12, title, buildSpacer(), chip);
        header.setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(header, meta);

        installHoverAnimation(this);
    }

    public void setActivity(Activity activity) {
        title.setText(activity.getName());
        chip.setText(activity.getClass().getSimpleName());
        if (activity instanceof Event event) {
            meta.setText(String.format("%s  •  %s  •  %s",
                    event.getEventDate(),
                    formatDuration(activity.getDurationInSlots()),
                    formatSlot(event.getStartSlot())));
        } else if (activity instanceof Task task) {
            String dayInfo = task.getTargetDay() == null ? "Any day" : "Day " + (task.getTargetDay() + 1);
            meta.setText(String.format("%s  •  %s  •  %s",
                    dayInfo,
                    formatDuration(activity.getDurationInSlots()),
                    prettyTime(task.getPreferredTime())));
        } else {
            meta.setText(formatDuration(activity.getDurationInSlots()));
        }
    }

    public void bindDarkMode(javafx.beans.property.BooleanProperty darkModeProperty) {
        getStyleClass().remove("dark");
        if (darkModeProperty.get()) {
            getStyleClass().add("dark");
        }
        darkModeProperty.addListener((obs, wasDark, isDark) -> {
            if (isDark) {
                if (!getStyleClass().contains("dark")) {
                    getStyleClass().add("dark");
                }
            } else {
                getStyleClass().remove("dark");
            }
        });

        chip.textFillProperty().bind(Bindings.when(darkModeProperty)
                .then(javafx.scene.paint.Color.web("#e0e7ff"))
                .otherwise(javafx.scene.paint.Color.web("#3730a3")));
    }

    private static Node buildSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private static void installHoverAnimation(Node node) {
        ScaleTransition enter = new ScaleTransition(Duration.millis(180), node);
        enter.setToX(1.025);
        enter.setToY(1.025);
        ScaleTransition exit = new ScaleTransition(Duration.millis(180), node);
        exit.setToX(1);
        exit.setToY(1);

        node.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> enter.playFromStart());
        node.addEventHandler(MouseEvent.MOUSE_EXITED, e -> exit.playFromStart());
    }

    private static String formatDuration(int slots) {
        int minutes = slots * 30;
        int hours = minutes / 60;
        int mins = minutes % 60;
        if (hours > 0 && mins > 0) {
            return hours + "h " + mins + "m";
        } else if (hours > 0) {
            return hours + (hours == 1 ? " hour" : " hours");
        }
        return mins + " minutes";
    }

    private static String formatSlot(int slot) {
        int hour = slot / 2;
        boolean half = slot % 2 == 1;
        return String.format("%02d:%02d", hour, half ? 30 : 0);
    }

    private static String prettyTime(String preferredTime) {
        return switch (preferredTime == null ? "any" : preferredTime.toLowerCase()) {
            case "morning" -> "Morning";
            case "evening" -> "Evening";
            default -> "Any time";
        };
    }
}
