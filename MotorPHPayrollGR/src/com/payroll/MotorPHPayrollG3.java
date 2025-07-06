package com.payroll;

import com.gui.Home.LoginForm;
import com.gui.Payroll.DtrCsvUtil;
import java.io.IOException;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import javax.swing.JOptionPane;

/**
 * MotorPHPayrollG3 - Main payroll processing system. Computes monthly salaries,
 * government deductions, and de minimis benefits.
 */
public class MotorPHPayrollG3 {

    public static void main(String[] args) {
        new LoginForm();
    }

    public static Object[] runPayrollSearch(LocalDate startDate, LocalDate endDate, String inputEmpId) throws IOException {

        // Load Employee Dataa
        Map<String, EmployeeData> employees = EmployeeData.loadEmployeeData("src/com/csv/EmployeeData.csv");
        if (employees.isEmpty()) {
            System.err.println("No employees loaded. Exiting...");
            return null;
        }

        // Load Holiday Calendar BEFORE anything else
        HolidayCalendar.loadHolidaysFromCSV("src/com/csv/HolidayCalendar.csv");

        // Load De Minimis Benefits
        Map<String, DeMinimisBenefits> benefits = DeMinimisBenefits.loadBenefits("src/com/csv/EmployeeData.csv");

        // Load Time Entries for this employee only (NEW)
        String timeEntryFilePath = "src/com/csv/DTR/" + inputEmpId + ".csv";
        List<TimeEntry> timeEntries = TimeEntry.loadTimeEntries(timeEntryFilePath);

        if (timeEntries.isEmpty()) {
            System.err.println("No time entries loaded for employee " + inputEmpId + ". Exiting...");
            showErrorDialog("No time entries found for Employee ID: " + inputEmpId);
            return null;
        }

        // Filter time entries by date range
        List<TimeEntry> filteredTimeEntries = TimeEntry.filterTimeEntriesByDate(timeEntries, startDate, endDate);
        if (filteredTimeEntries.isEmpty()) {
            System.out.println("No time entries found within the specified period.");
            showErrorDialog("No time entries found for Employee ID: " + inputEmpId + " within selected date range.");
            return null;
        }
// ── NEW: exclude anything not approved in BOTH columns ───────────
        Set<LocalDate> fullyApproved = collectFullyApprovedDates(inputEmpId);
        filteredTimeEntries.removeIf(te -> !fullyApproved.contains(te.getLogDate()));
        if (filteredTimeEntries.isEmpty()) {
            showErrorDialog("No fully-approved DTR + Payroll entries in the chosen range.");
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

    /* ------------------------------------------------------------------
 * Returns every log-date whose DTR *and* Payroll status are BOTH
 * “Approved” inside src/com/csv/DTR/{empId}.csv
 * -- DTR Status  = column 8
 * -- Payroll Status = column 11
 * ---------------------------------------------------------------- */
    private static Set<LocalDate> collectFullyApprovedDates(String empId)
            throws IOException {
        Set<LocalDate> ok = new HashSet<>();
        DateTimeFormatter CSV_DF = DateTimeFormatter.ofPattern("M/d/yyyy");
        for (String[] r : DtrCsvUtil.readAll(empId)) {                  // :contentReference[oaicite:0]{index=0}
            if (r.length > 11
                    && "Approved".equalsIgnoreCase(r[8])
                    && "Approved".equalsIgnoreCase(r[11])) {
                ok.add(LocalDate.parse(r[1].trim(), CSV_DF));
            }
        }
        return ok;
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
