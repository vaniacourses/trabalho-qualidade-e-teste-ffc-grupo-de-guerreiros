# Testes de integração com PostgreSQL real

> Escopo da **Entrega 2 (parte B)**. Foco: validar SQL, migrations Flyway, constraints, transações e locking contra um **Postgres real**. Cobre o que os mocks da [parte A](TESTES_UNITARIOS.md) não conseguem ver: SQL malformado, FK violadas, `ON CONFLICT`, `FOR UPDATE`, sequência de números de conta, fuso horário no `OffsetDateTime`.

## Status

- ✅ **Investment endpoint**: 3 casos via `MockMvc` + Postgres real — [`InvestmentIntegrationTest`](../src/test/java/com/bancodigital/integration/InvestmentIntegrationTest.java)
- ✅ **Signup endpoint**: 3 casos via `MockMvc` + Postgres real — [`SignupIntegrationTest`](../src/test/java/com/bancodigital/integration/SignupIntegrationTest.java)
- ✅ **Infraestrutura MockMvc**: [`AbstractIntegrationTest`](../src/test/java/com/bancodigital/integration/AbstractIntegrationTest.java) com `@SpringBootTest` + `MockMvc` + `JdbcTemplate` + TRUNCATE em `@BeforeEach`
- ✅ **E2E Selenium — Signup**: 4 casos (happy path + email duplicado + senha curta + nome em branco) — [`SignupE2ETest`](../src/test/java/com/bancodigital/e2e/SignupE2ETest.java)
- ✅ **E2E Selenium — Investment**: 5 casos (auth + investir + resgatar + 2 erros) — [`InvestmentE2ETest`](../src/test/java/com/bancodigital/e2e/InvestmentE2ETest.java)
- ✅ **E2E Selenium — Performance**: 4 casos com SLA (2s página, 3s submit) — [`PerformanceE2ETest`](../src/test/java/com/bancodigital/e2e/PerformanceE2ETest.java)
- ✅ **Infraestrutura E2E**: [`AbstractE2ETest`](../src/test/java/com/bancodigital/e2e/AbstractE2ETest.java) com `@SpringBootTest(RANDOM_PORT)` + Selenium ChromeDriver + TRUNCATE em `@BeforeEach`
- 🚧 **Account endpoints** (`deposit`/`withdraw`/`transfer`), **Statement**: pendentes
- 🚧 **Auth flow** (login, logout, CSRF, sessão): pendente

---

## 1. Por que integração?

Os mocks da parte A simulam o repositório em memória — não pegam erros como:

- Coluna renomeada na migration mas não no `SELECT`.
- `INSERT` que viola `NOT NULL` ou FK.
- `ON CONFLICT (user_id) DO NOTHING` que silenciosamente não insere.
- `FOR UPDATE` em transação fora de `@Transactional`.
- `OffsetDateTime` com fuso diferente entre Java e Postgres.
- Sequence `account_number_seq` desalinhada com `UNIQUE(number)`.

Esses bugs só aparecem com o banco real.

---

## 2. Stack adotada

Inicialmente planejamos **Testcontainers** (subir Postgres descartável via Docker). Tentamos com 1.21.3 mas batemos numa incompatibilidade de API: o Docker Desktop 4.66 expõe API 1.54 (mín. 1.40), enquanto o docker-java embarcado no Testcontainers usa API 1.32 por padrão. Diversos workarounds (variáveis `DOCKER_HOST`, `DOCKER_API_VERSION`, sockets alternativos) não resolveram.

**Decisão**: usar o **Postgres local do `docker-compose.yml`** (`postgres:16-alpine` na porta 5432), apontando os testes para uma database separada `bancodigital_test`. Vantagens:

- Mesma imagem da prod, sem fricção de versão de API Docker.
- Zero dependências extras no `pom.xml`.
- Mais rápido (container já está aquecido).
- Dev e teste convivem no mesmo Postgres mas em databases isoladas.

**Trade-off**: o colega/CI precisa subir o `docker-compose.yml` + criar a database de teste. Não é zero-setup como seria com Testcontainers funcional.

### Setup (uma vez)

```bash
docker compose up -d postgres
docker exec bancodigital-postgres psql -U bancodigital -d postgres -c "CREATE DATABASE bancodigital_test"
```

### Configuração

[`src/test/resources/application-integration-test.yml`](../src/test/resources/application-integration-test.yml):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bancodigital_test
    username: bancodigital
    password: bancodigital
  flyway:
    enabled: true
