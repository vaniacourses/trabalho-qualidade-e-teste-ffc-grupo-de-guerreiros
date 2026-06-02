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

    // Mocks das dependências externas
    @Mock AccountRepository accountRepository;
    @Mock TransactionRepository transactionRepository;

    private AccountService service;

    // Configuração inicial instanciando o serviço com os mocks
    @BeforeEach
    void setUp() {
        service = new AccountService(accountRepository, transactionRepository);
    }

    // Método utilitário para facilitar a criação de instâncias de BigDecimal
    private BigDecimal bd(String s) { return new BigDecimal(s); }

    // ------------------------------------------------------------
    // Testes de Transferência
    // ------------------------------------------------------------

    @Test
    void transferHappyPath() {
        // Arrange: Configura cenário base com contas válidas e saldos suficientes
        Account source = new Account(SOURCE_ACCOUNT_ID, "C001", bd("500.00"), SOURCE_USER_ID);
        Account dest = new Account(DEST_ACCOUNT_ID, "C002", bd("100.00"), 2L);

        when(accountRepository.findByUserId(SOURCE_USER_ID)).thenReturn(Optional.of(source));
        when(accountRepository.findByNumber("C002")).thenReturn(Optional.of(dest));
        when(accountRepository.findByIdForUpdate(SOURCE_ACCOUNT_ID)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(DEST_ACCOUNT_ID)).thenReturn(Optional.of(dest));

        // Act: Executa a transferência de um valor válido
        service.transfer(SOURCE_USER_ID, "C002", bd("100.00"));

        // Assert: Verifica se o débito, crédito e registro da transação foram processados corretamente
        verify(accountRepository).debit(SOURCE_ACCOUNT_ID, bd("100.00"));
        verify(accountRepository).credit(DEST_ACCOUNT_ID, bd("100.00"));
        verify(transactionRepository).recordTransfer(SOURCE_ACCOUNT_ID, DEST_ACCOUNT_ID, bd("100.00"));
    }

    @Test
    void transferEnforcesLockOrderToPreventDeadlock() {
        // Arrange: Contas configuradas de modo que a conta de origem tenha ID menor que a de destino
        Account source = new Account(SOURCE_ACCOUNT_ID, "C001", bd("500.00"), SOURCE_USER_ID);
        Account dest = new Account(DEST_ACCOUNT_ID, "C002", bd("100.00"), 2L);

        when(accountRepository.findByUserId(SOURCE_USER_ID)).thenReturn(Optional.of(source));
        when(accountRepository.findByNumber("C002")).thenReturn(Optional.of(dest));
        when(accountRepository.findByIdForUpdate(SOURCE_ACCOUNT_ID)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(DEST_ACCOUNT_ID)).thenReturn(Optional.of(dest));

        // Act
        service.transfer(SOURCE_USER_ID, "C002", bd("100.00"));

        // Assert: Garante que os registros foram travados sequencialmente do menor ID para o maior
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
        
        // AÇÃO E VERIFICAÇÃO
        DomainException ex = assertThrows(DomainException.class, 
            () -> service.transfer(SOURCE_USER_ID, "C002", bd("50.00")));
        
        assertEquals(Messages.INSUFFICIENT_BALANCE, ex.getMessage());
        
        verify(accountRepository, never()).debit(anyLong(), any());
        verify(accountRepository, never()).credit(anyLong(), any());
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transferThrowsWhenSourceAccountMissing() {
        // Arrange: Simula cenário onde a conta do usuário que iniciou a transferência não é encontrada
        when(accountRepository.findByUserId(SOURCE_USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DomainException.class, 
            () -> service.transfer(SOURCE_USER_ID, "C002", bd("100.00")));
        
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transferThrowsWhenDestinationMissing() {
        // Arrange: Simula cenário com origem válida, mas conta de destino inexistente
        Account source = new Account(SOURCE_ACCOUNT_ID, "C001", bd("500.00"), SOURCE_USER_ID);
        when(accountRepository.findByUserId(SOURCE_USER_ID)).thenReturn(Optional.of(source));
        
        when(accountRepository.findByNumber("C999")).thenReturn(Optional.empty());

        // Act & Assert
        DomainException ex = assertThrows(DomainException.class, 
            () -> service.transfer(SOURCE_USER_ID, "C999", bd("100.00")));

        assertEquals(Messages.INVALID_DESTINATION_ACCOUNT, ex.getMessage());
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transferToSameAccountThrowsException() {
        // Arrange: Configura a mesma conta para origem e destino
        Account source = new Account(SOURCE_ACCOUNT_ID, "C001", bd("500.00"), SOURCE_USER_ID);
        when(accountRepository.findByUserId(SOURCE_USER_ID)).thenReturn(Optional.of(source));

        // Act & Assert: Garante bloqueio ao transferir para a própria conta
        DomainException ex = assertThrows(DomainException.class, 
            () -> service.transfer(SOURCE_USER_ID, "C001", bd("100.00")));

        assertEquals(Messages.SAME_ACCOUNT, ex.getMessage());
        verify(accountRepository, never()).findByIdForUpdate(anyLong());
    }

    @Test
    void transferNormalizesAmountToScaleTwo() {
        // Arrange: Contas válidas com saldos suficientes
        Account source = new Account(SOURCE_ACCOUNT_ID, "C001", bd("500.00"), SOURCE_USER_ID);
        Account dest = new Account(DEST_ACCOUNT_ID, "C002", bd("100.00"), 2L);

        when(accountRepository.findByUserId(SOURCE_USER_ID)).thenReturn(Optional.of(source));
        when(accountRepository.findByNumber("C002")).thenReturn(Optional.of(dest));
        when(accountRepository.findByIdForUpdate(SOURCE_ACCOUNT_ID)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(DEST_ACCOUNT_ID)).thenReturn(Optional.of(dest));

        // Act: Envia um valor com precisão maior que a suportada (ex: 3 casas decimais)
        service.transfer(SOURCE_USER_ID, "C002", bd("100.999"));

        // Assert: Garante o arredondamento (ex: de 100.999 para 101.00) antes da persistência
        verify(accountRepository).debit(SOURCE_ACCOUNT_ID, bd("101.00"));
        verify(accountRepository).credit(DEST_ACCOUNT_ID, bd("101.00"));
    }

    @Test
    void transferReverseLockOrderToPreventDeadlock() {
        // Arrange: Configura a conta de origem com um ID maior que o da conta de destino
        long reverseSourceId = 30L;
        Account source = new Account(reverseSourceId, "C001", bd("500.00"), SOURCE_USER_ID);
        Account dest = new Account(SOURCE_ACCOUNT_ID, "C002", bd("100.00"), 2L); // ID 10

        when(accountRepository.findByUserId(SOURCE_USER_ID)).thenReturn(Optional.of(source));
        when(accountRepository.findByNumber("C002")).thenReturn(Optional.of(dest));
        when(accountRepository.findByIdForUpdate(reverseSourceId)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(SOURCE_ACCOUNT_ID)).thenReturn(Optional.of(dest));

        // Act
        service.transfer(SOURCE_USER_ID, "C002", bd("100.00"));

        // Assert: A ordem dos locks (via findByIdForUpdate) sempre deve privilegiar o ID numérico menor
        InOrder inOrder = inOrder(accountRepository);
        inOrder.verify(accountRepository).findByIdForUpdate(SOURCE_ACCOUNT_ID);
        inOrder.verify(accountRepository).findByIdForUpdate(reverseSourceId);
    }

    @Test
    void transferRejectsNegativeAmount() {
        // PREPARAÇÃO: Precisamos ensinar o Mockito que a conta existe primeiro!
        Account source = new Account(SOURCE_ACCOUNT_ID, "C001", bd("500.00"), SOURCE_USER_ID);
        Account dest = new Account(DEST_ACCOUNT_ID, "C002", bd("100.00"), 2L);

        when(accountRepository.findByUserId(SOURCE_USER_ID)).thenReturn(Optional.of(source));
        when(accountRepository.findByNumber("C002")).thenReturn(Optional.of(dest));

        // AÇÃO E VERIFICAÇÃO: Tentar transferir R$ -50.00
        DomainException ex = assertThrows(DomainException.class, 
            () -> service.transfer(SOURCE_USER_ID, "C002", bd("-50.00")));
        
        assertEquals(Messages.INVALID_AMOUNT_OR_ACCOUNT, ex.getMessage());
        
        verify(accountRepository, never()).debit(anyLong(), any());
        verifyNoInteractions(transactionRepository);
    }
    @Test
    void transferThrowsWhenDestinationIsNull() {
        // Ensinando o mock que a conta origem existe
        Account source = new Account(SOURCE_ACCOUNT_ID, "C001", bd("500.00"), SOURCE_USER_ID);
        when(accountRepository.findByUserId(SOURCE_USER_ID)).thenReturn(Optional.of(source));
        
        // Ação: Mandar null no destino
        DomainException ex = assertThrows(DomainException.class, 
            () -> service.transfer(SOURCE_USER_ID, null, bd("100.00")));
        assertEquals(Messages.INVALID_AMOUNT_OR_ACCOUNT, ex.getMessage());
    }

    @Test
    void transferThrowsWhenDestinationIsEmpty() {
        // Ação: Mandar espaços em branco no destino
        Account source = new Account(SOURCE_ACCOUNT_ID, "C001", bd("500.00"), SOURCE_USER_ID);
        when(accountRepository.findByUserId(SOURCE_USER_ID)).thenReturn(Optional.of(source));
        
        DomainException ex = assertThrows(DomainException.class, 
            () -> service.transfer(SOURCE_USER_ID, "   ", bd("100.00")));
        assertEquals(Messages.INVALID_AMOUNT_OR_ACCOUNT, ex.getMessage());
    }

    @Test
    void transferThrowsWhenSourceBalanceIsNull() {
        // PREPARAÇÃO: Simulando um erro bizarro onde o banco traz saldo NULL
        Account source = new Account(SOURCE_ACCOUNT_ID, "C001", null, SOURCE_USER_ID);
        Account dest = new Account(DEST_ACCOUNT_ID, "C002", bd("100.00"), 2L);

        when(accountRepository.findByUserId(SOURCE_USER_ID)).thenReturn(Optional.of(source));
        when(accountRepository.findByNumber("C002")).thenReturn(Optional.of(dest));

        // AÇÃO E VERIFICAÇÃO
        DomainException ex = assertThrows(DomainException.class, 
            () -> service.transfer(SOURCE_USER_ID, "C002", bd("50.00")));
        assertEquals(Messages.INSUFFICIENT_BALANCE, ex.getMessage());
    }
    
}