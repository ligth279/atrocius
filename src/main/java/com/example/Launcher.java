package com.example;

/**
 * A launcher class to work around the JavaFX + shaded JAR issue.
 */
public class Launcher {
    public static void main(String[] args) {
        SchedulerFX.main(args);
    }
}
