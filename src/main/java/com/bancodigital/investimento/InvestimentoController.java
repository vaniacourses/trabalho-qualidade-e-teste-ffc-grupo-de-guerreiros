package com.bancodigital.investimento;

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
public class InvestimentoController {

    private final InvestimentoService investimentoService;
    private final UsuarioAtual usuarioAtual;

    public InvestimentoController(InvestimentoService investimentoService, UsuarioAtual usuarioAtual) {
        this.investimentoService = investimentoService;
        this.usuarioAtual = usuarioAtual;
    }

    @GetMapping("/investimento")
    public String form(@AuthenticationPrincipal UserDetails principal, Model model) {
        var usuario = usuarioAtual.obrigatorio(principal);
        BigDecimal valor = investimentoService.consultar(usuario.id());
        model.addAttribute("valorAtual", valor);
        return "investimento";
    }

    @PostMapping("/investimento")
    public String submit(@AuthenticationPrincipal UserDetails principal,
                         @RequestParam("op") String op,
                         @RequestParam("valor") String valorRaw,
                         RedirectAttributes ra) {
        var usuario = usuarioAtual.obrigatorio(principal);
        BigDecimal valor = Money.parseOrNull(valorRaw);
        if (valor == null) {
            ra.addFlashAttribute("erro", Mensagens.VALOR_INVALIDO);
            return "redirect:/investimento";
        }
        try {
            investimentoService.executar(usuario.id(), op, valor);
            ra.addFlashAttribute("mensagem", "investir".equalsIgnoreCase(op)
                    ? Mensagens.INVESTIMENTO_REALIZADO
                    : Mensagens.RESGATE_REALIZADO);
        } catch (DomainException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/investimento";
    }
}
