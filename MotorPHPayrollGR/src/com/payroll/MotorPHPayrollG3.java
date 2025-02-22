//MainClass - Final Calculation of Payroll
package com.payroll;

import java.util.*;

public class MotorPHPayrollG3 {
    public static void main(String[] args) {
        System.out.println("Starting Payroll System...");

        Map<String, EmployeeData> employees = EmployeeData.loadEmployeeData("EmployeeData.csv");
        List<TimeEntry> timeEntries = TimeEntry.loadTimeEntries("EmployeeTimeEntries.csv");

        if (employees.isEmpty()) {
            System.err.println("No employees loaded. Exiting...");
            return;
        }

        Map<String, WeeklySummary> weeklySummaries = WeeklySummary.calculateWorkedHours(employees, timeEntries);

        for (WeeklySummary summary : weeklySummaries.values()) {
            double weeklySalary = Math.min(40, summary.getTotalWorkHours()) * summary.getEmployee().getHourlyRate();
            double overtimePay = summary.getTotalOvertime() * summary.getEmployee().getHourlyRate() * 1.25;
            double grossIncome = weeklySalary + overtimePay;

            // ------------------------------------------------------------------------------------
            // GOVERNMENT DEDUCTIONS
            // ------------------------------------------------------------------------------------
            double govtSSS = GovernmentDeductions.calculateSSS(grossIncome);
            double govtPhilHealth = GovernmentDeductions.calculatePhilHealth(grossIncome);
            double govtHDMF = GovernmentDeductions.calculatePagibig(grossIncome);
            double taxableIncome = grossIncome - (govtSSS + govtPhilHealth + govtHDMF);
            double govtBirTax = GovernmentDeductions.calculateBIR(taxableIncome);

            // ------------------------------------------------------------------------------------
            // NET PAY COMPUTATION
            // ------------------------------------------------------------------------------------
            double totalDeductions = govtSSS + govtHDMF + govtPhilHealth + govtBirTax + summary.getTotalLateDeductions();
            double netPay = grossIncome - totalDeductions;

            // ------------------------------------------------------------------------------------
            // WORKED HOURS & DEDUCTIONS BREAKDOWN
            // ------------------------------------------------------------------------------------
            String breakdownOutput = summary.getBreakdownReport();  // Get the full formatted breakdown from WeeklySummary

            // ------------------------------------------------------------------------------------
            // FINAL PAYROLL REPORT (WEEKLY) - PRINT LAST
            // ------------------------------------------------------------------------------------
            System.out.println("--------------------------------------------------------------");
            System.out.println("---------------- FINAL PAYROLL REPORT (WEEKLY) ----------------");
            System.out.printf(" Employee ID: %s | Name: %s | DOB: %s%n", summary.getEmployee().getEmpId(), summary.getEmployee().getName(), summary.getEmployee().getDob());
            System.out.println("--------------------------------------------------------------");
            System.out.printf(" Worked Hours (Per Week): %.2f hours%n", summary.getTotalWorkHours());
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
