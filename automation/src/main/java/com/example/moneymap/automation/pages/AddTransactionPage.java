package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class AddTransactionPage extends BasePage {

    private final By expenseToggle = byText("EXPENSE");
    private final By incomeToggle = byText("INCOME");
    
    // Amount field is usually the first EditText or contains '0.00'
    private final By amountField = By.xpath("//android.widget.EditText[contains(@text, '0.00') or @index='0']");
    
    // Note input is the second EditText or placeholder 'Add a note...'
    private final By noteField = By.xpath("//android.widget.EditText[contains(@text, 'note') or @index='1']");
    
    private final By selectCategoryButton = byText("SELECT CATEGORY");
    private final By saveButton = byText("Save Transaction");

    public AddTransactionPage(AndroidDriver driver) {
        super(driver);
    }

    public void selectExpense() {
        click(expenseToggle);
    }

    public void selectIncome() {
        click(incomeToggle);
    }

    public void enterAmount(String amount) {
        type(amountField, amount);
    }

    public void enterNote(String note) {
        type(noteField, note);
    }

    public void selectCategory(String categoryName) {
        click(selectCategoryButton);
        // Click on the specific category text in the popup/picker list
        click(byText(categoryName));
    }

    public void clickSave() {
        click(saveButton);
    }

    public void createTransaction(String type, String amount, String category, String note) {
        if ("income".equalsIgnoreCase(type)) {
            selectIncome();
        } else {
            selectExpense();
        }
        enterAmount(amount);
        selectCategory(category);
        if (note != null && !note.isEmpty()) {
            enterNote(note);
        }
        clickSave();
    }
}
