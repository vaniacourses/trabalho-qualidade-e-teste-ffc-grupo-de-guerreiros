package com.bancodigital.account;

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
public class WithdrawController {

    private final AccountService accountService;
    private final CurrentUser currentUser;

    public WithdrawController(AccountService accountService, CurrentUser currentUser) {
        this.accountService = accountService;
        this.currentUser = currentUser;
    }

    @GetMapping("/saque")
    public String form(@AuthenticationPrincipal UserDetails principal, Model model) {
        var user = currentUser.required(principal);
        Account account = accountService.getAccount(user.id());
        model.addAttribute("conta", account);
        return "saque";
    }

    @PostMapping("/saque")
    public String submit(@AuthenticationPrincipal UserDetails principal,
                         @RequestParam("valor") String rawAmount,
                         RedirectAttributes ra) {
        var user = currentUser.required(principal);
        BigDecimal amount = Money.parseOrNull(rawAmount);
        if (amount == null) {
            ra.addFlashAttribute("erro", Messages.INVALID_AMOUNT);
            return "redirect:/saque";
        }
        try {
            accountService.withdraw(user.id(), amount);
            ra.addFlashAttribute("mensagem", Messages.WITHDRAW_SUCCESS);
        } catch (DomainException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/saque";
    }
}
