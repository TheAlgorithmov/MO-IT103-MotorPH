/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gui;

/**
 *
 * @author Miles
 */
public class User {
    private String uEmpId;
    private String uFirstName;
    private String uLastName;
    private String uDob;
    private String uPosition;
    private String uStatus;
    
    public User(String uEmpId, String uFirstName, String uLastName, String uDob, String uPosition, String uStatus) {
        this.uEmpId = uEmpId;
        this.uFirstName = uFirstName;
        this.uLastName = uLastName;
        this.uDob = uDob;
        this.uPosition = uPosition;
        this.uStatus = uStatus;
    }
    
    public String getuEmpId() {
        return uEmpId;
    }
    
    public String getuFirstName() {
        return uFirstName;
    }
    
    public String getuLastName() {
        return uLastName;
    }
    
    public String getuDob() {
        return uDob;
    }
    
    public String getuPosition() {
        return uPosition;
    }
    
    public String getuStatus() {
        return uStatus;
    }
}
