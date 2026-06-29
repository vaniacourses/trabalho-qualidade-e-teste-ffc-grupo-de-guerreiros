package com.bancodigital.integration;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

// Este teste herda AbstractIntegrationTest para subir o contexto Spring completo,
// aplicar as migrations Flyway e usar MockMvc com os filtros reais do Spring Security.
class LoginIntegrationTest extends AbstractIntegrationTest {

    // As constantes deixam claro qual credencial esta sendo semeada no banco.
    // O hash BCrypt foi usado porque o login real compara a senha pelo PasswordEncoder.
    private static final String EMAIL = "joao@email.com";
    private static final String PASSWORD = "senha123";
    private static final String BCRYPT_SENHA123 =
            "$2a$10$fy1UbQcOh5tYVPpfzhX5ceRqLpA1OGa7hsalIwmD2oiNXrnlbSu66";

    @Test
    void loginPageShowsSignupMessageFromController() throws Exception {
        // O GET exercita diretamente o LoginController, validando a view e o
        // atributo de mensagem gerado quando o usuario chega apos cadastro.
        mockMvc.perform(get("/login").param("signup", ""))
               .andExpect(status().isOk())
               .andExpect(view().name("login"))
               .andExpect(model().attributeExists("message"));
    }

    @Test
    void loginWithValidCredentialsAuthenticatesAndRedirectsToDashboard() throws Exception {
        // O usuario e inserido no banco real para testar a integracao entre
        // SecurityFilterChain, CustomUserDetailsService, JdbcUserRepository e BCrypt.
        insertUser("Joao Silva", EMAIL, BCRYPT_SENHA123);

        // O envio inclui a protecao CSRF exigida pelo Spring Security, confirmando
        // que o teste atravessa a mesma barreira aplicada ao formulario real.
        MvcResult result = mockMvc.perform(post("/login")
                .param("email", EMAIL)
                .param("password", PASSWORD)
                .with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/dashboard"))
               .andExpect(authenticated().withUsername(EMAIL))
               .andReturn();

        // A sessao e validada para garantir que o login criou estado autenticado
        // reutilizavel nos proximos requests protegidos.
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        mockMvc.perform(get("/dashboard").session(session))
               .andExpect(status().isOk());
    }

    @Test
    void loginWithInvalidPasswordRedirectsBackWithError() throws Exception {
        // O mesmo usuario real e usado para isolar o erro somente na senha,
        // evitando falso positivo causado por usuario inexistente.
        insertUser("Joao Silva", EMAIL, BCRYPT_SENHA123);

        // A expectativa unauthenticated() comprova que a falha de login nao
        // deixa uma autenticacao residual na sessao do MockMvc.
        mockMvc.perform(post("/login")
                .param("email", EMAIL)
                .param("password", "senha-errada")
                .with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/login?error"))
               .andExpect(unauthenticated());
    }
}
