import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.mycompany.a.Transferencia;

public class TransferenciaTest {

    @Test
    public void testTransferenciaComSucesso() {
        Transferencia operacao = new Transferencia();
        String resultado = operacao.validarTransferencia(50.0, "C999", "C111", 100.0, true);
        assertEquals("OK", resultado);
    }
     @Test
    public void testTransferenciaSaldoInsuficiente() {
        Transferencia operacao = new Transferencia();
        String resultado = operacao.validarTransferencia(200.0, "C999", "C111", 100.0, true);
        assertEquals("Saldo insuficiente.", resultado);
    }

    @Test
    public void testTransferenciaMesmaConta() {
        Transferencia operacao = new Transferencia();
        String resultado = operacao.validarTransferencia(50.0, "C111", "C111", 100.0, true);
        assertEquals("Não é possível transferir para a própria conta.", resultado);
    }
     @Test
    public void testTransferenciaValorNegativo() {
        Transferencia operacao = new Transferencia();
        String resultado = operacao.validarTransferencia(-20.0, "C999", "C111", 100.0, true);
        assertEquals("Valor ou conta inválidos.", resultado);
    }

    @Test
    public void testTransferenciaContaDestinoInexistente() {
        Transferencia operacao = new Transferencia();
        String resultado = operacao.validarTransferencia(50.0, "C999", "C111", 100.0, false);
        assertEquals("Conta destino não encontrada.", resultado);
    }

   
}