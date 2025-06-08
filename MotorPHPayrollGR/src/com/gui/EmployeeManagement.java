 /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author Leo Azarcon & ongoj
 */

package com.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmployeeManagement extends JFrame {

    private JTextField txtSearch;
    private JTable tblPayroll;
    private DefaultTableModel tableModel;
    private User currentUser;

    private JPanel mainPanel; // NEW: main panel to swap views

    public EmployeeManagement(User currentUser) {
        this.currentUser = currentUser;
        setTitle("MotorPH - Employee Management");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Don't close HomePage!

        initComponents();
    }

    private void initComponents() {
        // Main Layout
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // === NORTH: Search bar + buttons ===
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel lblSearch = new JLabel("Enter Employee Number:");
        txtSearch = new JTextField(10);

        JButton btnSearch = new JButton("Search");
        JButton btnLoad = new JButton("Show All Records");
        JButton btnClear = new JButton("Clear");
        JButton btnExit = new JButton("Exit");
        JButton btnViewLogs = new JButton("View Change Logs");
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnViewLoginCredentials = new JButton("View Login Credentials");
        
        // Role check
        String position = currentUser.getuPosition();
        boolean isHRorIT = position.equals("IT Operations and Systems") ||
                           position.equals("HR Manager") ||
                           position.equals("HR Team Leader") ||
                           position.equals("HR Rank and File");
        
        //Leadership Roles
        boolean isLeader = currentUser.getuPosition().equals("HR Manager")
        || currentUser.getuPosition().equals("HR Team Leader")
        || currentUser.getuPosition().equals("Chief Executive Officer")
        || currentUser.getuPosition().equals("Chief Operating Officer")
        || currentUser.getuPosition().equals("Chief Finance Officer")
        || currentUser.getuPosition().equals("Chief Marketing Officer")
        || currentUser.getuPosition().equals("IT Operations and Systems")
        || currentUser.getuPosition().equals("Accounting Head");


        btnAdd.setEnabled(isHRorIT);
        btnUpdate.setEnabled(true);

        // Add buttons to panel
        topPanel.add(lblSearch);
        topPanel.add(txtSearch);
        topPanel.add(btnSearch);
        topPanel.add(btnLoad);
        topPanel.add(btnClear);
        topPanel.add(btnExit);
        topPanel.add(btnViewLogs);
        topPanel.add(btnAdd);
        topPanel.add(btnUpdate);

        // ONLY IT can see "View Login Credentials"
        if (position.equals("IT Operations and Systems")) {
            topPanel.add(btnViewLoginCredentials);
        }
        
        panel.add(topPanel, BorderLayout.NORTH);

        // === CENTER: Main Panel ===
        mainPanel = new JPanel(new BorderLayout());
        panel.add(mainPanel, BorderLayout.CENTER);

        // Initialize payroll table inside mainPanel
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        tblPayroll = new JTable(tableModel);

        // Mouse hover tooltip logic
        tblPayroll.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = tblPayroll.rowAtPoint(e.getPoint());
                if (row > -1) {
                    StringBuilder tooltip = new StringBuilder("<html>");
                    int colCount = tblPayroll.getColumnCount();
                    for (int col = 0; col < colCount; col++) {
                        String colName = tblPayroll.getColumnName(col);
                        String value = String.valueOf(tblPayroll.getValueAt(row, col));
                        tooltip.append("<b>").append(colName).append(":</b> ").append(value).append("<br>");
                    }
                    tooltip.append("</html>");
                    tblPayroll.setToolTipText(tooltip.toString());
                } else {
                    tblPayroll.setToolTipText(null);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblPayroll);

        // Add Table as initial content
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Set content pane
        setContentPane(panel);

        // === Event Listeners ===

        // Search
        btnSearch.addActionListener(e -> {
            String input = txtSearch.getText().trim();
            if (!input.matches("\\d+")) {
                JOptionPane.showMessageDialog(this,
                    "Please enter a valid numeric Employee Number.",
                    "Invalid Input",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
            tblPayroll.setRowSorter(sorter);
            sorter.setRowFilter(RowFilter.regexFilter("^" + input + "$", 0));
            
        });

        // Load All Records
        btnLoad.addActionListener(e -> {
            try {
                loadEmployeeData();
            tblPayroll.setRowSorter(null); // <=== Clear RowSorter to show ALL records
            } catch (CsvValidationException ex) {
                Logger.getLogger(EmployeeManagement.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        // Clear
        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            tableModel.setRowCount(0);
        });

        // Exit button
        btnExit.addActionListener(e -> dispose());

        // View Change Logs button
        btnViewLogs.addActionListener(e -> {
            JFrame logFrame = new JFrame("Employee Change Logs");
            logFrame.setSize(1000, 600);
            logFrame.setLocationRelativeTo(this);

            ChangeLogs changeLogsPanel = new ChangeLogs();
            logFrame.add(changeLogsPanel);

            logFrame.setVisible(true);
        });

        // === MOST IMPORTANT PART ===

        // Add button → open editEmployee panel in a new JFrame (Add mode)
        btnAdd.addActionListener(e -> {
            JFrame addFrame = new JFrame("Add New Employee");
            editEmployee addPanel = new editEmployee(currentUser, null); // null = add mode

            addFrame.setContentPane(addPanel);
            addFrame.setSize(600, 800);
            addFrame.setLocationRelativeTo(this);
            addFrame.setVisible(true);
        });

        // Update button → open editEmployee panel in a new JFrame (Update mode)
        btnUpdate.addActionListener(e -> {
            if (isLeader) {
                // Leadership → update any selected employee
                int selectedRow = tblPayroll.getSelectedRow();

                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(this, "Please select an employee to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String selectedEmpID = tableModel.getValueAt(selectedRow, 0).toString();

                JFrame updateFrame = new JFrame("Update Employee - ID " + selectedEmpID);
                editEmployee updatePanel = new editEmployee(currentUser, selectedEmpID);

                updateFrame.setContentPane(updatePanel);
                updateFrame.setSize(600, 800);
                updateFrame.setLocationRelativeTo(this);
                updateFrame.setVisible(true);

            } else {
                // Regular user → can only update own profile
                String myEmpID = currentUser.getuEmpId();

                JFrame myProfileFrame = new JFrame("Update My Profile");
                editEmployee myProfilePanel = new editEmployee(currentUser, myEmpID);

                myProfileFrame.setContentPane(myProfilePanel);
                myProfileFrame.setSize(600, 800);
                myProfileFrame.setLocationRelativeTo(this);
                myProfileFrame.setVisible(true);
            }
        });

            //ViewLogin Credentials
            btnViewLoginCredentials.addActionListener(e -> {
                JFrame credentialsFrame = new JFrame("Login Credentials");
                credentialsFrame.setSize(800, 600);
                credentialsFrame.setLocationRelativeTo(this);

                LoginCredentialsView credentialsPanel = new LoginCredentialsView();
                credentialsFrame.add(credentialsPanel);

                credentialsFrame.setVisible(true);
            });

        }

    private void loadEmployeeData() throws CsvValidationException {
        tableModel.setRowCount(0); // Clear table
        try (CSVReader reader = new CSVReader(new FileReader("src/com/csv/EmployeeData.csv"))) {
            String[] nextLine;
            boolean isHeader = true;
            while ((nextLine = reader.readNext()) != null) {
                if (isHeader) {
                    tableModel.setColumnIdentifiers(nextLine);
                    isHeader = false;
                } else {
                    nextLine[0] = nextLine[0].trim(); // <== THIS LINE GOES HERE
                    tableModel.addRow(nextLine);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage());
        }
    }
}