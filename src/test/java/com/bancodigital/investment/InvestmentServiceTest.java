package com.bancodigital.investment;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bancodigital.shared.Messages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InvestmentServiceTest {

    private InvestmentService service;

    @BeforeEach
    void setUp() {
        service = new InvestmentService(null, null, null, null);
    }

    private BigDecimal bd(String s) { return new BigDecimal(s); }

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
}
