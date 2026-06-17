package com.bancodigital.e2e;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bancodigital.e2e.pages.LoginPage;
import com.bancodigital.e2e.pages.StatementPage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

// Esta suite valida pelo Chrome a integracao entre StatementController, banco,
// conversao para StatementLine e tabela final renderizada pelo Thymeleaf.
class StatementE2ETest extends AbstractE2ETest {

    // Os ids ficam nos campos porque as transacoes precisam referenciar as
    // contas preparadas antes de cada cenario.
    private static final String EMAIL = "joao@email.com";
    private long accountId;
    private long otherAccountId;

    // Duas contas sao criadas para representar deposito, saque e transferencia,
    // enquanto a limpeza da classe base preserva isolamento entre os testes.
    @BeforeEach
    void seedAccounts() {
        long userId = seedDefaultUser();
        accountId = jdbc.queryForObject(
                "SELECT id FROM accounts WHERE user_id = ?", Long.class, userId);

        long otherUserId = insertUser("Maria Souza", "maria@email.com", BCRYPT_TEST_PASSWORD);
        otherAccountId = insertAccount("C00002", new BigDecimal("100.00"), otherUserId);
    }

    @Test
    void statementShowsTransactionsInDescendingDateOrder() {
        // Datas relativas distintas tornam a ordem deterministica sem usar
        // Thread.sleep entre inserts e sem depender da velocidade da maquina.
        jdbc.update("INSERT INTO transactions (destination_account, type, amount, date) "
                        + "VALUES (?, 'deposit', ?, now() - interval '3 minutes')",
                accountId, new BigDecimal("100.00"));
        jdbc.update("INSERT INTO transactions (source_account, type, amount, date) "
                        + "VALUES (?, 'withdraw', ?, now() - interval '2 minutes')",
                accountId, new BigDecimal("40.00"));
        jdbc.update("INSERT INTO transactions "
                        + "(source_account, destination_account, type, amount, date) "
                        + "VALUES (?, ?, 'transfer', ?, now() - interval '1 minute')",
                accountId, otherAccountId, new BigDecimal("25.00"));

        // A autenticacao pela interface garante que CurrentUser recebe o mesmo
        // principal e a mesma sessao presentes em uso real.
        new LoginPage(driver, baseUrl).loginAs(EMAIL, TEST_PASSWORD);
        StatementPage statementPage = new StatementPage(driver, baseUrl);

        // A ordem visual dos tipos valida o ORDER BY date DESC e a quantidade
        // comprova que nenhuma transacao foi perdida durante o mapeamento.
        assertEquals("C00001", statementPage.getAccountNumber());
        assertEquals(3, statementPage.getRowCount());
        assertEquals(List.of("TRANSFER", "WITHDRAW", "DEPOSIT"),
                statementPage.getTransactionTypes());
    }

    @Test
    void statementShowsEmptyMessageWhenThereAreNoTransactions() {
        // Nenhuma movimentacao adicional e inserida para exercitar o ramo
        // #lists.isEmpty(lines) existente no template do extrato.
        new LoginPage(driver, baseUrl).loginAs(EMAIL, TEST_PASSWORD);
        StatementPage statementPage = new StatementPage(driver, baseUrl);

        // A ausencia de linhas e a mensagem visivel garantem uma resposta clara
        // para contas novas ou sem movimentacoes.
        assertEquals(0, statementPage.getRowCount());
        assertFalse(statementPage.getEmptyMessage().isBlank());
    }
}
