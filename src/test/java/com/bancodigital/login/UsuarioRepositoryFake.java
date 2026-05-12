package com.bancodigital.login;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class UsuarioRepositoryFake extends UsuarioRepository {

    private final Map<String, Usuario> porEmail = new HashMap<>();

    UsuarioRepositoryFake() {
        super(null);
    }

    void seed(Usuario u) {
        porEmail.put(u.email(), u);
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return Optional.ofNullable(porEmail.get(email));
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }
}
