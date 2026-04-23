package com.mycompany.a;

import org.junit.jupiter.api.Test;
import com.mycompany.a.deposito;
import static org.junit.jupiter.api.Assertions.*;


public class DepositoTest {
      @Test
    public void testDepositoComSucesso() {
        deposito operacao = new deposito();
        String resultado = operacao.validarDeposito(150.0);
        assertEquals("OK", resultado);
    }

    @Test
    public void testDepositoValorNegativo() {
        deposito operacao = new deposito();
        String resultado = operacao.validarDeposito(-100.0);
        assertEquals("Valor inválido.", resultado);
    }

    @Test
    public void testDepositoValorZero() {
        deposito operacao = new deposito();
        String resultado = operacao.validarDeposito(0.0);
        assertEquals("Valor inválido.", resultado);
    }
}
