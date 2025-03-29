package com.payroll;

import java.time.*;
import java.util.*;

/**
 * MonthlySummary - Computes an employee's monthly work hours, overtime, and deductions.
 */
public class MonthlySummary {
    private final EmployeeData employee;
    private float totalRegularHours = 0f; // Added declaration for Regular hours
    private float totalHolidayWorkedHours = 0f; // Added declaration for Holiday Worked Hours
    private float totalWorkHours = 0f, totalOvertime = 0f, totalLateDeductions = 0f, totalOvertimePay = 0f;
    private float totalHolidayPay = 0f, totalRestDayOTPay = 0f, totalLateHours = 0f;
    private final StringBuilder breakdownOutput = new StringBuilder();

    /**
     * Constructor to initialize monthly summary for an employee.
     * @param employee The {@link EmployeeData} object
     */
    public MonthlySummary(EmployeeData employee) {
        this.employee = employee;
    }

    /**
     * Adds daily work details to the monthly summary.
     * @param date The date
     * @param rawDailyWorkHours The raw amount of daily work hours the employee has
     * @param lateMinutes How many minutes the employee is late by
     * @param lateDeduction The late deduction
     * @param isHoliday Whether the employee worked on a holiday
     * @param isRestDay Whether the employee worked on their rest day
     * @param isHolidayRestDay Whether the employee worked on a holiday and their rest day
     * @param holidayMultiplier The holiday multiplier
     */
    public void addDailyWork(LocalDate date, float rawDailyWorkHours, float lateMinutes, float lateDeduction,
                         boolean isHoliday, boolean isRestDay, boolean isHolidayRestDay, float holidayMultiplier) {
        float lateHours = lateMinutes / 60f;
        totalLateHours += lateHours;

        float dailyWorkHours = Math.max(0f, rawDailyWorkHours - 1f); // subtract 1 hr lunch
        float regularWorkHours = Math.min(8f, dailyWorkHours);       // regular hours up to 8
        float overtimeHours = (dailyWorkHours >= 9f) ? dailyWorkHours - 8f : 0f;

        totalWorkHours += dailyWorkHours;
        totalRegularHours += regularWorkHours; // Accumulate regular hours separately
        totalOvertime += overtimeHours;
        totalLateDeductions += lateDeduction;

        float dailyHolidayPay = 0f;
        if (isHoliday) {
            totalHolidayWorkedHours += dailyWorkHours; // ✅ Track holiday hours
            dailyHolidayPay = dailyWorkHours * employee.getHourlyRate() * (holidayMultiplier - 1f);
            totalHolidayPay += dailyHolidayPay;
        }

        float dailyRestDayOTPay = 0f;
        if (isRestDay) {
            dailyRestDayOTPay = dailyWorkHours * employee.getHourlyRate() * 1.5f;
            totalRestDayOTPay += dailyRestDayOTPay;
        }

        float overtimePay = overtimeHours * employee.getHourlyRate() * 1.25f;
        totalOvertimePay += overtimePay;

        // Determine the correct work type label for the breakdown report
        String workTypeLabel;

        if (isHolidayRestDay) {
            // Case: Employee worked on both a holiday and their rest day
            workTypeLabel = "Holiday + Rest Day";
        } else if (isHoliday) {
            // Case: Employee worked on a holiday (determine type by holiday multiplier)
            workTypeLabel = switch(holidayMultiplier){
                case 2.00f -> "Regular Holiday";
                case 1.30f -> "Special Holiday";
                default -> "Holiday"; // Fallback for any other special cases
            };
        } else if (isRestDay) {
            // Case: Employee worked on their scheduled rest day
            workTypeLabel = "Rest Day";
        } else {
            // Case: Regular working day
            workTypeLabel = "Regular Workday";
        }

        // Append the breakdown log entry with aligned formatting
        breakdownOutput.append(String.format(" %s | %-17s | %8.2f | %.2f   | PHP %8.2f | %12.2f | PHP %8.2f%n",
            date.toString(), workTypeLabel, overtimeHours, 1.25f, overtimePay, lateMinutes, lateDeduction));
    }

