/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.gui;

import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.ParseException;

/**
 *
 * @author AtlasPrimE
 */
public class HomePage extends javax.swing.JFrame {

    // To keep time logs for attendance
    private User currentUser; // Store the user info
    private String clockInDate = null;
    private String clockInTime = null;
    
    //View Attemdance Panel
    private JPanel dateRangePanel;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private com.toedter.calendar.JDateChooser jDateChooser2;
    
    // Constructor: receive User info
    public HomePage(User user) {
        this.currentUser = user;
        initComponents();
        
        setResizable(false);// Removes maximize and resizing
        setLocationRelativeTo(null); // This centers the window on the screen
        pack();// Fit frame to preferred size
        // Set the company logo on the left
        SwingUtilities.invokeLater(() -> setLogoOnLabel(jLabel1, "/com/gui/images/LoginIcons/RevisedLogo.png"));
        SwingUtilities.invokeLater(() -> setProfileImage(jLabel5, currentUser.getuEmpId()));
        setUserInfo();
        startClock();
        
        // Initialize Panel & DateChoosers
        dateRangePanel = new JPanel();
        dateRangePanel.setLayout(new GridLayout(3, 2)); // 3 rows, 2 columns

        dateRangePanel.add(new JLabel("From:"));
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        dateRangePanel.add(jDateChooser1);

        dateRangePanel.add(new JLabel("To:"));
        jDateChooser2 = new com.toedter.calendar.JDateChooser();
        dateRangePanel.add(jDateChooser2);
        
    }
    
    // Default constructor for GUI builder compatibility (not used in production)
    public HomePage() {
        initComponents();
        startClock();
    }

    HomePage(String user) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    // Assign user info to labels
    private void setUserInfo() {
        if (currentUser != null) {
            jLabel2.setText(currentUser.getuFirstName() + " " + currentUser.getuLastName()); // Sidebar name
            jLabel6.setText("Employee ID: " + currentUser.getuEmpId());
            jLabel7.setText("Name: " + currentUser.getuFirstName() + " " + currentUser.getuLastName());
            jLabel8.setText("Date of Birth: " + currentUser.getuDob());
            jLabel9.setText("Position: " + currentUser.getuPosition());
            jLabel10.setText("Status: " + currentUser.getuStatus());
            // Optionally set profile image:
            // jLabel1.setIcon(new ImageIcon(getClass().getResource("/com/gui/profile.png")));
            // jLabel5.setIcon(...) for main photo
        }
    }

