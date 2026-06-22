package com.bancodigital.account;

import com.bancodigital.auth.CurrentUser;
import com.bancodigital.auth.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DepositControllerTest {

    @Mock AccountService accountService;
    @Mock CurrentUser currentUser;
    @Mock UserDetails principal;
    @Mock RedirectAttributes redirectAttributes;


    private DepositController controller;

    @BeforeEach
    public void setup(){
        controller = new DepositController(accountService,currentUser);
    }

    @Test
    public void DepositHappyPath(){
        User user = new User(7L, "Joao", "joao@email.com", "hash");
        when(currentUser.required(principal)).thenReturn(user);

        assertEquals("redirect:/deposit", controller.submit(principal, "500.00", redirectAttributes));

    }

    @Test
    public void DepositZero(){
        User user = new User(7L, "João", "joao@email.com", "hash");
        when (currentUser.required(principal)).thenReturn(user);

        assertEquals("redirect:/deposit",controller.submit(principal, "0.0", redirectAttributes));
    }

    @Test
    public void DepositInvalid(){
        User user = new User(7L, "João", "joao@email.com", "hash");
        when (currentUser.required(principal)).thenReturn(user);

        assertEquals("redirect:/deposit",controller.submit(principal, "-100.00", redirectAttributes));

    }

}
