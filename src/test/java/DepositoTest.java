import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.mycompany.a.Deposito;


public class DepositoTest {
      @Test
    public void testDepositoComSucesso() {
        Deposito operacao = new Deposito();
        
        // Simulação: Usuário tenta depositar R$ 150.00
        String resultado = operacao.validarDeposito(150.0);
        
        // Esperado: O sistema deve aprovar a operação
        assertEquals("OK", resultado);
    }

    @Test
    public void testDepositoValorNegativo() {
        Deposito operacao = new Deposito();
        
        // Simulação: Usuário tenta depositar um valor negativo (R$ -100.00)
        String resultado = operacao.validarDeposito(-100.0);
        
        // Esperado: O sistema deve barrar a operação
        assertEquals("Valor inválido.", resultado);
    }

    @Test
    public void testDepositoValorZero() {
        Deposito operacao = new Deposito();
        
        // Simulação: Usuário tenta depositar R$ 0.00
        String resultado = operacao.validarDeposito(0.0);
        
        // Esperado: O sistema deve barrar a operação
        assertEquals("Valor inválido.", resultado);
    }
}
