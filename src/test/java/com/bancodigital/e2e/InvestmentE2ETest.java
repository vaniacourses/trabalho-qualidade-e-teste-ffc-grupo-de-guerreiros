package com.bancodigital.e2e;

import com.bancodigital.e2e.pages.InvestmentPage;
import com.bancodigital.e2e.pages.LoginPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvestmentE2ETest extends AbstractE2ETest {

    private static final String EMAIL = "joao@email.com";

    @BeforeEach
    void seed() {
        seedDefaultUser();
    }

    @Test
    void unauthenticatedAccessRedirectsToLogin() {
        InvestmentPage page = new InvestmentPage(driver, baseUrl);
        assertThat(page.getCurrentUrl()).contains("/login");
    }

    @Test
    void investWithSufficientBalanceSucceeds() {
        new LoginPage(driver, baseUrl).loginAs(EMAIL, TEST_PASSWORD);
        InvestmentPage page = new InvestmentPage(driver, baseUrl);
        page.submitOperation("investir", "100.00");
        assertThat(page.getSuccessMessage()).isEqualTo("Investimento realizado com sucesso!");
    }

    @Test
    void investMoreThanBalanceFails() {
        new LoginPage(driver, baseUrl).loginAs(EMAIL, TEST_PASSWORD);
        InvestmentPage page = new InvestmentPage(driver, baseUrl);
        page.submitOperation("investir", "600.00");
        assertThat(page.getErrorMessage()).isEqualTo("Saldo insuficiente na conta.");
    }

    @Test
    void withdrawSucceeds() {
        new LoginPage(driver, baseUrl).loginAs(EMAIL, TEST_PASSWORD);
        InvestmentPage page = new InvestmentPage(driver, baseUrl);
        page.submitOperation("investir", "100.00");
        page.submitOperation("retirar", "50.00");
        assertThat(page.getSuccessMessage()).isEqualTo("Resgate realizado com sucesso!");
    }

    @Test
    void withdrawMoreThanInvestedFails() {
        new LoginPage(driver, baseUrl).loginAs(EMAIL, TEST_PASSWORD);
        InvestmentPage page = new InvestmentPage(driver, baseUrl);
        page.submitOperation("investir", "100.00");
        page.submitOperation("retirar", "200.00");
        assertThat(page.getErrorMessage()).isEqualTo("Valor maior que o investido.");
    }
}
