# MoneyMap Launch Guide

Follow these steps to run the backend server and launch the Android application on your physical phone.

---

## Step 1: Start the Backend Server

Open a terminal (Command Prompt, PowerShell, or inside Android Studio's terminal) and navigate to the `backend` folder:

```bash
cd backend
```

Run the development server:

```bash
# In PowerShell:
npm.cmd run start:dev

# In standard Command Prompt (cmd) or Git Bash:
npm run start:dev
```

Keep this terminal open. You should see:
`🚀 MoneyMap Backend starting on: http://localhost:3000/api/v1`

---

## Step 2: Running the Android App

Depending on whether you want to run the app **plugged in** (via USB) or **unplugged** (via Wi-Fi):

### Option A: Unplugged (Wireless via Wi-Fi) — CURRENT SETUP
We have configured your app to connect directly to your computer's local Wi-Fi IP address (**`10.185.159.44`**).

1. Ensure both your computer and your phone are connected to the **same Wi-Fi network**.
2. Start the backend server (Step 1).
3. Open the app on your phone. It will connect wirelessly without requiring any USB connection or `adb reverse` command!

*Note: If your computer's IP address changes in the future (e.g. if you connect to a different Wi-Fi), you will need to update the IP address in `app/build.gradle.kts` line 17:*
```kotlin
buildConfigField("String", "MONEYMAP_API_BASE_URL", "\"http://<new-computer-ip>:3000/api/v1/\"")
```
*And then re-deploy the app to your phone.*

---

### Option B: Plugged In (Via USB Bridge)
If you want to use the default `localhost` setup instead of your Wi-Fi IP:

1. Restore the URL in `app/build.gradle.kts` to `localhost`:
   ```kotlin
   buildConfigField("String", "MONEYMAP_API_BASE_URL", "\"http://localhost:3000/api/v1/\"")
   ```
2. Plug in your phone via USB.
3. Open a new terminal on your computer and start the USB port forwarder:
   ```powershell
   & "C:\Users\surya\AppData\Local\Android\Sdk\platform-tools\adb.exe" reverse tcp:3000 tcp:3000
   ```
4. Deploy the app from Android Studio or via Gradle:
   ```powershell
   .\gradlew.bat installDebug
   ```

---

## Step 3: Start the Web App Server

To launch and run the plain HTML website dashboard:

1. Open a new terminal window in the project root directory.
2. Run the Node.js server to host the static website files:
   ```bash
   node serve.js
   ```
3. Open your browser and navigate to:
   ```
   http://localhost:8080
   ```

### Google Cloud OAuth Configuration for Web App
To make Google Sign-In work on the Web App:
1. Open the [Google Cloud Console Credentials](https://console.cloud.google.com/apis/credentials) page.
2. Under **OAuth 2.0 Client IDs**, select your **Web Application** client.
3. In **Authorized JavaScript origins**, click **ADD URI** and add:
   - `http://localhost:8080`
   - `http://localhost`
4. Click **Save**.
5. Ensure the Client ID matches exactly between:
   - Website: [script.js](file:///c:/Users/surya/AndroidStudioProjects/Moneymap/website/script.js#L1593)
   - Backend config: [.env](file:///c:/Users/surya/AndroidStudioProjects/Moneymap/backend/.env#L49)
