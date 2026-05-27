package com.bancodigital.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

public class InvestmentPage extends BasePage {

    public InvestmentPage(WebDriver driver, String baseUrl) {
        super(driver);
        driver.get(baseUrl + "/investment");
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/login"),
                ExpectedConditions.presenceOfElementLocated(By.id("op"))
        ));
    }

    // Fills the form without submitting — separates setup from latency measurement.
    public InvestmentPage prepareOperation(String op, String amount) {
        new Select(driver.findElement(By.id("op"))).selectByValue(op);
        WebElement amountField = driver.findElement(By.id("amount"));
        amountField.clear();
        amountField.sendKeys(amount);
        return this;
    }

    // Submits the form and waits for a response — measures only the server round-trip.
    public InvestmentPage executeOperation() {
        WebElement submitButton = driver.findElement(By.cssSelector("button.primary"));
        submitButton.click();
        wait.until(ExpectedConditions.stalenessOf(submitButton));
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.alert.success")),
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.alert.error"))
        ));
        return this;
    }

    public InvestmentPage submitOperation(String op, String amount) {
        return prepareOperation(op, amount).executeOperation();
    }

    public String getCurrentAmount() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.balance-highlight"))).getText();
    }
}
