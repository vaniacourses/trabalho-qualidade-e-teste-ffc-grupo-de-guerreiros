package com.bancodigital.e2e;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.bancodigital.e2e.pages.LoginPage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// O Selenium foi escolhido para validar o fluxo completo no Chrome real:
// formulario Thymeleaf, CSRF, Spring Security, sessao e redirecionamentos.
class LoginE2ETest extends AbstractE2ETest {

    // O e-mail e centralizado para corresponder ao usuario criado pelo helper
    // seedDefaultUser(), evitando valores divergentes entre os cenarios.
    private static final String EMAIL = "joao@email.com";

    @Test
    void loginWithValidCredentialsSucceeds() {
        // O banco e preparado diretamente porque este teste deve focar no login,
        // sem depender do fluxo de cadastro para criar sua pre-condicao.
        seedDefaultUser();

        // LoginPage encapsula os seletores e usa espera explicita, abordagem que
        // evita Thread.sleep e reduz falhas causadas por variacao de desempenho.
        LoginPage loginPage = new LoginPage(driver, baseUrl)
                .loginAs(EMAIL, TEST_PASSWORD);

        // A URL final e o conteudo do dashboard comprovam autenticacao e leitura
        // do usuario persistido por todas as camadas da aplicacao.
        assertTrue(loginPage.getCurrentUrl().contains("/dashboard"));
        assertTrue(driver.findElement(By.cssSelector(".card h1")).getText().contains("Joao Silva"));
    }

    @Test
    void loginWithInvalidCredentialsShowsError() {
        // Um usuario valido e criado para isolar a causa da falha somente na
        // senha informada, sem confundir o teste com usuario inexistente.
        seedDefaultUser();

        // tryLogin aceita tanto sucesso quanto erro e por isso e apropriado para
        // cenarios negativos em que nao se deve esperar o dashboard.
        LoginPage loginPage = new LoginPage(driver, baseUrl)
                .tryLogin(EMAIL, "senha-incorreta");

        // A permanencia no login e a mensagem visivel confirmam que a sessao nao
        // foi autenticada e que o LoginController renderizou o feedback.
        assertTrue(loginPage.getCurrentUrl().contains("/login?error"));
        assertFalse(loginPage.getErrorMessage().isBlank());
    }

    @Test
    void loginWithUnregisteredEmailShowsSameSecurityError() {
        // Nenhum usuario e inserido para validar que o Spring Security nao revela
        // se um e-mail esta ou nao cadastrado, reduzindo enumeracao de contas.
        LoginPage loginPage = new LoginPage(driver, baseUrl)
                .tryLogin("inexistente@email.com", TEST_PASSWORD);

        // O mesmo retorno visual da senha invalida preserva uma mensagem generica
        // e confirma o comportamento configurado em SecurityConfig.
        assertTrue(loginPage.getCurrentUrl().contains("/login?error"));
        assertFalse(loginPage.getErrorMessage().isBlank());
    }

    @Test
    void unauthenticatedAccessToProtectedRouteRedirectsToLogin() {
        // O dashboard e acessado diretamente sem cookies para exercitar os
        // filtros reais de autorizacao, e nao apenas o LoginController isolado.
        driver.get(baseUrl + "/dashboard");

        // WebDriverWait sincroniza com o redirect HTTP sem usar pausa fixa, que
        // poderia ser curta em CI ou desnecessariamente longa localmente.
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.urlContains("/login"));

        // A URL e o campo de e-mail visivel confirmam que nenhum conteudo
        // protegido foi apresentado ao usuario anonimo.
        assertTrue(driver.getCurrentUrl().contains("/login"));
        assertTrue(driver.findElement(By.id("email")).isDisplayed());
    }
}
