package com.bancodigital.integration;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import com.bancodigital.account.Account;
import com.bancodigital.transaction.StatementLine;
import com.bancodigital.transaction.TransactionType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

// Esta suite cobre o StatementController de ponta a ponta na camada web,
// verificando banco real, repository, montagem de StatementLine e renderizacao MVC.
class StatementIntegrationTest extends AbstractIntegrationTest {

    // Os ids sao guardados em campos porque as transacoes precisam referenciar
    // as contas criadas no seed e depois serem lidas pelo controller testado.
    private static final String EMAIL = "joao@email.com";
    private static final String BCRYPT_SENHA123 =
            "$2a$10$fy1UbQcOh5tYVPpfzhX5ceRqLpA1OGa7hsalIwmD2oiNXrnlbSu66";

    private long accountId;
    private long otherAccountId;

    // O seed cria uma conta principal e uma conta de contraparte para permitir
    // validar extrato com deposito, saque e transferencia no mesmo fluxo.
    @BeforeEach
    void seed() {
        long userId = insertUser("Joao Silva", EMAIL, BCRYPT_SENHA123);
        accountId = insertAccount("C00001", new BigDecimal("500.00"), userId);

        long otherUserId = insertUser("Maria Souza", "maria@email.com", BCRYPT_SENHA123);
        otherAccountId = insertAccount("C00002", new BigDecimal("100.00"), otherUserId);
    }

    @Test
    @WithMockUser(username = EMAIL)
    void statementPageShowsTransactionsMappedToStatementLines() throws Exception {
        // As insercoes diretas por JdbcTemplate montam um historico controlado
        // sem depender de outro controller; assim o foco permanece no extrato.
        jdbc.update("INSERT INTO transactions (destination_account, type, amount) VALUES (?, 'deposit', ?)",
                accountId, new BigDecimal("100.00"));
        jdbc.update("INSERT INTO transactions (source_account, type, amount) VALUES (?, 'withdraw', ?)",
                accountId, new BigDecimal("40.00"));
        jdbc.update("INSERT INTO transactions (source_account, destination_account, type, amount) "
                        + "VALUES (?, ?, 'transfer', ?)",
                accountId, otherAccountId, new BigDecimal("25.00"));

        // A verificacao usa o Model gerado pelo MVC para provar que o controller
        // buscou a conta atual, consultou o repository e converteu para StatementLine.
        MvcResult result = mockMvc.perform(get("/statement"))
               .andExpect(status().isOk())
               .andExpect(view().name("statement"))
               .andExpect(model().attribute("lines", hasSize(3)))
               .andReturn();

        // O resultado MVC permite ler os records diretamente pelos seus acessores,
        // sem depender de convencoes de propriedades JavaBean.
        Account account = (Account) result.getModelAndView().getModel().get("account");
        assertEquals("C00001", account.number());

        @SuppressWarnings("unchecked")
        List<StatementLine> lines = (List<StatementLine>) result.getModelAndView().getModel().get("lines");
        assertEquals(List.of(TransactionType.TRANSFER, TransactionType.WITHDRAW, TransactionType.DEPOSIT),
                lines.stream().map(StatementLine::type).toList());
        assertEquals(0, lines.get(2).amount().compareTo(new BigDecimal("100.00")));
    }

    @Test
    @WithMockUser(username = EMAIL)
    void statementPageShowsEmptyListWhenAccountHasNoTransactions() throws Exception {
        // O cenario sem transacoes garante que o controller entrega uma lista vazia,
        // permitindo que o template mostre a mensagem de extrato vazio sem erro.
        mockMvc.perform(get("/statement"))
               .andExpect(status().isOk())
               .andExpect(view().name("statement"))
               .andExpect(model().attribute("account",
                       is(new Account(1L, "C00001", new BigDecimal("500.00"), 1L))))
               .andExpect(model().attribute("lines", hasSize(0)));
    }

    @Test
    void statementPageRequiresAuthentication() throws Exception {
        // Assim como saldo, extrato contem dados sensiveis; por isso o teste
        // confirma que a rota anonima e interceptada pelo Spring Security.
        mockMvc.perform(get("/statement"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrlPattern("**/login"));
    }
}
