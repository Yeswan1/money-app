package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class NotificationPermissionPage extends BasePage {

    private final By allowButton = byText("Allow");
    private final By notNowButton = byText("Not Now");

    public NotificationPermissionPage(AndroidDriver driver) {
        super(driver);
    }

    public void clickAllow() {
        click(allowButton);
    }

    public void clickNotNow() {
        click(notNowButton);
    }
}
