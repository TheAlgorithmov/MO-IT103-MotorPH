package com.payroll;

import java.io.*;
import java.util.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

public class DeMinimisBenefits {

    private String empId;
    private float riceSubsidy;
    private float phoneAllowance;
    private float clothingAllowance;

    /**
     * Constructor to initialize employee's de minimis benefits.
     *
     * @param empId Employee ID
     * @param riceSubsidy Rice subsidy allowance
     * @param phoneAllowance Phone allowance
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
     * @return A map containing Employee ID as the key and DeMinimisBenefits
     * object as the value
     */
    public static Map<String, DeMinimisBenefits> loadBenefits(String filePath) {
        Map<String, DeMinimisBenefits> benefitsMap = new HashMap<>();

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            reader.readNext();                     // skip header row

            String[] row;
            while ((row = reader.readNext()) != null) {
                // columns: 0-EmpID … 5-Rice 6-Phone 7-Clothing
                if (row.length < 8) {
                    continue;      // ignore malformed lines
                }
                String empId = row[0].trim();
                float riceSubsidy = parseFloat(row[5]);
                float phoneAllowance = parseFloat(row[6]);
                float clothingAllowance = parseFloat(row[7]);

                benefitsMap.put(
                        empId,
                        new DeMinimisBenefits(empId, riceSubsidy, phoneAllowance, clothingAllowance)
                );
            }
        } catch (IOException | CsvValidationException ex) {
            System.err.println("Error loading De Minimis Benefits: " + ex.getMessage());
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
