/* Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.gui.Home;

import com.gui.EmpManage.AttendanceManagement;
import com.gui.EmpManage.EmployeeManagement;
import com.gui.Payroll.PayrollManagement;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.awt.Color;
import java.awt.EventQueue;
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
import java.awt.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.ImageIcon;
import com.gui.EmpManage.editEmployee;
import com.systemMaintenance.SystemMaintenance;
import javax.swing.JFrame;
import com.gui.Payroll.PayrollDisplay;

/**
 *
 * @author ongoj
 */
public class HomePage extends javax.swing.JFrame {

    // To keep time logs for attendance
    private User currentUser; // Store the user info
    private String clockInDate = null;
    private String clockInTime = null;

    //jPanel2
    private javax.swing.JLabel jLabelGovHeader;
    private javax.swing.JLabel jLabelSSS;
    private javax.swing.JLabel jLabelPhilhealth;
    private javax.swing.JLabel jLabelTIN;
    private javax.swing.JLabel jLabelPagibig;

    private javax.swing.JLabel jLabelPayHeader;
    private javax.swing.JLabel jLabelBasicSalary;
    private javax.swing.JLabel jLabelGrossSemi;

    private javax.swing.JLabel jLabelAllowHeader;
    private javax.swing.JLabel jLabelHourlyRate;
    private javax.swing.JLabel jLabelRiceSubsidy;
    private javax.swing.JLabel jLabelPhoneAllowance;
    private javax.swing.JLabel jLabelClothingAllowance;

    private PayrollDisplay payrollDisplay;

    // Constructor: receive User info
    public HomePage(User user) {
        this.currentUser = user;
        initComponents();
        // ── role-based button controls ───────────────────────────────
        String role = currentUser.getuPosition();
        // Employee Management → IT, HR Manager, HR Team Leader, **HR Rank and File**
        boolean canManageEmployees
                = role.equals("IT Operations and Systems")
                || role.equals("HR Manager")
                || role.equals("HR Team Leader")
                || role.equals("HR Rank and File");
        jButton3.setVisible(canManageEmployees);

        // Payroll Management → CFO, Payroll Manager, Payroll Team Leader, **Payroll Rank and File**
        boolean canManagePayroll
                = role.equals("Chief Finance Officer")
                || role.equals("Payroll Manager")
                || role.equals("Payroll Team Leader")
                || role.equals("Payroll Rank and File");
        jButton2.setVisible(canManagePayroll);

        boolean canSystemMaintenance
                = role.equals("IT Operations and Systems");
        jButton9.setVisible(canSystemMaintenance);

        //jPanel2 method
        setupJPanel2(); // we will define this method

        setResizable(false);// Removes maximize and resizing
        setLocationRelativeTo(null); // This centers the window on the screen
        pack();// Fit frame to preferred size

        // Set the company logo on the left
        SwingUtilities.invokeLater(() -> setLogoOnLabel(jLabel1, "/com/gui/images/LoginIcons/RevisedLogo.png"));
        SwingUtilities.invokeLater(() -> setProfileImage(jLabel5, currentUser.getuEmpId()));
        setUserInfo();
        startClock();

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
            jLabel6.setText("<html><b>Employee ID:</b> " + currentUser.getuEmpId() + "</html>");
            jLabel7.setText("<html><b>Name:</b> " + currentUser.getuFirstName() + " " + currentUser.getuLastName() + "</html>");
            jLabel8.setText("<html><b>Date of Birth:</b> " + currentUser.getuDob() + "</html>");
            jLabel9.setText("<html><b>Position:</b> " + currentUser.getuPosition() + "</html>");
            jLabel10.setText("<html><b>Status:</b> " + currentUser.getuStatus() + "</html>");
            jLabel11.setText("<html><b>Phone Number:</b> " + currentUser.getuPhoneNumber() + "</html>");
            jLabel12.setText("<html><b>Immediate Supervisor:</b><br>" + currentUser.getuImmediateSupervisor() + "</html>");
            jLabel14.setText("<html><b>Address:</b> " + currentUser.getuAddress() + "</html>");

            //jPanel2 Values
            jLabelSSS.setText("SSS Number: " + currentUser.getuSSS());
            jLabelPhilhealth.setText("Philhealth Number: " + currentUser.getuPhilHealth());
            jLabelTIN.setText("TIN Number: " + currentUser.getuTIN());
            jLabelPagibig.setText("Pag-ibig Number: " + currentUser.getuPagIbig());

            jLabelBasicSalary.setText("Basic Salary: " + currentUser.getuBasicSalary());
            jLabelGrossSemi.setText("Gross Semi-monthly Rate: " + currentUser.getuGrossSemiRate());

            jLabelHourlyRate.setText("Hourly Rate: " + currentUser.getuHourlyRate());
            jLabelRiceSubsidy.setText("Rice Subsidy: " + currentUser.getuRiceSubsidy());
            jLabelPhoneAllowance.setText("Phone Allowance: " + currentUser.getuPhoneAllowance());
            jLabelClothingAllowance.setText("Clothing Allowance: " + currentUser.getuClothingAllowance());

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
                int width = label.getWidth();
                int height = label.getHeight();
                float aspectRatio = (float) icon.getIconWidth() / icon.getIconHeight();
                if (width / (float) height > aspectRatio) {
                    width = (int) (height * aspectRatio);
                } else {
                    height = (int) (width / aspectRatio);
                }
                Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(img));
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

