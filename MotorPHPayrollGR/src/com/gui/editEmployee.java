package com.gui;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*;

public class editEmployee extends JPanel {

    private JTextField txtEmpNum, txtFirstname, txtLastname, txtBirthday, txtAddress, txtPhoneNumber, txtSSS,
            txtPhilHealth, txtTin, txtPagIbig, txtPosition, txtBasicSalary, txtRiceSubsidy, txtPhoneAllowance,
            txtClothingAllowance, txtGrossSemiMonthlyRate, txtHourlyRate;

    private JComboBox<String> cmbStatus, cmbSupervisor;

    private User currentUser;
    private String editingEmpID; // null for Add, not null for Update

    public editEmployee(User currentUser) {
        this(currentUser, null);
    }

    public editEmployee(User currentUser, String editingEmpID) {
        this.currentUser = currentUser;
        this.editingEmpID = editingEmpID;

        initComponents();
        initCustomFeatures();

        if (editingEmpID == null) {
            autoGenerateEmployeeID();
        } else {
            loadSelectedEmployee(editingEmpID);
        }
    }

    private void initComponents() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtEmpNum = new JTextField();
        txtFirstname = new JTextField();
        txtLastname = new JTextField();
        txtBirthday = new JTextField();
        txtAddress = new JTextField();
        txtPhoneNumber = new JTextField();
        txtSSS = new JTextField();
        txtPhilHealth = new JTextField();
        txtTin = new JTextField();
        txtPagIbig = new JTextField();
        cmbStatus = new JComboBox<>();
        txtPosition = new JTextField();
        cmbSupervisor = new JComboBox<>();
        txtBasicSalary = new JTextField();
        txtRiceSubsidy = new JTextField();
        txtPhoneAllowance = new JTextField();
        txtClothingAllowance = new JTextField();
        txtGrossSemiMonthlyRate = new JTextField();
        txtHourlyRate = new JTextField();

        txtEmpNum.setEditable(false); // auto-generated
        txtGrossSemiMonthlyRate.setEditable(false); // auto-calculated
        txtHourlyRate.setEditable(false); // auto-calculated

        // === Populate cmbStatus with all statuses
        cmbStatus.setModel(new DefaultComboBoxModel<>(new String[]{
                "Regular", "Probationary", "Leave", "Rehire",
                "Retired", "Resigned", "Terminated"
        }));

        // === Now add the fields, 2-column layout
        int row = 0;

        // LEFT COLUMN
        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Employee #:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtEmpNum, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtLastname, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtAddress, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("SSS #:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtSSS, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("TIN #:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtTin, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(cmbStatus, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Immediate Supervisor:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(cmbSupervisor, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Rice Subsidy:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtRiceSubsidy, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Clothing Allowance:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtClothingAllowance, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Hourly Rate:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtHourlyRate, gbc);

        // RIGHT COLUMN
        row = 0;

        gbc.gridx = 2; gbc.gridy = row; formPanel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 3; gbc.gridy = row++; formPanel.add(txtFirstname, gbc);

        gbc.gridx = 2; gbc.gridy = row; formPanel.add(new JLabel("Birthday:"), gbc);
        gbc.gridx = 3; gbc.gridy = row++; formPanel.add(txtBirthday, gbc);

        gbc.gridx = 2; gbc.gridy = row; formPanel.add(new JLabel("Phone #:"), gbc);
        gbc.gridx = 3; gbc.gridy = row++; formPanel.add(txtPhoneNumber, gbc);

        gbc.gridx = 2; gbc.gridy = row; formPanel.add(new JLabel("Philhealth #:"), gbc);
        gbc.gridx = 3; gbc.gridy = row++; formPanel.add(txtPhilHealth, gbc);

        gbc.gridx = 2; gbc.gridy = row; formPanel.add(new JLabel("Pag-Ibig #:"), gbc);
        gbc.gridx = 3; gbc.gridy = row++; formPanel.add(txtPagIbig, gbc);

        gbc.gridx = 2; gbc.gridy = row; formPanel.add(new JLabel("Position:"), gbc);
        gbc.gridx = 3; gbc.gridy = row++; formPanel.add(txtPosition, gbc);

        gbc.gridx = 2; gbc.gridy = row; formPanel.add(new JLabel("Basic Salary:"), gbc);
        gbc.gridx = 3; gbc.gridy = row++; formPanel.add(txtBasicSalary, gbc);

        gbc.gridx = 2; gbc.gridy = row; formPanel.add(new JLabel("Phone Allowance:"), gbc);
        gbc.gridx = 3; gbc.gridy = row++; formPanel.add(txtPhoneAllowance, gbc);

