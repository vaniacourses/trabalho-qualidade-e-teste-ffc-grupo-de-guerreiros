package com.bancodigital.signup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bancodigital.shared.Messages;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SignupServiceTest {

    private SignupService service;

    @BeforeEach
    void setUp() {
        service = new SignupService(null, null, null);
    }

    @Test
    void shouldReturnOkWhenAllFieldsAreFilled() {
        assertEquals("OK", service.validateSignup("João", "joao@email.com", "12345678"));
    }

    @Test
    void emptyName() {
        assertEquals(Messages.INVALID_NAME, service.validateSignup("", "joao@email.com", "12345678"));
    }

    @Test
    void nullName() {
        assertEquals(Messages.INVALID_NAME, service.validateSignup(null, "joao@email.com", "12345678"));
    }

    @Test
    void whitespaceOnlyName() {
        assertEquals(Messages.INVALID_NAME, service.validateSignup("   ", "joao@email.com", "12345678"));
    }

    @Test
    void emptyEmail() {
        assertEquals(Messages.INVALID_EMAIL, service.validateSignup("João", "", "12345678"));
    }

    @Test
    void nullEmail() {
        assertEquals(Messages.INVALID_EMAIL, service.validateSignup("João", null, "12345678"));
    }

    @Test
    void invalidEmailFormat() {
        assertEquals(Messages.INVALID_EMAIL, service.validateSignup("João", "joaoemail.com", "12345678"));
    }

    @Test
    void emailMissingDomain() {
        assertEquals(Messages.INVALID_EMAIL, service.validateSignup("João", "joao@", "12345678"));
    }

    @Test
    void shortPassword() {
        assertEquals(Messages.PASSWORD_TOO_SHORT, service.validateSignup("João", "joao@email.com", "1234567"));
    }

    @Test
    void emptyPassword() {
        assertEquals(Messages.PASSWORD_TOO_SHORT, service.validateSignup("João", "joao@email.com", ""));
    }

    @Test
    void nullPassword() {
        assertEquals(Messages.PASSWORD_TOO_SHORT, service.validateSignup("João", "joao@email.com", null));
    }

    @Test
    void passwordAtMinimumBoundary() {
        assertEquals("OK", service.validateSignup("João", "joao@email.com", "12345678"));
    }
}
