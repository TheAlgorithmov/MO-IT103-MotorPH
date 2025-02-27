package com.payroll;

import java.time.*;
import java.time.temporal.WeekFields;
import java.util.*;

/**
 * WeeklySummary - Computes an employee's weekly work hours, overtime, and deductions.
 */
public class WeeklySummary {
    private final EmployeeData employee;
    private float totalWorkHours = 0f, totalOvertime = 0f, totalLateDeductions = 0f, totalOvertimePay = 0f;
    private float totalHolidayPay = 0f, totalRestDayOTPay = 0f, totalLateHours = 0f;
    private final StringBuilder breakdownOutput = new StringBuilder();

    /**
     * Constructor to initialize weekly summary for an employee.
     * 
     * @param employee The employee for whom the weekly summary is generated.
     */
    public WeeklySummary(EmployeeData employee) {
        this.employee = employee;
    }

    /**
     * Adds daily work details to the weekly summary.
     * 
     * @param date               Work date
     * @param rawDailyWorkHours  Hours worked before deductions
     * @param lateMinutes        Late minutes
     * @param lateDeduction      Salary deduction due to lateness
     * @param isHoliday          Whether the day is a holiday
     * @param isRestDay          Whether the day is a scheduled rest day
     * @param isHolidayRestDay   Whether the day is a holiday that falls on a rest day
     * @param holidayMultiplier  Multiplier for holiday pay
     */
    public void addDailyWork(LocalDate date, float rawDailyWorkHours, float lateMinutes, float lateDeduction,
                             boolean isHoliday, boolean isRestDay, boolean isHolidayRestDay, float holidayMultiplier) {
        // Convert late minutes to hours
        float lateHours = lateMinutes / 60f;
        totalLateHours += lateHours;

        // Deduct 1-hour unpaid lunch from raw work hours
        float dailyWorkHours = Math.max(0f, rawDailyWorkHours - 1f);

        // Regular work shift is 8 hours
        float regularWorkHours = Math.min(8f, dailyWorkHours);
        float overtimeHours = (dailyWorkHours >= 9f) ? dailyWorkHours - 8f : 0f;

        totalWorkHours += dailyWorkHours;
        totalOvertime += overtimeHours;
        totalLateDeductions += lateDeduction;

        // Compute Holiday Pay (if applicable)
        float dailyHolidayPay = 0f;
        if (isHoliday) {
            dailyHolidayPay = dailyWorkHours * employee.getHourlyRate() * (holidayMultiplier - 1f);
            totalHolidayPay += dailyHolidayPay;
        }

        // Compute Rest Day OT Pay (if applicable)
        float dailyRestDayOTPay = 0f;
        if (isRestDay) {
            dailyRestDayOTPay = dailyWorkHours * employee.getHourlyRate() * 1.5f;
            totalRestDayOTPay += dailyRestDayOTPay;
        }

        // Compute Overtime Pay
        float overtimePay = overtimeHours * employee.getHourlyRate() * 1.25f;
        totalOvertimePay += overtimePay;

        // Add breakdown log
        breakdownOutput.append(String.format(" %s | %-17s | %8.2f | %.2f   | PHP %8.2f | %12.2f | PHP %8.2f%n",
            date.toString(), (isHoliday ? "Holiday" : isRestDay ? "Rest Day" : "Regular Workday"),
            overtimeHours, 1.25f, overtimePay, lateMinutes, lateDeduction));
    }

    // ** Getters for Payroll Calculation **
    public float getTotalWorkHours() { return totalWorkHours; }
    public float getTotalOvertime() { return totalOvertime; }
    public float getTotalLateDeductions() { return totalLateDeductions; }
    public float getTotalOvertimePay() { return totalOvertimePay; }
    public float getTotalHolidayPay() { return totalHolidayPay; }
    public float getTotalRestDayOTPay() { return totalRestDayOTPay; }
    public EmployeeData getEmployee() { return employee; }

