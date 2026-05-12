package com.bancodigital.investimento;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class InvestimentoRepositoryFake implements InvestimentoRepository {

    private final Map<Long, Investimento> porUsuarioId = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public Investimento seed(long usuarioId, BigDecimal valor, OffsetDateTime ultimaAtt) {
        Investimento inv = new Investimento(nextId.getAndIncrement(), usuarioId, valor, ultimaAtt);
        porUsuarioId.put(usuarioId, inv);
        return inv;
    }

    @Override
    public Optional<Investimento> findByUsuarioId(long usuarioId) {
        return Optional.ofNullable(porUsuarioId.get(usuarioId));
    }

    @Override
    public void ensureExists(long usuarioId) {
        porUsuarioId.computeIfAbsent(usuarioId, uid -> new Investimento(
                nextId.getAndIncrement(), uid, BigDecimal.ZERO.setScale(2), OffsetDateTime.now()));
    }

    @Override
    public void atualizar(long usuarioId, BigDecimal valor, OffsetDateTime ultimaAtt) {
        Investimento atual = porUsuarioId.get(usuarioId);
        if (atual == null) return;
        porUsuarioId.put(usuarioId, new Investimento(atual.id(), usuarioId, valor, ultimaAtt));
    }
}
