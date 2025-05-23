/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gui;

/**
 *
 * @author Miles
 */

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class PayrollSlip extends JFrame {
    private JTextArea slipTextArea;
    private JButton downloadButton;
    
    public PayrollSlip(Object[] payrollReport) {
        
        String[] payrollReportString = new String[payrollReport.length];
        for (int i = 0; i < payrollReport.length; i++) {
            payrollReportString[i] = String.valueOf(payrollReport[i]);
        }
        
        String empId = payrollReportString[0];
        String name = payrollReportString[1];
        String dob = payrollReportString[2];
        String position = payrollReportString[3];
        String status = payrollReportString[4];
        String hourlyRate = payrollReportString[5];
        String payStart = payrollReportString[6];
        String payEnd = payrollReportString[7];
        String wrkHrs = payrollReportString[8];
        String otHrs = payrollReportString[9];
        String grossIncome = payrollReportString[10];
        String sssCon = payrollReportString[11];
        String hdmfCon = payrollReportString[12];
        String philHealthCon = payrollReportString[13];
        String taxIncome = payrollReportString[14];
        String birTax = payrollReportString[15];
        String lateDed = payrollReportString[15];
        String totalDed = payrollReportString[17];
        String rice = payrollReportString[18];
        String phone = payrollReportString[19];
        String clothing = payrollReportString[20];
        String netIncome = payrollReportString[21];
        
        setTitle("Payroll Slip");
        setSize(500, 600);
        setLocationRelativeTo(null); // Center window
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
        
        slipTextArea = new JTextArea();
        slipTextArea.setText(generateSlipText(empId, name, dob, position, status, hourlyRate, payStart, payEnd, wrkHrs, otHrs, grossIncome, sssCon, hdmfCon, philHealthCon, taxIncome, birTax, lateDed, totalDed, rice, phone, clothing, netIncome));
        slipTextArea.setEditable(false);
        slipTextArea.setMargin(new Insets(20, 20, 20, 20));
        slipTextArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
        
        JScrollPane scrollPane = new JScrollPane(slipTextArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        
        downloadButton = new JButton("Download");
        downloadButton.setPreferredSize(new Dimension(120, 35));
        
        downloadButton.addActionListener(e -> downloadAsTextFile(name, payStart, payEnd, slipTextArea.getText()));
        
        add(scrollPane, BorderLayout.CENTER);
        add(downloadButton, BorderLayout.SOUTH);
        
    }
    
    private String generateSlipText(String empId, String name, String dob, String position, String status, String hourlyRate, String payStart, String payEnd, String wrkHrs, String otHrs, String grossIncome, String sssCon, String hdmfCon, String philHealthCon, String taxIncome, String birTax, String lateDed, String totalDed,String rice, String phone, String clothing,String netIncome) {
        return "=====================================\n"
                + "        MOTORPH PAYROLL SLIP\n"
                + "=====================================\n"
                + "Employee ID: " + empId + "\n"
                + "Name: " + name + "\n"
                + "Date of Birth: " + dob + "\n"
                + "Position: " + position + "\n"
                + "Status: " + status + "\n"
                + "Hourly Rate: " + hourlyRate + "\n"
                + "Pay Coverage \n" 
                + "  Start Date: " + payStart + "\n" 
                + "  End Date: " + payEnd + "\n"
                + "=====================================\n"
                + "Worked Hours: " + wrkHrs + "\n"
                + "Overtime Hours: " + otHrs + "\n"
                + "=====================================\n"
                + "GROSS INCOME: " + grossIncome + "\n"
                + "=====================================\n"
                + "Goverment Deductions \n" 
                + "  SSS Contribution: " + sssCon + "\n" 
                + "  Pag-IBIG Contribution: " + hdmfCon + "\n"
                + "  PhilHealth Contribution: " + philHealthCon + "\n" 
                + "  Taxable Income: " + taxIncome + "\n"
                + "  BIR Withholding Tax: " + birTax + "\n" 
                + "Other Deductions \n" 
                + "  Late Deductions: " + lateDed + "\n"
                + "TOTAL DEDUCTIONS: " + totalDed + "\n"
                + "=====================================\n"
                + "De Minimis Benefits \n" 
                + "  Rice Subsidy: " + rice + "\n" 
                + "  Phone Allowance: " + phone + "\n"
                + "  Clothing Allowance: " + clothing + "\n" 
                + "=====================================\n"
                + "NET INCOME: " + netIncome + "\n"
                + "=====================================";
    }
    
    private void downloadAsTextFile(String name, String payStart, String payEnd, String slipText) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(name.replaceAll(" ", "_") + "_" + payStart + "_" + payEnd + "_Payslip.txt"));
        
        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(slipText);
                JOptionPane.showMessageDialog(this, "Payroll slip saved as:\n" + file.getAbsolutePath());
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving file." + ex.getMessage());
            }
        }
    } 
    
}