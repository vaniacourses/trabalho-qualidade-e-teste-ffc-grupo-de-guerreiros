package com.bancodigital.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "signup", required = false) String signup,
                            Model model) {
        if (error != null) model.addAttribute("error", "E-mail ou senha inválidos.");
        if (logout != null) model.addAttribute("message", "Sessão encerrada.");
        if (signup != null) model.addAttribute("message", "Cadastro realizado. Faça login para continuar.");
        return "login";
    }
}
