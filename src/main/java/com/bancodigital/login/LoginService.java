package com.bancodigital.login;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public LoginService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario autenticar(String email, String senha) {
        if (email == null || email.trim().isEmpty()) return null;
        if (senha == null || senha.isEmpty()) return null;
        Usuario usuario = repository.findByEmail(email.trim()).orElse(null);
        if (usuario == null) return null;
        if (!passwordEncoder.matches(senha, usuario.senhaHash())) return null;
        return usuario;
    }
}