    /**
     * Returns the total number of work hours (excluding lunch breaks) for the employee within the payroll period.
     * @return Total worked hours as a float value.
     */
    public float getTotalWorkHours() { return totalWorkHours; }

    /**
     * Returns the total number of overtime hours worked beyond the standard shift (8 hours/day).
     * @return Total overtime hours as a float value.
     */
    public float getTotalOvertime() { return totalOvertime; }

    /**
     * Returns the total amount of salary deductions due to employee lateness.
     * @return Total late deductions in PHP.
     */
    public float getTotalLateDeductions() { return totalLateDeductions; }

    /**
     * Returns the total monetary compensation received for overtime hours.
     * @return Total overtime pay in PHP.
     */
    public float getTotalOvertimePay() { return totalOvertimePay; }

    /**
     * Returns the total additional compensation received for working during holidays.
     * @return Total holiday pay in PHP.
     */
    public float getTotalHolidayPay() { return totalHolidayPay; }

    /**
     * Returns the total additional compensation received for working on rest days.
     * @return Total rest day overtime pay in PHP.
     */
    public float getTotalRestDayOTPay() { return totalRestDayOTPay; }

    /**
     * Returns the employee's personal and payroll-related data object.
     * @return EmployeeData object containing employee ID, name, rate, and other attributes.
     */
    public EmployeeData getEmployee() { return employee; }

