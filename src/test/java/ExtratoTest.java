import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.mycompany.a.Extrato;

public class ExtratoTest {

    @Test
    public void deveRetornarCorVerdeParaDeposito() {
        assertEquals("#3bb54a", Extrato.corPorTipo("deposito"));
    }

    @Test
    public void deveRetornarCorVermelhaParaSaque() {
        assertEquals("#e74c3c", Extrato.corPorTipo("saque"));
    }

    @Test
    public void deveRetornarCorAzulParaTransferencia() {
        assertEquals("#3498db", Extrato.corPorTipo("transferencia"));
    }

    @Test
    public void deveRetornarCorRoxaParaInvestimento() {
        assertEquals("#9b59b6", Extrato.corPorTipo("investimento"));
    }

    @Test
    public void deveRetornarCorPadraoParaTipoDesconhecido() {
        assertEquals("#999", Extrato.corPorTipo("desconhecido"));
    }

    @Test
    public void deveRetornarDescricaoDeDeposito() {
        assertEquals("Depósito realizado", Extrato.descricaoPorTipo("deposito", 0));
    }

    @Test
    public void deveRetornarDescricaoDeSaque() {
        assertEquals("Saque efetuado", Extrato.descricaoPorTipo("saque", 0));
    }

    @Test
    public void deveRetornarDescricaoDeTransferenciaComContaDestino() {
        assertEquals("Transferência para conta 42", Extrato.descricaoPorTipo("transferencia", 42));
    }

    @Test
    public void deveRetornarDescricaoDeInvestimento() {
        assertEquals("Investimento aplicado", Extrato.descricaoPorTipo("investimento", 0));
    }

    @Test
    public void deveRetornarDescricaoPadraoParaTipoDesconhecido() {
        assertEquals("Outro tipo", Extrato.descricaoPorTipo("desconhecido", 0));
    }
}
