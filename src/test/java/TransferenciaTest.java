import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.mycompany.a.transferencia;

public class TransferenciaTest {

    @Test
    public void testTransferenciaComSucesso() {
        transferencia operacao = new transferencia();
        String resultado = operacao.validarTransferencia(50.0, "C999", "C111", 100.0, true);
        assertEquals("OK", resultado);
    }
     @Test
    public void testTransferenciaSaldoInsuficiente() {
        transferencia operacao = new transferencia();
        String resultado = operacao.validarTransferencia(200.0, "C999", "C111", 100.0, true);
        assertEquals("Saldo insuficiente.", resultado);
    }

    @Test
    public void testTransferenciaMesmaConta() {
        transferencia operacao = new transferencia();
        String resultado = operacao.validarTransferencia(50.0, "C111", "C111", 100.0, true);
        assertEquals("Não é possível transferir para a própria conta.", resultado);
    }

   
}