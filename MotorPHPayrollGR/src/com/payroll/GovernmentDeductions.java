package com.payroll;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * GovernmentDeductions - Computes mandatory government deductions for employees.
 *
 * This class provides methods to calculate:
 * 1. **SSS (Social Security System)** - Based on a salary bracket system using Basic Salary.
 * 2. **PhilHealth (Health Insurance)** - 3% of the monthly salary, with a cap.
 * 3. **Pag-IBIG (Housing Fund)** - 1% or 2% depending on salary.
 * 4. **BIR (Income Tax)** - Based on progressive tax brackets.
 */
public class GovernmentDeductions {

    /**
     * Calculates the SSS (Social Security System) contribution based on Basic Salary.
     *
     * @param basicSalary The employee's Basic Salary (monthly, from EmployeeData.csv)
     * @return The SSS contribution amount (monthly)
     */
    public static float calculateSSS(float basicSalary) {
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

        Map.Entry<Float, Float> entry = sssTable.floorEntry(basicSalary);
        return (entry != null) ? entry.getValue() : 0.00f;
    }

    /**
     * Calculates the PhilHealth (health insurance) contribution based on gross income.
     *
     * @param grossIncome The employee's gross income (monthly)
     * @return The monthly PhilHealth contribution (employee share)
     */
    public static float calculatePhilHealth(float grossIncome) {
        return (grossIncome <= 10000) ? 300f : Math.min(1800f, 0.03f * grossIncome);
    }

    /**
     * Calculates the Pag-IBIG (housing fund) contribution based on gross income.
     *
     * @param grossIncome The employee's gross income (monthly)
     * @return The monthly Pag-IBIG contribution
     */
    public static float calculatePagibig(float grossIncome) {
        return (grossIncome <= 1500) ? Math.min(100f, 0.01f * grossIncome) : Math.min(100f, 0.02f * grossIncome);
    }

    /**
     * Calculates the BIR (Bureau of Internal Revenue) withholding tax.
     *
     * Uses a **progressive tax system** for monthly taxable income:
     *
     * - **≤ PHP 20,832** → **0%** (No tax)
     * - **PHP 20,833 - PHP 33,332** → **20%** of excess over PHP 20,833
     * - **PHP 33,333 - PHP 66,666** → PHP 2,500 + **25%** of excess over PHP 33,333
     * - **PHP 66,667 - PHP 166,666** → PHP 10,833 + **30%** of excess over PHP 66,667
     * - **PHP 166,667 - PHP 666,666** → PHP 40,833.33 + **32%** of excess over PHP 166,667
     * - **≥ PHP 666,667** → PHP 200,833.33 + **35%** of excess over PHP 666,667
     *
     * @param taxableIncome The employee's monthly taxable income (gross - deductions)
     * @return The monthly BIR tax amount
     */
    public static float calculateBIR(float taxableIncome) {
        if (taxableIncome <= 20832) {
            return 0f; // No tax for income ≤ 20,832
        } else if (taxableIncome <= 33332) {
            return 0.20f * (taxableIncome - 20833);
        } else if (taxableIncome <= 66666) {
            return 2500f + 0.25f * (taxableIncome - 33333);
        } else if (taxableIncome <= 166666) {
            return 10833f + 0.30f * (taxableIncome - 66667);
        } else if (taxableIncome <= 666666) {
            return 40833.33f + 0.32f * (taxableIncome - 166667);
        } else {
            return 200833.33f + 0.35f * (taxableIncome - 666667);
        }
    }
}
