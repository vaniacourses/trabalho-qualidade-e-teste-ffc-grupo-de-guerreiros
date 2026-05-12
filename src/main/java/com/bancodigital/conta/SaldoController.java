package com.bancodigital.conta;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.bancodigital.login.UsuarioAtual;

@Controller
public class SaldoController {

    private final ContaService contaService;
    private final UsuarioAtual usuarioAtual;

    public SaldoController(ContaService contaService, UsuarioAtual usuarioAtual) {
        this.contaService = contaService;
        this.usuarioAtual = usuarioAtual;
    }

    @GetMapping("/saldo")
    public String saldo(@AuthenticationPrincipal UserDetails principal, Model model) {
        var usuario = usuarioAtual.obrigatorio(principal);
        Conta conta = contaService.consultarConta(usuario.id());
        model.addAttribute("conta", conta);
        return "saldo";
    }
}
