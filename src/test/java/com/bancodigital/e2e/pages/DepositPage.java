package com.bancodigital.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class DepositPage extends BasePage{

    private final WebDriver driver = null;

    // Locators
    private final By tituloPagina = By.tagName("h1");
    private final By saldoAtual = By.xpath("//p[contains(text(),'Saldo atual')]/strong");
    private final By numeroConta = By.xpath("//p[contains(text(),'Conta')]/strong");

    private final By campoValorDeposito = By.id("amount");
    private final By botaoDepositar = By.cssSelector("button[type='submit']");

    public DepositPage(WebDriver driver) {
        super(driver);
    }

    public String obterTitulo() {
        return driver.findElement(tituloPagina).getText();
    }

    public String obterSaldoAtual() {
        return driver.findElement(saldoAtual).getText();
    }

    public String obterNumeroConta() {
        return driver.findElement(numeroConta).getText();
    }

    public DepositPage informarValor(String valor) {
        WebElement campo = driver.findElement(campoValorDeposito);
        campo.clear();
        campo.sendKeys(valor);
        return this;
    }

    public void clicarDepositar() {
        driver.findElement(botaoDepositar).click();
    }

    public void realizarDeposito(String valor) {
        informarValor(valor);
        clicarDepositar();
    }

    public boolean estaNaPagina() {
        return driver.findElement(tituloPagina)
            .getText()
            .equalsIgnoreCase("Depósito");
    }
}

