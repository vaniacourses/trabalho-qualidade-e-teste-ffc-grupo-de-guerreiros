package com.bancodigital.login;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.bancodigital.shared.exception.DomainException;

@Component
public class UsuarioAtual {

    private final CustomUserDetailsService userDetailsService;

    public UsuarioAtual(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public Usuario obrigatorio(UserDetails principal) {
        if (principal == null) throw new DomainException("Sessão expirada.");
        Usuario usuario = userDetailsService.buscarPorEmail(principal.getUsername());
        if (usuario == null) throw new DomainException("Usuário não encontrado.");
        return usuario;
    }
}
