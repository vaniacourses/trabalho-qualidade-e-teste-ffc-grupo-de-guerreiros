# Testes E2E de Login, Saldo e Extrato

## Objetivo

Os testes desta entrega validam pelo Google Chrome os fluxos relacionados a:

- `LoginController`: tela de login e mensagens de falha.
- `BalanceController`: consulta e exibicao do saldo.
- `StatementController`: consulta e exibicao do extrato.

O objetivo do E2E e verificar o comportamento que um usuario encontra no navegador, atravessando HTTP, Spring Security, controllers, services, repositories, PostgreSQL e templates Thymeleaf.

## Abordagem e bibliotecas

### Selenium WebDriver

O Selenium foi usado porque controla um navegador real. Ele permite preencher formularios, enviar o token CSRF gerado pelo Thymeleaf, acompanhar redirecionamentos, manter cookies de sessao e inspecionar o DOM final.

### WebDriverManager

O WebDriverManager configura automaticamente um ChromeDriver compativel com o Chrome instalado. Assim, nenhum executavel de driver precisa ser armazenado no repositorio.

### Spring Boot com porta aleatoria

`AbstractE2ETest` usa `@SpringBootTest(webEnvironment = RANDOM_PORT)`. A aplicacao inicia um servidor HTTP real em uma porta livre, evitando conflito com uma instancia na porta 8080.

O profile `integration-test` direciona os testes para a database PostgreSQL `bancodigital_test`.

### Page Object

Foram criados:

- `BalancePage`: encapsula numero da conta e saldo formatado.
- `StatementPage`: encapsula conta, linhas, tipos e estado vazio.
- `LoginPage`: ja existente, foi reutilizado para login valido e invalido.

Page Objects evitam repetir seletores e deixam as suites focadas nos comportamentos esperados.

### Esperas explicitas

Os testes usam `WebDriverWait` e `ExpectedConditions`. Essa abordagem reage quando a pagina ou o redirecionamento termina e e mais confiavel que `Thread.sleep`, cujo tempo pode ser insuficiente em CI ou excessivo localmente.

## Casos implementados

### `LoginE2ETest`

| Caso | Resultado esperado |
|---|---|
| Credenciais validas | Redireciona para `/dashboard` e exibe o nome do usuario |
| Senha invalida | Permanece em `/login?error` e exibe mensagem |
| E-mail inexistente | Apresenta o mesmo erro generico de seguranca |
| Rota protegida sem login | Redireciona `/dashboard` para `/login` |

### `BalanceE2ETest`

| Caso | Resultado esperado |
|---|---|
| Usuario autenticado | Exibe conta `C00001` e saldo formatado `500,00` |
| Usuario anonimo | Redireciona `/balance` para `/login` |

### `StatementE2ETest`

| Caso | Resultado esperado |
|---|---|
| Extrato com movimentos | Renderiza tres linhas em ordem `TRANSFER`, `WITHDRAW`, `DEPOSIT` |
| Extrato sem movimentos | Renderiza zero linhas e apresenta mensagem de estado vazio |

## Isolamento dos testes

Antes de cada caso, `AbstractE2ETest`:

1. Abre um navegador novo.
2. Limpa as tabelas com `TRUNCATE ... RESTART IDENTITY CASCADE`.
3. Reinicia a sequencia de contas.

Ao final, `driver.quit()` encerra o Chrome mesmo quando o teste falha.

## Como executar

O PostgreSQL de teste deve estar ativo e a database `bancodigital_test` deve existir.

Executar somente as suites desta entrega em modo headless:

```bash
mvn verify -Dheadless=true "-Dit.test=LoginE2ETest,BalanceE2ETest,StatementE2ETest"
```

Executar com o Chrome visivel:

```bash
mvn verify -Dheadless=false "-Dit.test=LoginE2ETest,BalanceE2ETest,StatementE2ETest"
```

Executar todos os testes, incluindo os demais E2E:

```bash
mvn verify -Dheadless=true
```

## Resultado esperado

As oito verificacoes devem passar:

- 4 casos de Login.
- 2 casos de Saldo.
- 2 casos de Extrato.
