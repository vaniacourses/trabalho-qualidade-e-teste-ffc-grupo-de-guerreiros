package com.bancodigital.transaction;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionTypeTest {

    @Test
    void depositDbValue() {
        assertEquals("deposit", TransactionType.DEPOSIT.getDbValue());
    }

    @Test
    void withdrawDbValue() {
        assertEquals("withdraw", TransactionType.WITHDRAW.getDbValue());
    }

    @Test
    void transferDbValue() {
        assertEquals("transfer", TransactionType.TRANSFER.getDbValue());
    }

    @Test
    void investmentDbValue() {
        assertEquals("investment", TransactionType.INVESTMENT.getDbValue());
    }

    @Test
    void redemptionDbValue() {
        assertEquals("redemption", TransactionType.REDEMPTION.getDbValue());
    }

    @Test
    void fromDbValueResolvesDeposit() {
        assertEquals(TransactionType.DEPOSIT, TransactionType.fromDbValue("deposit"));
    }

    @Test
    void fromDbValueResolvesAllTypes() {
        for (TransactionType type : TransactionType.values()) {
            assertEquals(type, TransactionType.fromDbValue(type.getDbValue()));
        }
    }

    @Test
    void throwsForUnknownDbValue() {
        assertThrows(IllegalArgumentException.class, () -> TransactionType.fromDbValue("outro"));
    }

    @Test
    void throwsForNullDbValue() {
        assertThrows(IllegalArgumentException.class, () -> TransactionType.fromDbValue(null));
    }
}
