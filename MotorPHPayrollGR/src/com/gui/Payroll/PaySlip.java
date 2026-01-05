/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author ongoj
 */
package com.gui.Payroll;

import com.gui.Home.User;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;

public class PaySlip extends JPanel {

    private Object[] payrollReport;
    private LocalDate startDate, endDate, payDate;
    private JButton exportBtn;
    private User currentUser;

    public PaySlip(Object[] payrollReport, LocalDate startDate, LocalDate endDate, LocalDate payDate, User currentUser) {
        this.payrollReport = payrollReport;
        this.startDate = startDate;
        this.endDate = endDate;
        this.payDate = payDate;
        this.currentUser = currentUser; // add this line!
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 50, 20, 50));
        buildUI();
    }

    private void buildUI() {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");

        // HEADER: Centered logo and title
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/com/gui/images/LoginIcons/logo.png"));
            java.awt.Image awtLogo = icon.getImage().getScaledInstance(130, 130, java.awt.Image.SCALE_SMOOTH);
            JLabel lblLogo = new JLabel(new ImageIcon(awtLogo));
            lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
            headerPanel.add(lblLogo);
        } catch (Exception ex) {
            JLabel lblLogo = new JLabel("MotorPH");
            lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblLogo.setFont(new Font("Arial", Font.BOLD, 32));
            headerPanel.add(lblLogo);
        }

        add(headerPanel, BorderLayout.NORTH);

        // Main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(15, 50, 15, 50));

        // SECTION 1 - Pay Details (top info)
        contentPanel.add(makeInfoRow("Pay Date:", payDate.format(fmt)));
        contentPanel.add(makeInfoRow("Pay Period:", startDate.format(fmt) + " - " + endDate.format(fmt)));
        contentPanel.add(Box.createVerticalStrut(12));
        contentPanel.add(makeInfoRow("Employee ID:", String.valueOf(payrollReport[0])));

        // Name formatting
        String empName = String.valueOf(payrollReport[1]);
        String[] nameParts = empName.trim().split("\\s+");
        String formattedName = empName;
        if (nameParts.length >= 2) {
            formattedName = nameParts[nameParts.length - 1] + ", " + nameParts[0];
        }
        contentPanel.add(makeInfoRow("Employee Name:", formattedName));
        contentPanel.add(makeInfoRow("Position:", String.valueOf(payrollReport[3])));
        contentPanel.add(makeInfoRow("Status:", String.valueOf(payrollReport[4])));
        contentPanel.add(Box.createVerticalStrut(12));

        // --- EARNINGS SECTION ---
        JPanel earningsPanel = new JPanel();
        earningsPanel.setLayout(new BoxLayout(earningsPanel, BoxLayout.Y_AXIS));
        earningsPanel.setOpaque(false);
        earningsPanel.setBorder(BorderFactory.createTitledBorder("Earnings"));

        earningsPanel.add(makePayRow("Worked Hours", payrollReport[8], df));
        earningsPanel.add(makePayRow("Overtime Hours", payrollReport[9], df));
        earningsPanel.add(makeSectionHeader("Gross Income", payrollReport[10], df));

        contentPanel.add(earningsPanel);
        contentPanel.add(Box.createVerticalStrut(10));

        // --- DEDUCTIONS SECTION ---
        JPanel deductionsPanel = new JPanel();
        deductionsPanel.setLayout(new BoxLayout(deductionsPanel, BoxLayout.Y_AXIS));
        deductionsPanel.setOpaque(false);
        deductionsPanel.setBorder(BorderFactory.createTitledBorder("Deductions"));

        deductionsPanel.add(makeSectionSubHeader("Government Deductions"));
        deductionsPanel.add(makePayRow("SSS Contribution", payrollReport[11], df));
        deductionsPanel.add(makePayRow("Pag-IBIG Contribution", payrollReport[12], df));
        deductionsPanel.add(makePayRow("PhilHealth Contribution", payrollReport[13], df));

        deductionsPanel.add(makePayRow("Taxable Income", payrollReport[14], df));
        deductionsPanel.add(makePayRow("BIR Withholding", payrollReport[15], df));
        deductionsPanel.add(makePayRow("Late Deductions", payrollReport[16], df));
        deductionsPanel.add(makeSectionHeader("Total Deductions", payrollReport[17], df));

        contentPanel.add(deductionsPanel);
        contentPanel.add(Box.createVerticalStrut(10));

        // --- DE MINIMIS BENEFITS SECTION ---
        JPanel deminimisPanel = new JPanel();
        deminimisPanel.setLayout(new BoxLayout(deminimisPanel, BoxLayout.Y_AXIS));
        deminimisPanel.setOpaque(false);
        deminimisPanel.setBorder(BorderFactory.createTitledBorder("De Minimis Benefits"));

        deminimisPanel.add(makePayRow("Rice Subsidy", payrollReport[18], df));
        deminimisPanel.add(makePayRow("Phone Allowance", payrollReport[19], df));
        deminimisPanel.add(makePayRow("Clothing Allowance", payrollReport[20], df));

        contentPanel.add(deminimisPanel);
        contentPanel.add(Box.createVerticalStrut(10));

        // --- NET INCOME SECTION ---
        JPanel netIncomePanel = new JPanel();
        netIncomePanel.setLayout(new BoxLayout(netIncomePanel, BoxLayout.Y_AXIS));
        netIncomePanel.setOpaque(false);
        netIncomePanel.setBorder(BorderFactory.createTitledBorder("Net Income"));

        netIncomePanel.add(makeSectionHeader("Net Income", payrollReport[21], df));

        contentPanel.add(netIncomePanel);
        contentPanel.add(Box.createVerticalStrut(20));

        // Center contentPanel horizontally
        JPanel centerWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerWrap.setOpaque(false);
        centerWrap.add(contentPanel);

        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(centerWrap);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);

        add(scrollPane, BorderLayout.CENTER);
        int PAD = 24;                                      // 24-px uniform margin
        setBorder(BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));
        int MAX_W = 500;                                   // cap to 500 px so it never balloons
        setMaximumSize(new Dimension(MAX_W, Integer.MAX_VALUE));
        // Export button
        exportBtn = new JButton("Export to PDF");
        exportBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        exportBtn.addActionListener(e -> exportToPDF());
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.add(exportBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    // Info row (label bold, value regular)
    private JPanel makeInfoRow(String label, String value) {
        JPanel row = new JPanel();
        row.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);

        JLabel lbl = new JLabel(label + " ");
        lbl.setFont(new Font("Arial", Font.BOLD, 15));
        row.add(lbl);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Arial", Font.PLAIN, 15));
        row.add(val);

        return row;
    }

    // Payroll table row
    private JPanel makePayRow(String label, Object value, DecimalFormat df) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.PLAIN, 14));
        lbl.setPreferredSize(new Dimension(220, 20));
        row.add(lbl, BorderLayout.WEST);

        JLabel val = new JLabel(df.format(Double.parseDouble(value.toString())));
        val.setFont(new Font("Arial", Font.PLAIN, 14));
        val.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(val, BorderLayout.EAST);

        return row;
    }

    // Section header with amount (bold)
    private JPanel makeSectionHeader(String label, Object value, DecimalFormat df) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.BOLD, 15));
        lbl.setPreferredSize(new Dimension(220, 24));
        row.add(lbl, BorderLayout.WEST);

        JLabel val = new JLabel(df.format(Double.parseDouble(value.toString())));
        val.setFont(new Font("Arial", Font.BOLD, 15));
        val.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(val, BorderLayout.EAST);

        return row;
    }

    // Section sub-header, no value
    private JPanel makeSectionSubHeader(String label) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.BOLD, 14));
        lbl.setPreferredSize(new Dimension(220, 22));
        row.add(lbl, BorderLayout.WEST);

        return row;
    }

    // --- PDF EXPORT ---
    private void exportToPDF() {
        exportBtn.setVisible(false); // Hide button from export
        // Render only what's visible in scrollpane's viewport
        int width = getWidth(), height = getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        paint(image.getGraphics());

        String empId = payrollReport[0].toString();
        String payDateStr = payDate.toString();
        String defaultFileName = String.format("PaySlip_%s_%s.pdf", empId, payDateStr);

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Payslip PDF As");
        fileChooser.setSelectedFile(new File(defaultFileName));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                com.itextpdf.text.Image pdfImg = com.itextpdf.text.Image.getInstance(image, null);
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
                document.open();

                // Center and scale image
                float docWidth = document.getPageSize().getWidth();
                float docHeight = document.getPageSize().getHeight();
                float imgWidth = pdfImg.getScaledWidth();
                float imgHeight = pdfImg.getScaledHeight();
                pdfImg.setAlignment(com.itextpdf.text.Image.ALIGN_CENTER);
                float scalePercent = Math.min((docWidth - 72) / imgWidth, (docHeight - 72) / imgHeight) * 100;
                pdfImg.scalePercent(scalePercent);

                document.add(pdfImg);
                document.close();

                JOptionPane.showMessageDialog(this, "Payslip exported successfully!");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting payslip: " + ex.getMessage());
            }
        }
        exportBtn.setVisible(true); // Show again for normal use
    }
}
