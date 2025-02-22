// Employee's Weekly Worked Hours Calculator
package com.payroll;

import java.time.*;
import java.time.temporal.WeekFields;
import java.util.*;

public class WeeklySummary {
    private final EmployeeData employee;
    private double totalWorkHours = 0, totalOvertime = 0, totalLateDeductions = 0, totalOvertimePay = 0;
    private double totalHolidayPay = 0, totalRestDayOTPay = 0;
    private final StringBuilder breakdownOutput = new StringBuilder();

    public WeeklySummary(EmployeeData employee) {
        this.employee = employee;
    }

    public void addDailyWork(LocalDate date, double rawDailyWorkHours, double lateMinutes, double lateDeduction,
                             boolean isHoliday, boolean isRestDay, boolean isHolidayRestDay, double holidayMultiplier) {

        //Deduct 1-hour unpaid lunch from raw work hours
        double dailyWorkHours = Math.max(0, rawDailyWorkHours - 1); // Ensure it doesn’t go negative

        //Regular work shift is 8 hours
        double regularWorkHours = Math.min(8, dailyWorkHours);
        double overtimeHours = (dailyWorkHours >= 9) ? dailyWorkHours - 8 : 0;  //OT applies only if 9+ hours worked

        totalWorkHours += dailyWorkHours;  //Track all worked hours (not capped at 40)
        totalOvertime += overtimeHours;    //Only valid overtime is counted
        totalLateDeductions += lateDeduction;

        //Compute Holiday Pay (if applicable)
        double dailyHolidayPay = 0;
        if (isHoliday) {
            dailyHolidayPay = dailyWorkHours * employee.getHourlyRate() * (holidayMultiplier - 1.0);
            totalHolidayPay += dailyHolidayPay;
        }

        //Compute Rest Day OT Pay (if applicable)
        double dailyRestDayOTPay = 0;
        if (isRestDay) {
            dailyRestDayOTPay = dailyWorkHours * employee.getHourlyRate() * 1.5;
            totalRestDayOTPay += dailyRestDayOTPay;
        }

        //Compute Overtime Pay
        double overtimePay = overtimeHours * employee.getHourlyRate() * 1.25;
        totalOvertimePay += overtimePay;

        //Add breakdown log
        breakdownOutput.append(String.format(" %s | %-17s | %8.2f | %.2f   | PHP %8.2f | %12.2f | PHP %8.2f%n",
                date.toString(), (isHoliday ? "Holiday" : isRestDay ? "Rest Day" : "Regular Workday"),
                overtimeHours, 1.25, overtimePay, lateMinutes, lateDeduction));
    }

    // Getters for Payroll Calculation
    public double getTotalWorkHours() { return totalWorkHours; }
    public double getTotalOvertime() { return totalOvertime; }
    public double getTotalLateDeductions() { return totalLateDeductions; }
    public double getTotalOvertimePay() { return totalOvertimePay; }
    public double getTotalHolidayPay() { return totalHolidayPay; }
    public double getTotalRestDayOTPay() { return totalRestDayOTPay; }
    public EmployeeData getEmployee() { return employee; }

    public String getSummaryReport() {
        return "\n--------------------------------------------------------------\n"
            + " Summary of Work Hours & Deductions \n"
            + "--------------------------------------------------------------\n"
            + String.format(" Total Worked Hours: %.2f%n", totalWorkHours)
            + String.format(" Total Overtime Hours: %.2f%n", totalOvertime)
            + String.format(" Total Overtime Pay: PHP %.2f%n", totalOvertimePay)
            + String.format(" Total Holiday Pay: PHP %.2f%n", totalHolidayPay)
            + String.format(" Total Rest Day OT Pay: PHP %.2f%n", totalRestDayOTPay)
            + String.format(" Total Late Deductions: PHP %.2f%n", totalLateDeductions)
            + "--------------------------------------------------------------\n"
            + getBreakdownReport();
    }

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

    //Fixed Weekly Summary Calculation
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

            double dailyWorkHours = entry.getHoursWorked();
            double rawDailyWorkHours = Math.max(0, dailyWorkHours);  // Prevent negative values

            double overtime = (rawDailyWorkHours >= 9) ? rawDailyWorkHours - 8 : 0;  //Only count OT if 9+ hours worked
            double lateMinutes = Math.max(0, Duration.between(LocalTime.of(8, 30), entry.getClockIn().toLocalTime()).toMinutes());
            double lateDeduction = (lateMinutes > 0) ? (lateMinutes / 60.0) * emp.getHourlyRate() : 0;

            boolean isHoliday = entry.isRegularHoliday() || entry.isSpecialNonWorking();
            boolean isRestDay = entry.isRestDay();
            boolean isHolidayRestDay = entry.isHolidayRestDay();
            double holidayMultiplier = entry.getHolidayMultiplier();

            summary.addDailyWork(entry.getClockIn().toLocalDate(), rawDailyWorkHours, lateMinutes, lateDeduction,
                                 isHoliday, isRestDay, isHolidayRestDay, holidayMultiplier);
        }

        return weeklySummaries;
    }
}
