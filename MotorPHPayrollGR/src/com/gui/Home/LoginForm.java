/* lick nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
/**
 *
 * @author JEO & Miles
 */
package com.gui.Home;

import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.DocumentFilter.FilterBypass;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * LoginForm - JFrame login UI with username/password, placeholders, spinner,
 * credential lookup, and HomePage launch with full User. Make sure to update
 * variable names if your .form file uses different ones!
 */
public class LoginForm extends javax.swing.JFrame {

    //Login Handler Declarations 
    private final Map<String, Integer> failedAttempts = new HashMap<>();
    private final Map<String, javax.swing.Timer> lockoutTimers = new HashMap<>();
    private final Map<String, Boolean> lockoutFlags = new HashMap<>();
    private static final int MAX_ATTEMPTS = 3;
    private static final int LOCKOUT_DURATION_MS = 60_000; // 1 minute for testing; use 300_000 for 5 mins
    private static final Path CRED_PATH = Paths.get("src", "com", "csv", "LoginCredentials.csv");

    // credentials.get("10001")[0] = password
    // credentials.get("10001")[1] = lockoutStatus
    private Map<String, String[]> loadLoginCredentials() {
        Map<String, String[]> map = new HashMap<>();
        try (CSVReader r = new CSVReader(Files.newBufferedReader(CRED_PATH))) {
            String[] row;
            boolean header = true;
            while ((row = r.readNext()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                map.put(row[0].trim(), row);  // ID → whole row
            }
        } catch (IOException | CsvValidationException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to read " + CRED_PATH.toAbsolutePath()
                    + "\n" + ex.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return map;
    }

    public LoginForm() {
        initComponents();

        //Frame Size and Position
        setSize(500, 600); // Or any size best fits form
        setResizable(false);
        setLocationRelativeTo(null); // This centers the window on the screen

        // Placeholders for username
        jTextField1.setForeground(Color.GRAY);
        jTextField1.setText("USERNAME");
        jTextField1.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (jTextField1.getText().equals("USERNAME")) {
                    jTextField1.setText("");
                    jTextField1.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (jTextField1.getText().isEmpty()) {
                    jTextField1.setForeground(Color.GRAY);
                    jTextField1.setText("USERNAME");
                }
            }
        });

        // Force Employee ID field to accept numbers only
        ((AbstractDocument) jTextField1.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string.matches("\\d+")) {
                    super.insertString(fb, offset, string, attr);
                } // else ignore non-digit input
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text.matches("\\d*")) { // allow empty or digits
                    super.replace(fb, offset, length, text, attrs);
                }
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                super.remove(fb, offset, length);
            }
        });
        // Placeholders for password
        jPasswordField1.setForeground(Color.GRAY);
        jPasswordField1.setEchoChar((char) 0);
        jPasswordField1.setText("PASSWORD");
        jPasswordField1.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                String pwd = new String(jPasswordField1.getPassword());
                if (pwd.equals("PASSWORD")) {
                    jPasswordField1.setText("");
                    jPasswordField1.setForeground(Color.BLACK);
                    jPasswordField1.setEchoChar('•');
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                String pwd = new String(jPasswordField1.getPassword());
                if (pwd.isEmpty()) {
                    jPasswordField1.setForeground(Color.GRAY);
                    jPasswordField1.setEchoChar((char) 0);
                    jPasswordField1.setText("PASSWORD");
                }
            }
        });

        // hide spinner
        jProgressBar1.setIndeterminate(false);
        jProgressBar1.setVisible(false);

        // Make Enter key activate Login
        getRootPane().setDefaultButton(jButton1);

        // Enable/disable Log in button dynamically based on field values and lockout state
        jButton1.setEnabled(false);

        javax.swing.event.DocumentListener docListener = new javax.swing.event.DocumentListener() {
            void update() {
                String user = jTextField1.getText().trim();
                String pass = new String(jPasswordField1.getPassword());

                boolean enable = !user.isEmpty() && !pass.isEmpty()
                        && !user.equals("USERNAME") && !pass.equals("PASSWORD");

                jButton1.setEnabled(enable);
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                update();
            }
        };

        jTextField1.getDocument().addDocumentListener(docListener);
        jPasswordField1.getDocument().addDocumentListener(docListener);
    }

    private void updateLockoutStatusCSV(String empId, String newStatus) {
        try {
            List<String[]> rows = new ArrayList<>();

            // read the whole file
            try (CSVReader r = new CSVReader(Files.newBufferedReader(CRED_PATH))) {
                String[] line;
                while ((line = r.readNext()) != null) {
                    if (line.length >= 6 && line[0].trim().equals(empId)) {
                        line[5] = newStatus;        // 6th col = Lock Out Status
                    }
                    rows.add(line);
                }
            }

            // rewrite the file
            try (CSVWriter w = new CSVWriter(
                    Files.newBufferedWriter(CRED_PATH,
                            StandardOpenOption.TRUNCATE_EXISTING))) {
                w.writeAll(rows);
            }

        } catch (IOException | CsvValidationException ex) {
            JOptionPane.showMessageDialog(this,
                    "Unable to update " + CRED_PATH.toAbsolutePath()
                    + "\n" + ex.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Reads employee info from /com/payroll/EmployeeData.csv and returns User
     */
    private User getUserFromEmployeeData(String username) {
        String path = "/com/csv/EmployeeData.csv";
        try (
                InputStream is = getClass().getResourceAsStream(path); Reader reader = new InputStreamReader(is); CSVReader csvReader = new CSVReader(reader)) {
            String[] parts;
            csvReader.readNext(); // skip header

            while ((parts = csvReader.readNext()) != null) {
                if (parts.length >= 19 && parts[0].trim().equals(username)) {
                    return new User(
                            parts[0].trim(), // EmpID
                            parts[1].trim(), // First Name
                            parts[2].trim(), // Last Name
                            parts[3].trim(), // Birthday
                            parts[4].trim(), // Hourly Rate
                            parts[5].trim(), // Rice Subsidy
                            parts[6].trim(), // Phone Allowance
                            parts[7].trim(), // Clothing Allowance
                            parts[8].trim(), // Status
                            parts[9].trim(), // Position
                            parts[10].trim(), // Basic Salary
                            parts[11].trim(), // Phone Number
                            parts[12].trim(), // SSS #
                            parts[13].trim(), // PhilHealth #
                            parts[14].trim(), // TIN #
                            parts[15].trim(), // Pag-ibig #
                            parts[16].trim(), // Immediate Supervisor
                            parts[17].trim(), // Gross Semi-monthly Rate
                            parts[18].trim() // Address
                    );
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error reading user info:\n" + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jButton1 = new javax.swing.JButton();
        jPasswordField1 = new javax.swing.JPasswordField();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jProgressBar1.setPreferredSize(new java.awt.Dimension(100, 5));

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/gui/images/LoginIcons/login.png"))); // NOI18N
        jButton1.setText("Log in");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jPasswordField1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPasswordField1.setText("Password");
        jPasswordField1.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jPasswordField1.setPreferredSize(new java.awt.Dimension(75, 25));

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/gui/images/LoginIcons/password.png")));
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/gui/images/LoginIcons/username.png")));
        jTextField1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField1.setText("USERNAME");
        jTextField1.setPreferredSize(new java.awt.Dimension(75, 25));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/gui/images/LoginIcons/Logo2.png"))); // NOI18N
        jLabel1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jLabel1.setDoubleBuffered(true);
        jLabel1.setFocusCycleRoot(true);
        jLabel1.setFocusTraversalPolicyProvider(true);
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel1.setMaximumSize(new java.awt.Dimension(400, 400));
        jLabel1.setOpaque(true);
        jLabel1.setPreferredSize(new java.awt.Dimension(350, 350));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(96, 96, 96)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(53, 53, 53)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(jLabel3)
                                            .addGap(12, 12, 12)
                                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(jLabel4)
                                            .addGap(12, 12, 12)
                                            .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)))))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(208, 208, 208)
                        .addComponent(jButton1)))
                .addContainerGap(87, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jLabel3))
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(jLabel4))
                    .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jButton1)
                .addGap(18, 18, 18)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(49, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        // Validate Employee ID first
        String user = jTextField1.getText().trim();

        if (user.isEmpty() || !user.matches("\\d+")) {
            JOptionPane.showMessageDialog(this,
                    "Enter correct Employee User ID (numbers only).",
                    "Invalid Employee ID",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        // show spinner
        jProgressBar1.setVisible(true);
        jProgressBar1.setIndeterminate(true);

        String pass = new String(jPasswordField1.getPassword());

        Map<String, String[]> credentials = loadLoginCredentials();
        // Blank fields

        if (user.isEmpty() || user.equals("USERNAME")) {
            JOptionPane.showMessageDialog(this, "Please Enter Username & Password", "Missing Information", JOptionPane.WARNING_MESSAGE);
        } else if (pass.isEmpty() || pass.equals("PASSWORD")) {
            JOptionPane.showMessageDialog(this, "Please Enter Password", "Missing Information", JOptionPane.WARNING_MESSAGE);
        } else if (!credentials.containsKey(user)) {
            JOptionPane.showMessageDialog(this, "Please enter valid username. Username is your Employee ID Number.", "Invalid Username", JOptionPane.ERROR_MESSAGE);
            jProgressBar1.setIndeterminate(false);
            jProgressBar1.setVisible(false);
            return;
        } else {
            String[] cred = credentials.get(user);
            String storedPass = cred.length > 1 ? cred[1] : "";
            String lockStatus = cred.length > 5 ? cred[5] : "No";
            lockoutFlags.put(user, "Yes".equalsIgnoreCase(lockStatus));
            int attempts = failedAttempts.getOrDefault(user, 0);
            boolean isLocked = lockoutFlags.getOrDefault(user, false) || "Yes".equalsIgnoreCase(lockStatus);
            if (isLocked) {
                // stop spinner
                jProgressBar1.setIndeterminate(false);
                jProgressBar1.setVisible(false);

                JOptionPane.showMessageDialog(
                        this,
                        "Your account has been locked by policy.\n"
                        + "Please contact IT to reactivate your login.",
                        "Account Locked",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            } else if (!storedPass.equals(pass)) {
                // Wrong password
                attempts++;
                failedAttempts.put(user, attempts);
                int remaining = MAX_ATTEMPTS - attempts;
                if (attempts >= MAX_ATTEMPTS) {
                    lockoutFlags.put(user, true);
                    updateLockoutStatusCSV(user, "Yes"); // Update the CSV file
                    jButton1.setEnabled(false);

                    javax.swing.Timer lockout = new javax.swing.Timer(LOCKOUT_DURATION_MS, e -> {
                        failedAttempts.put(user, 0);
                        lockoutTimers.remove(user);
                        lockoutFlags.put(user, false);

                        updateLockoutStatusCSV(user, "No"); // Reset in CSV

                        JOptionPane.showMessageDialog(LoginForm.this, "Login re-enabled. You may try again.", "Login Available", JOptionPane.INFORMATION_MESSAGE);
                        // Re-enable if fields are valid and user is not locked
                        String typedUser = jTextField1.getText().trim();
                        String typedPass = new String(jPasswordField1.getPassword());
                        boolean enable = !lockoutFlags.getOrDefault(typedUser, false)
                                && !typedUser.isEmpty() && !typedUser.equals("USERNAME")
                                && !typedPass.isEmpty() && !typedPass.equals("PASSWORD");
                        jButton1.setEnabled(enable);
                    });
                    lockout.setRepeats(false);
                    lockout.start();
                    lockoutTimers.put(user, lockout);

                    JOptionPane.showMessageDialog(
                            this,
                            "You have reached the maximum number of login attempts.\n"
                            + "This account is now locked.\nPlease contact the IT team or try again after "
                            + (LOCKOUT_DURATION_MS / 1000) + " seconds.",
                            "Login Locked",
                            JOptionPane.ERROR_MESSAGE
                    );
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Incorrect password. Attempt " + attempts + " of " + MAX_ATTEMPTS + ".",
                            "Incorrect Password",
                            JOptionPane.ERROR_MESSAGE
                    );
                    jPasswordField1.setText("");
                }
            } else {
                // Success
                failedAttempts.put(user, 0);
                lockoutFlags.put(user, false);
                if (lockoutTimers.containsKey(user)) {
                    lockoutTimers.get(user).stop();
                    lockoutTimers.remove(user);
                }
                User loggedUser = getUserFromEmployeeData(user);
                if (loggedUser != null) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Login successful.\nWelcome, " + loggedUser.getuFirstName() + "!",
                            "Welcome",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    new HomePage(loggedUser).setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "User data not found. Contact admin.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }

            jProgressBar1.setIndeterminate(false);
            jProgressBar1.setVisible(false);
    }//GEN-LAST:event_jButton1ActionPerformed
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(LoginForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LoginForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LoginForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LoginForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LoginForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables

}
