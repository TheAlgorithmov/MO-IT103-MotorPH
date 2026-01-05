/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gui.EmpManage;

/**
 *
 * @author ongoj
 */
import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class DateRangeDialog extends JDialog {

    private final com.toedter.calendar.JDateChooser fromDateChooser;
    private final com.toedter.calendar.JDateChooser toDateChooser;
    private boolean confirmed = false;

    public DateRangeDialog(JFrame parent) {
        super(parent, "Select Date Range", true);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        fromDateChooser = new com.toedter.calendar.JDateChooser();
        toDateChooser = new com.toedter.calendar.JDateChooser();
        
        // Set proper format and size so display is correct
        fromDateChooser.setDateFormatString("MM/dd/yyyy");
        fromDateChooser.setPreferredSize(new Dimension(120, 25));

        toDateChooser.setDateFormatString("MM/dd/yyyy");
        toDateChooser.setPreferredSize(new Dimension(120, 25));

        // Row 1: From Date
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("From:"), gbc);
        gbc.gridx = 1;
        add(fromDateChooser, gbc);

        // Row 2: To Date
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("To:"), gbc);
        gbc.gridx = 1;
        add(toDateChooser, gbc);

        // Row 3: Buttons
        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> {
            Date from = fromDateChooser.getDate();
            Date to = toDateChooser.getDate();

            if (from == null || to == null) {
                JOptionPane.showMessageDialog(this, "Please select both From and To dates.", "Date Range Error", JOptionPane.ERROR_MESSAGE);
                return; // do NOT close dialog
            }

            if (from.after(to)) {
                JOptionPane.showMessageDialog(this, "Invalid date range!\n'From' date must not be after 'To' date.", "Date Range Error", JOptionPane.ERROR_MESSAGE);
                return; // do NOT close dialog
            }

            // If valid:
            confirmed = true;
            dispose();
        });

        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        add(buttonPanel, gbc);

        pack();
        setLocationRelativeTo(parent);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Date getFromDate() {
        return fromDateChooser.getDate();
    }

    public Date getToDate() {
        return toDateChooser.getDate();
    }
}
