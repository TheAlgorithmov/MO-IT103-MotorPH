//Employee's Weekly Worked Hours calculator
package com.payroll;

import java.time.*;
import java.util.*;

public class WeeklySummary {
    private final EmployeeData employee;
    private double totalWorkHours = 0, totalOvertime = 0, totalLateDeductions = 0, totalOvertimePay = 0;
    private final StringBuilder breakdownOutput = new StringBuilder();

    public WeeklySummary(EmployeeData employee) {
        this.employee = employee;
    }

    public void addDailyWork(LocalDate date, double workHours, double overtime, double lateMinutes, double lateDeduction) {
        totalWorkHours += workHours;
        totalOvertime += overtime;
        totalLateDeductions += lateDeduction;
        double overtimePay = overtime * employee.getHourlyRate() * 1.25;
        totalOvertimePay += overtimePay;

        breakdownOutput.append(String.format(" %s | %-17s | %8.2f | %.2f   | PHP %8.2f | %12.2f | PHP %8.2f%n",
                date.toString(), "Regular Workday", overtime, 1.25, overtimePay, lateMinutes, lateDeduction));
    }

    public double getTotalWorkHours() { return totalWorkHours; }
    public double getTotalOvertime() { return totalOvertime; }
    public double getTotalLateDeductions() { return totalLateDeductions; }
    public double getTotalOvertimePay() { return totalOvertimePay; }
    public EmployeeData getEmployee() { return employee; }
    
    public String getSummaryReport() {
        return "\n--------------------------------------------------------------\n"
            + " Summary of Work Hours & Deductions \n"
            + "--------------------------------------------------------------\n"
            + String.format(" Total Worked Hours: %.2f%n", totalWorkHours)
            + String.format(" Total Overtime Pay: PHP %.2f%n", totalOvertimePay)
            + String.format(" Total Late Deductions: PHP %.2f%n", totalLateDeductions)
            + "--------------------------------------------------------------\n"
            + getBreakdownReport(); // Append breakdown report here
    }
    
    public String getBreakdownReport() {
        if (breakdownOutput.length() == 0) {
            return "\n---------------- Overtime & Unpaid Work Hour Deductions Breakdown ----------------\n"
                + "Day | Work Type         | OT Hours | OT Rate | OT Pay      | Late Minutes | Late Deduction\n"
                + "----------------------------------------------------------------------------------------\n"
                + "No overtime or late deductions recorded.\n";
        }
        return "\n---------------- Overtime & Unpaid Work Hour Deductions Breakdown ----------------\n"
            + "Day | Work Type         | OT Hours | OT Rate | OT Pay      | Late Minutes | Late Deduction\n"
            + "----------------------------------------------------------------------------------------\n"
            + breakdownOutput.toString()
            + "----------------------------------------------------------------------------------------\n";
    }

    public static Map<String, WeeklySummary> calculateWorkedHours(Map<String, EmployeeData> employees, List<TimeEntry> timeEntries) {
        Map<String, WeeklySummary> weeklySummaries = new HashMap<>();

        for (TimeEntry entry : timeEntries) {
            EmployeeData emp = employees.get(entry.getEmpId());
            if (emp == null) continue;

            double workHours = Duration.between(entry.getClockIn(), entry.getClockOut()).toMinutes() / 60.0;
            double overtimeHours = Math.max(0, workHours - 9);
            double lateMinutes = Math.max(0, Duration.between(LocalTime.of(8, 30), entry.getClockIn().toLocalTime()).toMinutes());
            double lateDeduction = (lateMinutes > 0) ? (lateMinutes / 60.0) * emp.getHourlyRate() : 0;
            
            //Debugging: Check time entries & work hours
            System.out.printf("Debug: Employee %s clocked in at %s and out at %s. Computed Work Hours: %.2f%n",
        entry.getEmpId(), entry.getClockIn(), entry.getClockOut(), workHours);

            weeklySummaries.putIfAbsent(entry.getEmpId(), new WeeklySummary(emp));
            weeklySummaries.get(entry.getEmpId()).addDailyWork(entry.getClockIn().toLocalDate(), workHours, overtimeHours, lateMinutes, lateDeduction);
        }

        return weeklySummaries;
    }
}





