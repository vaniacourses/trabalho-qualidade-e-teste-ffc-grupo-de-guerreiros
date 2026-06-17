package com.bancodigital.e2e;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.bancodigital.e2e.pages.BalancePage;
import com.bancodigital.e2e.pages.LoginPage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// A suite atravessa navegador, seguranca, BalanceController, service, repository,
// PostgreSQL e Thymeleaf para validar o saldo como o usuario realmente o ve.
class BalanceE2ETest extends AbstractE2ETest {

    // O e-mail corresponde ao helper compartilhado da infraestrutura E2E e
    // permite reutilizar a senha forte cujo hash BCrypt ja foi validado.
    private static final String EMAIL = "joao@email.com";

    @Test
    void authenticatedUserSeesAccountAndFormattedBalance() {
        // seedDefaultUser cria saldo conhecido de 500.00 diretamente no banco,
        // mantendo este teste independente das telas de deposito e cadastro.
        seedDefaultUser();

        // O login e feito pelo formulario real para criar cookies e sessao no
        // mesmo Chrome que depois acessara a rota protegida de saldo.
        new LoginPage(driver, baseUrl).loginAs(EMAIL, TEST_PASSWORD);
        BalancePage balancePage = new BalancePage(driver, baseUrl);

        // Conta e valor formatado no DOM confirmam que os dados persistidos
        // chegaram corretamente ate a interface.
        assertEquals("C00001", balancePage.getAccountNumber());
        assertTrue(balancePage.getFormattedBalance().contains("500,00"));
    }

    @Test
    void anonymousUserIsRedirectedToLogin() {
        // O acesso ocorre sem autenticacao para testar o SecurityFilterChain
        // usando redirecionamento e pagina reais do navegador.
        driver.get(baseUrl + "/balance");

        // A espera explicita reage assim que o redirect termina e evita sleeps
        // fixos, que deixam suites E2E mais lentas e instaveis.
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.urlContains("/login"));

        // O formulario visivel comprova que o controller protegido nao revelou
        // numero de conta nem saldo ao visitante anonimo.
        assertTrue(driver.getCurrentUrl().contains("/login"));
        assertTrue(driver.findElement(By.id("email")).isDisplayed());
    }
}
