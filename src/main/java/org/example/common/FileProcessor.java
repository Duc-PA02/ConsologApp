package org.example.common;

import org.example.util.MessageKeys;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileProcessor<T> {
    public List<String[]> readFile(String filePath) throws IOException {
        List<String[]> data = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] values = line.split(MessageKeys.CHARACTER);
                    data.add(values);
                }
            }
        }
        return data;
    }

    public void writeFile(String filePath, List<T> objects, CSVFormatter<T> formatter, String header) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
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
