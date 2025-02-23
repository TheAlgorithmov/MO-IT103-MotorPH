//Subclass - Employee Data csv parser
package com.payroll;

import java.io.*;
import java.util.*;

/**
 * EmployeeData - Parses and loads employee data from CSV.
 */
public class EmployeeData {
    private final String empId;
    private final String name;
    private final String dob;
    private final double hourlyRate;
    private String status;   // ✅ New field for Employment Status
    private String position; // ✅ New field for Position
    
    public EmployeeData(String empId, String name, String dob, double hourlyRate, String status, String position) {
        this.empId = empId;
        this.name = name;
        this.dob = dob;
        this.hourlyRate = hourlyRate;
        this.status = status;
        this.position = position;
    }
    
    //Go fetch!!!
    public String getEmpId() { return empId; }
    public String getName() { return name; }
    public String getDob() { return dob; }
    public double getHourlyRate() { return hourlyRate; }
    public String getStatus() { return status; }
    public String getPosition() { return position; }

    // Load Employee Data from CSV
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

                String[] data = line.split(","); // ✅ Ensure proper CSV delimiter

                if (data.length >= 10) {  // ✅ Ensure at least 10 columns exist
                    String empId = data[0].trim();
                    String name = data[1].trim() + " " + data[2].trim();  // Combine First Name + Last Name
                    String dob = data[3].trim();
                    double hourlyRate = Double.parseDouble(data[4].trim());
                    String status = data[8].trim();
                    String position = data[9].trim();

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
}
