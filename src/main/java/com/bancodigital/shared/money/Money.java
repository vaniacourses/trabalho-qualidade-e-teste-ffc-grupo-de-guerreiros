package com.bancodigital.shared.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public final class Money {

    private static final Locale BRAZIL = Locale.forLanguageTag("pt-BR");

    private Money() {}

    public static BigDecimal normalize(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal parseOrNull(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim().replace(",", ".");
        if (trimmed.isEmpty()) return null;
        try {
            return normalize(new BigDecimal(trimmed));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static boolean isPositive(BigDecimal value) {
        return value != null && value.signum() > 0;
    }

    public static String format(BigDecimal value) {
        BigDecimal v = value == null ? BigDecimal.ZERO : value;
        return NumberFormat.getCurrencyInstance(BRAZIL).format(v);
    }
}
