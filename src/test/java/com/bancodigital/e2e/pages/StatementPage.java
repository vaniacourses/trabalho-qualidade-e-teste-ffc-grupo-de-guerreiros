package com.bancodigital.e2e.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

// Este Page Object representa a tabela e o estado vazio do extrato, isolando
// os localizadores Selenium das regras verificadas pela suite E2E.
public class StatementPage extends BasePage {

    // Os seletores usam a estrutura semantica existente: conta no cabecalho,
    // linhas no tbody, tags de tipo e a segunda mensagem muted no card.
    private static final By HEADING = By.cssSelector(".card h1");
    private static final By ACCOUNT_NUMBER = By.cssSelector(".card .muted strong");
    private static final By ROWS = By.cssSelector("tbody tr");
    private static final By TYPES = By.cssSelector("tbody .tag");
    private static final By EMPTY_MESSAGE = By.cssSelector(".card > p.muted:nth-of-type(2)");

    // A pagina aguarda o titulo porque ele existe tanto no extrato preenchido
    // quanto no vazio, funcionando como marcador comum de carregamento.
    public StatementPage(WebDriver driver, String baseUrl) {
        super(driver);
        driver.get(baseUrl + "/statement");
        wait.until(ExpectedConditions.visibilityOfElementLocated(HEADING));
    }

    // O numero confirma qual conta foi obtida pelo CurrentUser e AccountService.
    public String getAccountNumber() {
        return driver.findElement(ACCOUNT_NUMBER).getText();
    }

    // A quantidade de linhas comprova quantas transacoes foram transformadas em
    // StatementLine e renderizadas pelo Thymeleaf.
    public int getRowCount() {
        return driver.findElements(ROWS).size();
    }

    // Os tipos sao devolvidos na ordem visual para validar tambem o ORDER BY
    // date DESC implementado pelo JdbcTransactionRepository.
    public List<String> getTransactionTypes() {
        return driver.findElements(TYPES).stream()
                .map(element -> element.getText())
                .toList();
    }

    // A mensagem alternativa garante que contas sem historico ainda recebam
    // feedback compreensivel em vez de uma tabela vazia.
    public String getEmptyMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(EMPTY_MESSAGE)).getText();
    }
}
