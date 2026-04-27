import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.mycompany.a.Saque;

public class SaqueTest {

    @Test
    public void testSaqueOk() {
        Saque sacado = new Saque();
        String resultado = sacado.validaSaque(500.0, 100.0);
        assertEquals("Ok", resultado);
    }
    
    @Test
    public void testValorInvalido() {
        Saque sacado = new Saque();
        String resultado = sacado.validaSaque(500.0, -10.0);
        assertEquals("Valor Inválido.", resultado);
    }

    @Test
    public void testSaqueSaldoInsuficiente() {
        Saque sacado = new Saque();
        String resultado = sacado.validaSaque(100.0, 200.0);
        assertEquals("Saldo insuficiente.", resultado);
    }

    @Test
    public void testSaqueLimite() {
        Saque sacado = new Saque();
        String resultado = sacado.validaSaque(20500.0, 12000.0);
        assertEquals("Limite máximo por saque excedido.", resultado);
    }
}