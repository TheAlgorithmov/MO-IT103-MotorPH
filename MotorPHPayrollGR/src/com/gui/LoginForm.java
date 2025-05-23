/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package com.gui;

/**
 *
 * @author Miles
 */

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LoginForm extends JFrame {
    
    private String uEmpId, uFirstName, uLastName, uDob, uPosition, uStatus;
    
    public LoginForm() {
        
        setTitle("Login");
        setSize(425, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout()); // Set layout at the beginning
        

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.LIGHT_GRAY);

        // Load Logo Image
        ImageIcon logo = new ImageIcon(getClass().getResource("/logo.png"));
        JLabel logoLabel = new JLabel(logo);
        
        // Load Title Image
        ImageIcon title = new ImageIcon(getClass().getResource("/Title.png"));
        JLabel titleLabel = new JLabel(title);
        
        headerPanel.add(logoLabel, BorderLayout.WEST);
        headerPanel.add(titleLabel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH); // Add header at the top

        // Center Panel for Login Fields
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(userLabel, gbc);

        JTextField userField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 0;
        centerPanel.add(userField, gbc);

        JLabel passLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        centerPanel.add(passLabel, gbc);

        JPasswordField passField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        centerPanel.add(passField, gbc);

        JButton loginBtn = new JButton("Login");
        gbc.gridx = 1;
        gbc.gridy = 2;
        centerPanel.add(loginBtn, gbc);

        add(centerPanel, BorderLayout.CENTER); // Add login form to center

        // Login Button Action
        loginBtn.addActionListener(e -> {
            String logCred = "src/com/gui/LoginCredentials.csv";
            String userInput = userField.getText();
            String passInput = new String(passField.getPassword());
            
            if (authenticateUser(logCred, userInput, passInput)) {
                String firstName = userFullName[0];
                String lastName = userFullName[1];
                fetchEmployeeDetails(firstName, lastName);
                
                // Create User Object
                User currentUser = new User(uEmpId, uFirstName, uLastName, uDob, uPosition, uStatus);
                // Call HomePage
                HomePage home = new HomePage(currentUser);
                dispose();
                
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials");
            }
            
        });

        revalidate(); // Refresh UI
        repaint();
        
        // Get screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - getWidth()) / 2;
        int y = (screenSize.height - getHeight()) / 2;

        // Set window position
        setLocation(x, y);
        setResizable(false);
        setVisible(true);
        
    }
    
    private String[] userFullName = new String[2];
    
    public boolean authenticateUser(String logCred, String user, String pass) {
        try (BufferedReader br = new BufferedReader(new FileReader(logCred))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 4 && values[1].equals(user) && values[0].equals(pass)) {
                    userFullName[0] = values[2];
                    userFullName[1] = values[3];
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private void fetchEmployeeDetails(String firstName, String lastName) {
        String employeeFile = "src/com/payroll/EmployeeData.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(employeeFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 10 && values[1].equalsIgnoreCase(firstName) && values[2].equalsIgnoreCase(lastName)) {
                    uEmpId = values[0];
                    uFirstName = values[1];
                    uLastName = values[2];
                    uDob = values[3];
                    uPosition = values[9];
                    uStatus = values[8];
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}