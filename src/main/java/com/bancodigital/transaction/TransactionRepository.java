package com.bancodigital.transaction;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionRepository {

    List<Transaction> findByAccountId(long accountId);

    void recordDeposit(long destinationAccount, BigDecimal amount);

    void recordWithdraw(long sourceAccount, BigDecimal amount);

    void recordTransfer(long source, long destination, BigDecimal amount);

    void recordInvestment(long sourceAccount, BigDecimal amount);

    void recordRedemption(long destinationAccount, BigDecimal amount);
}
