package com.payroll;

import java.util.*;
import java.time.*;

/**
 * MotorPHPayrollG3 - Main payroll processing system.
 * Computes monthly salaries, government deductions, and de minimis benefits.
 */
public class MotorPHPayrollG3 {

    /**
     * The main method serves as the entry point for the payroll processing system.
     * It performs the following steps:
     * 1. Loads employee data from a CSV file.
     * 2. Loads de minimis benefits for employees.
     * 3. Loads time entries for employees.
     * 4. Computes monthly worked hours.
     * 5. Calculates payroll details including gross income, government deductions, taxable income, and net pay.
     * 6. Generates and prints the final payroll report for each employee.
     */
    public static void main(String[] args) {
        System.out.println("Starting Payroll System...");

        // Load Employee Data
        Map<String, EmployeeData> employees = EmployeeData.loadEmployeeData("src/com/payroll/EmployeeData.csv");
        if (employees.isEmpty()) {
            System.err.println("No employees loaded. Exiting...");
            return;
        }

        // Load De Minimis Benefits
        Map<String, DeMinimisBenefits> benefits = DeMinimisBenefits.loadBenefits("src/com/payroll/EmployeeData.csv");

        // Load Time Entries
        List<TimeEntry> timeEntries = TimeEntry.loadTimeEntries("src/com/payroll/EmployeeTimeEntries.csv");
        if (timeEntries.isEmpty()) {
            System.err.println("No time entries loaded. Exiting...");
            return;
        }

        // Calculate Monthly Worked Hours
        Map<String, MonthlySummary> monthlySummaries = MonthlySummary.calculateWorkedHours(employees, timeEntries);

        // Process payroll for each employee
        for (Map.Entry<String, MonthlySummary> entry : monthlySummaries.entrySet()) {
            String monthlyKey = entry.getKey();
            MonthlySummary summary = entry.getValue();
            EmployeeData employee = summary.getEmployee();

            // Compute De Minimis Benefits (Monthly)
            float riceSubsidy = benefits.getOrDefault(employee.getEmpId(), new DeMinimisBenefits(employee.getEmpId(), 0f, 0f, 0f)).getRiceSubsidy();
            float phoneAllowance = benefits.getOrDefault(employee.getEmpId(), new DeMinimisBenefits(employee.getEmpId(), 0f, 0f, 0f)).getPhoneAllowance();
            float clothingAllowance = benefits.getOrDefault(employee.getEmpId(), new DeMinimisBenefits(employee.getEmpId(), 0f, 0f, 0f)).getClothingAllowance();
            float totalDeMinimisBenefits = riceSubsidy + phoneAllowance + clothingAllowance;

            // Step 1: Compute Monthly Salary (Before Deductions)
            float regularPay = summary.getTotalWorkHours() * employee.getHourlyRate();
            float overtimePay = summary.getTotalOvertime() * employee.getHourlyRate() * 1.25f;
            float holidayPay = summary.getTotalHolidayPay();
            float restDayOvertimePay = summary.getTotalRestDayOTPay();
            float grossIncome = regularPay + overtimePay + holidayPay + restDayOvertimePay;

            // Step 2: Compute Government Deductions
            float basicSalary = employee.getBasicSalary(); // Get Basic Salary from EmployeeData.csv
            float govtSSS = GovernmentDeductions.calculateSSS(basicSalary);

            float govtPhilHealth = GovernmentDeductions.calculatePhilHealth(grossIncome);
            float govtHDMF = GovernmentDeductions.calculatePagibig(grossIncome);

            // Step 3: Calculate Taxable Income (Gross - Deductions)
            float taxableIncome = grossIncome - (govtSSS + govtPhilHealth + govtHDMF);

            // Step 4: Compute BIR Tax
            float govtBirTax = GovernmentDeductions.calculateBIR(taxableIncome);

            // Step 5: Compute Net Pay (After Tax, Add De Minimis Benefits)
            float totalDeductions = govtSSS + govtHDMF + govtPhilHealth + govtBirTax + summary.getTotalLateDeductions();
            float netPay = (grossIncome - totalDeductions) + totalDeMinimisBenefits;

            // Final Payroll Report
            System.out.println("--------------------------------------------------------------");
            System.out.println("---------------- FINAL PAYROLL REPORT (MONTHLY) ----------------");
            System.out.printf(" Employee ID: %s | Name: %s | DOB: %s%n", 
                employee.getEmpId(), employee.getName(), employee.getDob());
            System.out.printf(" Hourly Rate: PHP %.2f | Status: %s | Position: %s%n", 
                employee.getHourlyRate(), employee.getStatus(), employee.getPosition());

            // Extract and Display Month
            String[] keyParts = monthlyKey.split("-");
            String yearMonth = (keyParts.length > 1) ? keyParts[1] : "Unknown";
            System.out.printf(" Payroll Period: %s%n", yearMonth);
            System.out.println("--------------------------------------------------------------");
            System.out.printf(" Worked Hours (Per Month): %.2f hours%n", summary.getTotalWorkHours());
            System.out.printf(" Overtime Hours (Per Month): %.2f hours%n", summary.getTotalOvertime());
            System.out.printf(" Gross Monthly Income (Before Tax): PHP %.2f%n", grossIncome);
            System.out.printf(" Taxable Income (After SSS, PhilHealth, Pag-Ibig): PHP %.2f%n", taxableIncome);
            System.out.printf(" Net Monthly Income (After Tax, with Benefits): PHP %.2f%n", netPay);
            System.out.println("--------------------------------------------------------------");
            System.out.println(" Government Deductions:");
            System.out.printf(" SSS Contribution (Employee Share): PHP %.2f%n", govtSSS);
            System.out.printf(" Pag-IBIG Contribution: PHP %.2f%n", govtHDMF);
            System.out.printf(" PhilHealth Contribution: PHP %.2f%n", govtPhilHealth);
            System.out.printf(" BIR Withholding Tax (Monthly): PHP %.2f%n", govtBirTax);
            System.out.println("--------------------------------------------------------------");
            System.out.println(" De Minimis Benefits:");
            System.out.printf(" Monthly Rice Subsidy: PHP %.2f%n", riceSubsidy);
            System.out.printf(" Monthly Phone Allowance: PHP %.2f%n", phoneAllowance);
            System.out.printf(" Monthly Clothing Allowance: PHP %.2f%n", clothingAllowance);
            System.out.println("--------------------------------------------------------------");
            System.out.println(summary.getSummaryReport());
        }
    }
}
