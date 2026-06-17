package com.bancodigital.e2e;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.boot.test.web.server.LocalServerPort;

import io.github.bonigarcia.wdm.WebDriverManager;

class WithdrawE2ETest extends AbstractE2ETest {

    @LocalServerPort
    private int port;

        private static final String EMAIL = "joao@email.com";

    @BeforeEach
    @Override
    void setupDriver() {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        
        boolean headless = "true".equalsIgnoreCase(System.getProperty("headless", "false"));
        if (headless) {
            options.addArguments("-headless"); 
        }
        
        driver = new FirefoxDriver(options);
        
        baseUrl = "http://localhost:" + port;
        jdbc.execute("TRUNCATE TABLE transactions, investments, accounts, users RESTART IDENTITY CASCADE");
        jdbc.execute("ALTER SEQUENCE account_number_seq RESTART WITH 100");

    }

    @BeforeEach
    void seedAndLogin() throws Exception {
        seedDefaultUser();
        
        driver.get(baseUrl + "/login");
        driver.findElement(By.id("email")).sendKeys(EMAIL);
        driver.findElement(By.id("password")).sendKeys(TEST_PASSWORD);

        driver.findElement(By.xpath("//button[text()='Entrar']")).click();
        Thread.sleep(1000); 
    }

    @Test

    void withdrawE2EHappyPath() throws Exception {
        driver.get(baseUrl + "/withdraw");
        driver.findElement(By.id("amount")).sendKeys("100"); 
        
        driver.findElement(By.xpath("//button[text()='Sacar']")).click();
        Thread.sleep(1500);
        
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("alert success"), "O sistema deve exibir a caixa verde de sucesso do saque. ");
    }


    @Test
    void withdrawE2ERejectsInsufficientBalance() throws Exception {
        driver.get(baseUrl + "/withdraw");
        driver.findElement(By.id("amount")).sendKeys("600"); 
        
        driver.findElement(By.xpath("//button[text()='Sacar']")).click() ;
        Thread.sleep(1500);
        
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("alert error"), "O sistema deve exibir a caixa vermelha de erro de saldo. ");
    }
    
    @Test
    void withdrawE2ERejectsOverDailyLimit() throws Exception {
        driver.get(baseUrl + "/withdraw");
        driver.findElement(By.id("amount")).sendKeys("10001"); 
        
        driver.findElement(By.xpath("//button[text()='Sacar']")).click();
        Thread.sleep(1500);
        
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("alert error"), "O sistema deve exibir a caixa vermelha de limite excedido. ");
    }
}