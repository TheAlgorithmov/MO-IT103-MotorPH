/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gui.Home;

/**
 *
 * @author Miles & JEO
 */
public class User {
    private final String uEmpId;
    private final String uFirstName;
    private final String uLastName;
    private final String uDob;
    private final String uPosition;
    private final String uStatus;
    private final String uPhoneNumber;
    private final String uImmediateSupervisor;
    private final String uBasicSalary;
    private final String uHourlyRate;
    private final String uRiceSubsidy;
    private final String uPhoneAllowance;
    private final String uClothingAllowance;
    private final String uGrossSemiRate;
    private final String uSSS;
    private final String uPhilHealth;
    private final String uTIN;
    private final String uPagIbig;
    private final String uAddress;
    
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
        
        public boolean isLeadership() {
        return switch (uPosition) {
            case "Chief Executive Officer", "Chief Operating Officer", "Chief Finance Officer", "Chief Marketing Officer", "IT Operations and Systems", "Account Manager", "Accounting Head", "HR Manager", "HR Team Leader", "Payroll Team Leader", "Account Team Leader", "Payroll Manager" -> true;
            default -> false;
        };
  }
            public boolean isFinanceRole() {
        return switch (uPosition) {
            case "Chief Finance Officer", "Payroll Manager", "Payroll Team Leader", "Payroll Rank and File" -> true;
            default -> false;
        };
    }

            public boolean isITRole() {
                return "IT Operations and Systems".equals(uPosition);
            }

            public boolean isHRRole() {
        return switch (uPosition) {
            case "HR Manager", "HR Team Leader", "HR Rank and File" -> true;
            default -> false;
        };
            }

            /** Leadership, IT or HR can access full Employee Management
     * @return  */
            public boolean canAccessEmployeeManagement() {
                return isLeadership() || isITRole() || isHRRole();
            }

            /** Only finance/payroll positions can access Payroll Management
     * @return  */
            public boolean canAccessPayrollManagement() {
                return isFinanceRole();
            }
}
