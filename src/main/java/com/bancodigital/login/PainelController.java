package com.bancodigital.login;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PainelController {

    private final CustomUserDetailsService userDetailsService;

    public PainelController(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/painel")
    public String painel(@AuthenticationPrincipal UserDetails principal, Model model) {
        Usuario usuario = userDetailsService.buscarPorEmail(principal.getUsername());
        model.addAttribute("nomeUsuario", usuario != null ? usuario.nome() : principal.getUsername());
        return "painel";
    }
}
