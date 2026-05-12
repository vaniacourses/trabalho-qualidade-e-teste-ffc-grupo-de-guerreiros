package com.bancodigital.account;

import java.math.BigDecimal;

public record Account(Long id, String number, BigDecimal balance, Long userId) {
}
