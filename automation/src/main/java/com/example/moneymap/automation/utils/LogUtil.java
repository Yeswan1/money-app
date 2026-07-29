package com.example.moneymap.automation.utils;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.logging.LogEntry;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class LogUtil {

    private static final String LOG_DIR = "automation/reports/logs/";

    public static void log(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        String formatted = String.format("[%s] [INFO] %s", timestamp, message);
        System.out.println(formatted);
        writeToLogFile("execution.log", formatted);
    }

    public static void logError(String message, Throwable t) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        String formatted = String.format("[%s] [ERROR] %s - %s", timestamp, message, t != null ? t.getMessage() : "No trace");
        System.err.println(formatted);
        writeToLogFile("execution.log", formatted);
        if (t != null) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(new File(LOG_DIR, "execution.log"), true))) {
                t.printStackTrace(pw);
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    public static String captureDeviceLogs(AndroidDriver driver, String testCaseId) {
        if (driver == null) {
            return "";
        }
        
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = testCaseId + "_device_" + timestamp + ".log";
        File dir = new File(LOG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File logFile = new File(dir, fileName);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile))) {
            List<LogEntry> logEntries = driver.manage().logs().get("logcat").getAll();
            for (LogEntry entry : logEntries) {
                writer.println(new Date(entry.getTimestamp()) + " - " + entry.getLevel() + " - " + entry.getMessage());
            }
            System.out.println("Device logs captured: " + logFile.getAbsolutePath());
            return "logs/" + fileName; // Return relative path from reports/
        } catch (Exception e) {
            System.err.println("Failed to capture device logs: " + e.getMessage());
            return "";
        }
    }

    private static synchronized void writeToLogFile(String fileName, String message) {
        File dir = new File(LOG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(new File(dir, fileName), true))) {
            writer.println(message);
        } catch (Exception e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }
}