```

[`AbstractIntegrationTest`](../src/test/java/com/bancodigital/integration/AbstractIntegrationTest.java):

```java
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
public abstract class AbstractIntegrationTest {
    @Autowired protected MockMvc mockMvc;
    @Autowired protected JdbcTemplate jdbc;

    @BeforeEach
    void cleanDatabase() {
        jdbc.execute("TRUNCATE TABLE transactions, investments, accounts, users RESTART IDENTITY CASCADE");
        jdbc.execute("ALTER SEQUENCE account_number_seq RESTART WITH 1");
    }
}
```

Flyway aplica as migrations no primeiro boot. TRUNCATE em `@BeforeEach` garante isolamento entre testes. Helpers `insertUser`, `insertAccount`, `insertInvestment` montam o estado inicial.

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

## 5. Plano web (E2E com `MockMvc`)

Sobe a aplicação inteira (controllers + security + templates) e exercita os endpoints reais com `MockMvc`. Garante CSRF, autenticação, redirect.

### 5.1 ✅ Signup endpoint — implementado

[`SignupIntegrationTest`](../src/test/java/com/bancodigital/integration/SignupIntegrationTest.java) (3 casos):

| Caso | Verificação |
|---|---|
| `signupEndpointCreatesUserAndAccount` | POST válido → 302 `/login?signup`, 1 user + 1 conta no DB, hash ≠ senha raw |
| `signupEndpointRejectsDuplicateEmail` | E-mail existente → 200 com erro, DB inalterado |
| `signupEndpointRejectsShortPassword` | Senha < 8 chars → 200 com erro, DB sem novo user |

### 5.2 ✅ Investment endpoint — implementado

[`InvestmentIntegrationTest`](../src/test/java/com/bancodigital/integration/InvestmentIntegrationTest.java) (3 casos, autenticados via `@WithMockUser`):

| Caso | Verificação |
|---|---|
| `investmentEndpointExecutesInvestAndPersistsTransaction` | POST `/investment` (investir 100) → saldo cai 400, investido sobe 100, 1 tx `investment` |
| `investmentEndpointRejectsInsufficientBalance` | Saldo baixo → 302 com flash error, DB inalterado |
| `investmentEndpointRequiresAuthentication` | GET sem auth → 302 para login |

### 5.3 🚧 Auth + outros endpoints — pendente

| # | Cenário | Verificação |
|---|---|---|
| 1 | POST `/login` com credencial dos seeds | 302 → `/dashboard`, sessão criada |
| 2 | POST `/login` com senha errada | 302 → `/login?error` |
| 3 | GET `/dashboard` anônimo | 302 → `/login` |
| 4 | POST `/deposit` valor válido | Flash de sucesso, saldo atualizado |
| 5 | POST `/withdraw` valor inválido | Flash de erro, saldo intacto |
| 6 | POST `/transfer` conta inexistente | Flash de erro |
| 7 | GET `/statement` | 200, lista transações ordenada DESC |
| 8 | POST sem CSRF token | 403 |

---

## 6. E2E com Selenium (navegador real)

Enquanto a seção 5 usa `MockMvc` (HTTP sintético sem render de template), os testes E2E sobem o Spring em porta aleatória e controlam um **Chrome headless real** via Selenium WebDriver. Cada teste vê a UI como o usuário veria.

### 6.1 Infraestrutura

[`AbstractE2ETest`](../src/test/java/com/bancodigital/e2e/AbstractE2ETest.java):

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
public abstract class AbstractE2ETest {
    @BeforeEach void setupDriver() {
        // WebDriverManager baixa o chromedriver automaticamente
        // --headless=new por padrão; -Dheadless=false para ver o browser
        // TRUNCATE + RESTART IDENTITY no Postgres antes de cada teste
    }
    @AfterEach void teardownDriver() { driver.quit(); }
}
```

**Page Objects** (`e2e/pages/`): encapsulam localizadores e waiters, evitam que os testes manipulem o DOM diretamente.

### 6.2 Suítes implementadas

