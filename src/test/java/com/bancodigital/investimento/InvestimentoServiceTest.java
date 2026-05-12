package com.bancodigital.investimento;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bancodigital.shared.Mensagens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InvestimentoServiceTest {

    private InvestimentoService service;

    @BeforeEach
    void setUp() {
        service = new InvestimentoService(null, null, null, null);
    }

    private BigDecimal bd(String s) { return new BigDecimal(s); }

    @Test
    void valorInalteradoQuandoMinutosForemZero() {
        assertEquals(0, service.calcularValorComJuros(bd("100.00"), 0).compareTo(bd("100.00")));
    }

    @Test
    void aplicaJurosDeUmPorCentoEmUmMinuto() {
        assertEquals(0, service.calcularValorComJuros(bd("100.00"), 1).compareTo(bd("101.00")));
    }

    @Test
    void aplicaJurosCompostosEmCincoMinutos() {
        BigDecimal r = service.calcularValorComJuros(bd("100.00"), 5);
        assertEquals(0, r.compareTo(bd("105.10")));
    }

    @Test
    void minutosNegativosMantemValor() {
        assertEquals(0, service.calcularValorComJuros(bd("100.00"), -10).compareTo(bd("100.00")));
    }

    @Test
    void valorZeroMantem() {
        assertEquals(0, service.calcularValorComJuros(BigDecimal.ZERO, 60).compareTo(BigDecimal.ZERO));
    }

    @Test
    void validacaoOperacaoInvalida() {
        assertEquals(Mensagens.OPERACAO_INVALIDA,
                service.validarOperacao("transferir", bd("100"), bd("500"), bd("0")));
    }

    @Test
    void validacaoOperacaoNula() {
        assertEquals(Mensagens.OPERACAO_INVALIDA,
                service.validarOperacao(null, bd("100"), bd("500"), bd("0")));
    }

    @Test
    void investirSemSaldoSuficiente() {
        assertEquals(Mensagens.SALDO_INSUFICIENTE_CONTA,
                service.validarOperacao("investir", bd("100"), bd("50"), bd("0")));
    }

    @Test
    void investirComSaldoExato() {
        assertNull(service.validarOperacao("investir", bd("100"), bd("100"), bd("0")));
    }

    @Test
    void retirarMaiorQueInvestido() {
        assertEquals(Mensagens.VALOR_MAIOR_QUE_INVESTIDO,
                service.validarOperacao("retirar", bd("100"), bd("500"), bd("50")));
    }

    @Test
    void retirarComValorExato() {
        assertNull(service.validarOperacao("retirar", bd("100"), bd("500"), bd("100")));
    }

    @Test
    void investirValorZero() {
        assertEquals(Mensagens.VALOR_INVALIDO,
                service.validarOperacao("investir", BigDecimal.ZERO, bd("500"), bd("0")));
    }

    @Test
    void investirValorNegativo() {
        assertEquals(Mensagens.VALOR_INVALIDO,
                service.validarOperacao("investir", bd("-10"), bd("500"), bd("0")));
    }

    @Test
    void investirValorNulo() {
        assertEquals(Mensagens.VALOR_INVALIDO,
                service.validarOperacao("investir", null, bd("500"), bd("0")));
    }
}
