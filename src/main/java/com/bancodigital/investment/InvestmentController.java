package com.bancodigital.investment;

import java.math.BigDecimal;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bancodigital.auth.CurrentUser;
import com.bancodigital.shared.Messages;
import com.bancodigital.shared.exception.DomainException;
import com.bancodigital.shared.money.Money;

@Controller
public class InvestmentController {

    private final InvestmentService investmentService;
    private final CurrentUser currentUser;

    public InvestmentController(InvestmentService investmentService, CurrentUser currentUser) {
        this.investmentService = investmentService;
        this.currentUser = currentUser;
    }

    @GetMapping("/investimento")
    public String form(@AuthenticationPrincipal UserDetails principal, Model model) {
        var user = currentUser.required(principal);
        BigDecimal amount = investmentService.query(user.id());
        model.addAttribute("valorAtual", amount);
        return "investimento";
    }

    @PostMapping("/investimento")
    public String submit(@AuthenticationPrincipal UserDetails principal,
                         @RequestParam("op") String op,
                         @RequestParam("valor") String rawAmount,
                         RedirectAttributes ra) {
        var user = currentUser.required(principal);
        BigDecimal amount = Money.parseOrNull(rawAmount);
        if (amount == null) {
            ra.addFlashAttribute("erro", Messages.INVALID_AMOUNT);
            return "redirect:/investimento";
        }
        try {
            investmentService.execute(user.id(), op, amount);
            ra.addFlashAttribute("mensagem", "investir".equalsIgnoreCase(op)
                    ? Messages.INVESTMENT_SUCCESS
                    : Messages.REDEMPTION_SUCCESS);
        } catch (DomainException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/investimento";
    }
}
