package com.bancodigital.e2e;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.boot.test.web.server.LocalServerPort;

class TransferE2ETest extends AbstractE2ETest {

    @LocalServerPort
    private int port; // Pega a porta aleatória que o Spring Boot subir

    private static final String EMAIL = "joao@email.com";
    private static final String BCRYPT_SENHA123 = "$2a$10$fy1UbQcOh5tYVPpfzhX5ceRqLpA1OGa7hsalIwmD2oiNXrnlbSu66";

    @BeforeEach
    void seedAndLogin() {
        // 1. Preparar o banco de dados
        long userId = insertUser("Joao Silva", EMAIL, BCRYPT_SENHA123);
        insertAccount("C00001", new BigDecimal("500.00"), userId);
        
        long destUserId = insertUser("Maria Souza", "maria@email.com", BCRYPT_SENHA123);
        insertAccount("C00002", new BigDecimal("100.00"), destUserId);

        // 2. O Robô faz o Login
        driver.get("http://localhost:" + port + "/login");
        driver.findElement(By.name("username")).sendKeys(EMAIL);
        driver.findElement(By.name("password")).sendKeys("senha123");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
    }
    @Test
    void transferE2EHappyPath() {
        // AÇÃO: O robô entra na tela de transferência e preenche o HTML
        driver.get("http://localhost:" + port + "/transfer");
        
        driver.findElement(By.id("destination")).sendKeys("C00002");
        driver.findElement(By.id("amount")).sendKeys("200.00");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // VERIFICAÇÃO: O robô lê o HTML da página resultante procurando a mensagem de sucesso
        String pageSource = driver.getPageSource();
        // A mensagem de sucesso (Messages.TRANSFER_SUCCESS) vai estar no HTML
        assertTrue(pageSource.contains("Transferência"), "A página deve indicar sucesso na transferência");
    }

    @Test
    void transferE2ERejectsInsufficientBalance() {
        // AÇÃO: O robô tenta transferir mais do que tem (600 reais)
        driver.get("http://localhost:" + port + "/transfer");
        
        driver.findElement(By.id("destination")).sendKeys("C00002");
        driver.findElement(By.id("amount")).sendKeys("600.00");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // VERIFICAÇÃO: O robô procura a mensagem de erro no HTML
        String pageSource = driver.getPageSource();
        // A página deve recarregar mostrando o erro
        assertTrue(pageSource.contains("Saldo"), "A página deve exibir erro de saldo insuficiente");
    }
    
}