        gbc.gridx = 2; gbc.gridy = row; formPanel.add(new JLabel("Gross Semi-Monthly Rate:"), gbc);
        gbc.gridx = 3; gbc.gridy = row++; formPanel.add(txtGrossSemiMonthlyRate, gbc);

        // === Buttons Panel ===
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton btnAdd = new JButton(editingEmpID == null ? "Add" : "Update");
        JButton btnClear = new JButton("Clear");
        JButton btnBack = new JButton("Back");

        btnAdd.addActionListener(this::btnAddActionPerformed);
        btnClear.addActionListener(this::btnClearActionPerformed);
        btnBack.addActionListener(e -> {
            // Go back to EmployeeManagement view
            SwingUtilities.getWindowAncestor(this).dispose();
        });

        buttonsPanel.add(btnAdd);
        buttonsPanel.add(btnClear);
        buttonsPanel.add(btnBack);

        // === Main Layout ===
        setLayout(new BorderLayout());
        add(formPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    private void initCustomFeatures() {
        // Status dropdown
        cmbStatus.setModel(new DefaultComboBoxModel<>(new String[]{
            "Regular", "Probationary", "Retired", "Resigned", "Terminated", "Leave"
        }));

        // Supervisor dropdown → names of leadership users from EmployeeData.csv
        cmbSupervisor.removeAllItems();
        Set<String> leaders = new TreeSet<>(); // sorted + no duplicate

        try (BufferedReader br = new BufferedReader(new FileReader("src/com/csv/EmployeeData.csv"))) {
            String line;
            boolean header = true;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (header) {
                    header = false;
                    continue;
                }

                String role = data[11].trim(); // Position
                String fullName = data[1] + " " + data[2]; // FirstName + LastName

                if (role.equals("HR Manager") ||
                    role.equals("HR Team Leader") ||
                    role.equals("Chief Executive Officer") ||
                    role.equals("Chief Operating Officer") ||
                    role.equals("Chief Finance Officer") ||
                    role.equals("Chief Marketing Officer") ||
                    role.equals("IT Operations and Systems") ||
                    role.equals("Accounting Head")) {

                    leaders.add(fullName);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading EmployeeData.csv for supervisors: " + e.getMessage());
        }

        for (String leader : leaders) {
            cmbSupervisor.addItem(leader);
        }

        // Validation → numeric only
        addNumericFilter(txtPhoneNumber);
        addNumericFilter(txtSSS);
        addNumericFilter(txtPhilHealth);
        addNumericFilter(txtTin);
        addNumericFilter(txtPagIbig);

        // Salary auto-calculation
        txtBasicSalary.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                calculateRates();
            }
        });
    }

    private void autoGenerateEmployeeID() {
        int maxID = 10000;
        try (BufferedReader br = new BufferedReader(new FileReader("src/com/csv/EmployeeData.csv"))) {
            String line;
            boolean header = true;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (header) {
                    header = false;
                    continue;
                }
                int currentID = Integer.parseInt(data[0].trim());
                if (currentID > maxID) {
                    maxID = currentID;
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading EmployeeData.csv: " + e.getMessage());
        }

        int newID = maxID + 1;
        txtEmpNum.setText(String.valueOf(newID));
    }

    private void calculateRates() {
        try {
            String basicSalaryStr = txtBasicSalary.getText().replace(",", "").trim();
            if (basicSalaryStr.isEmpty()) {
                txtHourlyRate.setText("");
                txtGrossSemiMonthlyRate.setText("");
                return;
            }

            double basicSalary = Double.parseDouble(basicSalaryStr);
            double hourlyRate = basicSalary / 168.0;
            double grossSemiMonthlyRate = basicSalary / 2.0;

            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
            nf.setMaximumFractionDigits(2);
            nf.setMinimumFractionDigits(2);

            txtHourlyRate.setText(nf.format(hourlyRate));
            txtGrossSemiMonthlyRate.setText(nf.format(grossSemiMonthlyRate));

        } catch (NumberFormatException e) {
            txtHourlyRate.setText("");
            txtGrossSemiMonthlyRate.setText("");
        }
    }

    private void addNumericFilter(JTextField field) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string.matches("[0-9\\-.,]*")) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text.matches("[0-9\\-.,]*")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }

    private void loadSelectedEmployee(String empID) {
        try (BufferedReader br = new BufferedReader(new FileReader("src/com/csv/EmployeeData.csv"))) {
            String line;
            boolean header = true;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (header) {
                    header = false;
                } else {
                    if (data[0].trim().equals(empID)) {
                        txtEmpNum.setText(data[0].trim());
                        txtFirstname.setText(data[1].trim());
                        txtLastname.setText(data[2].trim());
                        txtBirthday.setText(data[3].trim());
                        txtHourlyRate.setText(data[4].trim());
                        txtRiceSubsidy.setText(data[5].trim());
                        txtPhoneAllowance.setText(data[6].trim());
                        txtClothingAllowance.setText(data[7].trim());
                        cmbStatus.setSelectedItem(data[8].trim());
                        txtPosition.setText(data[9].trim());
                        txtBasicSalary.setText(data[10].trim());
                        txtPhoneNumber.setText(data[11].trim());
                        txtSSS.setText(data[12].trim());
                        txtPhilHealth.setText(data[13].trim());
                        txtTin.setText(data[14].trim());
                        txtPagIbig.setText(data[15].trim());
                        cmbSupervisor.setSelectedItem(data[16].trim());
                        txtGrossSemiMonthlyRate.setText(data[17].trim());
                        txtAddress.setText(data[18].trim());
                        break;
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading employee: " + e.getMessage());
        }
    }

    private void btnAddActionPerformed(ActionEvent evt) {
        String id = txtEmpNum.getText().trim();
        String firstname = txtFirstname.getText().trim();
        String lastname = txtLastname.getText().trim();
        String birthday = txtBirthday.getText().trim();
        String address = txtAddress.getText().trim();
        String phoneNumber = txtPhoneNumber.getText().trim();
        String sss = txtSSS.getText().trim();
        String philhealth = txtPhilHealth.getText().trim();
        String tin = txtTin.getText().trim();
        String pagibig = txtPagIbig.getText().trim();
        String status = cmbStatus.getSelectedItem().toString();
        String position = txtPosition.getText().trim();
        String immediate = cmbSupervisor.getSelectedItem() != null ? cmbSupervisor.getSelectedItem().toString() : "";
        String basicSalary = txtBasicSalary.getText().trim();
        String riceSubsidy = txtRiceSubsidy.getText().trim();
        String phoneAllowance = txtPhoneAllowance.getText().trim();
        String clothingAllowance = txtClothingAllowance.getText().trim();
        String grossSemi = txtGrossSemiMonthlyRate.getText().trim();
        String hourlyRate = txtHourlyRate.getText().trim();

        // Required field check
        if (firstname.isEmpty() || lastname.isEmpty() || birthday.isEmpty() || address.isEmpty() || phoneNumber.isEmpty() ||
            sss.isEmpty() || philhealth.isEmpty() || tin.isEmpty() || pagibig.isEmpty() || position.isEmpty() || basicSalary.isEmpty()) {

            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Write EmployeeData.csv
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("src/com/csv/EmployeeData.csv", true))) {
            bw.write(id + "," + firstname + "," + lastname + "," + birthday + "," + address + "," + phoneNumber + "," + sss + "," + philhealth + "," + tin + "," + pagibig + "," + status + "," + position + "," + immediate + "," + basicSalary + "," + riceSubsidy + "," + phoneAllowance + "," + clothingAllowance + "," + grossSemi + "," + hourlyRate);
            bw.newLine();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error writing to EmployeeData.csv: " + e.getMessage());
            return;
        }

        // Write LoginCredentials.csv
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("src/com/csv/LoginCredentials.csv", true))) {
            String username = firstname.substring(0, 1).toLowerCase() + lastname + id;
            String password = "welcome123";
            bw.write(password + "," + username + "," + firstname + "," + lastname + "," + position);
            bw.newLine();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error writing to LoginCredentials.csv: " + e.getMessage());
            return;
        }

        JOptionPane.showMessageDialog(this, "Record added successfully!");
        clearFields();
        autoGenerateEmployeeID();
    }

    private void btnClearActionPerformed(ActionEvent evt) {
        clearFields();
    }

    private void clearFields() {
        txtFirstname.setText("");
        txtLastname.setText("");
        txtBirthday.setText("");
        txtAddress.setText("");
        txtPhoneNumber.setText("");
        txtSSS.setText("");
        txtPhilHealth.setText("");
        txtTin.setText("");
        txtPagIbig.setText("");
        cmbStatus.setSelectedIndex(0);
        txtPosition.setText("");
        cmbSupervisor.setSelectedIndex(0);
        txtBasicSalary.setText("");
        txtRiceSubsidy.setText("");
        txtPhoneAllowance.setText("");
        txtClothingAllowance.setText("");
        txtGrossSemiMonthlyRate.setText("");
        txtHourlyRate.setText("");
    }
}