    private void setupJPanel2() {
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Additional Details"));
        jPanel2.setLayout(new java.awt.GridLayout(0, 1));

        jLabelGovHeader = new javax.swing.JLabel("Government & Contributions:");
        jLabelGovHeader.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        jLabelSSS = new javax.swing.JLabel("SSS Number: ");
        jLabelPhilhealth = new javax.swing.JLabel("Philhealth Number: ");
        jLabelTIN = new javax.swing.JLabel("TIN Number: ");
        jLabelPagibig = new javax.swing.JLabel("Pag-ibig Number: ");

        jLabelPayHeader = new javax.swing.JLabel("Pay Details:");
        jLabelPayHeader.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        jLabelBasicSalary = new javax.swing.JLabel("Basic Salary: ");
        jLabelGrossSemi = new javax.swing.JLabel("Gross Semi-monthly Rate: ");

        jLabelAllowHeader = new javax.swing.JLabel("Allowances:");
        jLabelAllowHeader.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        jLabelHourlyRate = new javax.swing.JLabel("Hourly Rate: ");
        jLabelRiceSubsidy = new javax.swing.JLabel("Rice Subsidy: ");
        jLabelPhoneAllowance = new javax.swing.JLabel("Phone Allowance: ");
        jLabelClothingAllowance = new javax.swing.JLabel("Clothing Allowance: ");

        // Add labels to jPanel2
        jPanel2.add(jLabelGovHeader);
        jPanel2.add(jLabelSSS);
        jPanel2.add(jLabelPhilhealth);
        jPanel2.add(jLabelTIN);
        jPanel2.add(jLabelPagibig);

        jPanel2.add(jLabelPayHeader);
        jPanel2.add(jLabelBasicSalary);
        jPanel2.add(jLabelGrossSemi);

        jPanel2.add(jLabelAllowHeader);
        jPanel2.add(jLabelHourlyRate);
        jPanel2.add(jLabelRiceSubsidy);
        jPanel2.add(jLabelPhoneAllowance);
        jPanel2.add(jLabelClothingAllowance);
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
        jButton7 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jButton8 = new javax.swing.JButton();

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(900, 900));
        setModalExclusionType(null);
        setSize(new java.awt.Dimension(700, 500));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.lightGray, java.awt.Color.darkGray));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel1.setMaximumSize(new java.awt.Dimension(200, 200));
        jLabel1.setMinimumSize(new java.awt.Dimension(150, 150));
        jLabel1.setPreferredSize(null);
        jLabel1.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("MENU");

        jButton2.setText("Payroll Management");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton1.setText("Update Profile");
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

        jButton7.setText("Attendance Management");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton9.setText("System Maintenance");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jButton10.setText("View Payroll Details");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
                                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jButton10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jButton7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jButton9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButton1, jButton10, jButton2, jButton3, jButton7, jButton9, jLabel2});

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(67, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jButton1, jButton10, jButton2, jButton3, jButton7, jButton9, jLabel2});

        jButton6.setText("Log out");
        jButton6.setMaximumSize(new java.awt.Dimension(75, 25));
        jButton6.setMinimumSize(new java.awt.Dimension(75, 25));
        jButton6.setPreferredSize(new java.awt.Dimension(75, 25));
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jLabel12.setText("jLabel12");

        jLabel6.setText("jLabel6");

        jLabel8.setText("jLabel8");

        jLabel14.setText("jLabel14");

        jLabel7.setText("jLabel7");

        jLabel10.setText("jLabel10");

        jLabel11.setText("jLabel11");

        jLabel9.setText("jLabel9");

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel5.setMaximumSize(new java.awt.Dimension(200, 200));
        jLabel5.setMinimumSize(new java.awt.Dimension(150, 150));
        jLabel5.setPreferredSize(null);

        jButton8.setText("Upload");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton8))
                        .addGap(6, 6, 6)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(6, 6, 6)
                        .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(6, 6, 6)
                        .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton8)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(311, 311, 311)
                        .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
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
            // Close ALL open windows first
            Window[] windows = Window.getWindows();
            for (Window window : windows) {
                if (window.isVisible()) {
                    window.dispose();
                }
            }

            // Then open LoginForm
            new com.gui.Home.LoginForm().setVisible(true);
        }
        // If NO, do nothing
    }//GEN-LAST:event_jButton6ActionPerformed
    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // Clock In Button
        // Prevent duplicate clock-in
        if (clockInTime != null && clockInDate != null) {
            JOptionPane.showMessageDialog(this, "You already clocked in at: " + clockInTime + " today.");
            return;
        }

        clockInTime = getCurrentManilaTime();
        clockInDate = getCurrentManilaDate();
        JOptionPane.showMessageDialog(this, "Time In recorded: " + clockInTime);
    }//GEN-LAST:event_jButton4ActionPerformed
    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // Clock In must be recorded first
        if (clockInTime == null || clockInDate == null) {
            JOptionPane.showMessageDialog(this, "You need to clock in first!");
            return;
        }

        String clockOutTime = getCurrentManilaTime();
        String empId = currentUser.getuEmpId();
        String userCsvFile = "src/com/csv/DTR/" + empId + ".csv";
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");

        try {
            Date clockInDateObj = timeFormat.parse(clockInTime);
            Date clockOutDateObj = timeFormat.parse(clockOutTime);

            long durationMs = clockOutDateObj.getTime() - clockInDateObj.getTime();
            if (durationMs < 0) {
                durationMs += 24 * 60 * 60 * 1000; // Handle overnight shift
            }

            long durationMinutes = durationMs / (60 * 1000);
            long hours = durationMinutes / 60;
            long minutes = durationMinutes % 60;

            // Warn if under 8 hours
            if (durationMinutes < 480) {
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "You have worked less than 8 hours (" + hours + "h " + minutes + "m).\nAre you sure you want to clock out?",
                        "Confirm Early Clock Out",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            // Write to CSV
            File file = new File(userCsvFile);
            boolean isNewFile = !file.exists();

            try (FileWriter fw = new FileWriter(file, true); BufferedWriter bw = new BufferedWriter(fw); PrintWriter out = new PrintWriter(bw)) {

                if (isNewFile) {
                    out.println("EmpID,Log Date,Clock In,Clock Out,Duration");
                }

                String durationStr = hours + "h " + minutes + "m";

                out.println(empId + ","
                        + clockInDate + ","
                        + clockInTime + ","
                        + clockOutTime + ","
                        + durationStr);
            }

            // Reset session values
            clockInTime = null;
            clockInDate = null;

            JOptionPane.showMessageDialog(this, "Time Out recorded and attendance saved!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error during Clock Out:\n" + e.getMessage());
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        editEmployee panel = new editEmployee(currentUser, currentUser.getuEmpId());
        JFrame frame = new JFrame("Update Profile — ID " + currentUser.getuEmpId());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(this);
        frame.setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (currentUser.canAccessPayrollManagement()) {
            new PayrollManagement(currentUser).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Access restricted to Finance/Payroll roles only.",
                    "Permission Denied",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // Employee Management Button
        if (currentUser.canAccessEmployeeManagement()) {
            new EmployeeManagement(currentUser).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Access restricted to leadership, IT or HR roles only.",
                    "Permission Denied",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // Open the Attendance Management window for the logged-in user
        AttendanceManagement am = new AttendanceManagement(currentUser);
        am.pack();
        am.setLocationRelativeTo(this);
        am.setVisible(true);

    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("PNG / JPG Images", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File src = chooser.getSelectedFile();
        String empId = currentUser.getuEmpId();
        String ext = src.getName().substring(src.getName().lastIndexOf('.') + 1).toLowerCase();
        String expected = empId + "." + ext;
        if (!src.getName().equals(expected)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Filename must be exactly “" + expected + "”",
                    "Invalid Filename",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        File destDir = new File("src/com/gui/images/EmployeeIDs");
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        File dest = new File(destDir, expected);

        try {
            Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            JOptionPane.showMessageDialog(this, "Upload successful");
            // no label‐preview code here
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error saving file: " + ex.getMessage(),
                    "I/O Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        // TODO add your handling code here:
        if ("IT Operations and Systems".equals(currentUser.getuPosition())) {
            new SystemMaintenance().setVisible(true);
        } else {
            JOptionPane.showMessageDialog(null, "Access restricted to IT roles only.");
        }
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        // TODO add your handling code here:
        if (payrollDisplay == null) {                       // first time
            payrollDisplay = new PayrollDisplay(currentUser, this);
        }
        payrollDisplay.setLocationRelativeTo(this);         // center on parent
        payrollDisplay.setVisible(true);                    // show (or bring to front)
        payrollDisplay.toFront();
    }//GEN-LAST:event_jButton10ActionPerformed

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
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables

    public static class LoginForm extends JFrame {

        // ────── CUSTOM FIELDS ─────────────────────────────────────────────────────
        /**
         * CSV‐backed username/password store
         */
        private Map<String, String> credentials = new HashMap<>();
        // ────────────────────────────────────────────────────────────────────────────

        /**
         * Creates new form LoginUI
         */
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
            jTextField1.setToolTipText("Format: EmployeeNumber\n(e.g. 10034)");
            jPasswordField1.setToolTipText("Format: Lowercase Initial of First Name+ LastName \n(e.g. jCruz)");
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
         * This method is called from within the constructor to initialize the
         * form. WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
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
            jLabel2.setText("MENU");
            jLabel4.setHorizontalAlignment(SwingConstants.LEFT);
            jLabel4.setIcon(new ImageIcon(getClass().getResource("/com/LoginUI/images/username.png"))); // NOI18N
            jLabel4.setLabelFor(jTextField1);
            jLabel4.setToolTipText("Format: FLastNameEmployeeNumber (ex. JRizal00001) ");
            jLabel5.setIcon(new ImageIcon(getClass().getResource("/com/LoginUI/images/password.png"))); // NOI18N
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

        /**
         * Login button handler
         */
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
        /**
         * Reads username/password pairs from a CSV at the project root
         */
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
