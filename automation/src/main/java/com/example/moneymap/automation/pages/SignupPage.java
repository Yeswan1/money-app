package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class SignupPage extends BasePage {

    private final By nameField = By.xpath("//android.widget.EditText[contains(@text, 'Name') or @index='0']");
    private final By emailField = By.xpath("//android.widget.EditText[contains(@text, 'Email') or contains(@text, 'Address') or @index='1']");
    private final By passwordField = By.xpath("//android.widget.EditText[contains(@text, 'Password') and not(contains(@text, 'Confirm')) or @index='2']");
    private final By confirmPasswordField = By.xpath("//android.widget.EditText[contains(@text, 'Confirm') or @index='3']");
    private final By signUpButton = By.xpath("//android.widget.Button[contains(@text, 'Sign Up') or contains(@text, 'Create')]");
    private final By loginLink = byText("Login");

    public SignupPage(AndroidDriver driver) {
        super(driver);
    }

    public void enterFullName(String name) {
        type(nameField, name);
    }

    public void enterEmail(String email) {
        type(emailField, email);
    }

    public void enterPassword(String password) {
        type(passwordField, password);
    }

    public void enterConfirmPassword(String password) {
        type(confirmPasswordField, password);
    }

    public void clickSignUp() {
        click(signUpButton);
    }

    public void clickLoginLink() {
        click(loginLink);
    }

    public boolean isErrorMessageDisplayed(String expectedError) {
        return isElementDisplayed(byText(expectedError));
    }

    public void register(String name, String email, String password, String confirmPassword) {
        enterFullName(name);
        enterEmail(email);
        enterPassword(password);
        enterConfirmPassword(confirmPassword);
        clickSignUp();
    }
}
