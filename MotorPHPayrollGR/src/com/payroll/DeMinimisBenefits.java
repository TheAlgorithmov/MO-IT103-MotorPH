package com.payroll;

import java.io.*;
import java.util.*;

public class DeMinimisBenefits {
    private String empId;
    private float riceSubsidy;
    private float phoneAllowance;
    private float clothingAllowance;

    /**
     * Constructor to initialize employee's de minimis benefits.
     * 
     * @param empId            Employee ID
     * @param riceSubsidy      Rice subsidy allowance
     * @param phoneAllowance   Phone allowance
     * @param clothingAllowance Clothing allowance
     */
    public DeMinimisBenefits(String empId, float riceSubsidy, float phoneAllowance, float clothingAllowance) {
        this.empId = empId;
        this.riceSubsidy = riceSubsidy;
        this.phoneAllowance = phoneAllowance;
        this.clothingAllowance = clothingAllowance;
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
     * Retrieves the rice subsidy amount.
     * 
     * @return Rice subsidy as a float
     */
    public float getRiceSubsidy() { 
        return riceSubsidy; 
    }

    /**
     * Retrieves the phone allowance amount.
     * 
     * @return Phone allowance as a float
     */
    public float getPhoneAllowance() { 
        return phoneAllowance; 
    }

    /**
     * Retrieves the clothing allowance amount.
     * 
     * @return Clothing allowance as a float
     */
    public float getClothingAllowance() { 
        return clothingAllowance; 
    }

    /**
     * Loads de minimis benefits data from a CSV file.
     * 
     * @param filePath Path to the EmployeeData.csv file
     * @return A map containing Employee ID as the key and DeMinimisBenefits object as the value
     */
    public static Map<String, DeMinimisBenefits> loadBenefits(String filePath) {
        Map<String, DeMinimisBenefits> benefitsMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                if (isHeader) { 
                    isHeader = false; // Skip header row
                    continue;
                }

                String[] data = line.split(","); // Assuming CSV is comma-separated

                // Ensure there are at least 8 columns in the row
                if (data.length >= 8) {  
                    String empId = data[0].trim();
                    float riceSubsidy = parseFloat(data[5]);
                    float phoneAllowance = parseFloat(data[6]);
                    float clothingAllowance = parseFloat(data[7]);

                    // Store the employee's benefits in the map
                    benefitsMap.put(empId, new DeMinimisBenefits(empId, riceSubsidy, phoneAllowance, clothingAllowance));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading De Minimis Benefits file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing numeric values in De Minimis Benefits file: " + e.getMessage());
        }

        return benefitsMap;
    }

    /**
     * Parses a String into a float, ensuring proper error handling.
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
