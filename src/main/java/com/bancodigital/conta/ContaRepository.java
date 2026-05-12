package com.bancodigital.conta;

import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ContaRepository {

    private static final RowMapper<Conta> ROW_MAPPER = (rs, n) -> new Conta(
            rs.getLong("id"),
            rs.getString("numero"),
            rs.getBigDecimal("saldo"),
            rs.getLong("usuario_id"));

    private final NamedParameterJdbcTemplate jdbc;

    public ContaRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Conta> findByUsuarioId(long usuarioId) {
        return jdbc.query(
                "SELECT id, numero, saldo, usuario_id FROM conta WHERE usuario_id = :uid",
                new MapSqlParameterSource("uid", usuarioId), ROW_MAPPER)
                .stream().findFirst();
    }

    public Optional<Conta> findByNumero(String numero) {
        return jdbc.query(
                "SELECT id, numero, saldo, usuario_id FROM conta WHERE numero = :numero",
                new MapSqlParameterSource("numero", numero), ROW_MAPPER)
                .stream().findFirst();
    }

    public Optional<Conta> findByIdForUpdate(long id) {
        return jdbc.query(
                "SELECT id, numero, saldo, usuario_id FROM conta WHERE id = :id FOR UPDATE",
                new MapSqlParameterSource("id", id), ROW_MAPPER)
                .stream().findFirst();
    }

    public void creditar(long id, BigDecimal valor) {
        jdbc.update("UPDATE conta SET saldo = saldo + :valor WHERE id = :id",
                new MapSqlParameterSource().addValue("valor", valor).addValue("id", id));
    }

    public void debitar(long id, BigDecimal valor) {
        jdbc.update("UPDATE conta SET saldo = saldo - :valor WHERE id = :id",
                new MapSqlParameterSource().addValue("valor", valor).addValue("id", id));
    }

    public String proximoNumeroConta() {
        Long n = jdbc.queryForObject("SELECT nextval('conta_numero_seq')",
                new MapSqlParameterSource(), Long.class);
        return String.format("C%05d", n);
    }

    public void inserir(String numero, long usuarioId) {
        jdbc.update("INSERT INTO conta (numero, saldo, usuario_id) VALUES (:numero, 0, :uid)",
                new MapSqlParameterSource()
                        .addValue("numero", numero)
                        .addValue("uid", usuarioId));
    }
}
