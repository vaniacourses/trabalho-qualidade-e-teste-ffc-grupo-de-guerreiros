package com.bancodigital.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bancodigital.auth.CurrentUser;
import com.bancodigital.auth.User;

@ExtendWith(MockitoExtension.class)
class DepositControllerTest {

    @Mock AccountService accountService;
    @Mock CurrentUser currentUser;
    @Mock UserDetails principal;
    @Mock RedirectAttributes redirectAttributes;


    private DepositController controller;

    @BeforeEach
    void setup(){
        controller = new DepositController(accountService,currentUser);
    }

    // Os tres valores percorrem o mesmo contrato do controller; parametrizar
    // evita duplicacao e mantem cada entrada identificada no relatorio JUnit.
    @ParameterizedTest
    @ValueSource(strings = {"500.00", "0.0", "-100.00"})
    void depositAlwaysRedirectsToForm(String amount){
        User user = new User(7L, "Joao", "joao@email.com", "hash");
        when(currentUser.required(principal)).thenReturn(user);

        assertEquals("redirect:/deposit", controller.submit(principal, amount, redirectAttributes));
    }

}
