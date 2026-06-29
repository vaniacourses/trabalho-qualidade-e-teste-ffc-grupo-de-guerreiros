package com.bancodigital.signup;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.bancodigital.shared.exception.DomainException;

@Controller
public class SignupController {

    // O nome do atributo precisa ser identico no GET, no binding do POST e no
    // retorno de erro; a constante preserva esse contrato com o template.
    private static final String SIGNUP_FORM_ATTRIBUTE = "signupForm";

    private final SignupService service;

    public SignupController(SignupService service) {
        this.service = service;
    }

    @GetMapping("/signup")
    public String form(Model model) {
        if (!model.containsAttribute(SIGNUP_FORM_ATTRIBUTE)) {
            model.addAttribute(SIGNUP_FORM_ATTRIBUTE, new SignupForm());
        }
        return "signup";
    }

    @PostMapping("/signup")
    public String submit(@ModelAttribute(SIGNUP_FORM_ATTRIBUTE) SignupForm signupForm, Model model) {
        try {
            service.register(signupForm);
        } catch (DomainException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute(SIGNUP_FORM_ATTRIBUTE, signupForm);
            return "signup";
        }
        return "redirect:/login?signup";
    }
}
