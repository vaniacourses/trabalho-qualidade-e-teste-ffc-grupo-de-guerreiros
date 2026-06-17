package com.bancodigital.shared.money;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoneyTest {

    @Test
    void normalizeAppliesScale2HalfUp() {
        assertEquals(new BigDecimal("12.35"), Money.normalize(new BigDecimal("12.345")));
        assertEquals(new BigDecimal("12.34"), Money.normalize(new BigDecimal("12.344")));
    }

    @Test
    void normalizeOfNullReturnsNull() {
        assertNull(Money.normalize(null));
    }

    @Test
    void normalizeKeepsAlreadyRoundedValue() {
        assertEquals(new BigDecimal("10.00"), Money.normalize(new BigDecimal("10")));
    }

    @Test
    void parseOrNullFromValidString() {
        assertEquals(new BigDecimal("100.50"), Money.parseOrNull("100.50"));
    }

    @Test
    void parseOrNullAcceptsCommaAsSeparator() {
        assertEquals(new BigDecimal("100.50"), Money.parseOrNull("100,50"));
    }

    @Test
    void parseOrNullTrimsInput() {
        assertEquals(new BigDecimal("100.00"), Money.parseOrNull("  100  "));
    }

    @Test
    void parseOrNullFromEmptyString() {
        assertNull(Money.parseOrNull(""));
    }

    @Test
    void parseOrNullFromWhitespace() {
        assertNull(Money.parseOrNull("   "));
    }

    @Test
    void parseOrNullFromNull() {
        assertNull(Money.parseOrNull(null));
    }

    @Test
    void parseOrNullFromNonNumericGarbage() {
        assertNull(Money.parseOrNull("abc"));
    }

    @Test
    void isPositiveTrueForPositiveAmount() {
        assertTrue(Money.isPositive(new BigDecimal("0.01")));
        assertTrue(Money.isPositive(new BigDecimal("1000")));
    }

    @Test
    void isPositiveFalseForZero() {
        assertFalse(Money.isPositive(BigDecimal.ZERO));
    }

    @Test
    void isPositiveFalseForNegative() {
        assertFalse(Money.isPositive(new BigDecimal("-1")));
    }

    @Test
    void isPositiveFalseForNull() {
        assertFalse(Money.isPositive(null));
    }

    @Test
    void formatProducesStringWithBrazilianCurrency() {
        String out = Money.format(new BigDecimal("1234.56"));
        assertTrue(out.contains("1.234,56"), "O valor em decimal deveria possuir ponto na casa do milhar e virgula antes dos decimais em: " + out);
        assertTrue(out.contains("R$"), "Deveria haver R$ antes do texto em: " + out);
    }

    @Test
    void formatNullTreatedAsZero() {
        String out = Money.format(null);
        assertTrue(out.contains("0,00"));
    }

    @Test
    void parseOrNullRemovesCurrencyPrefixes() {
        assertEquals(new BigDecimal("100.00"), Money.parseOrNull("R$ 100"));
        assertEquals(new BigDecimal("100.00"), Money.parseOrNull("$ 100"));
        assertEquals(new BigDecimal("100.00"), Money.parseOrNull("US$ 100"));
    }

    @Test
    void parseOrNullHandlesAccountingNegativeFormat(){
        assertEquals(new BigDecimal("-50.00"), Money.parseOrNull("(50.00)"));
        assertEquals(new BigDecimal("-50.00"), Money.parseOrNull("R$ (50.00)"));
    }

    @Test
    void parseOrNullRejectsExcessiveDecimalPrecision() {
        assertNull(Money.parseOrNull("100.12345"));
        assertEquals(new BigDecimal("100.12"), Money.parseOrNull("100.1234"));
    }
    
}
