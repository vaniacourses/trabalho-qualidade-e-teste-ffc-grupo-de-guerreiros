package com.bancodigital.transaction;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcTransactionRepository implements TransactionRepository {

    private static final RowMapper<Transaction> ROW_MAPPER = (rs, n) -> new Transaction(
            rs.getLong("id"),
            (Long) rs.getObject("conta_origem"),
            (Long) rs.getObject("conta_destino"),
            TransactionType.fromDbValue(rs.getString("tipo")),
            rs.getBigDecimal("valor"),
            rs.getObject("data", java.time.OffsetDateTime.class));

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcTransactionRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Transaction> findByAccountId(long accountId) {
        return jdbc.query(
                "SELECT id, conta_origem, conta_destino, tipo, valor, data "
                        + "FROM transacao "
                        + "WHERE conta_origem = :id OR conta_destino = :id "
                        + "ORDER BY data DESC",
                new MapSqlParameterSource("id", accountId), ROW_MAPPER);
    }

    @Override
    public void recordDeposit(long destinationAccount, BigDecimal amount) {
        jdbc.update(
                "INSERT INTO transacao (conta_destino, tipo, valor) VALUES (:c, 'deposito', :v)",
                new MapSqlParameterSource().addValue("c", destinationAccount).addValue("v", amount));
    }

    @Override
    public void recordWithdraw(long sourceAccount, BigDecimal amount) {
        jdbc.update(
                "INSERT INTO transacao (conta_origem, tipo, valor) VALUES (:c, 'saque', :v)",
                new MapSqlParameterSource().addValue("c", sourceAccount).addValue("v", amount));
    }

    @Override
    public void recordTransfer(long source, long destination, BigDecimal amount) {
        jdbc.update(
                "INSERT INTO transacao (conta_origem, conta_destino, tipo, valor) "
                        + "VALUES (:o, :d, 'transferencia', :v)",
                new MapSqlParameterSource()
                        .addValue("o", source)
                        .addValue("d", destination)
                        .addValue("v", amount));
    }

    @Override
    public void recordInvestment(long sourceAccount, BigDecimal amount) {
        jdbc.update(
                "INSERT INTO transacao (conta_origem, tipo, valor) VALUES (:c, 'investimento', :v)",
                new MapSqlParameterSource().addValue("c", sourceAccount).addValue("v", amount));
    }

    @Override
    public void recordRedemption(long destinationAccount, BigDecimal amount) {
        jdbc.update(
                "INSERT INTO transacao (conta_destino, tipo, valor) VALUES (:c, 'resgate', :v)",
                new MapSqlParameterSource().addValue("c", destinationAccount).addValue("v", amount));
    }
}
