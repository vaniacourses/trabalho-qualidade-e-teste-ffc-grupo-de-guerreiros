package com.bancodigital.conta;

import java.math.BigDecimal;
import java.util.Optional;

public interface ContaRepository {

    Optional<Conta> findByUsuarioId(long usuarioId);

    Optional<Conta> findByNumero(String numero);

    Optional<Conta> findByIdForUpdate(long id);

    void creditar(long id, BigDecimal valor);

    void debitar(long id, BigDecimal valor);

    String proximoNumeroConta();

    void inserir(String numero, long usuarioId);
}
