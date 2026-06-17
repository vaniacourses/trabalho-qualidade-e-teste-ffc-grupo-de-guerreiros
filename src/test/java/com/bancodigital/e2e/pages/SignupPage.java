package com.bancodigital.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class SignupPage extends BasePage {

    public SignupPage(WebDriver driver, String baseUrl) {
        super(driver);
        driver.get(baseUrl + "/signup");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nome")));
    }

    // Fills the form without submitting — separates setup from latency measurement.
    public SignupPage fill(String name, String email, String password) {
        driver.findElement(By.id("nome")).sendKeys(name);
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(password);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].setAttribute('novalidate', '')",
                driver.findElement(By.cssSelector("form"))
        );
        return this;
    }

    // Submits the form and waits for redirect or error message.
    public SignupPage submit() {
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        wait.until(ExpectedConditions.stalenessOf(submitButton));
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/login?signup"),
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.alert.error"))
        ));
        return this;
    }

    public SignupPage fillAndSubmit(String name, String email, String password) {
        return fill(name, email, password).submit();
    }
}
