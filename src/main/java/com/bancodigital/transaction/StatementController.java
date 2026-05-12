package com.bancodigital.transaction;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.bancodigital.account.Account;
import com.bancodigital.account.AccountService;
import com.bancodigital.auth.CurrentUser;

@Controller
public class StatementController {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final CurrentUser currentUser;

    public StatementController(TransactionRepository transactionRepository,
                               AccountService accountService,
                               CurrentUser currentUser) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.currentUser = currentUser;
    }

    @GetMapping("/statement")
    public String statement(@AuthenticationPrincipal UserDetails principal, Model model) {
        var user = currentUser.required(principal);
        Account account = accountService.getAccount(user.id());
        List<Transaction> transactions = transactionRepository.findByAccountId(account.id());
        List<StatementLine> lines = transactions.stream()
                .map(t -> StatementLine.from(t, account.id()))
                .toList();
        model.addAttribute("account", account);
        model.addAttribute("lines", lines);
        return "statement";
    }
}
