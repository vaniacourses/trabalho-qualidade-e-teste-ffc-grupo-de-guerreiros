package com.bancodigital.cadastro;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.bancodigital.shared.exception.DomainException;

@Controller
public class CadastroController {

    private final CadastroService service;

    public CadastroController(CadastroService service) {
        this.service = service;
    }

    @GetMapping("/cadastro")
    public String form(Model model) {
        if (!model.containsAttribute("cadastroForm")) {
            model.addAttribute("cadastroForm", new CadastroForm());
        }
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String submit(@ModelAttribute CadastroForm cadastroForm, Model model) {
        try {
            service.cadastrar(cadastroForm);
        } catch (DomainException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("cadastroForm", cadastroForm);
            return "cadastro";
        }
        return "redirect:/login?cadastro";
    }
}
