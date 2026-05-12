package com.bancodigital.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "erro", required = false) String erro,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "cadastro", required = false) String cadastro,
                            Model model) {
        if (erro != null) model.addAttribute("erro", "E-mail ou senha inválidos.");
        if (logout != null) model.addAttribute("mensagem", "Sessão encerrada.");
        if (cadastro != null) model.addAttribute("mensagem", "Cadastro realizado. Faça login para continuar.");
        return "login";
    }
}
