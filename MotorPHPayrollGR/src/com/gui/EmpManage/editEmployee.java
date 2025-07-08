/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
/**
 *
 * @JEO
 */
package com.gui.EmpManage;

import com.gui.Home.User;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public class editEmployee extends JPanel {

    private User currentUser;
    private String editingEmpID;  // null = Add mode; non-null = Edit mode

    // Form fields
    private JTextField txtEmpNum, txtFirstname, txtLastname, txtAddress;
    private JTextField txtBasicSalary, txtPhoneNumber, txtRiceSubsidy;
    private JTextField txtPhoneAllowance, txtClothingAllowance;
    private JTextField txtGrossSemiMonthlyRate, txtHourlyRate;
    private JFormattedTextField txtSSS, txtPhilHealth, txtTin, txtPagIbig;
    private JComboBox<String> cmbStatus, cmbPosition, cmbSupervisor;
    private JDateChooser dateChooserBirthday;

    // Buttons
    private JButton btnAdd, btnUpdate, btnClear, btnBack;

    /**
     * "Juan", "Dela Cruz" -> "Dela Cruz, Juan"
     */
    private static String formatName(String first, String last) {
        return last.trim() + ", " + first.trim();
    }

    public editEmployee(User user) {
        this(user, null);
    }

    public editEmployee(User user, String editingEmpID) {
        this.currentUser = user;
        this.editingEmpID = editingEmpID;
        initComponents();           // ① create all controls, including cmbSupervisor
        initCustomFeatures();
        cmbSupervisor.removeAllItems();
        // gather every leader’s full name
        Set<String> supervisors = new TreeSet<>();
        try (CSVReader reader = new CSVReader(new FileReader("src/com/csv/EmployeeData.csv"))) {
            String[] row;
            reader.readNext();            // skip header
            while ((row = reader.readNext()) != null) {
                if (row.length > 9) {
                    String role = row[9].trim();
                    // match exactly the roles you allow as supervisors
                    if (role.equals("Chief Executive Officer")
                            || role.equals("Chief Operating Officer")
                            || role.equals("Chief Finance Officer")
                            || role.equals("Chief Marketing Officer")
                            || role.equals("IT Operations and Systems")
                            || role.equals("Accounting Head")
                            || role.equals("HR Manager")
                            || role.equals("HR Team Leader")) {
                        supervisors.add(formatName(row[1], row[2]));
                    }
                }
            }
        } catch (IOException | CsvException e) {
            // Tell the user something went wrong
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load the Immediate Supervisor list.\nPlease check your data file and try again.",
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        // refill the combo
        for (String name : supervisors) {
            cmbSupervisor.addItem(name);
        }

        if (editingEmpID == null) {
            autoGenerateEmployeeID();
        } else {
            loadSelectedEmployee(editingEmpID);
        }

        applyFieldRestrictions();
        configureButtonMode();
    }

    // Show/hide Add vs Update buttons
    private void configureButtonMode() {
        boolean isEdit = (editingEmpID != null);
        btnAdd.setVisible(!isEdit);
        btnUpdate.setVisible(isEdit);
    }

    // Restrict fields for non-leadership users
    private void applyFieldRestrictions() {
        if (!currentUser.isLeadership()) {
            txtAddress.setEnabled(true);
            txtPhoneNumber.setEnabled(true);
            // disable all others
            txtFirstname.setEnabled(false);
            txtLastname.setEnabled(false);
            dateChooserBirthday.setEnabled(false);
            txtSSS.setEnabled(false);
            txtPhilHealth.setEnabled(false);
            txtTin.setEnabled(false);
            txtPagIbig.setEnabled(false);
            cmbStatus.setEnabled(false);
            cmbPosition.setEnabled(false);
            cmbSupervisor.setEnabled(false);
            txtBasicSalary.setEnabled(false);
            txtRiceSubsidy.setEnabled(false);
            txtPhoneAllowance.setEnabled(false);
            txtClothingAllowance.setEnabled(false);
            txtGrossSemiMonthlyRate.setEnabled(false);
            txtHourlyRate.setEnabled(false);
        }
    }

    private void initComponents() {
        // initialize components
        txtEmpNum = new JTextField(10);
        txtFirstname = new JTextField(10);
        txtLastname = new JTextField(10);
        dateChooserBirthday = new JDateChooser();
        dateChooserBirthday.setDateFormatString("d-MMM-yy");
        txtAddress = new JTextField(20);
        txtPhoneNumber = new JTextField(10);
        txtSSS = new JFormattedTextField(createMaskFormatter("##-#######-#"));
        txtPhilHealth = new JFormattedTextField(createMaskFormatter("##-#########-#"));
        txtTin = new JFormattedTextField(createMaskFormatter("###-###-###-###"));
        txtPagIbig = new JFormattedTextField(createMaskFormatter("####-####-####"));
        cmbStatus = new JComboBox<>();
        cmbPosition = new JComboBox<>();
        cmbSupervisor = new JComboBox<>();
        txtBasicSalary = new JTextField(10);
        txtRiceSubsidy = new JTextField(6);
        txtPhoneAllowance = new JTextField(6);
        txtClothingAllowance = new JTextField(6);
        txtGrossSemiMonthlyRate = new JTextField(10);
        txtHourlyRate = new JTextField(10);

        // read-only fields
        txtEmpNum.setEditable(false);
        txtEmpNum.setBackground(Color.LIGHT_GRAY);
        txtGrossSemiMonthlyRate.setEditable(false);
        txtGrossSemiMonthlyRate.setBackground(Color.LIGHT_GRAY);
        txtHourlyRate.setEditable(false);
        txtHourlyRate.setBackground(Color.LIGHT_GRAY);

        setLayout(new BorderLayout());
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        // Row 0: Employee #, Last Name, First Name
        gbc.gridy = row;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Employee #:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtEmpNum, gbc);
        gbc.gridx = 2;
        formPanel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 3;
        formPanel.add(txtLastname, gbc);
        gbc.gridx = 4;
        formPanel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 5;
        formPanel.add(txtFirstname, gbc);

        // Row 1: Address, Birthday
        gbc.gridy = ++row;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        formPanel.add(txtAddress, gbc);
        gbc.gridwidth = 1;
        gbc.gridx = 4;
        formPanel.add(new JLabel("Birthday:"), gbc);
        gbc.gridx = 5;
        formPanel.add(dateChooserBirthday, gbc);

        // Row 2: SSS, Pag-Ibig, TIN, PhilHealth
        gbc.gridy = ++row;
        gbc.gridx = 0;
        formPanel.add(new JLabel("SSS #:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtSSS, gbc);
        gbc.gridx = 2;
        formPanel.add(new JLabel("Pag-Ibig #:"), gbc);
        gbc.gridx = 3;
        formPanel.add(txtPagIbig, gbc);
        gbc.gridx = 4;
        formPanel.add(new JLabel("TIN #:"), gbc);
        gbc.gridx = 5;
        formPanel.add(txtTin, gbc);

        gbc.gridy = ++row;
        gbc.gridx = 4;
        formPanel.add(new JLabel("Philhealth #:"), gbc);
        gbc.gridx = 5;
        formPanel.add(txtPhilHealth, gbc);

        // Row 4: Status and Phone
        gbc.gridy = ++row;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        formPanel.add(cmbStatus, gbc);
        gbc.gridx = 2;
        formPanel.add(new JLabel("Phone #:"), gbc);
        gbc.gridx = 3;
        formPanel.add(txtPhoneNumber, gbc);

        // Row 5: Supervisor and Position
        gbc.gridy = ++row;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Immediate Supervisor:"), gbc);
        gbc.gridx = 1;
        formPanel.add(cmbSupervisor, gbc);
        gbc.gridx = 2;
        formPanel.add(new JLabel("Position:"), gbc);
        gbc.gridx = 3;
        formPanel.add(cmbPosition, gbc);

        // Row 6: Basic Salary, Rice Subsidy, Phone Allowance
        gbc.gridy = ++row;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Basic Salary:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtBasicSalary, gbc);
        gbc.gridx = 2;
        formPanel.add(new JLabel("Rice Subsidy:"), gbc);
        gbc.gridx = 3;
        formPanel.add(txtRiceSubsidy, gbc);
        gbc.gridx = 4;
        formPanel.add(new JLabel("Phone Allowance:"), gbc);
        gbc.gridx = 5;
        formPanel.add(txtPhoneAllowance, gbc);

        // Row 7: Gross Semi-Monthly Rate, Clothing Allowance
        gbc.gridy = ++row;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Gross Semi-Monthly Rate:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtGrossSemiMonthlyRate, gbc);
        gbc.gridx = 2;
        formPanel.add(new JLabel("Clothing Allowance:"), gbc);
        gbc.gridx = 3;
        formPanel.add(txtClothingAllowance, gbc);

        // Row 8: Hourly Rate
        gbc.gridy = ++row;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Hourly Rate:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtHourlyRate, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnAdd = new JButton("Add");
        btnUpdate = new JButton("Update");
        btnClear = new JButton("Clear");
        btnBack = new JButton("Back");

        btnAdd.addActionListener(e -> handleAdd());
        btnUpdate.addActionListener(e -> handleUpdate());
        btnClear.addActionListener(e -> clearFields());
        btnBack.addActionListener(e -> SwingUtilities.getWindowAncestor(this).dispose());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnClear);
        btnPanel.add(btnBack);
        add(btnPanel, BorderLayout.SOUTH);
    }

    /**
     * Populates all fields from the CSV for the given empID
     */
    private MaskFormatter createMaskFormatter(String format) {
        try {
            MaskFormatter mf = new MaskFormatter(format);
            mf.setPlaceholderCharacter('_');
            return mf;
        } catch (ParseException e) {
            return null;
        }
    }

    private void initCustomFeatures() {
        cmbStatus.setModel(new DefaultComboBoxModel<>(new String[]{
            "Regular", "Probationary", "Leave", "Rehire", "Retired", "Resigned", "Terminated"}));
        cmbPosition.setModel(new DefaultComboBoxModel<>(new String[]{
            "Chief Executive Officer", "Chief Operating Officer", "Chief Finance Officer",
            "Chief Marketing Officer", "IT Operations and Systems", "Accounting Head",
            "HR Manager", "HR Team Leader", "HR Rank and File", "Account Manager",
            "Account Team Leader", "Account Rank and File", "Sales", "Supply Chain", "Customer Service"}));

        Set<String> leaders = new TreeSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/com/csv/EmployeeData.csv"))) {
            String line;
            boolean hdr = true;
            while ((line = br.readLine()) != null) {
                if (hdr) {
                    hdr = false;
                    continue;
                }
                String[] d = line.split(",", -1);
                if (d.length < 10) {
                    continue;
                }
                String role = d[9].trim();
                String name = formatName(d[1], d[2]);
                if (role.matches("HR Manager|HR Team Leader|Chief Executive Officer|Chief Operating Officer|Chief Finance Officer|Chief Marketing Officer|IT Operations and Systems|Accounting Head")) {
                    leaders.add(name);
                }
            }
        } catch (IOException e) {
            // ignore
        }
        leaders.forEach(cmbSupervisor::addItem);

        addNumericFilter(txtPhoneNumber);
        addNumericFilter(txtSSS);
        addNumericFilter(txtPhilHealth);
        addNumericFilter(txtTin);
        addNumericFilter(txtRiceSubsidy);
        addNumericFilter(txtBasicSalary);
        addNumericFilter(txtClothingAllowance);
        addNumericFilter(txtPhoneAllowance);
        txtBasicSalary.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                calculateRates();
            }
        });
    }

    private void handleAdd() {
        // 1) Read & trim all fields
        String id = txtEmpNum.getText().trim();
        String firstname = txtFirstname.getText().trim();
        String lastname = txtLastname.getText().trim();
        String birthday = "";
        if (dateChooserBirthday.getDate() != null) {
            birthday = new SimpleDateFormat("d-MMM-yy").format(dateChooserBirthday.getDate());
        }
        String hourlyRate = txtHourlyRate.getText().trim();
        String riceSubsidy = txtRiceSubsidy.getText().trim();
        String phoneAllowance = txtPhoneAllowance.getText().trim();
        String clothingAllowance = txtClothingAllowance.getText().trim();
        String status = cmbStatus.getSelectedItem().toString();
        String position = cmbPosition.getSelectedItem().toString();
        String basicSalary = txtBasicSalary.getText().trim();
        String phoneNumber = txtPhoneNumber.getText().trim();
        String sss = txtSSS.getText().trim();
        String philhealth = txtPhilHealth.getText().trim();
        String tin = txtTin.getText().trim();
        String pagibig = txtPagIbig.getText().trim();
        String supervisor = cmbSupervisor.getSelectedItem() == null
                ? ""
                : cmbSupervisor.getSelectedItem().toString();
        String grossSemi = txtGrossSemiMonthlyRate.getText().trim();
        String address = txtAddress.getText().trim();

        // 2) Simple required-field check
        if (firstname.isEmpty() || lastname.isEmpty() || birthday.isEmpty()
                || address.isEmpty() || phoneNumber.isEmpty()
                || sss.isEmpty() || philhealth.isEmpty()
                || tin.isEmpty() || pagibig.isEmpty()
                || basicSalary.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all required fields.",
                    "Input Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3) Append to EmployeeData.csv
        try (CSVWriter w = new CSVWriter(
                new FileWriter("src/com/csv/EmployeeData.csv", true))) {
            String[] record = new String[]{
                id,
                firstname,
                lastname,
                birthday,
                hourlyRate,
                riceSubsidy,
                phoneAllowance,
                clothingAllowance,
                status,
                position,
                basicSalary,
                phoneNumber,
                sss,
                philhealth,
                tin,
                pagibig,
                supervisor,
                grossSemi,
                address
            };
            w.writeNext(record);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error writing to EmployeeData.csv:\n" + ex.getMessage(),
                    "I/O Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // 4) Append to LoginCredentials.csv
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter("src/com/csv/LoginCredentials.csv", true))) {
            String username = id;
            String password = firstname.substring(0, 1).toLowerCase() + lastname;
            bw.write(String.join(",",
                    username,
                    password,
                    firstname,
                    lastname,
                    position,
                    "No" // Lock Out Status ← Always set to "No" by default
            ));
            bw.newLine();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error writing to LoginCredentials.csv:\n" + e.getMessage(),
                    "I/O Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 5) Append to EmpDataChangeLogs.csv
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter("src/com/csv/EmpDataChangeLogs.csv", true))) {
            String timestamp = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss"
            ).format(new Date());
            String userName = currentUser.getuFirstName() + " " + currentUser.getuLastName();
            bw.write(String.join(",",
                    "ADD",
                    userName,
                    id,
                    "ALL FIELDS",
                    "N/A",
                    "New Record",
                    timestamp,
                    "Approved",
                    "New employee added"
            ));
            bw.newLine();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error writing to EmpDataChangeLogs.csv:\n" + e.getMessage(),
                    "I/O Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 6) Create DTR file (if missing)
        File dtrDir = new File("src/com/csv/DTR");
        if (!dtrDir.exists()) {
            dtrDir.mkdirs();
        }

        File dtrFile = new File(dtrDir, id + ".csv");
        if (!dtrFile.exists()) {
            try (CSVWriter w = new CSVWriter(new FileWriter(dtrFile))) {
                w.writeNext(new String[]{
                    "Employee #",
                    "Date",
                    "Log In",
                    "Log Out",
                    "First Name",
                    "Last Name",
                    "DTR Approved By",
                    "DTR Approved Date",
                    "DTR Status",
                    "Payroll Approved By",
                    "Payroll Approved Date",
                    "Payroll Status"
                });
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error creating DTR file:\n" + e.getMessage(),
                        "I/O Error",
                        JOptionPane.ERROR_MESSAGE
                );
                // still continue—DTR can be re-created later
            }
        }

        // ── Append to SupervisorLists.csv ─────────────────────────────────────
        try (CSVWriter w = new CSVWriter(
                new FileWriter("src/com/csv/SupervisorLists.csv", true))) {
            // Columns: EmpID,FirstName,LastName,Department,SupervisorName
            w.writeNext(new String[]{
                id,
                firstname,
                lastname,
                position, // or department if you have one
                supervisor // the selected supervisor name
            });
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error updating SupervisorLists.csv:\n" + ex.getMessage(),
                    "I/O Error", JOptionPane.ERROR_MESSAGE);
        }

        // 7) Success!
        JOptionPane.showMessageDialog(this,
                "Record added successfully!",
                "Add Complete",
                JOptionPane.INFORMATION_MESSAGE);

        clearFields();
        autoGenerateEmployeeID();
    }

    private void handleUpdate() {
        // 1) Read & trim all fields
        String id = txtEmpNum.getText().trim();
        String firstname = txtFirstname.getText().trim();
        String lastname = txtLastname.getText().trim();
        String birthday = "";
        if (dateChooserBirthday.getDate() != null) {
            birthday = new SimpleDateFormat("d-MMM-yy")
                    .format(dateChooserBirthday.getDate());
        }
        String hourlyRate = txtHourlyRate.getText().trim();
        String riceSubsidy = txtRiceSubsidy.getText().trim();
        String phoneAllowance = txtPhoneAllowance.getText().trim();
        String clothingAllowance = txtClothingAllowance.getText().trim();
        String status = cmbStatus.getSelectedItem().toString();
        String position = cmbPosition.getSelectedItem().toString();
        String basicSalary = txtBasicSalary.getText().trim();
        String phoneNumber = txtPhoneNumber.getText().trim();
        String sss = txtSSS.getText().trim();
        String philhealth = txtPhilHealth.getText().trim();
        String tin = txtTin.getText().trim();
        String pagibig = txtPagIbig.getText().trim();
        String supervisor = cmbSupervisor.getSelectedItem() == null
                ? "" : cmbSupervisor.getSelectedItem().toString();
        String grossSemi = txtGrossSemiMonthlyRate.getText().trim();
        String address = txtAddress.getText().trim();

        // 2) Required‐field check
        if (firstname.isEmpty() || lastname.isEmpty() || birthday.isEmpty()
                || address.isEmpty() || phoneNumber.isEmpty()
                || sss.isEmpty() || philhealth.isEmpty()
                || tin.isEmpty() || pagibig.isEmpty()
                || basicSalary.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all required fields.",
                    "Input Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3) Overwrite EmployeeData.csv via OpenCSV
        try {
            List<String[]> rows;
            try (CSVReader r = new CSVReader(
                    new FileReader("src/com/csv/EmployeeData.csv"))) {
                rows = r.readAll();
            }

            try (CSVWriter w = new CSVWriter(
                    new FileWriter("src/com/csv/EmployeeData.csv"))) {
                for (int i = 0; i < rows.size(); i++) {
                    String[] row = rows.get(i);
                    if (i == 0) {
                        w.writeNext(row);  // header
                    } else if (row[0].equals(editingEmpID)) {
                        // build updated row
                        String[] updated = {
                            id,
                            firstname,
                            lastname,
                            birthday,
                            hourlyRate,
                            riceSubsidy,
                            phoneAllowance,
                            clothingAllowance,
                            status,
                            position,
                            basicSalary,
                            phoneNumber,
                            sss,
                            philhealth,
                            tin,
                            pagibig,
                            supervisor,
                            grossSemi,
                            address
                        };
                        w.writeNext(updated);
                    } else {
                        w.writeNext(row);
                    }
                }
            }
        } catch (IOException | CsvException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error updating data:\n" + ex.getMessage(),
                    "I/O Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3.5) Also update the Department in LoginCredentials.csv
        try {
            List<String[]> creds;
            try (CSVReader r = new CSVReader(new FileReader("src/com/csv/LoginCredentials.csv"))) {
                creds = r.readAll();
            }
            try (CSVWriter w = new CSVWriter(new FileWriter("src/com/csv/LoginCredentials.csv"))) {
                for (int i = 0; i < creds.size(); i++) {
                    String[] row = creds.get(i);
                    if (i == 0) {
                        w.writeNext(row);
                    } else if (row[0].equals(id)) {
                        row[4] = position;
                        w.writeNext(row);
                    } else {
                        w.writeNext(row);
                    }
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error updating LoginCredentials.csv:\n" + ex.getMessage(),
                    "I/O Error", JOptionPane.ERROR_MESSAGE);
        }

        // ── Update SupervisorLists.csv ────────────────────────────────────────
        try {
            List<String[]> rows = new ArrayList<>();
            try (CSVReader r = new CSVReader(
                    new FileReader("src/com/csv/SupervisorLists.csv"))) {
                rows = r.readAll();
            }

            try (CSVWriter w = new CSVWriter(
                    new FileWriter("src/com/csv/SupervisorLists.csv"))) {
                for (int i = 0; i < rows.size(); i++) {
                    String[] row = rows.get(i);
                    if (i == 0) {
                        // header
                        w.writeNext(row);
                    } else if (row[0].equals(editingEmpID)) {
                        // replace with updated data
                        w.writeNext(new String[]{
                            editingEmpID,
                            firstname,
                            lastname,
                            status, // or position/department
                            supervisor
                        });
                    } else {
                        w.writeNext(row);
                    }
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error updating SupervisorLists.csv:\n" + ex.getMessage(),
                    "I/O Error", JOptionPane.ERROR_MESSAGE);
        }

        // 4) Append an UPDATE log
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter("src/com/csv/EmpDataChangeLogs.csv", true))) {
            String timestamp = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss"
            ).format(new Date());
            String userName
                    = currentUser.getuFirstName() + " " + currentUser.getuLastName();

            bw.write(String.join(",",
                    "UPDATE",
                    userName,
                    id,
                    "ALL FIELDS",
                    "N/A",
                    "Record modified",
                    timestamp,
                    "Approved",
                    "Employee data updated"
            ));
            bw.newLine();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error writing change log:\n" + e.getMessage(),
                    "I/O Error",
                    JOptionPane.ERROR_MESSAGE);
            // but we can continue
        }

        // 5) Success!
        JOptionPane.showMessageDialog(this,
                "Employee updated successfully!",
                "Update Complete",
                JOptionPane.INFORMATION_MESSAGE);

        // 6) Close this panel/window
        SwingUtilities.getWindowAncestor(this).dispose();
    }

    private void autoGenerateEmployeeID() {
        int maxId = 0;
        try (
                CSVReader reader = new CSVReader(new FileReader("src/com/csv/EmployeeData.csv"))) {
            // skip header
            reader.readNext();

            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length > 0 && !row[0].trim().isEmpty()) {
                    try {
                        int id = Integer.parseInt(row[0].trim());
                        maxId = Math.max(maxId, id);
                    } catch (NumberFormatException ignored) {
                        // non‐numeric ID, skip
                    }
                }
            }
        } catch (IOException | CsvException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error reading EmployeeData.csv: " + e.getMessage(),
                    "I/O Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
        // Set the next ID
        txtEmpNum.setText(String.valueOf(maxId + 1));
    }

    private void loadSelectedEmployee(String empID) {
        try (CSVReader reader = new CSVReader(new FileReader("src/com/csv/EmployeeData.csv"))) {
            // skip header
            reader.readNext();

            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length > 0 && empID.equals(row[0].trim())) {
                    // ID, First, Last
                    txtEmpNum.setText(row[0].trim());
                    if (row.length > 1) {
                        txtFirstname.setText(row[1].trim());
                    }
                    if (row.length > 2) {
                        txtLastname.setText(row[2].trim());
                    }

                    // Birthday (idx 3)
                    if (row.length > 3 && !row[3].trim().isEmpty()) {
                        try {
                            Date d = new SimpleDateFormat("d-MMM-yy").parse(row[3].trim());
                            dateChooserBirthday.setDate(d);
                        } catch (Exception ex) {
                            dateChooserBirthday.setDate(null);
                        }
                    } else {
                        dateChooserBirthday.setDate(null);
                    }

                    // Hourly, Rice, PhoneAllow, ClothAllow
                    if (row.length > 4) {
                        txtHourlyRate.setText(row[4].trim());
                    }
                    if (row.length > 5) {
                        txtRiceSubsidy.setText(row[5].trim());
                    }
                    if (row.length > 6) {
                        txtPhoneAllowance.setText(row[6].trim());
                    }
                    if (row.length > 7) {
                        txtClothingAllowance.setText(row[7].trim());
                    }

                    // Status (idx 8), Position (9)
                    if (row.length > 8) {
                        cmbStatus.setSelectedItem(row[8].trim());
                    }
                    if (row.length > 9) {
                        cmbPosition.setSelectedItem(row[9].trim());
                    }

                    // BasicSalary (10), Phone# (11)
                    if (row.length > 10) {
                        txtBasicSalary.setText(row[10].trim());
                    }
                    if (row.length > 11) {
                        txtPhoneNumber.setText(row[11].trim());
                    }

                    // SSS (12), PhilHealth (13), TIN (14), PagIbig (15)
                    if (row.length > 12) {
                        txtSSS.setText(row[12].trim());
                    }
                    if (row.length > 13) {
                        txtPhilHealth.setText(row[13].trim());
                    }
                    if (row.length > 14) {
                        txtTin.setText(row[14].trim());
                    }
                    if (row.length > 15) {
                        txtPagIbig.setText(row[15].trim());
                    }

                    // Supervisor (16), GrossSemi (17), Address (18)
                    if (row.length > 16) {
                        cmbSupervisor.setSelectedItem(row[16].trim());
                    }
                    if (row.length > 17) {
                        txtGrossSemiMonthlyRate.setText(row[17].trim());
                    }
                    if (row.length > 18) {
                        txtAddress.setText(row[18].trim());
                    }

                    break;
                }
            }
        } catch (IOException | CsvException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error loading employee: " + ex.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void calculateRates() {
        try {
            double bs = Double.parseDouble(txtBasicSalary.getText().replace(",", ""));
            double hr = bs / 168.0, semi = bs / 2.0;
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);
            txtHourlyRate.setText(nf.format(hr));
            txtGrossSemiMonthlyRate.setText(nf.format(semi));
        } catch (Exception e) {
            txtHourlyRate.setText("");
            txtGrossSemiMonthlyRate.setText("");
        }
    }

    private void addNumericFilter(JTextField f) {
        ((AbstractDocument) f.getDocument()).setDocumentFilter(new DocumentFilter() {
            public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string.matches("[0-9\\-.,]*")) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text.matches("[0-9\\-.,]*")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }

    private void clearFields() {
        txtFirstname.setText("");
        txtLastname.setText("");
        dateChooserBirthday.setDate(null);
        txtAddress.setText("");
        txtPhoneNumber.setText("");
        txtSSS.setText("");
        txtPhilHealth.setText("");
        txtTin.setText("");
        txtPagIbig.setText("");
        cmbStatus.setSelectedIndex(0);
        cmbPosition.setSelectedIndex(0);
        cmbSupervisor.setSelectedIndex(0);
        txtBasicSalary.setText("");
        txtRiceSubsidy.setText("");
        txtPhoneAllowance.setText("");
        txtClothingAllowance.setText("");
        txtGrossSemiMonthlyRate.setText("");
        txtHourlyRate.setText("");
    }
}
