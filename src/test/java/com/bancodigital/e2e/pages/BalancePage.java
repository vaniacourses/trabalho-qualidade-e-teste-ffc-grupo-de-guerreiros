package com.bancodigital.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

// O Page Object concentra seletores da tela de saldo para que a suite descreva
// regras de negocio e nao detalhes de implementacao do HTML.
public class BalancePage extends BasePage {

    // Seletores CSS foram usados por apontarem para classes e estrutura estaveis
    // do template, sem depender de textos que podem mudar por acentuacao.
    private static final By ACCOUNT_NUMBER = By.cssSelector(".card .muted strong");
    private static final By BALANCE = By.cssSelector(".balance-highlight");

    // O construtor abre a rota real e aguarda seu conteudo principal, garantindo
    // que as assercoes ocorram somente depois da renderizacao Thymeleaf.
    public BalancePage(WebDriver driver, String baseUrl) {
        super(driver);
        driver.get(baseUrl + "/balance");
        wait.until(ExpectedConditions.visibilityOfElementLocated(BALANCE));
    }

    // O numero e lido do DOM final para validar a conta associada ao principal
    // autenticado pelo BalanceController.
    public String getAccountNumber() {
        return driver.findElement(ACCOUNT_NUMBER).getText();
    }

    // O texto monetario valida simultaneamente o saldo persistido e a formatacao
    // aplicada pelo template no navegador.
    public String getFormattedBalance() {
        return driver.findElement(BALANCE).getText();
    }
}
