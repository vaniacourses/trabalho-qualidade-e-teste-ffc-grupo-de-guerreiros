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
public class TransferController {

    private final AccountService accountService;
    private final CurrentUser currentUser;

    public TransferController(AccountService accountService, CurrentUser currentUser) {
        this.accountService = accountService;
        this.currentUser = currentUser;
    }

    @GetMapping("/transfer")
    public String form(@AuthenticationPrincipal UserDetails principal, Model model) {
        var user = currentUser.required(principal);
        Account account = accountService.getAccount(user.id());
        model.addAttribute("account", account);
        return "transfer";
    }

    @PostMapping("/transfer")
    public String submit(@AuthenticationPrincipal UserDetails principal,
                         @RequestParam("destination") String destination,
                         @RequestParam("amount") String rawAmount,
                         RedirectAttributes ra) {
        var user = currentUser.required(principal);
        BigDecimal amount = Money.parseOrNull(rawAmount);
        if (amount == null) {
            ra.addFlashAttribute("error", Messages.INVALID_AMOUNT_OR_ACCOUNT);
            return "redirect:/transfer";
        }
        try {
            accountService.transfer(user.id(), destination, amount);
            ra.addFlashAttribute("message", Messages.TRANSFER_SUCCESS);
        } catch (DomainException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/transfer";
    }
}
