package com.bancodigital.transaction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.bancodigital.shared.money.Money;

public record StatementLine(
        TransactionType type,
        BigDecimal amount,
        OffsetDateTime date,
        String description,
        String color,
        String formattedAmount) {

    public static StatementLine from(Transaction t, long contextAccountId) {
        return new StatementLine(
                t.type(),
                t.amount(),
                t.date(),
                descriptionFor(t.type(), t.destinationAccount(), contextAccountId),
                colorFor(t.type()),
                Money.format(t.amount()));
    }

    public static String colorFor(TransactionType type) {
        switch (type) {
            case DEPOSITO: return "#3bb54a";
            case SAQUE: return "#e74c3c";
            case TRANSFERENCIA: return "#3498db";
            case INVESTIMENTO: return "#9b59b6";
            case RESGATE: return "#f39c12";
            default: return "#999999";
        }
    }

    public static String descriptionFor(TransactionType type, Long destinationAccount, long contextAccountId) {
        switch (type) {
            case DEPOSITO: return "Depósito realizado";
            case SAQUE: return "Saque efetuado";
            case TRANSFERENCIA:
                return destinationAccount != null && destinationAccount == contextAccountId
                        ? "Transferência recebida"
                        : "Transferência enviada";
            case INVESTIMENTO: return "Investimento aplicado";
            case RESGATE: return "Resgate de investimento";
            default: return "Outra operação";
        }
    }
}
