package com.bancodigital.transacao;

public enum TipoTransacao {
    DEPOSITO("deposito"),
    SAQUE("saque"),
    TRANSFERENCIA("transferencia"),
    INVESTIMENTO("investimento"),
    RESGATE("resgate");

    private final String dbValue;

    TipoTransacao(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static TipoTransacao fromDbValue(String value) {
        for (TipoTransacao t : values()) {
            if (t.dbValue.equals(value)) return t;
        }
        throw new IllegalArgumentException("Tipo de transacao desconhecido: " + value);
    }
}
