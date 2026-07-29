# MoneyMap E2E Mobile Automation Framework

This directory contains the production-ready, enterprise-grade Appium mobile automation framework for testing the **Moneymap** Android application. It supports data-driven test runs, failure screenshot capturing, device log extraction, and generates multi-sheet Excel, interactive HTML, and Markdown reports.

---

## Folder Structure

```
automation/
├── pom.xml                      # Maven project configuration with all dependencies
├── README.md                    # Setup and execution guide
├── config/
│   └── appium-config.json       # Appium capabilities and emulator connection properties
├── data/
│   ├── test_cases.json          # Master database of 510+ test cases
│   └── test_data.json           # Input parameter sets for data-driven testing
└── src/
    ├── main/
    │   └── java/
    │       └── com/example/moneymap/automation/
    │           ├── model/
    │           │   └── TestCase.java                 # Data model for test case details
    │           ├── pages/
    │           │   ├── BasePage.java                 # Common interactive wrappers
    │           │   ├── OnboardingPage.java           # Onboarding screen maps
    │           │   ├── LoginPage.java                # Login page object
    │           │   ├── SignupPage.java               # Signup form page object
    │           │   ├── RoleSelectionPage.java        # Roles cards selection
    │           │   ├── ProfileSetupPage.java         # Roles profiles configuration
    │           │   ├── BudgetSetupPage.java          # Limits allocation inputs
    │           │   ├── NotificationPermissionPage.java# Permission buttons clicker
    │           │   ├── DashboardPage.java            # Metrics & tabs navigation
    │           │   ├── AddTransactionPage.java       # Transaction builder
    │           │   ├── TransactionSuccessPage.java   # Success state verification
    │           │   └── HistoryPage.java              # Filter & search list view
    │           ├── reporting/
    │           │   ├── ExcelReportGenerator.java     # Multi-sheet POI Excel exporter
    │           │   └── HTMLReportGenerator.java      # Dashboard & Trends visual generator
    │           └── utils/
    │               ├── AppiumDriverFactory.java      # Appium session builder
    │               ├── ScreenshotUtil.java           # PNG failure snapshot capture
    │               └── LogUtil.java                  # ADB logcat & session log collector
    └── test/
        ├── java/
        │   └── com/example/moneymap/automation/
        │       ├── listeners/
        │       │   └── TestNGListener.java           # Captures trace/screenshot on test failure
        │       └── tests/
        │           ├── BaseTest.java                 # Suite setups and report compilation
        │           └── E2EAutomationTest.java        # Dynamic execution runner
        └── resources/
            └── testng.xml        # Test suite configurations and listeners link
```

---

## Local Execution Guide

### Prerequisites
1. **Java JDK 21**: Verify with `java -version`.
2. **Maven**: Verify with `mvn -version`.
3. **Node.js & Appium**: Install Appium and driver locally:
   ```bash
   npm install -g appium appium-uiautomator2-driver
   ```
4. **Android SDK & Emulator**: Set environment variables `ANDROID_HOME`. Start a virtual emulator.

### Running the Tests Locally
1. Build the Android APK from the root directory:
   ```bash
   ./gradlew assembleDebug
   ```
2. Start the Appium Server:
   ```bash
   appium
   ```
3. Run the automation tests:
   ```bash
   cd automation
   mvn clean test
   ```

All test outcomes, screenshots, and logs will be written to `automation/Test Results/`.

---

## CI/CD Execution Guide

The pipeline runs automatically on every push, pull request, and daily cron at 2:00 AM UTC.
Execution workflow:
1. **Compile & Package**: Compiles the Android code and packages a fresh APK.
2. **Launch VM Emulator**: Boots a headless macOS Android runner.
3. **Appium Daemon**: Starts the Appium service.
4. **Test Run**: Executes `mvn clean test` running the 510+ test suite catalog.
5. **Report Compilation**: Generates the Excel charts, HTML dashboards, and logs.
6. **Pages Publishing**: Deploys reports to GitHub Pages (`gh-pages` branch) and saves trends history.
7. **Job Summary**: Renders a rich Markdown card in the GHA run summary.

---

## Repository Configuration Guide

To deploy reports successfully to GitHub Pages:
1. Go to **Settings** -> **Pages** in your repository.
2. Under **Build and deployment**, select **Deploy from a branch**.
3. Choose the **`gh-pages`** branch and the **`/` (root)** folder, then click **Save**.
4. Go to **Settings** -> **Actions** -> **General**, scroll to **Workflow permissions**, select **Read and write permissions**, and click **Save**. This allows the action to push outputs to `gh-pages`.

### Live Report URL
Once configured, the live execution dashboard will be available at:
`https://<github-username>.github.io/<repository-name>/reports/latest/execution-report.html`

---

## Troubleshooting Guide

### 1. Appium Session Creation Timed Out
- **Cause**: The emulator did not boot in time, or Appium took too long to connect.
- **Fix**: Check `adb devices` to ensure the emulator is online. Increase `adbExecTimeout` in `appium-config.json`.

### 2. APK Path Mismatch
- **Cause**: Gradle output directory changed.
- **Fix**: Verify the output path in `appium-config.json` corresponds to the actual path of the debug APK:
  `c:/Users/surya/AndroidStudioProjects/Moneymap/app/build/outputs/apk/debug/app-debug.apk`

### 3. Permission Denied on `gh-pages` Commit
- **Cause**: GitHub Actions lacks permissions.
- **Fix**: Verify "Read and write permissions" is checked in Actions Settings.
