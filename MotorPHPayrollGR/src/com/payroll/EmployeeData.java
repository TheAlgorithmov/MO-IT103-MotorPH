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

    public EmployeeData(String empId, String name, String dob, double hourlyRate) {
        this.empId = empId;
        this.name = name;
        this.dob = dob;
        this.hourlyRate = hourlyRate;
    }

    public String getEmpId() { return empId; }
    public String getName() { return name; }
    public String getDob() { return dob; }
    public double getHourlyRate() { return hourlyRate; }

    public static Map<String, EmployeeData> loadEmployeeData(String filename) {
        Map<String, EmployeeData> employeeMap = new HashMap<>();
        File file = new File(filename);
        if (!file.exists()) {
            System.err.println("Error: Employee data file not found.");
            return employeeMap;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                    if (fields.length < 5) continue;
                    String empId = fields[0].trim();
                    String fullName = fields[1].trim() + " " + fields[2].trim();
                    String dob = fields[3].trim();
                    double hourlyRate = Double.parseDouble(fields[4].trim().replace(",", ""));
                    employeeMap.put(empId, new EmployeeData(empId, fullName, dob, hourlyRate));
                } catch (Exception e) {
                    System.err.println("Skipping invalid employee data row: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading employee data: " + e.getMessage());
        }
        return employeeMap;
    }
}
