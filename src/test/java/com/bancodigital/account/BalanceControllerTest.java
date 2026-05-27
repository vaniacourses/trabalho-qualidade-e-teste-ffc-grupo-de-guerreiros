package com.bancodigital.account;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;

import com.bancodigital.auth.CurrentUser;
import com.bancodigital.auth.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceControllerTest {

    @Mock AccountService accountService;
    @Mock CurrentUser currentUser;
    @Mock UserDetails principal;
    @Mock Model model;

    private BalanceController controller;

    @BeforeEach
    void setUp() {
        controller = new BalanceController(accountService, currentUser);
    }

    @Test
    void balanceReturnsViewWithCurrentUserAccount() {
        User user = new User(7L, "Joao", "joao@email.com", "hash");
        Account account = new Account(99L, "0001", new BigDecimal("500.00"), user.id());
        when(currentUser.required(principal)).thenReturn(user);
        when(accountService.getAccount(user.id())).thenReturn(account);

        String view = controller.balance(principal, model);

        assertEquals("balance", view);
        verify(currentUser).required(principal);
        verify(accountService).getAccount(user.id());
        verify(model).addAttribute("account", account);
    }
}
