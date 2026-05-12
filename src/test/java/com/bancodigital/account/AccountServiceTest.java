package com.bancodigital.account;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bancodigital.shared.Messages;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountServiceTest {

    private AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService(null, null);
    }

    private BigDecimal bd(String s) { return new BigDecimal(s); }

    @Test
    void withdrawOk() {
        assertEquals("OK", service.validateWithdraw(bd("500.00"), bd("100.00")));
    }

    @Test
    void withdrawWithNegativeAmount() {
        assertEquals(Messages.INVALID_AMOUNT, service.validateWithdraw(bd("500.00"), bd("-10.00")));
    }

    @Test
    void withdrawWithZeroAmount() {
        assertEquals(Messages.INVALID_AMOUNT, service.validateWithdraw(bd("500.00"), BigDecimal.ZERO));
    }

    @Test
    void withdrawWithInsufficientBalance() {
        assertEquals(Messages.INSUFFICIENT_BALANCE, service.validateWithdraw(bd("100.00"), bd("200.00")));
    }

    @Test
    void withdrawAboveLimit() {
        assertEquals(Messages.WITHDRAW_LIMIT_EXCEEDED,
                service.validateWithdraw(bd("20000.00"), bd("15000.00")));
    }

    @Test
    void withdrawAtExactBalance() {
        assertEquals("OK", service.validateWithdraw(bd("500.00"), bd("500.00")));
    }

    @Test
    void withdrawAtExactLimit() {
        assertEquals("OK", service.validateWithdraw(bd("15000.00"), bd("10000.00")));
    }

    @Test
    void withdrawJustAboveLimit() {
        assertEquals(Messages.WITHDRAW_LIMIT_EXCEEDED,
                service.validateWithdraw(bd("15000.00"), bd("10000.01")));
    }

    @Test
    void withdrawFromEmptyAccount() {
        assertEquals(Messages.INSUFFICIENT_BALANCE,
                service.validateWithdraw(BigDecimal.ZERO, bd("100.00")));
    }

    @Test
    void depositOk() {
        assertEquals("OK", service.validateDeposit(bd("150.00")));
    }

    @Test
    void depositWithNegativeAmount() {
        assertEquals(Messages.INVALID_AMOUNT, service.validateDeposit(bd("-100.00")));
    }

    @Test
    void depositWithZeroAmount() {
        assertEquals(Messages.INVALID_AMOUNT, service.validateDeposit(BigDecimal.ZERO));
    }

    @Test
    void depositAtMinimumBoundary() {
        assertEquals("OK", service.validateDeposit(bd("0.01")));
    }

    @Test
    void depositLargeAmount() {
        assertEquals("OK", service.validateDeposit(bd("1000000.00")));
    }

    @Test
    void transferOk() {
        assertEquals("OK", service.validateTransfer(bd("50.00"), "C999", "C111", bd("100.00"), true));
    }

    @Test
    void transferWithInsufficientBalance() {
        assertEquals(Messages.INSUFFICIENT_BALANCE,
                service.validateTransfer(bd("200.00"), "C999", "C111", bd("100.00"), true));
    }

    @Test
    void transferToSameAccount() {
        assertEquals(Messages.SAME_ACCOUNT,
                service.validateTransfer(bd("50.00"), "C111", "C111", bd("100.00"), true));
    }

    @Test
    void transferWithNegativeAmount() {
        assertEquals(Messages.INVALID_AMOUNT_OR_ACCOUNT,
                service.validateTransfer(bd("-20.00"), "C999", "C111", bd("100.00"), true));
    }

    @Test
    void transferToNonexistentAccount() {
        assertEquals(Messages.INVALID_DESTINATION_ACCOUNT,
                service.validateTransfer(bd("50.00"), "C999", "C111", bd("100.00"), false));
    }

    @Test
    void transferAtExactBalance() {
        assertEquals("OK",
                service.validateTransfer(bd("500.00"), "C002", "C001", bd("500.00"), true));
    }

    @Test
    void transferWithWhitespaceDestination() {
        assertEquals(Messages.INVALID_AMOUNT_OR_ACCOUNT,
                service.validateTransfer(bd("100.00"), "   ", "C001", bd("1000.00"), true));
    }

    @Test
    void transferWithZeroAmount() {
        assertEquals(Messages.INVALID_AMOUNT_OR_ACCOUNT,
                service.validateTransfer(BigDecimal.ZERO, "C002", "C001", bd("1000.00"), true));
    }

    @Test
    void transferWithNullDestination() {
        assertEquals(Messages.INVALID_AMOUNT_OR_ACCOUNT,
                service.validateTransfer(bd("100.00"), null, "C001", bd("1000.00"), true));
    }
}
