/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author HP
 */
public class StudentGrades {
    
public static void main(String[] args) {
int[][] grades = {{80, 85, 90}, {70, 75, 80}, {90, 95, 100}};
printGrades(grades);
System.out.println("Average grade: " + calculateAverage(grades));
System.out.println("Highest grade: " + findHighest(grades));
}

public static void printGrades(int[][] grades) {
for(int i = 0; i < grades.length; i++) {
System.out.print("Student " + (i+1) + ": ");
for(int j = 0; j < grades[i].length; j++) {
System.out.print(grades[i][j] + " ");
}
System.out.println();
}
}

public static double calculateAverage(int[][] grades) {
double total = 0;
int count = 0;
for(int i = 0; i < grades.length; i++) {
for(int j = 0; j < grades[i].length; j++) {
total += grades[i][j];
count++;
}
}
return total / count;
}

public static int findHighest(int[][] grades) {
int highest = grades[0][0];
for(int i = 0; i < grades.length; i++) {
for(int j = 0; j < grades[i].length; j++) {
if(grades[i][j] > highest) {
highest = grades[i][j];
}
}
}
return highest;
}
}