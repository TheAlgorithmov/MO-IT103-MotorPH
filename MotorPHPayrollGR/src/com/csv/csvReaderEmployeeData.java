/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
/**
 *
 * @JEO
 */
package com.csv;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class csvReaderEmployeeData {

    // === READ CSV ===
    public static List<String[]> readCsv(String path, boolean skipHeader) {
        try {
            return readCsv(new FileReader(path), skipHeader);
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + path);
            return new ArrayList<>();
        }
    }

    public static List<String[]> readCsvFromResource(String resourcePath, boolean skipHeader) {
        InputStream is = csvReaderEmployeeData.class.getResourceAsStream(resourcePath);
        if (is == null) {
            System.err.println("Resource not found: " + resourcePath);
            return new ArrayList<>();
        }

        try {
            InputStreamReader reader = new InputStreamReader(is, "UTF-8");
            return readCsv(reader, skipHeader);
        } catch (Exception e) {
            System.err.println("Error reading resource: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static List<String[]> readCsv(Reader reader, boolean skipHeader) {
        List<String[]> rows = new ArrayList<>();

        try (CSVReader csvReader = new CSVReader(reader)) {
            String[] nextLine;
            boolean isFirstLine = true;

            while ((nextLine = csvReader.readNext()) != null) {
                // Skip blank lines
                if (nextLine.length == 0 || (nextLine.length == 1 && nextLine[0].trim().isEmpty())) {
                    continue;
                }

                if (isFirstLine && skipHeader) {
                    isFirstLine = false;
                    continue;
                }

                rows.add(nextLine);
                isFirstLine = false;
            }
        } catch (IOException | CsvValidationException ex) {
            System.err.println("Error reading CSV: " + ex.getMessage());
        }

        return rows;
    }

    // === WRITE CSV FULL ===
    public static void writeCsv(String path, List<String[]> data) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(path))) {
            for (String[] row : data) {
                writer.writeNext(row);
            }
        } catch (IOException e) {
            System.err.println("Error writing CSV to " + path + ": " + e.getMessage());
        }
    }

    // === APPEND CSV ROW ===
    public static void appendCsvRow(String path, String[] row) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(path, true))) {
            writer.writeNext(row);
        } catch (IOException e) {
            System.err.println("Error appending row to CSV " + path + ": " + e.getMessage());
        }
    }

    // === DELETE ROW BASED ON FIRST COLUMN VALUE ===
    public static void deleteCsvRowByFirstColumn(String path, String targetValue) {
        List<String[]> data = readCsv(path, false);
        if (data.isEmpty()) return;

        List<String[]> newData = new ArrayList<>();
        String[] header = data.get(0);
        newData.add(header);

        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);
            if (!row[0].trim().equals(targetValue)) {
                newData.add(row);
            }
        }

        writeCsv(path, newData);
    }
}
