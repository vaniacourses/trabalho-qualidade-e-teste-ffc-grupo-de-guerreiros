package com.bancodigital.investment;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

public interface InvestmentRepository {

    Optional<Investment> findByUserId(long userId);

    void ensureExists(long userId);

    void update(long userId, BigDecimal amount, OffsetDateTime lastUpdate);
}
