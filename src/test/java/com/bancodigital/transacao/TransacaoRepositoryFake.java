package com.bancodigital.transacao;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class TransacaoRepositoryFake implements TransacaoRepository {

    private final List<Transacao> transacoes = new ArrayList<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public List<Transacao> all() {
        return List.copyOf(transacoes);
    }

    @Override
    public List<Transacao> findByContaId(long contaId) {
        List<Transacao> out = new ArrayList<>();
        for (Transacao t : transacoes) {
            boolean origem = t.contaOrigem() != null && t.contaOrigem() == contaId;
            boolean destino = t.contaDestino() != null && t.contaDestino() == contaId;
            if (origem || destino) out.add(t);
        }
        out.sort(Comparator.comparing(Transacao::data).reversed());
        return out;
    }

    @Override
    public void registrarDeposito(long contaDestino, BigDecimal valor) {
        adicionar(null, contaDestino, TipoTransacao.DEPOSITO, valor);
    }

    @Override
    public void registrarSaque(long contaOrigem, BigDecimal valor) {
        adicionar(contaOrigem, null, TipoTransacao.SAQUE, valor);
    }

    @Override
    public void registrarTransferencia(long origem, long destino, BigDecimal valor) {
        adicionar(origem, destino, TipoTransacao.TRANSFERENCIA, valor);
    }

    @Override
    public void registrarInvestimento(long contaOrigem, BigDecimal valor) {
        adicionar(contaOrigem, null, TipoTransacao.INVESTIMENTO, valor);
    }

    @Override
    public void registrarResgate(long contaDestino, BigDecimal valor) {
        adicionar(null, contaDestino, TipoTransacao.RESGATE, valor);
    }

    private void adicionar(Long origem, Long destino, TipoTransacao tipo, BigDecimal valor) {
        transacoes.add(new Transacao(nextId.getAndIncrement(), origem, destino, tipo, valor, OffsetDateTime.now()));
    }
}
