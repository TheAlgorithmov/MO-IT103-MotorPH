//Government Deductions Calculations
package com.payroll;

import java.util.NavigableMap;
import java.util.TreeMap;

public class GovernmentDeductions {

    // ------------------------------------------------------------------------------------
    // GOVERNMENT DEDUCTIONS (SSS, PhilHealth, Pag-IBIG, BIR Tax)
    // ------------------------------------------------------------------------------------

    public static double calculateSSS(double grossIncome) {
        // SSS CONTRIBUTION (Jan 2024 Table)
        NavigableMap<Double, Double> sssTable = new TreeMap<>();
        sssTable.put(3250.0, 135.00);
        sssTable.put(3750.0, 157.50);
        sssTable.put(4250.0, 180.00);
        sssTable.put(4750.0, 202.50);
        sssTable.put(5250.0, 225.00);
        sssTable.put(5750.0, 247.50);
        sssTable.put(6250.0, 270.00);
        sssTable.put(6750.0, 292.50);
        sssTable.put(7250.0, 315.00);
        sssTable.put(7750.0, 337.50);
        sssTable.put(8250.0, 360.00);
        sssTable.put(8750.0, 382.50);
        sssTable.put(9250.0, 405.00);
        sssTable.put(9750.0, 427.50);
        sssTable.put(10250.0, 450.00);
        sssTable.put(10750.0, 472.50);
        sssTable.put(11250.0, 495.00);
        sssTable.put(11750.0, 517.50);
        sssTable.put(12250.0, 540.00);
        sssTable.put(12750.0, 562.50);
        sssTable.put(13250.0, 585.00);
        sssTable.put(13750.0, 607.50);
        sssTable.put(14250.0, 630.00);
        sssTable.put(14750.0, 652.50);
        sssTable.put(15250.0, 675.00);
        sssTable.put(15750.0, 697.50);
        sssTable.put(16250.0, 720.00);
        sssTable.put(16750.0, 742.50);
        sssTable.put(17250.0, 765.00);
        sssTable.put(17750.0, 787.50);
        sssTable.put(18250.0, 810.00);
        sssTable.put(18750.0, 832.50);
        sssTable.put(19250.0, 855.00);
        sssTable.put(19750.0, 877.50);
        sssTable.put(20250.0, 900.00);
        sssTable.put(24750.0, 1125.00);
        return sssTable.floorEntry(grossIncome).getValue();
    }

    public static double calculatePhilHealth(double grossIncome) {
        // PHILHEALTH CONTRIBUTION (Deducted Monthly)
        double monthlyPhilHealth = (grossIncome <= 10000) ? 300 : Math.min(1800, 0.03 * grossIncome);
        return (monthlyPhilHealth / 2) / 4; // Employee share divided by 4 for weekly deduction
    }

    public static double calculatePagibig(double grossIncome) {
        // PAG-IBIG CONTRIBUTION (Pro-rated Weekly)
        double monthlyHDMF = (grossIncome <= 1500) ? Math.min(100, 0.01 * grossIncome) : Math.min(100, 0.02 * grossIncome);
        return monthlyHDMF / 4;
    }

    public static double calculateBIR(double taxableIncome) {
        // BIR WITHHOLDING TAX (DOLE Rates)
        if (taxableIncome > 20832 && taxableIncome < 33333) {
            return 0.20 * (taxableIncome - 20833);
        } else if (taxableIncome >= 33333 && taxableIncome < 66667) {
            return 2500 + 0.25 * (taxableIncome - 33333);
        } else if (taxableIncome >= 66667 && taxableIncome < 166667) {
            return 10833 + 0.30 * (taxableIncome - 66667);
        } else if (taxableIncome >= 166667 && taxableIncome < 666667) {
            return 40833.33 + 0.32 * (taxableIncome - 166667);
        } else if (taxableIncome >= 666667) {
            return 200833.33 + 0.35 * (taxableIncome - 666667);
        }
        return 0;
    }
}
