package com.bancodigital.login;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class UsuarioRepositoryFake implements UsuarioRepository {

    private final Map<String, Usuario> porEmail = new LinkedHashMap<>();
    private final Map<Long, Usuario> porId = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public void seed(Usuario u) {
        porEmail.put(u.email(), u);
        porId.put(u.id(), u);
        if (u.id() >= nextId.get()) nextId.set(u.id() + 1);
    }

    public Usuario byId(long id) {
        return porId.get(id);
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

    @Override
    public long save(String nome, String email, String senhaHash) {
        long id = nextId.getAndIncrement();
        Usuario u = new Usuario(id, nome, email, senhaHash);
        porEmail.put(email, u);
        porId.put(id, u);
        return id;
    }
}
