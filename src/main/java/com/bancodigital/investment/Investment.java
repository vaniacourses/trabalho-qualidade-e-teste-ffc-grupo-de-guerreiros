package com.bancodigital.investment;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record Investment(Long id, Long userId, BigDecimal amount, OffsetDateTime lastUpdate) {
}
