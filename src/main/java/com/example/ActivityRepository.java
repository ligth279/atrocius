package com.example;

import java.sql.*;
import java.util.*;

public class ActivityRepository {
    private final String dbUrl;
    private final String dbDriver;

    public ActivityRepository() {
        this("jdbc:sqlite:activities.db", "org.sqlite.JDBC");
    }

    public ActivityRepository(String dbUrl, String dbDriver) {
        this.dbUrl = dbUrl;
        this.dbDriver = dbDriver;
        loadDriver();
        createTablesIfNotExist();
    }

    private void loadDriver() {
        try {
            Class.forName(dbDriver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("JDBC Driver not found: " + dbDriver, e);
        }
    }

    private void createTablesIfNotExist() {
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS activities (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "durationInSlots INTEGER NOT NULL," +
                    "type TEXT NOT NULL" +
                    ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addActivity(Activity activity) {
        String sql = "INSERT INTO activities (name, durationInSlots, type) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(dbUrl);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, activity.getName());
            pstmt.setInt(2, activity.getDurationInSlots());
            pstmt.setString(3, activity.getClass().getSimpleName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Activity> getAllActivities() {
        List<Activity> activities = new ArrayList<>();
        String sql = "SELECT name, durationInSlots, type FROM activities";
        try (Connection conn = DriverManager.getConnection(dbUrl);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String name = rs.getString("name");
                int duration = rs.getInt("durationInSlots");
                String type = rs.getString("type");
                Activity activity;
                switch (type) {
                    case "FixedActivity":
                        activity = new FixedActivity(name, duration);
                        break;
                    case "Event":
                        activity = new Event(name, duration);
                        break;
                    case "Task":
                        activity = new Task(name, duration);
                        break;
                    default:
                        continue;
                }
                activities.add(activity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return activities;
    }
}
