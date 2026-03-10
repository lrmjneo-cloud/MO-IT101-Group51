/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package motorph.payroll.system;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class MotorPHPayrollSystem {
         //============================================
         // Main Method, Payroll Staff and Employee Menu
         //============================================
    public static void main(String[] args) {
        
        
       // File Location of employee data
       String empdata = "resources/EmployeeData.csv"; // location for Employee Details
       String attdata = "resources/AttendanceRecord.csv"; // location for Attendance Record
       
       Scanner input = new Scanner(System.in);
                  
       // Login System
       System.out.print("Username: "); 
       String username = input.nextLine(); // This is where we input Username
        
       System.out.print("Password: ");
       String password = input.nextLine(); // This is where we input Password
        
       // Will display error if username and password are not entered correctly
    if ((!username.equals("employee") && 
         !username.equals("payroll_staff")) ||
         !password.equals("12345"))
        
    { System.out.println("Incorrect username and/or password");
        return; }
     
       // Payroll_staff Menu
       if (username.equals("payroll_staff")) {
        System.out.println("1. Process One Employee");
        System.out.println("2. Process All Employees");
        System.out.println("3. Exit.");
        System.out.println("Select Option: ");
       
       int choice = Integer.parseInt(input.nextLine());
       
       if (choice == 3 ) return;
       
       if (choice == 1 ) {System.out.print("Enter Employee#: "); 
       String empInput = input.nextLine();
       viewData(empInput, empdata, attdata);
       
       return; }
         
       if (choice == 2) {
       
       try { BufferedReader rd = new BufferedReader(new FileReader(empdata));
       rd.readLine(); 
       
       String line;
       
       while ((line = rd.readLine()) != null) // Assign value to "line" and check if it's Null
       
       { if (line.trim().isEmpty()) continue;
        
        String[] data = line.split(","); viewData(data[0], empdata, attdata);}
        
        rd.close(); }
        
       // Show error if something goes wrong with reading the file
        catch (IOException e) { System.out.println("Error reading employee file: " + e.getMessage());}
       
       return; }
       }
        
       // Employee View
        if (username.equals("employee")) {

        System.out.println("1. View My Data");
        System.out.println("2. Exit Program");
        System.out.print("Select Option: ");

        int choice = Integer.parseInt(input.nextLine());

        if (choice == 2) {
        return;
    }

        if (choice == 1) {
        System.out.print("Enter Employee#: ");
        String empInput = input.nextLine();
        viewBasicEmp(empInput, empdata);
        return;
    }

       System.out.println("Invalid option.");
        }
                   

       input.close();
    }
          //============================================
         // Loop Cutoff: 1-15 and 16-end-of-month, CSV Employee and Attendance Data Reader
         //============================================
         // CSV EmpData Reader
        public static void viewData(String empInput, String empdata, String attdata){
        
        boolean found = false;
        double hourlyRate = 0;
                                      
        try (BufferedReader rd = new BufferedReader(new FileReader(empdata)))
       
        { rd.readLine(); //
        
        String line;
                                       
        while ((line = rd.readLine()) != null) 
        
        { if (line.trim().isEmpty()) { continue;}
        
        String[] data = line.split(",");
             
        if (!data[0].equals(empInput)) continue;
        found = true;
        System.out.println("Employee #: " + data[0]);
        System.out.println("Last name: " + data[1]);
        System.out.println("First name: " + data[2]);
        System.out.println("Date of Birth: " + data[3]);
        
        hourlyRate = Double.parseDouble(data[data.length - 1 ].trim());
        
        break;}
        }
       catch (Exception e) { System.out.println("Error reading employee file." + e.getMessage());
         return; }
        
        if (!found){System.out.println("Employee does not exist.");}
                            
       DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("H:mm");
       
       // Attendance Loop June - December Cut-Off && Attendance Data Reader
       for (int month = 6; month <= 12; month++) {
       
       double firstCutOff = 0;
       double secondCutOff = 0;
              
      int daysInMonth = YearMonth.of(2024, month).lengthOfMonth();
       
      try (BufferedReader rd = new BufferedReader(new FileReader(attdata))){
       
       rd.readLine();
       
       String line;
       
       while ((line = rd.readLine()) != null) 
           
       {if (line.trim().isEmpty()) { continue;}
       
       String[] data = line.split(",");
       
       if (!data[0].equals(empInput)) continue;
       
       String [] dateParts = data[3].split("/");
       
       int recordMonth = Integer.parseInt(dateParts[0]);
       int day = Integer.parseInt(dateParts[1]);
       int year = Integer.parseInt(dateParts[2]);
       
       if (year !=2024 || recordMonth != month){ continue;}
       
       LocalTime login = LocalTime.parse(data[4].trim(), timeFormat);
       LocalTime logout = LocalTime.parse(data[5].trim(), timeFormat);
       
       double hoursWorked = computeHours(login, logout);
          
       
       if (day <= 15) firstCutOff += hoursWorked;
                   
       else secondCutOff += hoursWorked;
      }
        
      } catch (IOException | NumberFormatException e) { System.out.println("Error reading attendance file for month " + month + e.getMessage());
                continue; }  
      
       // Payroll Calculation - calling Salary and Government deductions method  
       double firstGrossSalary = hourlyRate * firstCutOff;
       double secondGrossSalary = hourlyRate * secondCutOff;
       double monthlyGrossSalary = firstGrossSalary + secondGrossSalary;
       double sss = computeSSS(monthlyGrossSalary);
       double premium = computePhilHealth(monthlyGrossSalary);
       double pagibig = computePagIBIG(monthlyGrossSalary);
       double taxableIncome = monthlyGrossSalary - sss - premium - pagibig;
       double tax = computeTax(taxableIncome);
       double totalDeductions = sss + premium + pagibig + tax;
       double netSalary1 = firstGrossSalary;
       double netSalary2 = secondGrossSalary - totalDeductions;
       
       
        String nameofMonth = switch (month) {
        case 6 -> "June";
        case 7 -> "July";
        case 8 -> "August";
        case 9 -> "September";
        case 10 -> "October";
        case 11 -> "November";
        case 12 -> "December";
        default -> "Month " + month;};
    
           
    System.out.println("\nCutoff Date: " + nameofMonth + " 1 to 15");
    System.out.println("Total Hours Worked: " + firstCutOff);
    System.out.println("Gross Salary: " + firstGrossSalary);
    System.out.println("Net Salary: " + netSalary1);

    System.out.println("\nCutoff Date: " + nameofMonth + " 16 to " + daysInMonth);
    System.out.println("Total Hours Worked: " + secondCutOff);
    System.out.println("Gross Salary: " + secondGrossSalary);
    System.out.println("Deductions: ");
    System.out.println("    SSS: " + sss);
    System.out.println("    PhilHealth: " + premium);
    System.out.println("    Pag-IBIG: " + pagibig);
    System.out.println("    Tax: " + tax);
    System.out.println("    Total Deductions: " + totalDeductions);
    System.out.println("Net Salary: " + netSalary2);
       }
        }
         //============================================
         // Method to Compute Hours Worked
         //============================================
        
        public static double computeHours(LocalTime login, LocalTime logout) {
        
            LocalTime graceTime = LocalTime.of(8, 10);
            LocalTime cutoffTime = LocalTime.of(17, 0);
            
            if (logout.isAfter(cutoffTime)) { logout = cutoffTime;}
            
            long minutesWorked = Duration.between(login, logout).toMinutes();
            
            if (minutesWorked > 60){ minutesWorked = minutesWorked - 60;}
            else { minutesWorked = 0;}
        
            double hoursWorked = minutesWorked / 60.0;

            if (!login.isAfter(graceTime)) {hoursWorked = 8.0;}
            if (hoursWorked > 8) {hoursWorked = 8;}
            return hoursWorked;}

          //============================================ 
         // Method to call basic employee details only
        //============================================
        public static void viewBasicEmp(String empInput, String empdata) {

    try { BufferedReader rd = new BufferedReader(new FileReader(empdata));
        rd.readLine(); // skip header

        String line;

        while ((line = rd.readLine()) != null) {

            String[] data = line.split(",");

            if (data[0].equals(empInput)) {

                System.out.println("\nEmployee Information");
                System.out.println("----------------------");
                System.out.println("Employee Number: " + data[0]);
                System.out.println("Last Name: " + data[1]);
                System.out.println("First Name: " + data[2]);
                System.out.println("Date of Birth: " + data[3]);

                break;
            }
        }

        rd.close();

    } catch (IOException e) {
        System.out.println("Error reading employee file.");
    }
        }
        
         //============================================
        // Method to Call Goverment Deductions
        //=============================================
        
        // Method to Call SSS Deduction
         public static double computeSSS (double monthlyGrossSalary){
            
    double[] maxRange = {3250, 3750, 4250, 4750, 5250, 5750, 6250, 6750, 7250, 7750,
                         8250, 8750, 9250, 9750, 10250, 10750, 11250, 11750, 12250,
                         12750, 13250, 13750, 14250, 14750, 15250, 15750, 16250, 16750,
                         17250, 17750, 18250, 18750, 19250, 19750, 20250, 20750, 21250,
                         21750, 22250, 22750, 23250, 23750, 24250, 24750, 999999};

    double[] contribution = {135, 157.5, 180, 202.5, 225, 247.5, 270, 292.5, 315, 337.5,
                             360, 382.5, 405, 427.5, 450, 472.5, 495, 517.5, 540,
                             562.5, 585, 607.5, 630, 652.5, 675, 697.5, 720, 742.5,
                             765, 787.5, 810, 832.5, 855, 877.5, 900, 922.5, 945,
                             967.5, 990, 1012.5, 1035, 1057.5, 1080, 1102.5, 1125};

           for (int i = 0; i < maxRange.length; i++) {
           if (monthlyGrossSalary <= maxRange[i]) { return contribution[i]; }
           
            }
           return 0;
         }
         
         // Method to Call PhilHealth Deduction
        public static double computePhilHealth(double monthlyGrossSalary){

    double premium = monthlyGrossSalary * 0.03;

           if (premium < 300) {  premium = 300 / 2; }

       
           if (premium > 1800) { premium = 1800 / 2;}
        
           return premium;
}
        
        // Method to Call PagIbig Deduction
        public static double computePagIBIG(double monthlyGrossSalary){

        double contribution;

        if (monthlyGrossSalary <= 1500){ contribution = monthlyGrossSalary * 0.01;}
    
        else { contribution = monthlyGrossSalary * 0.02;}
       
        // Maximum employee contribution
        if (contribution > 100){ contribution = 100;}
        
        return contribution;
}
        // Method to Call Withholding Tax
       public static double computeTax(double taxableIncome){

       double tax = 0;

       if (taxableIncome <= 20832){ tax = 0; }
        
       else if (taxableIncome < 33333){ tax = (taxableIncome - 20833) * 0.20; }
            
       else if (taxableIncome < 66667){ tax = 2500 + (taxableIncome - 33333) * 0.25; }
            
       else if (taxableIncome < 166667){ tax = 10833 + (taxableIncome - 66667) * 0.30; }
        
       else if (taxableIncome < 666667){ tax = 40833.33 + (taxableIncome - 166667) * 0.32; }
            
       else{ tax = 200833.33 + (taxableIncome - 666667) * 0.35; }
        
       return tax;
    }
}

   
