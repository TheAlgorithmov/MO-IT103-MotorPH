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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import com.payroll.*;

public class PayrollManagement extends JFrame {
    private JTextField startDateField, endDateField, empIdField;
    private LocalDate startDate, endDate;
    private String inputEmpId;
    private Object[] payrollReport;
    boolean isValidated = false;

    public PayrollManagement() {
        setTitle("Payroll Management System");
        setSize(375, 240);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        SpringLayout layout = new SpringLayout();
        JPanel panel = new JPanel(layout);
        getContentPane().add(panel);

        JLabel startDateLabel = new JLabel("Start Date (YYYY-MM-DD):");
        startDateField = new JTextField(15);

        JLabel endDateLabel = new JLabel("End Date (YYYY-MM-DD):");
        endDateField = new JTextField(15);

        JLabel empIdLabel = new JLabel("Employee ID (5-digit):");
        empIdField = new JTextField(15);

        JButton submitButton = new JButton("Submit");

        panel.add(startDateLabel);
        panel.add(startDateField);
        panel.add(endDateLabel);
        panel.add(endDateField);
        panel.add(empIdLabel);
        panel.add(empIdField);
        panel.add(submitButton);

        // Layout constraints
        layout.putConstraint(SpringLayout.WEST, startDateLabel, 10, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.NORTH, startDateLabel, 20, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.WEST, startDateField, 180, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.NORTH, startDateField, 20, SpringLayout.NORTH, panel);

        layout.putConstraint(SpringLayout.WEST, endDateLabel, 10, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.NORTH, endDateLabel, 20, SpringLayout.SOUTH, startDateLabel);
        layout.putConstraint(SpringLayout.WEST, endDateField, 180, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.NORTH, endDateField, 20, SpringLayout.SOUTH, startDateField);

        layout.putConstraint(SpringLayout.WEST, empIdLabel, 10, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.NORTH, empIdLabel, 20, SpringLayout.SOUTH, endDateLabel);
        layout.putConstraint(SpringLayout.WEST, empIdField, 180, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.NORTH, empIdField, 20, SpringLayout.SOUTH, endDateField);

        layout.putConstraint(SpringLayout.WEST, submitButton, 180, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.NORTH, submitButton, 30, SpringLayout.SOUTH, empIdField);

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                validateInputs();
                if (isValidated) {
                    loadPayrollSlip();
                    dispose();
                }
                
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void validateInputs() {
        try {
            startDate = LocalDate.parse(startDateField.getText().trim());
        } catch (DateTimeParseException e) {
            showErrorDialog("Invalid start date format.");
            return;
        }

        try {
            endDate = LocalDate.parse(endDateField.getText().trim());
            if (endDate.isBefore(startDate)) {
                showErrorDialog("End date cannot be before start date.");
                return;
            }
        } catch (DateTimeParseException e) {
            showErrorDialog("Invalid end date format.");
            return;
        }

        inputEmpId = empIdField.getText().trim();
        if (!inputEmpId.matches("\\d{5}")) {
            showErrorDialog("Invalid Employee ID format.");
            return;
        } 

        // Run Payroll Search
        payrollReport = MotorPHPayrollG3.runPayrollSearch(startDate, endDate, inputEmpId);
        if (payrollReport != null) {
            isValidated = true;
        }
        
        revalidate();
        repaint();
        
    }
    
    private void loadPayrollSlip() {
        SwingUtilities.invokeLater(() -> {
            new PayrollSlip(payrollReport);
        });
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getInputEmpId() {
        return inputEmpId;
    }

}
