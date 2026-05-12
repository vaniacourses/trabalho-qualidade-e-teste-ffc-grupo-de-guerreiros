package com.bancodigital.conta;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bancodigital.shared.Mensagens;
import com.bancodigital.shared.exception.DomainException;
import com.bancodigital.shared.money.Money;
import com.bancodigital.transacao.TransacaoRepository;

@Service
public class ContaService {

    public static final BigDecimal LIMITE_SAQUE_DIARIO = new BigDecimal("10000.00");

    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;

    public ContaService(ContaRepository contaRepository, TransacaoRepository transacaoRepository) {
        this.contaRepository = contaRepository;
        this.transacaoRepository = transacaoRepository;
    }

    public Conta consultarConta(long usuarioId) {
        return contaRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new DomainException("Conta não encontrada."));
    }

    public String validaSaque(BigDecimal saldo, BigDecimal valor) {
        if (!Money.isPositive(valor)) return Mensagens.VALOR_INVALIDO;
        if (valor.compareTo(LIMITE_SAQUE_DIARIO) > 0) return Mensagens.LIMITE_SAQUE_EXCEDIDO;
        if (saldo == null || saldo.compareTo(valor) < 0) return Mensagens.SALDO_INSUFICIENTE;
        return "OK";
    }

    public String validarDeposito(BigDecimal valor) {
        if (!Money.isPositive(valor)) return Mensagens.VALOR_INVALIDO;
        return "OK";
    }

    public String validarTransferencia(BigDecimal valor, String numDestino, String numOrigem,
                                       BigDecimal saldoOrigem, boolean destinoExiste) {
        if (!Money.isPositive(valor)) return Mensagens.VALOR_OU_CONTA_INVALIDOS;
        if (numDestino == null || numDestino.trim().isEmpty()) return Mensagens.VALOR_OU_CONTA_INVALIDOS;
        if (numOrigem != null && numOrigem.equals(numDestino.trim())) return Mensagens.MESMA_CONTA;
        if (!destinoExiste) return Mensagens.CONTA_DESTINO_INVALIDA;
        if (saldoOrigem == null || saldoOrigem.compareTo(valor) < 0) return Mensagens.SALDO_INSUFICIENTE;
        return "OK";
    }

    @Transactional
    public void sacar(long usuarioId, BigDecimal valorBruto) {
        BigDecimal valor = Money.normalize(valorBruto);
        Conta resumo = consultarConta(usuarioId);
        Conta conta = contaRepository.findByIdForUpdate(resumo.id())
                .orElseThrow(() -> new DomainException("Conta não encontrada."));
        String erro = validaSaque(conta.saldo(), valor);
        if (!"OK".equals(erro)) throw new DomainException(erro);
        contaRepository.debitar(conta.id(), valor);
        transacaoRepository.registrarSaque(conta.id(), valor);
    }

    @Transactional
    public void depositar(long usuarioId, BigDecimal valorBruto) {
        BigDecimal valor = Money.normalize(valorBruto);
        Conta resumo = consultarConta(usuarioId);
        String erro = validarDeposito(valor);
        if (!"OK".equals(erro)) throw new DomainException(erro);
        Conta conta = contaRepository.findByIdForUpdate(resumo.id())
                .orElseThrow(() -> new DomainException("Conta não encontrada."));
        contaRepository.creditar(conta.id(), valor);
        transacaoRepository.registrarDeposito(conta.id(), valor);
    }

    @Transactional
    public void transferir(long usuarioId, String numeroDestino, BigDecimal valorBruto) {
        BigDecimal valor = Money.normalize(valorBruto);
        Conta origemResumo = consultarConta(usuarioId);
        String destinoTrim = numeroDestino == null ? null : numeroDestino.trim();
        Conta destino = (destinoTrim == null || destinoTrim.isEmpty())
                ? null
                : contaRepository.findByNumero(destinoTrim).orElse(null);
        String erro = validarTransferencia(valor, destinoTrim, origemResumo.numero(),
                origemResumo.saldo(), destino != null);
        if (!"OK".equals(erro)) throw new DomainException(erro);

        long origemId = origemResumo.id();
        long destinoId = destino.id();
        long primeiro = Math.min(origemId, destinoId);
        long segundo = Math.max(origemId, destinoId);
        Conta first = contaRepository.findByIdForUpdate(primeiro)
                .orElseThrow(() -> new DomainException("Conta não encontrada."));
        Conta second = contaRepository.findByIdForUpdate(segundo)
                .orElseThrow(() -> new DomainException("Conta não encontrada."));
        Conta origemAtual = origemId == first.id() ? first : second;
        if (origemAtual.saldo() == null || origemAtual.saldo().compareTo(valor) < 0) {
            throw new DomainException(Mensagens.SALDO_INSUFICIENTE);
        }
        contaRepository.debitar(origemId, valor);
        contaRepository.creditar(destinoId, valor);
        transacaoRepository.registrarTransferencia(origemId, destinoId, valor);
    }
}
