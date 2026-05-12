package com.bancodigital.investment;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcInvestmentRepository implements InvestmentRepository {

    private static final RowMapper<Investment> ROW_MAPPER = (rs, n) -> new Investment(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getBigDecimal("amount"),
            rs.getObject("last_update", OffsetDateTime.class));

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcInvestmentRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Investment> findByUserId(long userId) {
        return jdbc.query(
                "SELECT id, user_id, amount, last_update FROM investments WHERE user_id = :uid",
                new MapSqlParameterSource("uid", userId), ROW_MAPPER)
                .stream().findFirst();
    }

    @Override
    public void ensureExists(long userId) {
        jdbc.update(
                "INSERT INTO investments (user_id, amount, last_update) VALUES (:uid, 0, now()) "
                        + "ON CONFLICT (user_id) DO NOTHING",
                new MapSqlParameterSource("uid", userId));
    }

    @Override
    public void update(long userId, BigDecimal amount, OffsetDateTime lastUpdate) {
        jdbc.update(
                "UPDATE investments SET amount = :v, last_update = :ts WHERE user_id = :uid",
                new MapSqlParameterSource()
                        .addValue("v", amount)
                        .addValue("ts", lastUpdate)
                        .addValue("uid", userId));
    }
}
