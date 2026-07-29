package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class TransactionSuccessPage extends BasePage {

    private final By successTitle = byText("Transaction Saved!");
    private final By backHomeButton = byText("Back to Home");

    public TransactionSuccessPage(AndroidDriver driver) {
        super(driver);
    }

    public boolean isSuccessDisplayed() {
        return isElementDisplayed(successTitle);
    }

    public void clickBackToHome() {
        click(backHomeButton);
    }
}