    /**
     * Returns summary data in key-value map for external use (e.g., payroll report).
     * @return a {@link Map} of summary data.
     */
    public Map<String, Object> getSummaryData() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalWorkHours", totalWorkHours);
        data.put("totalRegularWorkHours", totalRegularHours); // Fixed to return correct regular hours
        data.put("totalHolidayWorkHours", totalHolidayWorkedHours); // Updated tracker to so that Holiday Work hours won't print 0.00 Values
        data.put("totalOvertime", totalOvertime);
        data.put("totalOvertimePay", totalOvertimePay);
        data.put("totalHolidayPay", totalHolidayPay);
        data.put("totalRestDayOTPay", totalRestDayOTPay);
        data.put("totalLateHours", totalLateHours);
        data.put("totalLateDeductions", totalLateDeductions);
        return data;
    }

    /**
     * Generates a summary report of work hours and deductions.
     * @return a summary report.
     */
    public String getSummaryReport() {
        return """
               
               --------------------------------------------------------------
                Summary of Monthly Work Hours & Deductions 
               --------------------------------------------------------------
               """
            + String.format(" Total Worked Hours      : %.2f%n", totalWorkHours)
            + String.format(" Total Regular Hours     : %.2f%n", totalRegularHours)
            + String.format(" Total Overtime Hours    : %.2f%n", totalOvertime)
            + String.format(" Total Holiday Hours      : %.2f%n", totalHolidayWorkedHours)    
            + String.format(" Total Overtime Pay      : PHP %.2f%n", totalOvertimePay)
            + String.format(" Total Holiday Pay       : PHP %.2f%n", totalHolidayPay)
            + String.format(" Total Rest Day OT Pay   : PHP %.2f%n", totalRestDayOTPay)
            + String.format(" Total Late Hours        : %.2f%n", totalLateHours)
            + String.format(" Total Late Deductions   : PHP %.2f%n", totalLateDeductions)
            + "--------------------------------------------------------------\n"
            + getBreakdownReport();
    }

    /**
     * Returns detailed daily breakdown of overtime and deductions.
     * @return A breakdown of overtime & unpaid work hour deductions. "No overtime or late deductions recorded." otherwise.
     */
    public String getBreakdownReport() {
        if (breakdownOutput.length() == 0) {
            return """
                   
                   ---------------- Overtime & Unpaid Work Hour Deductions Breakdown ----------------
                        Day     |     Work Type    | OT Hours |OT Rate |    OT Pay    |  Late Minutes|  Late Deduction
                   ----------------------------------------------------------------------------------------
                   No overtime or late deductions recorded.
                   """;
        }
        return """
               
               ---------------- Overtime & Unpaid Work Hour Deductions Breakdown ----------------
                    Day     |     Work Type    | OT Hours |OT Rate |    OT Pay    |  Late Minutes|  Late Deduction
               ----------------------------------------------------------------------------------------
               """
            + breakdownOutput.toString()
            + "----------------------------------------------------------------------------------------\n";
    }

    /**
     * Combines employee header, summary, and breakdown.
     * @return a full breakdown report.
     */
    public String generateFullBreakdownReport() {
        StringBuilder report = new StringBuilder();

        report.append("========== Monthly Work Summary Report ==========\n");
        report.append("Employee Name         : ").append(employee.getName()).append("\n");
        report.append("Employee ID           : ").append(employee.getEmpId()).append("\n");
        report.append("Hourly Rate           : PHP ").append(String.format("%.2f", employee.getHourlyRate())).append("\n");
        report.append("--------------------------------------------------\n");
        report.append(String.format("Total Work Hours      : %.2f\n", totalWorkHours));
        report.append(String.format("Total Regular Hours   : %.2f\n", totalRegularHours));
        report.append(String.format("Total Overtime Hours  : %.2f\n", totalOvertime));
        report.append(String.format("Total Holiday Hours      : %.2f\n", totalHolidayWorkedHours));
        report.append(String.format("Total Overtime Pay    : PHP %.2f\n", totalOvertimePay));
        report.append(String.format("Total Holiday Pay     : PHP %.2f\n", totalHolidayPay));
        report.append(String.format("Total Rest Day OT Pay : PHP %.2f\n", totalRestDayOTPay));
        report.append(String.format("Total Late Hours      : %.2f\n", totalLateHours));
        report.append(String.format("Late Deductions       : PHP %.2f\n", totalLateDeductions));
        report.append("==================================================\n");

        report.append(getBreakdownReport());
        return report.toString();
    }

    /**
     * Computes and aggregates monthly work summaries for all employees based on their time entries.
     *
     * This method performs the following steps:
     *  - Iterates over all time entries within the specified date range.
     *  - Groups entries by employee and calendar month (e.g., "EMP001-2024-06").
     *  - Calculates daily work hours, late minutes, and corresponding late deductions.
     *  - Determines workday classifications such as Regular Holiday, Special Holiday, Rest Day, or Regular Workday.
     *  - Computes overtime, holiday pay, rest day OT pay, and populates a breakdown report per employee.
     *
     * @param employees     A map of employee ID to EmployeeData, loaded from the employee CSV.
     * @param timeEntries   A list of time entries (clock in/out) to be processed.
     * @return A map of employee-month keys (e.g., "EMP001-2024-06") to their MonthlySummary objects,
     *         containing computed totals and breakdowns for payroll processing.
     */
    public static Map<String, MonthlySummary> calculateWorkedHours(
        Map<String, EmployeeData> employees,
        List<TimeEntry> timeEntries) {

        Map<String, MonthlySummary> monthlySummaries = new HashMap<>();

        for (TimeEntry entry : timeEntries) {
            EmployeeData emp = employees.get(entry.getEmpId());
            if (emp == null) continue;

            YearMonth yearMonth = YearMonth.from(entry.getClockIn().toLocalDate());
            String monthlyKey = entry.getEmpId() + "-" + yearMonth;

            monthlySummaries.putIfAbsent(monthlyKey, new MonthlySummary(emp));
            MonthlySummary summary = monthlySummaries.get(monthlyKey);

            float rawDailyWorkHours = Math.max(0f, entry.getHoursWorked());
            float lateMinutes = Math.max(0f, Duration.between(LocalTime.of(8, 30), entry.getClockIn().toLocalTime()).toMinutes());
            float lateDeduction = (lateMinutes > 0f) ? (lateMinutes / 60f) * emp.getHourlyRate() : 0f;

            boolean isHoliday = entry.isRegularHoliday() || entry.isSpecialNonWorking();
            boolean isRestDay = entry.isRestDay();
            boolean isHolidayRestDay = entry.isHolidayRestDay();
            float holidayMultiplier = entry.getHolidayMultiplier();

            summary.addDailyWork(entry.getClockIn().toLocalDate(), rawDailyWorkHours, lateMinutes, lateDeduction,
                                 isHoliday, isRestDay, isHolidayRestDay, holidayMultiplier);
        }

        return monthlySummaries;
    }
}
