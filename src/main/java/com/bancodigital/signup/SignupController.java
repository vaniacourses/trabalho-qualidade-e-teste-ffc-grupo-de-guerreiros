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

    @GetMapping("/signup")
    public String form(Model model) {
        if (!model.containsAttribute("signupForm")) {
            model.addAttribute("signupForm", new SignupForm());
        }
        return "signup";
    }

    @PostMapping("/signup")
    public String submit(@ModelAttribute("signupForm") SignupForm signupForm, Model model) {
        try {
            service.register(signupForm);
        } catch (DomainException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("signupForm", signupForm);
            return "signup";
        }
        return "redirect:/login?signup";
    }
}
