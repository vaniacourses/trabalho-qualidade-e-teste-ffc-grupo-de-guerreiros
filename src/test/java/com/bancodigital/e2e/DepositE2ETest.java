package com.bancodigital.e2e;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bancodigital.e2e.pages.DepositPage;

class DepositE2ETest extends AbstractE2ETest {

    private static final String EMAIL = "joao@email.com";

    @BeforeEach
    void seedAndLogin() {
        seedDefaultUser();
        loginAs(EMAIL, TEST_PASSWORD);
    }

    @Test
    void depositE2EHappyPath() {
        driver.get(baseUrl + "/deposit");
        DepositPage depositoPage = new DepositPage(driver);
        assertTrue(depositoPage.estaNaPagina(), "deve estar na página de depósito após o login");

        String saldoAntes = depositoPage.obterSaldoAtual();
        depositoPage.realizarDeposito("100.00");

        assertTrue(driver.getPageSource().contains("alert success"),
                "o sistema deve exibir a caixa verde de sucesso do depósito");

        driver.get(baseUrl + "/deposit");
        String saldoDepois = new DepositPage(driver).obterSaldoAtual();
        assertNotEquals(saldoAntes, saldoDepois, "o saldo deve mudar após o depósito");
    }

    @Test
    void depositE2ERejectsZero() {
        driver.get(baseUrl + "/deposit");
        new DepositPage(driver).realizarDepositoIgnorandoValidacao("0");

        assertTrue(driver.getPageSource().contains("alert error"),
                "o sistema deve exibir a caixa vermelha de valor inválido para zero");
    }

    @Test
    void depositE2ERejectsNegative() {
        driver.get(baseUrl + "/deposit");
        new DepositPage(driver).realizarDepositoIgnorandoValidacao("-100");

        assertTrue(driver.getPageSource().contains("alert error"),
                "o sistema deve exibir a caixa vermelha de valor inválido para negativo");
    }
}
