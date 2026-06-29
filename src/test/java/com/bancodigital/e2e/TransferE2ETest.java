package com.bancodigital.e2e;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

class TransferE2ETest extends AbstractE2ETest {

    private static final String EMAIL = "joao@email.com";
    private static final String BCRYPT_SENHA123 = "$2a$10$fy1UbQcOh5tYVPpfzhX5ceRqLpA1OGa7hsalIwmD2oiNXrnlbSu66";

    @BeforeEach
    void seedAndLogin() {
        long userId = insertUser("Joao Silva", EMAIL, BCRYPT_SENHA123);
        insertAccount("C00001", new BigDecimal("500.00"), userId);
        long destUserId = insertUser("Maria Souza", "maria@email.com", BCRYPT_SENHA123);
        insertAccount("C00002", new BigDecimal("100.00"), destUserId);
        loginAs(EMAIL, "senha123");
    }

    @Test
    void transferE2EHappyPath() {
        driver.get(baseUrl + "/transfer");
        driver.findElement(By.id("destination")).sendKeys("C00002");
        driver.findElement(By.id("amount")).sendKeys("200"); 
        driver.findElement(By.xpath("//button[text()='Transferir']")).click();
        waitForAlert("success");
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("alert success"), "O sistema deve exibir a caixa verde de sucesso");
    }

    @Test
    void transferE2ERejectsInsufficientBalance() {
        driver.get(baseUrl + "/transfer");
        driver.findElement(By.id("destination")).sendKeys("C00002");
        driver.findElement(By.id("amount")).sendKeys("600"); 
        driver.findElement(By.xpath("//button[text()='Transferir']")).click();
        waitForAlert("error");
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("alert error"), "O sistema deve exibir a caixa vermelha informando que não tem saldo");
    }
}
