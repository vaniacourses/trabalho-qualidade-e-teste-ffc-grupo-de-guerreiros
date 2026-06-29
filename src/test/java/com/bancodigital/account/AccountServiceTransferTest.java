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
import org.mockito.InOrder;
import org.mockito.Mock;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bancodigital.shared.Messages;
import com.bancodigital.shared.exception.DomainException;
import com.bancodigital.transaction.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class AccountServiceTransferTest {

    private static final long SOURCE_USER_ID = 1L;
    private static final long SOURCE_ACCOUNT_ID = 10L;
    private static final long DEST_ACCOUNT_ID = 20L;   

    @Mock AccountRepository accountRepository;
    @Mock TransactionRepository transactionRepository;

    private AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService(accountRepository, transactionRepository);
    }
    private BigDecimal bd(String s) { return new BigDecimal(s); }

    // O valor e convertido antes da lambda para que o assertThrows contenha
    // somente a chamada cuja DomainException realmente esta sendo verificada.
    private DomainException assertTransferThrows(String destination, String amount) {
        BigDecimal parsedAmount = bd(amount);
        return assertThrows(DomainException.class,
                () -> service.transfer(SOURCE_USER_ID, destination, parsedAmount));
    }
    @Test
    void transferHappyPath() {
        Account source = new Account(SOURCE_ACCOUNT_ID, "C001", bd("500.00"), SOURCE_USER_ID);
        Account dest = new Account(DEST_ACCOUNT_ID, "C002", bd("100.00"), 2L);

        when(accountRepository.findByUserId(SOURCE_USER_ID)).thenReturn(Optional.of(source));
        when(accountRepository.findByNumber("C002")).thenReturn(Optional.of(dest));
        when(accountRepository.findByIdForUpdate(SOURCE_ACCOUNT_ID)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(DEST_ACCOUNT_ID)).thenReturn(Optional.of(dest));
        service.transfer(SOURCE_USER_ID, "C002", bd("100.00"));

        verify(accountRepository).debit(SOURCE_ACCOUNT_ID, bd("100.00"));
        verify(accountRepository).credit(DEST_ACCOUNT_ID, bd("100.00"));
        verify(transactionRepository).recordTransfer(SOURCE_ACCOUNT_ID, DEST_ACCOUNT_ID, bd("100.00"));
    }

    @Test
    void transferEnforcesLockOrderToPreventDeadlock() {
        Account source = new Account(SOURCE_ACCOUNT_ID, "C001", bd("500.00"), SOURCE_USER_ID);
        Account dest = new Account(DEST_ACCOUNT_ID, "C002", bd("100.00"), 2L);

        when(accountRepository.findByUserId(SOURCE_USER_ID)).thenReturn(Optional.of(source));
        when(accountRepository.findByNumber("C002")).thenReturn(Optional.of(dest));
        when(accountRepository.findByIdForUpdate(SOURCE_ACCOUNT_ID)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(DEST_ACCOUNT_ID)).thenReturn(Optional.of(dest));

        service.transfer(SOURCE_USER_ID, "C002", bd("100.00"));

        InOrder inOrder = inOrder(accountRepository);
        inOrder.verify(accountRepository).findByIdForUpdate(SOURCE_ACCOUNT_ID);
        inOrder.verify(accountRepository).findByIdForUpdate(DEST_ACCOUNT_ID);
    }

    @Test
    void transferRejectsInsufficientBalance() {
        Account source = new Account(SOURCE_ACCOUNT_ID, "C001", bd("10.00"), SOURCE_USER_ID);
        Account dest = new Account(DEST_ACCOUNT_ID, "C002", bd("100.00"), 2L);

        when(accountRepository.findByUserId(SOURCE_USER_ID)).thenReturn(Optional.of(source));
        when(accountRepository.findByNumber("C002")).thenReturn(Optional.of(dest));
        
        DomainException ex = assertTransferThrows("C002", "50.00");
        
        assertEquals(Messages.INSUFFICIENT_BALANCE, ex.getMessage());
        
        verify(accountRepository, never()).debit(anyLong(), any());
        verify(accountRepository, never()).credit(anyLong(), any());
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transferThrowsWhenSourceAccountMissing() {
        when(accountRepository.findByUserId(SOURCE_USER_ID)).thenReturn(Optional.empty());

        assertTransferThrows("C002", "100.00");
        
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transferThrowsWhenDestinationMissing() {
        Account source = new Account(SOURCE_ACCOUNT_ID, "C001", bd("500.00"), SOURCE_USER_ID);
        when(accountRepository.findByUserId(SOURCE_USER_ID)).thenReturn(Optional.of(source));
        
        when(accountRepository.findByNumber("C999")).thenReturn(Optional.empty());

        DomainException ex = assertTransferThrows("C999", "100.00");

        assertEquals(Messages.INVALID_DESTINATION_ACCOUNT, ex.getMessage());
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transferToSameAccountThrowsException() {
        Account source = new Account(SOURCE_ACCOUNT_ID, "C001", bd("500.00"), SOURCE_USER_ID);
        when(accountRepository.findByUserId(SOURCE_USER_ID)).thenReturn(Optional.of(source));

        DomainException ex = assertTransferThrows("C001", "100.00");

        assertEquals(Messages.SAME_ACCOUNT, ex.getMessage());
        verify(accountRepository, never()).findByIdForUpdate(anyLong());
    }

    @Test
    void transferNormalizesAmountToScaleTwo() {
        Account source = new Account(SOURCE_ACCOUNT_ID, "C001", bd("500.00"), SOURCE_USER_ID);
        Account dest = new Account(DEST_ACCOUNT_ID, "C002", bd("100.00"), 2L);

        when(accountRepository.findByUserId(SOURCE_USER_ID)).thenReturn(Optional.of(source));
        when(accountRepository.findByNumber("C002")).thenReturn(Optional.of(dest));
        when(accountRepository.findByIdForUpdate(SOURCE_ACCOUNT_ID)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(DEST_ACCOUNT_ID)).thenReturn(Optional.of(dest));

        service.transfer(SOURCE_USER_ID, "C002", bd("100.999"));

        verify(accountRepository).debit(SOURCE_ACCOUNT_ID, bd("101.00"));
        verify(accountRepository).credit(DEST_ACCOUNT_ID, bd("101.00"));
    }

    @Test
    void transferReverseLockOrderToPreventDeadlock() {
        long reverseSourceId = 30L;
        Account source = new Account(reverseSourceId, "C001", bd("500.00"), SOURCE_USER_ID);
        Account dest = new Account(SOURCE_ACCOUNT_ID, "C002", bd("100.00"), 2L);

        when(accountRepository.findByUserId(SOURCE_USER_ID)).thenReturn(Optional.of(source));
        when(accountRepository.findByNumber("C002")).thenReturn(Optional.of(dest));
        when(accountRepository.findByIdForUpdate(reverseSourceId)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(SOURCE_ACCOUNT_ID)).thenReturn(Optional.of(dest));

        service.transfer(SOURCE_USER_ID, "C002", bd("100.00"));

        InOrder inOrder = inOrder(accountRepository);
        inOrder.verify(accountRepository).findByIdForUpdate(SOURCE_ACCOUNT_ID);
        inOrder.verify(accountRepository).findByIdForUpdate(reverseSourceId);
    }

    @Test
    void transferRejectsNegativeAmount() {
        Account source = new Account(SOURCE_ACCOUNT_ID, "C001", bd("500.00"), SOURCE_USER_ID);
        Account dest = new Account(DEST_ACCOUNT_ID, "C002", bd("100.00"), 2L);

        when(accountRepository.findByUserId(SOURCE_USER_ID)).thenReturn(Optional.of(source));
        when(accountRepository.findByNumber("C002")).thenReturn(Optional.of(dest));

        DomainException ex = assertTransferThrows("C002", "-50.00");
        
        assertEquals(Messages.INVALID_AMOUNT_OR_ACCOUNT, ex.getMessage());
        
        verify(accountRepository, never()).debit(anyLong(), any());
        verifyNoInteractions(transactionRepository);
    }
    @Test
    void transferThrowsWhenDestinationIsNull() {
        Account source = new Account(SOURCE_ACCOUNT_ID, "C001", bd("500.00"), SOURCE_USER_ID);
        when(accountRepository.findByUserId(SOURCE_USER_ID)).thenReturn(Optional.of(source));
        
        DomainException ex = assertTransferThrows(null, "100.00");
        assertEquals(Messages.INVALID_AMOUNT_OR_ACCOUNT, ex.getMessage());
    }

    @Test
    void transferThrowsWhenDestinationIsEmpty() {
        Account source = new Account(SOURCE_ACCOUNT_ID, "C001", bd("500.00"), SOURCE_USER_ID);
        when(accountRepository.findByUserId(SOURCE_USER_ID)).thenReturn(Optional.of(source));
        
        DomainException ex = assertTransferThrows("   ", "100.00");
        assertEquals(Messages.INVALID_AMOUNT_OR_ACCOUNT, ex.getMessage());
    }

    @Test
    void transferThrowsWhenSourceBalanceIsNull() {
        Account source = new Account(SOURCE_ACCOUNT_ID, "C001", null, SOURCE_USER_ID);
        Account dest = new Account(DEST_ACCOUNT_ID, "C002", bd("100.00"), 2L);

        when(accountRepository.findByUserId(SOURCE_USER_ID)).thenReturn(Optional.of(source));
        when(accountRepository.findByNumber("C002")).thenReturn(Optional.of(dest));

        DomainException ex = assertTransferThrows("C002", "50.00");
        assertEquals(Messages.INSUFFICIENT_BALANCE, ex.getMessage());
    }
    
}
