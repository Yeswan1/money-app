package com.example.moneymap.automation.listeners;

import com.example.moneymap.automation.model.TestCase;
import com.example.moneymap.automation.tests.BaseTest;
import com.example.moneymap.automation.utils.AppiumDriverFactory;
import com.example.moneymap.automation.utils.LogUtil;
import com.example.moneymap.automation.utils.ScreenshotUtil;
import io.appium.java_client.android.AndroidDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.PrintWriter;
import java.io.StringWriter;

public class TestNGListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        TestCase tc = getTestCaseParam(result);
        if (tc != null) {
            LogUtil.log("Starting test execution: " + tc.getTestId() + " - " + tc.getName());
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        TestCase tc = getTestCaseParam(result);
        long duration = result.getEndMillis() - result.getStartMillis();
        if (tc != null) {
            BaseTest.updateTestCase(tc.getTestId(), "PASSED", "Executed successfully.", duration, "", "");
            LogUtil.log("Test PASSED: " + tc.getTestId());
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        TestCase tc = getTestCaseParam(result);
        long duration = result.getEndMillis() - result.getStartMillis();
        
        Throwable throwable = result.getThrowable();
        StringWriter sw = new StringWriter();
        if (throwable != null) {
            throwable.printStackTrace(new PrintWriter(sw));
        }
        String stackTrace = sw.toString();
        String errorMessage = throwable != null ? throwable.getMessage() : "Unknown Appium error";

        String screenshotPath = "";
        String deviceLogPath = "";
        
        try {
            AndroidDriver driver = AppiumDriverFactory.getDriver();
            if (driver != null) {
                String testId = tc != null ? tc.getTestId() : "UnknownTC";
                screenshotPath = ScreenshotUtil.captureScreenshot(driver, testId);
                deviceLogPath = LogUtil.captureDeviceLogs(driver, testId);
            }
        } catch (Exception e) {
            System.err.println("Listener failed to capture screenshot/logs: " + e.getMessage());
        }

        if (tc != null) {
            BaseTest.updateTestCase(tc.getTestId(), "FAILED", errorMessage + "\n\n" + stackTrace, duration, screenshotPath, deviceLogPath);
            LogUtil.log("Test FAILED: " + tc.getTestId() + " Reason: " + errorMessage);
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        TestCase tc = getTestCaseParam(result);
        long duration = result.getEndMillis() - result.getStartMillis();
        if (tc != null) {
            BaseTest.updateTestCase(tc.getTestId(), "SKIPPED", "Test skipped by TestNG runner.", duration, "", "");
            LogUtil.log("Test SKIPPED: " + tc.getTestId());
        }
    }

    private TestCase getTestCaseParam(ITestResult result) {
        Object[] params = result.getParameters();
        if (params != null && params.length > 0 && params[0] instanceof TestCase) {
            return (TestCase) params[0];
        }
        return null;
    }

    @Override
    public void onStart(ITestContext context) {}

    @Override
    public void onFinish(ITestContext context) {}
}
