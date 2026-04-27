package com.mycompany.a;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;


public class DepositoTest {

    deposito operacao;

    @BeforeAll
    public void iniciar(){
        operacao = new deposito();
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
