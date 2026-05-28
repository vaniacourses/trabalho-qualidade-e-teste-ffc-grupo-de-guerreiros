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
    private static final long SOURCE_ACCOUNT_ID = 10L; // ID menor
    private static final long DEST_ACCOUNT_ID = 20L;   // ID maior

    // 1. Criando os Clones (Mocks)
    @Mock AccountRepository accountRepository;
    @Mock TransactionRepository transactionRepository;

    private AccountService service;

    // 2. Injetando os Clones no Serviço Real
    @BeforeEach
    void setUp() {
        service = new AccountService(accountRepository, transactionRepository);
    }

    // Tradutor de Dinheiro (Helper)
    private BigDecimal bd(String s) { return new BigDecimal(s); }

    // ------------------------------------------------------------
    // CENÁRIOS DE TESTE COM MOCKITO
    // ------------------------------------------------------------

    @Test
    void transferHappyPath() {
        // PREPARAÇÃO: Ensinando o clone a mentir
        Account source = new Account(SOURCE_ACCOUNT_ID, "C001", bd("500.00"), SOURCE_USER_ID);
        Account dest = new Account(DEST_ACCOUNT_ID, "C002", bd("100.00"), 2L);

        when(accountRepository.findByUserId(SOURCE_USER_ID)).thenReturn(Optional.of(source));
        when(accountRepository.findByNumber("C002")).thenReturn(Optional.of(dest));
        when(accountRepository.findByIdForUpdate(SOURCE_ACCOUNT_ID)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(DEST_ACCOUNT_ID)).thenReturn(Optional.of(dest));

        // AÇÃO: O Controller mandou transferir R$ 100
        service.transfer(SOURCE_USER_ID, "C002", bd("100.00"));

        // INVESTIGAÇÃO: O banco não foi tocado, mas verificamos se os métodos certos foram chamados no clone
        verify(accountRepository).debit(SOURCE_ACCOUNT_ID, bd("100.00"));
        verify(accountRepository).credit(DEST_ACCOUNT_ID, bd("100.00"));
        verify(transactionRepository).recordTransfer(SOURCE_ACCOUNT_ID, DEST_ACCOUNT_ID, bd("100.00"));
    }

    @Test
    void transferEnforcesLockOrderToPreventDeadlock() {
        // PREPARAÇÃO: Contas iguais ao caminho feliz
        Account source = new Account(SOURCE_ACCOUNT_ID, "C001", bd("500.00"), SOURCE_USER_ID);
        Account dest = new Account(DEST_ACCOUNT_ID, "C002", bd("100.00"), 2L);

        when(accountRepository.findByUserId(SOURCE_USER_ID)).thenReturn(Optional.of(source));
        when(accountRepository.findByNumber("C002")).thenReturn(Optional.of(dest));
        when(accountRepository.findByIdForUpdate(SOURCE_ACCOUNT_ID)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(DEST_ACCOUNT_ID)).thenReturn(Optional.of(dest));

        // AÇÃO
        service.transfer(SOURCE_USER_ID, "C002", bd("100.00"));

        // INVESTIGAÇÃO: Aqui provamos que o Math.min trancou o ID 10 antes do ID 20!
        InOrder inOrder = inOrder(accountRepository);
        inOrder.verify(accountRepository).findByIdForUpdate(SOURCE_ACCOUNT_ID);
        inOrder.verify(accountRepository).findByIdForUpdate(DEST_ACCOUNT_ID);
    }

    @Test
    void transferRejectsInsufficientBalance() {
        // PREPARAÇÃO: Conta origem só tem 10 reais
        Account source = new Account(SOURCE_ACCOUNT_ID, "C001", bd("10.00"), SOURCE_USER_ID);
        Account dest = new Account(DEST_ACCOUNT_ID, "C002", bd("100.00"), 2L);

        when(accountRepository.findByUserId(SOURCE_USER_ID)).thenReturn(Optional.of(source));
        when(accountRepository.findByNumber("C002")).thenReturn(Optional.of(dest));
        when(accountRepository.findByIdForUpdate(SOURCE_ACCOUNT_ID)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(DEST_ACCOUNT_ID)).thenReturn(Optional.of(dest));

        // AÇÃO E VERIFICAÇÃO: Tenta transferir R$ 50 e garante que a exceção estourou
        DomainException ex = assertThrows(DomainException.class, 
            () -> service.transfer(SOURCE_USER_ID, "C002", bd("50.00")));
        
        assertEquals(Messages.INSUFFICIENT_BALANCE, ex.getMessage());
        
        // Garante que o dinheiro de ninguém sumiu por acidente
        verify(accountRepository, never()).debit(anyLong(), any());
        verify(accountRepository, never()).credit(anyLong(), any());
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transferThrowsWhenSourceAccountMissing() {
        // O usuário logado simplesmente não existe no banco
        when(accountRepository.findByUserId(SOURCE_USER_ID)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, 
            () -> service.transfer(SOURCE_USER_ID, "C002", bd("100.00")));
        
        verifyNoInteractions(transactionRepository);
    }

    
}