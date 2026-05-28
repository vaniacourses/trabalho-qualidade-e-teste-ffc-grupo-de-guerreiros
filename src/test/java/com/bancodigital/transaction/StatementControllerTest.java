package com.bancodigital.transaction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;

import com.bancodigital.account.Account;
import com.bancodigital.account.AccountService;
import com.bancodigital.auth.CurrentUser;
import com.bancodigital.auth.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatementControllerTest {

    @Mock TransactionRepository transactionRepository;
    @Mock AccountService accountService;
    @Mock CurrentUser currentUser;
    @Mock UserDetails principal;
    @Mock Model model;

    private StatementController controller;

    @BeforeEach
    void setUp() {
        controller = new StatementController(transactionRepository, accountService, currentUser);
    }

    @Test
    void statementReturnsViewWithAccountAndLines() {
        User user = new User(7L, "Joao", "joao@email.com", "hash");
        Account account = new Account(99L, "0001", new BigDecimal("500.00"), user.id());
        OffsetDateTime date = OffsetDateTime.parse("2026-05-13T12:00:00Z");
        Transaction deposit = new Transaction(1L, null, account.id(), TransactionType.DEPOSIT,
                new BigDecimal("100.00"), date);
        Transaction withdraw = new Transaction(2L, account.id(), null, TransactionType.WITHDRAW,
                new BigDecimal("50.00"), date.plusMinutes(1));
        when(currentUser.required(principal)).thenReturn(user);
        when(accountService.getAccount(user.id())).thenReturn(account);
        when(transactionRepository.findByAccountId(account.id())).thenReturn(List.of(deposit, withdraw));

        String view = controller.statement(principal, model);

        assertEquals("statement", view);
        verify(currentUser).required(principal);
        verify(accountService).getAccount(user.id());
        verify(transactionRepository).findByAccountId(account.id());
        verify(model).addAttribute("account", account);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<StatementLine>> linesCaptor = ArgumentCaptor.forClass(List.class);
        verify(model).addAttribute(eq("lines"), linesCaptor.capture());
        List<StatementLine> lines = linesCaptor.getValue();
        assertEquals(2, lines.size());
        assertEquals("Depósito realizado", lines.get(0).description());
        assertEquals("Saque efetuado", lines.get(1).description());
        assertEquals(new BigDecimal("100.00"), lines.get(0).amount());
        assertEquals(new BigDecimal("50.00"), lines.get(1).amount());
    }

    @Test
    void statementHandlesEmptyTransactionList() {
        User user = new User(7L, "Joao", "joao@email.com", "hash");
        Account account = new Account(99L, "0001", new BigDecimal("500.00"), user.id());
        when(currentUser.required(principal)).thenReturn(user);
        when(accountService.getAccount(user.id())).thenReturn(account);
        when(transactionRepository.findByAccountId(account.id())).thenReturn(List.of());

        String view = controller.statement(principal, model);

        assertEquals("statement", view);
        verify(model).addAttribute("account", account);
        verify(model).addAttribute("lines", List.of());
    }
}
