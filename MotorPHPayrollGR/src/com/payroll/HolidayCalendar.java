package com.payroll;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

public class HolidayCalendar {

    private static final Set<LocalDate> REGULAR_HOLIDAYS = new HashSet<>();
    private static final Set<LocalDate> SPECIAL_HOLIDAYS = new HashSet<>();
    private static final Set<LocalDate> SPECIAL_WORKING_DAYS = new HashSet<>();

    public static void loadHolidaysFromCSV(String filename) {
        REGULAR_HOLIDAYS.clear();
        SPECIAL_HOLIDAYS.clear();
        SPECIAL_WORKING_DAYS.clear();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("M/d/yyyy");

        try (CSVReader reader = new CSVReader(new FileReader(filename))) {
            reader.readNext();                  // skip header row

            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length < 3) {
                    continue;   // bad / incomplete line → ignore
                }
                // Strip surrounding quotes if present; CSVReader usually does this for us,
                // but calling trim() guarantees no stray spaces.
                LocalDate date = LocalDate.parse(row[0].trim(), fmt);
                String type = row[2].trim().toLowerCase();

                if (type.contains("regular holiday")) {
                    REGULAR_HOLIDAYS.add(date);
                } else if (type.contains("special non-working holiday")) {
                    SPECIAL_HOLIDAYS.add(date);
                } else if (type.contains("special working day")) {
                    SPECIAL_WORKING_DAYS.add(date);
                }
            }
        } catch (IOException | CsvValidationException ex) {
            System.err.println("Error loading holidays: " + ex.getMessage());
        }
    }

    public static boolean isRegularHoliday(LocalDate date) {
        return REGULAR_HOLIDAYS.contains(date);
    }

    public static boolean isSpecialNonWorkingHoliday(LocalDate date) {
        return SPECIAL_HOLIDAYS.contains(date);
    }

    public static boolean isSpecialWorkingDay(LocalDate date) {
        return SPECIAL_WORKING_DAYS.contains(date);
    }
}
