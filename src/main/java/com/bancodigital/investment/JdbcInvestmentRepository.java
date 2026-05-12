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
            rs.getLong("usuario_id"),
            rs.getBigDecimal("valor"),
            rs.getObject("ultima_att", OffsetDateTime.class));

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcInvestmentRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Investment> findByUserId(long userId) {
        return jdbc.query(
                "SELECT id, usuario_id, valor, ultima_att FROM investimento WHERE usuario_id = :uid",
                new MapSqlParameterSource("uid", userId), ROW_MAPPER)
                .stream().findFirst();
    }

    @Override
    public void ensureExists(long userId) {
        jdbc.update(
                "INSERT INTO investimento (usuario_id, valor, ultima_att) VALUES (:uid, 0, now()) "
                        + "ON CONFLICT (usuario_id) DO NOTHING",
                new MapSqlParameterSource("uid", userId));
    }

    @Override
    public void update(long userId, BigDecimal amount, OffsetDateTime lastUpdate) {
        jdbc.update(
                "UPDATE investimento SET valor = :v, ultima_att = :ts WHERE usuario_id = :uid",
                new MapSqlParameterSource()
                        .addValue("v", amount)
                        .addValue("ts", lastUpdate)
                        .addValue("uid", userId));
    }
}
