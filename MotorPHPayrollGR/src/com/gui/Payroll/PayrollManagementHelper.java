package com.gui.Payroll;

import com.gui.Home.User;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.table.DefaultTableModel;

public class PayrollManagementHelper {
    private final DefaultTableModel model;
    private final User currentUser;
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("M/d/yyyy");

    public PayrollManagementHelper(JTable table, User currentUser) {
        this.model = (DefaultTableModel) table.getModel();
        this.currentUser = currentUser;
    }

        public void approveAllPayrolls() {
        String approverId   = currentUser.getuEmpId();
        String approvedDate = LocalDate.now().format(DF);

        for (int i = 0; i < model.getRowCount(); i++) {
            // column 5 = “Payroll Status” in your table
            if (!"Approved".equalsIgnoreCase(model.getValueAt(i, 5).toString())) {
                continue;
            }

            String empId = model.getValueAt(i, 0).toString();
            String date  = model.getValueAt(i, 1).toString(); // the “Date” column

            try {
                // stamp into columns  9=PayrollApprovedBy, 
                //                     10=PayrollApprovedDate,
                //                     11=PayrollStatus
                DtrCsvUtil.updateCell(empId, date, 9,  approverId);
                DtrCsvUtil.updateCell(empId, date,10, approvedDate);
                DtrCsvUtil.updateCell(empId, date,11, "Approved");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                    "Failed approving " + empId + " on " + date + ":\n"
                    + ex.getMessage(),
                    "I/O Error",
                    JOptionPane.ERROR_MESSAGE);
                // keep going to the next row
            }
        }

        JOptionPane.showMessageDialog(null,
            "All approved payrolls have been stamped into each DTR file.",
            "Done", JOptionPane.INFORMATION_MESSAGE);
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

    public void addAuditTrailButton(JPanel panel) {
        JButton auditButton = new JButton("View Audit Trail");
        auditButton.addActionListener(e -> showPayrollAuditTrail());
        panel.add(auditButton);
    }
}
