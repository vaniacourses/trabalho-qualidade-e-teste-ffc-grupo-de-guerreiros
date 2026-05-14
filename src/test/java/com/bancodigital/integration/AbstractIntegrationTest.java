package com.bancodigital.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
public abstract class AbstractIntegrationTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected JdbcTemplate jdbc;

    @BeforeEach
    void cleanDatabase() {
        jdbc.execute("TRUNCATE TABLE transactions, investments, accounts, users RESTART IDENTITY CASCADE");
        jdbc.execute("ALTER SEQUENCE account_number_seq RESTART WITH 1");
    }

    protected long insertUser(String name, String email, String passwordHash) {
        jdbc.update("INSERT INTO users (name, email, password_hash) VALUES (?, ?, ?)", name, email, passwordHash);
        return jdbc.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email);
    }

    protected long insertAccount(String number, java.math.BigDecimal balance, long userId) {
        jdbc.update("INSERT INTO accounts (number, balance, user_id) VALUES (?, ?, ?)", number, balance, userId);
        return jdbc.queryForObject("SELECT id FROM accounts WHERE number = ?", Long.class, number);
    }

    protected void insertInvestment(long userId, java.math.BigDecimal amount) {
        jdbc.update("INSERT INTO investments (user_id, amount) VALUES (?, ?)", userId, amount);
    }
}
