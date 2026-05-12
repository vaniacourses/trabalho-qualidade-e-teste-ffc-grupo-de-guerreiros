package com.bancodigital.transacao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
