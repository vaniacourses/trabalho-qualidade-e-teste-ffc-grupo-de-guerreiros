package com.bancodigital.e2e;

import com.bancodigital.e2e.pages.DepositPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DepositE2ETest extends AbstractE2ETest {

    private static final String EMAIL = "joao@email.com";

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
    void depositE2EHappyPath() throws Exception {
        driver.get(baseUrl + "/deposit");
        DepositPage depositoPage = new DepositPage(driver);
        assertTrue(depositoPage.estaNaPagina(), "deve estar na página de depósito após o login");

        String saldoAntes = depositoPage.obterSaldoAtual();
        depositoPage.realizarDeposito("100.00");
        Thread.sleep(1500);

        assertTrue(driver.getPageSource().contains("alert success"),
                "o sistema deve exibir a caixa verde de sucesso do depósito");

        driver.get(baseUrl + "/deposit");
        String saldoDepois = new DepositPage(driver).obterSaldoAtual();
        assertNotEquals(saldoAntes, saldoDepois, "o saldo deve mudar após o depósito");
    }

    @Test
    void depositE2ERejectsZero() throws Exception {
        driver.get(baseUrl + "/deposit");
        new DepositPage(driver).realizarDepositoIgnorandoValidacao("0");
        Thread.sleep(1500);

        assertTrue(driver.getPageSource().contains("alert error"),
                "o sistema deve exibir a caixa vermelha de valor inválido para zero");
    }

    @Test
    void depositE2ERejectsNegative() throws Exception {
        driver.get(baseUrl + "/deposit");
        new DepositPage(driver).realizarDepositoIgnorandoValidacao("-100");
        Thread.sleep(1500);

        assertTrue(driver.getPageSource().contains("alert error"),
                "o sistema deve exibir a caixa vermelha de valor inválido para negativo");
    }
}
