/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gui;

/**
 *
 * @author Miles & JEO
 */
public class User {
    private String uEmpId;
    private String uFirstName;
    private String uLastName;
    private String uDob;
    private String uPosition;
    private String uStatus;
    private String uPhoneNumber;
    private String uImmediateSupervisor;
    private String uBasicSalary;
    private String uHourlyRate;
    private String uRiceSubsidy;
    private String uPhoneAllowance;
    private String uClothingAllowance;
    private String uGrossSemiRate;
    private String uSSS;
    private String uPhilHealth;
    private String uTIN;
    private String uPagIbig;
    private String uAddress;
    private String uFirstname;
    private String uLastname;
    
        public User(String uEmpId, String uFirstName, String uLastName, String uDob,
                String uHourlyRate, String uRiceSubsidy, String uPhoneAllowance, String uClothingAllowance,
                String uStatus, String uPosition, String uBasicSalary, String uPhoneNumber,
                String uSSS, String uPhilHealth, String uTIN, String uPagIbig,
                String uImmediateSupervisor, String uGrossSemiRate, String uAddress) {

        this.uEmpId = uEmpId;
        this.uFirstName = uFirstName;
        this.uLastName = uLastName;
        this.uDob = uDob;
        this.uHourlyRate = uHourlyRate;
        this.uRiceSubsidy = uRiceSubsidy;
        this.uPhoneAllowance = uPhoneAllowance;
        this.uClothingAllowance = uClothingAllowance;
        this.uStatus = uStatus;
        this.uPosition = uPosition;
        this.uBasicSalary = uBasicSalary;
        this.uPhoneNumber = uPhoneNumber;
        this.uSSS = uSSS;
        this.uPhilHealth = uPhilHealth;
        this.uTIN = uTIN;
        this.uPagIbig = uPagIbig;
        this.uImmediateSupervisor = uImmediateSupervisor;
        this.uGrossSemiRate = uGrossSemiRate;
        this.uAddress = uAddress;
    }

        public String getuEmpId() { return uEmpId; }
        public String getuFirstName() { return uFirstName; }
        public String getuLastName() { return uLastName; }
        public String getuDob() { return uDob; }
        public String getuPosition() { return uPosition; }
        public String getuStatus() { return uStatus; }
        public String getuPhoneNumber() { return uPhoneNumber; }
        public String getuImmediateSupervisor() { return uImmediateSupervisor; }
        public String getuBasicSalary() { return uBasicSalary; }
        public String getuHourlyRate() { return uHourlyRate; }
        public String getuRiceSubsidy() { return uRiceSubsidy; }
        public String getuPhoneAllowance() { return uPhoneAllowance; }
        public String getuClothingAllowance() { return uClothingAllowance; }
        public String getuGrossSemiRate() { return uGrossSemiRate; }
        public String getuSSS() { return uSSS; }
        public String getuPhilHealth() { return uPhilHealth; }
        public String getuTIN() { return uTIN; }
        public String getuPagIbig() { return uPagIbig; }
        public String getuAddress() { return uAddress; }
}
