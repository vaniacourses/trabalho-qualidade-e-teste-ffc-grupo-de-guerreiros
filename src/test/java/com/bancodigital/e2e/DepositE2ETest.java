package com.bancodigital.e2e;

import com.bancodigital.e2e.pages.DepositPage;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DepositE2ETest extends AbstractE2ETest {

    @Test
    public void DepositHappyPath(){

        DepositPage depositoPage = new DepositPage(driver);

        assertTrue(depositoPage.estaNaPagina());

        String saldoAntes = depositoPage.obterSaldoAtual();

        depositoPage.realizarDeposito("100.00");

        assertTrue(
            driver.getPageSource().contains("Depósito realizado com sucesso")
        );

        String saldoDepois = depositoPage.obterSaldoAtual();

        assertNotEquals(saldoAntes, saldoDepois);
    }

    @Test
    public void DepositZero() {

        DepositPage depositoPage = new DepositPage(driver);

        depositoPage.realizarDeposito("0");

        assertTrue(
            driver.getPageSource().contains("Valor inválido")
                || driver.getPageSource().contains("O valor deve ser maior que zero")
        );
    }

    @Test
    public void DepositInvalid() {

        DepositPage depositoPage = new DepositPage(driver);

        depositoPage.informarValor("-100");

        depositoPage.clicarDepositar();

        assertTrue(
            driver.getPageSource().contains("Valor inválido")
                || driver.getPageSource().contains("O valor deve ser positivo")
        );
    }
}
