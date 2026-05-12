package com.bancodigital.conta;

import java.math.BigDecimal;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bancodigital.login.UsuarioAtual;
import com.bancodigital.shared.Mensagens;
import com.bancodigital.shared.exception.DomainException;
import com.bancodigital.shared.money.Money;

@Controller
public class TransferenciaController {

    private final ContaService contaService;
    private final UsuarioAtual usuarioAtual;

    public TransferenciaController(ContaService contaService, UsuarioAtual usuarioAtual) {
        this.contaService = contaService;
        this.usuarioAtual = usuarioAtual;
    }

    @GetMapping("/transferencia")
    public String form(@AuthenticationPrincipal UserDetails principal, Model model) {
        var usuario = usuarioAtual.obrigatorio(principal);
        Conta conta = contaService.consultarConta(usuario.id());
        model.addAttribute("conta", conta);
        return "transferencia";
    }

    @PostMapping("/transferencia")
    public String submit(@AuthenticationPrincipal UserDetails principal,
                         @RequestParam("destino") String destino,
                         @RequestParam("valor") String valorRaw,
                         RedirectAttributes ra) {
        var usuario = usuarioAtual.obrigatorio(principal);
        BigDecimal valor = Money.parseOrNull(valorRaw);
        if (valor == null) {
            ra.addFlashAttribute("erro", Mensagens.VALOR_OU_CONTA_INVALIDOS);
            return "redirect:/transferencia";
        }
        try {
            contaService.transferir(usuario.id(), destino, valor);
            ra.addFlashAttribute("mensagem", Mensagens.TRANSFERENCIA_REALIZADA);
        } catch (DomainException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/transferencia";
    }
}
