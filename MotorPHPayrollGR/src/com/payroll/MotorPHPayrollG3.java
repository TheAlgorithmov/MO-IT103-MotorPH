// MainClass - Final Calculation of Payroll
package com.payroll;

import java.util.*;

public class MotorPHPayrollG3 {
    public static void main(String[] args) {
        System.out.println("Starting Payroll System...");

        // Load Employee Data
        Map<String, EmployeeData> employees = EmployeeData.loadEmployeeData("EmployeeData.csv");
        if (employees.isEmpty()) {
            System.err.println("No employees loaded. Exiting...");
            return;
        }

        // Load Time Entries (Corrected)
        List<TimeEntry> timeEntries = TimeEntry.loadTimeEntries("EmployeeTimeEntries.csv"); 
        if (timeEntries.isEmpty()) {
            System.err.println("No time entries loaded. Exiting...");
            return;
        }

        // Calculate Weekly Worked Hours
        Map<String, WeeklySummary> weeklySummaries = WeeklySummary.calculateWorkedHours(employees, timeEntries); 

        // Loop through each employee and compute payroll
        // Iterate over weekly summaries (Ensuring separate weekly payroll per employee)
        for (Map.Entry<String, WeeklySummary> entry : weeklySummaries.entrySet()) {
            String weeklyKey = entry.getKey();  // ✅ This ensures weeklyKey is available
            WeeklySummary summary = entry.getValue();

            // Regular salary: Work up to 40 hours at normal rate
            double regularPay = summary.getTotalWorkHours() * summary.getEmployee().getHourlyRate();
            double overtimePay = summary.getTotalOvertime() * summary.getEmployee().getHourlyRate() * 1.25;
            double holidayPay = summary.getTotalHolidayPay();
            double restDayOvertimePay = summary.getTotalRestDayOTPay();

            double grossIncome = regularPay + overtimePay + holidayPay + restDayOvertimePay;

            // Government Deductions
            double govtSSS = GovernmentDeductions.calculateSSS(grossIncome);
            double govtPhilHealth = GovernmentDeductions.calculatePhilHealth(grossIncome);
            double govtHDMF = GovernmentDeductions.calculatePagibig(grossIncome);
            double taxableIncome = grossIncome - (govtSSS + govtPhilHealth + govtHDMF);
            double govtBirTax = GovernmentDeductions.calculateBIR(taxableIncome);

            // Net Pay Computation
            double totalDeductions = govtSSS + govtHDMF + govtPhilHealth + govtBirTax + summary.getTotalLateDeductions();
            double netPay = grossIncome - totalDeductions;

        // Final Payroll Report
        System.out.println("--------------------------------------------------------------");
        System.out.println("---------------- FINAL PAYROLL REPORT (WEEKLY) ----------------");
        System.out.printf(" Employee ID: %s | Name: %s | DOB: %s%n", 
            summary.getEmployee().getEmpId(), summary.getEmployee().getName(), summary.getEmployee().getDob());

        //Fix: Extract week number safely
        String[] keyParts = weeklyKey.split("-");
        String weekNumber = (keyParts.length > 1) ? keyParts[1] : "Unknown";
        System.out.printf(" Week Number: %s%n", weekNumber);

        System.out.println("--------------------------------------------------------------");
        System.out.printf(" Worked Hours (Per Week): %.2f hours%n", summary.getTotalWorkHours());
        System.out.printf(" Overtime Hours (Per Week): %.2f hours%n", summary.getTotalOvertime());  // ✅ Add OT hours
        System.out.printf(" Gross Weekly Income: PHP %.2f%n", grossIncome);
        System.out.printf(" Net Weekly Income: PHP %.2f%n", netPay);
        System.out.println("--------------------------------------------------------------");
        System.out.println(" Government Deductions:");
        System.out.printf(" SSS Contribution (Employee Share): PHP %.2f%n", govtSSS);
        System.out.printf(" Pag-IBIG Contribution: PHP %.2f%n", govtHDMF);
        System.out.printf(" PhilHealth Contribution: PHP %.2f%n", govtPhilHealth);
        System.out.printf(" BIR Withholding Tax (Weekly): PHP %.2f%n", govtBirTax);
        System.out.println("--------------------------------------------------------------");
        System.out.println(summary.getSummaryReport());

        }
    }
}
