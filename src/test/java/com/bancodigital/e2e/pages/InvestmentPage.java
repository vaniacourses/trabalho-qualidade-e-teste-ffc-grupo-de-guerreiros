package com.bancodigital.e2e.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class InvestmentPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public InvestmentPage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        driver.get(baseUrl + "/investment");
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/login"),
                ExpectedConditions.presenceOfElementLocated(By.id("op"))
        ));
    }

    public InvestmentPage submitOperation(String op, String amount) {
        new Select(driver.findElement(By.id("op"))).selectByValue(op);
        WebElement amountField = driver.findElement(By.id("amount"));
        amountField.clear();
        amountField.sendKeys(amount);
        WebElement submitButton = driver.findElement(By.cssSelector("button.primary"));
        submitButton.click();
        wait.until(ExpectedConditions.stalenessOf(submitButton));
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.alert.success")),
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.alert.error"))
        ));
        return this;
    }

    public String getCurrentAmount() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.balance-highlight"))).getText();
    }

    public String getSuccessMessage() {
        return driver.findElement(By.cssSelector("p.alert.success")).getText();
    }

    public String getErrorMessage() {
        return driver.findElement(By.cssSelector("p.alert.error")).getText();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
