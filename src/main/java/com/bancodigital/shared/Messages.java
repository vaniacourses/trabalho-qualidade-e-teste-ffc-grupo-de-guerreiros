package com.bancodigital.shared;

public final class Messages {

    public static final String INVALID_AMOUNT = "Valor inválido.";
    // A mensagem compartilhada evita literais duplicados nos serviços e mantém
    // consistente o erro apresentado quando uma conta deixa de existir.
    public static final String ACCOUNT_NOT_FOUND = "Conta não encontrada.";
    public static final String INSUFFICIENT_BALANCE = "Saldo insuficiente.";
    public static final String WITHDRAW_LIMIT_EXCEEDED = "Limite máximo por saque excedido.";
    public static final String SAME_ACCOUNT = "Não é possível transferir para a própria conta.";
    public static final String INVALID_DESTINATION_ACCOUNT = "Conta destino não encontrada.";
    public static final String INVALID_AMOUNT_OR_ACCOUNT = "Valor ou conta inválidos.";
    public static final String INVALID_OPERATION = "Operação inválida.";
    public static final String INSUFFICIENT_ACCOUNT_BALANCE = "Saldo insuficiente na conta.";
    public static final String AMOUNT_EXCEEDS_INVESTED = "Valor maior que o investido.";

    public static final String INVALID_NAME = "Nome inválido.";
    public static final String INVALID_EMAIL = "E-mail inválido.";
    public static final String PASSWORD_TOO_SHORT = "Senha deve ter ao menos 8 caracteres.";
    public static final String DUPLICATE_EMAIL = "E-mail já cadastrado.";

    public static final String WITHDRAW_SUCCESS = "Saque realizado com sucesso!";
    public static final String DEPOSIT_SUCCESS = "Depósito realizado com sucesso!";
    public static final String TRANSFER_SUCCESS = "Transferência realizada com sucesso!";
    public static final String INVESTMENT_SUCCESS = "Investimento realizado com sucesso!";
    public static final String REDEMPTION_SUCCESS = "Resgate realizado com sucesso!";
    public static final String SIGNUP_SUCCESS = "Cadastro realizado. Faça login para continuar.";

    private Messages() {}
}
