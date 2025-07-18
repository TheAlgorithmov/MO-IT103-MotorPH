package com.gui.EmpManage;

import com.gui.Home.User;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;

public class AttendanceManagement extends JFrame {

    private static final String SUPERVISOR_LIST_CSV = "src/com/csv/SupervisorLists.csv";
    private static final String DTR_STATUS_CSV = "src/com/csv/DTR/DTRPayrollStatus.csv";
    private static final String DTR_CHANGE_LOGS = "src/com/csv/DTR/DTRChangeLogs.csv";
    private static final String DTR_FOLDER = "src/com/csv/DTR/";

    private final User currentUser;
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> employeeSelector;
    private JDateChooser fromDatePicker, toDatePicker;
    private JLabel nameLabel; // displays Last, First of selected employee

    private static String clean(String val) {
        return (val == null || "-".equals(val.trim())) ? "" : val.trim();
    }

    private class TimeInputVerifier extends InputVerifier {

        private static final String TIME_PATTERN = "^(0?[1-9]|1[0-2]):[0-5][0-9]\\s?(AM|PM)$";

        @Override
        public boolean verify(JComponent input) {
            String text = ((JTextField) input).getText().trim().toUpperCase();
            return text.matches(TIME_PATTERN);
        }
    }

    private boolean isValidTimeFormat(String time) {
        return time.matches("^(0?[1-9]|1[0-2]):[0-5][0-9]\\s?(AM|PM)$");
    }

