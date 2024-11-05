package org.mock.common;

import org.mock.util.MessageKeys;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileProcessor<T> {
    private final String folderPath;

    public FileProcessor(String folderPath) {
        this.folderPath = folderPath;
    }
    public List<String[]> readFile(String fileName) {
        List<String[]> data = new ArrayList<>();
        String filePath = Paths.get(folderPath, fileName).toString();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8)))  {
            String line;
            while ((line = reader.readLine()) != null) {
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


    public void writeFile(String fileName, List<T> objects, CSVFormatter<T> formatter, String header) {
        String filePath = Paths.get(folderPath, fileName).toString();
        File file = new File(filePath);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            if (file.length() == 0) {
                writer.write(header);
                writer.newLine();
            }
            for (T obj : objects) {
                writer.write(formatter.format(obj));
                writer.newLine();
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
        String logFilePath = Paths.get(folderPath, errorLogPath).toString();
        File logFile = new File(logFilePath);


        File parentDir = logFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true), StandardCharsets.UTF_8))) {
            bw.write(message);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Failed to write error log: " + e.getMessage());
        }
    }
}
