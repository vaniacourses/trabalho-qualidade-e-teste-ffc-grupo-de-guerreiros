package com.mycompany.a;

import org.junit.jupiter.api.Test;

import com.mycompany.a.deposito;

import static org.junit.jupiter.api.Assertions.*;


public class DepositoTest {
      @Test
    public void testDepositoComSucesso() {
        deposito operacao = new deposito();
        
        // Simulação: Usuário tenta depositar R$ 150.00
        String resultado = operacao.validarDeposito(150.0);
        
        // Esperado: O sistema deve aprovar a operação
        assertEquals("OK", resultado);
    }

    @Test
    public void testDepositoValorNegativo() {
        deposito operacao = new deposito();
        
        // Simulação: Usuário tenta depositar um valor negativo (R$ -100.00)
        String resultado = operacao.validarDeposito(-100.0);
        
        // Esperado: O sistema deve barrar a operação
        assertEquals("Valor inválido.", resultado);
    }

    @Test
    public void testDepositoValorZero() {
        deposito operacao = new deposito();
        
        // Simulação: Usuário tenta depositar R$ 0.00
        String resultado = operacao.validarDeposito(0.0);
        
        // Esperado: O sistema deve barrar a operação
        assertEquals("Valor inválido.", resultado);
    }
}
