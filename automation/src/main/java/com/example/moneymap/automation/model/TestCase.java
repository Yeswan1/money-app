package com.example.moneymap.automation.model;

public class TestCase {
    private String testId;
    private String module;
    private String name;
    private String priority;
    private String preconditions;
    private String steps;
    private String testData;
    private String expectedResult;
    private String actualResult;
    private String status; // PASSED, FAILED, SKIPPED, UNEXECUTED
    private long durationMs;
    private String screenshotPath = "";
    private String deviceLogPath = "";

    public TestCase() {}

    public TestCase(String testId, String module, String name, String priority, String preconditions, String steps, String testData, String expectedResult) {
        this.testId = testId;
        this.module = module;
        this.name = name;
        this.priority = priority;
        this.preconditions = preconditions;
        this.steps = steps;
        this.testData = testData;
        this.expectedResult = expectedResult;
        this.status = "UNEXECUTED";
    }

    // Getters and Setters
    public String getTestId() { return testId; }
    public void setTestId(String testId) { this.testId = testId; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getPreconditions() { return preconditions; }
    public void setPreconditions(String preconditions) { this.preconditions = preconditions; }

    public String getSteps() { return steps; }
    public void setSteps(String steps) { this.steps = steps; }

    public String getTestData() { return testData; }
    public void setTestData(String testData) { this.testData = testData; }

    public String getExpectedResult() { return expectedResult; }
    public void setExpectedResult(String expectedResult) { this.expectedResult = expectedResult; }

    public String getActualResult() { return actualResult; }
    public void setActualResult(String actualResult) { this.actualResult = actualResult; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    public String getScreenshotPath() { return screenshotPath; }
    public void setScreenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; }

    public String getDeviceLogPath() { return deviceLogPath; }
    public void setDeviceLogPath(String deviceLogPath) { this.deviceLogPath = deviceLogPath; }
}
