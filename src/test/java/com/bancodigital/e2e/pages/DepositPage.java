package com.bancodigital.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class DepositPage extends BasePage{

    // Locators
    private final By tituloPagina = By.tagName("h1");
    private final By saldoAtual = By.xpath("//p[contains(text(),'Saldo atual')]/strong");
    private final By numeroConta = By.xpath("//p[contains(text(),'Conta')]/strong");

    private final By campoValorDeposito = By.id("amount");
    private final By botaoDepositar = By.xpath("//button[text()='Depositar']");

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
        WebElement campo = driver.findElement(campoValorDeposito);
        informarValor(valor);
        clicarDepositar();
        // A troca da pagina confirma que o POST terminou antes de validar o
        // alerta, evitando leitura prematura do HTML anterior.
        wait.until(ExpectedConditions.stalenessOf(campo));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.alert.success")));
    }

    /**
     * Submete o formulário ignorando a validação HTML5 (min/step) do input,
     * para exercitar a validação do lado do servidor com valores como 0 ou negativos.
     */
    public void realizarDepositoIgnorandoValidacao(String valor) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement campo = driver.findElement(campoValorDeposito);
        js.executeScript("arguments[0].value = arguments[1];", campo, valor);
        js.executeScript("HTMLFormElement.prototype.submit.call(arguments[0].form);", campo);
        // O submit via JavaScript contorna somente a validacao HTML5; a espera
        // ainda exige que o servidor responda e apresente o erro de dominio.
        wait.until(ExpectedConditions.stalenessOf(campo));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.alert.error")));
    }

    public boolean estaNaPagina() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(tituloPagina))
            .getText()
            .equalsIgnoreCase("Depósito");
    }
}

