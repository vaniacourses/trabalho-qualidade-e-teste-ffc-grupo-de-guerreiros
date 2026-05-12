package com.bancodigital.login;

import java.util.Optional;

public interface UsuarioRepository {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    long save(String nome, String email, String senhaHash);
}
