import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

import com.mycompany.a.Investimento;

public class InvestimentoTest {

    private static final BigDecimal CEM = new BigDecimal("100.00");
    private static final BigDecimal CINQUENTA = new BigDecimal("50.00");
    private static final BigDecimal DUZENTOS = new BigDecimal("200.00");
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    @Test
    public void deveRetornarValorInalteradoQuandoMinutosForemZero() {
        Investimento inv = new Investimento();

        BigDecimal resultado = inv.calcularValorComJuros(CEM, 0);

        assertEquals(0, resultado.compareTo(CEM));
    }

    @Test
    public void deveRetornarValorInalteradoQuandoMinutosForemNegativos() {
        Investimento inv = new Investimento();

        BigDecimal resultado = inv.calcularValorComJuros(CEM, -10);

        assertEquals(0, resultado.compareTo(CEM));
    }

    @Test
    public void deveAplicarJurosDeUmPorCentoEmUmMinuto() {
        Investimento inv = new Investimento();

        BigDecimal resultado = inv.calcularValorComJuros(CEM, 1);

        assertEquals(0, resultado.compareTo(new BigDecimal("101.00")));
    }

    @Test
    public void deveAplicarJurosCompostosEmCincoMinutos() {
        Investimento inv = new Investimento();

        // 100.00 * 1.01^5 = 105.10100..., scale 2 HALF_UP = 105.10
        BigDecimal resultado = inv.calcularValorComJuros(CEM, 5);

        assertEquals(0, resultado.compareTo(new BigDecimal("105.10")));
    }

    @Test
    public void deveRetornarZeroQuandoValorInicialForZero() {
        Investimento inv = new Investimento();

        BigDecimal resultado = inv.calcularValorComJuros(ZERO, 60);

        assertEquals(0, resultado.compareTo(ZERO));
    }

    @Test
    public void deveRetornarNullQuandoInvestirComSaldoSuficiente() {
        Investimento inv = new Investimento();

        String resultado = inv.validarOperacao("investir", CINQUENTA, CEM, ZERO);

        assertNull(resultado);
    }

    @Test
    public void deveRetornarNullQuandoInvestirComSaldoExatoAoValor() {
        Investimento inv = new Investimento();

        String resultado = inv.validarOperacao("investir", CEM, CEM, ZERO);

        assertNull(resultado);
    }

    @Test
    public void deveRetornarErroQuandoInvestirComSaldoInsuficiente() {
        Investimento inv = new Investimento();

        String resultado = inv.validarOperacao("investir", DUZENTOS, CEM, ZERO);

        assertEquals("Saldo insuficiente na conta.", resultado);
    }

    @Test
    public void deveRetornarNullQuandoRetirarComValorInvestidoSuficiente() {
        Investimento inv = new Investimento();

        String resultado = inv.validarOperacao("retirar", CINQUENTA, ZERO, CEM);

        assertNull(resultado);
    }

    @Test
    public void deveRetornarErroQuandoRetirarMaiorQueValorInvestido() {
        Investimento inv = new Investimento();

        String resultado = inv.validarOperacao("retirar", DUZENTOS, ZERO, CEM);

        assertEquals("Valor maior que o investido.", resultado);
    }

    @Test
    public void deveRetornarErroQuandoOperacaoForDesconhecida() {
        Investimento inv = new Investimento();

        String resultado = inv.validarOperacao("transferir", CINQUENTA, CEM, CEM);

        assertEquals("Operação inválida.", resultado);
    }

    @Test
    public void deveRetornarErroQuandoValorForZero() {
        Investimento inv = new Investimento();

        String resultado = inv.validarOperacao("investir", ZERO, CEM, CEM);

        assertEquals("Valor inválido.", resultado);
    }

    @Test
    public void deveRetornarErroQuandoValorForNegativo() {
        Investimento inv = new Investimento();

        String resultado = inv.validarOperacao("investir", new BigDecimal("-10.00"), CEM, CEM);

        assertEquals("Valor inválido.", resultado);
    }

    @Test
    public void deveRetornarErroQuandoValorForNulo() {
        Investimento inv = new Investimento();

        String resultado = inv.validarOperacao("investir", null, CEM, CEM);

        assertEquals("Valor inválido.", resultado);
    }
}
