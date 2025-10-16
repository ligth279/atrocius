
package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;



public class SchedulerGUI extends JFrame {
    private JTextArea outputArea;
    private JButton generateButton;
    private JTextField workdaysField;
    private JSpinner workHourSpinner;
    private JComboBox<String> workMinuteBox;
    private JSpinner workDurationSpinner;
    private JSpinner sleepDurationSpinner;
    private JTextArea tasksArea;
    private JTextArea eventsArea;
    private JSpinner endDateSpinner;
    // For timetable management
    private JComboBox<String> timetableDateBox;
    private JButton loadTimetableButton;
    private JButton deleteTimetableButton;


    public SchedulerGUI() {
        setTitle("Weekly Scheduler");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel workdayHeading = new JLabel("Workdays");
        workdayHeading.setFont(workdayHeading.getFont().deriveFont(Font.BOLD));
        inputPanel.add(workdayHeading, gbc);
        gbc.gridy++;
        inputPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        // End date picker
        gbc.gridx = 0; gbc.gridy++;
        inputPanel.add(new JLabel("Show timetable until (inclusive):"), gbc);
        gbc.gridx = 1;
        endDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd");
        endDateSpinner.setEditor(dateEditor);
        endDateSpinner.setValue(java.sql.Date.valueOf(java.time.LocalDate.now().plusDays(7)));
        inputPanel.add(endDateSpinner, gbc);
    gbc.gridy++;
    inputPanel.add(new JLabel("Workdays (1=Mon,7=Sun):"), gbc);
    gbc.gridy++;
    gbc.gridx = 1;
    workdaysField = new JTextField("0,1,2,3,4", 8);
    workdaysField.setPreferredSize(new Dimension(80, 22));
    inputPanel.add(workdaysField, gbc);

    gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
    JLabel workHeading = new JLabel("Work Time");
    workHeading.setFont(workHeading.getFont().deriveFont(Font.BOLD));
    inputPanel.add(workHeading, gbc);
    gbc.gridy++;
    inputPanel.add(new JSeparator(), gbc);
    gbc.gridwidth = 1;
    gbc.gridy++;
    inputPanel.add(new JLabel("Work start hour (0-23):"), gbc);
    gbc.gridx = 1;
    workHourSpinner = new JSpinner(new SpinnerNumberModel(8, 0, 23, 1));
    workHourSpinner.setPreferredSize(new Dimension(50, 22));
    inputPanel.add(workHourSpinner, gbc);

    gbc.gridx = 0; gbc.gridy++;
    inputPanel.add(new JLabel("Work start minute:"), gbc);
    gbc.gridx = 1;
    workMinuteBox = new JComboBox<>(new String[] {"00", "30"});
    workMinuteBox.setPreferredSize(new Dimension(50, 22));
    inputPanel.add(workMinuteBox, gbc);

    gbc.gridx = 0; gbc.gridy++;
    inputPanel.add(new JLabel("Work duration (hours):"), gbc);
    gbc.gridx = 1;
    workDurationSpinner = new JSpinner(new SpinnerNumberModel(8, 1, 24, 1));
    workDurationSpinner.setPreferredSize(new Dimension(50, 22));
    inputPanel.add(workDurationSpinner, gbc);

    gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
    JLabel sleepHeading = new JLabel("Sleep");
    sleepHeading.setFont(sleepHeading.getFont().deriveFont(Font.BOLD));
    inputPanel.add(sleepHeading, gbc);
    gbc.gridy++;
    inputPanel.add(new JSeparator(), gbc);
    gbc.gridwidth = 1;
    gbc.gridy++;
    inputPanel.add(new JLabel("Sleep duration (hours):"), gbc);
    gbc.gridx = 1;
    sleepDurationSpinner = new JSpinner(new SpinnerNumberModel(8, 1, 24, 1));
    sleepDurationSpinner.setPreferredSize(new Dimension(50, 22));
    inputPanel.add(sleepDurationSpinner, gbc);

    gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
    JLabel tasksHeading = new JLabel("Tasks");
    tasksHeading.setFont(tasksHeading.getFont().deriveFont(Font.BOLD));
    inputPanel.add(tasksHeading, gbc);
    gbc.gridy++;
    JLabel tasksExample = new JLabel("Tasks (name,duration,days) per line: e.g. Read,1,1,3,5 for Mon,Wed,Fri");
    inputPanel.add(tasksExample, gbc);
    gbc.gridy++;
    gbc.gridwidth = 2;
    tasksArea = new JTextArea(6, 32);
    tasksArea.setText("Read,1,1,3,5\nExercise,1,2,4,6");
    JScrollPane tasksScroll = new JScrollPane(tasksArea);
    tasksScroll.setPreferredSize(new Dimension(400, 100));
    inputPanel.add(tasksScroll, gbc);
    gbc.gridwidth = 1;

    gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
    JLabel eventsHeading = new JLabel("Events");
    eventsHeading.setFont(eventsHeading.getFont().deriveFont(Font.BOLD));
    inputPanel.add(eventsHeading, gbc);
    gbc.gridy++;
    JLabel eventsExample = new JLabel("Events (name,startDay,startHour,startMinute,durationHours) per line: e.g. Party,5,18,00,4");
    inputPanel.add(eventsExample, gbc);
    gbc.gridy++;
    gbc.gridwidth = 2;
    eventsArea = new JTextArea(6, 32);
    eventsArea.setText("Party,5,18,00,4");
    JScrollPane eventsScroll = new JScrollPane(eventsArea);
    eventsScroll.setPreferredSize(new Dimension(400, 100));
    inputPanel.add(eventsScroll, gbc);
    gbc.gridwidth = 1;

        // Output area
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setText("Welcome! Enter your schedule and click 'Generate Schedule'.");
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // Timetable management panel
        JPanel timetablePanel = new JPanel();
        timetablePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        timetablePanel.add(new JLabel("Old timetables (by date):"));
        timetableDateBox = new JComboBox<>();
        refreshTimetableDates();
        timetablePanel.add(timetableDateBox);
        loadTimetableButton = new JButton("Load");
        deleteTimetableButton = new JButton("Delete");
        timetablePanel.add(loadTimetableButton);
        timetablePanel.add(deleteTimetableButton);

        loadTimetableButton.addActionListener(e -> loadSelectedTimetable());
        deleteTimetableButton.addActionListener(e -> deleteSelectedTimetable());

        // Button
        generateButton = new JButton("Generate Schedule");
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateSchedule();
                refreshTimetableDates();
            }
        });

        JPanel topPanel = new JPanel(new BorderLayout());
        JScrollPane inputScrollPane = new JScrollPane(inputPanel);
        inputScrollPane.setPreferredSize(new Dimension(600, 350));
        topPanel.add(inputScrollPane, BorderLayout.CENTER);
        topPanel.add(generateButton, BorderLayout.SOUTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(topPanel, BorderLayout.NORTH);
        northPanel.add(timetablePanel, BorderLayout.SOUTH);
        mainPanel.add(northPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
    }
    // Refresh the timetable date dropdown
    private void refreshTimetableDates() {
        ActivityRepository repo = new ActivityRepository();
        List<String> dates = repo.getAllTimetableDates();
        timetableDateBox.removeAllItems();
        for (String d : dates) timetableDateBox.addItem(d);
    }

    // Load and display timetable for selected date
    private void loadSelectedTimetable() {
        String date = (String) timetableDateBox.getSelectedItem();
        if (date == null) {
            outputArea.setText("No timetable date selected.");
            return;
        }
        ActivityRepository repo = new ActivityRepository();
        List<ActivityRepository.TimetableEntry> entries = repo.getTimetableForDate(date);
        if (entries.isEmpty()) {
            outputArea.setText("No timetable found for " + date);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Timetable for ").append(date).append(":\n");
        // Group consecutive slots with the same activity
        int n = entries.size();
        int i = 0;
        while (i < n) {
            String activity = entries.get(i).activityName;
            int startSlot = entries.get(i).slot;
            int j = i + 1;
            while (j < n && entries.get(j).activityName.equals(activity) && entries.get(j).slot == entries.get(j-1).slot + 1) {
                j++;
            }
            int endSlot = entries.get(j-1).slot;
            int startHour = startSlot / 4;
            int startMin = (startSlot % 4) * 15;
            int endHour = (endSlot + 1) / 4;
            int endMin = ((endSlot + 1) % 4) * 15;
            sb.append(String.format("%02d:%02d-%02d:%02d %s\n", startHour, startMin, endHour, endMin, activity));
            i = j;
        }
        outputArea.setText(sb.toString());
    }

    // Delete timetable for selected date
    private void deleteSelectedTimetable() {
        String date = (String) timetableDateBox.getSelectedItem();
        if (date == null) {
            outputArea.setText("No timetable date selected.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete all timetable entries for " + date + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        ActivityRepository repo = new ActivityRepository();
        repo.deleteTimetableForDateRange(date, date);
        refreshTimetableDates();
        outputArea.setText("Timetable for " + date + " deleted.");
    }

    private void generateSchedule() {
        try {
            // Parse workdays
            String[] workdayTokens = workdaysField.getText().split(",");
            java.util.List<Integer> workdays = new java.util.ArrayList<>();
            for (String token : workdayTokens) {
                try {
                    int day = Integer.parseInt(token.trim());
                    if (day >= 1 && day <= 7) workdays.add(day - 1);
                } catch (NumberFormatException e) {}
            }
            int workHour = (Integer) workHourSpinner.getValue();
            int workMinute = Integer.parseInt((String) workMinuteBox.getSelectedItem());
            int workStartSlot = workHour * 4 + workMinute / 15;
            int workDurationSlots = ((Integer) workDurationSpinner.getValue()) * 4;
            int sleepDurationSlots = ((Integer) sleepDurationSpinner.getValue()) * 4;

            // Parse tasks (support recurring on multiple days)
            java.util.List<Task> tasks = new java.util.ArrayList<>();
            String[] lines = tasksArea.getText().split("\n");
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String name = parts[0].trim();
                    double dur = Double.parseDouble(parts[1].trim());
                    int slots = (int) Math.round(dur * 2);
                    if (parts.length > 2) {
                        for (int i = 2; i < parts.length; i++) {
                            try {
                                int day = Integer.parseInt(parts[i].trim());
                                if (day >= 1 && day <= 7) tasks.add(new Task(name, slots, day - 1));
                            } catch (NumberFormatException e) {}
                        }
                    } else {
                        // If no days specified, add as unscheduled (null day)
                        tasks.add(new Task(name, slots, null));
                    }
                }
            }

            // Parse events
            java.util.List<Event> events = new java.util.ArrayList<>();
            String[] eventLines = eventsArea.getText().split("\n");
            for (String line : eventLines) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String name = parts[0].trim();
                    java.time.LocalDate eventDate = java.time.LocalDate.parse(parts[1].trim()); // yyyy-MM-dd
                    int hour = Integer.parseInt(parts[2].trim());
                    int minute = Integer.parseInt(parts[3].trim());
                    int startSlot = hour * 4 + minute / 15;
                    double dur = Double.parseDouble(parts[4].trim());
                    int slots = (int) Math.round(dur * 4);
                    events.add(new Event(name, slots, eventDate, startSlot));
                }
            }

            // Get user-specified start and end date
            java.util.Date endDateVal = (java.util.Date) endDateSpinner.getValue();
            java.time.LocalDate endDate = new java.sql.Date(endDateVal.getTime()).toLocalDate();
            // Always start from the Monday of the week containing today
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.DayOfWeek dow = today.getDayOfWeek();
            int daysSinceMonday = (dow.getValue() - java.time.DayOfWeek.MONDAY.getValue() + 7) % 7;
            java.time.LocalDate thisMonday = today.minusDays(daysSinceMonday);

            SchedulerService scheduler = new SchedulerService();
            ScheduleResult result = scheduler.generateTimetable(
                workdays, workStartSlot, workDurationSlots, sleepDurationSlots, tasks, events, thisMonday, endDate
            );
            ScheduleViewer viewer = new ScheduleViewer();

            // Save activities to DB if not already present (simple: add all, ignore duplicates)
            ActivityRepository repo = new ActivityRepository();
            for (Task t : tasks) repo.addActivity(t);
            for (Event e : events) repo.addActivity(e);
            repo.addActivity(new FixedActivity("Work", workDurationSlots));
            repo.addActivity(new FixedActivity("Sleep", sleepDurationSlots));

            // Save timetable to DB
            java.util.Map<String, Integer> activityNameToId = repo.getActivityNameToIdMap();
            repo.saveTimetable(result.getTimetable(), thisMonday, endDate, activityNameToId);

            outputArea.setText(viewer.getScheduleString(result.getTimetable(), thisMonday, endDate));
        } catch (Exception ex) {
            outputArea.setText("Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SchedulerGUI gui = new SchedulerGUI();
            gui.setVisible(true);
        });
    }
}
