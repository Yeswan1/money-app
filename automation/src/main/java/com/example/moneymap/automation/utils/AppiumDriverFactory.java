package com.example.moneymap.automation.utils;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileReader;
import java.net.URI;
import java.net.URL;
import java.time.Duration;

public class AppiumDriverFactory {

    private static AndroidDriver driver;

    public static AndroidDriver getDriver() {
        if (driver == null) {
            try {
                // Load configuration
                String configPath = "automation/config/appium-config.json";
                if (!new java.io.File(configPath).exists()) {
                    configPath = "config/appium-config.json";
                }
                JSONObject config = new JSONObject(new JSONTokener(new FileReader(configPath)));

                UiAutomator2Options options = new UiAutomator2Options();
                options.setPlatformName(config.optString("platformName", "Android"));
                options.setAutomationName(config.optString("automationName", "UiAutomator2"));
                options.setDeviceName(config.optString("deviceName", "Android Emulator"));
                
                // Set path to APK dynamically (fallback to relative if absolute path doesn't exist)
                String appPath = config.optString("app");
                java.io.File appFile = new java.io.File(appPath);
                if (!appFile.exists()) {
                    java.io.File relativePath1 = new java.io.File("../app/build/outputs/apk/debug/app-debug.apk");
                    java.io.File relativePath2 = new java.io.File("app/build/outputs/apk/debug/app-debug.apk");
                    if (relativePath1.exists()) {
                        appPath = relativePath1.getAbsolutePath();
                    } else if (relativePath2.exists()) {
                        appPath = relativePath2.getAbsolutePath();
                    }
                }
                options.setApp(appPath);
                
                options.setAppPackage(config.optString("appPackage", "com.example.moneymap"));
                options.setAppActivity(config.optString("appActivity", "com.example.moneymap.MainActivity"));
                options.setNoReset(config.optBoolean("noReset", false));
                options.setFullReset(config.optBoolean("fullReset", false));
                options.setCapability("autoGrantPermissions", config.optBoolean("autoGrantPermissions", true));
                options.setCapability("newCommandTimeout", config.optInt("newCommandTimeout", 300));
                options.setCapability("systemPort", config.optInt("systemPort", 8200));
                options.setAdbExecTimeout(Duration.ofMillis(config.optInt("adbExecTimeout", 120000)));

                URL serverUrl = new URI("http://127.0.0.1:4723").toURL();
                driver = new AndroidDriver(serverUrl, options);
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            } catch (Exception e) {
                System.err.println("Failed to initialize AndroidDriver: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return driver;
    }

    public static void quitDriver() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                System.err.println("Error quitting driver: " + e.getMessage());
            } finally {
                driver = null;
            }
        }
    }
}
