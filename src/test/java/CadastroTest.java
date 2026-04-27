import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.mycompany.a.Cadastro;

public class CadastroTest {

    @Test
    public void deveRetornarOkQuandoTodosOsCamposEstaoPreenchidos() {
        Cadastro cad = new Cadastro();

        String resultado = cad.validarCadastro("João", "joao@email.com", "123456");

        assertEquals("OK", resultado);
    }

    @Test
    public void deveRetornarErroQuandoNomeForVazio() {
        Cadastro cad = new Cadastro();

        String resultado = cad.validarCadastro("", "joao@email.com", "123456");

        assertEquals("Nome inválido.", resultado);
    }

    @Test
    public void deveRetornarErroQuandoEmailForVazio() {
        Cadastro cad = new Cadastro();

        String resultado = cad.validarCadastro("João", "", "123456");

        assertEquals("Email inválido.", resultado);
    }

    @Test
    public void deveRetornarErroQuandoSenhaForVazia() {
        Cadastro cad = new Cadastro();

        String resultado = cad.validarCadastro("João", "joao@email.com", "");

        assertEquals("Senha inválida.", resultado);
    }

    @Test
    public void deveRetornarErroDeNomeQuandoTodosOsCamposForemNulos() {
        Cadastro cad = new Cadastro();

        String resultado = cad.validarCadastro(null, null, null);

        assertEquals("Nome inválido.", resultado);
    }
}
