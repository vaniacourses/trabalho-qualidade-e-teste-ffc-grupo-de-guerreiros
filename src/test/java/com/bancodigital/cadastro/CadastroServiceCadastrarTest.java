package com.bancodigital.cadastro;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bancodigital.conta.Conta;
import com.bancodigital.conta.ContaRepositoryFake;
import com.bancodigital.login.Usuario;
import com.bancodigital.login.UsuarioRepositoryFake;
import com.bancodigital.shared.Mensagens;
import com.bancodigital.shared.exception.DomainException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CadastroServiceCadastrarTest {

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder(4);

    private UsuarioRepositoryFake usuarioFake;
    private ContaRepositoryFake contaFake;
    private CadastroService service;

    @BeforeEach
    void setUp() {
        usuarioFake = new UsuarioRepositoryFake();
        contaFake = new ContaRepositoryFake();
        service = new CadastroService(usuarioFake, contaFake, ENCODER);
    }

    private CadastroForm form(String nome, String email, String senha) {
        CadastroForm f = new CadastroForm();
        f.setNome(nome);
        f.setEmail(email);
        f.setSenha(senha);
        return f;
    }

    @Test
    void cadastroCompletoPersisteUsuarioEConta() {
        service.cadastrar(form("João Silva", "joao@email.com", "senha1234"));

        Optional<Usuario> usuario = usuarioFake.findByEmail("joao@email.com");
        assertTrue(usuario.isPresent());
        assertEquals("João Silva", usuario.get().nome());

        Optional<Conta> conta = contaFake.findByUsuarioId(usuario.get().id());
        assertTrue(conta.isPresent(), "conta deve ser criada");
        assertNotNull(conta.get().numero());
        assertEquals(0, conta.get().saldo().signum(), "saldo inicial deve ser 0");
    }

    @Test
    void cadastroAplicaBcryptNaSenha() {
        service.cadastrar(form("João Silva", "joao@email.com", "senha1234"));

        String hash = usuarioFake.findByEmail("joao@email.com").get().senhaHash();
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"),
                "esperava BCrypt hash, recebi: " + hash);
        assertTrue(ENCODER.matches("senha1234", hash));
    }

    @Test
    void cadastroNormalizaEmailComTrim() {
        service.cadastrar(form("  João  ", "  joao@email.com  ", "senha1234"));

        assertTrue(usuarioFake.findByEmail("joao@email.com").isPresent());
        assertEquals("João", usuarioFake.findByEmail("joao@email.com").get().nome());
    }

    @Test
    void cadastroComNomeVazioFalha() {
        DomainException ex = assertThrows(DomainException.class,
                () -> service.cadastrar(form("", "joao@email.com", "senha1234")));
        assertEquals(Mensagens.NOME_INVALIDO, ex.getMessage());
    }

    @Test
    void cadastroComEmailInvalidoFalha() {
        DomainException ex = assertThrows(DomainException.class,
                () -> service.cadastrar(form("João", "naoehemail", "senha1234")));
        assertEquals(Mensagens.EMAIL_INVALIDO, ex.getMessage());
    }

    @Test
    void cadastroComSenhaCurtaFalha() {
        DomainException ex = assertThrows(DomainException.class,
                () -> service.cadastrar(form("João", "joao@email.com", "curta")));
        assertEquals(Mensagens.SENHA_CURTA, ex.getMessage());
    }

    @Test
    void cadastroDuplicadoFalha() {
        service.cadastrar(form("João Silva", "joao@email.com", "senha1234"));
        DomainException ex = assertThrows(DomainException.class,
                () -> service.cadastrar(form("Outro João", "joao@email.com", "senha1234")));
        assertEquals(Mensagens.EMAIL_DUPLICADO, ex.getMessage());
    }

    @Test
    void cadastroGeraNumerosDeContaUnicos() {
        service.cadastrar(form("João", "joao@email.com", "senha1234"));
        service.cadastrar(form("Maria", "maria@email.com", "senha1234"));

        long idJoao = usuarioFake.findByEmail("joao@email.com").get().id();
        long idMaria = usuarioFake.findByEmail("maria@email.com").get().id();

        String numJoao = contaFake.findByUsuarioId(idJoao).get().numero();
        String numMaria = contaFake.findByUsuarioId(idMaria).get().numero();

        assertTrue(numJoao.startsWith("C"));
        assertTrue(numMaria.startsWith("C"));
        assertEquals(false, numJoao.equals(numMaria), "números devem ser distintos");
    }
}
