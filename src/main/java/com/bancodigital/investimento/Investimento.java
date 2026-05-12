package com.bancodigital.investimento;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record Investimento(Long id, Long usuarioId, BigDecimal valor, OffsetDateTime ultimaAtt) {
}
