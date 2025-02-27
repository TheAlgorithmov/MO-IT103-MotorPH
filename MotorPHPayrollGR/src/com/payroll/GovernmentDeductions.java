package com.payroll;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * GovernmentDeductions - Computes SSS, PhilHealth, Pag-IBIG, and BIR tax deductions.
 */
public class GovernmentDeductions {

    // ------------------------------------------------------------------------------------
    // GOVERNMENT DEDUCTIONS (SSS, PhilHealth, Pag-IBIG, BIR Tax)
    // ------------------------------------------------------------------------------------

    /**
     * Calculates the SSS contribution based on gross income.
     * 
     * @param grossIncome The employee's gross income
     * @return The SSS contribution amount
     */
    public static float calculateSSS(float grossIncome) {
        // SSS CONTRIBUTION (Jan 2024 Table)
        NavigableMap<Float, Float> sssTable = new TreeMap<>();
        sssTable.put(3250f, 135.00f);
        sssTable.put(3750f, 157.50f);
        sssTable.put(4250f, 180.00f);
        sssTable.put(4750f, 202.50f);
        sssTable.put(5250f, 225.00f);
        sssTable.put(5750f, 247.50f);
        sssTable.put(6250f, 270.00f);
        sssTable.put(6750f, 292.50f);
        sssTable.put(7250f, 315.00f);
        sssTable.put(7750f, 337.50f);
        sssTable.put(8250f, 360.00f);
        sssTable.put(8750f, 382.50f);
        sssTable.put(9250f, 405.00f);
        sssTable.put(9750f, 427.50f);
        sssTable.put(10250f, 450.00f);
        sssTable.put(10750f, 472.50f);
        sssTable.put(11250f, 495.00f);
        sssTable.put(11750f, 517.50f);
        sssTable.put(12250f, 540.00f);
        sssTable.put(12750f, 562.50f);
        sssTable.put(13250f, 585.00f);
        sssTable.put(13750f, 607.50f);
        sssTable.put(14250f, 630.00f);
        sssTable.put(14750f, 652.50f);
        sssTable.put(15250f, 675.00f);
        sssTable.put(15750f, 697.50f);
        sssTable.put(16250f, 720.00f);
        sssTable.put(16750f, 742.50f);
        sssTable.put(17250f, 765.00f);
        sssTable.put(17750f, 787.50f);
        sssTable.put(18250f, 810.00f);
        sssTable.put(18750f, 832.50f);
        sssTable.put(19250f, 855.00f);
        sssTable.put(19750f, 877.50f);
        sssTable.put(20250f, 900.00f);
        sssTable.put(24750f, 1125.00f);

        // Get the nearest lower or equal salary bracket
        Map.Entry<Float, Float> entry = sssTable.floorEntry(grossIncome);
        if (entry == null) {
            return 0.00f; // Return 0 if no valid SSS bracket is found
        }

        return entry.getValue();
    }

    /**
     * Calculates the PhilHealth contribution.
     * 
     * @param grossIncome The employee's gross income
     * @return The weekly PhilHealth contribution (employee share)
     */
    public static float calculatePhilHealth(float grossIncome) {
        // PHILHEALTH CONTRIBUTION (Deducted Monthly)
        float monthlyPhilHealth = (grossIncome <= 10000) ? 300f : Math.min(1800f, 0.03f * grossIncome);
        return (monthlyPhilHealth / 4); // Employee share divided by 4 for weekly deduction
    }

    /**
     * Calculates the Pag-IBIG contribution.
     * 
     * @param grossIncome The employee's gross income
     * @return The weekly Pag-IBIG contribution
     */
    public static float calculatePagibig(float grossIncome) {
        // PAG-IBIG CONTRIBUTION (Pro-rated Weekly)
        float monthlyHDMF = (grossIncome <= 1500) ? Math.min(100f, 0.01f * grossIncome) : Math.min(100f, 0.02f * grossIncome);
        return monthlyHDMF / 4;
    }

    /**
     * Calculates the BIR withholding tax based on taxable income.
     * 
     * @param taxableIncome The employee's taxable income
     * @return The BIR tax amount
     */
    public static float calculateBIR(float taxableIncome) {
        // BIR WITHHOLDING TAX (DOLE Rates)
        if (taxableIncome > 20832 && taxableIncome < 33333) {
            return 0.20f * (taxableIncome - 20833);
        } else if (taxableIncome >= 33333 && taxableIncome < 66667) {
            return 2500f + 0.25f * (taxableIncome - 33333);
        } else if (taxableIncome >= 66667 && taxableIncome < 166667) {
            return 10833f + 0.30f * (taxableIncome - 66667);
        } else if (taxableIncome >= 166667 && taxableIncome < 666667) {
            return 40833.33f + 0.32f * (taxableIncome - 166667);
        } else if (taxableIncome >= 666667) {
            return 200833.33f + 0.35f * (taxableIncome - 666667);
        }
        return 0f;
    }
}
