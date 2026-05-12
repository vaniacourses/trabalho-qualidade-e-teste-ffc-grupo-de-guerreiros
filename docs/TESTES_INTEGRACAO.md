# Plano — Testes de integração com PostgreSQL real

> Escopo da **Entrega 2 (parte B)**. Foco: validar SQL, migrations Flyway, constraints, transações e locking contra um **Postgres real**, usando **Testcontainers**. Cobre o que os fakes da [parte A](TESTES_UNITARIOS.md) não conseguem ver: SQL malformado, FK violadas, `ON CONFLICT`, `FOR UPDATE`, sequência de números de conta, fuso horário no `OffsetDateTime`.

---

## 1. Por que integração?

Os fakes da parte A simulam o repositório em memória — não pegam erros como:

- Coluna renomeada na migration mas não no `SELECT`.
- `INSERT` que viola `NOT NULL` ou FK.
- `ON CONFLICT (user_id) DO NOTHING` que silenciosamente não insere.
- `FOR UPDATE` em transação fora de `@Transactional`.
- `OffsetDateTime` com fuso diferente entre Java e Postgres.
- Sequence `account_number_seq` desalinhada com `UNIQUE(number)`.

Esses bugs só aparecem com o banco real. Testcontainers sobe um Postgres 16-alpine descartável por suite.

---

## 2. Stack proposta

| Componente | Versão | Justificativa |
|---|---|---|
| Testcontainers | 1.20.x | API estável, integração nativa com Spring Boot |
| `org.testcontainers:postgresql` | 1.20.x | Container Postgres pronto |
| `org.testcontainers:junit-jupiter` | 1.20.x | `@Testcontainers` + `@Container` para JUnit 5 |
| `postgres:16-alpine` | — | Mesma imagem do `docker-compose.yml` |

### Dependências a adicionar no `pom.xml`

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>1.20.4</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <version>1.20.4</version>
    <scope>test</scope>
</dependency>
```

Sem precisar de `spring-boot-testcontainers` (que puxa BOM). Conexão via `@DynamicPropertySource`.

### Classe-base proposta

```
src/test/java/com/bancodigital/integration/PostgresIntegrationTest.java
```

- `@SpringBootTest` (ou `@JdbcTest` para suítes que só tocam repositório).
- `@Testcontainers` + `@Container static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16-alpine")`.
- `@DynamicPropertySource` injeta `spring.datasource.url`, user, password.
- Flyway aplica `V1__init_schema.sql` + `V2__seed_data.sql` automaticamente no boot.
- `@Sql(scripts = "/cleanup.sql")` ou `@Transactional` rollback por método (preferir rollback).

---

## 3. Plano por repositório (testes finos com `@JdbcTest`)

`@JdbcTest` carrega só `NamedParameterJdbcTemplate` + Flyway — sobe rápido (~3s), suficiente para validar SQL.

### 3.1 `JdbcUserRepository` — [`src/main/java/com/bancodigital/auth/JdbcUserRepository.java`](../src/main/java/com/bancodigital/auth/JdbcUserRepository.java)

| # | Cenário | Verificação |
|---|---|---|
| 1 | `save("João", "j@e.com", "hash")` retorna id > 0 | `findByEmail` devolve o mesmo registro |
| 2 | `save` com e-mail já existente | Lança `DuplicateKeyException` (constraint `UNIQUE(email)`) |
| 3 | `findByEmail("inexistente@e.com")` | Retorna `Optional.empty()` |
| 4 | `findByEmail` com e-mail dos seeds (`joao@email.com`) | Retorna `User` com hash BCrypt válido |
| 5 | `existsByEmail` para e-mail existente / inexistente | `true` / `false` |
| 6 | `save` com `name` null | Lança `DataIntegrityViolationException` (coluna `NOT NULL`) |

---

### 3.2 `JdbcAccountRepository` — [`src/main/java/com/bancodigital/account/JdbcAccountRepository.java`](../src/main/java/com/bancodigital/account/JdbcAccountRepository.java)

| # | Cenário | Verificação |
|---|---|---|
| 1 | `nextAccountNumber()` chamado 3× | Devolve 3 números **distintos e crescentes** no formato `Cxxxxx` |
| 2 | `insert(number, userId)` para `user_id` inexistente | Lança violação de FK |
| 3 | `insert` com `number` duplicado | Lança violação de unique constraint |
| 4 | `findByUserId` / `findByNumber` para conta dos seeds | Retorna `Account` com saldo correto |
| 5 | `credit(id, 100.00)` após saldo inicial 50.00 | Novo saldo = 150.00 (2 casas) |
| 6 | `debit(id, 30.00)` após saldo 50.00 | Novo saldo = 20.00 |
| 7 | `debit` levando saldo a negativo | Hoje **aceita** (sem CHECK) — registrar isso como issue ou adicionar `CHECK (balance >= 0)` na migration. Teste documenta o comportamento atual |
| 8 | `findByIdForUpdate` dentro de transação | Retorna `Account`; abrir 2ª transação concorrente deveria bloquear (testado em 5.2) |

