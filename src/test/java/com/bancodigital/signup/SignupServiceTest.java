package com.bancodigital.signup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bancodigital.account.AccountRepository;
import com.bancodigital.auth.UserRepository;
import com.bancodigital.shared.Messages;
import com.bancodigital.shared.exception.DomainException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignupServiceTest {

    @Mock UserRepository userRepository;
    @Mock AccountRepository accountRepository;
    @Mock PasswordEncoder passwordEncoder;

    private SignupService service;

    @BeforeEach
    void setUp() {
        service = new SignupService(userRepository, accountRepository, passwordEncoder);
    }

    private SignupForm form(String name, String email, String password) {
        SignupForm f = new SignupForm();
        f.setName(name);
        f.setEmail(email);
        f.setPassword(password);
        return f;
    }

    // ------------------------------------------------------------
    // validateSignup (puro)
    // ------------------------------------------------------------

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

    // ------------------------------------------------------------
    // register() — isolado via Mockito
    // ------------------------------------------------------------

    @Test
    void registerHappyPath() {
        when(userRepository.existsByEmail("joao@email.com")).thenReturn(false);
        when(passwordEncoder.encode("12345678")).thenReturn("hashed");
        when(userRepository.save("João", "joao@email.com", "hashed")).thenReturn(42L);
        when(accountRepository.nextAccountNumber()).thenReturn("0001");

        service.register(form("João", "joao@email.com", "12345678"));

        InOrder order = inOrder(userRepository, passwordEncoder, accountRepository);
        order.verify(userRepository).existsByEmail("joao@email.com");
        order.verify(passwordEncoder).encode("12345678");
        order.verify(userRepository).save("João", "joao@email.com", "hashed");
        order.verify(accountRepository).nextAccountNumber();
        order.verify(accountRepository).insert("0001", 42L);
    }

    @Test
    void registerRejectsInvalidForm() {
        DomainException ex = assertThrows(DomainException.class,
                () -> service.register(form("", "joao@email.com", "12345678")));

        assertEquals(Messages.INVALID_NAME, ex.getMessage());
        verifyNoInteractions(userRepository, accountRepository, passwordEncoder);
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmail("joao@email.com")).thenReturn(true);

        DomainException ex = assertThrows(DomainException.class,
                () -> service.register(form("João", "joao@email.com", "12345678")));

        assertEquals(Messages.DUPLICATE_EMAIL, ex.getMessage());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(anyString(), anyString(), anyString());
        verify(accountRepository, never()).nextAccountNumber();
        verify(accountRepository, never()).insert(anyString(), anyLong());
    }

    @Test
    void registerTrimsEmailAndName() {
        when(userRepository.existsByEmail("joao@email.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any(), any(), any())).thenReturn(1L);
        when(accountRepository.nextAccountNumber()).thenReturn("0001");

        service.register(form("  João  ", "  joao@email.com  ", "12345678"));

        verify(userRepository).existsByEmail("joao@email.com");
        verify(userRepository).save("João", "joao@email.com", "hashed");
    }

    @Test
    void registerEncodesPasswordBeforeSaving() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("12345678")).thenReturn("ENC_SENHA");
        when(userRepository.save(any(), any(), any())).thenReturn(1L);
        when(accountRepository.nextAccountNumber()).thenReturn("0001");

        service.register(form("João", "joao@email.com", "12345678"));

        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        verify(userRepository).save(any(), any(), hashCaptor.capture());
        assertEquals("ENC_SENHA", hashCaptor.getValue());
    }

    @Test
    void registerGeneratesSequentialAccountNumbers() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any(), any(), any())).thenReturn(1L, 2L);
        when(accountRepository.nextAccountNumber()).thenReturn("0001", "0002");

        service.register(form("João", "joao@email.com", "12345678"));
        service.register(form("Maria", "maria@email.com", "12345678"));

        ArgumentCaptor<String> numberCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        verify(accountRepository, org.mockito.Mockito.times(2))
                .insert(numberCaptor.capture(), userIdCaptor.capture());
        assertEquals("0001", numberCaptor.getAllValues().get(0));
        assertEquals("0002", numberCaptor.getAllValues().get(1));
        assertEquals(1L, userIdCaptor.getAllValues().get(0));
        assertEquals(2L, userIdCaptor.getAllValues().get(1));
    }
}
