package com.bancodigital.transacao;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record Transacao(
        Long id,
        Long contaOrigem,
        Long contaDestino,
        TipoTransacao tipo,
        BigDecimal valor,
        OffsetDateTime data) {
}
