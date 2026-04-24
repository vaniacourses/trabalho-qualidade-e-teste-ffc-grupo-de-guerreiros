import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.mycompany.a.saque;

public class SaqueTest {

    @Test
    public void testSaqueOk() {
        saque sacado = new saque();
        String resultado = sacado.validaSaque(500.0, 100.0);
        assertEquals("Ok", resultado);
    }
    
    @Test
    public void testValorInvalido() {
        saque sacado = new saque();
        String resultado = sacado.validaSaque(500.0, -10.0);
        assertEquals("Valor Inválido.", resultado);
    }

    @Test
    public void testSaqueSaldoInsuficiente() {
        saque sacado = new saque();
        String resultado = sacado.validaSaque(200.0, 100.0);
        assertEquals("Saldo insuficiente.", resultado);
    }

    @Test
    public void testSaqueLimite() {
        saque sacado = new saque();
        String resultado = sacado.validaSaque(500.0, 12000.0);
        assertEquals("Limite máximo por saque excedido.", resultado);
    }

    @Test
    public void testSaqueHorario() {
        saque sacado = new saque();
        String resultado = sacado.validaSaque(500.0, 12000.0);
        assertEquals("Saques permitidos apenas entre 06h e 22h.", resultado);
    }
}