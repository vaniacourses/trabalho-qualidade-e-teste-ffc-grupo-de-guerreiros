package com.bancodigital.account;

import java.math.BigDecimal;
import java.util.Optional;

public interface AccountRepository {

    Optional<Account> findByUserId(long userId);

    Optional<Account> findByNumber(String number);

    Optional<Account> findByIdForUpdate(long id);

    void credit(long id, BigDecimal amount);

    void debit(long id, BigDecimal amount);

    String nextAccountNumber();

    void insert(String number, long userId);
}
