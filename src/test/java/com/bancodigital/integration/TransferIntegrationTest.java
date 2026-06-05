package com.bancodigital.integration;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransferIntegrationTest extends AbstractIntegrationTest {

    private static final String EMAIL = "joao@email.com";
    private static final String BCRYPT_SENHA123 = "$2a$10$fy1UbQcOh5tYVPpfzhX5ceRqLpA1OGa7hsalIwmD2oiNXrnlbSu66";

    private long userId;
    private long sourceAccountId;
    private long destAccountId;

    @BeforeEach
    void seed() {
        // Criando as duas contas zeradas no banco real antes de cada teste
        userId = insertUser("Joao Silva", EMAIL, BCRYPT_SENHA123);
        sourceAccountId = insertAccount("C00001", new BigDecimal("500.00"), userId);
        
        long destUserId = insertUser("Maria Souza", "maria@email.com", BCRYPT_SENHA123);
        destAccountId = insertAccount("C00002", new BigDecimal("100.00"), destUserId);
    }

    @Test
    @WithMockUser(username = EMAIL)
    void transferEndpointExecutesTransferSuccessfully() throws Exception {
        // AÇÃO: Transferir 200 reais
        mockMvc.perform(post("/transfer")
                .param("destination", "C00002")
                .param("amount", "200.00")
                .with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/transfer")); // <-- CORRIGIDO AQUI!

        // VERIFICAÇÃO: Checando os saldos fisicamente no PostgreSQL
        BigDecimal sourceBalance = jdbc.queryForObject("SELECT balance FROM accounts WHERE id = ?", BigDecimal.class, sourceAccountId);
        assertEquals(0, sourceBalance.compareTo(new BigDecimal("300.00")));

        BigDecimal destBalance = jdbc.queryForObject("SELECT balance FROM accounts WHERE id = ?", BigDecimal.class, destAccountId);
        assertEquals(0, destBalance.compareTo(new BigDecimal("300.00")));
        
        // Confirmando que gerou uma linha de extrato no banco
        Long txCount = jdbc.queryForObject("SELECT COUNT(*) FROM transactions WHERE source_account = ? AND type = 'transfer'", Long.class, sourceAccountId);
        assertEquals(1L, txCount);
    }

    @Test
    @WithMockUser(username = EMAIL)
    void transferEndpointRejectsInsufficientBalanceAndRollbacks() throws Exception {
        // AÇÃO: Tentar transferir 600 reais (origem só tem 500)
        mockMvc.perform(post("/transfer")
                .param("destination", "C00002")
                .param("amount", "600.00")
                .with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/transfer")); 

        // VERIFICAÇÃO (ROLLBACK): O banco deve barrar e manter o saldo original de 500
        BigDecimal sourceBalance = jdbc.queryForObject("SELECT balance FROM accounts WHERE id = ?", BigDecimal.class, sourceAccountId);
        assertEquals(0, sourceBalance.compareTo(new BigDecimal("500.00")));

        BigDecimal destBalance = jdbc.queryForObject("SELECT balance FROM accounts WHERE id = ?", BigDecimal.class, destAccountId);
        assertEquals(0, destBalance.compareTo(new BigDecimal("100.00")));
        
        // Confirmando que a transação falsa NÃO foi salva pela metade
        Long txCount = jdbc.queryForObject("SELECT COUNT(*) FROM transactions WHERE source_account = ?", Long.class, sourceAccountId);
        assertEquals(0L, txCount);
    }
}