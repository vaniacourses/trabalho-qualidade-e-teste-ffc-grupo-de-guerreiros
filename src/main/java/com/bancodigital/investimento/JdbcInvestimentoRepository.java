package com.bancodigital.investimento;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcInvestimentoRepository implements InvestimentoRepository {

    private static final RowMapper<Investimento> ROW_MAPPER = (rs, n) -> new Investimento(
            rs.getLong("id"),
            rs.getLong("usuario_id"),
            rs.getBigDecimal("valor"),
            rs.getObject("ultima_att", OffsetDateTime.class));

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcInvestimentoRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Investimento> findByUsuarioId(long usuarioId) {
        return jdbc.query(
                "SELECT id, usuario_id, valor, ultima_att FROM investimento WHERE usuario_id = :uid",
                new MapSqlParameterSource("uid", usuarioId), ROW_MAPPER)
                .stream().findFirst();
    }

    @Override
    public void ensureExists(long usuarioId) {
        jdbc.update(
                "INSERT INTO investimento (usuario_id, valor, ultima_att) VALUES (:uid, 0, now()) "
                        + "ON CONFLICT (usuario_id) DO NOTHING",
                new MapSqlParameterSource("uid", usuarioId));
    }

    @Override
    public void atualizar(long usuarioId, BigDecimal valor, OffsetDateTime ultimaAtt) {
        jdbc.update(
                "UPDATE investimento SET valor = :v, ultima_att = :ts WHERE usuario_id = :uid",
                new MapSqlParameterSource()
                        .addValue("v", valor)
                        .addValue("ts", ultimaAtt)
                        .addValue("uid", usuarioId));
    }
}
