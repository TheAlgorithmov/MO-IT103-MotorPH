package com.payroll;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * TimeEntry - Parses and processes employee time entries from a CSV file.
 * Determines worked hours, holiday pay, and overtime conditions.
 */
public class TimeEntry {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy h:mm a");

    private final String empId;
    private final LocalDateTime clockIn;
    private final LocalDateTime clockOut;
    private final boolean isRegularHoliday;
    private final boolean isSpecialNonWorking;
    private final boolean isRestDay;
    private final boolean isHolidayRestDay;
    private final float holidayMultiplier;
    private final float hoursWorked;

    // **Regular Philippine Holidays (200% pay)**
    private static final Set<LocalDate> REGULAR_HOLIDAYS = Set.of(
        LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 28), LocalDate.of(2024, 3, 29),
        LocalDate.of(2024, 4, 9), LocalDate.of(2024, 5, 1), LocalDate.of(2024, 6, 12),
        LocalDate.of(2024, 8, 26), LocalDate.of(2024, 11, 30), LocalDate.of(2024, 12, 25),
        LocalDate.of(2024, 12, 30), LocalDate.of(2024, 4, 10), LocalDate.of(2024, 6, 17)
    );

    // **Special Non-Working Holidays (130% pay)**
    private static final Set<LocalDate> SPECIAL_HOLIDAYS = Set.of(
        LocalDate.of(2024, 2, 10), LocalDate.of(2024, 3, 30), LocalDate.of(2024, 8, 21),
        LocalDate.of(2024, 11, 1), LocalDate.of(2024, 11, 2), LocalDate.of(2024, 12, 8),
        LocalDate.of(2024, 12, 24), LocalDate.of(2024, 12, 31)
    );

    /**
     * Constructor to initialize a time entry.
     * 
     * @param empId        Employee ID
     * @param clockIn      Clock-in time
     * @param clockOut     Clock-out time
     * @param hasOvertime  Indicates if overtime was worked
     */
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

    /**
     * Determines the pay multiplier based on the type of day.
     * 
     * @return Pay multiplier for the workday
     */
    private float calculateHolidayMultiplier() {
        if (isHolidayRestDay) return 2.6f;  // Holiday + Rest Day OT (260% pay)
        if (isRegularHoliday) return 2.00f; // Regular Holiday (200% pay)
        if (isSpecialNonWorking) return 1.30f; // Special Non-Working Holiday (130% pay)
        if (isRestDay) return 1.50f; // Rest Day OT (150% pay)
        return 1.00f; // Normal workday (100% pay)
    }

    /**
     * Calculates total worked hours for the day.
     * 
     * @return Total worked hours as a float
     */
    private float calculateWorkHours() {
        return (float) Duration.between(clockIn, clockOut).toMinutes() / 60;
    }

    // **Getter Methods**
    public String getEmpId() { return empId; }
    public LocalDateTime getClockIn() { return clockIn; }
    public LocalDateTime getClockOut() { return clockOut; }
    public boolean isRegularHoliday() { return isRegularHoliday; }
    public boolean isSpecialNonWorking() { return isSpecialNonWorking; }
    public boolean isRestDay() { return isRestDay; }
    public boolean isHolidayRestDay() { return isHolidayRestDay; }
    public float getHolidayMultiplier() { return holidayMultiplier; }
    public float getHoursWorked() { return hoursWorked; }

    /**
     * Loads employee time entries from a CSV file.
     * 
     * @param filename Path to the time entries CSV file
     * @return List of TimeEntry objects
     */
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
                    // Handle CSV format properly
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
