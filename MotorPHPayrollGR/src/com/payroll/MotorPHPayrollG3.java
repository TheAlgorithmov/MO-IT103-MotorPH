package com.payroll;

import java.util.*;
import java.time.*;
import com.gui.*;
import javax.swing.JOptionPane;

/**
 * MotorPHPayrollG3 - Main payroll processing system.
 * Computes monthly salaries, government deductions, and de minimis benefits.
 */
public class MotorPHPayrollG3 {
    
    public static void main(String[] args) {
        new LoginForm();
    }

    public static Object[] runPayrollSearch(LocalDate startDate, LocalDate endDate, String inputEmpId) {
        
        // Load Employee Dataa
        Map<String, EmployeeData> employees = EmployeeData.loadEmployeeData("src/com/payroll/EmployeeData.csv");
        if (employees.isEmpty()) {
            System.err.println("No employees loaded. Exiting...");
            return null;
        }

        // Load Holiday Calendar BEFORE anything else
        HolidayCalendar.loadHolidaysFromCSV("src/com/payroll/HolidayCalendar.csv");

        // Load De Minimis Benefits
        Map<String, DeMinimisBenefits> benefits = DeMinimisBenefits.loadBenefits("src/com/payroll/EmployeeData.csv");

        // Load Time Entries
        List<TimeEntry> timeEntries = TimeEntry.loadTimeEntries("src/com/payroll/EmployeeTimeEntries.csv");
        if (timeEntries.isEmpty()) {
            System.err.println("No time entries loaded. Exiting...");
            return null;
        }
        
        // Filter time entries based on user input
        List<TimeEntry> filteredTimeEntries = TimeEntry.filterTimeEntriesByDate(timeEntries, startDate, endDate);
        if (filteredTimeEntries.isEmpty()) {
            System.out.println("No time entries found within the specified period.");
            return null;
        }

        // Calculate Monthly Worked Hours only for the filtered entries
        Map<String, MonthlySummary> monthlySummaries = MonthlySummary.calculateWorkedHours(employees, filteredTimeEntries);        
        
        Object[] payrollReport = new Object[22];
        
        // Prompt for Employee ID until found in the summary
        boolean employeeFound = false;
        while (!employeeFound) {

            for (Map.Entry<String, MonthlySummary> entry : monthlySummaries.entrySet()) {
                MonthlySummary summary = entry.getValue();
                EmployeeData employee = summary.getEmployee();

                if (employee.getEmpId().equals(inputEmpId)) {
                    employeeFound = true;
                    payrollReport = printPayrollReport(summary, employee, benefits, startDate, endDate);
                    
                }
            }

            if (!employeeFound) {
                // System.out.println("No payroll data found for Employee ID: " + inputEmpId + ". Please try again.");
                showErrorDialog("No payroll data found for Employee ID: " + inputEmpId);
                return null;
            }
        }
        
        return payrollReport;
    }
    
    public static Object[] printPayrollReport(MonthlySummary summary, EmployeeData employee,
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
            // System.out.println("[Warning] Missing or invalid Basic Salary. Using fallback estimate.");
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
        
        // Store Payroll Slip values to an object array
        Object[] data = new Object[22];
        data[0] = employee.getEmpId();
        data[1] = employee.getName();
        data[2] = employee.getDob();
        data[3] = employee.getPosition();
        data[4] = employee.getStatus();
        data[5] = employee.getHourlyRate();
        data[6] = startDate;
        data[7] = endDate;
        data[8] = summary.getTotalWorkHours();
        data[9] = summary.getTotalOvertime();
        data[10] = grossIncome;
        data[11] = govtSSS;
        data[12] = govtHDMF;
        data[13] = govtPhilHealth;
        data[14] = taxableIncome;
        data[15] = govtBirTax;
        data[16] = summary.getTotalLateDeductions();
        data[17] = totalDeductions;
        data[18] = riceSubsidy;
        data[19] = phoneAllowance;
        data[20] = clothingAllowance;
        data[21] = netPay;
        
        return data;
       
    }
    
    public static void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(null, message, "Error.", JOptionPane.ERROR_MESSAGE);
    }
}
