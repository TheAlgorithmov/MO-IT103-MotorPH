//Subclass- EmployeeTimeEntries csv Parser
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

    public TimeEntry(String empId, LocalDateTime clockIn, LocalDateTime clockOut) {
        this.empId = empId;
        this.clockIn = clockIn;
        this.clockOut = clockOut;
    }

    public String getEmpId() { return empId; }
    public LocalDateTime getClockIn() { return clockIn; }
    public LocalDateTime getClockOut() { return clockOut; }

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
                    if (data.length < 4) continue;
                    String empId = data[0].trim();
                    LocalDateTime clockIn = LocalDateTime.parse(data[1].trim() + " " + data[2].trim(), DATE_TIME_FORMATTER);
                    LocalDateTime clockOut = LocalDateTime.parse(data[1].trim() + " " + data[3].trim(), DATE_TIME_FORMATTER);
                    timeEntries.add(new TimeEntry(empId, clockIn, clockOut));
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
