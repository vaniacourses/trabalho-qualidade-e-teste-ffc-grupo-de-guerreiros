package com.bancodigital.shared.money;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoneyTest {

    @Test
    void normalizeAplicaScale2HalfUp() {
        assertEquals(new BigDecimal("12.35"), Money.normalize(new BigDecimal("12.345")));
        assertEquals(new BigDecimal("12.34"), Money.normalize(new BigDecimal("12.344")));
    }

    @Test
    void normalizeDeNullRetornaNull() {
        assertNull(Money.normalize(null));
    }

    @Test
    void normalizeMantemValorJaArredondado() {
        assertEquals(new BigDecimal("10.00"), Money.normalize(new BigDecimal("10")));
    }

    @Test
    void parseOrNullDeStringValida() {
        assertEquals(new BigDecimal("100.50"), Money.parseOrNull("100.50"));
    }

    @Test
    void parseOrNullAceitaVirgulaComoSeparador() {
        assertEquals(new BigDecimal("100.50"), Money.parseOrNull("100,50"));
    }

    @Test
    void parseOrNullFazTrim() {
        assertEquals(new BigDecimal("100.00"), Money.parseOrNull("  100  "));
    }

    @Test
    void parseOrNullDeStringVazia() {
        assertNull(Money.parseOrNull(""));
    }

    @Test
    void parseOrNullDeWhitespace() {
        assertNull(Money.parseOrNull("   "));
    }

    @Test
    void parseOrNullDeNull() {
        assertNull(Money.parseOrNull(null));
    }

    @Test
    void parseOrNullDeLixoNaoEhNumero() {
        assertNull(Money.parseOrNull("abc"));
    }

    @Test
    void isPositiveTrueParaValorPositivo() {
        assertTrue(Money.isPositive(new BigDecimal("0.01")));
        assertTrue(Money.isPositive(new BigDecimal("1000")));
    }

    @Test
    void isPositiveFalseParaZero() {
        assertFalse(Money.isPositive(BigDecimal.ZERO));
    }

    @Test
    void isPositiveFalseParaNegativo() {
        assertFalse(Money.isPositive(new BigDecimal("-1")));
    }

    @Test
    void isPositiveFalseParaNull() {
        assertFalse(Money.isPositive(null));
    }

    @Test
    void formatGeraStringComMoedaBR() {
        String out = Money.format(new BigDecimal("1234.56"));
        assertTrue(out.contains("1.234,56"), "esperava milhar com ponto e decimal com vírgula em " + out);
        assertTrue(out.contains("R$"), "esperava prefixo R$ em " + out);
    }

    @Test
    void formatNullTratadoComoZero() {
        String out = Money.format(null);
        assertTrue(out.contains("0,00"));
    }
}
