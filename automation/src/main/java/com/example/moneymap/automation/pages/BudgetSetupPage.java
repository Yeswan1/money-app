package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class BudgetSetupPage extends BasePage {

    private final By totalLimitField = By.xpath("//android.widget.EditText[contains(@text, 'LIMIT') or @index='0']");
    private final By foodLimitField = By.xpath("//android.widget.EditText[@index='1']");
    private final By shoppingLimitField = By.xpath("//android.widget.EditText[@index='2']");
    private final By completeButton = By.xpath("//android.widget.Button[contains(@text, 'Complete') or contains(@text, 'Setup') or contains(@text, 'Finish')]");

    public BudgetSetupPage(AndroidDriver driver) {
        super(driver);
    }

    public void enterTotalLimit(String limit) {
        type(totalLimitField, limit);
    }

    public void enterFoodLimit(String limit) {
        type(foodLimitField, limit);
    }

    public void enterShoppingLimit(String limit) {
        type(shoppingLimitField, limit);
    }

    public void clickCompleteSetup() {
        click(completeButton);
    }

    public void setupBudgets(String total, String food, String shopping) {
        enterTotalLimit(total);
        try {
            enterFoodLimit(food);
            enterShoppingLimit(shopping);
        } catch (Exception e) {
            // Some configurations might only have total limit
        }
        clickCompleteSetup();
    }
}