    // Start a timer to update date and time labels
    private String getCurrentManilaDate() {
    SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy");
    dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));
    return dateFormat.format(new Date());
}
    private String getCurrentManilaTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        timeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));
        return timeFormat.format(new Date());
}
    private void startClock() {
        Timer timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                Date now = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
                SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm:ss a");
                dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));
                timeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));
                jLabel3.setText("Date: " + dateFormat.format(now));
                jLabel4.setText("Time: " + timeFormat.format(now));
            }
        });
        timer.start();
    }
    
        private void setProfileImage(JLabel label, String empId) {
        String[] exts = {".png", ".jpg", ".jpeg"};
        boolean found = false;
        for (String ext : exts) {
            String path = "/com/gui/images/EmployeeIDs/" + empId + ext;
            java.net.URL imgURL = getClass().getResource(path);
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);
                int width = label.getWidth() > 0 ? label.getWidth() : label.getPreferredSize().width;
                int height = label.getHeight() > 0 ? label.getHeight() : label.getPreferredSize().height;
                Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(img));
                label.setText("");
                found = true;
                break;
            }
        }
        if (!found) {
            // Use NULL.png as the fallback image
            java.net.URL defaultImg = getClass().getResource("/com/gui/images/EmployeeIDs/NULL.png");
            if (defaultImg != null) {
                ImageIcon icon = new ImageIcon(defaultImg);
                int width = label.getWidth() > 0 ? label.getWidth() : label.getPreferredSize().width;
                int height = label.getHeight() > 0 ? label.getHeight() : label.getPreferredSize().height;
                Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(img));
                label.setText("");
            } else {
                // If even NULL.png is missing, fallback to text
                label.setText("No Photo");
                label.setIcon(null);
            }
        }
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
}
        // Helper method for setLogoOnLabel
        private void setLogoOnLabel(JLabel label, String resourcePath) {
        java.net.URL logoURL = getClass().getResource(resourcePath);
        if (logoURL != null) {
            ImageIcon icon = new ImageIcon(logoURL);
            // Always use 200x200 for scaling
            int width = 200;
            int height = 200;
            Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(img));
            label.setText("");
            label.setPreferredSize(new java.awt.Dimension(width, height)); // Optional: Forces the label to reserve this space
        } else {
            label.setText("Logo not found");
            label.setIcon(null);
        }
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        }
        
        // Only reads EmployeeTimeEntries.csv and writes a new file in user's chosen folder
        private void generateAttendanceReport(File directory, Date from, Date to) {
            String empId = currentUser.getuEmpId();
            SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy");
            SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy.MM.dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");

            String fromStr = (from != null) ? fileDateFormat.format(from) : "start";
            String toStr = (to != null) ? fileDateFormat.format(to) : "end";

            // Filename format: EmpID_yyyy.mm.dd_yyyy.mm.dd.csv
            String fileName = empId + "_" + fromStr + "_" + toStr + ".csv";
            File csvFile = new File(directory, fileName);

            try (BufferedReader br = new BufferedReader(new FileReader("src/com/payroll/EmployeeTimeEntries.csv"));
                 PrintWriter out = new PrintWriter(new FileWriter(csvFile))) {

                // Write header
                out.println("Date,Clock In,Clock Out,Duration");

                String line;
                boolean hasData = false;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 5 && parts[0].equals(empId)) {
                        Date entryDate = sdf.parse(parts[1]);
                        boolean inRange = (from == null || !entryDate.before(from)) &&
                                          (to == null || !entryDate.after(to));
                        if (inRange) {
                            String clockIn = parts[2].trim();
                            String clockOut = parts[3].trim();
                            String durationStr = "";
                            try {
                                Date clockInDate = timeFormat.parse(clockIn);
                                Date clockOutDate = timeFormat.parse(clockOut);
                                long durationMs = clockOutDate.getTime() - clockInDate.getTime();
                                if (durationMs < 0) durationMs += 24 * 60 * 60 * 1000;
                                long diffMinutes = durationMs / (60 * 1000);
                                long hours = diffMinutes / 60;
                                long minutes = diffMinutes % 60;
                                durationStr = hours + "h " + minutes + "m";
                            } catch (Exception ex) {
                                durationStr = "Error";
                            }
                            out.println(parts[1] + "," + clockIn + "," + clockOut + "," + durationStr);
                            hasData = true;
                        }
                    }
                }
                if (hasData) {
                    JOptionPane.showMessageDialog(this, "Report saved to:\n" + csvFile.getAbsolutePath());
                } else {
                    JOptionPane.showMessageDialog(this, "No records found in the selected date range.");
                    csvFile.delete(); // Remove empty file
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error generating report: " + e.getMessage());
            }
        }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        javax.swing.JButton jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMaximumSize(new java.awt.Dimension(700, 500));
        setMinimumSize(new java.awt.Dimension(700, 500));
        setPreferredSize(new java.awt.Dimension(700, 500));
        setSize(new java.awt.Dimension(700, 500));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.lightGray, java.awt.Color.darkGray));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel1.setMaximumSize(new java.awt.Dimension(150, 150));
        jLabel1.setMinimumSize(new java.awt.Dimension(150, 150));
        jLabel1.setPreferredSize(new java.awt.Dimension(150, 150));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("jLabel2");

        jButton2.setText("Payroll Management");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton1.setText("Home Page");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton3.setText("Employee Management");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel3.setText("jLabel3");

        jLabel4.setText("jLabel4");

        jButton4.setText("Clock In");
        jButton4.setMaximumSize(new java.awt.Dimension(110, 30));
        jButton4.setMinimumSize(new java.awt.Dimension(110, 30));
        jButton4.setPreferredSize(new java.awt.Dimension(110, 30));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setText("Clock Out");
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton5.setMaximumSize(new java.awt.Dimension(110, 30));
        jButton5.setMinimumSize(new java.awt.Dimension(110, 30));
        jButton5.setPreferredSize(new java.awt.Dimension(110, 30));
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(9, 9, 9))))
                .addContainerGap(17, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addContainerGap(65, Short.MAX_VALUE))
        );

        jLabel5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel5.setMaximumSize(new java.awt.Dimension(128, 128));
        jLabel5.setMinimumSize(new java.awt.Dimension(128, 128));
        jLabel5.setPreferredSize(new java.awt.Dimension(128, 128));

        jLabel6.setText("jLabel6");

        jLabel7.setText("jLabel7");

        jLabel8.setText("jLabel8");

        jLabel9.setText("jLabel9");

        jLabel10.setText("jLabel10");

        jButton7.setText("View Attendance");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton6.setText("Log out");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(102, 102, 102)
                                .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(104, 104, 104)
                        .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton6)
                        .addGap(0, 0, 0)
                        .addComponent(jLabel6)
                        .addGap(6, 6, 6)
                        .addComponent(jLabel7)
                        .addGap(6, 6, 6)
                        .addComponent(jLabel8)
                        .addGap(6, 6, 6)
                        .addComponent(jLabel9)
                        .addGap(6, 6, 6)
                        .addComponent(jLabel10)))
                .addGap(233, 233, 233)
                .addComponent(jButton7))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // Logout Button - returns to LoginForm
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to log out?",
            "Logout Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            // Close HomePage and open LoginForm
            new com.gui.LoginForm().setVisible(true);
            this.dispose();
        }
        // If NO, do nothing (just return)
    }//GEN-LAST:event_jButton6ActionPerformed
    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // Clock In Button
        clockInTime = getCurrentManilaTime();
        clockInDate = getCurrentManilaDate();
        JOptionPane.showMessageDialog(this, "Time In recorded: " + clockInTime);
    }//GEN-LAST:event_jButton4ActionPerformed
    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // Clock Out Button
        if (clockInTime == null) {
        JOptionPane.showMessageDialog(this, "You need to clock in first!");
        return;
    }
        String clockOutTime = getCurrentManilaTime();
        // Save attendance record to CSV in the format: empID,Date,ClockinTime,ClockoutTime,EmployeeName
            try (FileWriter fw = new FileWriter("src/com/payroll/EmployeeTimeEntries.csv", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            // Write: empId, date, clockIn, clockOut, firstName lastName
            out.println(currentUser.getuEmpId() + "," +
                        clockInDate + "," +
                        clockInTime + "," +
                        clockOutTime + "," +
                        currentUser.getuFirstName() + " " + currentUser.getuLastName());

            // Clear clock in
            clockInTime = null;
            clockInDate = null;
            JOptionPane.showMessageDialog(this, "Time Out recorded and attendance saved!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving attendance: " + e.getMessage());
    }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // Payroll Management Button
        new PayrollManagement(currentUser).setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // Employee Management Button
        new EmployeeManagement().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        int result = JOptionPane.showConfirmDialog(
            this,
            dateRangePanel,
            "Please select date range.",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            // User cancelled or closed dialog
            return;
        }

        Date from = jDateChooser1.getDate();
        Date to = jDateChooser2.getDate();
        SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        // Date validation: Make sure 'from' is not after 'to'
        if (from != null && to != null && from.after(to)) {
            JOptionPane.showMessageDialog(this, "Invalid date range!\n'From' date must not be after 'To' date.\nPlease try again.", "Date Range Error", JOptionPane.ERROR_MESSAGE);
            // Recursively call the handler to prompt again, do NOT exit or reset to main
            jButton7ActionPerformed(evt);
            return;
        }
        
        StringBuilder records = new StringBuilder("Date\tClock In\tClock Out\tDuration\n\n");
        boolean found = false;
        long totalMinutes = 0;

        try (BufferedReader br = new BufferedReader(new FileReader("src/com/payroll/EmployeeTimeEntries.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                // Format: empId, date, clockIn, clockOut, employeeName
                if (parts.length >= 5 && parts[0].equals(currentUser.getuEmpId())) {
                    Date entryDate = sdf.parse(parts[1]);
                    boolean inRange = (from == null || !entryDate.before(from)) &&
                                      (to == null || !entryDate.after(to));
                    if (inRange) {
                        String clockIn = parts[2].trim();
                        String clockOut = parts[3].trim();

                        try {
                            Date clockInDate = timeFormat.parse(clockIn);
                            Date clockOutDate = timeFormat.parse(clockOut);
                            long durationMs = clockOutDate.getTime() - clockInDate.getTime();
                            if (durationMs < 0) durationMs += 24 * 60 * 60 * 1000; // Overnight fix
                            long diffMinutes = durationMs / (60 * 1000);
                            totalMinutes += diffMinutes;
                            long hours = diffMinutes / 60;
                            long minutes = diffMinutes % 60;
                            String durationStr = hours + "h " + minutes + "m";
                            records.append(parts[1]).append("\t")
                                   .append(clockIn).append("\t")
                                   .append(clockOut).append("\t")
                                   .append(durationStr).append("\n");
                            found = true;
                        } catch (Exception ex) {
                            // If parsing fails, just skip duration for that day
                            records.append(parts[1]).append("\t")
                                   .append(clockIn).append("\t")
                                   .append(clockOut).append("\t")
                                   .append("Error\n");
                        }
                    }
                }
            }
        } catch (Exception e) {
            records.append("Error reading attendance: ").append(e.getMessage());
        }

        // Add total worked hours at the bottom
        long totalHours = totalMinutes / 60;
        long totalMins = totalMinutes % 60;
        records.append("\nTotal Worked Hours: ").append(totalHours).append("h ").append(totalMins).append("m\n");

        if (!found) {
            JOptionPane.showMessageDialog(this, "No attendance records found.");
        } else {
            JTextArea area = new JTextArea(records.toString());
            area.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(area);
            scrollPane.setPreferredSize(new java.awt.Dimension(500, 350));
            JOptionPane.showMessageDialog(this, scrollPane, "Attendance Log", JOptionPane.INFORMATION_MESSAGE);
        }

        // Prompt for export
        int exportOption = JOptionPane.showConfirmDialog(
            this,
            "Would you like to export the result?",
            "Export Attendance Report",
            JOptionPane.YES_NO_OPTION
        );

        if (exportOption == JOptionPane.YES_OPTION) {
            // Let the user choose where to save
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle("Select Folder to Save Report");

            int chooserResult = fileChooser.showSaveDialog(this);
            if (chooserResult == JFileChooser.APPROVE_OPTION) {
                File selectedDir = fileChooser.getSelectedFile();
                // Call your export method, passing the chosen folder and dates
                generateAttendanceReport(selectedDir, from, to);
            }
        } else {
            // Do nothing, just return to HomePage
        }
    }//GEN-LAST:event_jButton7ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(HomePage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(HomePage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(HomePage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(HomePage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new HomePage().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables

    public static class LoginForm extends JFrame {

        // ────── CUSTOM FIELDS ─────────────────────────────────────────────────────
        /** CSV‐backed username/password store */
        private Map<String, String> credentials = new HashMap<>();
        // ────────────────────────────────────────────────────────────────────────────

        /** Creates new form LoginUI */
        public LoginForm() {
            initComponents();
            // generated code – sets up jTextField1, jPasswordField1, etc.
            // placeholder & focus logic goes here
            jTextField1.setForeground(Color.GRAY);
            // … rest of that FocusListener code …
            jPasswordField1.setForeground(Color.GRAY);
            // … rest of the password FocusListener code …
            // 1) Load credentials from root‐level CSV
            loadCredentialsFromCSV("UserLogIns.csv");
            // 2) Hide the spinner until login is attempted
            jProgressBar1.setVisible(false);
            // 3) Single‐Enter to click Login
            getRootPane().setDefaultButton(jButton1);
            // 4) Tooltips for format hints
            jTextField1.setToolTipText("Format: EmployeeNumber\n(e.g. 000000)");
            jPasswordField1.setToolTipText("Format: NameInitials+Emp#+BirthYear!\n(e.g. JR100351861!)");
            // in LoginUI() constructor, after initComponents():
            // Make the field show a gray "USERNAME" placeholder
            jTextField1.setForeground(Color.GRAY);
            jTextField1.setText("USERNAME");
            jTextField1.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (jTextField1.getText().equals("USERNAME")) {
                        jTextField1.setText("");
                        jTextField1.setForeground(Color.BLACK);
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (jTextField1.getText().isEmpty()) {
                        jTextField1.setForeground(Color.GRAY);
                        jTextField1.setText("USERNAME");
                    }
                }
            });
            // Do the same for password, but manage echo char so placeholder is visible:
            jPasswordField1.setForeground(Color.GRAY);
            jPasswordField1.setEchoChar((char) 0); // show text
            jPasswordField1.setText("PASSWORD");
            jPasswordField1.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    String pwd = new String(jPasswordField1.getPassword());
                    if (pwd.equals("PASSWORD")) {
                        jPasswordField1.setText("");
                        jPasswordField1.setForeground(Color.BLACK);
                        jPasswordField1.setEchoChar('\u2022'); // or '*'
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    String pwd = new String(jPasswordField1.getPassword());
                    if (pwd.isEmpty()) {
                        jPasswordField1.setForeground(Color.GRAY);
                        jPasswordField1.setEchoChar((char) 0); // show placeholder
                        jPasswordField1.setText("PASSWORD");
                    }
                }
            });
        }

        /**
         * This method is called from within the constructor to initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is always
         * regenerated by the Form Editor.
         */
        @SuppressWarnings(value = "unchecked")
        private void initComponents() {
            jScrollPane1 = new JScrollPane();
            jTextArea1 = new JTextArea();
            jLabel1 = new JLabel();
            jLabel2 = new JLabel();
            jLabel4 = new JLabel();
            jLabel5 = new JLabel();
            jPasswordField1 = new JPasswordField();
            jTextField1 = new JTextField();
            jButton1 = new JButton();
            jProgressBar1 = new JProgressBar();
            jLabel3 = new JLabel();
            jLabel6 = new JLabel();
            jLabel7 = new JLabel();
            jLabel8 = new JLabel();
            jTextArea1.setColumns(20);
            jTextArea1.setRows(5);
            jScrollPane1.setViewportView(jTextArea1);
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setResizable(false);
            jLabel1.setIcon(new ImageIcon(getClass().getResource("/com/gui/images/LogInIcons/Title.png"))); // NOI18N
            jLabel1.setText("jLabel1");
            jLabel2.setIcon(new ImageIcon(getClass().getResource("/com/gui/images/LogInIcons/logo.png"))); // NOI18N
            jLabel2.setLabelFor(jLabel2);
            jLabel2.setText("jLabel2");
            jLabel4.setHorizontalAlignment(SwingConstants.LEFT);
            jLabel4.setIcon(new ImageIcon(getClass().getResource("/com/LoginUI/images/username.png"))); // NOI18N
            jLabel4.setLabelFor(jTextField1);
            jLabel4.setToolTipText("Format: FLastNameEmployeeNumber (ex. JRizal00001) ");
            jLabel5.setIcon(new ImageIcon(getClass().getResource("/com/LoginUI/images/password.png"))); // NOI18N
            jLabel5.setLabelFor(jPasswordField1);
            jLabel5.setToolTipText("Format: FLNumberBirthYear! (ex. JR100351861!) ");
            jPasswordField1.setHorizontalAlignment(JTextField.CENTER);
            jPasswordField1.setToolTipText("");
            jTextField1.setHorizontalAlignment(JTextField.CENTER);
            jTextField1.setToolTipText("");
            jButton1.setIcon(new ImageIcon(getClass().getResource("/com/LoginUI/images/login.png"))); // NOI18N
            jButton1.setText("Login");
            jButton1.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    jButton1ActionPerformed(evt);
                }
            });
            jLabel3.setIcon(new ImageIcon(getClass().getResource("/com/gui/images/password.png"))); // NOI18N
            jLabel3.setToolTipText("Format: 5 DigitEmployee # (ex. 00000))");
            jLabel6.setIcon(new ImageIcon(getClass().getResource("/com/gui/images/login.png"))); // NOI18N
            jLabel6.setToolTipText("Format: firstnameinitial+LastName (ex. fLast)");
            jLabel7.setHorizontalAlignment(SwingConstants.CENTER);
            jLabel7.setIcon(new ImageIcon(getClass().getResource("/com/gui/images/LogInIcons/login.png"))); // NOI18N
            jLabel7.setLabelFor(jLabel7);
            jLabel8.setHorizontalAlignment(SwingConstants.CENTER);
            jLabel8.setIcon(new ImageIcon(getClass().getResource("/com/gui/images/LogInIcons/password.png"))); // NOI18N
            jLabel8.setLabelFor(jLabel8);
            GroupLayout layout = new GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(65, 65, 65).addComponent(jLabel4).addGap(204, 204, 204).addComponent(jLabel5).addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(jProgressBar1, GroupLayout.PREFERRED_SIZE, 180, GroupLayout.PREFERRED_SIZE).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false).addComponent(jLabel6, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(jLabel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addGap(18, 18, 18).addComponent(jLabel8)).addComponent(jLabel7)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, 180, GroupLayout.PREFERRED_SIZE).addComponent(jPasswordField1, GroupLayout.PREFERRED_SIZE, 180, GroupLayout.PREFERRED_SIZE)))).addGap(90, 90, 90)).addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addComponent(jButton1).addGap(145, 145, 145)).addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, 194, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 190, GroupLayout.PREFERRED_SIZE).addGap(14, 14, 14)))));
            layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(jLabel4).addGap(50, 50, 50).addComponent(jLabel5).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(jLabel2).addComponent(jLabel1)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGroup(layout.createSequentialGroup().addComponent(jLabel6).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabel7))).addGap(18, 18, 18).addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addComponent(jLabel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(jPasswordField1).addComponent(jLabel8))).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(jButton1).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(jProgressBar1, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE).addGap(108, 108, 108)));
            pack();
        } // </editor-fold>

        /** Login button handler */
        private void jButton1ActionPerformed(ActionEvent evt) {
            // TODO add your handling code here:
            // show spinner
            jProgressBar1.setVisible(true);
            jProgressBar1.setIndeterminate(true);
            String user = jTextField1.getText().trim();
            String pass = new String(jPasswordField1.getPassword());
            if (credentials.containsKey(user) && credentials.get(user).equals(pass)) {
                // show personalized welcome
                JOptionPane.showMessageDialog(this, "Login successful.\nWelcome, " + user + "!", "Welcome", JOptionPane.INFORMATION_MESSAGE);
                // pass the user along to the new frame
                new HomePage(user).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
                jPasswordField1.setText("");
            }
            // hide spinner
            jProgressBar1.setIndeterminate(false);
            jProgressBar1.setVisible(false);
        }

        // ────── CUSTOM METHOD ──────────────────────────────────────────────────────
        /** Reads username/password pairs from a CSV at the project root */
        private void loadCredentialsFromCSV(String path) {
            credentials.clear();
            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                // Skip the header row
                String line = br.readLine();
                if (line != null && line.startsWith("\ufeff")) {
                    // strip BOM if present
                    line = line.substring(1);
                }
                // Now read each data row
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length < 2) {
                        continue; // malformed row
                    }
                    String user = parts[0].trim();
                    if (user.equalsIgnoreCase("username")) {
                        continue; // safety skip
                    }
                    String pass = parts[1].trim();
                    credentials.put(user, pass);
                }
                System.out.println("Loaded credentials: " + credentials.keySet());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error loading credentials:\n" + ex.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        // ────────────────────────────────────────────────────────────────────────────
        /**
         * @param args the command line arguments
         */
        public static void main(String[] args) {
            /* Set the Nimbus look and feel */
            //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
            /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
             * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
             */
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(LoginForm.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(LoginForm.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(LoginForm.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedLookAndFeelException ex) {
                Logger.getLogger(LoginForm.class.getName()).log(Level.SEVERE, null, ex);
            }
            //</editor-fold>
            //</editor-fold>
            /* Create and display the form */
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new LoginForm().setVisible(true);
                }
            });
        }
        // Variables declaration - do not modify
        private JButton jButton1;
        private JLabel jLabel1;
        private JLabel jLabel2;
        private JLabel jLabel3;
        private JLabel jLabel4;
        private JLabel jLabel5;
        private JLabel jLabel6;
        private JLabel jLabel7;
        private JLabel jLabel8;
        private JPasswordField jPasswordField1;
        private JProgressBar jProgressBar1;
        private JScrollPane jScrollPane1;
        private JTextArea jTextArea1;
        private JTextField jTextField1;
        // End of variables declaration
    }
}
