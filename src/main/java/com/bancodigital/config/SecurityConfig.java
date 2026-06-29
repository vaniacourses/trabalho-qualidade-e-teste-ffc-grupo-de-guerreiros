package com.bancodigital.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // Este valor unico mantem todas as etapas da autenticacao na mesma rota.
    @SuppressWarnings("java:S1075") // Trata-se de rota interna do sistema, nao de endereco externo configuravel.
    private static final String LOGIN_PATH = "/login";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(LOGIN_PATH, "/signup", "/css/**", "/js/**", "/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage(LOGIN_PATH)
                .loginProcessingUrl(LOGIN_PATH)
                .usernameParameter("email")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl(LOGIN_PATH + "?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl(LOGIN_PATH + "?logout")
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
            );
        return http.build();
    }
}
