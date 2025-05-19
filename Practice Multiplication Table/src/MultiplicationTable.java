/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author HP
 */
public class MultiplicationTable {
 public static void main(String[] args) {
int[][] multiplicationTable = generateMultiplicationTable(10);
printMultiplicationTable(multiplicationTable);
}

public static int[][] generateMultiplicationTable(int size) {
int[][] table = new int[size][size];
for(int i = 0; i < size; i++) {
for(int j = 0; j < size; j++) {
table[i][j] = (i+1) * (j+1);
}
}
return table;
}

public static void printMultiplicationTable(int[][] table) {
for(int i = 0; i < table.length; i++) {
for(int j = 0; j < table[i].length; j++) {
System.out.print(table[i][j] + "\t");
}
System.out.println();
}
}
}
