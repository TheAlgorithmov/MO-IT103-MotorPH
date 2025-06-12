/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Example;

/**
 *
 * @author AtlasPrimE
 */
import java.util.HashSet;
import java.util.Set;

public class Example {
public static void main(String[] args) {
// create a new HashSet to store strings
Set<String> mySet = new HashSet<>();

// add some strings to the set
mySet.add("apple");
mySet.add("banana");
mySet.add("orange");
mySet.add("apple"); // this will not be added to the set, since it is a duplicate

// remove a string from the set
mySet.remove("orange");

// check if the set contains a particular string
boolean containsBanana = mySet.contains("banana");
System.out.println("Does the set contain 'banana'? " + containsBanana);

// get the size of the set
int size = mySet.size();
System.out.println("The size of the set is: " + size);

// print out the elements in the set
System.out.println("The elements in the set are:");
for (String element : mySet) {
System.out.println(element);
}
}
}
