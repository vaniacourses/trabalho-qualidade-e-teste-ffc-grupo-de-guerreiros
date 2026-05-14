package com.bancodigital.integration;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SignupIntegrationTest extends AbstractIntegrationTest {

    @Test
    void signupEndpointCreatesUserAndAccount() throws Exception {
        mockMvc.perform(post("/signup")
                .param("name", "Maria Souza")
                .param("email", "maria@email.com")
                .param("password", "senha1234")
                .with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/login?signup"));

        Long userCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ?", Long.class, "maria@email.com");
        assertEquals(1L, userCount);

        String storedHash = jdbc.queryForObject(
                "SELECT password_hash FROM users WHERE email = ?", String.class, "maria@email.com");
        assertNotEquals("senha1234", storedHash);
        assertNotNull(storedHash);

        Long userId = jdbc.queryForObject(
                "SELECT id FROM users WHERE email = ?", Long.class, "maria@email.com");
        Long accountCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM accounts WHERE user_id = ?", Long.class, userId);
        assertEquals(1L, accountCount);

        String accountNumber = jdbc.queryForObject(
                "SELECT number FROM accounts WHERE user_id = ?", String.class, userId);
        assertNotNull(accountNumber);
    }

    @Test
    void signupEndpointRejectsDuplicateEmail() throws Exception {
        insertUser("Existente", "joao@email.com", "$2a$10$dummyhashvalue000000000000000000000000000000000000000000");
        long existingUserId = jdbc.queryForObject(
                "SELECT id FROM users WHERE email = ?", Long.class, "joao@email.com");
        insertAccount("C00099", new BigDecimal("0.00"), existingUserId);

        mockMvc.perform(post("/signup")
                .param("name", "Joao Novo")
                .param("email", "joao@email.com")
                .param("password", "senha1234")
                .with(csrf()))
               .andExpect(status().isOk());

        Long userCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ?", Long.class, "joao@email.com");
        assertEquals(1L, userCount);

        Long accountCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM accounts WHERE user_id = ?", Long.class, existingUserId);
        assertEquals(1L, accountCount);
    }

    @Test
    void signupEndpointRejectsShortPassword() throws Exception {
        mockMvc.perform(post("/signup")
                .param("name", "Maria Souza")
                .param("email", "maria@email.com")
                .param("password", "1234")
                .with(csrf()))
               .andExpect(status().isOk());

        Long userCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ?", Long.class, "maria@email.com");
        assertEquals(0L, userCount);
    }
}
