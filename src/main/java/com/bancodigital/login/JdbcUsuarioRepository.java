package com.bancodigital.login;

import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcUsuarioRepository implements UsuarioRepository {

    private static final RowMapper<Usuario> ROW_MAPPER = (rs, n) -> new Usuario(
            rs.getLong("id"),
            rs.getString("nome"),
            rs.getString("email"),
            rs.getString("senha_hash"));

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcUsuarioRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        var params = new MapSqlParameterSource("email", email);
        return jdbc.query("SELECT id, nome, email, senha_hash FROM usuario WHERE email = :email",
                        params, ROW_MAPPER)
                .stream().findFirst();
    }

    @Override
    public boolean existsByEmail(String email) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM usuario WHERE email = :email",
                new MapSqlParameterSource("email", email), Integer.class);
        return count != null && count > 0;
    }

    @Override
    public long save(String nome, String email, String senhaHash) {
        var params = new MapSqlParameterSource()
                .addValue("nome", nome)
                .addValue("email", email)
                .addValue("senha_hash", senhaHash);
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update("INSERT INTO usuario (nome, email, senha_hash) VALUES (:nome, :email, :senha_hash)",
                params, kh, new String[]{"id"});
        return kh.getKey().longValue();
    }
}
