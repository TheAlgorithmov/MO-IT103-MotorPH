/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author Leo Azarcon & ongoj
 */
package com.gui.EmpManage;

import com.gui.Home.ChangeLogs;
import com.gui.Home.User;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmployeeManagement extends JFrame {

    private JTextField txtSearch;
    private JTable tblPayroll;
    private DefaultTableModel tableModel;
    private User currentUser;

    private JPanel mainPanel;

    public EmployeeManagement(User currentUser) {
        this.currentUser = currentUser;
        setTitle("MotorPH - Employee Management");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
        boolean isHRorIT = position.equals("IT Operations and Systems")
                || position.equals("HR Manager")
                || position.equals("HR Team Leader")
                || position.equals("HR Rank and File");

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

        JButton btnDelete = new JButton("Delete");

        // Check role:
        boolean canDelete = position.equals("IT Operations and Systems")
                || position.equals("HR Manager")
                || position.equals("HR Team Leader");

        btnDelete.setEnabled(canDelete);
        topPanel.add(btnDelete);

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
        // Auto-resize columns to fit content
        tblPayroll.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int column = 0; column < tblPayroll.getColumnCount(); column++) {
            int width = 100; // Minimum width
            for (int row = 0; row < tblPayroll.getRowCount(); row++) {
                TableCellRenderer renderer = tblPayroll.getCellRenderer(row, column);
                Component comp = tblPayroll.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width + 10, width);
            }
            tblPayroll.getColumnModel().getColumn(column).setPreferredWidth(width);
        }

        // Mouse hover tooltip logic
        tblPayroll.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = tblPayroll.rowAtPoint(e.getPoint());
                if (row > -1) {
                    StringBuilder tooltip = new StringBuilder("<html>");
                    int colCount = tblPayroll.getColumnCount();
                    for (int col = 0; col < colCount; col++) {
                        Object valObj = tblPayroll.getValueAt(row, col);
                        // skip null or blank
                        if (valObj == null) {
                            continue;
                        }
                        String value = valObj.toString().trim();
                        if (value.isEmpty()) {
                            continue;
                        }

                        String colName = tblPayroll.getColumnName(col);
                        tooltip
                                .append("<b>").append(colName).append(":</b> ")
                                .append(value)
                                .append("<br>");
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
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric Employee Number.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                // Make sure EmployeeData is loaded first → so Search always works
                loadEmployeeData();
            } catch (CsvValidationException ex) {
                Logger.getLogger(EmployeeManagement.class.getName()).log(Level.SEVERE, null, ex);
            }

            // Now filter based on input
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
            addFrame.pack();
            addFrame.setLocationRelativeTo(this);
            addFrame.setVisible(true);
        });

        // View Login Credentials button → opens LoginCredentialsView in new window
        btnViewLoginCredentials.addActionListener(e -> {
            JFrame credentialsFrame = new JFrame("Login Credentials");
            credentialsFrame.setSize(800, 600);
            credentialsFrame.setLocationRelativeTo(this);

            LoginCredentialsView credentialsPanel
                    = new LoginCredentialsView(currentUser.getuEmpId());
            credentialsFrame.add(credentialsPanel);

            credentialsFrame.setVisible(true);
        });

        //Delete Button
        btnDelete.addActionListener(e -> {
            int selectedRow = tblPayroll.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an employee to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String selectedEmpID = tableModel.getValueAt(selectedRow, 0).toString();
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete employee ID: " + selectedEmpID + "?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                deleteEmployeeByID(selectedEmpID);
                logDeletion(selectedEmpID);
                try {
                    loadEmployeeData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error reloading table after deletion: " + ex.getMessage());
                }
            }
        });

        // Update button → open editEmployee panel in a new JFrame (Update mode)
        btnUpdate.addActionListener(e -> {
            int selectedRow = tblPayroll.getSelectedRow();

            // Leadership / HR / IT → can update anyone
            if (isLeader || isHRorIT) {
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(this, "Please select an employee to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int modelRow = tblPayroll.convertRowIndexToModel(selectedRow);
                String selectedEmpID = tableModel.getValueAt(modelRow, 0).toString();

                JFrame updateFrame = new JFrame("Update Employee - ID " + selectedEmpID);
                editEmployee updatePanel = new editEmployee(currentUser, selectedEmpID);

                updateFrame.setContentPane(updatePanel);
                updateFrame.pack();
                updateFrame.setLocationRelativeTo(this);
                updateFrame.setVisible(true);

            } else {
                // Other roles → can update ONLY their own profile
                String myEmpID = currentUser.getuEmpId();

                if (selectedRow == -1) {
                    // No selection → open MY profile
                    JFrame myProfileFrame = new JFrame("Update My Profile");
                    editEmployee myProfilePanel = new editEmployee(currentUser, myEmpID);

                    myProfileFrame.setContentPane(myProfilePanel);
                    myProfileFrame.setSize(800, 850);
                    myProfileFrame.setLocationRelativeTo(this);
                    myProfileFrame.setVisible(true);

                } else {
                    // Row selected → check if it's their own record
                    String selectedEmpID = tableModel.getValueAt(selectedRow, 0).toString();

                    if (selectedEmpID.equals(myEmpID)) {
                        // OK → open own profile
                        JFrame myProfileFrame = new JFrame("Update My Profile");
                        editEmployee myProfilePanel = new editEmployee(currentUser, myEmpID);

                        myProfileFrame.setContentPane(myProfilePanel);
                        myProfileFrame.setSize(800, 850);
                        myProfileFrame.setLocationRelativeTo(this);
                        myProfileFrame.setVisible(true);

                    } else {
                        // Not allowed
                        JOptionPane.showMessageDialog(this,
                                "You do not have permission to update this employee record.",
                                "Permission Denied", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        });

    }

    private void loadEmployeeData() throws CsvValidationException {
        tableModel.setRowCount(0);
        try (CSVReader reader = new CSVReader(new InputStreamReader(
                new FileInputStream("src/com/csv/EmployeeData.csv"), "UTF-8"))) {
            String[] nextLine;
            boolean isHeader = true;
            while ((nextLine = reader.readNext()) != null) {
                if (isHeader) {
                    tableModel.setColumnIdentifiers(nextLine);
                    isHeader = false;
                } else {
                    nextLine[0] = nextLine[0].trim();
                    tableModel.addRow(nextLine);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage());
        }
    }

    /**
     * Deletes the given employee ID row from EmployeeData.csv, using OpenCSV.
     */
    private void deleteEmployeeByID(String empID) {
        // 1) remove from EmployeeData.csv
        String empDataPath = "src/com/csv/EmployeeData.csv";
        try {
            List<String[]> rows;
            try (CSVReader r = new CSVReader(new FileReader(empDataPath))) {
                rows = r.readAll();
            }
            try (CSVWriter w = new CSVWriter(new FileWriter(empDataPath))) {
                for (int i = 0; i < rows.size(); i++) {
                    String[] row = rows.get(i);
                    // keep header or any row whose ID != empID
                    if (i == 0 || !row[0].equals(empID)) {
                        w.writeNext(row);
                    }
                }
            }
        } catch (IOException | CsvException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error deleting employee from EmployeeData.csv:\n" + ex.getMessage(),
                    "I/O Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2) delete DTR file
        File dtrFile = new File("src/com/csv/DTR", empID + ".csv");
        if (dtrFile.exists() && !dtrFile.delete()) {
            JOptionPane.showMessageDialog(this,
                    "Warning: could not delete DTR file for " + empID,
                    "File I/O Warning", JOptionPane.WARNING_MESSAGE);
        }

        // 3) remove from LoginCredentials.csv
        String credPath = "src/com/csv/LoginCredentials.csv";
        try {
            List<String[]> creds;
            try (CSVReader r = new CSVReader(new FileReader(credPath))) {
                creds = r.readAll();
            }
            try (CSVWriter w = new CSVWriter(new FileWriter(credPath))) {
                for (int i = 0; i < creds.size(); i++) {
                    String[] line = creds.get(i);
                    // keep header or lines whose username != empID
                    if (i == 0 || !line[0].equals(empID)) {
                        w.writeNext(line);
                    }
                }
            }
        } catch (IOException | CsvException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error removing from LoginCredentials.csv:\n" + ex.getMessage(),
                    "I/O Error", JOptionPane.ERROR_MESSAGE);
            // note: we already deleted the main record, so proceed
        }

        JOptionPane.showMessageDialog(this,
                "Employee " + empID + " deleted (and associated DTR + login creds removed).",
                "Deletion Complete",
                JOptionPane.INFORMATION_MESSAGE);
        
        // 4) remove from SupervisorLists.csv
        String supervisorListPath = "src/com/csv/SupervisorLists.csv";
            try {
                List<String[]> rows;
                try (CSVReader r = new CSVReader(new FileReader(supervisorListPath))) {
                    rows = r.readAll();
                }
                try (CSVWriter w = new CSVWriter(new FileWriter(supervisorListPath))) {
                    for (int i = 0; i < rows.size(); i++) {
                        String[] row = rows.get(i);
                        // Keep header or rows where empID does not match
                        if (i == 0 || !row[0].trim().equals(empID.trim())) {
                            w.writeNext(row);
                        }
                    }
                }
            } catch (IOException | CsvException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error removing from SupervisorLists.csv:\n" + ex.getMessage(),
                        "I/O Error", JOptionPane.ERROR_MESSAGE);
            }
    }
    
    /**
     * Deletes an employee from: - EmployeeData.csv - LoginCredentials.csv -
     * DTR/<empID>.csv
     */
    private void logDeletion(String empID) {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter("src/com/csv/EmpDataChangeLogs.csv", true))) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String currentUserName = currentUser.getuFirstName() + " " + currentUser.getuLastName();
            writer.write(String.join(",",
                    "DELETE", currentUserName, empID,
                    "ALL FIELDS", "N/A", "Employee Deleted",
                    timestamp, "Approved", "Record removed"
            ));
            writer.newLine();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error logging deletion: " + e.getMessage());
        }
    }
}
