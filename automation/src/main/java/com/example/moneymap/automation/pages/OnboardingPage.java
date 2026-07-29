package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class OnboardingPage extends BasePage {

    private final By skipButton = byText("Skip");
    private final By continueButton = byText("Continue");
    private final By getStartedButton = byText("Get Started");

    public OnboardingPage(AndroidDriver driver) {
        super(driver);
    }

    public void clickSkip() {
        click(skipButton);
    }

    public void clickContinue() {
        click(continueButton);
    }

    public void clickGetStarted() {
        click(getStartedButton);
    }

    public boolean isTitleDisplayed(String titleText) {
        return isElementDisplayed(byText(titleText));
    }
}
