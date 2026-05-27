package com.bancodigital.e2e;

import com.bancodigital.e2e.pages.SignupPage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SignupE2ETest extends AbstractE2ETest {

    @Test
    void signupWithValidDataSucceeds() {
        SignupPage page = new SignupPage(driver, baseUrl);
        page.fillAndSubmit("Maria Souza", "maria@email.com", TEST_PASSWORD);
        assertThat(page.getCurrentUrl()).contains("/login?signup");
        assertThat(page.getSuccessMessage()).isEqualTo("Cadastro realizado. Faça login para continuar.");
    }

    @Test
    void signupWithDuplicateEmailFails() {
        long userId = insertUser("Joao Silva", "joao@email.com", BCRYPT_TEST_PASSWORD);
        insertAccount("C00001", new java.math.BigDecimal("0.00"), userId);

        SignupPage page = new SignupPage(driver, baseUrl);
        page.fillAndSubmit("Joao Novo", "joao@email.com", TEST_PASSWORD);
        assertThat(page.getErrorMessage()).isEqualTo("E-mail já cadastrado.");
    }

    @Test
    void signupWithShortPasswordFails() {
        SignupPage page = new SignupPage(driver, baseUrl);
        page.fillAndSubmit("Maria Souza", "maria@email.com", "1234");
        assertThat(page.getErrorMessage()).isEqualTo("Senha deve ter ao menos 8 caracteres.");
    }

    @Test
    void signupWithBlankNameFails() {
        SignupPage page = new SignupPage(driver, baseUrl);
        page.fillAndSubmit("  ", "maria@email.com", TEST_PASSWORD);
        assertThat(page.getErrorMessage()).isEqualTo("Nome inválido.");
    }

    @Test
    void signupWithInvalidEmailFails() {
        SignupPage page = new SignupPage(driver, baseUrl);
        page.fillAndSubmit("Maria Souza", "email-sem-arroba", TEST_PASSWORD);
        assertThat(page.getErrorMessage()).isEqualTo("E-mail inválido.");
    }
}
