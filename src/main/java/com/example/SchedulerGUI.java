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
    gbc.gridy++;
    inputPanel.add(new JLabel("Workdays (1=Mon,7=Sun):"), gbc);
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

        // Button
        generateButton = new JButton("Generate Schedule");
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateSchedule();
            }
        });

        JPanel topPanel = new JPanel(new BorderLayout());
    JScrollPane inputScrollPane = new JScrollPane(inputPanel);
    inputScrollPane.setPreferredSize(new Dimension(600, 350));
    topPanel.add(inputScrollPane, BorderLayout.CENTER);
    topPanel.add(generateButton, BorderLayout.SOUTH);

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(topPanel, BorderLayout.NORTH);
    mainPanel.add(scrollPane, BorderLayout.CENTER);

    add(mainPanel);
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
            int workStartSlot = workHour * 2 + (workMinute == 30 ? 1 : 0);
            int workDurationSlots = ((Integer) workDurationSpinner.getValue()) * 2;
            int sleepDurationSlots = ((Integer) sleepDurationSpinner.getValue()) * 2;

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
                    int day = Integer.parseInt(parts[1].trim()) - 1; // 1=Mon,7=Sun
                    int hour = Integer.parseInt(parts[2].trim());
                    int minute = Integer.parseInt(parts[3].trim());
                    int startSlot = hour * 2 + (minute == 30 ? 1 : 0);
                    double dur = Double.parseDouble(parts[4].trim());
                    int slots = (int) Math.round(dur * 2);
                    events.add(new Event(name, slots, day, startSlot));
                }
            }

            SchedulerService scheduler = new SchedulerService();
            ScheduleResult result = scheduler.generateTimetable(workdays, workStartSlot, workDurationSlots, sleepDurationSlots, tasks, events);
            ScheduleViewer viewer = new ScheduleViewer();
            outputArea.setText(viewer.getScheduleString(result.timetable()));
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
