/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gui;

/**
 *
 * @author Miles
 */

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.border.*;

public class HomePage extends JFrame {
    
    private String uFullName = "";
    private List<String> timeLogs = new ArrayList<>();

    public HomePage(User currentUser) {
        setTitle("Home Page");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // === SIDEBAR ===
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridBagLayout());
        sidebar.setPreferredSize(new Dimension(220, getHeight()));
        sidebar.setBackground(new Color(45, 52, 71));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- User Info Panel ---
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        userInfoPanel.setOpaque(false);
        
        JLabel userIcon = new JLabel("👤", SwingConstants.CENTER);
        userIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 46));
        userIcon.setPreferredSize(new Dimension(80, 80));
        userIcon.setMinimumSize(new Dimension(80, 80));
        userIcon.setMaximumSize(new Dimension(80, 80));
        userIcon.setOpaque(true);
        userIcon.setBackground(Color.GRAY);
        userIcon.setForeground(Color.WHITE);
        userIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        userIcon.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        
        uFullName = currentUser.getuFirstName() + " " + currentUser.getuLastName();
        JLabel nameLabel = new JLabel(uFullName);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        userInfoPanel.add(Box.createVerticalStrut(30));
        userInfoPanel.add(userIcon);
        userInfoPanel.add(Box.createVerticalStrut(10));
        userInfoPanel.add(nameLabel);

        // --- Navigation Panel ---
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);
        // navPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        JButton homeBtn = styleButton("Home Page");
        JButton payrollBtn = styleButton("Payroll Management");
        JButton employeeBtn = styleButton("Employee Management");

        payrollBtn.addActionListener(e -> new PayrollManagement());
        employeeBtn.addActionListener(e -> new EmployeeManagement());

        navPanel.add(Box.createVerticalStrut(10));
        navPanel.add(homeBtn);
        navPanel.add(payrollBtn);
        navPanel.add(employeeBtn);

        // --- Account Panel ---
        JPanel accountPanel = new JPanel();
        accountPanel.setLayout(new BoxLayout(accountPanel, BoxLayout.Y_AXIS));
        accountPanel.setOpaque(false);
        // accountPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        JLabel accountLabel = new JLabel("Account");
        accountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        accountLabel.setBorder(new EmptyBorder(0, 10, 10, 0));
        accountLabel.setForeground(Color.WHITE);
        accountPanel.add(accountLabel);

        JButton timeInBtn = styleButton("Time In");
        JButton timeOutBtn = styleButton("Time Out");
        JButton logoutBtn = styleButton("Logout");
        
        JLabel dateLabel = new JLabel();
        dateLabel.setForeground(Color.WHITE);
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
        
        JLabel timeLabel = new JLabel();
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        timeLabel.setBorder(new EmptyBorder(10, 10, 0, 0));

        updateClock(timeLabel, dateLabel);

        timeInBtn.addActionListener(e -> {
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            timeLogs.add("Time In: " + time);
            JOptionPane.showMessageDialog(this, "Time In recorded.");
        });

        timeOutBtn.addActionListener(e -> {
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            timeLogs.add("Time Out: " + time);
            JOptionPane.showMessageDialog(this, "Time Out recorded.");
        });

        logoutBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout Confirmation", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        
        accountPanel.add(timeInBtn);
        accountPanel.add(timeOutBtn);
        accountPanel.add(logoutBtn);
        accountPanel.add(Box.createVerticalStrut(10));
        accountPanel.add(dateLabel);
        accountPanel.add(timeLabel);
        accountPanel.add(Box.createVerticalStrut(10));

        // === Add panels to sidebar ===
        sidebar.add(userInfoPanel, gbc);
        gbc.gridy++;
        sidebar.add(navPanel, gbc);
        gbc.gridy++;
        gbc.weighty = 1;
        sidebar.add(Box.createVerticalGlue(), gbc);
        gbc.gridy++;
        gbc.weighty = 0;
        sidebar.add(accountPanel, gbc);

        add(sidebar, BorderLayout.WEST);

        // === MAIN PANEL ===
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        JPanel infoPanel = new JPanel(new BorderLayout(10, 10));
        infoPanel.setBackground(Color.WHITE);

        JLabel photoLabel = new JLabel("👤", SwingConstants.CENTER);
        photoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 92));
        photoLabel.setPreferredSize(new Dimension(150, 150));
        photoLabel.setMinimumSize(new Dimension(150, 150));
        photoLabel.setMaximumSize(new Dimension(150, 150));
        photoLabel.setOpaque(true);
        photoLabel.setBackground(Color.GRAY);
        photoLabel.setForeground(Color.WHITE);
        photoLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        infoPanel.add(photoLabel, BorderLayout.WEST);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new GridLayout(5, 1, 5, 5));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.add(new JLabel("Employee ID: " + currentUser.getuEmpId()));
        detailsPanel.add(new JLabel("Name: " + currentUser.getuFirstName() + " " + currentUser.getuLastName()));
        detailsPanel.add(new JLabel("Date of Birth: " + currentUser.getuDob()));
        detailsPanel.add(new JLabel("Position: " + currentUser.getuPosition()));
        detailsPanel.add(new JLabel("Status: " + currentUser.getuStatus()));

        infoPanel.add(detailsPanel, BorderLayout.CENTER);

        JButton attendanceButton = new JButton("\uD83D\uDCC5 View Attendance");
        attendanceButton.setFocusPainted(false);
        attendanceButton.setBackground(new Color(70, 130, 180));
        attendanceButton.setForeground(Color.WHITE);
        attendanceButton.setPreferredSize(new Dimension(150, 40));

        attendanceButton.addActionListener(e -> {
            if (timeLogs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No attendance records found.");
            } else {
                StringBuilder records = new StringBuilder("Attendance Records:\n\n");
                for (String log : timeLogs) {
                    records.append(log).append("\n");
                }
                JTextArea area = new JTextArea(records.toString());
                area.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(area);
                scrollPane.setPreferredSize(new Dimension(400, 300));
                JOptionPane.showMessageDialog(this, scrollPane, "Attendance Log", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        mainPanel.add(infoPanel, BorderLayout.NORTH);
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(attendanceButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);
    }

    private JButton styleButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 100), 1),
            new EmptyBorder(5, 15, 5, 5)
        ));
        return button;
    }

    private void updateClock(JLabel timeLabel, JLabel dateLabel) {
        Timer timer = new Timer(1000, e -> {
            String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
            String date = new SimpleDateFormat("EEEE, MMMM, dd, yyyy").format(new Date());
            timeLabel.setText("Time: " + time);
            dateLabel.setText("Date: " + date);
            
        });
        timer.start();
    }
} 
