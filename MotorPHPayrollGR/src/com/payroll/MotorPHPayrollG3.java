package com.payroll;

import java.util.*;
import java.time.*;

/**
 * MotorPHPayrollG3 - Main payroll processing system.
 * Computes monthly salaries, government deductions, and de minimis benefits.
 */
public class MotorPHPayrollG3 {

    public static void main(String[] args) {
        System.out.println("Starting Payroll System...");
        Scanner scanner = new Scanner(System.in);

        // Load Employee Data
        Map<String, EmployeeData> employees = EmployeeData.loadEmployeeData("src/com/payroll/EmployeeData.csv");
        if (employees.isEmpty()) {
            System.err.println("No employees loaded. Exiting...");
            return;
        }

        // Load Holiday Calendar BEFORE anything else
        HolidayCalendar.loadHolidaysFromCSV("src/com/payroll/HolidayCalendar.csv");

        // Load De Minimis Benefits
        Map<String, DeMinimisBenefits> benefits = DeMinimisBenefits.loadBenefits("src/com/payroll/EmployeeData.csv");

        // Load Time Entries
        List<TimeEntry> timeEntries = TimeEntry.loadTimeEntries("src/com/payroll/EmployeeTimeEntries.csv");
        if (timeEntries.isEmpty()) {
            System.err.println("No time entries loaded. Exiting...");
            return;
        }

        // Prompt user for Start and End Dates
        LocalDate startDate = null, endDate = null;

        // Prompt Start Date until valid
        while (startDate == null) {
            try {
                System.out.print("Enter Start Date (YYYY-MM-DD): ");
                startDate = LocalDate.parse(scanner.nextLine().trim());
            } catch (Exception e) {
                System.out.println("Invalid start date format. Please enter date in YYYY-MM-DD format.");
            }
        }

        // Prompt End Date until valid and after Start Date
        while (endDate == null || endDate.isBefore(startDate)) {
            try {
                System.out.print("Enter End Date (YYYY-MM-DD): ");
                endDate = LocalDate.parse(scanner.nextLine().trim());
                if (endDate.isBefore(startDate)) {
                    System.out.println("Error: End date cannot be before start date. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("Invalid end date format. Please enter date in YYYY-MM-DD format.");
            }
        }

        // Filter time entries based on user input
        List<TimeEntry> filteredTimeEntries = TimeEntry.filterTimeEntriesByDate(timeEntries, startDate, endDate);
        if (filteredTimeEntries.isEmpty()) {
            System.out.println("No time entries found within the specified period.");
            return;
        }

        // Calculate Monthly Worked Hours only for the filtered entries
        Map<String, MonthlySummary> monthlySummaries = MonthlySummary.calculateWorkedHours(employees, filteredTimeEntries);

        // Prompt for Employee ID until found in the summary
        boolean employeeFound = false;
        while (!employeeFound) {
            System.out.print("Enter the 5-digit Employee ID to generate the compensation details: ");
            String inputEmpId = scanner.nextLine().trim();

            for (Map.Entry<String, MonthlySummary> entry : monthlySummaries.entrySet()) {
                MonthlySummary summary = entry.getValue();
                EmployeeData employee = summary.getEmployee();

                if (employee.getEmpId().equals(inputEmpId)) {
                    employeeFound = true;
                    printPayrollReport(summary, employee, benefits, startDate, endDate);
                    break;
                }
            }

            if (!employeeFound) {
                System.out.println("No payroll data found for Employee ID: " + inputEmpId + ". Please try again.");
            }
        }

        scanner.close();
    }

    /**
     * Prints the payroll report for an employee.
     */
    public static void printPayrollReport(MonthlySummary summary, EmployeeData employee,
                                          Map<String, DeMinimisBenefits> benefits, LocalDate startDate, LocalDate endDate) {
        // Compute De Minimis Benefits (Monthly)
        float riceSubsidy = benefits.getOrDefault(employee.getEmpId(), new DeMinimisBenefits(employee.getEmpId(), 0f, 0f, 0f)).getRiceSubsidy();
        float phoneAllowance = benefits.getOrDefault(employee.getEmpId(), new DeMinimisBenefits(employee.getEmpId(), 0f, 0f, 0f)).getPhoneAllowance();
        float clothingAllowance = benefits.getOrDefault(employee.getEmpId(), new DeMinimisBenefits(employee.getEmpId(), 0f, 0f, 0f)).getClothingAllowance();
        float totalDeMinimisBenefits = riceSubsidy + phoneAllowance + clothingAllowance;

        // Compute Monthly Salary (Before Deductions)
        float regularPay = summary.getTotalWorkHours() * employee.getHourlyRate();
        float overtimePay = summary.getTotalOvertimePay();
        float holidayPay = summary.getTotalHolidayPay();
        float restDayOvertimePay = summary.getTotalRestDayOTPay();
        float grossIncome = regularPay + overtimePay + holidayPay + restDayOvertimePay;

        // Compute Government Deductions
        float basicSalary = employee.getBasicSalary();
        if (basicSalary <= 0f) {
            System.out.println("[Warning] Missing or invalid Basic Salary. Using fallback estimate.");
            basicSalary = employee.getHourlyRate() * 8 * 22;
        }

        float govtSSS = GovernmentDeductions.calculateSSS(basicSalary);
        float govtPhilHealth = GovernmentDeductions.calculatePhilHealth(grossIncome);
        float govtHDMF = GovernmentDeductions.calculatePagibig(grossIncome);

        // Compute Taxable Income
        float taxableIncome = grossIncome - (govtSSS + govtPhilHealth + govtHDMF);

        // Compute BIR Tax
        float govtBirTax = GovernmentDeductions.calculateBIR(taxableIncome);

        // Compute Total Deductions and Net Pay
        float totalGovtDeductions = govtSSS + govtHDMF + govtPhilHealth + govtBirTax;
        float totalDeductions = totalGovtDeductions + summary.getTotalLateDeductions();
        float netPay = (grossIncome - totalDeductions) + totalDeMinimisBenefits;

        // Print Payroll Report
        System.out.println("--------------------------------------------------------------");
        System.out.println("---------------- FINAL PAYROLL REPORT (MONTHLY) ----------------");
        System.out.printf(" Employee ID: %s | Name: %s | DOB: %s%n",
                employee.getEmpId(), employee.getName(), employee.getDob());
        System.out.printf(" Hourly Rate: PHP %,.2f | Status: %s | Position: %s%n",
                employee.getHourlyRate(), employee.getStatus(), employee.getPosition());
        System.out.printf(" Payroll Period: %s to %s%n", startDate, endDate);
        System.out.println("--------------------------------------------------------------");
        System.out.printf(" Worked Hours               : %.2f hours%n", summary.getTotalWorkHours());
        System.out.printf(" Overtime Hours             : %.2f hours%n", summary.getTotalOvertime());
        System.out.printf(" Gross Monthly Income       : PHP %,.2f%n", grossIncome);
        System.out.printf(" Taxable Income (after SSS, PhilHealth, Pag-IBIG): PHP %,.2f%n", taxableIncome);
        System.out.println("--------------------------------------------------------------");

        // Deduction Section - Accounting Style
        System.out.println(" Government Deductions:");
        System.out.printf(" - SSS Contribution         : PHP (%,.2f)%n", govtSSS);
        System.out.printf(" - Pag-IBIG Contribution    : PHP (%,.2f)%n", govtHDMF);
        System.out.printf(" - PhilHealth Contribution  : PHP (%,.2f)%n", govtPhilHealth);
        System.out.printf(" - BIR Withholding Tax      : PHP (%,.2f)%n", govtBirTax);
        System.out.println("--------------------------------------------------------------");
        System.out.printf(" Total Government Deductions: PHP (%,.2f)%n", totalGovtDeductions);
        System.out.println("--------------------------------------------------------------");

        // Added Late  Deductions for accounting and audit transparency 
        System.out.println(" Other Deductions:");
        System.out.printf(" - Late Deductions          : PHP (%,.2f)%n", summary.getTotalLateDeductions());
        System.out.println("--------------------------------------------------------------");
        System.out.printf(" Total Deductions           : PHP (%,.2f)%n", totalDeductions);
        System.out.println("--------------------------------------------------------------");

        // De Minimis Section
        System.out.println(" De Minimis Benefits:");
        System.out.printf(" Rice Subsidy              : PHP %,.2f%n", riceSubsidy);
        System.out.printf(" Phone Allowance           : PHP %,.2f%n", phoneAllowance);
        System.out.printf(" Clothing Allowance        : PHP %,.2f%n", clothingAllowance);
        System.out.println("--------------------------------------------------------------");
        System.out.printf(" Net Monthly Income (After Tax, with Benefits): PHP %,.2f%n", netPay);
        System.out.println("--------------------------------------------------------------");

        printSummaryReport(summary);
    }

    /**
     * Prints the summary report for an employee.
     */
    public static void printSummaryReport(MonthlySummary summary) {
        System.out.println("\n---------------- Monthly Work Hours & Deductions ----------------");
        Map<String, Object> data = summary.getSummaryData();
        System.out.printf(" Total Worked Hours      : %.2f hours%n", data.get("totalWorkHours"));
        System.out.printf(" Regular Worked Hours    : %.2f hours%n", data.get("totalRegularWorkHours"));
        System.out.printf(" Overtime Hours          : %.2f hours%n", data.get("totalOvertime"));
        System.out.printf(" Holiday Worked Hours    : %.2f hours%n", data.get("totalHolidayWorkHours"));
        System.out.printf(" Late Hours              : %.2f hours%n", summary.getSummaryData().get("totalLateHours")); //Added Late Hours for accounting and audit transparency 
        System.out.printf(" Overtime Pay            : PHP %,.2f%n", data.get("totalOvertimePay"));
        System.out.printf(" Holiday Pay             : PHP %,.2f%n", data.get("totalHolidayPay"));
        System.out.printf(" Rest Day OT Pay         : PHP %,.2f%n", data.get("totalRestDayOTPay"));
        System.out.println("--------------------------------------------------------------");

        printBreakdownReport(summary);
    }

    /**
     * Prints the breakdown log of daily work and deductions.
     */
    public static void printBreakdownReport(MonthlySummary summary) {
        System.out.println(summary.getBreakdownReport());
    }
}
