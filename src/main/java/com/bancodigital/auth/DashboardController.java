package com.bancodigital.auth;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final CustomUserDetailsService userDetailsService;

    public DashboardController(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/painel")
    public String dashboard(@AuthenticationPrincipal UserDetails principal, Model model) {
        User user = userDetailsService.findByEmail(principal.getUsername());
        model.addAttribute("nomeUsuario", user != null ? user.name() : principal.getUsername());
        return "painel";
    }
}
