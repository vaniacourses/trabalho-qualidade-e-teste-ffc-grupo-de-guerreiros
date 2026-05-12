package com.bancodigital.transaction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StatementLineTest {

    @Test
    void returnsGreenColorForDeposit() {
        assertEquals("#3bb54a", StatementLine.colorFor(TransactionType.DEPOSITO));
    }

    @Test
    void returnsRedColorForWithdraw() {
        assertEquals("#e74c3c", StatementLine.colorFor(TransactionType.SAQUE));
    }

    @Test
    void returnsBlueColorForTransfer() {
        assertEquals("#3498db", StatementLine.colorFor(TransactionType.TRANSFERENCIA));
    }

    @Test
    void returnsPurpleColorForInvestment() {
        assertEquals("#9b59b6", StatementLine.colorFor(TransactionType.INVESTIMENTO));
    }

    @Test
    void returnsOrangeColorForRedemption() {
        assertEquals("#f39c12", StatementLine.colorFor(TransactionType.RESGATE));
    }

    @Test
    void depositDescription() {
        assertEquals("Depósito realizado",
                StatementLine.descriptionFor(TransactionType.DEPOSITO, null, 1));
    }

    @Test
    void withdrawDescription() {
        assertEquals("Saque efetuado",
                StatementLine.descriptionFor(TransactionType.SAQUE, null, 1));
    }

    @Test
    void outgoingTransferDescription() {
        assertEquals("Transferência enviada",
                StatementLine.descriptionFor(TransactionType.TRANSFERENCIA, 2L, 1));
    }

    @Test
    void incomingTransferDescription() {
        assertEquals("Transferência recebida",
                StatementLine.descriptionFor(TransactionType.TRANSFERENCIA, 1L, 1));
    }

    @Test
    void investmentDescription() {
        assertEquals("Investimento aplicado",
                StatementLine.descriptionFor(TransactionType.INVESTIMENTO, null, 1));
    }

    @Test
    void redemptionDescription() {
        assertEquals("Resgate de investimento",
                StatementLine.descriptionFor(TransactionType.RESGATE, null, 1));
    }

    @Test
    void copiesTransactionFields() {
        OffsetDateTime when = OffsetDateTime.now();
        Transaction t = new Transaction(1L, 1L, null, TransactionType.SAQUE, new BigDecimal("50.00"), when);

        StatementLine line = StatementLine.from(t, 1L);

        assertEquals(TransactionType.SAQUE, line.type());
        assertEquals(new BigDecimal("50.00"), line.amount());
        assertEquals(when, line.date());
    }

    @Test
    void formatsMonetaryAmount() {
        Transaction t = new Transaction(1L, 1L, null, TransactionType.SAQUE, new BigDecimal("1234.56"), OffsetDateTime.now());

        StatementLine line = StatementLine.from(t, 1L);

        assertNotNull(line.formattedAmount());
        assertEquals(true, line.formattedAmount().contains("1.234,56"));
    }

    @Test
    void distinguishesOutgoingFromIncomingTransfer() {
        Transaction outgoing = new Transaction(1L, 1L, 2L, TransactionType.TRANSFERENCIA, new BigDecimal("100"), OffsetDateTime.now());
        Transaction incoming = new Transaction(2L, 1L, 2L, TransactionType.TRANSFERENCIA, new BigDecimal("100"), OffsetDateTime.now());

        assertEquals("Transferência enviada", StatementLine.from(outgoing, 1L).description());
        assertEquals("Transferência recebida", StatementLine.from(incoming, 2L).description());
    }

    @Test
    void appliesDefaultColorByType() {
        Transaction t = new Transaction(1L, null, 1L, TransactionType.DEPOSITO, new BigDecimal("100"), OffsetDateTime.now());
        assertEquals("#3bb54a", StatementLine.from(t, 1L).color());
    }
}