---

### 3.3 `JdbcTransactionRepository` — [`src/main/java/com/bancodigital/transaction/JdbcTransactionRepository.java`](../src/main/java/com/bancodigital/transaction/JdbcTransactionRepository.java)

| # | Cenário | Verificação |
|---|---|---|
| 1 | `recordDeposit(accountId, 100.00)` | `findByAccountId` devolve transação com `type = deposit`, `destination = accountId`, `source = null`, `date` preenchida |
| 2 | `recordWithdraw` | Idem, `type = withdraw`, `source = accountId` |
| 3 | `recordTransfer(o, d, 50.00)` | Cria uma única linha com source e destination corretos |
| 4 | `recordInvestment` / `recordRedemption` | Tipos corretos no banco; `TransactionType.fromDbValue` carrega de volta |
| 5 | `findByAccountId` retorna **DESC por data** | Inserir 3 transações com `Thread.sleep` ou `clock_timestamp()` e validar a ordem |
| 6 | `findByAccountId` retorna tx onde a conta é destino **ou** origem | Cobre o `OR` no WHERE |
| 7 | `record*` para `account_id` inexistente | Lança FK violation |

---

### 3.4 `JdbcInvestmentRepository` — [`src/main/java/com/bancodigital/investment/JdbcInvestmentRepository.java`](../src/main/java/com/bancodigital/investment/JdbcInvestmentRepository.java)

