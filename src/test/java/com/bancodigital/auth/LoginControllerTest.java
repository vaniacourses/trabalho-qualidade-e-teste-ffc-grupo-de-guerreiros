package com.bancodigital.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Mock Model model;

    private LoginController controller;

    @BeforeEach
    void setUp() {
        controller = new LoginController();
    }

    @Test
    void loginFormReturnsLoginViewWithoutMessages() {
        String view = controller.loginForm(null, null, null, model);

        assertEquals("login", view);
        verifyNoInteractions(model);
    }

    @Test
    void loginFormAddsErrorMessageWhenErrorParamExists() {
        String view = controller.loginForm("true", null, null, model);

        assertEquals("login", view);
        verify(model).addAttribute("error", "E-mail ou senha inválidos.");
        verify(model, never()).addAttribute("message", "Sessão encerrada.");
        verify(model, never()).addAttribute("message", "Cadastro realizado. Faça login para continuar.");
    }

    @Test
    void loginFormAddsLogoutMessageWhenLogoutParamExists() {
        String view = controller.loginForm(null, "true", null, model);

        assertEquals("login", view);
        verify(model).addAttribute("message", "Sessão encerrada.");
        verify(model, never()).addAttribute("error", "E-mail ou senha inválidos.");
        verify(model, never()).addAttribute("message", "Cadastro realizado. Faça login para continuar.");
    }

    @Test
    void loginFormAddsSignupMessageWhenSignupParamExists() {
        String view = controller.loginForm(null, null, "success", model);

        assertEquals("login", view);
        verify(model).addAttribute("message", "Cadastro realizado. Faça login para continuar.");
        verify(model, never()).addAttribute("error", "E-mail ou senha inválidos.");
        verify(model, never()).addAttribute("message", "Sessão encerrada.");
    }

    @Test
    void loginFormAddsErrorAndSignupMessagesWhenBothParamsExist() {
        String view = controller.loginForm("true", null, "success", model);

        assertEquals("login", view);
        verify(model).addAttribute("error", "E-mail ou senha inválidos.");
        verify(model).addAttribute("message", "Cadastro realizado. Faça login para continuar.");
    }

    @Test
    void loginFormAddsLogoutThenSignupMessageWhenBothParamsExist() {
        String view = controller.loginForm(null, "true", "success", model);

        assertEquals("login", view);
        verify(model).addAttribute("message", "Sessão encerrada.");
        verify(model).addAttribute("message", "Cadastro realizado. Faça login para continuar.");
    }
}
