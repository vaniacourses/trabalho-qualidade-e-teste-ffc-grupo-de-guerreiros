package com.bancodigital.investment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bancodigital.account.Account;
import com.bancodigital.account.AccountRepository;
import com.bancodigital.shared.Messages;
import com.bancodigital.shared.exception.DomainException;
import com.bancodigital.shared.money.Money;
import com.bancodigital.transaction.TransactionRepository;

@Service
public class InvestmentService {

    public static final BigDecimal INTEREST_RATE_PER_MINUTE = new BigDecimal("1.01");

    public enum Operation {
        INVEST, WITHDRAW;

        public static Operation parse(String raw) {
            if (raw == null) return null;
            if ("investir".equalsIgnoreCase(raw)) return INVEST;
            if ("retirar".equalsIgnoreCase(raw)) return WITHDRAW;
            return null;
        }
    }

    private final InvestmentRepository investmentRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final Clock clock;

    public InvestmentService(InvestmentRepository investmentRepository,
                             AccountRepository accountRepository,
                             TransactionRepository transactionRepository,
                             Clock clock) {
        this.investmentRepository = investmentRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.clock = clock;
    }

    public BigDecimal calculateInterest(BigDecimal currentAmount, long minutes) {
        if (currentAmount == null) return null;
        if (minutes <= 0) return currentAmount;
        BigDecimal factor = INTEREST_RATE_PER_MINUTE.pow((int) Math.min(minutes, Integer.MAX_VALUE));
        return currentAmount.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    public String validateOperation(String op, BigDecimal amount, BigDecimal accountBalance, BigDecimal investedAmount) {
        Operation operation = Operation.parse(op);
        if (operation == null) return Messages.INVALID_OPERATION;
        if (!Money.isPositive(amount)) return Messages.INVALID_AMOUNT;
        if (operation == Operation.INVEST && accountBalance.compareTo(amount) < 0) return Messages.INSUFFICIENT_ACCOUNT_BALANCE;
        if (operation == Operation.WITHDRAW && investedAmount.compareTo(amount) < 0) return Messages.AMOUNT_EXCEEDS_INVESTED;
        return null;
    }

    @Transactional
    public BigDecimal query(long userId) {
        investmentRepository.ensureExists(userId);
        return applyInterestIfNeeded(userId);
    }

    @Transactional
    public void execute(long userId, String op, BigDecimal rawAmount) {
        BigDecimal amount = Money.normalize(rawAmount);
        investmentRepository.ensureExists(userId);
        BigDecimal investedAmount = applyInterestIfNeeded(userId);

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new DomainException("Conta não encontrada."));
        Account lockedAccount = accountRepository.findByIdForUpdate(account.id())
                .orElseThrow(() -> new DomainException("Conta não encontrada."));

        String error = validateOperation(op, amount, lockedAccount.balance(), investedAmount);
        if (error != null) throw new DomainException(error);

        OffsetDateTime now = OffsetDateTime.now(clock);
        Operation operation = Operation.parse(op);
        if (operation == Operation.INVEST) {
            accountRepository.debit(lockedAccount.id(), amount);
            investmentRepository.update(userId, investedAmount.add(amount), now);
            transactionRepository.recordInvestment(lockedAccount.id(), amount);
        } else {
            accountRepository.credit(lockedAccount.id(), amount);
            investmentRepository.update(userId, investedAmount.subtract(amount), now);
            transactionRepository.recordRedemption(lockedAccount.id(), amount);
        }
    }

    private BigDecimal applyInterestIfNeeded(long userId) {
        Investment inv = investmentRepository.findByUserId(userId)
                .orElseThrow(() -> new DomainException("Investimento não encontrado."));
        OffsetDateTime now = OffsetDateTime.now(clock).withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime last = inv.lastUpdate().withOffsetSameInstant(ZoneOffset.UTC);
        long minutes = Duration.between(last, now).toMinutes();
        if (minutes <= 0) return inv.amount();
        BigDecimal updated = calculateInterest(inv.amount(), minutes);
        investmentRepository.update(userId, updated, now);
        return updated;
    }
}
