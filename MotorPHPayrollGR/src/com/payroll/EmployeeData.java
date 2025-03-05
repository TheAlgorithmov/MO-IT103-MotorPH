package com.payroll;

import java.io.*;
import java.util.*;

/**
 * EmployeeData - Parses and loads employee data from a CSV file.
 * Now includes **Basic Salary** from EmployeeData.csv (Column K).
 */
public class EmployeeData {
    private final String empId;    // Employee ID
    private final String name;     // Full name (First + Last)
    private final String dob;      // Date of Birth
    private final float hourlyRate; // Hourly rate of the employee
    private final float basicSalary; // Basic Salary from Column K
    private String status;         // Employment status (e.g., Full-time, Part-time)
    private String position;       // Job position/title

    /**
     * Constructor to initialize employee data.
     *
     * @param empId       Employee ID
     * @param name        Employee's full name
     * @param dob         Date of Birth
     * @param hourlyRate  Hourly pay rate
     * @param basicSalary Employee's fixed basic salary
     * @param status      Employment status
     * @param position    Employee position/title
     */
    public EmployeeData(String empId, String name, String dob, float hourlyRate, float basicSalary, String status, String position) {
        this.empId = empId;
        this.name = name;
        this.dob = dob;
        this.hourlyRate = hourlyRate;
        this.basicSalary = basicSalary;
        this.status = status;
        this.position = position;
    }

    // ** Getter for Basic Salary **
    public float getBasicSalary() { 
        return basicSalary; 
    }

    public String getEmpId() { return empId; }
    public String getName() { return name; }
    public String getDob() { return dob; }
    public float getHourlyRate() { return hourlyRate; }
    public String getStatus() { return status; }
    public String getPosition() { return position; }

    /**
     * Loads employee data from a CSV file and stores it in a HashMap.
     * 
     * **Updated to read Basic Salary from Column K (Index 10).**
     *
     * Expected CSV Format (Columns):
     * 1. Employee ID
     * 2. First Name
     * 3. Last Name
     * 4. Date of Birth
     * 5. Hourly Rate
     * 6-7. (Unused columns)
     * 8. Employment Status
     * 9. Job Position
     * 10. Basic Salary  ← **Now included**
     *
     * @param filePath The path to EmployeeData.csv
     * @return A HashMap with Employee ID as the key and EmployeeData objects as the value
     */
    public static Map<String, EmployeeData> loadEmployeeData(String filePath) {
        Map<String, EmployeeData> employees = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false; // Skip the header row
                    continue;
                }

                String[] data = line.split(","); // Ensure proper CSV delimiter

                // Ensure there are at least 11 columns before processing (Column K is index 10)
                if (data.length >= 11) {  
                    String empId = data[0].trim();
                    String name = data[1].trim() + " " + data[2].trim(); // Combine First Name + Last Name
                    String dob = data[3].trim();
                    float hourlyRate = parseFloat(data[4]); // Convert to float
                    String status = data[8].trim();
                    String position = data[9].trim();
                    float basicSalary = parseFloat(data[10]); // Read Basic Salary from Column K

                    // Store the employee data in the map
                    employees.put(empId, new EmployeeData(empId, name, dob, hourlyRate, basicSalary, status, position));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading Employee Data file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing numeric values in Employee Data file: " + e.getMessage());
        }

        return employees;
    }

    /**
     * Parses a String into a float with error handling.
     * 
     * @param value String representation of a float value
     * @return Parsed float value; defaults to 0.00 if parsing fails
     */
    private static float parseFloat(String value) {
        try {
            return Float.parseFloat(value.replace(",", "").trim()); // Remove commas & parse
        } catch (NumberFormatException e) {
            return 0.00f; // Default to 0 if parsing fails
        }
    }
}
