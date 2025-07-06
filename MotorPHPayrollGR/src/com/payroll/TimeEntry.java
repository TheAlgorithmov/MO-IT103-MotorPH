package com.payroll;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.time.LocalDate;

public class TimeEntry {

// make it tolerant of lower-/upper-case “am/pm”
    private static final DateTimeFormatter DATE_TIME_FORMATTER
            = new DateTimeFormatterBuilder()
                    .parseCaseInsensitive() // ← key line
                    .appendPattern("M/d/yyyy h:mm a") // keep the “ a” token!
                    .toFormatter(Locale.ENGLISH);
    private final String empId;
    private final LocalDateTime clockIn;
    private final LocalDateTime clockOut;
    private final boolean isRegularHoliday;
    private final boolean isSpecialNonWorking;
    private final boolean isSpecialWorking;
    private final boolean isRestDay;
    private final float holidayMultiplier;
    private final float hoursWorked;

    public TimeEntry(String empId, LocalDateTime clockIn, LocalDateTime clockOut, boolean hasOvertime) {
        this.empId = empId;
        this.clockIn = clockIn;
        this.clockOut = clockOut;

        LocalDate workDate = clockIn.toLocalDate();
        boolean isWeekend = (clockIn.getDayOfWeek() == DayOfWeek.SATURDAY || clockIn.getDayOfWeek() == DayOfWeek.SUNDAY);

        this.isRegularHoliday = HolidayCalendar.isRegularHoliday(workDate);
        this.isSpecialNonWorking = HolidayCalendar.isSpecialNonWorkingHoliday(workDate);
        this.isSpecialWorking = HolidayCalendar.isSpecialWorkingDay(workDate);
        this.isRestDay = isWeekend && !isSpecialWorking;
        this.holidayMultiplier = calculateHolidayMultiplier();
        this.hoursWorked = calculateWorkHours();
    }

    private float calculateHolidayMultiplier() {
        if (isRestDay && isRegularHoliday) {
            return 2.6f;
        }
        if (isRegularHoliday) {
            return 2.00f;
        }
        if (isSpecialNonWorking) {
            return 1.30f;
        }
        if (isRestDay) {
            return 1.50f;
        }
        if (isSpecialWorking) {
            return 1.00f;
        }
        return 1.00f;
    }

    private float calculateWorkHours() {
        return (float) Duration.between(clockIn, clockOut).toMinutes() / 60;
    }

    public boolean isHolidayRestDay() {
        return (isRegularHoliday || isSpecialNonWorking) && isRestDay;
    }

    public String getEmpId() {
        return empId;
    }

    public LocalDateTime getClockIn() {
        return clockIn;
    }

    public LocalDateTime getClockOut() {
        return clockOut;
    }

    public boolean isRegularHoliday() {
        return isRegularHoliday;
    }

    public boolean isSpecialNonWorking() {
        return isSpecialNonWorking;
    }

    public boolean isSpecialWorking() {
        return isSpecialWorking;
    }

    public boolean isRestDay() {
        return isRestDay;
    }

    public float getHolidayMultiplier() {
        return holidayMultiplier;
    }

    public float getHoursWorked() {
        return hoursWorked;
    }

    public static List<TimeEntry> loadTimeEntries(String filename) {
        List<TimeEntry> timeEntries = new ArrayList<>();
        File file = new File(filename);

        if (!file.exists()) {
            System.err.println("Error: Time entries file not found -> " + file.getAbsolutePath());
            return timeEntries;
        }

        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            reader.readNext();                      // skip header row

            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length < 5) {
                    continue;       // malformed → skip
                }
                try {
                    String empId = row[0].trim();
                    String dateStr = row[1].trim();      // e.g. 7/24/2024
                    String inStr = row[2].trim();      // e.g. 10:06 am
                    String outStr = row[3].trim();      // e.g. 7:15 pm
                    boolean overtime = Boolean.parseBoolean(row[4].trim());

                    LocalDateTime clockIn = LocalDateTime.parse(
                            dateStr + " " + inStr, DATE_TIME_FORMATTER);
                    LocalDateTime clockOut = LocalDateTime.parse(
                            dateStr + " " + outStr, DATE_TIME_FORMATTER);

                    timeEntries.add(new TimeEntry(empId, clockIn, clockOut, overtime));
                } catch (Exception ex) {
                    System.err.println("Skipping invalid entry "
                            + Arrays.toString(row) + " → " + ex.getMessage());
                }
            }
        } catch (IOException | CsvValidationException ex) {
            System.err.println("Error reading time entries: " + ex.getMessage());
        }

        return timeEntries;
    }

    public static List<TimeEntry> filterTimeEntriesByDate(List<TimeEntry> timeEntries, LocalDate startDate, LocalDate endDate) {
        List<TimeEntry> filteredEntries = new ArrayList<>();
        for (TimeEntry entry : timeEntries) {
            LocalDate entryDate = entry.getClockIn().toLocalDate();
            if ((entryDate.isEqual(startDate) || entryDate.isAfter(startDate))
                    && (entryDate.isEqual(endDate) || entryDate.isBefore(endDate))) {
                filteredEntries.add(entry);
            }
        }
        return filteredEntries;
    }

    /**
     * Returns the work-date (yyyy-MM-dd) for this time-entry.
     */
    public LocalDate getLogDate() {
        return clockIn.toLocalDate();
    }
}
