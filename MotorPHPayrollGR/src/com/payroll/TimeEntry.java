// Subclass - EmployeeTimeEntries CSV Parser
package com.payroll;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TimeEntry {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy h:mm a");

    private final String empId;
    private final LocalDateTime clockIn;
    private final LocalDateTime clockOut;
    private final boolean isRegularHoliday;
    private final boolean isSpecialNonWorking;
    private final boolean isRestDay;
    private final boolean isHolidayRestDay;
    private final double holidayMultiplier;
    private final double hoursWorked;

    // **Regular Philippine Holidays (200% pay)**
    private static final Set<LocalDate> REGULAR_HOLIDAYS = Set.of(
        LocalDate.of(2024, 1, 1),   // New Year's Day
        LocalDate.of(2024, 3, 28),  // Maundy Thursday
        LocalDate.of(2024, 3, 29),  // Good Friday
        LocalDate.of(2024, 4, 9),   // Day of Valor
        LocalDate.of(2024, 5, 1),   // Labor Day
        LocalDate.of(2024, 6, 12),  // Independence Day
        LocalDate.of(2024, 8, 26),  // National Heroes Day
        LocalDate.of(2024, 11, 30), // Bonifacio Day
        LocalDate.of(2024, 12, 25), // Christmas Day
        LocalDate.of(2024, 12, 30), // Rizal Day
        LocalDate.of(2024, 4, 10),  // Eidul Fitr
        LocalDate.of(2024, 6, 17)   // Eidul Adha
    );

    // **Special Non-Working Holidays (130% pay)**
    private static final Set<LocalDate> SPECIAL_HOLIDAYS = Set.of(
        LocalDate.of(2024, 2, 10),  // Chinese New Year
        LocalDate.of(2024, 3, 30),  // Black Saturday
        LocalDate.of(2024, 8, 21),  // Ninoy Aquino Day
        LocalDate.of(2024, 11, 1),  // All Saints' Day
        LocalDate.of(2024, 11, 2),  // All Souls' Day
        LocalDate.of(2024, 12, 8),  // Feast of the Immaculate Conception
        LocalDate.of(2024, 12, 24), // Christmas Eve
        LocalDate.of(2024, 12, 31)  // Last Day of the Year
    );

    public TimeEntry(String empId, LocalDateTime clockIn, LocalDateTime clockOut, boolean hasOvertime) {
        this.empId = empId;
        this.clockIn = clockIn;
        this.clockOut = clockOut;

        // **Check if it's a weekend (Saturday/Sunday)**
        boolean isWeekend = (clockIn.getDayOfWeek() == DayOfWeek.SATURDAY || clockIn.getDayOfWeek() == DayOfWeek.SUNDAY);
        this.isRegularHoliday = REGULAR_HOLIDAYS.contains(clockIn.toLocalDate());
        this.isSpecialNonWorking = SPECIAL_HOLIDAYS.contains(clockIn.toLocalDate());
        this.isRestDay = isWeekend && hasOvertime;
        this.isHolidayRestDay = (isRegularHoliday || isSpecialNonWorking) && isRestDay;
        this.holidayMultiplier = calculateHolidayMultiplier();
        this.hoursWorked = calculateWorkHours();
    }

    // **Determine the pay multiplier based on the type of day**
    private double calculateHolidayMultiplier() {
        if (isHolidayRestDay) return 2.6;  // Holiday + Rest Day OT (260% pay)
        if (isRegularHoliday) return 2.00;  // Regular Holiday (200% pay)
        if (isSpecialNonWorking) return 1.30;  // Special Non-Working Holiday (130% pay)
        if (isRestDay) return 1.50;  // Rest Day OT (150% pay)
        return 1.00;  // Normal workday (100% pay)
    }

    // **Calculate total worked hours for the day**
    private double calculateWorkHours() {
        return Duration.between(clockIn, clockOut).toMinutes() / 60.0;
    }

    // **Getter Methods**
    public String getEmpId() { return empId; }
    public LocalDateTime getClockIn() { return clockIn; }
    public LocalDateTime getClockOut() { return clockOut; }
    public boolean isRegularHoliday() { return isRegularHoliday; }
    public boolean isSpecialNonWorking() { return isSpecialNonWorking; }
    public boolean isRestDay() { return isRestDay; }
    public boolean isHolidayRestDay() { return isHolidayRestDay; }
    public double getHolidayMultiplier() { return holidayMultiplier; }
    public double getHoursWorked() { return hoursWorked; }

    // `loadTimeEntries()` method**
    public static List<TimeEntry> loadTimeEntries(String filename) {
        List<TimeEntry> timeEntries = new ArrayList<>();
        File file = new File(filename);

        if (!file.exists()) {
            System.err.println("Error: Employee time entries file not found.");
            return timeEntries;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // Skip header
            String line;

            while ((line = br.readLine()) != null) {
                try {
                    String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                    if (data.length < 5) continue;

                    String empId = data[0].trim();
                    LocalDateTime clockIn = LocalDateTime.parse(data[1].trim() + " " + data[2].trim(), DATE_TIME_FORMATTER);
                    LocalDateTime clockOut = LocalDateTime.parse(data[1].trim() + " " + data[3].trim(), DATE_TIME_FORMATTER);
                    boolean hasOvertime = Boolean.parseBoolean(data[4].trim());

                    TimeEntry entry = new TimeEntry(empId, clockIn, clockOut, hasOvertime);
                    timeEntries.add(entry);
                } catch (Exception e) {
                    System.err.println("Skipping invalid time entry row: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading time entries: " + e.getMessage());
        }

        return timeEntries;
    }
}
