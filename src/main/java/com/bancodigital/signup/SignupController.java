package com.bancodigital.signup;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.bancodigital.shared.exception.DomainException;

@Controller
public class SignupController {

    private final SignupService service;

    public SignupController(SignupService service) {
        this.service = service;
    }

    @GetMapping("/cadastro")
    public String form(Model model) {
        if (!model.containsAttribute("cadastroForm")) {
            model.addAttribute("cadastroForm", new SignupForm());
        }
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String submit(@ModelAttribute("cadastroForm") SignupForm cadastroForm, Model model) {
        try {
            service.register(cadastroForm);
        } catch (DomainException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("cadastroForm", cadastroForm);
            return "cadastro";
        }
        return "redirect:/login?cadastro";
    }
}
