// Main - Final Calculation of Payroll
package com.payroll;

import java.util.*;

public class MotorPHPayrollG3 {
    public static void main(String[] args) {
        System.out.println("Starting Payroll System...");

        //Load Employee Data
        Map<String, EmployeeData> employees = EmployeeData.loadEmployeeData("src/com/payroll/EmployeeData.csv");
        if (employees.isEmpty()) {
            System.err.println("No employees loaded. Exiting...");
            return;
        }

        //Load De Minimis Benefits from EmployeeData.csv
        Map<String, DeMinimisBenefits> benefits = DeMinimisBenefits.loadBenefits("src/com/payroll/EmployeeData.csv");

        //Load Time Entries
        List<TimeEntry> timeEntries = TimeEntry.loadTimeEntries("src/com/payroll/EmployeeTimeEntries.csv");
        if (timeEntries.isEmpty()) {
            System.err.println("No time entries loaded. Exiting...");
            return;
        }

        //Calculate Weekly Worked Hours
        Map<String, WeeklySummary> weeklySummaries = WeeklySummary.calculateWorkedHours(employees, timeEntries);

        //Loop through each employee and compute payroll
        for (Map.Entry<String, WeeklySummary> entry : weeklySummaries.entrySet()) {
            String weeklyKey = entry.getKey();
            WeeklySummary summary = entry.getValue();
            EmployeeData employee = summary.getEmployee();

            //Compute De Minimis Benefits directly
            double weeklyRiceSubsidy = benefits.getOrDefault(employee.getEmpId(), new DeMinimisBenefits(employee.getEmpId(), 0, 0, 0)).getRiceSubsidy() / 4;
            double weeklyPhoneAllowance = benefits.getOrDefault(employee.getEmpId(), new DeMinimisBenefits(employee.getEmpId(), 0, 0, 0)).getPhoneAllowance() / 4;
            double weeklyClothingAllowance = benefits.getOrDefault(employee.getEmpId(), new DeMinimisBenefits(employee.getEmpId(), 0, 0, 0)).getClothingAllowance() / 4;
            double totalDeMinimisBenefits = weeklyRiceSubsidy + weeklyPhoneAllowance + weeklyClothingAllowance;

            //Step 1: Compute Weekly Salary (Before Deductions)
            double regularPay = summary.getTotalWorkHours() * employee.getHourlyRate();
            double overtimePay = summary.getTotalOvertime() * employee.getHourlyRate() * 1.25;
            double holidayPay = summary.getTotalHolidayPay();
            double restDayOvertimePay = summary.getTotalRestDayOTPay();

            double grossIncome = regularPay + overtimePay + holidayPay + restDayOvertimePay; //Gross Pay BEFORE tax

            //Step 2: Compute Government Deductions
            double govtSSS = GovernmentDeductions.calculateSSS(grossIncome);
            double govtPhilHealth = GovernmentDeductions.calculatePhilHealth(grossIncome);
            double govtHDMF = GovernmentDeductions.calculatePagibig(grossIncome);

            //Step 3: Calculate Taxable Income (Gross - SSS - PhilHealth - Pag-Ibig)
            double taxableIncome = grossIncome - (govtSSS + govtPhilHealth + govtHDMF);

            //Step 4: Compute BIR Tax
            double govtBirTax = GovernmentDeductions.calculateBIR(taxableIncome);

            //Step 5: Compute Net Pay (After Tax, Add De Minimis Benefits)
            double totalDeductions = govtSSS + govtHDMF + govtPhilHealth + govtBirTax + summary.getTotalLateDeductions();
            double netPay = (grossIncome - totalDeductions) + totalDeMinimisBenefits;  //Add De Minimis AFTER tax

            //Final Payroll Report
            System.out.println("--------------------------------------------------------------");
            System.out.println("---------------- FINAL PAYROLL REPORT (WEEKLY) ----------------");
            System.out.printf(" Employee ID: %s | Name: %s | DOB: %s%n", 
                employee.getEmpId(), employee.getName(), employee.getDob());
            // ✅ Include Hourly Rate, Status, and Position
            System.out.printf(" Hourly Rate: PHP %.2f | Status: %s | Position: %s%n", 
            employee.getHourlyRate(), employee.getStatus(), employee.getPosition());
            //Extract and Display Week Number
            String[] keyParts = weeklyKey.split("-");
            String weekNumber = (keyParts.length > 1) ? keyParts[1] : "Unknown";
            System.out.printf(" Week Number: %s%n", weekNumber);
            System.out.println("--------------------------------------------------------------");
            System.out.printf(" Worked Hours (Per Week): %.2f hours%n", summary.getTotalWorkHours());
            System.out.printf(" Overtime Hours (Per Week): %.2f hours%n", summary.getTotalOvertime());
            System.out.printf(" Gross Weekly Income (Before Tax): PHP %.2f%n", grossIncome);
            System.out.printf(" Taxable Income (After SSS, PhilHealth, Pag-Ibig): PHP %.2f%n", taxableIncome);
            System.out.printf(" Net Weekly Income (After Tax, with Benefits): PHP %.2f%n", netPay);
            System.out.println("--------------------------------------------------------------");
            System.out.println(" Government Deductions:");
            System.out.printf(" SSS Contribution (Employee Share): PHP %.2f%n", govtSSS);
            System.out.printf(" Pag-IBIG Contribution: PHP %.2f%n", govtHDMF);
            System.out.printf(" PhilHealth Contribution: PHP %.2f%n", govtPhilHealth);
            System.out.printf(" BIR Withholding Tax (Weekly): PHP %.2f%n", govtBirTax);
            System.out.println("--------------------------------------------------------------");
            System.out.println(" De Minimis Benefits:");
            System.out.printf(" Weekly Rice Subsidy: PHP %.2f%n", weeklyRiceSubsidy);
            System.out.printf(" Weekly Phone Allowance: PHP %.2f%n", weeklyPhoneAllowance);
            System.out.printf(" Weekly Clothing Allowance: PHP %.2f%n", weeklyClothingAllowance);
            System.out.println("--------------------------------------------------------------");
            System.out.println(summary.getSummaryReport());
        }
    }
}

