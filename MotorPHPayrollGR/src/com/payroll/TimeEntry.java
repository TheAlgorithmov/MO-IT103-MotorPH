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
        if (isRestDay && isRegularHoliday) return 2.6f;
        if (isRegularHoliday) return 2.00f;
        if (isSpecialNonWorking) return 1.30f;
        if (isRestDay) return 1.50f;
        if (isSpecialWorking) return 1.00f;
        return 1.00f;
    }

    private float calculateWorkHours() {
        return (float) Duration.between(clockIn, clockOut).toMinutes() / 60;
    }

    public boolean isHolidayRestDay() {
        return (isRegularHoliday || isSpecialNonWorking) && isRestDay;
    }

    public String getEmpId() { return empId; }
    public LocalDateTime getClockIn() { return clockIn; }
    public LocalDateTime getClockOut() { return clockOut; }
    public boolean isRegularHoliday() { return isRegularHoliday; }
    public boolean isSpecialNonWorking() { return isSpecialNonWorking; }
    public boolean isSpecialWorking() { return isSpecialWorking; }
    public boolean isRestDay() { return isRestDay; }
    public float getHolidayMultiplier() { return holidayMultiplier; }
    public float getHoursWorked() { return hoursWorked; }

    public static List<TimeEntry> loadTimeEntries(String filename) {
        List<TimeEntry> timeEntries = new ArrayList<>();
        File file = new File(filename);

        if (!file.exists()) {
            System.err.println("Error: Time entries file not found.");
            return timeEntries;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    String[] data = line.split(",");
                    if (data.length < 5) continue;

                    String empId = data[0].trim();
                    LocalDateTime clockIn = LocalDateTime.parse(data[1].trim() + " " + data[2].trim(), DATE_TIME_FORMATTER);
                    LocalDateTime clockOut = LocalDateTime.parse(data[1].trim() + " " + data[3].trim(), DATE_TIME_FORMATTER);
                    boolean hasOvertime = Boolean.parseBoolean(data[4].trim());

                    timeEntries.add(new TimeEntry(empId, clockIn, clockOut, hasOvertime));
                } catch (Exception e) {
                    System.err.println("Skipping invalid entry: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading time entries: " + e.getMessage());
        }

        return timeEntries;
    }

    public static List<TimeEntry> filterTimeEntriesByDate(List<TimeEntry> timeEntries, LocalDate startDate, LocalDate endDate) {
        List<TimeEntry> filteredEntries = new ArrayList<>();
        for (TimeEntry entry : timeEntries) {
            LocalDate entryDate = entry.getClockIn().toLocalDate();
            if ((entryDate.isEqual(startDate) || entryDate.isAfter(startDate)) &&
                (entryDate.isEqual(endDate) || entryDate.isBefore(endDate))) {
                filteredEntries.add(entry);
            }
        }
        return filteredEntries;
    }
}
