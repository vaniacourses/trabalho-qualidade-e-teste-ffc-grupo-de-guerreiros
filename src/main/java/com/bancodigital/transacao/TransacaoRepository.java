package com.bancodigital.transacao;

import java.math.BigDecimal;
import java.util.List;

public interface TransacaoRepository {

    List<Transacao> findByContaId(long contaId);

    void registrarDeposito(long contaDestino, BigDecimal valor);

    void registrarSaque(long contaOrigem, BigDecimal valor);

    void registrarTransferencia(long origem, long destino, BigDecimal valor);

    void registrarInvestimento(long contaOrigem, BigDecimal valor);

    void registrarResgate(long contaDestino, BigDecimal valor);
}
