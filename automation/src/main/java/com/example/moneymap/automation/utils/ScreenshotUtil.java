package com.example.moneymap.automation.utils;

import io.appium.java_client.android.AndroidDriver;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenshotUtil {

    public static String captureScreenshot(AndroidDriver driver, String testCaseId) {
        if (driver == null) {
            return "";
        }
        
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = testCaseId + "_" + timestamp + ".png";
        
        // Define directory paths
        String relativeDir = "reports/screenshots/";
        if (new File("automation").exists()) {
            relativeDir = "automation/reports/screenshots/";
        }
        File dir = new File(relativeDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        File destFile = new File(dir, fileName);
        
        try {
            FileUtils.copyFile(srcFile, destFile);
            System.out.println("Screenshot captured: " + destFile.getAbsolutePath());
            return "screenshots/" + fileName; // Return relative path from reports/
        } catch (IOException e) {
            System.err.println("Failed to save screenshot: " + e.getMessage());
            return "";
        }
    }

    public static String captureBase64(AndroidDriver driver) {
        if (driver == null) {
            return "";
        }
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
        } catch (Exception e) {
            System.err.println("Failed to capture Base64 screenshot: " + e.getMessage());
            return "";
        }
    }
}
