package com.bancodigital.e2e;

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
        seedDefaultUser();
    }

    @Test
    void signupPageLoadsWithinSla() {
        long start = System.currentTimeMillis();
        new SignupPage(driver, baseUrl);
        long elapsed = System.currentTimeMillis() - start;
        assertThat(elapsed).isLessThan(PAGE_LOAD_SLA_MS);
    }

    @Test
    void investmentPageLoadsWithinSla() {
        new LoginPage(driver, baseUrl).loginAs(EMAIL, TEST_PASSWORD);
        long start = System.currentTimeMillis();
        new InvestmentPage(driver, baseUrl);
        long elapsed = System.currentTimeMillis() - start;
        assertThat(elapsed).isLessThan(PAGE_LOAD_SLA_MS);
    }

    @Test
    void signupFormSubmissionRespondsWithinSla() {
        // fill() separates field population (Selenium overhead) from the server round-trip
        SignupPage page = new SignupPage(driver, baseUrl).fill("Maria Souza", "maria@email.com", TEST_PASSWORD);
        long start = System.currentTimeMillis();
        page.submit();
        long elapsed = System.currentTimeMillis() - start;
        assertThat(page.getCurrentUrl()).contains("/login?signup");
        assertThat(elapsed).isLessThan(FORM_SUBMIT_SLA_MS);
    }

    @Test
    void investmentFormSubmissionRespondsWithinSla() {
        new LoginPage(driver, baseUrl).loginAs(EMAIL, TEST_PASSWORD);
        // prepareOperation() separates field setup from the click+wait that measures server latency
        InvestmentPage page = new InvestmentPage(driver, baseUrl);
        page.prepareOperation("investir", "100.00");
        long start = System.currentTimeMillis();
        page.executeOperation();
        long elapsed = System.currentTimeMillis() - start;
        assertThat(page.getSuccessMessage()).isEqualTo("Investimento realizado com sucesso!");
        assertThat(elapsed).isLessThan(FORM_SUBMIT_SLA_MS);
    }
}
