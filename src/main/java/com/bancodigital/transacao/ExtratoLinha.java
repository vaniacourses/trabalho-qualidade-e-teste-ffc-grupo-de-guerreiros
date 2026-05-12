package com.bancodigital.transacao;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.bancodigital.shared.money.Money;

public record ExtratoLinha(
        TipoTransacao tipo,
        BigDecimal valor,
        OffsetDateTime data,
        String descricao,
        String cor,
        String valorFormatado) {

    public static ExtratoLinha de(Transacao t, long contaContexto) {
        return new ExtratoLinha(
                t.tipo(),
                t.valor(),
                t.data(),
                descricaoPara(t.tipo(), t.contaDestino(), contaContexto),
                corPara(t.tipo()),
                Money.format(t.valor()));
    }

    public static String corPara(TipoTransacao tipo) {
        switch (tipo) {
            case DEPOSITO: return "#3bb54a";
            case SAQUE: return "#e74c3c";
            case TRANSFERENCIA: return "#3498db";
            case INVESTIMENTO: return "#9b59b6";
            case RESGATE: return "#f39c12";
            default: return "#999999";
        }
    }

    public static String descricaoPara(TipoTransacao tipo, Long contaDestino, long contextoContaId) {
        switch (tipo) {
            case DEPOSITO: return "Depósito realizado";
            case SAQUE: return "Saque efetuado";
            case TRANSFERENCIA:
                return contaDestino != null && contaDestino == contextoContaId
                        ? "Transferência recebida"
                        : "Transferência enviada";
            case INVESTIMENTO: return "Investimento aplicado";
            case RESGATE: return "Resgate de investimento";
            default: return "Outra operação";
        }
    }
}
