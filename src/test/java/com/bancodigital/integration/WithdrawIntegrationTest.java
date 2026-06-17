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

class WithdrawIntegrationTest extends AbstractIntegrationTest {

    private static final String EMAIL = "pedro@email.com";
    private static final String BCRYPT_SENHA123 = "$2a$10$fy1UbQcOh5tYVPpfzhX5ceRqLpA1OGa7hsalIwmD2oiNXrnlbSu66";

    private long userId;
    private long accountId;

    @BeforeEach
    void seed() {
        // PREPARAÇÃO: O Flyway sobe o banco zerado, então nós inserimos a conta do Pedro direto no PostgreSQL
        userId = insertUser("Pedro", EMAIL, BCRYPT_SENHA123);
        accountId = insertAccount("C00003", new BigDecimal("500.00"), userId);
    }

    @Test
    @WithMockUser(username = EMAIL)
    void withdrawEndpointExecutesWithdrawSuccessfully() throws Exception {
        // AÇÃO: Manda um HTTP POST simulando o clique do usuário no formulário de saque
        mockMvc.perform(post("/withdraw")
                .param("amount", "100.00")
                .with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/withdraw"));

        // PROVA REAL (BANCO DE DADOS): Fazemos um SELECT bruto no Postgres para ver se o dinheiro sumiu
        BigDecimal balance = jdbc.queryForObject("SELECT balance FROM accounts WHERE id = ?", BigDecimal.class, accountId);
        assertEquals(0, balance.compareTo(new BigDecimal("400.00")), "O saldo no banco deve cair para 400.00");
        
        // Verifica se a tabela de histórico (extrato) ganhou uma linha do tipo 'withdraw'
        Long txCount = jdbc.queryForObject("SELECT COUNT(*) FROM transactions WHERE source_account = ? AND type = 'withdraw'", Long.class, accountId);
        assertEquals(1L, txCount, "Deve haver 1 transação de saque registrada");
    }

    @Test
    @WithMockUser(username = EMAIL)
    void withdrawEndpointRejectsInsufficientBalanceAndRollbacks() throws Exception {
        // AÇÃO: Tenta sacar 600 reais de onde só tem 500
        mockMvc.perform(post("/withdraw")
                .param("amount", "600.00")
                .with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/withdraw"));

        // PROVA REAL (ROLLBACK): O banco não pode ter deixado o saldo ficar negativo
        BigDecimal balance = jdbc.queryForObject("SELECT balance FROM accounts WHERE id = ?", BigDecimal.class, accountId);
        assertEquals(0, balance.compareTo(new BigDecimal("500.00")), "O saldo no banco deve continuar 500.00");
        
        // Verifica que nenhuma transação falsa entrou no banco para não sujar o extrato
        Long txCount = jdbc.queryForObject("SELECT COUNT(*) FROM transactions WHERE source_account = ?", Long.class, accountId);
        assertEquals(0L, txCount, "Nenhuma transação deve ser registrada em caso de falha");
    }
    
    @Test
    @WithMockUser(username = EMAIL)
    void withdrawEndpointRejectsNegativeAmount() throws Exception {
        mockMvc.perform(post("/withdraw")
                .param("amount", "-50.00")
                .with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/withdraw"));

        // Saldo intacto no banco
        BigDecimal balance = jdbc.queryForObject("SELECT balance FROM accounts WHERE id = ?", BigDecimal.class, accountId);
        assertEquals(0, balance.compareTo(new BigDecimal("500.00")));
    }
}