    public AttendanceManagement(User user) {
        this.currentUser = user;
        setTitle("Attendance Management - " + user.getuPosition());
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();

        if (isSupervisor()) {
            loadEmployeeList();
            // ensure label reflects initial selection
            updateNameLabel();
        } else {
            loadAttendanceData(currentUser.getuEmpId());
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // ─── HEADER ─────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        JLabel logoLbl = new JLabel(new ImageIcon(
                new ImageIcon("src/com/gui/images/LoginIcons/motorph.jpg")
                        .getImage().getScaledInstance(150, 120, Image.SCALE_SMOOTH)));
        logoLbl.setPreferredSize(new Dimension(150, 120));
        header.add(logoLbl, BorderLayout.WEST);
        JLabel title = new JLabel("Attendance Management", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.add(title, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // ─── FILTER PANEL ───────────────────────────────────────────────────────
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        fromDatePicker = new JDateChooser();
        toDatePicker = new JDateChooser();
        fromDatePicker.setPreferredSize(new Dimension(125, 30));
        toDatePicker.setPreferredSize(new Dimension(125, 30));
        filter.add(new JLabel("From:"));
        filter.add(fromDatePicker);
        filter.add(new JLabel("To:"));
        filter.add(toDatePicker);

        JButton viewStatus = new JButton("View DTR Status");
        viewStatus.addActionListener(e -> viewDTRStatus());
        filter.add(viewStatus);

        JButton viewBtn = new JButton("View Attendance");
        viewBtn.addActionListener(e -> {
            String emp = isSupervisor()
                    ? (String) employeeSelector.getSelectedItem()
                    : currentUser.getuEmpId();
            loadAttendanceData(emp);
        });
        filter.add(viewBtn);

        if (isSupervisor()) {
            // name label
            nameLabel = new JLabel();
            filter.add(new JLabel("Employee Name:"));
            filter.add(nameLabel);

            // employee ID dropdown
            employeeSelector = new JComboBox<>();
            filter.add(new JLabel("Employee ID:"));
            filter.add(employeeSelector);

            // update on change
            employeeSelector.addActionListener(e -> {
                updateNameLabel();
                loadAttendanceData((String) employeeSelector.getSelectedItem());
            });
        }
        add(filter, BorderLayout.BEFORE_FIRST_LINE);

        // ─── TABLE ───────────────────────────────────────────────────────────────
        String[] cols = {"Date", "Clock In", "Clock Out", "Duration", "Late", "Overtime", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                if (!isSupervisor() || !(col == 1 || col == 2)) {
                    return false;
                }
                String sel = (String) employeeSelector.getSelectedItem();
                return !currentUser.getuEmpId().equals(sel);
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        DefaultTableCellRenderer paddedRenderer = new DefaultTableCellRenderer();
        paddedRenderer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // top, left, bottom, right
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(paddedRenderer);
        }

        // ─── Enforce hh:mm AM/PM input for Clock In/Out ───────────────
        InputVerifier timeVerifier = new TimeInputVerifier();
        JTextField timeField = new JTextField();
        timeField.setPreferredSize(new Dimension(100, 24)); // Wider editor field
        timeField.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Optional for better visibility

        DefaultCellEditor timeEditor = new DefaultCellEditor(timeField) {
            @Override
            public boolean stopCellEditing() {
                JTextField tf = (JTextField) getComponent();
                if (!timeVerifier.verify(tf)) {
                    JOptionPane.showMessageDialog(null,
                            "Please enter time in hh:mm AM/PM format (e.g., 08:30 AM)",
                            "Invalid Time", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                return super.stopCellEditing();
            }
        };

        // Set time editor on Clock In (index 1) and Clock Out (index 2) columns
        table.getColumnModel().getColumn(1).setCellEditor(timeEditor);
        table.getColumnModel().getColumn(2).setCellEditor(timeEditor);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ─── PERSIST INLINE EDITS ───────────────────────────────────────────────
        tableModel.addTableModelListener(e -> {
            if (e.getType() != TableModelEvent.UPDATE) {
                return;
            }
            int row = e.getFirstRow(), col = e.getColumn();
            if (col < 0 || col > 2) {
                return; // only date/in/out
            }
            String empId = isSupervisor()
                    ? (String) employeeSelector.getSelectedItem()
                    : currentUser.getuEmpId();
            String date = tableModel.getValueAt(row, 0).toString();
            String newValue = tableModel.getValueAt(row, col).toString();
            int csvCol = (col == 0 ? 1 : (col == 1 ? 2 : 3)); // CSV: 0=EmpID,1=Date,2=LogIn,3=LogOut
            try {
                updateCsvCell(empId, date, csvCol, newValue);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to save change:\n" + ex.getMessage(),
                        "I/O Error", JOptionPane.ERROR_MESSAGE
                );
            }
        });

        // ─── MANUAL ENTRY & SAVE BUTTON ────────────────────────────────────────
        if (isSupervisor()) {
            JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton save = new JButton("Save Changes");
            save.addActionListener(e -> {
                // just trigger model-write for all rows
                for (int r = 0; r < tableModel.getRowCount(); r++) {
                    for (int c = 0; c <= 2; c++) {
                        String emp = (String) employeeSelector.getSelectedItem();
                        String date = tableModel.getValueAt(r, 0).toString();
                        String val = tableModel.getValueAt(r, c).toString();
                        try {
                            updateCsvCell(emp, date, 1 + c, val);
                        } catch (IOException ignored) {
                        }
                    }
                }
                JOptionPane.showMessageDialog(this,
                        "All edits saved.", "Saved", JOptionPane.INFORMATION_MESSAGE);
            });

            south.add(save);
            add(south, BorderLayout.SOUTH);
        }

        // ─── SIDE BUTTONS ───────────────────────────────────────────────────────
        JPanel side = new JPanel(new GridLayout(0, 1, 5, 5));
        side.add(new JButton(new AbstractAction("Apply for Leave") {
            public void actionPerformed(ActionEvent e) {
                applyLeave();
            }
        }));
        if (isSupervisor()) {
            side.add(new JButton(new AbstractAction("Approve Leave Requests") {
                public void actionPerformed(ActionEvent e) {
                    approveLeave();
                }
            }));

            side.add(new JButton(new AbstractAction("Manual Time Entry") {
                public void actionPerformed(ActionEvent e) {
                    String sel = (String) employeeSelector.getSelectedItem();
                    if (currentUser.getuEmpId().equals(sel)) {
                        JOptionPane.showMessageDialog(AttendanceManagement.this,
                                "You're not allowed to manually edit your own Timesheet. Contact your Supervisor/Manager.",
                                "Access Denied", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    openManualDTRDialog();
                }
            }));

            side.add(new JButton(new AbstractAction("Validate Timesheet") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String sel = (String) employeeSelector.getSelectedItem();
                    if (currentUser.getuEmpId().equals(sel)) {
                        JOptionPane.showMessageDialog(AttendanceManagement.this,
                                "You're not allowed to validate your own Timesheet. Contact your Supervisor/Manager.",
                                "Access Denied", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    validateTimesheet();
                }
            }));
        }
        side.add(new JButton(new AbstractAction("Export Attendance") {
            public void actionPerformed(ActionEvent e) {
                exportAttendance();
            }
        }));
        side.add(new JButton(new AbstractAction("View Change Logs") {
            public void actionPerformed(ActionEvent e) {
                viewChangeLogs();
            }
        }));
        side.add(new JButton(new AbstractAction("Close") {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        }));
        add(side, BorderLayout.EAST);
    }

    private boolean isSupervisor() {
        String me = currentUser.getuLastName() + ", " + currentUser.getuFirstName();
        try (CSVReader r = new CSVReader(new FileReader(SUPERVISOR_LIST_CSV))) {
            r.readNext();
            String[] row;
            while ((row = r.readNext()) != null) {
                if (row[4].trim().equalsIgnoreCase(me)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void loadEmployeeList() {
        Set<String> supervised = new LinkedHashSet<>();
        supervised.add(currentUser.getuEmpId());
        String myName = currentUser.getuLastName().trim() + ", " + currentUser.getuFirstName().trim();
        try (CSVReader reader = new CSVReader(new FileReader(SUPERVISOR_LIST_CSV))) {
            reader.readNext();
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length > 4 && row[4].trim().equalsIgnoreCase(myName)) {
                    supervised.add(row[0].trim());
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading SupervisorLists.csv:\n" + ex.getMessage(),
                    "Load Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        employeeSelector.removeAllItems();
        for (String empId : supervised) {
            employeeSelector.addItem(empId);
        }
        if (employeeSelector.getItemCount() > 0) {
            employeeSelector.setSelectedIndex(0);
            updateNameLabel();
            loadAttendanceData(employeeSelector.getItemAt(0));
        }
    }

    private void updateNameLabel() {
        String empId = (String) employeeSelector.getSelectedItem();
        String fn = "", ln = "";
        try (CSVReader r = new CSVReader(new FileReader("src/com/csv/EmployeeData.csv"))) {
            r.readNext();
            String[] row;
            while ((row = r.readNext()) != null) {
                if (row[0].equals(empId)) {
                    fn = row[1];
                    ln = row[2];
                    break;
                }
            }
        } catch (Exception ignored) {
        }
        nameLabel.setText(ln + ", " + fn);
    }

    private void loadAttendanceData(String empId) {

        File f = new File("src/com/csv/DTR/" + empId + ".csv");
        if (!f.exists()) {
            tableModel.setRowCount(0);
            return;
        }

        /* ── read the employee’s CSV into a map keyed by Date ─────────── */
        Map<String, String[]> map = new HashMap<>();
        try (CSVReader r = new CSVReader(new FileReader(f))) {
            r.readNext();                       // skip header
            String[] row;
            while ((row = r.readNext()) != null) {
                if (row.length >= 4) {
                    map.put(row[1].trim(), row);   // key = Date “M/d/yyyy”
                }
            }
        } catch (IOException | CsvValidationException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error reading " + f.getName() + ":\n" + ex.getMessage(),
                    "I/O Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        /* ── figure out the date range ────────────────────────────────── */
        LocalDate start = fromDatePicker.getDate() != null
                ? fromDatePicker.getDate().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate()
                : LocalDate.now();
        LocalDate end = toDatePicker.getDate() != null
                ? toDatePicker.getDate().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate()
                : start;
        if (end.isBefore(start)) {
            JOptionPane.showMessageDialog(this,
                    "“To” date must be ≥ “From” date.", "Date Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        /* ── paint the table ──────────────────────────────────────────── */
        tableModel.setRowCount(0);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("M/d/yyyy");
        SimpleDateFormat tf = new SimpleDateFormat("h:mm a");

        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            String ds = d.format(df);
            boolean weekend = d.getDayOfWeek() == DayOfWeek.SATURDAY
                    || d.getDayOfWeek() == DayOfWeek.SUNDAY;

            if (map.containsKey(ds)) {
                String[] p = map.get(ds);

                /* early-out when both time cells are blank */
                if (p[2].trim().isEmpty() && p[3].trim().isEmpty()) {
                    tableModel.addRow(new Object[]{ds, "", "", "", "", "", "Absent"});
                    continue;
                }

                try {
                    Date inT = tf.parse(p[2].trim());
                    Date outT = tf.parse(p[3].trim());

                    long mins = (outT.getTime() - inT.getTime()) / 60000;
                    if (mins < 0) {
                        mins += 24 * 60;          // overnight shift
                    }
                    long workMins = Math.max(0, mins - 60);   // minus lunch
                    long lateMins = Math.max(0,
                            (inT.after(tf.parse("8:45 AM")))
                            ? (inT.getTime() - tf.parse("8:45 AM").getTime()) / 60000
                            : 0);
                    long otMins = Math.max(0, workMins - 8 * 60);

                    tableModel.addRow(new Object[]{
                        ds,
                        p[2].trim(),
                        p[3].trim(),
                        String.format("%.2f hrs", workMins / 60.0),
                        lateMins > 0 ? lateMins + " mins" : "",
                        otMins > 0 ? String.format("%.2f hrs", otMins / 60.0) : "",
                        "Present"
                    });
                } catch (Exception ignore) {
                    /* malformed time – skip row */ }
            } else if (!weekend && !d.isAfter(LocalDate.now())) {
                tableModel.addRow(new Object[]{ds, "", "", "", "", "", "Absent"});
            }
        }
    }

    /**
     * Pops up *payroll-status* (not DTR logs) for the selected employee,
     * showing only the approval columns.
     */
    private void viewDTRStatus() {
        // 1) Make sure they’ve picked a date range
        if (fromDatePicker.getDate() == null || toDatePicker.getDate() == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a date range first.",
                    "Date Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2) Figure out which employee
        String emp = isSupervisor()
                ? (String) employeeSelector.getSelectedItem()
                : currentUser.getuEmpId();

        // 3) Open their DTR CSV
        File f = new File(DTR_FOLDER + emp + ".csv");
        if (!f.exists()) {
            JOptionPane.showMessageDialog(this,
                    "No DTR file found for " + emp + ".\nUse Manual Entry first.",
                    "No Records", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 4) Build a model for the 9 columns you care about
        String[] cols = {
            "Employee #", "First Name", "Last Name",
            "DTR Approved By", "DTR Approved Date", "DTR Status",
            "Payroll Approved By", "Payroll Approved Date", "Payroll Status"
        };
        DefaultTableModel model = new DefaultTableModel(cols, 0);

        // 5) Parse & filter by date range
        LocalDate start = fromDatePicker.getDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = toDatePicker.getDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("M/d/yyyy");

        try (CSVReader reader = new CSVReader(new FileReader(f))) {
            String[] row = reader.readNext();  // skip header
            while ((row = reader.readNext()) != null) {
                // row[1] = Date
                LocalDate d = LocalDate.parse(row[1], df);
                if (d.isBefore(start) || d.isAfter(end)) {
                    continue;
                }

                // row indices:
                //   0=Employee#, 1=Date, 2=Log In, 3=Log Out,
                //   4=First Name, 5=Last Name,
                //   6=DTR Approved By, 7=DTR Approved Date, 8=DTR Status,
                //   9=Payroll Approved By, 10=Payroll Approved Date, 11=Payroll Status
                model.addRow(new Object[]{
                    row[0], row[4], row[5],
                    row[6], row[7], row[8],
                    row[9], row[10], row[11]
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error reading DTR for " + emp + ":\n" + ex.getMessage(),
                    "I/O Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 6) If nothing to show
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "No approval records found for “" + emp + "”.",
                    "No Records", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 7) Display in a dialog
        JTable tbl = new JTable(model);
        JScrollPane sp = new JScrollPane(tbl);
        sp.setPreferredSize(new Dimension(800, 300));
        JOptionPane.showMessageDialog(this, sp,
                "Approval Status for " + emp, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Updates a single cell in DTR/{empId}.csv, which now has columns:
     * 0=Employee #, 1=Date, 2=Log In, 3=Log Out, 4=First Name, 5=Last Name,
     * 6=DTR Approved By, 7=DTR Approved Date, 8=DTR Status, 9=Payroll Approved
     * By, 10=Payroll Approved Date, 11=Payroll Status
     */
    private void updateCsvCell(String empId, String date,
            int csvCol, String newValue) throws IOException {

        File f = new File(DTR_FOLDER + empId + ".csv");
        List<String[]> all = new ArrayList<>();

        /* 1 ─ Read the whole file (if it already exists) */
        if (f.exists()) {
            try (CSVReader r = new CSVReader(new FileReader(f))) {
                String[] row;
                while ((row = r.readNext()) != null) {
                    all.add(row);
                }
            } catch (CsvValidationException ex) {
                Logger.getLogger(AttendanceManagement.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }

        /* 2 ─ Ensure header is present */
        String[] header = {
            "Employee #", "Date", "Log In", "Log Out",
            "First Name", "Last Name",
            "DTR Approved By", "DTR Approved Date", "DTR Status",
            "Payroll Approved By", "Payroll Approved Date", "Payroll Status"
        };
        if (all.isEmpty()) {
            all.add(header);
        }

        /* 3 ─ Fetch employee name only once */
        String fn = "", ln = "";
        try (CSVReader rex = new CSVReader(
                new FileReader("src/com/csv/EmployeeData.csv"))) {
            rex.readNext();                         // skip header
            String[] rrow;
            while ((rrow = rex.readNext()) != null) {
                if (rrow[0].trim().equals(empId)) {
                    fn = rrow[1].trim();
                    ln = rrow[2].trim();
                    break;
                }
            }
        } catch (Exception ign) {
            /* ignore */ }

        /* 4 ─ Try to update an *existing* row for this date */
        boolean found = false;
        for (int i = 1; i < all.size(); i++) {
            String[] row = all.get(i);

            if (row.length < header.length) {                 // pad
                row = Arrays.copyOf(row, header.length);
            }
            if (row[1].equals(date)) {
                row[4] = clean(row[4].isEmpty() ? fn : row[4]);
                row[5] = clean(row[5].isEmpty() ? ln : row[5]);
                row[csvCol] = clean(newValue);
                row[2] = clean(row[2]);
                row[3] = clean(row[3]);
                all.set(i, row);
                found = true;
                break;
            }
        }

        /* 5 ─ OPTION A:  **only** append a new row when the value is real
           (skip if it is just the "-" placeholder)                      */
        if (!found && !newValue.trim().isEmpty()) {     // only for real values
            String[] newRow = new String[header.length];
            newRow[0] = empId;
            newRow[1] = date;
            newRow[2] = (csvCol == 2) ? newValue : "";
            newRow[3] = (csvCol == 3) ? newValue : "";
            newRow[4] = fn;
            newRow[5] = ln;
            newRow[8] = "Pending";
            newRow[11] = "Pending";
            all.add(newRow);
        }

        /* 6 ─ Write everything back */
        try (CSVWriter w = new CSVWriter(new FileWriter(f))) {
            for (String[] row : all) {
                w.writeNext(row);
            }
        }
    }

    /**
     * Updates exactly one cell in a per-employee DTR CSV. That file has exactly
     * 5 columns: 0=EmpID, 1=Date, 2=Log In, 3=Log Out, 4=Duration
     */
    private void updateDtrFileCell(String empId, String date, int csvCol, String newValue)
            throws IOException {
        File f = new File("src/com/csv/DTR/" + empId + ".csv");
        List<String[]> all = new ArrayList<>();

        // 1) Read existing file (if any)
        if (f.exists()) {
            try (CSVReader r = new CSVReader(new FileReader(f))) {
                String[] row;
                while ((row = r.readNext()) != null) {
                    all.add(row);
                }
            } catch (CsvValidationException ex) {
                Logger.getLogger(AttendanceManagement.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // 2) If completely empty, inject a 5‐column header
        if (all.isEmpty()) {
            all.add(new String[]{"EmpID", "Date", "Log In", "Log Out", "Duration"});
        }

        // 3) Find the row matching our date, update that column
        boolean found = false;
        for (int i = 1; i < all.size(); i++) {
            String[] row = all.get(i);
            if (row.length > 1 && row[1].equals(date)) {
                if (row.length < 5) {
                    row = Arrays.copyOf(row, 5);
                }
                row[csvCol] = newValue;
                all.set(i, row);
                found = true;
                break;
            }
        }

        // 4) If not found, append a new row (leaving other cells blank)
        if (!found) {
            String[] nr = new String[5];
            nr[0] = empId;
            nr[1] = date;
            if (csvCol == 2) {
                nr[2] = newValue;  // Log In
            } else if (csvCol == 3) {
                nr[3] = newValue;  // Log Out
            }        // duration (4) left blank for later recompute
            all.add(nr);
        }

        // 5) Overwrite the CSV with our updated rows
        try (CSVWriter w = new CSVWriter(new FileWriter(f))) {
            for (String[] row : all) {
                w.writeNext(row);
            }
        }
    }

    /**
     * Supervisor action: mark existing DTR rows Approved, writing into the
     * employee’s own DTR.csv at columns: 6 = DTR Approved By 7 = DTR Approved
     * Date 8 = DTR Status
     */
    private void validateTimesheet() {
        if (!isSupervisor()) {
            return;
        }

        String emp = (String) employeeSelector.getSelectedItem();
        Date rawFrom = fromDatePicker.getDate(), rawTo = toDatePicker.getDate();
        if (rawFrom == null || rawTo == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select both From and To dates before validating.",
                    "Date Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate start = rawFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = rawTo.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (end.isBefore(start)) {
            JOptionPane.showMessageDialog(this,
                    "“To” date must be on or after the “From” date.",
                    "Invalid Range", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Load all existing dates from the employee's CSV
        File dtrFile = new File(DTR_FOLDER + emp + ".csv");
        Set<String> existing = new HashSet<>();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("M/d/yyyy");
        try (CSVReader r = new CSVReader(new FileReader(dtrFile))) {
            r.readNext(); // skip header
            String[] row;
            while ((row = r.readNext()) != null) {
                if (row.length > 1) {
                    existing.add(row[1].trim());
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Cannot read DTR for " + emp + ":\n" + ex.getMessage(),
                    "I/O Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Build list of dates both in range AND existing in the file
        List<String> toApprove = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            String ds = d.format(df);
            if (existing.contains(ds)) {
                toApprove.add(ds);
            }
        }
        if (toApprove.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No existing DTR entries found in that date range.\n"
                    + "Use Manual Entry to add missing days first.",
                    "Nothing to Validate", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Confirm once
        if (JOptionPane.showConfirmDialog(this,
                "Approve " + toApprove.size() + " DTR day(s) for " + emp + "?",
                "Confirm Validation", JOptionPane.YES_NO_OPTION)
                != JOptionPane.YES_OPTION) {
            return;
        }

        // Stamp each approved row
        String approverId = currentUser.getuEmpId();
        String approverDate = new SimpleDateFormat("M/d/yyyy").format(new Date());
        for (String ds : toApprove) {
            try {
                updateCsvCell(emp, ds, 6, approverId);      // DTR Approved By
                updateCsvCell(emp, ds, 7, approverDate);    // DTR Approved Date
                updateCsvCell(emp, ds, 8, "Approved");      // DTR Status
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(this,
                        "Failed to stamp " + ds + ":\n" + ioe.getMessage(),
                        "I/O Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        JOptionPane.showMessageDialog(this,
                "Successfully approved DTR for " + toApprove.size() + " day(s).",
                "Done", JOptionPane.INFORMATION_MESSAGE);
    }

    private String computePayPeriod(LocalDate from) {
        YearMonth ym = YearMonth.from(from);
        int sd = from.getDayOfMonth() <= 15 ? 1 : 16;
        int ed = (sd == 1 ? 15 : ym.lengthOfMonth());
        String m = ym.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        return sd + "-" + ed + " " + m + " " + ym.getYear();
    }

    private void saveChanges() {
        if (JOptionPane.showConfirmDialog(this, "Save edits?",
                "Confirm Save", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        String emp = isSupervisor()
                ? (String) employeeSelector.getSelectedItem() : currentUser.getuEmpId();
        File f = new File("src/com/csv/DTR/" + emp + ".csv");
        try (CSVWriter w = new CSVWriter(new FileWriter(f))) {
            w.writeNext(new String[]{"EmpID", "Date", "Clock In", "Clock Out", "Duration"});
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                w.writeNext(new String[]{
                    emp,
                    tableModel.getValueAt(i, 0).toString(),
                    tableModel.getValueAt(i, 1).toString(),
                    tableModel.getValueAt(i, 2).toString(),
                    tableModel.getValueAt(i, 3).toString()
                });
            }
            JOptionPane.showMessageDialog(this, "Saved successfully.",
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving:\n" + ex.getMessage(),
                    "I/O Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewChangeLogs() {
        File f = new File(DTR_CHANGE_LOGS);
        if (!f.exists()) {
            JOptionPane.showMessageDialog(this, "No change logs found.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String l;
            while ((l = br.readLine()) != null) {
                sb.append(l).append("\n");
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error reading logs:\n" + ex.getMessage(),
                    "I/O Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        ta.setCaretPosition(0);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(700, 400));
        JOptionPane.showMessageDialog(this, sp, "DTR Change Logs", JOptionPane.INFORMATION_MESSAGE);
    }

    private void applyLeave() {
    }

    private void approveLeave() {
    }

    private void openManualDTRDialog() {

        JDialog dialog = new JDialog((Frame) null, "Manual DTR Entry", true);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));

        JDateChooser dateChooser = new JDateChooser();
        SpinnerDateModel timeModelIn = new SpinnerDateModel();
        JSpinner spinnerIn = new JSpinner(timeModelIn);
        ((JSpinner.DefaultEditor) spinnerIn.getEditor()).getTextField().setEditable(false);
        spinnerIn.setEditor(new JSpinner.DateEditor(spinnerIn, "h:mm a"));

        SpinnerDateModel timeModelOut = new SpinnerDateModel();
        JSpinner spinnerOut = new JSpinner(timeModelOut);
        spinnerOut.setEditor(new JSpinner.DateEditor(spinnerOut, "h:mm a"));
        ((JSpinner.DefaultEditor) spinnerOut.getEditor()).getTextField().setEditable(false);

        dialog.add(new JLabel("Date:"));
        dialog.add(dateChooser);
        dialog.add(new JLabel("Clock In:"));
        dialog.add(spinnerIn);
        dialog.add(new JLabel("Clock Out:"));
        dialog.add(spinnerOut);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        dialog.add(saveButton);
        dialog.add(cancelButton);

        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(null);

        saveButton.addActionListener(e -> {
            String date = new SimpleDateFormat("M/d/yyyy").format(dateChooser.getDate());
            String timeIn = new SimpleDateFormat("h:mm a").format((Date) spinnerIn.getValue()).toUpperCase();
            String timeOut = new SimpleDateFormat("h:mm a").format((Date) spinnerOut.getValue()).toUpperCase();
            String empID = (String) employeeSelector.getSelectedItem();
            logDTRChange(empID, "Manual DTR entry: " + timeIn + "–" + timeOut + " on " + date);

            // (1) Log to the change-log as you already do:
            logDTRChange(empID, "Manual DTR entry: " + timeIn + "–" + timeOut + " on " + date);

            // (2) **Update just those two cells** in the employee's DTR.csv:
            try {
                // CSV columns are now: 0=Employee#,1=Date,2=Log In,3=Log Out,…11=PayrollStatus
                updateCsvCell(empID, date, 2, timeIn);
                updateCsvCell(empID, date, 3, timeOut);
            } catch (IOException io) {
                JOptionPane.showMessageDialog(this,
                        "Error saving manual entry:\n" + io.getMessage(),
                        "I/O Error", JOptionPane.ERROR_MESSAGE);
            }
            // Validate the format before proceeding
            if (!isValidTimeFormat(timeIn) || !isValidTimeFormat(timeOut)) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter valid time in format hh:mm AM/PM (e.g., 8:30 AM)",
                        "Invalid Time Format",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            // (3) Now update *just that row* in the tableModel so the UI matches:
            boolean found = false;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (tableModel.getValueAt(i, 0).equals(date)) {
                    tableModel.setValueAt(timeIn, i, 1);
                    tableModel.setValueAt(timeOut, i, 2);
                    found = true;
                    break;
                }
            }
            if (!found) {
                // if it was an entirely new date, append it:
                tableModel.addRow(new Object[]{date, timeIn, timeOut, "", "", "", "Present"});
            }

            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void writeTableToCSV() {
        try (CSVWriter writer = new CSVWriter(new FileWriter("src/com/csv/DTRManualEntry.csv"))) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String[] row = new String[tableModel.getColumnCount()];
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    row[j] = tableModel.getValueAt(i, j).toString();
                }
                writer.writeNext(row);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving manual entry to CSV.");
        }
    }

    private void logDTRChange(String empId, String desc) {
        File f = new File("src/com/csv/DTR/DTRChangeLogs.csv");
        boolean needHeader = !f.exists() || f.length() == 0;
        try (PrintWriter pw = new PrintWriter(new FileWriter(f, true))) {
            if (needHeader) {
                pw.println("Supervisor,EmpID,Name,Date,Changes");
            }
            String now = new SimpleDateFormat("M/d/yyyy h:mm a").format(new java.util.Date());
            String sup = currentUser.getuFirstName() + " " + currentUser.getuLastName();
            pw.printf("%s,%s,%s,%s,%s%n", sup, empId, currentUser.getuLastName(), now, desc);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to log change:\n" + ex.getMessage(),
                    "I/O Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportAttendance() {

        String empId = isSupervisor() ? (String) employeeSelector.getSelectedItem() : currentUser.getuEmpId();
        String filePath = "src/com/csv/DTR/" + empId + ".csv";

        Date fromDate = fromDatePicker != null ? fromDatePicker.getDate() : new Date();
        Date toDate = toDatePicker != null ? toDatePicker.getDate() : new Date();
        fromDatePicker.setPreferredSize(new Dimension(120, 25));
        toDatePicker.setPreferredSize(new Dimension(120, 25));

        SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy.MM.dd");
        String fileName = empId + "_" + fileDateFormat.format(fromDate) + "_" + fileDateFormat.format(toDate) + ".csv";

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Folder to Save Report");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int chooserResult = fileChooser.showSaveDialog(this);
        if (chooserResult == JFileChooser.APPROVE_OPTION) {
            File selectedDir = fileChooser.getSelectedFile();
            File exportFile = new File(selectedDir, fileName);

            try (BufferedReader reader = new BufferedReader(new FileReader(filePath)); PrintWriter writer = new PrintWriter(new FileWriter(exportFile))) {

                writer.println("Date,Clock In,Clock Out,Duration");
                String line;
                boolean isFirstLine = true;
                SimpleDateFormat dateParser = new SimpleDateFormat("M/d/yyyy");

                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }
                    String[] parts = line.split(",");
                    if (parts.length >= 5) {
                        Date entryDate = dateParser.parse(parts[1].trim());
                        if (!entryDate.before(fromDate) && !entryDate.after(toDate)) {
                            writer.println(parts[1].trim() + "," + parts[2].trim() + "," + parts[3].trim() + "," + parts[4].trim());
                        }
                    }
                }

                JOptionPane.showMessageDialog(this, "Exported to: " + exportFile.getAbsolutePath());

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage());
            }
        }
    }

    /**
     * Overwrites src/com/csv/DTR/{empID}.csv with the contents of the
     * tableModel.
     */
    private void writeAttendanceCsv(String empID) {
        File f = new File("src/com/csv/DTR/" + empID + ".csv");
        try (CSVWriter writer = new CSVWriter(new FileWriter(f))) {
            // 1) write header row (adjust columns as in your original CSV)
            writer.writeNext(new String[]{"EmpID", "Date", "Clock In", "Clock Out", "Duration"});
            // 2) write each table row
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String date = tableModel.getValueAt(i, 0).toString();
                String in = tableModel.getValueAt(i, 1).toString();
                String out = tableModel.getValueAt(i, 2).toString();
                String dur = tableModel.getValueAt(i, 3).toString();
                writer.writeNext(new String[]{empID, date, in, out, dur});
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error writing attendance CSV for EmpID " + empID + ":\n" + ex.getMessage(),
                    "I/O Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
