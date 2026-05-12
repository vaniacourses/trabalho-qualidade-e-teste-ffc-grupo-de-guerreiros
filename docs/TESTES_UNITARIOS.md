# Plano — Testes unitários com isolamento de dependências

> Escopo da **Entrega 2 (parte A)**. Foco: subir a cobertura unitária das **camadas de serviço e controller**, isolando dependências (repositórios, `Clock`, `PasswordEncoder`, etc.) com **fakes/stubs escritos à mão** — Mockito está excluído por restrição acadêmica ([`pom.xml:77-86`](../pom.xml#L77-L86)).

---

## 1. Onde estamos hoje

**89 testes unitários puros** que cobrem só métodos sem dependências externas:

| Suite atual | Tipo | O que cobre |
|---|---|---|
| [`MoneyTest`](../src/test/java/com/bancodigital/shared/money/MoneyTest.java) | helpers | `parseOrNull`, `normalize`, `isPositive`, `format` |
| [`TransactionTypeTest`](../src/test/java/com/bancodigital/transaction/TransactionTypeTest.java) | enum | `fromDbValue` |
| [`StatementLineTest`](../src/test/java/com/bancodigital/transaction/StatementLineTest.java) | factory | `from(...)`, `colorFor`, `descriptionFor` |
| [`SignupServiceTest`](../src/test/java/com/bancodigital/signup/SignupServiceTest.java) | validação | só `validateSignup(...)` |
| [`AccountServiceTest`](../src/test/java/com/bancodigital/account/AccountServiceTest.java) | validação | só `validateWithdraw/Deposit/Transfer` |
| [`InvestmentServiceTest`](../src/test/java/com/bancodigital/investment/InvestmentServiceTest.java) | cálculo + validação | só `calculateInterest`, `validateOperation` |

**Limitação**: os métodos `@Transactional` (`register`, `deposit`, `withdraw`, `transfer`, `query`, `execute`) — onde mora o fluxo de negócio real — **não têm teste**, porque dependem dos repositórios.

---

## 2. Regra de isolamento (sem Mockito)

Para cada interface de repositório (ou colaborador) criar uma classe `XxxFake` em `src/test/java/com/bancodigital/.../fakes/` implementando a interface com armazenamento em memória (`HashMap`, `ArrayList`). Padrão já usado no projeto antigo (`UsuarioDAOFake`).

Princípios:

- **Determinístico** — `Clock` controlado via `Clock.fixed(...)`.
- **Inspecionável** — fake expõe `getSaldoAtual(id)`, `getUltimaTransacao()`, etc., para o teste assertar o estado pós-operação.
- **Estado mínimo** — só armazena o que a operação testada lê/escreve.
- **Sem lógica de negócio** — fake não valida, só persiste/devolve. Quem valida é o `Service`.

---

## 3. Plano por classe

### 3.1 `SignupService` — [`src/main/java/com/bancodigital/signup/SignupService.java`](../src/main/java/com/bancodigital/signup/SignupService.java)

**Já coberto**: `validateSignup` (12 casos).
**A cobrir**: `register(SignupForm)` — o método que orquestra a criação atômica de usuário + conta.

Fakes necessários:

| Colaborador | Fake | Comportamento |
|---|---|---|
| `UserRepository` | `UserRepositoryFake` | `Map<String, User>` por e-mail; `save` retorna id incremental |
| `AccountRepository` | `AccountRepositoryFake` (compartilhado com 3.2) | `nextAccountNumber` devolve `C00001, C00002...`; `insert` guarda na lista |
| `PasswordEncoder` | `FakePasswordEncoder` | `encode(p)` retorna `"hash:" + p`; `matches` compara o sufixo |

Cenários (mínimo 8):

1. Cadastro feliz → cria 1 user + 1 conta, número sequencial `C00001`.
2. Validação falha (nome vazio) → `DomainException(INVALID_NAME)`, nada persistido.
3. E-mail inválido → `DomainException(INVALID_EMAIL)`, nada persistido.
4. Senha curta → `DomainException(PASSWORD_TOO_SHORT)`, nada persistido.
5. E-mail duplicado → `existsByEmail` retorna `true`, lança `DomainException(DUPLICATE_EMAIL)`, **conta não é criada**.
6. Trim de nome/e-mail antes de persistir (com espaços nas pontas).
7. Senha vai para o repositório **hasheada**, nunca em texto puro.
8. Dois `register` consecutivos geram números de conta distintos via `nextAccountNumber`.

---

### 3.2 `AccountService` — [`src/main/java/com/bancodigital/account/AccountService.java`](../src/main/java/com/bancodigital/account/AccountService.java)

**Já coberto**: as 3 validações puras (23 casos).
**A cobrir**: `withdraw`, `deposit`, `transfer`, `getAccount`.

Fakes necessários:

| Colaborador | Fake | Comportamento |
|---|---|---|
| `AccountRepository` | `AccountRepositoryFake` | `Map<Long, Account>` indexado por id, mais índices por `userId` e `number`; `debit/credit` mutam o saldo; `findByIdForUpdate` retorna a mesma referência atualizada |
| `TransactionRepository` | `TransactionRepositoryFake` | `List<Transaction>` registra cada `record*` chamado |

Cenários por método:

**`getAccount(userId)`** (2 casos)
1. Conta existe → retorna `Account`.
2. Conta não existe → `DomainException("Conta não encontrada.")`.

**`withdraw(userId, amount)`** (6 casos)
1. Saque feliz → saldo cai, transação `withdraw` registrada com o valor normalizado (2 casas).
2. Valor 0 ou negativo → `DomainException(INVALID_AMOUNT)`, **saldo não muda**, **nenhuma transação**.
3. Acima do limite diário (10.000,01) → `DomainException(WITHDRAW_LIMIT_EXCEEDED)`.
4. Saldo insuficiente → `DomainException(INSUFFICIENT_BALANCE)`.
5. `rawAmount = "100"` (1 casa decimal) → normaliza para `100.00`.
6. Conta inexistente → `DomainException`, **fake não recebeu nem `findByIdForUpdate` para id inválido**.

**`deposit(userId, amount)`** (3 casos)
1. Depósito feliz → saldo sobe, transação `deposit` registrada.
2. Valor inválido (zero/negativo) → erro, saldo intacto.
3. Conta inexistente → erro.

**`transfer(userId, destination, amount)`** (8 casos)
1. Transferência feliz entre contas distintas → origem debitada, destino creditado, transação `transfer` com source/destination corretos.
2. Conta destino igual à origem → `DomainException(SAME_ACCOUNT)`.
3. Destino inexistente → `DomainException(INVALID_DESTINATION_ACCOUNT)`.
4. Destino vazio/null → `DomainException(INVALID_AMOUNT_OR_ACCOUNT)`.
5. Saldo insuficiente → erro, **nada é debitado/creditado**.
6. Valor zero ou negativo → erro.
7. Ordem de lock determinística: source.id < destination.id e source.id > destination.id (verificar que o fake é consultado na ordem `Math.min/Math.max` — usar fake que registra a ordem dos `findByIdForUpdate`).
8. Trim do número de destino (`" C00002 "`).

---

### 3.3 `InvestmentService` — [`src/main/java/com/bancodigital/investment/InvestmentService.java`](../src/main/java/com/bancodigital/investment/InvestmentService.java)

**Já coberto**: `calculateInterest`, `validateOperation` (14 casos).
**A cobrir**: `query(userId)`, `execute(userId, op, amount)`, `applyInterestIfNeeded` (via `query`).

Fakes necessários:

| Colaborador | Fake | Comportamento |
|---|---|---|
| `InvestmentRepository` | `InvestmentRepositoryFake` | `Map<Long, Investment>`; `ensureExists` cria se ausente com `amount=0` e timestamp do clock; `update` substitui |
| `AccountRepository` | reusa `AccountRepositoryFake` da 3.2 |
| `TransactionRepository` | reusa `TransactionRepositoryFake` |
| `Clock` | `Clock.fixed(Instant.parse("2026-01-01T12:00:00Z"), UTC)` e adiantar via `Clock.offset(base, Duration.ofMinutes(N))` |

Cenários:

**`query(userId)`** (5 casos)
1. Primeiro acesso → `ensureExists` cria com 0, `applyInterestIfNeeded` devolve 0 (0 minutos passados).
2. Já existe, 0 min desde último update → devolve `amount` sem mexer.
3. Já existe, 1 min → devolve `amount * 1.01` arredondado HALF_UP, `update` chamado com novo timestamp.
4. Já existe, 5 min → fator `1.01^5 ≈ 1.05101005`, com `setScale(2, HALF_UP)`.
5. Timestamp do banco em fuso diferente (`-03:00`) → conversão para UTC antes do `Duration.between`.

**`execute(userId, "investir", amount)`** (5 casos)
1. Investir feliz → conta debitada, investimento somado, transação `investment`.
2. Saldo da conta < amount → `DomainException(INSUFFICIENT_ACCOUNT_BALANCE)`, **nada é debitado nem somado**.
3. Operação inválida (string fora de "investir"/"retirar") → `DomainException(INVALID_OPERATION)`.
4. Valor zero → `DomainException(INVALID_AMOUNT)`.
5. Investimento ganhou juros antes de aplicar → ordem: `applyInterest` primeiro, depois soma o valor novo.

**`execute(userId, "retirar", amount)`** (3 casos)
1. Resgate feliz → conta creditada, investimento subtraído, transação `redemption`.
2. Valor > investido (após juros) → `DomainException(AMOUNT_EXCEEDS_INVESTED)`.
3. Resgate exatamente igual ao investido → OK, zera o investimento.

---

### 3.4 `CustomUserDetailsService` — [`src/main/java/com/bancodigital/auth/CustomUserDetailsService.java`](../src/main/java/com/bancodigital/auth/CustomUserDetailsService.java)

**A cobrir**: `loadUserByUsername`, `findByEmail`.

Fake: reusa `UserRepositoryFake`.

Cenários (4 casos):
1. Usuário existe → retorna `UserDetails` com username = e-mail, password = hash, authorities vazia.
2. Usuário inexistente → `UsernameNotFoundException`.
3. `findByEmail` retorna o user quando encontra.
4. `findByEmail` retorna `null` (não lança) quando não encontra.

---

### 3.5 `CurrentUser` — [`src/main/java/com/bancodigital/auth/CurrentUser.java`](../src/main/java/com/bancodigital/auth/CurrentUser.java)

**A cobrir**: `required(principal)`.

Cenários (3 casos):
1. `principal` válido com e-mail conhecido → retorna `User`.
2. `principal == null` → lança exceção apropriada.
3. `principal` sem correspondência no repositório → lança exceção.

---

### 3.6 Controllers — testes via `@WebMvcTest` (opcional, parte A)

Os controllers (`SignupController`, `LoginController`, `DashboardController`, `BalanceController`, `DepositController`, `WithdrawController`, `TransferController`, `StatementController`, `InvestmentController`) são finos: parseiam request, chamam service, populam flash messages.

Como o projeto **não tem Mockito**, dois caminhos:

**Opção A — fakes injetados via `@TestConfiguration`** (recomendado, mantém padrão do projeto).
Cria um `@TestConfiguration` que registra os mesmos `*Fake` como `@Bean` no contexto de teste. Usa `MockMvc` para exercitar o endpoint.

**Opção B — adiar para integração** ([`TESTES_INTEGRACAO.md`](TESTES_INTEGRACAO.md)) e cobrir os controllers só com `@SpringBootTest` + Testcontainers.

Decisão sugerida: **opção B**. A unitária dos controllers traz pouco valor sem Mockito (muito boilerplate de fake), e a integração já cobre o caminho completo.

---

## 4. Estrutura de pastas proposta

```
src/test/java/com/bancodigital/
├── fakes/                          ← compartilhado entre suítes
│   ├── UserRepositoryFake.java
│   ├── AccountRepositoryFake.java
│   ├── TransactionRepositoryFake.java
│   ├── InvestmentRepositoryFake.java
│   └── FakePasswordEncoder.java
├── signup/SignupServiceRegisterTest.java       ← novo, ~8 casos
├── account/AccountServiceFlowTest.java         ← novo, ~19 casos
├── investment/InvestmentServiceFlowTest.java   ← novo, ~13 casos
├── auth/CustomUserDetailsServiceTest.java      ← novo, ~4 casos
└── auth/CurrentUserTest.java                   ← novo, ~3 casos
```

**Meta de cobertura nova: ~47 testes**, totalizando ~136 unitários puros sem precisar de banco.

---

## 5. Convenções

- Nome do teste: `metodoQuandoCondicaoEntaoResultado` em camelCase (estilo já usado em `AccountServiceTest`).
- Cada teste cria seu próprio service com `new AccountService(fakeA, fakeT)` no `@BeforeEach` — sem estado compartilhado.
- Não usar reflection, não usar PowerMock, não usar Mockito (proibido pelo `pom`).
- Money sempre via `new BigDecimal("100.00")`, nunca `BigDecimal.valueOf(double)`.
- Asserções em mensagens de erro via constantes de `Messages` — nunca string literal.

---

## 6. Critério de pronto

- [ ] Fakes em `src/test/java/.../fakes/` cobrindo as 4 interfaces de repositório + `PasswordEncoder`.
- [ ] Pelo menos 47 novos testes verdes em `mvn test`.
- [ ] Cobertura JaCoCo (se adicionada) ≥ 85 % nas classes `*Service`.
- [ ] Nenhum teste novo abre conexão JDBC nem sobe contexto Spring (esses ficam para a parte B).
- [ ] README atualizado na seção "Cobertura atual".
