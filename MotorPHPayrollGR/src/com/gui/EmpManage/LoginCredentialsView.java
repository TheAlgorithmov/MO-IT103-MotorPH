package com.gui.EmpManage;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

public class LoginCredentialsView extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JButton toggleLockButton, btnSaveChanges;
    private JTextField txtSearch;
    private JButton btnSearch;
    private String currentUserId;

    public LoginCredentialsView() {
        this(null);
    }

    public LoginCredentialsView(String currentUserId) {
        this.currentUserId = currentUserId;
        setLayout(new BorderLayout());

        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (column == 5 && currentUserId != null && getValueAt(row, 0).equals(currentUserId)) {
                    return false;
                }
                return column == 1 || column == 5;
            }
        };

        table = new JTable(model) {
            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                if (column == 5) {
                    String empId = model.getValueAt(row, 0).toString();
                    if (empId.equals(currentUserId)) {
                        return null; // Prevent editing own Lock Out Status
                    }
                    JComboBox<String> comboBox = new JComboBox<>(new String[]{"No", "Yes"});
                    return new DefaultCellEditor(comboBox);
                }
                return super.getCellEditor(row, column);
            }
        };

        // Visual highlight for current user's row
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String empId = table.getValueAt(row, 0).toString();
                if (empId.equals(currentUserId)) {
                    c.setBackground(Color.LIGHT_GRAY);
                } else {
                    c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                }
                return c;
            }
        });

        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        txtSearch = new JTextField(10);
        btnSearch = new JButton("Search by Employee ID");
        toggleLockButton = new JButton("Toggle Lock Out Status");
        btnSaveChanges = new JButton("Save Changes");

        leftPanel.add(new JLabel("Employee ID:"));
        leftPanel.add(txtSearch);
        leftPanel.add(btnSearch);
        leftPanel.add(toggleLockButton);

        rightPanel.add(btnSaveChanges);

        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(rightPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        loadCredentials();
        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow();
            boolean ownRow = (r != -1)
                    && table.getValueAt(r, 0).toString().equals(currentUserId);
            toggleLockButton.setEnabled(!ownRow);
        });
        toggleLockButton.addActionListener(e -> toggleSelectedUser());
        btnSearch.addActionListener(e -> searchByEmployeeId());
        btnSaveChanges.addActionListener(this::btnSaveActionPerformed);
    }

    private void loadCredentials() {
        model.setRowCount(0);
        try (CSVReader reader = new CSVReader(new FileReader("src/com/csv/LoginCredentials.csv"))) {
            List<String[]> allRows = reader.readAll();
            if (!allRows.isEmpty()) {
                model.setColumnIdentifiers(allRows.get(0));
                for (int i = 1; i < allRows.size(); i++) {
                    model.addRow(allRows.get(i));
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading login credentials: " + e.getMessage());
        }
    }

    private void toggleSelectedUser() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user.");
            return;
        }

        String empId = model.getValueAt(selectedRow, 0).toString();
        if (currentUserId != null && empId.equals(currentUserId)) {
            JOptionPane.showMessageDialog(this, "You cannot modify your own Lock Out Status for security reasons.");
            return;
        }

        String currentStatus = model.getValueAt(selectedRow, 5).toString();
        String newStatus = currentStatus.equalsIgnoreCase("Yes") ? "No" : "Yes";
        model.setValueAt(newStatus, selectedRow, 5);
    }

    private void btnSaveActionPerformed(ActionEvent evt) {
        if (table.isEditing()) {               
            TableCellEditor ed = table.getCellEditor();
            if (ed != null) {
                ed.stopCellEditing();
            }
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to apply changes?",
                "Confirm Save", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter("src/com/csv/LoginCredentials.csv"))) {
            int columnCount = model.getColumnCount();
            String[] header = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                header[i] = model.getColumnName(i);
            }
            writer.writeNext(header);

            int rowCount = model.getRowCount();
            for (int row = 0; row < rowCount; row++) {
                String empId = model.getValueAt(row, 0).toString();
                String[] rowData = new String[columnCount];
                for (int col = 0; col < columnCount; col++) {
                    if (col == 5 && empId.equals(currentUserId)) {
                        rowData[col] = "No";
                    } else {
                        Object value = model.getValueAt(row, col);
                        rowData[col] = value != null ? value.toString() : "";
                    }
                }
                writer.writeNext(rowData);
            }

            JOptionPane.showMessageDialog(this, "Changes saved successfully!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage());
        }
    }

    private void searchByEmployeeId() {
        String searchId = txtSearch.getText().trim();
        if (searchId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an Employee ID to search.");
            return;
        }

        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).toString().equals(searchId)) {
                table.setRowSelectionInterval(i, i);
                table.scrollRectToVisible(table.getCellRect(i, 0, true));
                return;
            }
        }

        JOptionPane.showMessageDialog(this, "Employee ID not found.");
    }
}
