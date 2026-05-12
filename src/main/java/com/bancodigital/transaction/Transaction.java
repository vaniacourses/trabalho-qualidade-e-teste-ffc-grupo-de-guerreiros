package com.bancodigital.transaction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record Transaction(
        Long id,
        Long sourceAccount,
        Long destinationAccount,
        TransactionType type,
        BigDecimal amount,
        OffsetDateTime date) {
}
