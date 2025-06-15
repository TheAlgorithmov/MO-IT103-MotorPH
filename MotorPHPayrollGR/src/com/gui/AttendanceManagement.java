package com.gui;

import com.opencsv.CSVWriter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class AttendanceManagement extends JFrame {

    private User currentUser;
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> employeeSelector;
    private JDateChooser fromDatePicker, toDatePicker;
    private JButton btnFilter;

    public AttendanceManagement(User user) {
        this.currentUser = user;
        setTitle("Attendance Management - " + user.getuPosition());
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initComponents();

        if (isSupervisor()) {
            loadEmployeeList();
        } else {
            loadAttendanceData(currentUser.getuEmpId());
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Header with image on left and title center
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel logo = new JLabel();
        ImageIcon logoIcon = new ImageIcon("src/com/gui/images/LoginIcons/motorph.jpg");
        logo.setIcon(new ImageIcon(logoIcon.getImage().getScaledInstance(150, 120, Image.SCALE_SMOOTH)));
        logo.setHorizontalAlignment(JLabel.CENTER);
        logo.setPreferredSize(new Dimension(150, 120));
        headerPanel.add(logo, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("Attendance Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Filter panel in center below title
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        if (isSupervisor()) {
            employeeSelector = new JComboBox<>();
            fromDatePicker = new JDateChooser();
            toDatePicker = new JDateChooser();
            btnFilter = new JButton("View Attendance");

            btnFilter.addActionListener(e -> {
                String selectedEmpId = (String) employeeSelector.getSelectedItem();
                loadAttendanceData(selectedEmpId);
            });

            filterPanel.add(new JLabel("Employee ID:"));
            filterPanel.add(employeeSelector);
            filterPanel.add(new JLabel("From:"));
            filterPanel.add(fromDatePicker);
            filterPanel.add(new JLabel("To:"));
            filterPanel.add(toDatePicker);
            filterPanel.add(btnFilter);

            add(filterPanel, BorderLayout.BEFORE_FIRST_LINE);
        }

        String[] columns = {"Date", "Clock In", "Clock Out", "Duration", "Late", "Overtime", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Controls on right side
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(0, 1, 5, 5));

        JButton btnApplyLeave = new JButton("Apply for Leave");
        JButton btnApproveLeave = new JButton("Approve Leave Requests");
        JButton btnManualEntry = new JButton("Manual Time Entry");
        JButton btnExport = new JButton("Export Attendance");
        JButton btnValidateTimesheet = new JButton("Validate Timesheet");
        JButton btnViewChangeLogs = new JButton("View Change Logs");
        JButton btnClose = new JButton("Close");

        btnApplyLeave.addActionListener(e -> applyLeave());
        btnApproveLeave.addActionListener(e -> approveLeave());
        btnManualEntry.addActionListener(e -> openManualDTRDialog());
        btnExport.addActionListener(e -> exportAttendance());
        btnValidateTimesheet.addActionListener(e -> validateTimesheet());
        btnViewChangeLogs.addActionListener(e -> viewChangeLogs());
        btnClose.addActionListener(e -> dispose());

        buttonPanel.add(btnApplyLeave);
        if (isSupervisor()) {
            buttonPanel.add(btnApproveLeave);
            buttonPanel.add(btnManualEntry);
            buttonPanel.add(btnValidateTimesheet);
        }
        buttonPanel.add(btnExport);
        buttonPanel.add(btnViewChangeLogs);
        buttonPanel.add(btnClose);

        add(buttonPanel, BorderLayout.EAST);
    }

    private boolean isSupervisor() {
        String position = currentUser.getuPosition().toLowerCase();
        return position.contains("manager") || position.contains("team leader") ||
               position.contains("head") || position.contains("chief") ||
               position.contains("officer") || position.contains("operations and systems");
    }

    private void loadEmployeeList() {
        File folder = new File("src/com/csv/DTR");
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".csv") && !name.equals("DTRChangeLogs.csv"));

        if (files != null) {
            List<String> empIds = Arrays.stream(files)
                    .map(f -> f.getName().replace(".csv", ""))
                    .sorted()
                    .collect(Collectors.toList());

            for (String id : empIds) {
                employeeSelector.addItem(id);
            }

            if (!empIds.isEmpty()) {
                employeeSelector.setSelectedIndex(0);
                loadAttendanceData(empIds.get(0));
            }
        }
    }

    private void loadAttendanceData(String empId) {
            String filePath = "src/com/csv/DTR/" + empId + ".csv";
            tableModel.setRowCount(0);
            DecimalFormat df = new DecimalFormat("0.00");

            Map<String, String[]> attendanceMap = new HashMap<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                boolean isFirstLine = true;
                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) { isFirstLine = false; continue; }
                    String[] parts = line.split(",");
                    if (parts.length >= 5) {
                        attendanceMap.put(parts[1].trim(), parts);
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Unable to read file: " + e.getMessage());
                return;
            }

            LocalDate startDate = fromDatePicker != null && fromDatePicker.getDate() != null ?
                    fromDatePicker.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : LocalDate.now();
            LocalDate endDate = toDatePicker != null && toDatePicker.getDate() != null ?
                    toDatePicker.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : LocalDate.now();

            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                String dateStr = currentDate.format(DateTimeFormatter.ofPattern("M/d/yyyy"));
                DayOfWeek day = currentDate.getDayOfWeek();
                boolean isWeekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
                boolean isFutureDate = currentDate.isAfter(LocalDate.now());

                if (attendanceMap.containsKey(dateStr)) {
                    String[] parts = attendanceMap.get(dateStr);
                    String clockIn = parts[2].trim();
                    String clockOut = parts[3].trim();
                    String duration = parts[4].trim();
                    String late = "-";
                    String ot = "-";
                    String status = "Present";

                    try {
                        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
                        Date inTime = timeFormat.parse(clockIn);
                        Date outTime = timeFormat.parse(clockOut);
                        long durationMin = (outTime.getTime() - inTime.getTime()) / (60 * 1000);
                        if (durationMin < 0) durationMin += 24 * 60;

                        if (durationMin < 480) {
                            status = "Undertime";
                        } else if (durationMin > 480) {
                            ot = df.format((durationMin - 480) / 60.0) + " hrs";
                        }

                        Date lateThreshold = timeFormat.parse("8:45 AM");
                        if (inTime.after(lateThreshold)) {
                            status = "Late";
                            long lateMin = (inTime.getTime() - lateThreshold.getTime()) / (60 * 1000);
                            late = lateMin + " mins";
                        }
                    } catch (Exception ex) {
                        status = "Error";
                    }

                    tableModel.addRow(new Object[]{dateStr, clockIn, clockOut, duration, late, ot, status});

                } else if (!isWeekend && !isFutureDate) {
                    tableModel.addRow(new Object[]{dateStr, "-", "-", "-", "-", "-", "Absent"});
                }

                currentDate = currentDate.plusDays(1);
            }
        }

    private void applyLeave() {}

    private void approveLeave() {}

        private void openManualDTRDialog() {
        JDialog dialog = new JDialog((Frame) null, "Manual DTR Entry", true);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));

        JDateChooser dateChooser = new JDateChooser();
        SpinnerDateModel timeModelIn = new SpinnerDateModel();
        JSpinner spinnerIn = new JSpinner(timeModelIn);
        spinnerIn.setEditor(new JSpinner.DateEditor(spinnerIn, "h:mm a"));

        SpinnerDateModel timeModelOut = new SpinnerDateModel();
        JSpinner spinnerOut = new JSpinner(timeModelOut);
        spinnerOut.setEditor(new JSpinner.DateEditor(spinnerOut, "h:mm a"));

        dialog.add(new JLabel("Date:"));
        dialog.add(dateChooser);
        dialog.add(new JLabel("Clock In:"));
        dialog.add(spinnerIn);
        dialog.add(new JLabel("Clock Out:"));
        dialog.add(spinnerOut);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        dialog.add(saveButton);
        dialog.add(cancelButton);

        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(null);

        saveButton.addActionListener(e -> {
            String date = new SimpleDateFormat("M/d/yyyy").format(dateChooser.getDate());
            String timeIn = new SimpleDateFormat("h:mm a").format((Date) spinnerIn.getValue());
            String timeOut = new SimpleDateFormat("h:mm a").format((Date) spinnerOut.getValue());
            String empID = (String) employeeSelector.getSelectedItem();

            boolean found = false;

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String rowDate = tableModel.getValueAt(i, 0).toString().trim();
                if (rowDate.equals(date)) {
                    tableModel.setValueAt(timeIn, i, 1);
                    tableModel.setValueAt(timeOut, i, 2);
                    tableModel.setValueAt("Manual", i, 3);
                    found = true;
                    break;
                }
            }

            if (!found) {
                tableModel.addRow(new Object[]{date, timeIn, timeOut, "Manual", "", "", "Pending"});
            }

            writeTableToCSV();
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }
    private void validateTimesheet() {
        if (!isSupervisor()) return;

        String empId = (String) employeeSelector.getSelectedItem();
        Date fromDate = fromDatePicker.getDate();
        Date toDate = toDatePicker.getDate();

        if (fromDate == null || toDate == null) {
            JOptionPane.showMessageDialog(this, "Please select a valid date range.");
            return;
        }

        String filePath = "src/com/csv/DTR/" + empId + ".csv";
        long totalMinutes = 0;
        int entryCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;
            SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; }
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    Date entryDate = dateFormat.parse(parts[1].trim());
                    if (!entryDate.before(fromDate) && !entryDate.after(toDate)) {
                        Date in = timeFormat.parse(parts[2].trim());
                        Date out = timeFormat.parse(parts[3].trim());
                        long mins = (out.getTime() - in.getTime()) / (60 * 1000);
                        if (mins < 0) mins += 24 * 60;
                        totalMinutes += mins;
                        entryCount++;
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error validating timesheet: " + e.getMessage());
            return;
        }

        long hours = totalMinutes / 60;
        long mins = totalMinutes % 60;
        String status = (entryCount == 0) ? "Pending" : (hours < 80 ? "Flagged" : "Approved");

        String result = String.format("Employee: %s\nEntries: %d\nTotal Work Hours: %dh %dm\nStatus: %s",
                empId, entryCount, hours, mins, status);
        JOptionPane.showMessageDialog(this, result, "Timesheet Validation", JOptionPane.INFORMATION_MESSAGE);
    }

    private void writeTableToCSV() {
        try (CSVWriter writer = new CSVWriter(new FileWriter("src/com/csv/DTRManualEntry.csv"))) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String[] row = new String[tableModel.getColumnCount()];
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    row[j] = tableModel.getValueAt(i, j).toString();
                }
                writer.writeNext(row);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving manual entry to CSV.");
        }
    }

    private void viewChangeLogs() {
        String path = "src/com/csv/DTR/DTRChangeLogs.csv";
        if (!new File(path).exists()) {
            JOptionPane.showMessageDialog(this, "No change logs available.");
            return;
        }

        JTextArea textArea = new JTextArea();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                textArea.append(line + "\n");
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error loading change logs: " + ex.getMessage());
            return;
        }

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(700, 400));
        JOptionPane.showMessageDialog(this, scrollPane, "DTR Change Logs", JOptionPane.INFORMATION_MESSAGE);
    }

    private void logDTRChange(String empId, String date, String in, String out, String duration) {
        String path = "src/com/csv/DTR/DTRChangeLogs.csv";
        String line = currentUser.getuEmpId() + "," + currentUser.getuFirstName() + " " + currentUser.getuLastName() + "," + date + ",Manual Entry -> " + in + " to " + out + " (" + duration + ")";
        try (PrintWriter outFile = new PrintWriter(new FileWriter(path, true))) {
            if (new File(path).length() == 0) {
                outFile.println("Supervisor,EmpID,Name,Date,Changes");
            }
            outFile.println(line);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to log change: " + ex.getMessage());
        }
    }

    private void exportAttendance() {
    
        String empId = isSupervisor() ? (String) employeeSelector.getSelectedItem() : currentUser.getuEmpId();
        String filePath = "src/com/csv/DTR/" + empId + ".csv";

        Date fromDate = fromDatePicker != null ? fromDatePicker.getDate() : new Date();
        Date toDate = toDatePicker != null ? toDatePicker.getDate() : new Date();
        fromDatePicker.setPreferredSize(new Dimension(120, 25));
        toDatePicker.setPreferredSize(new Dimension(120, 25));

        SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy.MM.dd");
        String fileName = empId + "_" + fileDateFormat.format(fromDate) + "_" + fileDateFormat.format(toDate) + ".csv";

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Folder to Save Report");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int chooserResult = fileChooser.showSaveDialog(this);
        if (chooserResult == JFileChooser.APPROVE_OPTION) {
            File selectedDir = fileChooser.getSelectedFile();
            File exportFile = new File(selectedDir, fileName);

            try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
                 PrintWriter writer = new PrintWriter(new FileWriter(exportFile))) {

                writer.println("Date,Clock In,Clock Out,Duration");
                String line;
                boolean isFirstLine = true;
                SimpleDateFormat dateParser = new SimpleDateFormat("M/d/yyyy");

                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) { isFirstLine = false; continue; }
                    String[] parts = line.split(",");
                    if (parts.length >= 5) {
                        Date entryDate = dateParser.parse(parts[1].trim());
                        if (!entryDate.before(fromDate) && !entryDate.after(toDate)) {
                            writer.println(parts[1].trim() + "," + parts[2].trim() + "," + parts[3].trim() + "," + parts[4].trim());
                        }
                    }
                }

                JOptionPane.showMessageDialog(this, "Exported to: " + exportFile.getAbsolutePath());

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage());
            }
        }
    }
}
