/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package h1101.group3.student;

/**
 *
 * @author Leo Azarcon
 */
public class Student {
    // Attributes
    private String studentName;
    private String studentNo;
    private float quiz1;
    private float quiz2;
    private float quiz3;
    private float averageGrade;

    // Constructor
    public Student(String studentNo, String studentName, float quiz1, float quiz2, float quiz3) {
        this.studentNo = studentNo;
        this.studentName = studentName;
        this.quiz1 = quiz1;
        this.quiz2 = quiz2;
        this.quiz3 = quiz3;
    }

    // Getter methods
    public String getStudentNo() {
        return studentNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public float getQuiz1() {
        return quiz1;
    }

    public float getQuiz2() {
        return quiz2;
    }

    public float getQuiz3() {
        return quiz3;
    }

    public float getAveGrade() {
        return averageGrade;
    }

    // Setter methods
    public void setStudentNo(String studNo) {
        studentNo = studNo;
    }

    public void setStudentName(String studName) {
        studentName = studName;
    }

    public void setQuiz1(float q1) {
        quiz1 = q1;
    }

    public void setQuiz2(float q2) {
        quiz2 = q2;
    }

    public void setQuiz3(float q3) {
        quiz3 = q3;
    }

    // Method to compute average grade
    public float computeAverage() {
        averageGrade = (quiz1 + quiz2 + quiz3) / 3;
        return averageGrade;
    }
    public static void main(String[] args) {
        // Create a student object with a constructor
        Student student = new Student("2024170027", "Leonardo B. Azarcon III", 85.5f, 90.0f, 78.0f);

        // Display student information and computed average
        System.out.println("Student Name: " + student.getStudentName());
        System.out.println("Student No: " + student.getStudentNo());
        System.out.println("Quiz 1: " + student.getQuiz1());
        System.out.println("Quiz 2: " + student.getQuiz2());
        System.out.println("Quiz 3: " + student.getQuiz3());
        System.out.println("Average Grade: " + student.computeAverage());
    }
}

  //  public static void main(String[] args) {
    //    System.out.println("Hello World!");
   // }
//}
