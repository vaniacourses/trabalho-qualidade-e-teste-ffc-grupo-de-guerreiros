package com.bancodigital.transacao;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.bancodigital.conta.Conta;
import com.bancodigital.conta.ContaService;
import com.bancodigital.login.UsuarioAtual;

@Controller
public class ExtratoController {

    private final TransacaoRepository transacaoRepository;
    private final ContaService contaService;
    private final UsuarioAtual usuarioAtual;

    public ExtratoController(TransacaoRepository transacaoRepository,
                             ContaService contaService,
                             UsuarioAtual usuarioAtual) {
        this.transacaoRepository = transacaoRepository;
        this.contaService = contaService;
        this.usuarioAtual = usuarioAtual;
    }

    @GetMapping("/extrato")
    public String extrato(@AuthenticationPrincipal UserDetails principal, Model model) {
        var usuario = usuarioAtual.obrigatorio(principal);
        Conta conta = contaService.consultarConta(usuario.id());
        List<Transacao> transacoes = transacaoRepository.findByContaId(conta.id());
        List<ExtratoLinha> linhas = transacoes.stream()
                .map(t -> ExtratoLinha.de(t, conta.id()))
                .toList();
        model.addAttribute("conta", conta);
        model.addAttribute("linhas", linhas);
        return "extrato";
    }
}
