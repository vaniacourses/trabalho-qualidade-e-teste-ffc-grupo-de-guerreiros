package com.bancodigital.conta;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bancodigital.shared.Mensagens;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContaServiceTest {

    private ContaService service;

    @BeforeEach
    void setUp() {
        service = new ContaService(null, null);
    }

    private BigDecimal bd(String s) { return new BigDecimal(s); }

    @Test
    void saqueOk() {
        assertEquals("OK", service.validaSaque(bd("500.00"), bd("100.00")));
    }

    @Test
    void saqueValorNegativo() {
        assertEquals(Mensagens.VALOR_INVALIDO, service.validaSaque(bd("500.00"), bd("-10.00")));
    }

    @Test
    void saqueValorZero() {
        assertEquals(Mensagens.VALOR_INVALIDO, service.validaSaque(bd("500.00"), BigDecimal.ZERO));
    }

    @Test
    void saqueSaldoInsuficiente() {
        assertEquals(Mensagens.SALDO_INSUFICIENTE, service.validaSaque(bd("100.00"), bd("200.00")));
    }

    @Test
    void saqueLimiteExcedido() {
        assertEquals(Mensagens.LIMITE_SAQUE_EXCEDIDO,
                service.validaSaque(bd("20000.00"), bd("15000.00")));
    }

    @Test
    void saqueBoundarySaldoExato() {
        assertEquals("OK", service.validaSaque(bd("500.00"), bd("500.00")));
    }

    @Test
    void saqueBoundaryLimiteExato() {
        assertEquals("OK", service.validaSaque(bd("15000.00"), bd("10000.00")));
    }

    @Test
    void saqueAcimaDoLimitePorCentavo() {
        assertEquals(Mensagens.LIMITE_SAQUE_EXCEDIDO,
                service.validaSaque(bd("15000.00"), bd("10000.01")));
    }

    @Test
    void saqueContaZerada() {
        assertEquals(Mensagens.SALDO_INSUFICIENTE,
                service.validaSaque(BigDecimal.ZERO, bd("100.00")));
    }

    @Test
    void depositoComSucesso() {
        assertEquals("OK", service.validarDeposito(bd("150.00")));
    }

    @Test
    void depositoNegativo() {
        assertEquals(Mensagens.VALOR_INVALIDO, service.validarDeposito(bd("-100.00")));
    }

    @Test
    void depositoZero() {
        assertEquals(Mensagens.VALOR_INVALIDO, service.validarDeposito(BigDecimal.ZERO));
    }

    @Test
    void depositoBoundaryMinimo() {
        assertEquals("OK", service.validarDeposito(bd("0.01")));
    }

    @Test
    void depositoValorAlto() {
        assertEquals("OK", service.validarDeposito(bd("1000000.00")));
    }

    @Test
    void transferenciaOk() {
        assertEquals("OK", service.validarTransferencia(bd("50.00"), "C999", "C111", bd("100.00"), true));
    }

    @Test
    void transferenciaSaldoInsuficiente() {
        assertEquals(Mensagens.SALDO_INSUFICIENTE,
                service.validarTransferencia(bd("200.00"), "C999", "C111", bd("100.00"), true));
    }

    @Test
    void transferenciaMesmaConta() {
        assertEquals(Mensagens.MESMA_CONTA,
                service.validarTransferencia(bd("50.00"), "C111", "C111", bd("100.00"), true));
    }

    @Test
    void transferenciaValorNegativo() {
        assertEquals(Mensagens.VALOR_OU_CONTA_INVALIDOS,
                service.validarTransferencia(bd("-20.00"), "C999", "C111", bd("100.00"), true));
    }

    @Test
    void transferenciaContaDestinoInexistente() {
        assertEquals(Mensagens.CONTA_DESTINO_INVALIDA,
                service.validarTransferencia(bd("50.00"), "C999", "C111", bd("100.00"), false));
    }

    @Test
    void transferenciaBoundarySaldoExato() {
        assertEquals("OK",
                service.validarTransferencia(bd("500.00"), "C002", "C001", bd("500.00"), true));
    }

    @Test
    void transferenciaWhitespaceDestino() {
        assertEquals(Mensagens.VALOR_OU_CONTA_INVALIDOS,
                service.validarTransferencia(bd("100.00"), "   ", "C001", bd("1000.00"), true));
    }

    @Test
    void transferenciaValorZero() {
        assertEquals(Mensagens.VALOR_OU_CONTA_INVALIDOS,
                service.validarTransferencia(BigDecimal.ZERO, "C002", "C001", bd("1000.00"), true));
    }

    @Test
    void transferenciaDestinoNulo() {
        assertEquals(Mensagens.VALOR_OU_CONTA_INVALIDOS,
                service.validarTransferencia(bd("100.00"), null, "C001", bd("1000.00"), true));
    }
}
