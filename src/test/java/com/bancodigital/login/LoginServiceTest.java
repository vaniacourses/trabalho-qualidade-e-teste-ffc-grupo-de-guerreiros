package com.bancodigital.login;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class LoginServiceTest {

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder(4);
    private UsuarioRepositoryFake fake;
    private LoginService service;

    @BeforeEach
    void setUp() {
        fake = new UsuarioRepositoryFake();
        service = new LoginService(fake, ENCODER);
    }

    private Usuario novoUsuario(String email, String senhaPura) {
        return new Usuario(1L, "Teste", email, ENCODER.encode(senhaPura));
    }

    @Test
    void autenticaComCredenciaisCorretas() {
        fake.seed(novoUsuario("teste@email.com", "senha123"));
        Usuario u = service.autenticar("teste@email.com", "senha123");
        assertNotNull(u);
        assertEquals("teste@email.com", u.email());
    }

    @Test
    void retornaNullQuandoSenhaForVazia() {
        fake.seed(novoUsuario("teste@email.com", "senha123"));
        assertNull(service.autenticar("teste@email.com", ""));
    }

    @Test
    void retornaNullQuandoUsuarioNaoExiste() {
        assertNull(service.autenticar("inexistente@email.com", "senha123"));
    }

    @Test
    void retornaNullQuandoSenhaErrada() {
        fake.seed(novoUsuario("teste@email.com", "senha123"));
        assertNull(service.autenticar("teste@email.com", "errada"));
    }

    @Test
    void retornaNullQuandoEmailVazio() {
        assertNull(service.autenticar("", "senha123"));
    }

    @Test
    void retornaNullQuandoEmailNulo() {
        assertNull(service.autenticar(null, "senha123"));
    }

    @Test
    void retornaNullQuandoSenhaNula() {
        assertNull(service.autenticar("teste@email.com", null));
    }

    @Test
    void retornaNullQuandoEmailApenasWhitespace() {
        assertNull(service.autenticar("   ", "senha123"));
    }
}
