/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author AtlasPrimE
 */
package com.gui.Payroll;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class DtrCsvUtil {

    public static final String DTR_DIR = "src/com/csv/DTR/";
    public static final String PAYDATA_DIR = "src/com/csv/PayData/";

    private static final DateTimeFormatter CSV_DATE_FMT
            = DateTimeFormatter.ofPattern("M/d/yyyy", Locale.ENGLISH);

    private static final DateTimeFormatter PAY_PERIOD_FMT
            = DateTimeFormatter.ofPattern("yyyy.MMM.dd", Locale.ENGLISH);

    // no need to expose the full header—just guard for at least 2 columns
    // public static final String[] HEADER = { … };
    public static List<String[]> readAll(String empId) throws IOException {
        File f = new File(DTR_DIR + empId + ".csv");

        List<String[]> all = new ArrayList<>();
        if (f.exists()) {
            try (CSVReader r = new CSVReader(new FileReader(f))) {
                String[] row;
                while ((row = r.readNext()) != null) {
                    all.add(row);
                }
            } catch (CsvValidationException ex) {
                Logger.getLogger(DtrCsvUtil.class.getName())
                        .log(Level.SEVERE, "Corrupt CSV: " + f, ex);
            }
        }

        /* Guarantee at least a header row so callers never get NPEs on
           empty files. */
        if (all.isEmpty()) {
            all.add(new String[]{"EmpID", "Date"});
        }
        return all;
    }

    public static void writeAll(String empId, List<String[]> rows) throws IOException {
        try (CSVWriter w = new CSVWriter(new FileWriter(DTR_DIR + empId + ".csv"))) {
            for (String[] row : rows) {
                w.writeNext(row);
            }
        }
    }

    public static void updateCell(String empId,
            String date,
            int csvCol,
            String newValue) throws IOException {

        List<String[]> all = readAll(empId);
        boolean found = false;

        for (int i = 1; i < all.size(); i++) {          // skip header
            String[] row = all.get(i);
            if (row.length > 1 && date.equals(row[1])) {
                if (row.length <= csvCol) {
                    row = Arrays.copyOf(row, csvCol + 1);
                }
                row[csvCol] = newValue;
                all.set(i, row);
                found = true;
                break;
            }
        }

        /* Append brand-new line if date was not present */
        if (!found) {
            String[] nr = new String[Math.max(2, csvCol + 1)];
            nr[0] = empId;
            nr[1] = date;
            nr[csvCol] = newValue;
            all.add(nr);
        }

        writeAll(empId, all);
    }

    public static Set<String> discoverPayPeriods() throws IOException {

        Comparator<String> oldestFirst = (a, b) -> {
            try {
                return LocalDate.parse(a, PAY_PERIOD_FMT)
                        .compareTo(LocalDate.parse(b, PAY_PERIOD_FMT));
            } catch (DateTimeParseException ex) {
                return a.compareTo(b);      // plain text sort (descending)
            }
        };

        Set<String> periods = new TreeSet<>(oldestFirst);
        Path dir = Paths.get(DTR_DIR);

        if (!Files.isDirectory(dir)) {
            Logger.getLogger(DtrCsvUtil.class.getName())
                    .warning("DTR directory not found: " + dir.toAbsolutePath());
            return periods;                 // empty set
        }

        try (Stream<Path> files = Files.list(dir)) {
            files.filter(p -> p.getFileName().toString().matches("\\d+\\.csv"))
                    .forEach(p -> processCsvForPeriods(p, periods));
        }
        return periods;                     // may be empty – let GUI decide
    }

    /* Helper that scans one CSV file and adds its pay periods */
    private static void processCsvForPeriods(Path csv, Set<String> periods) {
        try (CSVReader r = new CSVReader(new FileReader(csv.toFile()))) {
            r.readNext();                   // skip header
            String[] rec;
            while ((rec = r.readNext()) != null) {
                if (rec.length < 2) {
                    continue;
                }

                String raw = rec[1].trim();
                if (raw.isEmpty()) {
                    continue;
                }

                try {
                    LocalDate d = LocalDate.parse(raw, CSV_DATE_FMT);
                    periods.add(toPayPeriod(d));
                } catch (DateTimeParseException ignore) {
                    /* malformed line – silently ignore */
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(DtrCsvUtil.class.getName())
                    .log(Level.WARNING, "Cannot read " + csv, ex);
        }
    }

    /**
     * Converts a *single* date to a pay-period label (either “1-15 …” or
     * “16-EOM …”).
     */
    private static String toPayPeriod(LocalDate d) {
        LocalDate first = (d.getDayOfMonth() <= 15)
                ? d.withDayOfMonth(1)
                : d.withDayOfMonth(16);
        return first.format(PAY_PERIOD_FMT);
    }
}
