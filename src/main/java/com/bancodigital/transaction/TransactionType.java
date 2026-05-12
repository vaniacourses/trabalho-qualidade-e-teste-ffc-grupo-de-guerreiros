package com.bancodigital.transaction;

public enum TransactionType {
    DEPOSITO("deposito"),
    SAQUE("saque"),
    TRANSFERENCIA("transferencia"),
    INVESTIMENTO("investimento"),
    RESGATE("resgate");

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
        throw new IllegalArgumentException("Tipo de transacao desconhecido: " + value);
    }
}
