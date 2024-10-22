package org.example.common;

import org.example.util.MessageKeys;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileProcessor<T> {
    public List<String[]> readFile(String filePath) {
        List<String[]> data = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] values = line.split(MessageKeys.CHARACTER);
                    data.add(values);
                }
            }
        } catch (FileNotFoundException e) {
            writeErrorLog(MessageKeys.FILE_ERROR, "File not found: " + filePath);
            System.exit(1);
        } catch (IOException e) {
            writeErrorLog(MessageKeys.FILE_ERROR, "IOException while reading file: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            writeErrorLog(MessageKeys.FILE_ERROR, "Unexpected error: " + e.getMessage());
            System.exit(1);
        }
        return data;
    }


    public void writeFile(String filePath, List<T> objects, CSVFormatter<T> formatter, String header) {
        File file = new File(filePath);
        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new IOException("Failed to create new file: " + filePath);
                }
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                if (file.length() == 0) {
                    bw.write(header);
                    bw.newLine();
                }
                for (T obj : objects) {
                    bw.write(formatter.format(obj));
                    bw.newLine();
                }
            }
        } catch (SecurityException e) {
            writeErrorLog(MessageKeys.FILE_ERROR, "SecurityException while writing file: " + filePath);
            System.exit(1);
        } catch (IOException e) {
            writeErrorLog(MessageKeys.FILE_ERROR, "IOException while writing file: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            writeErrorLog(MessageKeys.FILE_ERROR, "Unexpected error: " + e.getMessage());
            System.exit(1);
        }
    }


    public void writeErrorLog(String errorLogPath, String message) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(errorLogPath, true))) {
            bw.write(message);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Failed to write error log: " + e.getMessage());
        }
    }
}
