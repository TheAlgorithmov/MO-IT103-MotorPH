/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author AtlasPrimE
 */
package com.gui.Payroll;

import com.gui.Home.User;
import com.payroll.MotorPHPayrollG3;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;       // ← NEW
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public class PayrollDisplay extends JFrame {

    private final User currentUser;
    private final JComboBox<String> periodCombo = new JComboBox<>();
    private final JPanel slipHolder = new JPanel(new BorderLayout());   // where PaySlip goes

    private static final DateTimeFormatter PP_FMT
            = DateTimeFormatter.ofPattern("yyyy.MMM.dd");

    public PayrollDisplay(User user, JFrame parent) {
        super("View Payslip");
        this.currentUser = user;

        /* ── discover pay periods ─────────────────────────────────── */
        try {
            Set<String> periods = DtrCsvUtil.discoverPayPeriods();
            List<String> sorted = new ArrayList<>(periods);
            DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy.MMM.dd", Locale.ENGLISH);
            sorted.sort(Comparator.comparing(p -> LocalDate.parse((CharSequence) p, FMT)));   // oldest → newest

            periodCombo.setModel(new DefaultComboBoxModel<>(sorted.toArray(new String[0])));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Cannot read pay-period list:\n" + ex.getMessage(),
                    "I/O Error", JOptionPane.ERROR_MESSAGE);
        }

        JButton viewBtn = new JButton("View Payslip");
        viewBtn.addActionListener(e -> showSlip());

// ────────── 1. Build the toolbar  ─────────────────────────────
        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10)); // ← center, 15-px gap

// show full “yyyy.MMM.dd” in the combo even when not expanded
        periodCombo.setPrototypeDisplayValue("2025.Dec.16"); // widest possible text
        periodCombo.setPreferredSize(new Dimension(120, 30)); // adjust to taste

        top.add(new JLabel("Pay Period:"));
        top.add(periodCombo);
        top.add(viewBtn);

        add(top, BorderLayout.NORTH);
        add(slipHolder, BorderLayout.CENTER);

        setSize(700, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                parent.setVisible(true);           // bring HomePage back if user hid it
                parent.toFront();
            }
        });
    }

    /* ------------------------------------------------------------- */
 /*  Convert “2025.Jun.16” to LocalDate[ start=2025-06-16 end=EOM]*/
 /* ------------------------------------------------------------- */
    private LocalDate[] parsePeriod(String pp) {
        LocalDate start = LocalDate.parse(pp, PP_FMT);
        LocalDate end = (start.getDayOfMonth() == 1)
                ? start.withDayOfMonth(15)
                : start.withDayOfMonth(start.lengthOfMonth());
        return new LocalDate[]{start, end};
    }

    /* ------------------------------------------------------------- */
 /*  Main action: load data & embed PaySlip panel                 */
 /* ------------------------------------------------------------- */
    private void showSlip() {
        String sel = (String) periodCombo.getSelectedItem();
        if (sel == null) {
            return;
        }

        // ── 1. Convert the combo value into a LocalDate (the *pay-date*) ─────
        DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy.MMM.dd", Locale.ENGLISH);
        LocalDate payDate = LocalDate.parse(sel, FMT);

        // ── 2. Derive the correct cut-off range ─────────────────────────────
        LocalDate periodStart, periodEnd;

        if (payDate.getDayOfMonth() == 16) {                     // mid-month payout
            periodStart = payDate.withDayOfMonth(1);             // 1-15 of SAME month
            periodEnd = payDate.withDayOfMonth(15);
        } else {                                                 // 1st-of-month payout
            LocalDate prev = payDate.minusMonths(1);             // 16-EOM of PREV month
            periodStart = prev.withDayOfMonth(16);
            periodEnd = prev.withDayOfMonth(prev.lengthOfMonth());
        }

        try {
            Object[] rpt = MotorPHPayrollG3.runPayrollSearch(
                    periodStart, periodEnd, currentUser.getuEmpId());

            if (rpt == null) {
                try {
                    String reason = diagnoseMissingSlip(periodStart, periodEnd,
                            currentUser.getuEmpId());
                    JOptionPane.showMessageDialog(this,
                            reason, "Payslip Unavailable", JOptionPane.WARNING_MESSAGE);
                } catch (IOException ioEx) {
                    JOptionPane.showMessageDialog(this,
                            "Could not analyse DTR file:\n" + ioEx.getMessage(),
                            "I/O Error", JOptionPane.ERROR_MESSAGE);
                }
                return;
            }
            // ── 3. Refresh the panel ───────────────────────────────────────
            slipHolder.removeAll();
            slipHolder.add(
                    new PaySlip(rpt, periodStart, periodEnd, payDate, currentUser),
                    BorderLayout.CENTER
            );
            slipHolder.revalidate();
            slipHolder.repaint();

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not generate payslip:\n" + ex.getMessage(),
                    "I/O Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Returns a human-readable reason why a payslip can’t be shown for the
     * range. May return null if it *can* be shown (everything fully approved).
     */
    private String diagnoseMissingSlip(LocalDate start, LocalDate end, String empId)
            throws IOException {

        java.util.List<String[]> rows = DtrCsvUtil.readAll(empId);   // header at idx 0

        boolean foundInRange = false;
        boolean dtrPending = false;
        boolean payrollPending = false;

        java.time.format.DateTimeFormatter csvFmt
                = java.time.format.DateTimeFormatter.ofPattern("M/d/yyyy");

        for (int i = 1; i < rows.size(); i++) {
            String[] r = rows.get(i);
            if (r.length < 12) {
                continue;     // malformed row
            }
            LocalDate d = LocalDate.parse(r[1].trim(), csvFmt);
            if (d.isBefore(start) || d.isAfter(end)) {
                continue;
            }

            foundInRange = true;

            if (!"Approved".equalsIgnoreCase(r[8])) {
                dtrPending = true;
            }
            if (!"Approved".equalsIgnoreCase(r[11])) {
                payrollPending = true;
            }
        }

        if (!foundInRange) {
            return "No attendance records found for this pay period. "
                    + "If you believe this is an error, please contact the IT Department.";
        }
        if (dtrPending) {
            return "Your DTR for this period is still pending approval. "
                    + "Please contact your immediate supervisor.";
        }
        if (payrollPending) {
            return "Payroll for this period has not yet been approved. "
                    + "Please contact the Payroll Team.";
        }
        return null;   // should never reach here when runPayrollSearch() returned null
    }

}
