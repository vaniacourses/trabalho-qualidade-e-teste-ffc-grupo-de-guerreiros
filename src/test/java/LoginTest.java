import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

import com.mycompany.a.LoginService;
import com.mycompany.a.Usuario;
import com.mycompany.a.UsuarioDAO;

public class LoginTest {

    @Test
    public void deveRetornarNullQuandoSenhaForVazia() throws SQLException {
        UsuarioDAO daoFake = new UsuarioDAOFake(null);
        LoginService service = new LoginService(daoFake);

        Usuario resultado = service.autenticar("teste@email.com", "");

        assertNull(resultado);
    }

    @Test
    public void deveRetornarNullQuandoUsuarioNaoExiste() throws SQLException {
        UsuarioDAO daoFake = new UsuarioDAOFake(null);
        LoginService service = new LoginService(daoFake);

        Usuario resultado = service.autenticar("inexistente@email.com", "123456");

        assertNull(resultado);
    }

    @Test
    public void deveRetornarUsuarioQuandoLoginForValido() throws SQLException {
        Usuario usuarioFake = new Usuario(1, "João");
        UsuarioDAO daoFake = new UsuarioDAOFake(usuarioFake);
        LoginService service = new LoginService(daoFake);

        Usuario resultado = service.autenticar("joao@email.com", "123456");

        assertNotNull(resultado);
        assertEquals(1, resultado.getId());
        assertEquals("João", resultado.getNome());
    }

    class UsuarioDAOFake extends UsuarioDAO {

        private Usuario usuarioParaRetornar;

        public UsuarioDAOFake(Usuario usuarioParaRetornar) {
            this.usuarioParaRetornar = usuarioParaRetornar;
        }

        @Override
        public Usuario buscarPorEmailESenha(String email, String senha) {
            return usuarioParaRetornar;
        }
    }
}