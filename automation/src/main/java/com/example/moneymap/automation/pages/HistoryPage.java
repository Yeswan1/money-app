package com.example.moneymap.automation.pages;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

public class HistoryPage extends BasePage {

    private final By searchIcon = By.xpath("//android.widget.TextView[contains(@text, 'search') or contains(@content-desc, 'search')] or //*[contains(@content-desc, 'Search')]");
    private final By filterIcon = By.xpath("//android.widget.TextView[contains(@text, 'filter') or contains(@content-desc, 'filter')] or //*[contains(@content-desc, 'Filter')]");
    private final By transactionItem = By.xpath("//*[contains(@text, 'Food') or contains(@text, 'Transport') or contains(@text, 'Lunch')]");

    public HistoryPage(AndroidDriver driver) {
        super(driver);
    }

    public void clickSearch() {
        try {
            click(searchIcon);
        } catch (Exception e) {
            click(byText("Search"));
        }
    }

    public void clickFilter() {
        try {
            click(filterIcon);
        } catch (Exception e) {
            click(byText("Filter"));
        }
    }

    public boolean isTransactionListEmpty() {
        return !isElementDisplayed(transactionItem);
    }

    public boolean isTransactionVisible(String noteOrCategory) {
        return isElementDisplayed(byText(noteOrCategory));
    }
}
