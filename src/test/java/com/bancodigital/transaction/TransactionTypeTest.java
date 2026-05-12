package com.bancodigital.transaction;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionTypeTest {

    @Test
    void depositDbValue() {
        assertEquals("deposito", TransactionType.DEPOSITO.getDbValue());
    }

    @Test
    void withdrawDbValue() {
        assertEquals("saque", TransactionType.SAQUE.getDbValue());
    }

    @Test
    void transferDbValue() {
        assertEquals("transferencia", TransactionType.TRANSFERENCIA.getDbValue());
    }

    @Test
    void investmentDbValue() {
        assertEquals("investimento", TransactionType.INVESTIMENTO.getDbValue());
    }

    @Test
    void redemptionDbValue() {
        assertEquals("resgate", TransactionType.RESGATE.getDbValue());
    }

    @Test
    void fromDbValueResolvesDeposit() {
        assertEquals(TransactionType.DEPOSITO, TransactionType.fromDbValue("deposito"));
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
