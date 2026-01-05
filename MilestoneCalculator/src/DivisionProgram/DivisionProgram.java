package DivisionProgram;

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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DivisionProgram extends JFrame implements ActionListener {
private JTextField dividendField, divisorField, resultField;


public DivisionProgram() {
super("Division Program");


// Create the UI components
JLabel dividendLabel = new JLabel("Dividend:");
JLabel divisorLabel = new JLabel("Divisor:");
JLabel resultLabel = new JLabel("Result:");
dividendField = new JTextField();
divisorField = new JTextField();
resultField = new JTextField();
resultField.setEditable(false);
JButton divideButton = new JButton("Divide");
divideButton.addActionListener(this);


// Add the components to the content pane
Container contentPane = getContentPane();
contentPane.setLayout(new GridLayout(10, 8));
contentPane.add(dividendLabel);
contentPane.add(dividendField);
contentPane.add(divisorLabel);
contentPane.add(divisorField);
contentPane.add(resultLabel);
contentPane.add(resultField);
contentPane.add(new JLabel());
contentPane.add(divideButton);


// Set the window size and make it visible
pack();
setVisible(true);
}

public void actionPerformed(ActionEvent e) {
if (e.getActionCommand().equals("Divide")) {
try {
int dividend = Integer.parseInt(dividendField.getText());
int divisor = Integer.parseInt(divisorField.getText());
int result = dividend / divisor;
resultField.setText(Integer.toString(result));
} catch (NumberFormatException | ArithmeticException ex) {
resultField.setText("Error: " + ex.getMessage());
}
}
}

public static void main(String[] args) {
DivisionProgram program = new DivisionProgram();
program.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
}
}
