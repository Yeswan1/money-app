package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class LoginPage extends BasePage {

    // Locators based on Compose class hierarchy
    private final By emailField = By.xpath("//android.widget.EditText[contains(@text, 'Email') or contains(@text, 'Address') or @index='0']");
    private final By passwordField = By.xpath("//android.widget.EditText[contains(@text, 'Password') or @index='1']");
    private final By loginButton = byText("Login");
    private final By signUpLink = byText("Sign Up");
    private final By forgotPasswordLink = byText("Forgot Password?");
    private final By googleButton = byText("Continue with Google");
    private final By passwordToggle = By.xpath("//android.widget.EditText[contains(@text, 'Password')]/following-sibling::android.widget.Button[1]");

    public LoginPage(AndroidDriver driver) {
        super(driver);
    }

    public void enterEmail(String email) {
        type(emailField, email);
    }

    public void enterPassword(String password) {
        type(passwordField, password);
    }

    public void clickLogin() {
        click(loginButton);
    }

    public void clickSignUp() {
        click(signUpLink);
    }

    public void clickForgotPassword() {
        click(forgotPasswordLink);
    }

    public void clickContinueWithGoogle() {
        click(googleButton);
    }

    public boolean isErrorMessageDisplayed(String expectedError) {
        return isElementDisplayed(byText(expectedError));
    }

    public void login(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickLogin();
    }
}
