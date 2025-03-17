package com.payroll;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HolidayCalendar {
    private static final Set<LocalDate> REGULAR_HOLIDAYS = new HashSet<>();
    private static final Set<LocalDate> SPECIAL_HOLIDAYS = new HashSet<>();
    private static final Set<LocalDate> SPECIAL_WORKING_DAYS = new HashSet<>();

    public static void loadHolidaysFromCSV(String filename) {
        REGULAR_HOLIDAYS.clear();
        SPECIAL_HOLIDAYS.clear();
        SPECIAL_WORKING_DAYS.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            br.readLine(); // Skip header
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (data.length < 3) continue;

                LocalDate holidayDate = LocalDate.parse(data[0].trim(), formatter);
                String holidayType = data[2].trim().toLowerCase();

                if (holidayType.contains("regular holiday")) {
                    REGULAR_HOLIDAYS.add(holidayDate);
                } else if (holidayType.contains("special non-working holiday")) {
                    SPECIAL_HOLIDAYS.add(holidayDate);
                } else if (holidayType.contains("special working day")) {
                    SPECIAL_WORKING_DAYS.add(holidayDate);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading holidays: " + e.getMessage());
        }
    }

    public static boolean isRegularHoliday(LocalDate date) { return REGULAR_HOLIDAYS.contains(date); }
    public static boolean isSpecialNonWorkingHoliday(LocalDate date) { return SPECIAL_HOLIDAYS.contains(date); }
    public static boolean isSpecialWorkingDay(LocalDate date) { return SPECIAL_WORKING_DAYS.contains(date); }
}
