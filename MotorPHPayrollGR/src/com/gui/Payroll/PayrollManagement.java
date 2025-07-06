/**
 *
 * @author ongoj & Miles
 */
/**
 * PayrollManagement.java - Payroll Approval with Status Controls and Net Pay Display
 */
package com.gui.Payroll;

import com.gui.Home.User;
import com.opencsv.CSVWriter;
import com.payroll.MotorPHPayrollG3;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.table.DefaultTableCellRenderer;
import java.text.DecimalFormat;

public class PayrollManagement extends JFrame {

    private final User currentUser;
    private final JTable table;
    private final DefaultTableModel model;
    private final JComboBox<String> periodCombo;
    private final JComboBox<String> scopeCombo;
    private LocalDate periodStart, periodEnd;
    private static final DecimalFormat MONEY_FMT = new DecimalFormat("#,##0.00");

    public PayrollManagement(User user) {
        super("Payroll Management");
        this.currentUser = user;

        setSize(1000, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] scopeOpts = {"All Employees", "Selected Employee(s)"};
        scopeCombo = new JComboBox<>(scopeOpts);

        // ─── Top Bar ─────────────────────────────────────────────────
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        periodCombo = new JComboBox<>();
        JButton btnLoad = new JButton("Load Data");
        JButton btnApprove = new JButton("Approve Payroll");
        JButton btnCalculate = new JButton("Calculate Pay");
        JButton btnExport = new JButton("Export Pay Data");
        top.add(new JLabel("Pay Period:"));
        top.add(periodCombo);
        top.add(new JLabel("  Scope:"));        // NEW
        top.add(scopeCombo);                    // NEW
        top.add(btnLoad);
        top.add(btnApprove);
        top.add(btnCalculate);
        top.add(btnExport);

        // ─── Table ───────────────────────────────────────────────────
        String[] cols = {
            "EmpID", "First Name", "Last Name",
            "DTR Status", "DTR Approved Date",
            "Payroll Status", "Payroll Approved By", "Payroll Approved Date",
            "Net Pay"
        };
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.getColumnModel().getColumn(8).setCellRenderer(new MoneyRenderer());  // NEW

        // ─── Actions ────────────────────────────────────────────────
        btnLoad.addActionListener(e -> parsePeriodAndLoad());
        btnApprove.addActionListener(e -> approvePayroll());
        btnCalculate.addActionListener(e -> calculatePay());
        btnExport.addActionListener(e -> exportPayData());

        // ─── Populate period dropdown ───────────────────────────────
        try {
            Set<String> periods = DtrCsvUtil.discoverPayPeriods();
            periodCombo.setModel(new DefaultComboBoxModel<>(periods.toArray(new String[0])));
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this,
                    "Error discovering pay periods:\n" + ioe.getMessage(),
                    "I/O Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<Integer> targetRows(JComboBox<String> scope) {
        List<Integer> rows = new ArrayList<>();
        if ("Selected Employee(s)".equals(scope.getSelectedItem())) {
            for (int r : table.getSelectedRows()) {
                rows.add(r);
            }
        } else {                 // "All Employees"
            for (int r = 0; r < model.getRowCount(); r++) {
                rows.add(r);
            }
        }
        return rows;
    }

    private void parsePeriodAndLoad() {

        String per = (String) periodCombo.getSelectedItem();
        if (per == null || per.isBlank()) {
            return;
        }

        try {
            // Example: 2024.Aug.01  →  periodStart = 2024-08-01 , periodEnd = 2024-08-15
            DateTimeFormatter ppFmt
                    = DateTimeFormatter.ofPattern("yyyy.MMM.dd", java.util.Locale.ENGLISH);

            LocalDate start = LocalDate.parse(per, ppFmt);

            LocalDate end = (start.getDayOfMonth() == 1)
                    ? start.withDayOfMonth(15) // 1-15
                    : start.withDayOfMonth(start.lengthOfMonth()); // 16-EOM

            periodStart = start;
            periodEnd = end;

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error parsing period:\n" + ex.getMessage(),
                    "Parse Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        loadTableData();
    }

    private void loadTableData() {
        model.setRowCount(0);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("M/d/yyyy");

        try (DirectoryStream<Path> dsStream
                = Files.newDirectoryStream(Paths.get(DtrCsvUtil.DTR_DIR),
                        path -> path.getFileName().toString().matches("\\d+\\.csv"))) {

            for (Path p : dsStream) {
                String empId = p.getFileName().toString().replace(".csv", "");
                List<String[]> rows = DtrCsvUtil.readAll(empId);

                /* ── per-employee accumulators ───────────────────────── */
                String fn = "", ln = "";
                boolean anyInPeriod = false, allDTRApproved = true;
                LocalDate latestDtrDate = null;
                String payrollStatus = "", payrollBy = "", payrollDate = "";

                /* scan each data row (skip header at index 0)            */
                for (int j = 1; j < rows.size(); j++) {
                    String[] row = rows.get(j);
                    if (row.length < 2) {
                        continue;     // need at least EmpID + Date
                    }
                    /* capture name the first time we see it, independent
                   of whether the row falls in-period                     */
                    if (fn.isEmpty() && row.length > 5) {
                        fn = row[4];
                        ln = row[5];
                    }

                    String ds = row[1].trim();
                    if (ds.isEmpty()) {
                        continue;
                    }

                    LocalDate d;
                    try {
                        d = LocalDate.parse(ds, df);
                    } catch (Exception ex) {
                        continue;                     // malformed date → ignore row
                    }

                    /* Is this row inside the user-selected pay period?   */
                    if (d.isBefore(periodStart) || d.isAfter(periodEnd)) {
                        continue;                     // OUT-of-period → ignore but
                        // still keep name etc.
                    }
                    anyInPeriod = true;

                    /* DTR approval status                                 */
                    if (row.length > 8) {
                        String dtrStat = row[8];
                        if (!"Approved".equalsIgnoreCase(dtrStat)) {
                            allDTRApproved = false;
                        }
                    }
                    if (row.length > 7) {
                        String dd = row[7];
                        if (dd != null && !dd.isEmpty()) {
                            LocalDate ddL = LocalDate.parse(dd, df);
                            if (latestDtrDate == null || ddL.isAfter(latestDtrDate)) {
                                latestDtrDate = ddL;
                            }
                        }
                    }

                    /* payroll columns (take the last value we encounter) */
                    if (row.length > 11) {
                        payrollStatus = row[11];
                        payrollBy = row[9];
                        payrollDate = row[10];
                    }
                } // end rows loop

                /* ── Always add one table row, even if no in-period DTR ── */
                String dtrStatus;
                String dtrDateStr = (latestDtrDate == null) ? "" : latestDtrDate.format(df);

                if (anyInPeriod) {
                    dtrStatus = allDTRApproved ? "Approved" : "Pending";
                } else {
                    dtrStatus = "No DTR";
                    dtrDateStr = "";
                }

                model.addRow(new Object[]{
                    empId, fn, ln,
                    dtrStatus, dtrDateStr,
                    payrollStatus, payrollBy, payrollDate,
                    "" // Net Pay (calculated later)
                });
            }

        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this,
                    "Error loading DTR files:\n" + ioe.getMessage(),
                    "I/O Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void approvePayroll() {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("M/d/yyyy");
        String approver = currentUser.getuEmpId();
        String today = LocalDate.now().format(df);

        for (int i : targetRows(scopeCombo)) {

            String dtrStat = (String) model.getValueAt(i, 3);
            if (!"Approved".equalsIgnoreCase(dtrStat)) {
                continue;                         // skip rows not yet DTR-approved
            }

            String empId = (String) model.getValueAt(i, 0);

            try {
                List<String[]> rows = DtrCsvUtil.readAll(empId);

                for (int j = 1; j < rows.size(); j++) {   // ← digit 1, not letter L
                    String[] row = rows.get(j);
                    if (row.length < 2) {
                        continue;
                    }

                    LocalDate d = LocalDate.parse(row[1], df);
                    if (d.isBefore(periodStart) || d.isAfter(periodEnd)) {
                        continue;
                    }

                    /* Mark payroll approval inside the CSV */
                    DtrCsvUtil.updateCell(empId, row[1], 9, approver);
                    DtrCsvUtil.updateCell(empId, row[1], 10, today);
                    DtrCsvUtil.updateCell(empId, row[1], 11, "Approved");
                }

                /* Update the table row so the UI reflects the change */
                model.setValueAt("Approved", i, 5);
                model.setValueAt(approver, i, 6);
                model.setValueAt(today, i, 7);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error approving payroll for " + empId + ":\n" + ex.getMessage(),
                        "I/O Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void calculatePay() {
        for (int i = 0; i < model.getRowCount(); i++) {
            String payStat = (String) model.getValueAt(i, 5);
            if (!"Approved".equalsIgnoreCase(payStat)) {
                continue;                                   // skip rows still pending
            }

            String empId = (String) model.getValueAt(i, 0);

            try {   // ← runPayrollSearch now throws IOException
                Object[] report = MotorPHPayrollG3.runPayrollSearch(
                        periodStart, periodEnd, empId);

                if (report != null && report.length > 21 && report[21] instanceof Number) {
                    model.setValueAt(
                            MONEY_FMT.format(((Number) report[21]).doubleValue()),
                            i, 8);                              // Net-Pay column
                }

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Unable to calculate pay for " + empId + ":\n" + ex.getMessage(),
                        "I/O Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportPayData() {
        DateTimeFormatter fnmFmt = DateTimeFormatter.ofPattern("yyyyMMdd");
        String fnm = periodStart.format(fnmFmt) + "_" + periodEnd.format(fnmFmt) + ".csv";

        File dir = new File(DtrCsvUtil.PAYDATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File out = new File(dir, fnm);

        String[] hdr = {
            "EmpID", "Name", "DOB", "Position", "Status", "HourlyRate",
            "PayStart", "PayEnd", "WorkHours", "Overtime", "GrossIncome",
            "SSS", "HDMF", "PhilHealth", "TaxableIncome", "BIRTax",
            "LateDed", "TotalDed", "Rice", "Phone", "Clothing", "NetIncome"
        };

        try (CSVWriter w = new CSVWriter(new FileWriter(out))) {

            w.writeNext(hdr);

            for (int i = 0; i < model.getRowCount(); i++) {
                if (!"Approved".equalsIgnoreCase((String) model.getValueAt(i, 5))) {
                    continue;                               // export only fully-approved rows
                }

                String empId = (String) model.getValueAt(i, 0);

                try {                                       // runPayrollSearch can fail per-emp
                    Object[] rpt = MotorPHPayrollG3.runPayrollSearch(
                            periodStart, periodEnd, empId);

                    if (rpt == null || rpt.length <= 21) {
                        continue;
                    }

                    String[] row = new String[22];
                    for (int j = 0; j < 22; j++) {
                        Object v = rpt[j];
                        row[j] = (v instanceof Number)
                                ? MONEY_FMT.format(((Number) v).doubleValue())
                                : String.valueOf(v);
                    }
                    w.writeNext(row);

                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Skipped " + empId + " – could not compute payroll:\n" + ex.getMessage(),
                            "I/O Error", JOptionPane.WARNING_MESSAGE);
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Exported pay data to:\n" + out.getAbsolutePath(),
                    "Export Complete", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {          // file-open / write errors
            JOptionPane.showMessageDialog(this,
                    "Error exporting pay data:\n" + ex.getMessage(),
                    "I/O Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static final class MoneyRenderer extends DefaultTableCellRenderer {

        private static final DecimalFormat DF = new DecimalFormat("#,##0.00");

        @Override
        protected void setValue(Object value) {
            if (value instanceof Number) {
                super.setValue(DF.format(((Number) value).doubleValue()));
            } else {
                super.setValue(value);
            }
        }
    }
}
