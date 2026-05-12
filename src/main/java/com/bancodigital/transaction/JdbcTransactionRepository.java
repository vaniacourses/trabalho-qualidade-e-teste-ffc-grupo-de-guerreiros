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
            (Long) rs.getObject("source_account"),
            (Long) rs.getObject("destination_account"),
            TransactionType.fromDbValue(rs.getString("type")),
            rs.getBigDecimal("amount"),
            rs.getObject("date", java.time.OffsetDateTime.class));

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcTransactionRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Transaction> findByAccountId(long accountId) {
        return jdbc.query(
                "SELECT id, source_account, destination_account, type, amount, date "
                        + "FROM transactions "
                        + "WHERE source_account = :id OR destination_account = :id "
                        + "ORDER BY date DESC",
                new MapSqlParameterSource("id", accountId), ROW_MAPPER);
    }

    @Override
    public void recordDeposit(long destinationAccount, BigDecimal amount) {
        jdbc.update(
                "INSERT INTO transactions (destination_account, type, amount) VALUES (:c, 'deposit', :v)",
                new MapSqlParameterSource().addValue("c", destinationAccount).addValue("v", amount));
    }

    @Override
    public void recordWithdraw(long sourceAccount, BigDecimal amount) {
        jdbc.update(
                "INSERT INTO transactions (source_account, type, amount) VALUES (:c, 'withdraw', :v)",
                new MapSqlParameterSource().addValue("c", sourceAccount).addValue("v", amount));
    }

    @Override
    public void recordTransfer(long source, long destination, BigDecimal amount) {
        jdbc.update(
                "INSERT INTO transactions (source_account, destination_account, type, amount) "
                        + "VALUES (:o, :d, 'transfer', :v)",
                new MapSqlParameterSource()
                        .addValue("o", source)
                        .addValue("d", destination)
                        .addValue("v", amount));
    }

    @Override
    public void recordInvestment(long sourceAccount, BigDecimal amount) {
        jdbc.update(
                "INSERT INTO transactions (source_account, type, amount) VALUES (:c, 'investment', :v)",
                new MapSqlParameterSource().addValue("c", sourceAccount).addValue("v", amount));
    }

    @Override
    public void recordRedemption(long destinationAccount, BigDecimal amount) {
        jdbc.update(
                "INSERT INTO transactions (destination_account, type, amount) VALUES (:c, 'redemption', :v)",
                new MapSqlParameterSource().addValue("c", destinationAccount).addValue("v", amount));
    }
}