    /**
     * Generates a summary report for the employee's weekly worked hours.
     * 
     * @return A formatted string containing the work summary
     */
    public String getSummaryReport() {
        return "\n--------------------------------------------------------------\n"
            + " Summary of Work Hours & Deductions \n"
            + "--------------------------------------------------------------\n"
            + String.format(" Total Worked Hours: %.2f%n", totalWorkHours)
            + String.format(" Total Overtime Hours: %.2f%n", totalOvertime)
            + String.format(" Total Overtime Pay: PHP %.2f%n", totalOvertimePay)
            + String.format(" Total Holiday Pay: PHP %.2f%n", totalHolidayPay)
            + String.format(" Total Rest Day OT Pay: PHP %.2f%n", totalRestDayOTPay)
            + String.format(" Total Late Hours: %.2f%n", totalLateHours)
            + String.format(" Total Late Deductions: PHP %.2f%n", totalLateDeductions)
            + "--------------------------------------------------------------\n"
            + getBreakdownReport();
    }

    /**
     * Generates a breakdown report of daily work logs.
     * 
     * @return A formatted breakdown of work hours and deductions
     */
    public String getBreakdownReport() {
        if (breakdownOutput.length() == 0) {
            return "\n---------------- Overtime & Unpaid Work Hour Deductions Breakdown ----------------\n"
                + "     Day     |     Work Type    | OT Hours |OT Rate |    OT Pay    |  Late Minutes|  Late Deduction\n"
                + "----------------------------------------------------------------------------------------\n"
                + "No overtime or late deductions recorded.\n";
        }
        return "\n---------------- Overtime & Unpaid Work Hour Deductions Breakdown ----------------\n"
            + "     Day     |     Work Type    | OT Hours |OT Rate |    OT Pay    |  Late Minutes|  Late Deduction\n"
            + "----------------------------------------------------------------------------------------\n"
            + breakdownOutput.toString()
            + "----------------------------------------------------------------------------------------\n";
    }

    /**
     * Computes weekly worked hours for each employee from time entries.
     * 
     * @param employees    A map of employee data
     * @param timeEntries  A list of employee time entries
     * @return A map of weekly summaries for each employee
     */
    public static Map<String, WeeklySummary> calculateWorkedHours(
        Map<String, EmployeeData> employees, 
        List<TimeEntry> timeEntries) {  

        Map<String, WeeklySummary> weeklySummaries = new HashMap<>();

        for (TimeEntry entry : timeEntries) {
            EmployeeData emp = employees.get(entry.getEmpId());
            if (emp == null) continue;

            int weekOfYear = entry.getClockIn().get(WeekFields.of(Locale.getDefault()).weekOfYear());
            String weeklyKey = entry.getEmpId() + "-" + weekOfYear;  // Unique key per employee per week

            // Ensure we track weekly hours for each employee per week
            weeklySummaries.putIfAbsent(weeklyKey, new WeeklySummary(emp));
            WeeklySummary summary = weeklySummaries.get(weeklyKey);

            float dailyWorkHours = entry.getHoursWorked();
            float rawDailyWorkHours = Math.max(0f, dailyWorkHours);

            float overtime = (rawDailyWorkHours >= 9f) ? rawDailyWorkHours - 8f : 0f;
            float lateMinutes = Math.max(0f, Duration.between(LocalTime.of(8, 30), entry.getClockIn().toLocalTime()).toMinutes());
            float lateDeduction = (lateMinutes > 0f) ? (lateMinutes / 60f) * emp.getHourlyRate() : 0f;

            boolean isHoliday = entry.isRegularHoliday() || entry.isSpecialNonWorking();
            boolean isRestDay = entry.isRestDay();
            boolean isHolidayRestDay = entry.isHolidayRestDay();
            float holidayMultiplier = entry.getHolidayMultiplier();

            summary.addDailyWork(entry.getClockIn().toLocalDate(), rawDailyWorkHours, lateMinutes, lateDeduction,
                                 isHoliday, isRestDay, isHolidayRestDay, holidayMultiplier);
        }

        return weeklySummaries;
    }
}
