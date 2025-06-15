/**
 *
 * @author ongoj & Miles
 */
/**
 * PayrollManagement.java - Payroll Approval with Status Controls and Net Pay Display
 */
package com.gui;

import com.payroll.MotorPHPayrollG3;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class PayrollManagement extends JFrame {
    private User currentUser;
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JComboBox<String> payPeriodComboBox;
    private LocalDate startDate, endDate;

    public PayrollManagement(User currentUser) throws CsvValidationException {
        this.currentUser = currentUser;
        setTitle("Payroll Management System");
        setSize(1400, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        payPeriodComboBox = createPayPeriodComboBoxFromCSV();
        JButton approveAllButton = new JButton("Approve All Payrolls");
        JButton calcButton = new JButton("Calculate Pay");
        JButton reportButton = new JButton("Generate Report");
        JButton auditTrailButton = new JButton("View Audit Trail");
        JTextField searchField = new JTextField(15);

        String[] columns = {"EmpID", "First Name", "Last Name", "DTR Status", "DTR Approved Date", "Payroll Status", "Payroll Approved Date", "NetPay", "Payslip Ready", "Generate Payslip"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) {
                return col == 8 || col == 9; // Dropdown and export only
            }
        };
        employeeTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        employeeTable.setRowSorter(sorter);

        employeeTable.getColumn("Payslip Ready").setCellEditor(new DefaultCellEditor(new JComboBox<>(new String[]{"Approved", "Declined", "Pending", "Incomplete"})));
        employeeTable.getColumn("Generate Payslip").setCellRenderer(new ButtonRenderer());
        employeeTable.getColumn("Generate Payslip").setCellEditor(new ButtonEditor(new JCheckBox()));

        PayrollManagementHelper helper = new PayrollManagementHelper(employeeTable, payPeriodComboBox, currentUser);

        approveAllButton.addActionListener(e -> helper.approveAllPayrolls());
        calcButton.addActionListener(e -> calculateNetPays());
        reportButton.addActionListener(e -> helper.exportToCSVWithChooser());
        auditTrailButton.addActionListener(e -> helper.showPayrollAuditTrail());

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void filter() {
                String text = searchField.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        topPanel.add(new JLabel("Pay Period:"));
        topPanel.add(payPeriodComboBox);
        topPanel.add(calcButton);
        topPanel.add(approveAllButton);
        topPanel.add(reportButton);
        topPanel.add(auditTrailButton);
        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);

        loadTableFromCSV();
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(employeeTable), BorderLayout.CENTER);
    }

    private void calculateNetPays() {
        String selected = (String) payPeriodComboBox.getSelectedItem();
        if (selected == null || selected.equals("No pay periods found")) {
            JOptionPane.showMessageDialog(this, "Please select a pay period first.");
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

            startDate = LocalDate.of(year, month.getValue(), startDay);
            endDate = LocalDate.of(year, month.getValue(), endDay);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error parsing selected pay period: " + e.getMessage());
            return;
        }

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String dtrStatus = tableModel.getValueAt(i, 3).toString();
            if (!dtrStatus.equalsIgnoreCase("Approved")) continue;
            String empId = tableModel.getValueAt(i, 0).toString();
            Object[] result = MotorPHPayrollG3.runPayrollSearch(startDate, endDate, empId);
            if (result != null && result.length == 22) {
                tableModel.setValueAt(String.valueOf(result[21]), i, 7);
            }
        }
    }

    private JComboBox<String> createPayPeriodComboBoxFromCSV() {
        String csvFile = "src/com/csv/DTR/" + currentUser.getuEmpId() + ".csv";
        Set<String> payPeriods = new LinkedHashSet<>();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("M/d/yyyy");
        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            String[] line; reader.readNext();
            while ((line = reader.readNext()) != null) {
                LocalDate d = LocalDate.parse(line[1].trim(), df);
                int day = d.getDayOfMonth();
                YearMonth ym = YearMonth.from(d);
                String m = ym.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                int y = ym.getYear();
                int end = ym.lengthOfMonth();
                if (day <= 15) payPeriods.add("1-15 " + m + " " + y + " (Payday: " + m + " " + end + ")");
                else payPeriods.add("16-" + end + " " + m + " " + y + " (Payday: " + ym.plusMonths(1).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " 15)");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error reading pay periods: " + e.getMessage());
        }
        return new JComboBox<>(payPeriods.toArray(new String[0]));
    }

    private void loadTableFromCSV() throws CsvValidationException {
        try (CSVReader reader = new CSVReader(new FileReader("src/com/csv/DTR/DTRPayrollStatus.csv"))) {
            String[] row; reader.readNext();
            while ((row = reader.readNext()) != null) {
                String[] fullRow = Arrays.copyOf(row, 10);
                fullRow[7] = ""; // NetPay
                fullRow[8] = "Pending"; // Payslip ready dropdown
                fullRow[9] = "Generate Payslip"; // Button label
                tableModel.addRow(fullRow);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading table: " + e.getMessage());
        }
    }

    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            setText((value == null) ? "" : value.toString()); return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int row;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }
        public Component getTableCellEditorComponent(JTable table, Object val, boolean isSelected, int row, int col) {
            this.label = val == null ? "" : val.toString();
            this.button.setText(label); this.isPushed = true; this.row = row;
            return button;
        }
        public Object getCellEditorValue() {
            if (isPushed && "Generate Payslip".equalsIgnoreCase(label)) {
                String empId = tableModel.getValueAt(row, 0).toString();
                String payrollStatus = String.valueOf(tableModel.getValueAt(row, 5));
                String netPay = String.valueOf(tableModel.getValueAt(row, 7));

                if (!"Approved".equalsIgnoreCase(payrollStatus)) {
                    JOptionPane.showMessageDialog(null, "Cannot generate payslip. Please approve payroll first for EmpID: " + empId);
                } else if (netPay == null || netPay.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "NetPay is missing. Please calculate payroll first for EmpID: " + empId);
                } else {
                    JOptionPane.showMessageDialog(null, "This would now call PaySlip.java to generate PDF for EmpID: " + empId);
                }
            }
            isPushed = false;
            return label;
        }
    }
}