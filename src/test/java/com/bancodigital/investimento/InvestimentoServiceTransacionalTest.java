package com.bancodigital.investimento;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bancodigital.conta.ContaRepositoryFake;
import com.bancodigital.shared.Mensagens;
import com.bancodigital.shared.exception.DomainException;
import com.bancodigital.transacao.TipoTransacao;
import com.bancodigital.transacao.TransacaoRepositoryFake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvestimentoServiceTransacionalTest {

    private static final Instant FIXED = Instant.parse("2026-05-12T15:00:00Z");
    private static final Clock CLOCK = Clock.fixed(FIXED, ZoneOffset.UTC);

    private InvestimentoRepositoryFake invFake;
    private ContaRepositoryFake contaFake;
    private TransacaoRepositoryFake transacaoFake;
    private InvestimentoService service;

    @BeforeEach
    void setUp() {
        invFake = new InvestimentoRepositoryFake();
        contaFake = new ContaRepositoryFake();
        transacaoFake = new TransacaoRepositoryFake();
        service = new InvestimentoService(invFake, contaFake, transacaoFake, CLOCK);
    }

    private BigDecimal bd(String s) { return new BigDecimal(s); }
    private OffsetDateTime agora() { return OffsetDateTime.ofInstant(FIXED, ZoneOffset.UTC); }
    private OffsetDateTime agoraMenos(long minutos) { return agora().minusMinutes(minutos); }

    @Test
    void consultarCriaInvestimentoZeradoSeNaoExiste() {
        contaFake.seed(1L, "C00001", bd("500.00"), 10L);

        BigDecimal valor = service.consultar(10L);

        assertEquals(0, valor.signum());
        assertTrue(invFake.findByUsuarioId(10L).isPresent());
    }

    @Test
    void consultarAplicaJurosCompostosDesdeUltimaAtualizacao() {
        invFake.seed(10L, bd("100.00"), agoraMenos(5));
        contaFake.seed(1L, "C00001", bd("500.00"), 10L);

        BigDecimal valor = service.consultar(10L);

        assertEquals(0, valor.compareTo(bd("105.10")), "5 min de 1% ao minuto = 100 × 1.01^5 ≈ 105.10");
        Investimento atualizado = invFake.findByUsuarioId(10L).get();
        assertEquals(0, atualizado.valor().compareTo(bd("105.10")));
    }

    @Test
    void consultarSemMinutosCompletosNaoAplicaJuros() {
        invFake.seed(10L, bd("100.00"), agora());
        contaFake.seed(1L, "C00001", bd("500.00"), 10L);

        BigDecimal valor = service.consultar(10L);

        assertEquals(0, valor.compareTo(bd("100.00")));
    }

    @Test
    void investirDebitaContaECreditaInvestimento() {
        invFake.seed(10L, bd("0.00"), agora());
        contaFake.seed(1L, "C00001", bd("500.00"), 10L);

        service.executar(10L, "investir", bd("200.00"));

        assertEquals(0, contaFake.findByUsuarioId(10L).get().saldo().compareTo(bd("300.00")));
        assertEquals(0, invFake.findByUsuarioId(10L).get().valor().compareTo(bd("200.00")));
        assertEquals(1, transacaoFake.all().size());
        assertEquals(TipoTransacao.INVESTIMENTO, transacaoFake.all().get(0).tipo());
    }

    @Test
    void investirSemSaldoFalha() {
        invFake.seed(10L, bd("0.00"), agora());
        contaFake.seed(1L, "C00001", bd("50.00"), 10L);

        DomainException ex = assertThrows(DomainException.class,
                () -> service.executar(10L, "investir", bd("100.00")));
        assertEquals(Mensagens.SALDO_INSUFICIENTE_CONTA, ex.getMessage());
        assertEquals(0, contaFake.findByUsuarioId(10L).get().saldo().compareTo(bd("50.00")));
        assertEquals(0, invFake.findByUsuarioId(10L).get().valor().signum());
    }

    @Test
    void retirarCreditaContaEDebitaInvestimento() {
        invFake.seed(10L, bd("500.00"), agora());
        contaFake.seed(1L, "C00001", bd("100.00"), 10L);

        service.executar(10L, "retirar", bd("200.00"));

        assertEquals(0, contaFake.findByUsuarioId(10L).get().saldo().compareTo(bd("300.00")));
        assertEquals(0, invFake.findByUsuarioId(10L).get().valor().compareTo(bd("300.00")));
        assertEquals(1, transacaoFake.all().size());
        assertEquals(TipoTransacao.RESGATE, transacaoFake.all().get(0).tipo());
    }

    @Test
    void retirarMaiorQueInvestidoFalha() {
        invFake.seed(10L, bd("50.00"), agora());
        contaFake.seed(1L, "C00001", bd("100.00"), 10L);

        DomainException ex = assertThrows(DomainException.class,
                () -> service.executar(10L, "retirar", bd("200.00")));
        assertEquals(Mensagens.VALOR_MAIOR_QUE_INVESTIDO, ex.getMessage());
        assertEquals(0, invFake.findByUsuarioId(10L).get().valor().compareTo(bd("50.00")));
    }

    @Test
    void operacaoInvalidaFalha() {
        invFake.seed(10L, bd("0.00"), agora());
        contaFake.seed(1L, "C00001", bd("500.00"), 10L);

        DomainException ex = assertThrows(DomainException.class,
                () -> service.executar(10L, "transferir", bd("100.00")));
        assertEquals(Mensagens.OPERACAO_INVALIDA, ex.getMessage());
    }

    @Test
    void executarAplicaJurosAntesDaOperacao() {
        invFake.seed(10L, bd("100.00"), agoraMenos(5));
        contaFake.seed(1L, "C00001", bd("1000.00"), 10L);

        service.executar(10L, "investir", bd("50.00"));

        // valor investido foi 100 + 5 min de juros (~105.10) e depois soma 50
        Investimento inv = invFake.findByUsuarioId(10L).get();
        assertEquals(0, inv.valor().compareTo(bd("155.10")));
        assertEquals(0, contaFake.findByUsuarioId(10L).get().saldo().compareTo(bd("950.00")));
    }

    @Test
    void consultarContaInexistenteNaoCriaInvestimento() {
        // sem conta seedada
        invFake.seed(10L, bd("0.00"), agora());
        DomainException ex = assertThrows(DomainException.class,
                () -> service.executar(10L, "investir", bd("100.00")));
        assertNotNull(ex.getMessage());
    }
}
