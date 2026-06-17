package com.bancodigital.account;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bancodigital.shared.Messages;
import com.bancodigital.shared.exception.DomainException;
import com.bancodigital.transaction.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class AccountServiceWithdrawTest {

    private static final long USER_ID = 1L;
    private static final long ACCOUNT_ID = 10L;

    @Mock AccountRepository accountRepository;
    @Mock TransactionRepository transactionRepository;

    private AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService(accountRepository, transactionRepository);
    }

    private BigDecimal bd(String s) { return new BigDecimal(s); }

    @Test
    void withdrawHappyPath() {
        Account account = new Account(ACCOUNT_ID, "C001", bd("500.00"), USER_ID);
        when(accountRepository.findByUserId(USER_ID)).thenReturn(Optional.of(account));
        when(accountRepository.findByIdForUpdate(ACCOUNT_ID)).thenReturn(Optional.of(account));

        service.withdraw(USER_ID, bd("100.00"));

        // Garante que o banco foi chamado para debitar e registrar a transação
        verify(accountRepository).debit(ACCOUNT_ID, bd("100.00"));
        verify(transactionRepository).recordWithdraw(ACCOUNT_ID, bd("100.00"));
    }

    @Test
    void withdrawRejectsInsufficientBalance() {
        Account account = new Account(ACCOUNT_ID, "C001", bd("50.00"), USER_ID);
        when(accountRepository.findByUserId(USER_ID)).thenReturn(Optional.of(account));
        when(accountRepository.findByIdForUpdate(ACCOUNT_ID)).thenReturn(Optional.of(account));

        DomainException ex = assertThrows(DomainException.class, 
            () -> service.withdraw(USER_ID, bd("100.00")));
        
        assertEquals(Messages.INSUFFICIENT_BALANCE, ex.getMessage());
        // Garante que o débito NUNCA foi chamado (Rollback)
        verify(accountRepository, never()).debit(anyLong(), any());
    }

    @Test
    void withdrawRejectsOverDailyLimit() {
        Account account = new Account(ACCOUNT_ID, "C001", bd("50000.00"), USER_ID);
        when(accountRepository.findByUserId(USER_ID)).thenReturn(Optional.of(account));
        when(accountRepository.findByIdForUpdate(ACCOUNT_ID)).thenReturn(Optional.of(account));

        // Tenta sacar 1 centavo a mais que o limite diário de 10.000,00
        DomainException ex = assertThrows(DomainException.class, 
            () -> service.withdraw(USER_ID, bd("10000.01")));
        
        assertEquals(Messages.WITHDRAW_LIMIT_EXCEEDED, ex.getMessage());
        verify(accountRepository, never()).debit(anyLong(), any());
    }

    @Test
    void withdrawRejectsNegativeAmount() {
        Account account = new Account(ACCOUNT_ID, "C001", bd("500.00"), USER_ID);
        when(accountRepository.findByUserId(USER_ID)).thenReturn(Optional.of(account));
        when(accountRepository.findByIdForUpdate(ACCOUNT_ID)).thenReturn(Optional.of(account));

        DomainException ex = assertThrows(DomainException.class, 
            () -> service.withdraw(USER_ID, bd("-50.00")));
        
        assertEquals(Messages.INVALID_AMOUNT, ex.getMessage());
        // Garante que nenhuma transação falsa foi registrada
        verifyNoInteractions(transactionRepository);
    }
}