| Suíte | Casos | Cenários |
|---|---|---|
| [`SignupE2ETest`](../src/test/java/com/bancodigital/e2e/SignupE2ETest.java) | 4 | Cadastro válido, email duplicado, senha curta, nome em branco |
| [`InvestmentE2ETest`](../src/test/java/com/bancodigital/e2e/InvestmentE2ETest.java) | 5 | Acesso sem auth, investir com saldo, investir sem saldo, resgatar, resgatar acima do investido |
| [`PerformanceE2ETest`](../src/test/java/com/bancodigital/e2e/PerformanceE2ETest.java) | 4 | Carga da página signup ≤ 2s, carga da página investment ≤ 2s, submit signup ≤ 3s, submit investment ≤ 3s |

### 6.3 Como rodar

```bash
# Headless (padrão — usado em CI via mvn verify)
mvn verify

# Com Chrome visível para depuração
mvn failsafe:integration-test -Dheadless=false

# Com Chrome visível + pausa entre ações (1500ms)
mvn failsafe:integration-test -Dheadless=false -Dslowdown=1500

# Uma suíte específica
mvn failsafe:integration-test -Dheadless=false -Dit.test='InvestmentE2ETest'
```

---

## 7. Estrutura atual

```
src/test/java/com/bancodigital/
├── integration/
│   ├── AbstractIntegrationTest.java     ✅ base (MockMvc + JdbcTemplate)
│   ├── SignupIntegrationTest.java       ✅ 3 casos
│   └── InvestmentIntegrationTest.java   ✅ 3 casos
└── e2e/
    ├── AbstractE2ETest.java             ✅ base (Selenium + JdbcTemplate)
    ├── SignupE2ETest.java               ✅ 4 casos
    ├── InvestmentE2ETest.java           ✅ 5 casos
    ├── PerformanceE2ETest.java          ✅ 4 casos
    └── pages/
        ├── SignupPage.java
        ├── LoginPage.java
        └── InvestmentPage.java
```

Estrutura futura para os outros domínios:

```
src/test/java/com/bancodigital/integration/
├── repository/   🚧 Jdbc{User,Account,Transaction,Investment}RepositoryTest
├── service/      🚧 {AccountTransfer,AccountDepositWithdraw}ServiceTest
└── web/          🚧 LoginAuthTest, StatementTest, dashboardTest
```

---

## 8. Convenções

- **MockMvc** (`integration/`): sufixo `IntegrationTest` — Surefire pega no `mvn test`.
- **Selenium** (`e2e/`): sufixo `E2ETest` — excluídos do Surefire, rodados pelo Failsafe em `mvn verify`.
- Limpeza entre testes: `TRUNCATE ... RESTART IDENTITY CASCADE` no `@BeforeEach` das classes-base. Sequence `account_number_seq` reiniciada explicitamente (valor 100 nos E2E para evitar colisão com accounts inseridos via helper).
- Cada teste seed seu próprio estado via `insertUser`, `insertAccount`, `insertInvestment` — sem depender de seed data do Flyway (V2).
- CSRF: usar `.with(csrf())` em POSTs MockMvc (Spring Security está ativo).
- Autenticação MockMvc: `@WithMockUser(username = "<email>")` resolve o `CurrentUser` se o user existir no DB; senão usar `@WithUserDetails`.
- `BigDecimal`: comparar com `.compareTo(...) == 0`, não `.equals` (scale-sensitive).

---

## 9. Pipeline (futuro)

Quando rodar em CI (GitHub Actions, próxima entrega):

- `mvn test`: sobe Postgres via `services:`, cria `bancodigital_test`, roda unit + integração.
- `mvn verify`: adiciona Chrome headless (action `browser-actions/setup-chrome`) e roda os E2E.
- Cache do `~/.m2/repository` para acelerar.

---

## 10. Critério de pronto

- [x] Infraestrutura `AbstractIntegrationTest` funcionando contra Postgres local.
- [x] `SignupIntegrationTest` cobrindo happy path + 2 cenários de erro.
- [x] `InvestmentIntegrationTest` cobrindo invest + saldo insuficiente + requer auth.
- [x] Infraestrutura `AbstractE2ETest` com Selenium + TRUNCATE + headless toggle.
- [x] `SignupE2ETest` — 4 casos (happy path + 3 erros de validação).
- [x] `InvestmentE2ETest` — 5 casos (auth + fluxos + erros).
- [x] `PerformanceE2ETest` — 4 casos com SLA mensurado.
- [x] `mvn verify` roda tudo localmente (125 testes, 0 falhas).
- [ ] Repositórios cobertos (User/Account/Transaction/Investment) via `@JdbcTest`.
- [ ] 4 suítes de serviço verdes (~18 testes).
- [ ] 2 suítes web verdes (~12 testes).
