package com.payroll;

import java.io.*;
import java.util.*;

/**
 * EmployeeData - Parses and loads employee data from a CSV file.
 */
public class EmployeeData {
    private final String empId;   // Employee ID
    private final String name;    // Full name (First + Last)
    private final String dob;     // Date of Birth
    private final float hourlyRate; // Hourly rate of the employee
    private String status;        // Employment status (e.g., Full-time, Part-time)
    private String position;      // Job position/title

    /**
     * Constructor to initialize employee data.
     *
     * @param empId       Employee ID
     * @param name        Employee's full name
     * @param dob         Date of Birth
     * @param hourlyRate  Hourly pay rate
     * @param status      Employment status
     * @param position    Employee position/title
     */
    public EmployeeData(String empId, String name, String dob, float hourlyRate, String status, String position) {
        this.empId = empId;
        this.name = name;
        this.dob = dob;
        this.hourlyRate = hourlyRate;
        this.status = status;
        this.position = position;
    }

    /**
     * Retrieves the Employee ID.
     * 
     * @return Employee ID as a String
     */
    public String getEmpId() { 
        return empId; 
    }

    /**
     * Retrieves the employee's full name.
     * 
     * @return Full name as a String
     */
    public String getName() { 
        return name; 
    }

    /**
     * Retrieves the employee's date of birth.
     * 
     * @return Date of birth as a String
     */
    public String getDob() { 
        return dob; 
    }

    /**
     * Retrieves the hourly rate of the employee.
     * 
     * @return Hourly rate as a float
     */
    public float getHourlyRate() { 
        return hourlyRate; 
    }

    /**
     * Retrieves the employment status of the employee.
     * 
     * @return Employment status as a String
     */
    public String getStatus() { 
        return status; 
    }

    /**
     * Retrieves the employee's job position/title.
     * 
     * @return Position as a String
     */
    public String getPosition() { 
        return position; 
    }

    /**
     * Loads employee data from a CSV file and stores it in a HashMap.
     *
     * @param filePath The path to the EmployeeData.csv file
     * @return A map with Employee ID as the key and EmployeeData object as the value
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

                String[] data = line.split(","); // ✅ Ensure proper CSV delimiter

                // Ensure there are at least 10 columns before processing
                if (data.length >= 10) {  
                    String empId = data[0].trim();
                    String name = data[1].trim() + " " + data[2].trim();  // Combine First Name + Last Name
                    String dob = data[3].trim();
                    float hourlyRate = parseFloat(data[4]); // Convert to float
                    String status = data[8].trim();
                    String position = data[9].trim();

                    // Store the employee data in the map
                    employees.put(empId, new EmployeeData(empId, name, dob, hourlyRate, status, position));
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
