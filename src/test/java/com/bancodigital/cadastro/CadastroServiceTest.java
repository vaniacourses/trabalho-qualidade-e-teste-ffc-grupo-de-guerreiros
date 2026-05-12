package com.bancodigital.cadastro;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bancodigital.shared.Mensagens;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CadastroServiceTest {

    private CadastroService service;

    @BeforeEach
    void setUp() {
        service = new CadastroService(null, null, null);
    }

    @Test
    void cadastroValidoRetornaOk() {
        assertEquals("OK", service.validarCadastro("João", "joao@email.com", "12345678"));
    }

    @Test
    void nomeVazio() {
        assertEquals(Mensagens.NOME_INVALIDO, service.validarCadastro("", "joao@email.com", "12345678"));
    }

    @Test
    void nomeNulo() {
        assertEquals(Mensagens.NOME_INVALIDO, service.validarCadastro(null, "joao@email.com", "12345678"));
    }

    @Test
    void nomeApenasEspacos() {
        assertEquals(Mensagens.NOME_INVALIDO, service.validarCadastro("   ", "joao@email.com", "12345678"));
    }

    @Test
    void emailVazio() {
        assertEquals(Mensagens.EMAIL_INVALIDO, service.validarCadastro("João", "", "12345678"));
    }

    @Test
    void emailNulo() {
        assertEquals(Mensagens.EMAIL_INVALIDO, service.validarCadastro("João", null, "12345678"));
    }

    @Test
    void emailFormatoInvalido() {
        assertEquals(Mensagens.EMAIL_INVALIDO, service.validarCadastro("João", "joaoemail.com", "12345678"));
    }

    @Test
    void emailSemDominio() {
        assertEquals(Mensagens.EMAIL_INVALIDO, service.validarCadastro("João", "joao@", "12345678"));
    }

    @Test
    void senhaCurta() {
        assertEquals(Mensagens.SENHA_CURTA, service.validarCadastro("João", "joao@email.com", "1234567"));
    }

    @Test
    void senhaVazia() {
        assertEquals(Mensagens.SENHA_CURTA, service.validarCadastro("João", "joao@email.com", ""));
    }

    @Test
    void senhaNula() {
        assertEquals(Mensagens.SENHA_CURTA, service.validarCadastro("João", "joao@email.com", null));
    }

    @Test
    void senhaBoundaryMinimo() {
        assertEquals("OK", service.validarCadastro("João", "joao@email.com", "12345678"));
    }
}
