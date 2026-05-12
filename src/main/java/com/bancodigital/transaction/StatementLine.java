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
            case DEPOSIT: return "#3bb54a";
            case WITHDRAW: return "#e74c3c";
            case TRANSFER: return "#3498db";
            case INVESTMENT: return "#9b59b6";
            case REDEMPTION: return "#f39c12";
            default: return "#999999";
        }
    }

    public static String descriptionFor(TransactionType type, Long destinationAccount, long contextAccountId) {
        switch (type) {
            case DEPOSIT: return "Depósito realizado";
            case WITHDRAW: return "Saque efetuado";
            case TRANSFER:
                return destinationAccount != null && destinationAccount == contextAccountId
                        ? "Transferência recebida"
                        : "Transferência enviada";
            case INVESTMENT: return "Investimento aplicado";
            case REDEMPTION: return "Resgate de investimento";
            default: return "Outra operação";
        }
    }
}
