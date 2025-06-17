/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.systemMaintenance;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author ongoj
 */
public class backupDaily {

    private static final String SOURCE_DIR = "src/com/csv/";
    private static final String BACKUP_DIR = "src/com/backup/Daily/";
    private static final String[] FILES_TO_BACKUP = {
        "EmployeeData.csv", "LoginCredentials.csv", "DTR.csv"
    };

    public static void runBackup() {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd").format(new Date());

            for (String fileName : FILES_TO_BACKUP) {
                Path sourcePath = Paths.get(SOURCE_DIR + fileName);
                Path backupPath = Paths.get(BACKUP_DIR + fileName.replace(".csv", "_" + timestamp + ".csv"));

                Files.copy(sourcePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            }

            cleanupOldBackups();

            System.out.println("Daily backup completed on: " + timestamp);
        } catch (IOException e) {
            System.err.println("Error during daily backup: " + e.getMessage());
        }
    }

    private static void cleanupOldBackups() throws IOException {
        for (String fileName : FILES_TO_BACKUP) {
            File backupFolder = new File(BACKUP_DIR);
            File[] matchingFiles = backupFolder.listFiles((dir, name) -> name.startsWith(fileName.replace(".csv", "_")));

            if (matchingFiles != null && matchingFiles.length > 3) {
                Arrays.sort(matchingFiles, Comparator.comparingLong(File::lastModified).reversed());

                for (int i = 3; i < matchingFiles.length; i++) {
                    matchingFiles[i].delete();
                }
            }
        }
    }
}

