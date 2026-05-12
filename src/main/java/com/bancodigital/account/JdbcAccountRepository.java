package com.bancodigital.account;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcAccountRepository implements AccountRepository {

    private static final RowMapper<Account> ROW_MAPPER = (rs, n) -> new Account(
            rs.getLong("id"),
            rs.getString("number"),
            rs.getBigDecimal("balance"),
            rs.getLong("user_id"));

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcAccountRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Account> findByUserId(long userId) {
        return jdbc.query(
                "SELECT id, number, balance, user_id FROM accounts WHERE user_id = :uid",
                new MapSqlParameterSource("uid", userId), ROW_MAPPER)
                .stream().findFirst();
    }

    @Override
    public Optional<Account> findByNumber(String number) {
        return jdbc.query(
                "SELECT id, number, balance, user_id FROM accounts WHERE number = :number",
                new MapSqlParameterSource("number", number), ROW_MAPPER)
                .stream().findFirst();
    }

    @Override
    public Optional<Account> findByIdForUpdate(long id) {
        return jdbc.query(
                "SELECT id, number, balance, user_id FROM accounts WHERE id = :id FOR UPDATE",
                new MapSqlParameterSource("id", id), ROW_MAPPER)
                .stream().findFirst();
    }

    @Override
    public void credit(long id, BigDecimal amount) {
        jdbc.update("UPDATE accounts SET balance = balance + :amount WHERE id = :id",
                new MapSqlParameterSource().addValue("amount", amount).addValue("id", id));
    }

    @Override
    public void debit(long id, BigDecimal amount) {
        jdbc.update("UPDATE accounts SET balance = balance - :amount WHERE id = :id",
                new MapSqlParameterSource().addValue("amount", amount).addValue("id", id));
    }

    @Override
    public String nextAccountNumber() {
        Long n = jdbc.queryForObject("SELECT nextval('account_number_seq')",
                new MapSqlParameterSource(), Long.class);
        return String.format("C%05d", n);
    }

    @Override
    public void insert(String number, long userId) {
        jdbc.update("INSERT INTO accounts (number, balance, user_id) VALUES (:number, 0, :uid)",
                new MapSqlParameterSource()
                        .addValue("number", number)
                        .addValue("uid", userId));
    }
}
