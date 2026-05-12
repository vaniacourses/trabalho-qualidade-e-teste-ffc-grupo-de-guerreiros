package com.bancodigital.account;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bancodigital.shared.Messages;
import com.bancodigital.shared.exception.DomainException;
import com.bancodigital.shared.money.Money;
import com.bancodigital.transaction.TransactionRepository;

@Service
public class AccountService {

    public static final BigDecimal DAILY_WITHDRAW_LIMIT = new BigDecimal("10000.00");

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public Account getAccount(long userId) {
        return accountRepository.findByUserId(userId)
                .orElseThrow(() -> new DomainException("Conta não encontrada."));
    }

    public String validateWithdraw(BigDecimal balance, BigDecimal amount) {
        if (!Money.isPositive(amount)) return Messages.INVALID_AMOUNT;
        if (amount.compareTo(DAILY_WITHDRAW_LIMIT) > 0) return Messages.WITHDRAW_LIMIT_EXCEEDED;
        if (balance == null || balance.compareTo(amount) < 0) return Messages.INSUFFICIENT_BALANCE;
        return "OK";
    }

    public String validateDeposit(BigDecimal amount) {
        if (!Money.isPositive(amount)) return Messages.INVALID_AMOUNT;
        return "OK";
    }

    public String validateTransfer(BigDecimal amount, String destinationNumber, String sourceNumber,
                                   BigDecimal sourceBalance, boolean destinationExists) {
        if (!Money.isPositive(amount)) return Messages.INVALID_AMOUNT_OR_ACCOUNT;
        if (destinationNumber == null || destinationNumber.trim().isEmpty()) return Messages.INVALID_AMOUNT_OR_ACCOUNT;
        if (sourceNumber != null && sourceNumber.equals(destinationNumber.trim())) return Messages.SAME_ACCOUNT;
        if (!destinationExists) return Messages.INVALID_DESTINATION_ACCOUNT;
        if (sourceBalance == null || sourceBalance.compareTo(amount) < 0) return Messages.INSUFFICIENT_BALANCE;
        return "OK";
    }

    @Transactional
    public void withdraw(long userId, BigDecimal rawAmount) {
        BigDecimal amount = Money.normalize(rawAmount);
        Account summary = getAccount(userId);
        Account account = accountRepository.findByIdForUpdate(summary.id())
                .orElseThrow(() -> new DomainException("Conta não encontrada."));
        String error = validateWithdraw(account.balance(), amount);
        if (!"OK".equals(error)) throw new DomainException(error);
        accountRepository.debit(account.id(), amount);
        transactionRepository.recordWithdraw(account.id(), amount);
    }

    @Transactional
    public void deposit(long userId, BigDecimal rawAmount) {
        BigDecimal amount = Money.normalize(rawAmount);
        Account summary = getAccount(userId);
        String error = validateDeposit(amount);
        if (!"OK".equals(error)) throw new DomainException(error);
        Account account = accountRepository.findByIdForUpdate(summary.id())
                .orElseThrow(() -> new DomainException("Conta não encontrada."));
        accountRepository.credit(account.id(), amount);
        transactionRepository.recordDeposit(account.id(), amount);
    }

    @Transactional
    public void transfer(long userId, String destinationNumber, BigDecimal rawAmount) {
        BigDecimal amount = Money.normalize(rawAmount);
        Account sourceSummary = getAccount(userId);
        String destinationTrim = destinationNumber == null ? null : destinationNumber.trim();
        Account destination = (destinationTrim == null || destinationTrim.isEmpty())
                ? null
                : accountRepository.findByNumber(destinationTrim).orElse(null);
        String error = validateTransfer(amount, destinationTrim, sourceSummary.number(),
                sourceSummary.balance(), destination != null);
        if (!"OK".equals(error)) throw new DomainException(error);

        long sourceId = sourceSummary.id();
        long destinationId = destination.id();
        long firstId = Math.min(sourceId, destinationId);
        long secondId = Math.max(sourceId, destinationId);
        Account first = accountRepository.findByIdForUpdate(firstId)
                .orElseThrow(() -> new DomainException("Conta não encontrada."));
        Account second = accountRepository.findByIdForUpdate(secondId)
                .orElseThrow(() -> new DomainException("Conta não encontrada."));
        Account currentSource = sourceId == first.id() ? first : second;
        if (currentSource.balance() == null || currentSource.balance().compareTo(amount) < 0) {
            throw new DomainException(Messages.INSUFFICIENT_BALANCE);
        }
        accountRepository.debit(sourceId, amount);
        accountRepository.credit(destinationId, amount);
        transactionRepository.recordTransfer(sourceId, destinationId, amount);
    }
}
