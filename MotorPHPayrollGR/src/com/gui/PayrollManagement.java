/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author AtlasPrimE & Miles
 */
package com.gui;

import com.payroll.MotorPHPayrollG3;
import com.opencsv.CSVReader;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.format.DateTimeParseException;
import java.util.*;
import com.gui.PaySlip;


        /**
         * PayrollManagement
         * Dynamically loads pay periods for the logged-in employee based on time entries in EmployeeTimeEntries.csv.
         */
        public class PayrollManagement extends JFrame {

            private User currentUser;
            private JComboBox<String> payPeriodComboBox;
            private LocalDate startDate, endDate;
            private Object[] payrollReport;
            private boolean isValidated = false;

            /**
             * Constructs the Payroll Management window for the given user.
             * Dynamically loads pay periods from CSV for the current user.
             */
            public PayrollManagement(User currentUser) {
                    this.currentUser = currentUser;
            setTitle("Payroll Management System");
            setSize(420, 220);
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            setResizable(false);
            setLocationRelativeTo(null);

            // Panel and layout setup
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(new Color(242, 243, 247)); // Soft background color
            panel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25)); // Padding

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Row 1: Pay Period
            gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
            JLabel lblPayPeriod = new JLabel("Pay Period:");
            panel.add(lblPayPeriod, gbc);

            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
            payPeriodComboBox = createPayPeriodComboBoxFromCSV();
            panel.add(payPeriodComboBox, gbc);

            // Row 2: Employee ID
            gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
            JLabel lblEmpId = new JLabel("Employee ID:");
            panel.add(lblEmpId, gbc);

            gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
            JTextField empIdField = new JTextField(15);
            empIdField.setEditable(false);
            empIdField.setText(currentUser.getuEmpId());
            panel.add(empIdField, gbc);

            // Row 3: Buttons, centered
            gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
            JButton btnSubmit = new JButton("Submit");
            JButton btnCancel = new JButton("Cancel");
            JPanel btnPanel = new JPanel();
            btnPanel.setOpaque(false);
            btnPanel.add(btnCancel);
            btnPanel.add(Box.createHorizontalStrut(20));
            btnPanel.add(btnSubmit);
            panel.add(btnPanel, gbc);

            setContentPane(panel);

            // Button actions (AFTER layout)
            btnSubmit.addActionListener((ActionEvent e) -> {
                validateInputs();
                if (isValidated) {
                    loadPaySlip();
                    dispose();
                }
            });

            btnCancel.addActionListener(e -> {
                new HomePage(currentUser).setVisible(true);
                dispose();
            });

            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    new HomePage(currentUser).setVisible(true);
                    dispose();
                }
            });

            setVisible(true);

    }

    /**
     * Dynamically generates pay periods based on the user's time entries in the CSV.
     * Only periods for which the user has a log are shown.
     */
        private JComboBox<String> createPayPeriodComboBoxFromCSV() {
        String csvFile = "src/com/payroll/EmployeeTimeEntries.csv";
        Set<String> payPeriods = new LinkedHashSet<>(); // Unique, ordered set
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy"); // Adjust to CSV format

        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            String[] nextLine;
            boolean isFirstLine = true;
            while ((nextLine = reader.readNext()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; } // skip header
                String empId = nextLine[0].trim();      // Parses empID
                String logDateStr = nextLine[1].trim(); // Parses Logged Date

                if (!empId.equals(currentUser.getuEmpId())) continue; // filter for current user

                LocalDate logDate = LocalDate.parse(logDateStr, dateFormatter);
                int day = logDate.getDayOfMonth();
                YearMonth ym = YearMonth.from(logDate);
                String monthName = ym.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                int year = ym.getYear();
                int lastDay = ym.lengthOfMonth();
                
                String label, payday;
                if (day <= 15) {
                    label = "1-15 " + monthName + " " + year;
                    payday = "(Payday: " + monthName + " " + lastDay + ")";
                } else {
                    label = "16-" + lastDay + " " + monthName + " " + year;
                    YearMonth nextMonth = ym.plusMonths(1);
                    String nextMonthName = nextMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                    payday = "(Payday: " + nextMonthName + " 15)";
                }
                payPeriods.add(label + " " + payday);

            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading time entries: " + e.getMessage());
        }

        if (payPeriods.isEmpty()) payPeriods.add("No pay periods found");

        return new JComboBox<>(payPeriods.toArray(new String[0]));
    }


    /**
     * Validates the pay period selection and computes the exact date range to be used for payroll.
     * Parses the dropdown label (e.g., "1-15 June 2025 (Payday: June 30)").
     */
    private void validateInputs() {
        String selected = (String) payPeriodComboBox.getSelectedItem();
        if (selected == null || selected.equals("No pay periods found")) {
            showErrorDialog("Please select a valid pay period.");
            isValidated = false;
            return;
        }

        try {
            // "1-15 June 2025 (Payday: June 30)" -> [0]="1-15", [1]="June", [2]="2025", ...
            String[] mainParts = selected.split(" ");
            String[] range = mainParts[0].split("-");
            int startDay = Integer.parseInt(range[0]);
            int endDay = Integer.parseInt(range[1]);
            String monthName = mainParts[1];
            int year = Integer.parseInt(mainParts[2]);

            DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH);
            Month month;
            try {
                month = Month.from(monthFormatter.parse(monthName));
            } catch (DateTimeParseException ex) {
                showErrorDialog("Invalid month in pay period: " + monthName);
                isValidated = false;
                return;
            }

            startDate = LocalDate.of(year, month.getValue(), startDay);
            endDate = LocalDate.of(year, month.getValue(), endDay);

            // Run payroll calculation
            payrollReport = MotorPHPayrollG3.runPayrollSearch(startDate, endDate, currentUser.getuEmpId());
            if (payrollReport == null || payrollReport.length < 22) {
                showErrorDialog("Payroll report has only " + (payrollReport == null ? 0 : payrollReport.length) + " fields. Expected 22.");
                isValidated = false;
                return;
            }
            isValidated = true;
        } catch (Exception ex) {
            showErrorDialog("Error parsing pay period: " + ex.getMessage());
            isValidated = false;
        }
        revalidate();
        repaint();
    }

    /**
     * Helper: get the pay date based on the selected period.
     */
    private LocalDate getPayDateForPeriod() {
        if (startDate.getDayOfMonth() == 1) {
            YearMonth ym = YearMonth.of(startDate.getYear(), startDate.getMonth());
            return LocalDate.of(ym.getYear(), ym.getMonthValue(), ym.lengthOfMonth());
        } else {
            LocalDate nextMonth15 = startDate.plusMonths(1).withDayOfMonth(15);
            return nextMonth15;
        }
    }

    /**
     * Launches the payslip dialog for the computed period and user.
     */
    private void loadPaySlip() {
    if (payrollReport == null || payrollReport.length < 22) {
        showErrorDialog("Payroll data is missing or incomplete.");
        return;
    }
    SwingUtilities.invokeLater(() -> {
        PaySlip slipPanel = new PaySlip(payrollReport, startDate, endDate, getPayDateForPeriod(), currentUser);
        JDialog dialog = new JDialog((Frame) null, "Pay Slip", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(slipPanel);
        // When closed, return to homepage
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (currentUser != null) {
                    new HomePage(currentUser).setVisible(true);
                }
            }
        });
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    });
}

    /**
     * Utility for showing error dialogues.
     */
    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Input Error", JOptionPane.ERROR_MESSAGE);
    }
    
}