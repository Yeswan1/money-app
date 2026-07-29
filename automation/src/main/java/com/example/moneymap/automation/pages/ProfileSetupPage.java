package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class ProfileSetupPage extends BasePage {

    // Flexible locators matching input prompts or placeholder texts
    private final By nameInput = By.xpath("//android.widget.EditText[contains(@text, 'Name') or contains(@text, 'NAME') or contains(@text, 'your name') or @index='0']");
    private final By financialInput = By.xpath("//android.widget.EditText[contains(@text, 'MONEY') or contains(@text, 'SALARY') or contains(@text, 'INCOME') or contains(@text, 'BUDGET') or contains(@text, '0.00') or @index='1']");
    private final By secondaryInput = By.xpath("//android.widget.EditText[contains(@text, 'COLLEGE') or contains(@text, 'COMPANY') or contains(@text, 'DATE') or @index='2']");
    
    private final By nextButton = By.xpath("//android.widget.Button[contains(@text, 'Next') or contains(@text, 'Continue') or contains(@text, 'Complete')]");

    public ProfileSetupPage(AndroidDriver driver) {
        super(driver);
    }

    public void enterName(String name) {
        type(nameInput, name);
    }

    public void enterFinancialAmount(String amount) {
        type(financialInput, amount);
    }

    public void enterSecondaryDetail(String detail) {
        type(secondaryInput, detail);
    }

    public void clickNext() {
        click(nextButton);
    }

    public void setupStudentProfile(String name, String pocketMoney, String college) {
        enterName(name);
        enterFinancialAmount(pocketMoney);
        enterSecondaryDetail(college);
        clickNext();
    }

    public void setupProfessionalProfile(String name, String salary, String company) {
        enterName(name);
        enterFinancialAmount(salary);
        enterSecondaryDetail(company);
        clickNext();
    }

    public void setupGeneralProfile(String name, String income) {
        enterName(name);
        enterFinancialAmount(income);
        clickNext();
    }
}
