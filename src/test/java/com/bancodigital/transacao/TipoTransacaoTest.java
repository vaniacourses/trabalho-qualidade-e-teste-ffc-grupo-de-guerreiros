package com.bancodigital.transacao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TipoTransacaoTest {

    @Test
    void dbValueDoDeposito() {
        assertEquals("deposito", TipoTransacao.DEPOSITO.getDbValue());
    }

    @Test
    void dbValueDoSaque() {
        assertEquals("saque", TipoTransacao.SAQUE.getDbValue());
    }

    @Test
    void dbValueDaTransferencia() {
        assertEquals("transferencia", TipoTransacao.TRANSFERENCIA.getDbValue());
    }

    @Test
    void dbValueDoInvestimento() {
        assertEquals("investimento", TipoTransacao.INVESTIMENTO.getDbValue());
    }

    @Test
    void dbValueDoResgate() {
        assertEquals("resgate", TipoTransacao.RESGATE.getDbValue());
    }

    @Test
    void fromDbValueResolveDeposito() {
        assertEquals(TipoTransacao.DEPOSITO, TipoTransacao.fromDbValue("deposito"));
    }

    @Test
    void fromDbValueResolveTodosOsTipos() {
        for (TipoTransacao tipo : TipoTransacao.values()) {
            assertEquals(tipo, TipoTransacao.fromDbValue(tipo.getDbValue()));
        }
    }

    @Test
    void fromDbValueLancaParaDesconhecido() {
        assertThrows(IllegalArgumentException.class, () -> TipoTransacao.fromDbValue("outro"));
    }

    @Test
    void fromDbValueLancaParaNull() {
        assertThrows(IllegalArgumentException.class, () -> TipoTransacao.fromDbValue(null));
    }
}
