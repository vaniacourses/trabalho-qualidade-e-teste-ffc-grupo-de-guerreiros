package com.bancodigital.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LoginPage extends BasePage {

    public LoginPage(WebDriver driver, String baseUrl) {
        super(driver);
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
    }

    // Happy path — used as setup in other tests; assumes valid credentials.
    public LoginPage loginAs(String email, String password) {
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        return this;
    }

    // For failure scenarios — waits for /dashboard OR error message without assuming outcome.
    public LoginPage tryLogin(String email, String password) {
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/dashboard"),
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.alert.error"))
        ));
        return this;
    }
}
