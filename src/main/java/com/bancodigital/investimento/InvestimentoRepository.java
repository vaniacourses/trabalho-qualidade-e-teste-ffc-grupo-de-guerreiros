package com.bancodigital.investimento;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

public interface InvestimentoRepository {

    Optional<Investimento> findByUsuarioId(long usuarioId);

    void ensureExists(long usuarioId);

    void atualizar(long usuarioId, BigDecimal valor, OffsetDateTime ultimaAtt);
}
