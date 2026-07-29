package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class DashboardPage extends BasePage {

    // Bottom Navigation Bar Locators
    private final By homeTab = byText("Home");
    private final By reportsTab = byText("Reports");
    private final By addTransactionTab = By.xpath("//android.widget.ImageView[contains(@content-desc, 'Wallet') or contains(@text, 'Wallet')]/.. or //android.widget.FrameLayout[@index='2']");
    private final By budgetTab = byText("Budget");
    private final By profileTab = byText("Profile");

    // Chatbot locator
    private final By chatbotIcon = By.xpath("//android.widget.TextView[contains(@text, 'Chat') or contains(@text, 'AI')] or //*[contains(@content-desc, 'Chat')]");

    public DashboardPage(AndroidDriver driver) {
        super(driver);
    }

    public void navigateToHome() {
        click(homeTab);
    }

    public void navigateToReports() {
        click(reportsTab);
    }

    public void clickAddTransactionButton() {
        // Fallback clicks
        try {
            click(addTransactionTab);
        } catch (Exception e) {
            click(By.xpath("//android.widget.FrameLayout[@index='2']"));
        }
    }

    public void navigateToBudget() {
        click(budgetTab);
    }

    public void navigateToProfile() {
        click(profileTab);
    }

    public void clickChatbot() {
        try {
            click(chatbotIcon);
        } catch (Exception e) {
            click(byText("Chat"));
        }
    }

    public boolean isDashboardLoaded() {
        return isElementDisplayed(homeTab) || isElementDisplayed(By.xpath("//*[contains(@text, 'Available') or contains(@text, 'Remaining')]"));
    }

    public String getAvailableBalance() {
        try {
            return getText(By.xpath("//*[contains(@text, '₹')]"));
        } catch (Exception e) {
            return "₹0.00";
        }
    }
}
