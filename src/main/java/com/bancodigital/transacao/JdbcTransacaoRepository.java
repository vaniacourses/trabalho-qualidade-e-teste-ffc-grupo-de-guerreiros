package com.bancodigital.transacao;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcTransacaoRepository implements TransacaoRepository {

    private static final RowMapper<Transacao> ROW_MAPPER = (rs, n) -> new Transacao(
            rs.getLong("id"),
            (Long) rs.getObject("conta_origem"),
            (Long) rs.getObject("conta_destino"),
            TipoTransacao.fromDbValue(rs.getString("tipo")),
            rs.getBigDecimal("valor"),
            rs.getObject("data", java.time.OffsetDateTime.class));

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcTransacaoRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Transacao> findByContaId(long contaId) {
        return jdbc.query(
                "SELECT id, conta_origem, conta_destino, tipo, valor, data "
                        + "FROM transacao "
                        + "WHERE conta_origem = :id OR conta_destino = :id "
                        + "ORDER BY data DESC",
                new MapSqlParameterSource("id", contaId), ROW_MAPPER);
    }

    @Override
    public void registrarDeposito(long contaDestino, BigDecimal valor) {
        jdbc.update(
                "INSERT INTO transacao (conta_destino, tipo, valor) VALUES (:c, 'deposito', :v)",
                new MapSqlParameterSource().addValue("c", contaDestino).addValue("v", valor));
    }

    @Override
    public void registrarSaque(long contaOrigem, BigDecimal valor) {
        jdbc.update(
                "INSERT INTO transacao (conta_origem, tipo, valor) VALUES (:c, 'saque', :v)",
                new MapSqlParameterSource().addValue("c", contaOrigem).addValue("v", valor));
    }

    @Override
    public void registrarTransferencia(long origem, long destino, BigDecimal valor) {
        jdbc.update(
                "INSERT INTO transacao (conta_origem, conta_destino, tipo, valor) "
                        + "VALUES (:o, :d, 'transferencia', :v)",
                new MapSqlParameterSource()
                        .addValue("o", origem)
                        .addValue("d", destino)
                        .addValue("v", valor));
    }

    @Override
    public void registrarInvestimento(long contaOrigem, BigDecimal valor) {
        jdbc.update(
                "INSERT INTO transacao (conta_origem, tipo, valor) VALUES (:c, 'investimento', :v)",
                new MapSqlParameterSource().addValue("c", contaOrigem).addValue("v", valor));
    }

    @Override
    public void registrarResgate(long contaDestino, BigDecimal valor) {
        jdbc.update(
                "INSERT INTO transacao (conta_destino, tipo, valor) VALUES (:c, 'resgate', :v)",
                new MapSqlParameterSource().addValue("c", contaDestino).addValue("v", valor));
    }
}
