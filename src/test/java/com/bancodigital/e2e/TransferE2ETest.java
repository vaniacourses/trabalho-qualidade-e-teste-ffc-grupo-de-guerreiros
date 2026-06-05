package com.bancodigital.e2e;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
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

    
}