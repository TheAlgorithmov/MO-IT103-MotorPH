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

public class AttendanceManagement extends JFrame {

    private static final String SUPERVISOR_LIST_CSV = "src/com/csv/SupervisorLists.csv";
    private static final String DTR_STATUS_CSV = "src/com/csv/DTR/DTRPayrollStatus.csv";
    private static final String DTR_CHANGE_LOGS = "src/com/csv/DTR/DTRChangeLogs.csv";

    private final User currentUser;
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> employeeSelector;
    private JDateChooser fromDatePicker, toDatePicker;
    private JLabel nameLabel; // displays Last, First of selected employee

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
        fromDatePicker.setPreferredSize(new Dimension(125, 25));
        toDatePicker.setPreferredSize(new Dimension(125, 25));
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
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ─── SAVE PANEL ──────────────────────────────────────────────────────────
        if (isSupervisor()) {
            JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton save = new JButton("Save Changes");
            save.addActionListener(e -> saveChanges());
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
                @Override
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
                if (row[0].trim().equals(empId)) {
                    fn = row[1].trim();
                    ln = row[2].trim();
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
        Map<String, String[]> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            boolean hdr = true;
            while ((line = br.readLine()) != null) {
                if (hdr) {
                    hdr = false;
                    continue;
                }
                String[] p = line.split(",", -1);
                if (p.length >= 5) {
                    map.put(p[1].trim(), p);
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "I/O Error reading " + f.getName() + ":\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate start = fromDatePicker.getDate() != null
                ? fromDatePicker.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                : LocalDate.now();
        LocalDate end = toDatePicker.getDate() != null
                ? toDatePicker.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                : start;
        if (end.isBefore(start)) {
            JOptionPane.showMessageDialog(this,
                    "“To” date must be ≥ “From” date.",
                    "Date Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        tableModel.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("M/d/yyyy");
        SimpleDateFormat tf = new SimpleDateFormat("h:mm a");
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            String ds = d.format(fmt);
            boolean weekend = (d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY);

            if (map.containsKey(ds)) {
                String[] p = map.get(ds);
                try {
                    Date inT = tf.parse(p[2].trim());
                    Date outT = tf.parse(p[3].trim());
                    long mins = (outT.getTime() - inT.getTime()) / 60000;
                    if (mins < 0) {
                        mins += 24 * 60;
                    }
                    Date thr = tf.parse("8:45 AM");
                    long late = inT.after(thr) ? (inT.getTime() - thr.getTime()) / 60000 : 0;
                    long ot = Math.max(0, mins - 8 * 60);
                    tableModel.addRow(new Object[]{
                        ds,
                        p[2].trim(),
                        p[3].trim(),
                        p[4].trim(),
                        late > 0 ? late + " mins" : "-",
                        ot > 0 ? String.format("%.2f hrs", ot / 60.0) : "-",
                        "Present"
                    });
                } catch (Exception ign) {
                }
            } else if (!weekend && !d.isAfter(LocalDate.now())) {
                tableModel.addRow(new Object[]{ds, "-", "-", "-", "-", "-", "Absent"});
            }
        }
    }

    private void viewDTRStatus() {
        File f = new File(DTR_STATUS_CSV);
        if (!f.exists()) {
            JOptionPane.showMessageDialog(this, "No DTRPayrollStatus.csv found.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        DefaultTableModel m;
        try (CSVReader r = new CSVReader(new FileReader(f))) {
            List<String[]> all = r.readAll();
            if (all.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No records.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            m = new DefaultTableModel(all.get(0), 0);
            for (int i = 1; i < all.size(); i++) {
                m.addRow(all.get(i));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error reading status:\n" + ex.getMessage(),
                    "I/O Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JTable tbl = new JTable(m);
        JScrollPane sp = new JScrollPane(tbl);
        sp.setPreferredSize(new Dimension(800, 400));
        JDialog dlg = new JDialog(this, "DTR Approval Status", true);
        dlg.setLayout(new BorderLayout());
        dlg.add(sp, BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton del = new JButton("Delete Selected Row");
        del.addActionListener(e -> {
            int r = tbl.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(dlg, "Select a row.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (JOptionPane.showConfirmDialog(dlg, "Delete selected record?", "Confirm Delete",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            m.removeRow(r);
            try (CSVWriter w = new CSVWriter(new FileWriter(f))) {
                int cc = m.getColumnCount();
                String[] hdr = new String[cc];
                for (int i = 0; i < cc; i++) {
                    hdr[i] = m.getColumnName(i);
                }
                w.writeNext(hdr);
                for (int rr = 0; rr < m.getRowCount(); rr++) {
                    String[] row = new String[cc];
                    for (int c = 0; c < cc; c++) {
                        Object o = m.getValueAt(rr, c);
                        row[c] = o == null ? "" : o.toString();
                    }
                    w.writeNext(row);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error writing status:\n" + ex.getMessage(),
                        "I/O Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btns.add(del);
        JButton ok = new JButton("OK");
        ok.addActionListener(e -> dlg.dispose());
        btns.add(ok);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void validateTimesheet() {
        if (!isSupervisor()) {
            return;
        }
        String emp = (String) employeeSelector.getSelectedItem();
        Date d1 = fromDatePicker.getDate(), d2 = toDatePicker.getDate();
        if (emp == null || d1 == null || d2 == null) {
            JOptionPane.showMessageDialog(this, "Select employee & date range.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Review entries and proceed?",
                "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        LocalDate from = d1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String period = computePayPeriod(from);
        File stf = new File(DTR_STATUS_CSV);
        if (stf.exists()) {
            try (CSVReader r = new CSVReader(new FileReader(stf))) {
                r.readNext();
                String[] row;
                while ((row = r.readNext()) != null) {
                    if (row[0].equals(emp) && row[3].equals(period)) {
                        JOptionPane.showMessageDialog(this, "Already validated: " + period,
                                "Duplicate", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        String fn = "", ln = "";
        try (CSVReader r = new CSVReader(new FileReader("src/com/csv/EmployeeData.csv"))) {
            r.readNext();
            String[] row;
            while ((row = r.readNext()) != null) {
                if (row[0].equals(emp)) {
                    fn = row[1];
                    ln = row[2];
                    break;
                }
            }
        } catch (Exception ignored) {
        }
        boolean newFile = !stf.exists() || stf.length() == 0;
        try (PrintWriter pw = new PrintWriter(new FileWriter(stf, true))) {
            if (newFile) {
                pw.println("EmpID,First Name,Last Name,Pay Period,DTR Status,DTR Approved Date");
            }
            String date = new SimpleDateFormat("M/d/yyyy").format(new Date());
            pw.printf("%s,%s,%s,%s,Approved,%s%n", emp, fn, ln, period, date);
            JOptionPane.showMessageDialog(this, "Recorded for " + period,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error writing status:\n" + ex.getMessage(),
                    "I/O Error", JOptionPane.ERROR_MESSAGE);
        }
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
        spinnerIn.setEditor(new JSpinner.DateEditor(spinnerIn, "h:mm a"));

        SpinnerDateModel timeModelOut = new SpinnerDateModel();
        JSpinner spinnerOut = new JSpinner(timeModelOut);
        spinnerOut.setEditor(new JSpinner.DateEditor(spinnerOut, "h:mm a"));

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
            String timeIn = new SimpleDateFormat("h:mm a").format((Date) spinnerIn.getValue());
            String timeOut = new SimpleDateFormat("h:mm a").format((Date) spinnerOut.getValue());
            String empID = (String) employeeSelector.getSelectedItem();
            logDTRChange(empID, "Manual DTR entry: " + timeIn + "–" + timeOut + " on " + date);
            writeAttendanceCsv(empID);

            boolean found = false;

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String rowDate = tableModel.getValueAt(i, 0).toString().trim();
                if (rowDate.equals(date)) {
                    tableModel.setValueAt(timeIn, i, 1);
                    tableModel.setValueAt(timeOut, i, 2);
                    tableModel.setValueAt("Manual", i, 3);
                    found = true;
                    break;
                }
            }

            if (!found) {
                tableModel.addRow(new Object[]{date, timeIn, timeOut, "Manual", "", "", "Pending"});
            }

            writeTableToCSV();
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
