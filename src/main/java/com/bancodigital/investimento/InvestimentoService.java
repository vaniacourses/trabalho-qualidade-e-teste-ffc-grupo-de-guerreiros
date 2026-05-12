package com.bancodigital.investimento;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bancodigital.conta.Conta;
import com.bancodigital.conta.ContaRepository;
import com.bancodigital.shared.Mensagens;
import com.bancodigital.shared.exception.DomainException;
import com.bancodigital.shared.money.Money;
import com.bancodigital.transacao.TransacaoRepository;

@Service
public class InvestimentoService {

    public static final BigDecimal TAXA_JUROS_POR_MINUTO = new BigDecimal("1.01");

    public enum Operacao {
        INVESTIR, RETIRAR;

        public static Operacao parse(String raw) {
            if (raw == null) return null;
            if ("investir".equalsIgnoreCase(raw)) return INVESTIR;
            if ("retirar".equalsIgnoreCase(raw)) return RETIRAR;
            return null;
        }
    }

    private final InvestimentoRepository investimentoRepository;
    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;
    private final Clock clock;

    public InvestimentoService(InvestimentoRepository investimentoRepository,
                               ContaRepository contaRepository,
                               TransacaoRepository transacaoRepository,
                               Clock clock) {
        this.investimentoRepository = investimentoRepository;
        this.contaRepository = contaRepository;
        this.transacaoRepository = transacaoRepository;
        this.clock = clock;
    }

    public BigDecimal calcularValorComJuros(BigDecimal valorAtual, long minutos) {
        if (valorAtual == null) return null;
        if (minutos <= 0) return valorAtual;
        BigDecimal fator = TAXA_JUROS_POR_MINUTO.pow((int) Math.min(minutos, Integer.MAX_VALUE));
        return valorAtual.multiply(fator).setScale(2, RoundingMode.HALF_UP);
    }

    public String validarOperacao(String op, BigDecimal valor, BigDecimal saldoConta, BigDecimal valorInvestido) {
        Operacao operacao = Operacao.parse(op);
        if (operacao == null) return Mensagens.OPERACAO_INVALIDA;
        if (!Money.isPositive(valor)) return Mensagens.VALOR_INVALIDO;
        if (operacao == Operacao.INVESTIR && saldoConta.compareTo(valor) < 0) return Mensagens.SALDO_INSUFICIENTE_CONTA;
        if (operacao == Operacao.RETIRAR && valorInvestido.compareTo(valor) < 0) return Mensagens.VALOR_MAIOR_QUE_INVESTIDO;
        return null;
    }

    @Transactional
    public BigDecimal consultar(long usuarioId) {
        investimentoRepository.ensureExists(usuarioId);
        return aplicarJurosSeNecessario(usuarioId);
    }

    @Transactional
    public void executar(long usuarioId, String op, BigDecimal valorBruto) {
        BigDecimal valor = Money.normalize(valorBruto);
        investimentoRepository.ensureExists(usuarioId);
        BigDecimal valorInvestido = aplicarJurosSeNecessario(usuarioId);

        Conta conta = contaRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new DomainException("Conta não encontrada."));
        Conta contaLocked = contaRepository.findByIdForUpdate(conta.id())
                .orElseThrow(() -> new DomainException("Conta não encontrada."));

        String erro = validarOperacao(op, valor, contaLocked.saldo(), valorInvestido);
        if (erro != null) throw new DomainException(erro);

        OffsetDateTime agora = OffsetDateTime.now(clock);
        Operacao operacao = Operacao.parse(op);
        if (operacao == Operacao.INVESTIR) {
            contaRepository.debitar(contaLocked.id(), valor);
            investimentoRepository.atualizar(usuarioId, valorInvestido.add(valor), agora);
            transacaoRepository.registrarInvestimento(contaLocked.id(), valor);
        } else {
            contaRepository.creditar(contaLocked.id(), valor);
            investimentoRepository.atualizar(usuarioId, valorInvestido.subtract(valor), agora);
            transacaoRepository.registrarResgate(contaLocked.id(), valor);
        }
    }

    private BigDecimal aplicarJurosSeNecessario(long usuarioId) {
        Investimento inv = investimentoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new DomainException("Investimento não encontrado."));
        OffsetDateTime agora = OffsetDateTime.now(clock).withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime ultima = inv.ultimaAtt().withOffsetSameInstant(ZoneOffset.UTC);
        long minutos = Duration.between(ultima, agora).toMinutes();
        if (minutos <= 0) return inv.valor();
        BigDecimal novo = calcularValorComJuros(inv.valor(), minutos);
        investimentoRepository.atualizar(usuarioId, novo, agora);
        return novo;
    }
}
