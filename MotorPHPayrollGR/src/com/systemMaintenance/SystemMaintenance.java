package com.systemMaintenance;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class SystemMaintenance extends JFrame {

    private JLabel uptimeLabel, dateTimeLabel, userCountLabel;
    private LocalDateTime launchTime;

    public SystemMaintenance() {
        setTitle("System Maintenance - MotorPH");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Logo (center top)
        JLabel logoLabel = new JLabel(new ImageIcon("src/com/gui/images/motorph.png"));
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel logoPanel = new JPanel();
        logoPanel.add(logoLabel);

        // Center panel with buttons
        JPanel centerPanel = new JPanel(new GridLayout(3, 2, 15, 15));
        JButton viewChangesBtn = new JButton("View Changes");
        JButton createBackupBtn = new JButton("Create Backup");
        JButton restoreBtn = new JButton("Restore");
        JButton userPermissionsBtn = new JButton("User Permissions");

        // Bottom panel for info
        JPanel bottomPanel = new JPanel(new GridLayout(3, 1));
        dateTimeLabel = new JLabel("Local Time: ");
        uptimeLabel = new JLabel("System Uptime: ");
        userCountLabel = new JLabel("User Count: N/A");

        bottomPanel.add(dateTimeLabel);
        bottomPanel.add(uptimeLabel);
        bottomPanel.add(userCountLabel);

        // Add buttons to center panel
        centerPanel.add(viewChangesBtn);
        centerPanel.add(createBackupBtn);
        centerPanel.add(restoreBtn);
        centerPanel.add(userPermissionsBtn);

        // Set system start time
        launchTime = LocalDateTime.now();
        updateDateTimeAndUptime();

        // Timer for uptime and time
        Timer timer = new Timer(1000, e -> updateDateTimeAndUptime());
        timer.start();

        // Button actions
        viewChangesBtn.addActionListener(e -> showChangeLog());
        createBackupBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Triggering backup process..."));
        restoreBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Triggering restore process..."));
        userPermissionsBtn.addActionListener(e -> showUserPermissions("src/com/csv/EmployeeData.csv"));

        // Layout setup
        add(logoPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void updateDateTimeAndUptime() {
        LocalDateTime now = LocalDateTime.now();
        dateTimeLabel.setText("Local Time: " + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        Duration uptime = Duration.between(launchTime, now);
        uptimeLabel.setText("System Uptime: " + formatDuration(uptime));
    }

    private String formatDuration(Duration d) {
        long hours = d.toHours();
        long minutes = d.toMinutes() % 60;
        long seconds = d.getSeconds() % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void showChangeLog() {
        File changelog = new File("src/com/logs/changelog.txt");
        if (!changelog.exists()) {
            JOptionPane.showMessageDialog(this, "No changelog found.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(changelog))) {
            JTextArea textArea = new JTextArea();
            String line;
            while ((line = br.readLine()) != null) textArea.append(line + "\n");

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600, 300));
            JOptionPane.showMessageDialog(this, scrollPane, "System Change Log", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading changelog: " + e.getMessage());
        }
    }

    private void showUserPermissions(String path) {
        int active = 0, inactive = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String header = reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 7) {
                    String status = data[6].trim(); // assuming 7th column is Active/Inactive
                    if (status.equalsIgnoreCase("Active")) active++;
                    else inactive++;
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading user data: " + e.getMessage());
            return;
        }
        userCountLabel.setText("User Count - Active: " + active + " | Inactive: " + inactive);
    }
}
