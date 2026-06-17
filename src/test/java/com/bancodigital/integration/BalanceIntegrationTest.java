package com.bancodigital.integration;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import com.bancodigital.account.Account;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

// Esta classe valida o BalanceController em integracao com Security, CurrentUser,
// AccountService e JdbcAccountRepository, sem substituir dependencias por mocks.
class BalanceIntegrationTest extends AbstractIntegrationTest {

    // As constantes centralizam os dados do usuario autenticado para que o
    // @WithMockUser tenha o mesmo e-mail existente no banco de teste.
    private static final String EMAIL = "joao@email.com";
    private static final String BCRYPT_SENHA123 =
            "$2a$10$fy1UbQcOh5tYVPpfzhX5ceRqLpA1OGa7hsalIwmD2oiNXrnlbSu66";

    // O seed e feito antes de cada teste para manter isolamento: a base abstrata
    // limpa o banco e este metodo monta apenas o estado necessario para saldo.
    @BeforeEach
    void seed() {
        long userId = insertUser("Joao Silva", EMAIL, BCRYPT_SENHA123);
        insertAccount("C00001", new BigDecimal("750.25"), userId);
    }

    @Test
    @WithMockUser(username = EMAIL)
    void balancePageShowsCurrentUserAccountFromDatabase() throws Exception {
        // O GET autenticado usa MockMvc para passar pela camada web real e depois
        // inspeciona o Model, comprovando que a conta veio do banco pelo service.
        mockMvc.perform(get("/balance"))
               .andExpect(status().isOk())
               .andExpect(view().name("balance"))
               .andExpect(model().attribute("account",
                       is(new Account(1L, "C00001", new BigDecimal("750.25"), 1L))));
    }

    @Test
    void balancePageRequiresAuthentication() throws Exception {
        // O acesso anonimo confirma a regra de seguranca declarada no SecurityConfig,
        // pois saldo e uma pagina protegida e deve redirecionar para o login.
        mockMvc.perform(get("/balance"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrlPattern("**/login"));
    }
}
