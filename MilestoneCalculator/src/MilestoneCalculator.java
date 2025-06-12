/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


/**
 *
 * @author AtlasPrimE
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MilestoneCalculator extends JFrame {
    private JTextField txtMilestone1, txtMilestone2, txtTerminal;
    private JButton calculateButton;

    public MilestoneCalculator() {
        setTitle("Milestone Calculator");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 2, 10, 10));

        add(new JLabel("Milestone 1 (Max 25):"));
        txtMilestone1 = new JTextField();
        add(txtMilestone1);

        add(new JLabel("Milestone 2 (Max 40):"));
        txtMilestone2 = new JTextField();
        add(txtMilestone2);

        add(new JLabel("Terminal Assessment (Max 35):"));
        txtTerminal = new JTextField();
        add(txtTerminal);

        calculateButton = new JButton("Calculate Grade");
        add(calculateButton);

        // Enter key navigation and validation
        txtMilestone1.addActionListener(e -> {
            if (validateSingleField(txtMilestone1.getText(), 25, "Milestone 1")) {
                txtMilestone2.requestFocus();
            }
        });

        txtMilestone2.addActionListener(e -> {
            if (validateSingleField(txtMilestone2.getText(), 40, "Milestone 2")) {
                txtTerminal.requestFocus();
            }
        });

        txtTerminal.addActionListener(e -> calculateGrade());
        calculateButton.addActionListener(e -> calculateGrade());

        setVisible(true);
    }

    private boolean validateSingleField(String input, int max, String label) {
        try {
            float value = Float.parseFloat(input.trim());
            if (value < 0 || value > max) {
                JOptionPane.showMessageDialog(null, label + " must be between 0 and " + max + ".", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            return true;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Please enter a valid number for " + label + ".", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void calculateGrade() {
        try {
            float m1 = Float.parseFloat(txtMilestone1.getText().trim());
            float m2 = Float.parseFloat(txtMilestone2.getText().trim());
            float t = Float.parseFloat(txtTerminal.getText().trim());

            MilestoneEvaluator evaluator = new MilestoneEvaluator(m1, m2, t);
            float grade = evaluator.computeFinalGrade();

            JOptionPane.showMessageDialog(null, "Final Grade: " + String.format("%.2f", grade) + "%");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Please enter valid numbers only.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MilestoneCalculator());
    }
}

class MilestoneEvaluator {
    private float milestone1;
    private float milestone2;
    private float terminal;

    public MilestoneEvaluator(float m1, float m2, float t) {
        if (m1 < 0 || m1 > 25)
            throw new IllegalArgumentException("Milestone 1 must be between 0 and 25.");
        if (m2 < 0 || m2 > 40)
            throw new IllegalArgumentException("Milestone 2 must be between 0 and 40.");
        if (t < 0 || t > 35)
            throw new IllegalArgumentException("Terminal Assessment must be between 0 and 35.");

        this.milestone1 = m1;
        this.milestone2 = m2;
        this.terminal = t;
    }

    public float computeFinalGrade() {
        return milestone1 + milestone2 + terminal; // Total weighted score out of 100
    }
}

