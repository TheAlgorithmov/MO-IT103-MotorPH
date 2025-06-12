package Student;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author AtlasPrimE
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MyButtonHandler implements ActionListener {

    // This method runs when the button is clicked
    public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(null, "Hello, World!");
    }

    // Create a GUI with a button and link the event handler
    public static void main(String[] args) {
        JFrame frame = new JFrame("My Button Handler");
        JButton button = new JButton("Click me!");
        button.addActionListener(new MyButtonHandler()); // 👈 link the handler
        frame.getContentPane().add(button, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // optional
        frame.pack();
        frame.setVisible(true);
    }
public class ExceptionDemo {
    public static void main(String[] args) {
        try {
            int result = 5 / 0; // This will throw ArithmeticException
        } catch (ArithmeticException e) {
            System.out.println("Division by zero exception caught!");
        } finally {
            System.out.println("This block always runs.");
        }
    }
}

}
