package com.bancodigital.e2e;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.boot.test.web.server.LocalServerPort;

import io.github.bonigarcia.wdm.WebDriverManager;

class TransferE2ETest extends AbstractE2ETest {

    @LocalServerPort
    private int port;

    private static final String EMAIL = "joao@email.com";
    private static final String BCRYPT_SENHA123 = "$2a$10$fy1UbQcOh5tYVPpfzhX5ceRqLpA1OGa7hsalIwmD2oiNXrnlbSu66";

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
        long userId = insertUser("Joao Silva", EMAIL, BCRYPT_SENHA123);
        insertAccount("C00001", new BigDecimal("500.00"), userId);
        long destUserId = insertUser("Maria Souza", "maria@email.com", BCRYPT_SENHA123);
        insertAccount("C00002", new BigDecimal("100.00"), destUserId);
        driver.get(baseUrl + "/login");
        driver.findElement(By.id("email")).sendKeys(EMAIL);
        driver.findElement(By.id("password")).sendKeys("senha123");
        driver.findElement(By.xpath("//button[text()='Entrar']")).click();
        Thread.sleep(1000); 
    }

    @Test
    void transferE2EHappyPath() throws Exception {
        driver.get(baseUrl + "/transfer");
        driver.findElement(By.id("destination")).sendKeys("C00002");
        driver.findElement(By.id("amount")).sendKeys("200"); 
        driver.findElement(By.xpath("//button[text()='Transferir']")).click();
        Thread.sleep(1500);
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("alert success"), "O sistema deve exibir a caixa verde de sucesso");
    }

    @Test
    void transferE2ERejectsInsufficientBalance() throws Exception {
        driver.get(baseUrl + "/transfer");
        driver.findElement(By.id("destination")).sendKeys("C00002");
        driver.findElement(By.id("amount")).sendKeys("600"); 
        driver.findElement(By.xpath("//button[text()='Transferir']")).click();
        Thread.sleep(1500);
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("alert error"), "O sistema deve exibir a caixa vermelha informando que não tem saldo");
    }
}