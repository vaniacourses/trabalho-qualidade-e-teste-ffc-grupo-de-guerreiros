package com.bancodigital.integration;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InvestmentIntegrationTest extends AbstractIntegrationTest {

    private static final String EMAIL = "joao@email.com";
    private static final String BCRYPT_SENHA123 = "$2a$10$fy1UbQcOh5tYVPpfzhX5ceRqLpA1OGa7hsalIwmD2oiNXrnlbSu66";

    private long userId;
    private long accountId;

    @BeforeEach
    void seed() {
        userId = insertUser("Joao Silva", EMAIL, BCRYPT_SENHA123);
        accountId = insertAccount("C00001", new BigDecimal("500.00"), userId);
        insertInvestment(userId, new BigDecimal("0.00"));
    }

    @Test
    @WithMockUser(username = EMAIL)
    void investmentEndpointExecutesInvestAndPersistsTransaction() throws Exception {
        mockMvc.perform(post("/investment")
                .param("op", "investir")
                .param("amount", "100.00")
                .with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/investment"));

        BigDecimal balance = jdbc.queryForObject(
                "SELECT balance FROM accounts WHERE id = ?", BigDecimal.class, accountId);
        assertEquals(0, balance.compareTo(new BigDecimal("400.00")));

        BigDecimal invested = jdbc.queryForObject(
                "SELECT amount FROM investments WHERE user_id = ?", BigDecimal.class, userId);
        assertEquals(0, invested.compareTo(new BigDecimal("100.00")));

        Long txCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM transactions WHERE source_account = ? AND type = 'investment'",
                Long.class, accountId);
        assertEquals(1L, txCount);
    }

    @Test
    @WithMockUser(username = EMAIL)
    void investmentEndpointRejectsInsufficientBalance() throws Exception {
        jdbc.update("UPDATE accounts SET balance = 10.00 WHERE id = ?", accountId);

        mockMvc.perform(post("/investment")
                .param("op", "investir")
                .param("amount", "100.00")
                .with(csrf()))
               .andExpect(status().is3xxRedirection());

        BigDecimal balance = jdbc.queryForObject(
                "SELECT balance FROM accounts WHERE id = ?", BigDecimal.class, accountId);
        assertEquals(0, balance.compareTo(new BigDecimal("10.00")));

        BigDecimal invested = jdbc.queryForObject(
                "SELECT amount FROM investments WHERE user_id = ?", BigDecimal.class, userId);
        assertEquals(0, invested.compareTo(new BigDecimal("0.00")));

        Long txCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM transactions WHERE source_account = ?", Long.class, accountId);
        assertEquals(0L, txCount);
    }

    @Test
    void investmentEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/investment"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrlPattern("**/login"));
    }
}
