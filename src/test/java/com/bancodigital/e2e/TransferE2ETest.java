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

    // 1. O OVERWRITE: Substituindo o motor do Chrome pelo Firefox
    @BeforeEach
    @Override
    void setupDriver() {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        
        boolean headless = "true".equalsIgnoreCase(System.getProperty("headless", "false"));
        if (headless) {
            options.addArguments("-headless"); // A flag do Firefox é um pouco diferente da do Chrome
        }
        
        // Injeta a Raposa na variável da classe mãe
        driver = new FirefoxDriver(options);
        
        // Refaz a faxina de banco de dados obrigatória que a classe mãe fazia
        baseUrl = "http://localhost:" + port;
        jdbc.execute("TRUNCATE TABLE transactions, investments, accounts, users RESTART IDENTITY CASCADE");
        jdbc.execute("ALTER SEQUENCE account_number_seq RESTART WITH 100");
    }

    // 2. O PREPARO: Criando os dados e logando no site
    @BeforeEach
    void seedAndLogin() {
        long userId = insertUser("Joao Silva", EMAIL, BCRYPT_SENHA123);
        insertAccount("C00001", new BigDecimal("500.00"), userId);
        
        long destUserId = insertUser("Maria Souza", "maria@email.com", BCRYPT_SENHA123);
        insertAccount("C00002", new BigDecimal("100.00"), destUserId);

        driver.get(baseUrl + "/login");
        driver.findElement(By.name("username")).sendKeys(EMAIL);
        driver.findElement(By.name("password")).sendKeys("senha123");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
    }
    @Test
    void transferE2EHappyPath() {
        driver.get(baseUrl + "/transfer");
        
        driver.findElement(By.id("destination")).sendKeys("C00002");
        driver.findElement(By.id("amount")).sendKeys("200.00");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("Transferência"), "A página deve indicar sucesso na transferência");
    }

    @Test
    void transferE2ERejectsInsufficientBalance() {
        driver.get(baseUrl + "/transfer");
        
        driver.findElement(By.id("destination")).sendKeys("C00002");
        driver.findElement(By.id("amount")).sendKeys("600.00");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("Saldo"), "A página deve exibir erro de saldo insuficiente");
    }
}