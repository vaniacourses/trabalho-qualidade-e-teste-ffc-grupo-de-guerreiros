package com.bancodigital.account;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.bancodigital.auth.CurrentUser;

@Controller
public class BalanceController {

    private final AccountService accountService;
    private final CurrentUser currentUser;

    public BalanceController(AccountService accountService, CurrentUser currentUser) {
        this.accountService = accountService;
        this.currentUser = currentUser;
    }

    @GetMapping("/balance")
    public String balance(@AuthenticationPrincipal UserDetails principal, Model model) {
        var user = currentUser.required(principal);
        Account account = accountService.getAccount(user.id());
        model.addAttribute("account", account);
        return "balance";
    }
}
