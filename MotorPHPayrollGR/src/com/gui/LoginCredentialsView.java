/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author ongoj
 */
package com.gui;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class LoginCredentialsView extends JPanel {

    private JTable tblCredentials;
    private DefaultTableModel tableModel;

    public LoginCredentialsView() {
        initComponents();
        try {
            loadCredentialsData();
        } catch (CsvValidationException e) {
            JOptionPane.showMessageDialog(this, "Error reading LoginCredentials.csv: " + e.getMessage());
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only Password column (index 1) is editable
                return column == 1;
            }
        };

        tblCredentials = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tblCredentials);

        // === Buttons Panel ===
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Save Changes");

        btnSave.addActionListener(this::btnSaveActionPerformed);

        buttonsPanel.add(btnSave);

        // Add to main layout
        add(scrollPane, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    private void loadCredentialsData() throws CsvValidationException {
        tableModel.setRowCount(0); // Clear table
        try (CSVReader reader = new CSVReader(new FileReader("src/com/csv/LoginCredentials.csv"))) {
            String[] nextLine;
            boolean isHeader = true;
            while ((nextLine = reader.readNext()) != null) {
                if (isHeader) {
                    tableModel.setColumnIdentifiers(new Object[]{
                            "UserID", "Password", "First Name", "Last Name", "Roles"
                    });
                    isHeader = false;
                } else {
                    tableModel.addRow(nextLine);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage());
        }
    }

    private void btnSaveActionPerformed(ActionEvent evt) {
        try (CSVWriter writer = new CSVWriter(new FileWriter("src/com/csv/LoginCredentials.csv"))) {
            // Write header
            int columnCount = tableModel.getColumnCount();
            String[] header = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                header[i] = tableModel.getColumnName(i);
            }
            writer.writeNext(header);

            // Write data rows
            int rowCount = tableModel.getRowCount();
            for (int row = 0; row < rowCount; row++) {
                String[] rowData = new String[columnCount];
                for (int col = 0; col < columnCount; col++) {
                    Object value = tableModel.getValueAt(row, col);
                    rowData[col] = value != null ? value.toString() : "";
                }
                writer.writeNext(rowData);
            }

            JOptionPane.showMessageDialog(this, "Changes saved successfully!");

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage());
        }
    }
}
