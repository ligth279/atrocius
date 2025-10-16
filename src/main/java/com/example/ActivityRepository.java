package com.example;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ActivityRepository {
    private static final String DB_URL = initializeDbUrl();
    private static final String DB_DRIVER = "org.sqlite.JDBC";

    private static String initializeDbUrl() {
        String appDataPath = System.getenv("APPDATA");
        if (appDataPath == null || appDataPath.isEmpty()) {
            // Fallback for non-Windows or if APPDATA is not set
            appDataPath = System.getProperty("user.home");
        }
        File dbDir = new File(appDataPath, "iSmartSchedule");
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }
        return "jdbc:sqlite:" + dbDir.getAbsolutePath() + File.separator + "activities.db";
    }

    public ActivityRepository() {
        loadDriver();
        createTablesIfNotExist();
    }

    private void loadDriver() {
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("JDBC Driver not found: " + DB_DRIVER, e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private void createTablesIfNotExist() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS activities (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "durationInSlots INTEGER NOT NULL," +
                    "type TEXT NOT NULL," +
                    "event_date TEXT," + // ISO yyyy-MM-dd, nullable
                    "start_slot INTEGER" + // nullable
                    ")");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS timetable_entries (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "date TEXT NOT NULL," + // ISO yyyy-MM-dd
                    "slot INTEGER NOT NULL," + // 0-47
                    "activity_id INTEGER NOT NULL," +
                    "FOREIGN KEY(activity_id) REFERENCES activities(id)" +
                    ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addActivity(Activity activity) {
        String sql = "INSERT INTO activities (name, durationInSlots, type, event_date, start_slot) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, activity.getName());
            pstmt.setInt(2, activity.getDurationInSlots());
            pstmt.setString(3, activity.getClass().getSimpleName());
            if (activity instanceof Event) {
                Event event = (Event) activity;
                pstmt.setString(4, event.getEventDate().toString());
                pstmt.setInt(5, event.getStartSlot());
            } else {
                pstmt.setNull(4, java.sql.Types.VARCHAR);
                pstmt.setNull(5, java.sql.Types.INTEGER);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Activity> getAllActivities() {
        List<Activity> activities = new ArrayList<>();
        String sql = "SELECT name, durationInSlots, type, event_date, start_slot FROM activities";
        try (Connection conn = getConnection();
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
                        String dateStr = rs.getString("event_date");
                        java.time.LocalDate eventDate = (dateStr != null) ? java.time.LocalDate.parse(dateStr) : java.time.LocalDate.now();
                        int startSlot = rs.getInt("start_slot");
                        activity = new Event(name, duration, eventDate, startSlot);
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

    public void saveTimetable(Timetable timetable, java.time.LocalDate startDate, java.time.LocalDate endDate, Map<String, Integer> activityNameToId) {
        Activity[][] slots = timetable.getSlots();
        int days = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        try (Connection conn = getConnection()) {
            // Clear previous timetable entries for this date range
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM timetable_entries WHERE date >= ? AND date <= ?")) {
                del.setString(1, startDate.toString());
                del.setString(2, endDate.toString());
                del.executeUpdate();
            }
            // Insert new timetable entries
            String insertSql = "INSERT INTO timetable_entries (date, slot, activity_id) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                Set<String> seen = new HashSet<>();
                for (int day = 0; day < days; day++) {
                    java.time.LocalDate date = startDate.plusDays(day);
                    for (int slot = 0; slot < 48; slot++) {
                        Activity act = slots[day][slot];
                        if (act instanceof Event) {
                            String key = "EVENT:" + ((Event) act).getName() + ":" + ((Event) act).getEventDate() + ":" + ((Event) act).getStartSlot();
                            if (!seen.contains(key + ":" + date + ":" + slot)) {
                                Integer actId = null;
                                String sql = "SELECT id FROM activities WHERE name=? AND event_date=? AND start_slot=?";
                                try (PreparedStatement findStmt = conn.prepareStatement(sql)) {
                                    findStmt.setString(1, act.getName());
                                    findStmt.setString(2, ((Event) act).getEventDate().toString());
                                    findStmt.setInt(3, ((Event) act).getStartSlot());
                                    try (ResultSet rs = findStmt.executeQuery()) {
                                        if (rs.next()) actId = rs.getInt("id");
                                    }
                                }
                                if (actId != null) {
                                    pstmt.setString(1, date.toString());
                                    pstmt.setInt(2, slot);
                                    pstmt.setInt(3, actId);
                                    pstmt.addBatch();
                                    seen.add(key + ":" + date + ":" + slot);
                                }
                            }
                        } else if (act != null) {
                            String key = act.getClass().getSimpleName() + ":" + act.getName();
                            if (!seen.contains(key + ":" + date + ":" + slot)) {
                                Integer actId = activityNameToId.get(act.getName());
                                if (actId != null) {
                                    pstmt.setString(1, date.toString());
                                    pstmt.setInt(2, slot);
                                    pstmt.setInt(3, actId);
                                    pstmt.addBatch();
                                    seen.add(key + ":" + date + ":" + slot);
                                }
                            }
                        }
                    }
                }
                pstmt.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Integer> getActivityNameToIdMap() {
        Map<String, Integer> map = new HashMap<>();
        String sql = "SELECT id, name FROM activities";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("name"), rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public List<String> getAllTimetableDates() {
        List<String> dates = new ArrayList<>();
        String sql = "SELECT DISTINCT date FROM timetable_entries ORDER BY date";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                dates.add(rs.getString("date"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dates;
    }

    public List<TimetableEntry> getTimetableForDate(String date) {
        List<TimetableEntry> entries = new ArrayList<>();
        String sql = "SELECT t.slot, a.name, a.type, a.event_date, a.start_slot FROM timetable_entries t JOIN activities a ON t.activity_id = a.id WHERE t.date = ? ORDER BY t.slot";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int slot = rs.getInt("slot");
                    String name = rs.getString("name");
                    String type = rs.getString("type");
                    entries.add(new TimetableEntry(date, slot, name, type));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entries;
    }

    public void deleteTimetableForDateRange(String startDate, String endDate) {
        String sql = "DELETE FROM timetable_entries WHERE date >= ? AND date <= ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class TimetableEntry {
        public final String date;
        public final int slot;
        public final String activityName;
        public final String activityType;

        public TimetableEntry(String date, int slot, String activityName, String activityType) {
            this.date = date;
            this.slot = slot;
            this.activityName = activityName;
            this.activityType = activityType;
        }
    }
}