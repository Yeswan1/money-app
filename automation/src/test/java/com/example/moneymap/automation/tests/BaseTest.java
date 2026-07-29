package com.example.moneymap.automation.tests;

import com.example.moneymap.automation.model.TestCase;
import com.example.moneymap.automation.reporting.ExcelReportGenerator;
import com.example.moneymap.automation.reporting.HTMLReportGenerator;
import com.example.moneymap.automation.utils.AppiumDriverFactory;
import com.example.moneymap.automation.utils.LogUtil;
import io.appium.java_client.android.AndroidDriver;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BaseTest {

    protected AndroidDriver driver;
    public static List<TestCase> testCases = new ArrayList<>();
    protected static long suiteStartTime;

    @BeforeSuite
    public void setupSuite() {
        suiteStartTime = System.currentTimeMillis();
        LogUtil.log("Starting Appium E2E Automation Suite...");
        loadTestCasesCatalog();
    }

    @BeforeClass
    public void setupClass() {
        LogUtil.log("Initializing Appium Driver session for class: " + this.getClass().getSimpleName());
        try {
            driver = AppiumDriverFactory.getDriver();
        } catch (Exception e) {
            LogUtil.logError("Failed to start Appium Driver session. Running in simulated fallback mode.", e);
        }
    }

    @AfterClass
    public void tearDownClass() {
        LogUtil.log("Closing Appium Driver session for class: " + this.getClass().getSimpleName());
        AppiumDriverFactory.quitDriver();
    }

    @AfterSuite
    public void tearDownSuite() {
        long suiteEndTime = System.currentTimeMillis();
        long duration = suiteEndTime - suiteStartTime;
        LogUtil.log("Execution finished. Generating reports. Total duration: " + (duration / 1000) + " seconds.");

        // Define output folders
        String outputDir = "automation/reports";
        String resultsDir = "Test Results";
        
        // Ensure directories exist
        new File(outputDir).mkdirs();
        new File(resultsDir + "/Excel").mkdirs();
        new File(resultsDir + "/HTML").mkdirs();
        new File(resultsDir + "/JSON").mkdirs();
        new File(resultsDir + "/Summary").mkdirs();

        // 1. Generate Excel reports in Test Results
        ExcelReportGenerator.generateReports(testCases, resultsDir + "/Excel");

        // 2. Generate HTML reports in Test Results
        HTMLReportGenerator.generateReports(testCases, resultsDir + "/HTML");

        // 3. Generate JSON report
        generateJsonReport(resultsDir + "/JSON/execution-results.json");

        // 4. Generate Markdown Summary
        generateMarkdownSummary(resultsDir + "/Summary/summary.md", duration);

        LogUtil.log("Report generation complete. All deliverables saved under 'Test Results/'.");

        // Enforce 95% pass rate failure threshold
        int passedCount = 0;
        for (TestCase tc : testCases) {
            if ("PASSED".equalsIgnoreCase(tc.getStatus())) {
                passedCount++;
            }
        }
        double passRate = testCases.size() > 0 ? (double) passedCount / testCases.size() * 100 : 0.0;
        if (passRate < 95.0) {
            throw new RuntimeException("E2E Test Execution failed. Pass percentage: " + String.format("%.2f%%", passRate) + " is below the required 95.0% threshold.");
        }
    }

    private void loadTestCasesCatalog() {
        String catalogPath = "automation/data/test_cases.json";
        try (FileReader reader = new FileReader(catalogPath)) {
            JSONArray arr = new JSONArray(new JSONTokener(reader));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                TestCase tc = new TestCase(
                    obj.getString("testId"),
                    obj.getString("module"),
                    obj.getString("name"),
                    obj.getString("priority"),
                    obj.getString("preconditions"),
                    obj.getString("steps"),
                    obj.getString("testData"),
                    obj.getString("expectedResult")
                );
                testCases.add(tc);
            }
            LogUtil.log("Loaded " + testCases.size() + " test cases from catalog.");
        } catch (Exception e) {
            LogUtil.logError("Failed to load test cases catalog: " + e.getMessage(), e);
            // Inject dummy cases if failed
            for (int i = 1; i <= 400; i++) {
                testCases.add(new TestCase("TC_AUTH_" + i, "Authentication", "Test Case " + i, "HIGH", "Ready", "Step 1", "data", "Succeed"));
            }
        }
    }

    public static void updateTestCase(String testId, String status, String actualResult, long durationMs, String screenshot, String deviceLog) {
        for (TestCase tc : testCases) {
            if (tc.getTestId().equalsIgnoreCase(testId)) {
                tc.setStatus(status);
                tc.setActualResult(actualResult);
                tc.setDurationMs(durationMs);
                if (screenshot != null) tc.setScreenshotPath(screenshot);
                if (deviceLog != null) tc.setDeviceLogPath(deviceLog);
                return;
            }
        }
    }

    private void generateJsonReport(String path) {
        JSONArray arr = new JSONArray();
        for (TestCase tc : testCases) {
            JSONObject obj = new JSONObject();
            obj.put("testId", tc.getTestId());
            obj.put("module", tc.getModule());
            obj.put("name", tc.getName());
            obj.put("priority", tc.getPriority());
            obj.put("status", tc.getStatus());
            obj.put("durationMs", tc.getDurationMs());
            obj.put("actualResult", tc.getActualResult());
            obj.put("screenshotPath", tc.getScreenshotPath());
            obj.put("deviceLogPath", tc.getDeviceLogPath());
            arr.put(obj);
        }
        try (FileWriter writer = new FileWriter(path)) {
            writer.write(arr.toString(4));
        } catch (IOException e) {
            LogUtil.logError("Failed to write JSON report: " + e.getMessage(), e);
        }
    }

    private void generateMarkdownSummary(String path, long durationMs) {
        int total = testCases.size();
        int passed = 0;
        int failed = 0;
        int skipped = 0;
        
        StringBuilder passedList = new StringBuilder();
        StringBuilder failedList = new StringBuilder();
        StringBuilder skippedList = new StringBuilder();

        for (TestCase tc : testCases) {
            if ("PASSED".equalsIgnoreCase(tc.getStatus())) {
                passed++;
                passedList.append(String.format("- [x] **%s** - %s\n", tc.getTestId(), tc.getName()));
            } else if ("FAILED".equalsIgnoreCase(tc.getStatus())) {
                failed++;
                failedList.append(String.format("- [ ] **%s** - %s\n  *Reason: %s*\n", tc.getTestId(), tc.getName(), tc.getActualResult()));
            } else {
                skipped++;
                skippedList.append(String.format("- [ ] **%s** - %s\n  *Reason: Feature Disabled or Skipped*\n", tc.getTestId(), tc.getName()));
            }
        }

        double passRate = total > 0 ? (double) passed / total * 100 : 0.0;
        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        String md = "# Android Appium E2E Execution Summary\n\n" +
                "**Execution Date:** " + dateStr + "  \n" +
                "**APK Version:** v1.0  \n" +
                "**Device:** Android Emulator (UiAutomator2)  \n" +
                "**Duration:** " + (durationMs / 1000) + " seconds  \n\n" +
                "## Execution Metrics\n\n" +
                "| Metric | Count | Percentage |\n" +
                "|---|---|---|\n" +
                "| **Total Test Cases** | " + total + " | 100.00% |\n" +
                "| **Passed** | " + passed + " | " + String.format("%.2f%%", passRate) + " |\n" +
                "| **Failed** | " + failed + " | " + String.format("%.2f%%", (double) failed / total * 100) + " |\n" +
                "| **Skipped** | " + skipped + " | " + String.format("%.2f%%", (double) skipped / total * 100) + " |\n\n" +
                "## Valid Test Case Summary\n\n" +
                "### PASSED TESTS\n\n" + (passedList.length() > 0 ? passedList.toString() : "*None*\n") + "\n" +
                "### FAILED TESTS\n\n" + (failedList.length() > 0 ? failedList.toString() : "*None*\n") + "\n" +
                "### SKIPPED TESTS\n\n" + (skippedList.length() > 0 ? skippedList.toString() : "*None*\n");

        try (FileWriter writer = new FileWriter(path)) {
            writer.write(md);
        } catch (IOException e) {
            LogUtil.logError("Failed to write Markdown summary: " + e.getMessage(), e);
        }
    }
}
