package com.payroll;

import java.io.*;
import java.util.*;

/**
 * EmployeeData - Represents an employee's personal and payroll-related information.
 * This class provides access to employee identity, job details, pay rates, and basic salary.
 * It also includes logic for parsing employee records from a structured CSV file.
 */
public class EmployeeData {
    private final String empId;       // Employee ID
    private final String name;        // Full name (First Name + Last Name)
    private final String dob;         // Date of Birth
    private final float hourlyRate;   // Hourly rate of the employee
    private final float basicSalary;  // Basic monthly salary (from CSV Column K)
    private String status;            // Employment status (e.g., Regular, Contractual)
    private String position;          // Employee job position or title

    /**
     * Constructs an EmployeeData object with the specified employee details.
     *
     * @param empId        Unique employee identifier
     * @param name         Full name of the employee
     * @param dob          Date of birth in string format
     * @param hourlyRate   Hourly pay rate of the employee
     * @param basicSalary  Fixed monthly basic salary
     * @param status       Employment status (e.g., Regular, Contractual)
     * @param position     Job title or designation
     */
    public EmployeeData(String empId, String name, String dob, float hourlyRate,
                        float basicSalary, String status, String position) {
        this.empId = empId;
        this.name = name;
        this.dob = dob;
        this.hourlyRate = hourlyRate;
        this.basicSalary = basicSalary;
        this.status = status;
        this.position = position;
    }

    /**
     * Gets the employee's unique ID.
     *
     * @return Employee ID
     */
    public String getEmpId() { return empId; }

    /**
     * Gets the employee's full name.
     *
     * @return Full name (First Name + Last Name)
     */
    public String getName() { return name; }

    /**
     * Gets the employee's date of birth.
     *
     * @return Date of birth in string format
     */
    public String getDob() { return dob; }

    /**
     * Gets the employee's hourly pay rate.
     *
     * @return Hourly rate
     */
    public float getHourlyRate() { return hourlyRate; }

    /**
     * Gets the employee's basic monthly salary.
     *
     * @return Basic salary value
     */
    public float getBasicSalary() { return basicSalary; }

    /**
     * Gets the employee's employment status.
     *
     * @return Employment status (e.g., Regular, Part-time)
     */
    public String getStatus() { return status; }

    /**
     * Gets the employee's job position or title.
     *
     * @return Position or designation
     */
    public String getPosition() { return position; }

    /**
     * Loads employee records from a CSV file into a map.
     *
     * Expected CSV Format (Column Index):
     *  0 - Employee ID  
     *  1 - First Name  
     *  2 - Last Name  
     *  3 - Date of Birth  
     *  4 - Hourly Rate  
     *  5-6 - (Unused)  
     *  7 - Employment Status  
     *  8 - Position/Designation  
     * 10 - Basic Salary (Column K)
     *
     * @param filePath Path to the employee CSV file
     * @return A map containing employee IDs as keys and EmployeeData objects as values
     */
    public static Map<String, EmployeeData> loadEmployeeData(String filePath) {
        Map<String, EmployeeData> employees = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false; // Skip header row
                    continue;
                }

                String[] data = line.split(",");

                // Validate minimum column count (basic salary at index 10)
                if (data.length >= 11) {
                    String empId = data[0].trim();
                    String name = data[1].trim() + " " + data[2].trim();
                    String dob = data[3].trim();
                    float hourlyRate = parseFloat(data[4]);
                    String status = data[8].trim();
                    String position = data[9].trim();
                    float basicSalary = parseFloat(data[10]);

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
     * Converts a string to a float value, removing commas and trimming whitespace.
     * Returns 0.00 if parsing fails.
     *
     * @param value String value to parse
     * @return Parsed float value or 0.00 on error
     */
    private static float parseFloat(String value) {
        try {
            return Float.parseFloat(value.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return 0.00f;
        }
    }
}
