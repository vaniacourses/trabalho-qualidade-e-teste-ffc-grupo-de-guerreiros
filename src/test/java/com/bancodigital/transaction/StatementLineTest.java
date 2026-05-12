package com.bancodigital.transaction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StatementLineTest {

    @Test
    void returnsGreenColorForDeposit() {
        assertEquals("#3bb54a", StatementLine.colorFor(TransactionType.DEPOSIT));
    }

    @Test
    void returnsRedColorForWithdraw() {
        assertEquals("#e74c3c", StatementLine.colorFor(TransactionType.WITHDRAW));
    }

    @Test
    void returnsBlueColorForTransfer() {
        assertEquals("#3498db", StatementLine.colorFor(TransactionType.TRANSFER));
    }

    @Test
    void returnsPurpleColorForInvestment() {
        assertEquals("#9b59b6", StatementLine.colorFor(TransactionType.INVESTMENT));
    }

    @Test
    void returnsOrangeColorForRedemption() {
        assertEquals("#f39c12", StatementLine.colorFor(TransactionType.REDEMPTION));
    }

    @Test
    void depositDescription() {
        assertEquals("Depósito realizado",
                StatementLine.descriptionFor(TransactionType.DEPOSIT, null, 1));
    }

    @Test
    void withdrawDescription() {
        assertEquals("Saque efetuado",
                StatementLine.descriptionFor(TransactionType.WITHDRAW, null, 1));
    }

    @Test
    void outgoingTransferDescription() {
        assertEquals("Transferência enviada",
                StatementLine.descriptionFor(TransactionType.TRANSFER, 2L, 1));
    }

    @Test
    void incomingTransferDescription() {
        assertEquals("Transferência recebida",
                StatementLine.descriptionFor(TransactionType.TRANSFER, 1L, 1));
    }

    @Test
    void investmentDescription() {
        assertEquals("Investimento aplicado",
                StatementLine.descriptionFor(TransactionType.INVESTMENT, null, 1));
    }

    @Test
    void redemptionDescription() {
        assertEquals("Resgate de investimento",
                StatementLine.descriptionFor(TransactionType.REDEMPTION, null, 1));
    }

    @Test
    void copiesTransactionFields() {
        OffsetDateTime when = OffsetDateTime.now();
        Transaction t = new Transaction(1L, 1L, null, TransactionType.WITHDRAW, new BigDecimal("50.00"), when);

        StatementLine line = StatementLine.from(t, 1L);

        assertEquals(TransactionType.WITHDRAW, line.type());
        assertEquals(new BigDecimal("50.00"), line.amount());
        assertEquals(when, line.date());
    }

    @Test
    void formatsMonetaryAmount() {
        Transaction t = new Transaction(1L, 1L, null, TransactionType.WITHDRAW, new BigDecimal("1234.56"), OffsetDateTime.now());

        StatementLine line = StatementLine.from(t, 1L);

        assertNotNull(line.formattedAmount());
        assertEquals(true, line.formattedAmount().contains("1.234,56"));
    }

    @Test
    void distinguishesOutgoingFromIncomingTransfer() {
        Transaction outgoing = new Transaction(1L, 1L, 2L, TransactionType.TRANSFER, new BigDecimal("100"), OffsetDateTime.now());
        Transaction incoming = new Transaction(2L, 1L, 2L, TransactionType.TRANSFER, new BigDecimal("100"), OffsetDateTime.now());

        assertEquals("Transferência enviada", StatementLine.from(outgoing, 1L).description());
        assertEquals("Transferência recebida", StatementLine.from(incoming, 2L).description());
    }

    @Test
    void appliesDefaultColorByType() {
        Transaction t = new Transaction(1L, null, 1L, TransactionType.DEPOSIT, new BigDecimal("100"), OffsetDateTime.now());
        assertEquals("#3bb54a", StatementLine.from(t, 1L).color());
    }
}
