package com.bancodigital.conta;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bancodigital.shared.Mensagens;
import com.bancodigital.shared.exception.DomainException;
import com.bancodigital.transacao.TipoTransacao;
import com.bancodigital.transacao.Transacao;
import com.bancodigital.transacao.TransacaoRepositoryFake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContaServiceTransacionalTest {

    private ContaRepositoryFake contaFake;
    private TransacaoRepositoryFake transacaoFake;
    private ContaService service;

    @BeforeEach
    void setUp() {
        contaFake = new ContaRepositoryFake();
        transacaoFake = new TransacaoRepositoryFake();
        service = new ContaService(contaFake, transacaoFake);
    }

    private BigDecimal bd(String s) { return new BigDecimal(s); }

    @Test
    void consultarContaRetornaPorUsuario() {
        contaFake.seed(1L, "C00001", bd("100.00"), 10L);
        Conta c = service.consultarConta(10L);
        assertEquals("C00001", c.numero());
        assertEquals(0, c.saldo().compareTo(bd("100.00")));
    }

    @Test
    void consultarContaInexistenteFalha() {
        assertThrows(DomainException.class, () -> service.consultarConta(999L));
    }

    @Test
    void sacarReduzSaldoERegistraTransacao() {
        contaFake.seed(1L, "C00001", bd("500.00"), 10L);

        service.sacar(10L, bd("100.00"));

        assertEquals(0, contaFake.findByUsuarioId(10L).get().saldo().compareTo(bd("400.00")));
        List<Transacao> ts = transacaoFake.findByContaId(1L);
        assertEquals(1, ts.size());
        assertEquals(TipoTransacao.SAQUE, ts.get(0).tipo());
        assertEquals(0, ts.get(0).valor().compareTo(bd("100.00")));
    }

    @Test
    void sacarSaldoInsuficienteNaoPersiste() {
        contaFake.seed(1L, "C00001", bd("50.00"), 10L);

        DomainException ex = assertThrows(DomainException.class, () -> service.sacar(10L, bd("100.00")));
        assertEquals(Mensagens.SALDO_INSUFICIENTE, ex.getMessage());
        assertEquals(0, contaFake.findByUsuarioId(10L).get().saldo().compareTo(bd("50.00")));
        assertTrue(transacaoFake.all().isEmpty());
    }

    @Test
    void sacarValorNegativoNaoPersiste() {
        contaFake.seed(1L, "C00001", bd("500.00"), 10L);

        DomainException ex = assertThrows(DomainException.class, () -> service.sacar(10L, bd("-10.00")));
        assertEquals(Mensagens.VALOR_INVALIDO, ex.getMessage());
        assertEquals(0, contaFake.findByUsuarioId(10L).get().saldo().compareTo(bd("500.00")));
    }

    @Test
    void sacarAcimaDoLimiteNaoPersiste() {
        contaFake.seed(1L, "C00001", bd("20000.00"), 10L);

        DomainException ex = assertThrows(DomainException.class, () -> service.sacar(10L, bd("15000.00")));
        assertEquals(Mensagens.LIMITE_SAQUE_EXCEDIDO, ex.getMessage());
        assertEquals(0, contaFake.findByUsuarioId(10L).get().saldo().compareTo(bd("20000.00")));
    }

    @Test
    void depositarAumentaSaldoERegistra() {
        contaFake.seed(1L, "C00001", bd("100.00"), 10L);

        service.depositar(10L, bd("250.50"));

        assertEquals(0, contaFake.findByUsuarioId(10L).get().saldo().compareTo(bd("350.50")));
        List<Transacao> ts = transacaoFake.findByContaId(1L);
        assertEquals(1, ts.size());
        assertEquals(TipoTransacao.DEPOSITO, ts.get(0).tipo());
    }

    @Test
    void depositarValorZeroFalha() {
        contaFake.seed(1L, "C00001", bd("100.00"), 10L);

        DomainException ex = assertThrows(DomainException.class, () -> service.depositar(10L, BigDecimal.ZERO));
        assertEquals(Mensagens.VALOR_INVALIDO, ex.getMessage());
        assertEquals(0, contaFake.findByUsuarioId(10L).get().saldo().compareTo(bd("100.00")));
    }

    @Test
    void transferirEntreContasMovimentaAmbas() {
        contaFake.seed(1L, "C00001", bd("500.00"), 10L);
        contaFake.seed(2L, "C00002", bd("100.00"), 20L);

        service.transferir(10L, "C00002", bd("150.00"));

        assertEquals(0, contaFake.findByUsuarioId(10L).get().saldo().compareTo(bd("350.00")));
        assertEquals(0, contaFake.findByUsuarioId(20L).get().saldo().compareTo(bd("250.00")));
        assertEquals(1, transacaoFake.all().size());
        Transacao t = transacaoFake.all().get(0);
        assertEquals(TipoTransacao.TRANSFERENCIA, t.tipo());
        assertEquals(Long.valueOf(1L), t.contaOrigem());
        assertEquals(Long.valueOf(2L), t.contaDestino());
    }

    @Test
    void transferirParaContaInexistenteFalha() {
        contaFake.seed(1L, "C00001", bd("500.00"), 10L);

        DomainException ex = assertThrows(DomainException.class,
                () -> service.transferir(10L, "C99999", bd("100.00")));
        assertEquals(Mensagens.CONTA_DESTINO_INVALIDA, ex.getMessage());
        assertEquals(0, contaFake.findByUsuarioId(10L).get().saldo().compareTo(bd("500.00")));
        assertTrue(transacaoFake.all().isEmpty());
    }

    @Test
    void transferirParaMesmaContaFalha() {
        contaFake.seed(1L, "C00001", bd("500.00"), 10L);

        DomainException ex = assertThrows(DomainException.class,
                () -> service.transferir(10L, "C00001", bd("100.00")));
        assertEquals(Mensagens.MESMA_CONTA, ex.getMessage());
        assertEquals(0, contaFake.findByUsuarioId(10L).get().saldo().compareTo(bd("500.00")));
    }

    @Test
    void transferirComSaldoInsuficienteFalha() {
        contaFake.seed(1L, "C00001", bd("50.00"), 10L);
        contaFake.seed(2L, "C00002", bd("0.00"), 20L);

        DomainException ex = assertThrows(DomainException.class,
                () -> service.transferir(10L, "C00002", bd("100.00")));
        assertEquals(Mensagens.SALDO_INSUFICIENTE, ex.getMessage());
        assertEquals(0, contaFake.findByUsuarioId(10L).get().saldo().compareTo(bd("50.00")));
        assertEquals(0, contaFake.findByUsuarioId(20L).get().saldo().compareTo(bd("0.00")));
    }

    @Test
    void transferirValorNegativoFalha() {
        contaFake.seed(1L, "C00001", bd("500.00"), 10L);
        contaFake.seed(2L, "C00002", bd("0.00"), 20L);

        DomainException ex = assertThrows(DomainException.class,
                () -> service.transferir(10L, "C00002", bd("-10.00")));
        assertEquals(Mensagens.VALOR_OU_CONTA_INVALIDOS, ex.getMessage());
    }
}
