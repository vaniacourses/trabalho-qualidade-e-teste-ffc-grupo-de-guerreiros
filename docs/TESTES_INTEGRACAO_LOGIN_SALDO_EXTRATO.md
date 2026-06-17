# Testes de integracao: Login, Saldo e Extrato

## Objetivo

Este documento descreve os testes de integracao criados para os controllers `LoginController`, `BalanceController` e `StatementController`.

## Abordagem usada

Foi usado `@SpringBootTest` com `MockMvc`, herdando de `AbstractIntegrationTest`, porque essa combinacao sobe o contexto real do Spring, aplica as migrations Flyway, ativa os filtros do Spring Security e permite chamar as rotas HTTP sem abrir navegador.

O banco usado e o PostgreSQL de teste configurado em `application-integration-test.yml`. Isso valida SQL, repositories, services, constraints e conversao dos dados reais em objetos do model MVC.

`JdbcTemplate` foi usado apenas para preparar e consultar dados de teste. Essa abordagem evita depender de outros fluxos da aplicacao para montar o estado inicial de cada cenario.

## Classes criadas

### `LoginIntegrationTest`

Valida:

- GET `/login?signup`: confirma a view `login` e a mensagem exibida pelo `LoginController`.
- POST `/login` com credenciais validas (`senha123`): confirma autenticacao real, redirect para `/dashboard` e criacao de sessao.
- POST `/login` com senha invalida: confirma redirect para `/login?error` e ausencia de autenticacao.

Bibliotecas/recursos usados:

- `MockMvc`: simula requests HTTP dentro do contexto Spring.
- `spring-security-test`: fornece `csrf()`, `authenticated()` e `unauthenticated()`.
- BCrypt real no banco: garante que o login passe pelo `PasswordEncoder`.

### `BalanceIntegrationTest`

Valida:

- GET `/balance` autenticado: confirma view `balance` e conta carregada do banco no atributo `account`.
- GET `/balance` anonimo: confirma redirecionamento para login.

Bibliotecas/recursos usados:

- `@WithMockUser`: cria um usuario autenticado no contexto de seguranca do teste.
- `JdbcTemplate`: cria o usuario e a conta antes do request.
- Hamcrest `is`: valida o `Account` esperado diretamente, aproveitando a igualdade nativa dos records Java.

### `StatementIntegrationTest`

Valida:

- GET `/statement` autenticado com transacoes: confirma view `statement`, conta atual e lista `lines` com deposito, saque e transferencia.
- GET `/statement` autenticado sem transacoes: confirma lista vazia.
- GET `/statement` anonimo: confirma redirecionamento para login.

Bibliotecas/recursos usados:

- `MockMvc`: testa controller, service, repository e seguranca no mesmo request.
- `JdbcTemplate`: insere transacoes controladas diretamente no banco.
- Hamcrest `hasSize`: valida o tamanho da lista no model.
- `ModelAndView`: permite ler `Account` e `StatementLine` diretamente, usando os acessores de records Java (`number()`, `type()`, `amount()`).

## Como executar

Com o PostgreSQL de teste ativo:

```bash
mvn test -Dtest='LoginIntegrationTest,BalanceIntegrationTest,StatementIntegrationTest'
```

Ou junto com toda a suite:

```bash
mvn test
```

## Resultado esperado

Todos os testes devem passar, comprovando que:

- O login autentica com usuario persistido e senha BCrypt.
- As paginas de saldo e extrato exigem autenticacao.
- O saldo exibido vem da conta persistida.
- O extrato transforma transacoes persistidas em linhas de exibicao.
