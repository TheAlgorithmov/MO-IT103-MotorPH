/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author HP
 */
public class ArrayExample {
    
public static void main(String[] args) {
// Declare and initialize an array of integers
int[] myArray = { 1, 2, 3, 4, 5 };

// Traverse the array and print each element to the console
for (int i = 0; i < myArray.length; i++) {
System.out.println("Element at index " + i + ": " + myArray[i]);
}

// Find the sum of all elements in the array
int sum = 0;
for (int i = 0; i < myArray.length; i++) {
sum += myArray[i];
}
System.out.println("Sum of all elements: " + sum);

// Find the maximum value in the array
int max = myArray[0];
for (int i = 1; i < myArray.length; i++) {
if (myArray[i] > max) {
max = myArray[i];
}
}
System.out.println("Maximum value: " + max);
}
}
