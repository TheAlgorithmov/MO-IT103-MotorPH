package com.payroll;

import java.io.*;
import java.util.*;

public class DeMinimisBenefits {
    private String empId;
    private double riceSubsidy;
    private double phoneAllowance;
    private double clothingAllowance;

    public DeMinimisBenefits(String empId, double riceSubsidy, double phoneAllowance, double clothingAllowance) {
        this.empId = empId;
        this.riceSubsidy = riceSubsidy;
        this.phoneAllowance = phoneAllowance;
        this.clothingAllowance = clothingAllowance;
    }

    // Go get em!
    public String getEmpId() { return empId; }
    public double getRiceSubsidy() { return riceSubsidy; }
    public double getPhoneAllowance() { return phoneAllowance; }
    public double getClothingAllowance() { return clothingAllowance; }

    // Load De Minimis Benefits from EmployeeData.csv
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

                String[] data = line.split(","); //Assuming CSV is comma-separated

                if (data.length >= 8) {  // Ensure at least 8 columns exist
                    String empId = data[0].trim();
                    double riceSubsidy = parseDouble(data[5]);
                    double phoneAllowance = parseDouble(data[6]);
                    double clothingAllowance = parseDouble(data[7]);

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

    // Helper Method to Handle Parsing Errors
    private static double parseDouble(String value) {
        try {
            return Double.parseDouble(value.replace(",", "").trim()); //Remove commas & parse
        } catch (NumberFormatException e) {
            return 0.00; //Default to 0 if parsing fails
        }
    }
}
