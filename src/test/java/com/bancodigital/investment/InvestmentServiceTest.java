package com.bancodigital.investment;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bancodigital.account.Account;
import com.bancodigital.account.AccountRepository;
import com.bancodigital.shared.Messages;
import com.bancodigital.shared.exception.DomainException;
import com.bancodigital.transaction.TransactionRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvestmentServiceTest {

    private static final long USER_ID = 7L;
    private static final long ACCOUNT_ID = 99L;
    private static final OffsetDateTime FIXED_NOW =
            OffsetDateTime.of(2026, 5, 13, 12, 0, 0, 0, ZoneOffset.UTC);

    @Mock InvestmentRepository investmentRepository;
    @Mock AccountRepository accountRepository;
    @Mock TransactionRepository transactionRepository;

    private InvestmentService service;
    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(FIXED_NOW.toInstant(), ZoneOffset.UTC);
        service = new InvestmentService(investmentRepository, accountRepository, transactionRepository, clock);
    }

    private BigDecimal bd(String s) { return new BigDecimal(s); }

    // ------------------------------------------------------------
    // calculateInterest (puro)
    // ------------------------------------------------------------

    @Test
    void unchangedAmountWhenMinutesIsZero() {
        assertEquals(0, service.calculateInterest(bd("100.00"), 0).compareTo(bd("100.00")));
    }

    @Test
    void appliesOnePercentInterestPerMinute() {
        assertEquals(0, service.calculateInterest(bd("100.00"), 1).compareTo(bd("101.00")));
    }

    @Test
    void appliesCompoundInterestOverFiveMinutes() {
        BigDecimal r = service.calculateInterest(bd("100.00"), 5);
        assertEquals(0, r.compareTo(bd("105.10")));
    }

    @Test
    void negativeMinutesKeepsAmount() {
        assertEquals(0, service.calculateInterest(bd("100.00"), -10).compareTo(bd("100.00")));
    }

    @Test
    void zeroAmountStaysZero() {
        assertEquals(0, service.calculateInterest(BigDecimal.ZERO, 60).compareTo(BigDecimal.ZERO));
    }

    // ------------------------------------------------------------
    // validateOperation (puro)
    // ------------------------------------------------------------

    @Test
    void rejectsInvalidOperation() {
        assertEquals(Messages.INVALID_OPERATION,
                service.validateOperation("transferir", bd("100"), bd("500"), bd("0")));
    }

    @Test
    void rejectsNullOperation() {
        assertEquals(Messages.INVALID_OPERATION,
                service.validateOperation(null, bd("100"), bd("500"), bd("0")));
    }

    @Test
    void rejectsInvestWithInsufficientBalance() {
        assertEquals(Messages.INSUFFICIENT_ACCOUNT_BALANCE,
                service.validateOperation("investir", bd("100"), bd("50"), bd("0")));
    }

    @Test
    void acceptsInvestAtExactBalance() {
        assertNull(service.validateOperation("investir", bd("100"), bd("100"), bd("0")));
    }

    @Test
    void rejectsWithdrawExceedingInvested() {
        assertEquals(Messages.AMOUNT_EXCEEDS_INVESTED,
                service.validateOperation("retirar", bd("100"), bd("500"), bd("50")));
    }

    @Test
    void acceptsWithdrawAtExactInvested() {
        assertNull(service.validateOperation("retirar", bd("100"), bd("500"), bd("100")));
    }

    @Test
    void rejectsInvestWithZeroAmount() {
        assertEquals(Messages.INVALID_AMOUNT,
                service.validateOperation("investir", BigDecimal.ZERO, bd("500"), bd("0")));
    }

    @Test
    void rejectsInvestWithNegativeAmount() {
        assertEquals(Messages.INVALID_AMOUNT,
                service.validateOperation("investir", bd("-10"), bd("500"), bd("0")));
    }

    @Test
    void rejectsInvestWithNullAmount() {
        assertEquals(Messages.INVALID_AMOUNT,
                service.validateOperation("investir", null, bd("500"), bd("0")));
    }

    // ------------------------------------------------------------
    // query() — isolado via Mockito
    // ------------------------------------------------------------

    @Test
    void queryReturnsAmountWhenLastUpdateIsNow() {
        Investment inv = new Investment(1L, USER_ID, bd("100.00"), FIXED_NOW);
        when(investmentRepository.findByUserId(USER_ID)).thenReturn(Optional.of(inv));

        BigDecimal result = service.query(USER_ID);

        assertEquals(0, result.compareTo(bd("100.00")));
        verify(investmentRepository, never()).update(anyLong(), any(), any());
    }

    @Test
    void queryThrowsWhenInvestmentMissing() {
        when(investmentRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> service.query(USER_ID));
    }

    @Test
    void queryAppliesInterestAfterFiveMinutes() {
        OffsetDateTime fiveMinAgo = FIXED_NOW.minusMinutes(5);
        Investment inv = new Investment(1L, USER_ID, bd("100.00"), fiveMinAgo);
        when(investmentRepository.findByUserId(USER_ID)).thenReturn(Optional.of(inv));

        BigDecimal result = service.query(USER_ID);

        assertEquals(0, result.compareTo(bd("105.10")));
        ArgumentCaptor<BigDecimal> amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(investmentRepository, times(1)).update(eq(USER_ID), amountCaptor.capture(), any());
        assertEquals(0, amountCaptor.getValue().compareTo(bd("105.10")));
    }

    @Test
    void querySkipsUpdateWhenDeltaIsNegative() {
        OffsetDateTime future = FIXED_NOW.plusMinutes(10);
        Investment inv = new Investment(1L, USER_ID, bd("100.00"), future);
        when(investmentRepository.findByUserId(USER_ID)).thenReturn(Optional.of(inv));

        BigDecimal result = service.query(USER_ID);

        assertEquals(0, result.compareTo(bd("100.00")));
        verify(investmentRepository, never()).update(anyLong(), any(), any());
    }

    // ------------------------------------------------------------
    // execute() invest — isolado via Mockito
    // ------------------------------------------------------------

    @Test
    void executeInvestHappyPath() {
        Account account = new Account(ACCOUNT_ID, "0001", bd("500.00"), USER_ID);
        Investment inv = new Investment(1L, USER_ID, bd("0.00"), FIXED_NOW);
        when(investmentRepository.findByUserId(USER_ID)).thenReturn(Optional.of(inv));
        when(accountRepository.findByUserId(USER_ID)).thenReturn(Optional.of(account));
        when(accountRepository.findByIdForUpdate(ACCOUNT_ID)).thenReturn(Optional.of(account));

        service.execute(USER_ID, "investir", bd("100.00"));

        verify(accountRepository).debit(ACCOUNT_ID, bd("100.00"));
        ArgumentCaptor<BigDecimal> investedCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(investmentRepository).update(eq(USER_ID), investedCaptor.capture(), any());
        assertEquals(0, investedCaptor.getValue().compareTo(bd("100.00")));
        verify(transactionRepository).recordInvestment(ACCOUNT_ID, bd("100.00"));
    }

    @Test
    void executeInvestRejectsInsufficientBalance() {
        Account account = new Account(ACCOUNT_ID, "0001", bd("50.00"), USER_ID);
        Investment inv = new Investment(1L, USER_ID, bd("0.00"), FIXED_NOW);
        when(investmentRepository.findByUserId(USER_ID)).thenReturn(Optional.of(inv));
        when(accountRepository.findByUserId(USER_ID)).thenReturn(Optional.of(account));
        when(accountRepository.findByIdForUpdate(ACCOUNT_ID)).thenReturn(Optional.of(account));

        DomainException ex = assertThrows(DomainException.class,
                () -> service.execute(USER_ID, "investir", bd("100.00")));

        assertEquals(Messages.INSUFFICIENT_ACCOUNT_BALANCE, ex.getMessage());
        verify(accountRepository, never()).debit(anyLong(), any());
        verify(transactionRepository, never()).recordInvestment(anyLong(), any());
    }

    @Test
    void executeInvestWithAccumulatedInterest() {
        OffsetDateTime fiveMinAgo = FIXED_NOW.minusMinutes(5);
        Account account = new Account(ACCOUNT_ID, "0001", bd("500.00"), USER_ID);
        Investment inv = new Investment(1L, USER_ID, bd("100.00"), fiveMinAgo);
        when(investmentRepository.findByUserId(USER_ID)).thenReturn(Optional.of(inv));
        when(accountRepository.findByUserId(USER_ID)).thenReturn(Optional.of(account));
        when(accountRepository.findByIdForUpdate(ACCOUNT_ID)).thenReturn(Optional.of(account));

        service.execute(USER_ID, "investir", bd("50.00"));

        ArgumentCaptor<BigDecimal> investedCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(investmentRepository, times(2)).update(eq(USER_ID), investedCaptor.capture(), any());
        BigDecimal afterInterest = investedCaptor.getAllValues().get(0);
        BigDecimal afterInvest = investedCaptor.getAllValues().get(1);
        assertEquals(0, afterInterest.compareTo(bd("105.10")));
        assertEquals(0, afterInvest.compareTo(bd("155.10")));
    }

    // ------------------------------------------------------------
    // execute() withdraw
    // ------------------------------------------------------------

    @Test
    void executeWithdrawHappyPath() {
        Account account = new Account(ACCOUNT_ID, "0001", bd("0.00"), USER_ID);
        Investment inv = new Investment(1L, USER_ID, bd("200.00"), FIXED_NOW);
        when(investmentRepository.findByUserId(USER_ID)).thenReturn(Optional.of(inv));
        when(accountRepository.findByUserId(USER_ID)).thenReturn(Optional.of(account));
        when(accountRepository.findByIdForUpdate(ACCOUNT_ID)).thenReturn(Optional.of(account));

        service.execute(USER_ID, "retirar", bd("100.00"));

        verify(accountRepository).credit(ACCOUNT_ID, bd("100.00"));
        ArgumentCaptor<BigDecimal> investedCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(investmentRepository).update(eq(USER_ID), investedCaptor.capture(), any());
        assertEquals(0, investedCaptor.getValue().compareTo(bd("100.00")));
        verify(transactionRepository).recordRedemption(ACCOUNT_ID, bd("100.00"));
    }

    @Test
    void executeWithdrawRejectsExceedingInvested() {
        Account account = new Account(ACCOUNT_ID, "0001", bd("0.00"), USER_ID);
        Investment inv = new Investment(1L, USER_ID, bd("50.00"), FIXED_NOW);
        when(investmentRepository.findByUserId(USER_ID)).thenReturn(Optional.of(inv));
        when(accountRepository.findByUserId(USER_ID)).thenReturn(Optional.of(account));
        when(accountRepository.findByIdForUpdate(ACCOUNT_ID)).thenReturn(Optional.of(account));

        DomainException ex = assertThrows(DomainException.class,
                () -> service.execute(USER_ID, "retirar", bd("100.00")));

        assertEquals(Messages.AMOUNT_EXCEEDS_INVESTED, ex.getMessage());
        verify(accountRepository, never()).credit(anyLong(), any());
        verify(transactionRepository, never()).recordRedemption(anyLong(), any());
    }

    // ------------------------------------------------------------
    // execute() estrutural
    // ------------------------------------------------------------

    @Test
    void executeThrowsWhenAccountMissing() {
        Investment inv = new Investment(1L, USER_ID, bd("0.00"), FIXED_NOW);
        when(investmentRepository.findByUserId(USER_ID)).thenReturn(Optional.of(inv));
        when(accountRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThrows(DomainException.class,
                () -> service.execute(USER_ID, "investir", bd("100.00")));
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void executeNormalizesAmountToScaleTwo() {
        Account account = new Account(ACCOUNT_ID, "0001", bd("500.00"), USER_ID);
        Investment inv = new Investment(1L, USER_ID, bd("0.00"), FIXED_NOW);
        when(investmentRepository.findByUserId(USER_ID)).thenReturn(Optional.of(inv));
        when(accountRepository.findByUserId(USER_ID)).thenReturn(Optional.of(account));
        when(accountRepository.findByIdForUpdate(ACCOUNT_ID)).thenReturn(Optional.of(account));

        service.execute(USER_ID, "investir", bd("100.999"));

        ArgumentCaptor<BigDecimal> debitCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(accountRepository).debit(eq(ACCOUNT_ID), debitCaptor.capture());
        assertEquals(2, debitCaptor.getValue().scale());
        assertEquals(0, debitCaptor.getValue().compareTo(bd("101.00")));
    }
}
