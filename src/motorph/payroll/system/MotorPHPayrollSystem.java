package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    static String empFile = "public/employee_details.csv";
    static String attendanceFile = "public/attendance_record.csv";
    static Map<String, List<String>> employeeData = new HashMap<>();
    static Map<String, List<String[]>> attendanceData = new HashMap<>();
    static DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("H:mm");

    // GUI Components that need to be accessed across methods
    private static CardLayout cardLayout;
    private static JPanel mainCardPanel;
    private static JTextField usernameField;
    private static JPasswordField passwordField;

    public static void main(String[] args) {
        // Run the GUI thread safely
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("MotorPH Payroll System");
        frame.setSize(700, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainCardPanel = new JPanel(cardLayout);

        // Add Screens to the CardLayout container
        mainCardPanel.add(createLoginPanel(), "LoginScreen");
        mainCardPanel.add(createDashboardPanel(), "DashboardScreen");

        frame.add(mainCardPanel);
        cardLayout.show(mainCardPanel, "LoginScreen");
        frame.setVisible(true);
    }

    private static JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(120, 180, 120, 180));

        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        JButton loginButton = new JButton("Login");
        panel.add(new JLabel("")); // Empty space placeholder
        panel.add(loginButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (("employee".equals(username) || "payroll_staff".equals(username)) && "12345".equals(password)) {
                    initData(); 
                    cardLayout.show(mainCardPanel, "DashboardScreen");
                } else {
                    JOptionPane.showMessageDialog(null, "Incorrect username or password.", "Login Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return panel;
    }

    private static JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();

        // --- TAB 1: EMPLOYEE PORTAL ---
        JPanel empPanel = new JPanel(new BorderLayout(10, 10));
        empPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel empTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField empNumField = new JTextField(12);
        JButton searchButton = new JButton("View Profile");
        empTopPanel.add(new JLabel("Enter Employee #:"));
        empTopPanel.add(empNumField);
        empTopPanel.add(searchButton);
        
        JTextArea displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        empPanel.add(empTopPanel, BorderLayout.NORTH);
        empPanel.add(new JScrollPane(displayArea), BorderLayout.CENTER);

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String empNum = empNumField.getText().trim();
                String[] empData = findEmployeeRecord(empNum);
                if (empData != null) {
                    displayArea.setText("==================================================\n" +
                                        "          MOTORPH EMPLOYEE INFORMATION            \n" +
                                        "==================================================\n" +
                                        "Employee Number: " + empData[0] + "\n" +
                                        "Employee Name:   " + empData[2] + " " + empData[1] + "\n" +
                                        "Birthday:        " + empData[3] + "\n" +
                                        "Hourly Rate:     PHP " + empData[18] + "\n" +
                                        "==================================================");
                } else {
                    displayArea.setText("Employee number does not exist.");
                }
            }
        });

        // --- TAB 2: PAYROLL MANAGEMENT ---
        JPanel payrollPanel = new JPanel(new BorderLayout(10, 10));
        payrollPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel payrollTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField payrollEmpField = new JTextField(10);
        JButton processSingleButton = new JButton("Process Single Employee");
        JButton processAllButton = new JButton("Process All Employees");
        
        payrollTopPanel.add(new JLabel("Employee #:"));
        payrollTopPanel.add(payrollEmpField);
        payrollTopPanel.add(processSingleButton);
        payrollTopPanel.add(processAllButton);

        JTextArea payrollDisplayArea = new JTextArea();
        payrollDisplayArea.setEditable(false);
        payrollDisplayArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        payrollPanel.add(payrollTopPanel, BorderLayout.NORTH);
        payrollPanel.add(new JScrollPane(payrollDisplayArea), BorderLayout.CENTER);

        processSingleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String empNum = payrollEmpField.getText().trim();
                String[] empData = findEmployeeRecord(empNum);
                if (empData == null) {
                    payrollDisplayArea.setText("Employee number does not exist.");
                    return;
                }
                StringBuilder sb = new StringBuilder();
                processFullPayroll(empNum, sb);
                payrollDisplayArea.setText(sb.toString());
            }
        });

        processAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StringBuilder sb = new StringBuilder();
                sb.append("==================================================\n");
                sb.append("          SYSTEM GENERATED BATCH PAYROLL          \n");
                sb.append("==================================================\n");
                for (String empNum : employeeData.keySet()) {
                    processFullPayroll(empNum, sb);
                    sb.append("\n--------------------------------------------------\n");
                }
                payrollDisplayArea.setText(sb.toString());
            }
        });

        tabbedPane.addTab("Employee Portal", empPanel);
        tabbedPane.addTab("Payroll Management", payrollPanel);
        panel.add(tabbedPane, BorderLayout.CENTER);

        return panel;
    }

    // --- CORE PAYROLL PROCESSING RECONFIGURED FOR STRINGS ---

    public static void initData() {
        loadEmployeeData();
        loadAttendanceData();
    }

    private static void processFullPayroll(String empNum, StringBuilder sb) {
        String[] empData = findEmployeeRecord(empNum);
        if (empData == null) return;

        double hourlyRate = Double.parseDouble(empData[18]);
        String[] months = {"06", "07", "08", "09", "10", "11", "12"};

        for (String month : months) {
            calculateAndAppendCutoff(empData[0], empData[2], empData[1], month, 1, 15, hourlyRate, false, sb);
            calculateAndAppendCutoff(empData[0], empData[2], empData[1], month, 16, 31, hourlyRate, true, sb);
        }
    }

    private static void calculateAndAppendCutoff(String empNum, String fName, String lName, String month, int start, int end, double rate, boolean isSecondHalf, StringBuilder sb) {
        double hours = getTotalHoursForPeriod(empNum, month, start, end);
        double gross = hours * rate;

        sb.append(String.format("\nCutoff Period: %s %d-%d\n", getMonthName(month), start, end));
        sb.append(String.format("Employee ID: %s | Name: %s %s\n", empNum, fName, lName));
        sb.append(String.format("Total Hours worked: %.2f | Gross Income: %.2f\n", hours, gross));

        if (isSecondHalf) {
            double firstHalfHours = getTotalHoursForPeriod(empNum, month, 1, 15);
            double monthlyGross = (firstHalfHours + hours) * rate;

            double sss = computeSSS(monthlyGross);
            double ph = computePhilHealth(monthlyGross);
            double pi = computePagIBIG(monthlyGross);
            double tax = computeIncomeTax(monthlyGross - (sss + ph + pi));
            double netSalary = gross - (sss + ph + pi + tax);
            
            sb.append(String.format("Deductions: SSS: %.2f, PhilHealth: %.2f, PagIBIG: %.2f, Tax: %.2f\n", sss, ph, pi, tax));
            sb.append(String.format("NET SALARY: PHP %.2f\n", netSalary));
        } else {
            sb.append(String.format("NET SALARY: PHP %.2f\n", gross));
        }
    }

    private static double getTotalHoursForPeriod(String empNum, String month, int start, int end) {
        double total = 0;
        List<String[]> records = attendanceData.get(empNum);
        if (records == null) return 0;

        for (String[] record : records) {
            String[] dateParts = record[0].split("/"); 
            if (dateParts.length < 2) continue;
            
            int dDay = Integer.parseInt(dateParts[1]);
            if (dateParts[0].equals(month) && dDay >= start && dDay <= end) {
                total += computeWorkHours(LocalTime.parse(record[1], timeFormat), LocalTime.parse(record[2], timeFormat));
            }
        }
        return total;
    }

    private static double computeWorkHours(LocalTime login, LocalTime logout) {
        LocalTime startLimit = LocalTime.of(8, 0);
        LocalTime endLimit = LocalTime.of(17, 0);
        LocalTime graceLimit = LocalTime.of(8, 10);

        if (login.isBefore(graceLimit)) login = startLimit;
        if (logout.isAfter(endLimit)) logout = endLimit;
        if (logout.isBefore(login)) return 0;

        long mins = Duration.between(login, logout).toMinutes();
        return (mins > 60) ? (mins - 60) / 60.0 : mins / 60.0;
    }

    // --- Statutory Formula Implementations ---

    public static double computeSSS(double gross) {
        double[] limits = {0, 3250, 3750, 4250, 4750, 5250, 5750, 6250, 6750, 7250, 7750, 8250, 8750, 9250, 9750, 10250, 10750, 11250, 11750, 12250, 12750, 13250, 13750, 14250, 14750, 15250, 15750, 16250, 16750, 17250, 17750, 18250, 18750, 19250, 19750, 20250, 20750, 21250, 21750, 22250, 22750, 23250, 23750, 24250, 24750};
        double[] amounts = {135.0, 157.5, 180.0, 202.5, 225.0, 247.5, 270.0, 292.5, 315.0, 337.5, 360.0, 382.5, 405.0, 427.5, 450.0, 472.5, 495.0, 517.5, 540.0, 562.5, 585.0, 607.5, 630.0, 652.5, 675.0, 697.5, 720.0, 742.5, 765.0, 787.5, 810.0, 832.5, 855.0, 877.5, 900.0, 922.5, 945.0, 967.5, 990.0, 1012.5, 1035.0, 1057.5, 1080.0, 1102.5, 1125.0};
        
        for (int i = limits.length - 1; i >= 0; i--) {
            if (gross >= limits[i]) return amounts[i];
        }
        return 135.0;
    }

    public static double computePhilHealth(double gross) {
        if (gross <= 10000) return 150.0;
        if (gross >= 60000) return 900.0;
        return (gross * 0.03) / 2;
    }

    public static double computePagIBIG(double gross) {
        double rate = (gross <= 1500) ? 0.01 : 0.02;
        return Math.min(gross * rate, 100.0);
    }

    public static double computeIncomeTax(double taxable) {
        if (taxable <= 20832) return 0;
        if (taxable <= 33332) return (taxable - 20833) * 0.20;
        if (taxable <= 66666) return 2500 + (taxable - 33333) * 0.25;
        if (taxable <= 166666) return 10833 + (taxable - 66667) * 0.30;
        if (taxable <= 666666) return 40833.33 + (taxable - 166667) * 0.32;
        return 200833.33 + (taxable - 666667) * 0.35;
    }

    // --- CSV File Data Load Engines ---

    private static void loadEmployeeData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(empFile))) {
            reader.readLine(); // Ignore header line
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = splitCsvLine(line);
                List<String> details = new ArrayList<>();
                for (String p : parts) details.add(p.trim());
                employeeData.put(parts[0].trim(), details);
            }
        } catch (IOException e) { System.out.println("Error Loading Employees: " + e.getMessage()); }
    }

    private static void loadAttendanceData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(attendanceFile))) {
            reader.readLine(); // Ignore header line
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = splitCsvLine(line);
                String id = p[0].trim();
                if (employeeData.containsKey(id)) {
                    attendanceData.computeIfAbsent(id, k -> new ArrayList<>())
                                  .add(new String[] {p[3].trim(), p[4].trim(), p[5].trim()});
                }
            }
        } catch (IOException e) { System.out.println("Error Loading Attendance: " + e.getMessage()); }
    }

    private static String[] splitCsvLine(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    public static String[] findEmployeeRecord(String empNum) {
        List<String> data = employeeData.get(empNum);
        return (data == null) ? null : data.toArray(new String[0]);
    }

    private static String getMonthName(String month) {
        String[] n = {"", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        return n[Integer.parseInt(month)];
    }
}
