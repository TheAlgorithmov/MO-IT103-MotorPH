package com.gui.Payroll;

import com.gui.Home.User;
import com.opencsv.CSVWriter;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.table.DefaultTableModel;

public class PayrollManagementHelper {
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> payPeriodComboBox;
    private User currentUser;

    public PayrollManagementHelper(JTable employeeTable, JComboBox<String> payPeriodComboBox, User currentUser) {
        this.employeeTable = employeeTable;
        this.tableModel = (DefaultTableModel) employeeTable.getModel(); // FIXED
        this.payPeriodComboBox = payPeriodComboBox;
        this.currentUser = currentUser;
    }

    private void appendToPayrollReports(String empId, String firstName, String lastName, String approvedByEmpId, String approvedByName) {
        String filename = "src/com/csv/PayrollReports.csv";
        String approvedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("M/d/yyyy"));
        try (CSVWriter writer = new CSVWriter(new FileWriter(filename, true))) {
            File file = new File(filename);
            if (!file.exists() || file.length() == 0) {
                writer.writeNext(new String[]{"EmpID", "First Name", "Last Name", "Payroll Status", "Approved By EmpID", "Name", "Approved Date"});
            }
            writer.writeNext(new String[]{empId, firstName, lastName, "Approved", approvedByEmpId, approvedByName, approvedDate});
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error writing to PayrollReports.csv: " + e.getMessage());
        }
    }

    public void showPayrollAuditTrail() {
        String filename = "src/com/csv/PayrollReports.csv";
        DefaultTableModel model = new DefaultTableModel();
        JTable table = new JTable(model);
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (isHeader) {
                    for (String col : data) model.addColumn(col);
                    isHeader = false;
                } else {
                    model.addRow(data);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error loading audit trail: " + e.getMessage());
            return;
        }
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        JOptionPane.showMessageDialog(null, scrollPane, "Payroll Approval Audit Trail", JOptionPane.INFORMATION_MESSAGE);
    }

    public void approveAllPayrolls() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
        LocalDate today = LocalDate.now();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 3).toString().equalsIgnoreCase("Approved")) {
                tableModel.setValueAt("Approved", i, 5);
                tableModel.setValueAt(today.format(formatter), i, 6);
                tableModel.setValueAt("Approved", i, 8);
                appendToPayrollReports(
                    tableModel.getValueAt(i, 0).toString(),
                    tableModel.getValueAt(i, 1).toString(),
                    tableModel.getValueAt(i, 2).toString(),
                    currentUser.getuEmpId(),
                    currentUser.getuFirstName()
                );
            }
        }
    }

    public void addAuditTrailButton(JPanel panel) {
        JButton auditButton = new JButton("View Audit Trail");
        auditButton.addActionListener(e -> showPayrollAuditTrail());
        panel.add(auditButton);
    }

    public void addSaveButton(JPanel panel) {
        JButton saveButton = new JButton("Save Changes");
        saveButton.addActionListener(e -> saveTableToCSV());
        panel.add(saveButton);
    }

    private void saveTableToCSV() {
        String filename = "src/com/csv/DTR/DTRPayrollStatus.csv";
        try (CSVWriter writer = new CSVWriter(new FileWriter(filename))) {
            writer.writeNext(new String[]{"EmpID", "First Name", "Last Name", "DTR Status", "DTR Approved Date", "Payroll Status", "Payroll Approved Date", "NetPay", "Payslip Ready"});
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String[] row = new String[9];
                for (int j = 0; j < 9; j++) row[j] = String.valueOf(tableModel.getValueAt(i, j));
                writer.writeNext(row);
            }
            JOptionPane.showMessageDialog(null, "Changes saved successfully to DTRPayrollStatus.csv");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving changes: " + e.getMessage());
        }
    }

    public void exportToCSVWithChooser() {
        String selected = (String) payPeriodComboBox.getSelectedItem();
        if (selected == null || selected.equals("No pay periods found")) {
            JOptionPane.showMessageDialog(null, "Please select a pay period first.");
            return;
        }
        try {
            String[] mainParts = selected.split(" ");
            String[] range = mainParts[0].split("-");
            int startDay = Integer.parseInt(range[0]);
            int endDay = Integer.parseInt(range[1]);
            String monthName = mainParts[1];
            int year = Integer.parseInt(mainParts[2]);

            DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH);
            Month month = Month.from(monthFormatter.parse(monthName));

            LocalDate from = LocalDate.of(year, month.getValue(), startDay);
            LocalDate to = LocalDate.of(year, month.getValue(), endDay);
            String filename = String.format("Payroll_%s-to-%s.csv", from, to);

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(filename));
            int userSelection = fileChooser.showSaveDialog(null);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                try (CSVWriter writer = new CSVWriter(new FileWriter(fileToSave))) {
                    writer.writeNext(new String[]{"EmpID", "First Name", "Last Name", "DTR Status", "DTR Approved Date", "Payroll Status", "Payroll Approved Date", "NetPay", "Payslip Ready"});
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        String[] row = new String[9];
                        for (int j = 0; j < 9; j++) row[j] = String.valueOf(tableModel.getValueAt(i, j));
                        writer.writeNext(row);
                    }
                    JOptionPane.showMessageDialog(null, "Report exported as: " + fileToSave.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error exporting report: " + e.getMessage());
        }
    }
}