| # | Cenário | Verificação |
|---|---|---|
| 1 | `ensureExists(userId)` primeira chamada | Insere com `amount = 0`, `last_update = now()` |
| 2 | `ensureExists(userId)` segunda chamada | `ON CONFLICT DO NOTHING` → não duplica, `amount` permanece |
| 3 | `ensureExists` em paralelo (2 threads) | Constraint `UNIQUE(user_id)` + ON CONFLICT garante uma linha só (cobre issue #15) |
| 4 | `update(userId, 500.00, now)` | `findByUserId` devolve o novo valor e timestamp |
| 5 | `findByUserId` para usuário sem investimento | `Optional.empty()` |
| 6 | `last_update` round-trip Java → Postgres → Java | `OffsetDateTime` preserva instante (mesmo em fuso `-03:00`) |

---

## 4. Plano por flow de serviço (testes médios com `@SpringBootTest`)

Sobem o contexto Spring inteiro, exercitam Service + Repository + Postgres + transação real.

### 4.1 `SignupService.register` — [`src/main/java/com/bancodigital/signup/SignupService.java`](../src/main/java/com/bancodigital/signup/SignupService.java)

| # | Cenário | Verificação |
|---|---|---|
| 1 | Cadastro feliz | Usuário gravado, conta criada com número da sequence, saldo 0 |
| 2 | E-mail duplicado | Lança `DomainException`; **nenhum usuário novo, nenhuma conta nova** (rollback) |
| 3 | `accountRepository.insert` falha (simular FK quebrada via SQL inválido em teste isolado) | Usuário também não fica → cobre `@Transactional` rollback (issue #13) |
| 4 | Senha vai BCrypted no banco | `password_hash` começa com `$2a$10$` e não é igual ao texto puro |

### 4.2 `AccountService.transfer` — [`src/main/java/com/bancodigital/account/AccountService.java`](../src/main/java/com/bancodigital/account/AccountService.java)

| # | Cenário | Verificação |
|---|---|---|
| 1 | Transferência feliz entre duas contas dos seeds | Origem cai, destino sobe, transação registrada |
| 2 | Saldo insuficiente | Lança exceção, **saldos inalterados**, **nenhuma transação** (rollback) |
| 3 | Mesma conta (origem == destino) | `DomainException(SAME_ACCOUNT)` antes de tocar no banco |
| 4 | Concorrência: 2 transferências paralelas da mesma conta com saldo 100, valor 80 cada | Uma passa, outra falha por saldo insuficiente; saldo final ≥ 0 (cobre `FOR UPDATE`) |
| 5 | Ordem de lock determinística | Verificar via logs que `findByIdForUpdate` é chamado em `Math.min/Math.max` (previne deadlock) |

### 4.3 `AccountService.deposit` / `.withdraw`

| # | Cenário | Verificação |
|---|---|---|
| 1 | Depósito feliz | Saldo aumenta exato, transação registrada |
| 2 | Saque feliz | Saldo cai, transação registrada |
| 3 | Saque acima do limite diário | Exceção, saldo intacto |
| 4 | Saque com saldo insuficiente | Exceção, saldo intacto |

### 4.4 `InvestmentService.execute` — [`src/main/java/com/bancodigital/investment/InvestmentService.java`](../src/main/java/com/bancodigital/investment/InvestmentService.java)

Para controlar o tempo, injetar `Clock` mutável via `@TestConfiguration`:

| # | Cenário | Verificação |
|---|---|---|
| 1 | Investir 100 com saldo 200 | Conta cai para 100, investimento sobe para 100, transação `investment` |
| 2 | Investir, avançar clock 60 min, query | Investimento ≈ `100 * 1.01^60 ≈ 181.67` (juros aplicados pela `applyInterestIfNeeded`) |
| 3 | Resgatar mais que o investido | Exceção, saldos intactos |
| 4 | Investir + resgatar tudo | Saldo da conta volta ao original, investimento zera |
| 5 | Saldo insuficiente para investir | Exceção, **conta não debitada** |

---

## 5. Plano web (E2E com `MockMvc` ou `TestRestTemplate`)

Sobe a aplicação inteira (controllers + security + templates). Útil para garantir CSRF, autenticação, fluxo de redirect.

### 5.1 Auth + Signup

| # | Cenário | Verificação |
|---|---|---|
| 1 | GET `/signup` anônimo | 200, formulário com token CSRF |
| 2 | POST `/signup` válido | 302 → `/login?signup`, novo usuário no banco |
| 3 | POST `/signup` com e-mail duplicado | 200 com mensagem de erro, **sem novo usuário** |
| 4 | POST `/login` com credencial dos seeds | 302 → `/dashboard`, sessão criada |
| 5 | POST `/login` com senha errada | 302 → `/login?error` |
| 6 | GET `/dashboard` anônimo | 302 → `/login` |

### 5.2 Operações autenticadas (via `@WithUserDetails("joao@email.com")`)

| # | Cenário | Verificação |
|---|---|---|
| 1 | POST `/deposit` valor válido | 302 → `/deposit`, flash de sucesso, saldo atualizado |
| 2 | POST `/withdraw` valor inválido | Flash de erro, saldo intacto |
| 3 | POST `/transfer` conta inexistente | Flash de erro |
| 4 | POST `/investment` operação inválida | Flash de erro |
| 5 | GET `/statement` | 200, lista transações ordenada DESC |
| 6 | POST sem CSRF token | 403 |

---

## 6. Estrutura de pastas proposta

```
src/test/java/com/bancodigital/
├── integration/
│   ├── PostgresIntegrationTest.java           ← classe-base com @Testcontainers
│   ├── repository/
│   │   ├── JdbcUserRepositoryIT.java          ← ~6 casos
│   │   ├── JdbcAccountRepositoryIT.java       ← ~8 casos
│   │   ├── JdbcTransactionRepositoryIT.java   ← ~7 casos
│   │   └── JdbcInvestmentRepositoryIT.java    ← ~6 casos
│   ├── service/
│   │   ├── SignupServiceIT.java               ← ~4 casos
│   │   ├── AccountServiceTransferIT.java      ← ~5 casos
│   │   ├── AccountServiceDepositWithdrawIT.java ← ~4 casos
│   │   └── InvestmentServiceIT.java           ← ~5 casos
│   └── web/
│       ├── SignupLoginIT.java                 ← ~6 casos
│       └── OperationsIT.java                  ← ~6 casos
```

**Meta: ~57 testes de integração** verdes em CI.

---

## 7. Convenções

- Sufixo `IT` (Integration Test), não `Test` — separar do unitário no surefire/failsafe.
- Configurar `maven-failsafe-plugin` para rodar `**/*IT.java` numa fase separada (`mvn verify`).
- **Reuso de container**: `@Container static` + `withReuse(true)` + `~/.testcontainers.properties` com `testcontainers.reuse.enable=true` — derruba o tempo de suíte de ~120 s para ~15 s no segundo run.
- Limpeza entre testes: preferir `@Transactional` (rollback automático em `@SpringBootTest`) a `TRUNCATE`. Para repositórios com sequence (`account_number_seq`), usar `@Sql` com `ALTER SEQUENCE ... RESTART` antes da classe.
- Não checar dados dos seeds em assertions críticas — usar setup explícito por teste (seeds podem mudar).
- Concorrência: usar `CompletableFuture.runAsync` com `Executors.newFixedThreadPool(2)` + `CountDownLatch` para sincronizar início das threads.

---

## 8. Pipeline (futuro)

Quando rodar em CI (GitHub Actions, próxima entrega):

- Job separado para `mvn test` (unitário, rápido, sem Docker).
- Job para `mvn verify -DskipUnitTests` (integração, com `docker` disponível no runner — `ubuntu-latest` já vem).
- Cache do `~/.m2/repository` para acelerar.

---

## 9. Critério de pronto

- [ ] `pom.xml` com Testcontainers + failsafe-plugin configurado.
- [ ] `PostgresIntegrationTest` base funcionando, sobe container em < 10 s no primeiro run.
- [ ] 4 suítes de repositório verdes (~27 testes).
- [ ] 4 suítes de serviço verdes (~18 testes).
- [ ] 2 suítes web verdes (~12 testes).
- [ ] `mvn verify` roda tudo localmente.
- [ ] README atualizado com instruções de execução (`Docker daemon precisa estar rodando`).
