package com.bancodigital.e2e.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SignupPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public SignupPage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        driver.get(baseUrl + "/signup");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nome")));
    }

    public SignupPage fillAndSubmit(String name, String email, String password) {
        driver.findElement(By.id("nome")).sendKeys(name);
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(password);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].setAttribute('novalidate', '')",
                driver.findElement(By.cssSelector("form"))
        );
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        wait.until(ExpectedConditions.stalenessOf(submitButton));
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/login?signup"),
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.alert.error"))
        ));
        return this;
    }

    public String getErrorMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.alert.error"))).getText();
    }

    public String getSuccessMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.alert.success"))).getText();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public WebElement getNameField() {
        return driver.findElement(By.id("nome"));
    }
}
