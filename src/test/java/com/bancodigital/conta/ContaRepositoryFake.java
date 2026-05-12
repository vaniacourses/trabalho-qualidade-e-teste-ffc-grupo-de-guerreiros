package com.bancodigital.conta;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class ContaRepositoryFake implements ContaRepository {

    private final Map<Long, Conta> porId = new HashMap<>();
    private final Map<String, Conta> porNumero = new HashMap<>();
    private final Map<Long, Conta> porUsuario = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);
    private final AtomicLong nextNumero = new AtomicLong(1);

    public Conta seed(long id, String numero, BigDecimal saldo, long usuarioId) {
        Conta c = new Conta(id, numero, saldo, usuarioId);
        porId.put(id, c);
        porNumero.put(numero, c);
        porUsuario.put(usuarioId, c);
        if (id >= nextId.get()) nextId.set(id + 1);
        return c;
    }

    @Override
    public Optional<Conta> findByUsuarioId(long usuarioId) {
        return Optional.ofNullable(porUsuario.get(usuarioId));
    }

    @Override
    public Optional<Conta> findByNumero(String numero) {
        return Optional.ofNullable(porNumero.get(numero));
    }

    @Override
    public Optional<Conta> findByIdForUpdate(long id) {
        return Optional.ofNullable(porId.get(id));
    }

    @Override
    public void creditar(long id, BigDecimal valor) {
        Conta atual = porId.get(id);
        if (atual == null) return;
        Conta novo = new Conta(atual.id(), atual.numero(), atual.saldo().add(valor), atual.usuarioId());
        replace(novo);
    }

    @Override
    public void debitar(long id, BigDecimal valor) {
        Conta atual = porId.get(id);
        if (atual == null) return;
        Conta novo = new Conta(atual.id(), atual.numero(), atual.saldo().subtract(valor), atual.usuarioId());
        replace(novo);
    }

    @Override
    public String proximoNumeroConta() {
        return String.format("C%05d", nextNumero.getAndIncrement());
    }

    @Override
    public void inserir(String numero, long usuarioId) {
        long id = nextId.getAndIncrement();
        seed(id, numero, BigDecimal.ZERO.setScale(2), usuarioId);
    }

    private void replace(Conta c) {
        porId.put(c.id(), c);
        porNumero.put(c.numero(), c);
        porUsuario.put(c.usuarioId(), c);
    }
}
