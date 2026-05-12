package com.bancodigital.conta;

import java.math.BigDecimal;

public record Conta(Long id, String numero, BigDecimal saldo, Long usuarioId) {
}
