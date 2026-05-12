package com.bancodigital.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.bancodigital.shared.exception.DomainException;

@Component
public class CurrentUser {

    private final CustomUserDetailsService userDetailsService;

    public CurrentUser(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public User required(UserDetails principal) {
        if (principal == null) throw new DomainException("Sessão expirada.");
        User user = userDetailsService.findByEmail(principal.getUsername());
        if (user == null) throw new DomainException("Usuário não encontrado.");
        return user;
    }
}
