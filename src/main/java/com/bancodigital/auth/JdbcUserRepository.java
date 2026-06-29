package com.bancodigital.auth;

import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcUserRepository implements UserRepository {

    // A constante mantem o identificador de e-mail alinhado entre mapeamento,
    // parametros e consultas SQL, evitando literais duplicados.
    private static final String EMAIL = "email";

    private static final RowMapper<User> ROW_MAPPER = (rs, n) -> new User(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString(EMAIL),
            rs.getString("password_hash"));

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcUserRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        var params = new MapSqlParameterSource(EMAIL, email);
        return jdbc.query("SELECT id, name, email, password_hash FROM users WHERE email = :email",
                        params, ROW_MAPPER)
                .stream().findFirst();
    }

    @Override
    public boolean existsByEmail(String email) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = :email",
                new MapSqlParameterSource(EMAIL, email), Integer.class);
        return count != null && count > 0;
    }

    @Override
    public long save(String name, String email, String passwordHash) {
        var params = new MapSqlParameterSource()
                .addValue("name", name)
                .addValue(EMAIL, email)
                .addValue("password_hash", passwordHash);
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update("INSERT INTO users (name, email, password_hash) VALUES (:name, :email, :password_hash)",
                params, kh, new String[]{"id"});
        // O driver pode nao devolver a chave mesmo apos o insert; a validacao
        // transforma esse estado inesperado em erro explicito, em vez de NPE.
        Number generatedKey = kh.getKey();
        if (generatedKey == null) {
            throw new IllegalStateException("O banco não retornou o id do usuário criado.");
        }
        return generatedKey.longValue();
    }
}
