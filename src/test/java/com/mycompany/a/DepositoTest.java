import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.mycompany.a.Deposito;
import org.junit.jupiter.api.BeforeEach;


public class DepositoTest {

    Deposito operacao;

    @BeforeEach
    public void iniciar(){
        operacao = new Deposito();
    }

    @Test
    public void testDepositoComSucesso() {    
        String resultado = operacao.validarDeposito(150.0);
        assertEquals("OK", resultado);
    }

    @Test
    public void testDepositoValorNegativo() {

        String resultado = operacao.validarDeposito(-100.0);
        assertEquals("Valor inválido.", resultado);
    }

    @Test
    public void testDepositoValorZero() {
        String resultado = operacao.validarDeposito(0.0);
        assertEquals("Valor inválido.", resultado);
    }
}
