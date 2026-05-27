package com.bancodigital.e2e;

import java.math.BigDecimal;

import com.bancodigital.e2e.pages.InvestmentPage;
import com.bancodigital.e2e.pages.LoginPage;
import com.bancodigital.e2e.pages.SignupPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PerformanceE2ETest extends AbstractE2ETest {

    private static final String EMAIL = "joao@email.com";
    private static final long PAGE_LOAD_SLA_MS = 2_000L;
    private static final long FORM_SUBMIT_SLA_MS = 3_000L;

    @BeforeEach
    void seed() {
        long userId = insertUser("Joao Silva", EMAIL, BCRYPT_TEST_PASSWORD);
        insertAccount("C00001", new BigDecimal("500.00"), userId);
        insertInvestment(userId, new BigDecimal("0.00"));
    }

    @Test
    void paginaCadastroCarregaDentroDoSLA() {
        long start = System.currentTimeMillis();
        new SignupPage(driver, baseUrl);
        long elapsed = System.currentTimeMillis() - start;
        assertThat(elapsed).isLessThan(PAGE_LOAD_SLA_MS);
    }

    @Test
    void paginaInvestimentoCarregaDentroDoSLA() {
        new LoginPage(driver, baseUrl).loginAs(EMAIL, TEST_PASSWORD);
        long start = System.currentTimeMillis();
        new InvestmentPage(driver, baseUrl);
        long elapsed = System.currentTimeMillis() - start;
        assertThat(elapsed).isLessThan(PAGE_LOAD_SLA_MS);
    }

    @Test
    void submissaoCadastroRespondeNoSLA() {
        SignupPage page = new SignupPage(driver, baseUrl);
        long start = System.currentTimeMillis();
        page.fillAndSubmit("Maria Souza", "maria@email.com", TEST_PASSWORD);
        long elapsed = System.currentTimeMillis() - start;
        assertThat(page.getCurrentUrl()).contains("/login?signup");
        assertThat(elapsed).isLessThan(FORM_SUBMIT_SLA_MS);
    }

    @Test
    void submissaoInvestimentoRespondeNoSLA() {
        new LoginPage(driver, baseUrl).loginAs(EMAIL, TEST_PASSWORD);
        InvestmentPage page = new InvestmentPage(driver, baseUrl);
        long start = System.currentTimeMillis();
        page.submitOperation("investir", "100.00");
        long elapsed = System.currentTimeMillis() - start;
        assertThat(page.getSuccessMessage()).isEqualTo("Investimento realizado com sucesso!");
        assertThat(elapsed).isLessThan(FORM_SUBMIT_SLA_MS);
    }
}
