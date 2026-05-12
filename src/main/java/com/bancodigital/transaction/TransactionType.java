package com.bancodigital.transaction;

public enum TransactionType {
    DEPOSIT("deposit"),
    WITHDRAW("withdraw"),
    TRANSFER("transfer"),
    INVESTMENT("investment"),
    REDEMPTION("redemption");

    private final String dbValue;

    TransactionType(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static TransactionType fromDbValue(String value) {
        for (TransactionType t : values()) {
            if (t.dbValue.equals(value)) return t;
        }
        throw new IllegalArgumentException("Unknown transaction type: " + value);
    }
}
