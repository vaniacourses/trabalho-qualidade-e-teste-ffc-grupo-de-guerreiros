package com.bancodigital.transacao;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExtratoLinhaTest {

    @Test
    void corPorTipoDeposito() {
        assertEquals("#3bb54a", ExtratoLinha.corPara(TipoTransacao.DEPOSITO));
    }

    @Test
    void corPorTipoSaque() {
        assertEquals("#e74c3c", ExtratoLinha.corPara(TipoTransacao.SAQUE));
    }

    @Test
    void corPorTipoTransferencia() {
        assertEquals("#3498db", ExtratoLinha.corPara(TipoTransacao.TRANSFERENCIA));
    }

    @Test
    void corPorTipoInvestimento() {
        assertEquals("#9b59b6", ExtratoLinha.corPara(TipoTransacao.INVESTIMENTO));
    }

    @Test
    void corPorTipoResgate() {
        assertEquals("#f39c12", ExtratoLinha.corPara(TipoTransacao.RESGATE));
    }

    @Test
    void descricaoDeposito() {
        assertEquals("Depósito realizado",
                ExtratoLinha.descricaoPara(TipoTransacao.DEPOSITO, null, 1));
    }

    @Test
    void descricaoSaque() {
        assertEquals("Saque efetuado",
                ExtratoLinha.descricaoPara(TipoTransacao.SAQUE, null, 1));
    }

    @Test
    void descricaoTransferenciaEnviada() {
        assertEquals("Transferência enviada",
                ExtratoLinha.descricaoPara(TipoTransacao.TRANSFERENCIA, 2L, 1));
    }

    @Test
    void descricaoTransferenciaRecebida() {
        assertEquals("Transferência recebida",
                ExtratoLinha.descricaoPara(TipoTransacao.TRANSFERENCIA, 1L, 1));
    }

    @Test
    void descricaoInvestimento() {
        assertEquals("Investimento aplicado",
                ExtratoLinha.descricaoPara(TipoTransacao.INVESTIMENTO, null, 1));
    }

    @Test
    void descricaoResgate() {
        assertEquals("Resgate de investimento",
                ExtratoLinha.descricaoPara(TipoTransacao.RESGATE, null, 1));
    }

    @Test
    void deCopiaCamposDaTransacao() {
        OffsetDateTime quando = OffsetDateTime.now();
        Transacao t = new Transacao(1L, 1L, null, TipoTransacao.SAQUE, new BigDecimal("50.00"), quando);

        ExtratoLinha linha = ExtratoLinha.de(t, 1L);

        assertEquals(TipoTransacao.SAQUE, linha.tipo());
        assertEquals(new BigDecimal("50.00"), linha.valor());
        assertEquals(quando, linha.data());
    }

    @Test
    void deFormataValorMonetario() {
        Transacao t = new Transacao(1L, 1L, null, TipoTransacao.SAQUE, new BigDecimal("1234.56"), OffsetDateTime.now());

        ExtratoLinha linha = ExtratoLinha.de(t, 1L);

        assertNotNull(linha.valorFormatado());
        assertEquals(true, linha.valorFormatado().contains("1.234,56"));
    }

    @Test
    void deDistingueTransferenciaEnviadaDeRecebida() {
        Transacao enviada = new Transacao(1L, 1L, 2L, TipoTransacao.TRANSFERENCIA, new BigDecimal("100"), OffsetDateTime.now());
        Transacao recebida = new Transacao(2L, 1L, 2L, TipoTransacao.TRANSFERENCIA, new BigDecimal("100"), OffsetDateTime.now());

        assertEquals("Transferência enviada", ExtratoLinha.de(enviada, 1L).descricao());
        assertEquals("Transferência recebida", ExtratoLinha.de(recebida, 2L).descricao());
    }

    @Test
    void deAplicaCorPadraoPorTipo() {
        Transacao t = new Transacao(1L, null, 1L, TipoTransacao.DEPOSITO, new BigDecimal("100"), OffsetDateTime.now());
        assertEquals("#3bb54a", ExtratoLinha.de(t, 1L).cor());
    }
}
