# Testes unitários com isolamento de dependências

> Escopo da **Entrega 2 (parte A)**. Foco: subir a cobertura unitária das **camadas de serviço**, isolando dependências (repositórios, `Clock`, `PasswordEncoder`) com **Mockito** (`@Mock`, `@InjectMocks`, `verify`, `ArgumentCaptor`, `InOrder`).

## Status

- ✅ **Investment**: `query` e `execute` cobertos com Mockito — [`InvestmentServiceTest`](../src/test/java/com/bancodigital/investment/InvestmentServiceTest.java) (25 casos, 10 novos)
- ✅ **Signup**: `register` coberto com Mockito — [`SignupServiceTest`](../src/test/java/com/bancodigital/signup/SignupServiceTest.java) (18 casos, 6 novos)
- 🚧 **Account** (`withdraw`/`deposit`/`transfer`): pendente
- 🚧 **CustomUserDetailsService**, **CurrentUser**: pendente

---

## 1. Cobertura atual

**106 testes unitários** verdes em `mvn test`:

| Suite | Casos | Aborda |
|---|---|---|
| [`MoneyTest`](../src/test/java/com/bancodigital/shared/money/MoneyTest.java) | 16 | helpers (`parseOrNull`, `normalize`, `isPositive`, `format`) |
| [`TransactionTypeTest`](../src/test/java/com/bancodigital/transaction/TransactionTypeTest.java) | 9 | enum `fromDbValue` |
| [`StatementLineTest`](../src/test/java/com/bancodigital/transaction/StatementLineTest.java) | 15 | factory + `colorFor`/`descriptionFor` |
| [`AccountServiceTest`](../src/test/java/com/bancodigital/account/AccountServiceTest.java) | 23 | só validações puras (`validateWithdraw/Deposit/Transfer`) |
| [`SignupServiceTest`](../src/test/java/com/bancodigital/signup/SignupServiceTest.java) | 18 | `validateSignup` + `register` (Mockito) |
| [`InvestmentServiceTest`](../src/test/java/com/bancodigital/investment/InvestmentServiceTest.java) | 25 | `calculateInterest`/`validateOperation` + `query`/`execute` (Mockito) |

Métodos `@Transactional` de `AccountService` (`deposit`/`withdraw`/`transfer`) ainda não cobertos — escopo dos donos desses domínios.

---

## 2. Regra de isolamento (Mockito)

Para cada `Service` testado, anotar a classe com `@ExtendWith(MockitoExtension.class)` e declarar cada dependência como `@Mock`. Instanciar o service via construtor no `@BeforeEach` (passando os mocks) ou usar `@InjectMocks`.

Princípios:

- **Determinístico** — `Clock` controlado via `Clock.fixed(...)`. Não mockar `Clock` (é record-like).
- **Asserts via interação** — `verify(repo).save(...)` para confirmar chamadas, `verify(..., never())` ou `verifyNoInteractions(...)` para garantir ausência de side effects.
- **Captura de argumentos** — `ArgumentCaptor<BigDecimal>` para inspecionar valores passados (scale, ordem).
- **Ordem de chamadas** — `InOrder` quando a sequência importa (ex.: `save → insert` no `register`).
- **Stubs** — `when(repo.findByUserId(id)).thenReturn(Optional.of(...))`. Sem cadeia de fakes complexos.

---

## 3. Plano por classe

### 3.1 `SignupService` — ✅ implementado

Arquivo: [`SignupServiceTest`](../src/test/java/com/bancodigital/signup/SignupServiceTest.java).

Mocks: `@Mock UserRepository`, `@Mock AccountRepository`, `@Mock PasswordEncoder`.

Casos novos (6) para `register(SignupForm)`:

1. `registerHappyPath` — assert ordem com `InOrder`: `existsByEmail → encode → save → nextAccountNumber → insert`.
2. `registerRejectsInvalidForm` — nome vazio → `DomainException(INVALID_NAME)`, `verifyNoInteractions` em todos os mocks.
3. `registerRejectsDuplicateEmail` — `existsByEmail` true → `DUPLICATE_EMAIL`, encoder/save/insert nunca chamados.
4. `registerTrimsEmailAndName` — input com espaços → `existsByEmail` e `save` recebem versão trimmed.
5. `registerEncodesPasswordBeforeSaving` — captura arg de `save`, assert é hash, não raw.
6. `registerGeneratesSequentialAccountNumbers` — 2 chamadas → 2 números distintos em `insert`.

---

### 3.2 `AccountService` — 🚧 pendente (donos: Erivelton/Gabriel/Gleytton)

**Já coberto**: 3 validações puras (23 casos).
**A cobrir**: `withdraw`, `deposit`, `transfer`, `getAccount`.

Mocks: `@Mock AccountRepository`, `@Mock TransactionRepository`.

Cenários sugeridos por método:

**`getAccount(userId)`** (2 casos): existe → retorna `Account`; não existe → `DomainException`.

**`withdraw(userId, amount)`** (6 casos): feliz, valor inválido, acima do limite, saldo insuficiente, normalização de scale, conta inexistente.

**`deposit(userId, amount)`** (3 casos): feliz, valor inválido, conta inexistente.

**`transfer(userId, destination, amount)`** (8 casos): feliz, mesma conta, destino inexistente, destino vazio, saldo insuficiente, valor inválido, ordem de lock determinística (verificar com `InOrder` que `findByIdForUpdate` é chamado em `Math.min/Math.max`), trim do número.

---

### 3.3 `InvestmentService` — ✅ implementado

Arquivo: [`InvestmentServiceTest`](../src/test/java/com/bancodigital/investment/InvestmentServiceTest.java).

Mocks: `@Mock InvestmentRepository`, `@Mock AccountRepository`, `@Mock TransactionRepository`. `Clock` real fixo (`Clock.fixed(FIXED_NOW.toInstant(), UTC)`).

Casos novos (10):

**`query()`** (4):
1. `queryReturnsAmountWhenLastUpdateIsNow` — sem update, retorna amount original.
2. `queryThrowsWhenInvestmentMissing` — `findByUserId` empty → `DomainException`.
3. `queryAppliesInterestAfterFiveMinutes` — clock +5 min → 105.10, `update` 1x.
4. `querySkipsUpdateWhenDeltaIsNegative` — clock antes do `lastUpdate` → sem update.

**`execute()` invest** (3):
5. `executeInvestHappyPath` — debit + update + recordInvestment com captura.
6. `executeInvestRejectsInsufficientBalance` — saldo baixo → exceção, nenhum side effect.
7. `executeInvestWithAccumulatedInterest` — clock avança, 2 chamadas a `update` (juros + invest).

**`execute()` withdraw** (2):
8. `executeWithdrawHappyPath` — credit + update + recordRedemption.
9. `executeWithdrawRejectsExceedingInvested` — valor > invested → exceção.

**`execute()` estrutural** (2):
10. `executeThrowsWhenAccountMissing` — `accountRepository.findByUserId` empty → exceção.
11. `executeNormalizesAmountToScaleTwo` — `100.999` → `101.00`, scale 2 garantida via `ArgumentCaptor`.

---

### 3.4 `CustomUserDetailsService` — 🚧 pendente

Mocks: `@Mock UserRepository`.

Cenários sugeridos (4):
1. `loadUserByUsername` com user existente → `UserDetails` com username = e-mail, password = hash.
2. `loadUserByUsername` com user inexistente → `UsernameNotFoundException`.
3. `findByEmail` retorna o user quando encontra.
4. `findByEmail` retorna `null` (não lança) quando não encontra.

---

### 3.5 `CurrentUser` — 🚧 pendente

Mocks: `@Mock CustomUserDetailsService`.

Cenários sugeridos (3):
1. `principal` válido com e-mail conhecido → retorna `User`.
2. `principal == null` → `DomainException("Sessão expirada.")`.
3. `principal` sem correspondência no repositório → `DomainException("Usuário não encontrado.")`.

---

### 3.6 Controllers — adiados para integração

Os controllers (`SignupController`, `LoginController`, `BalanceController`, `DepositController`, `WithdrawController`, `TransferController`, `StatementController`, `InvestmentController`) são finos: parseiam request, chamam service, populam flash messages. Cobertura mais valiosa via `@SpringBootTest` + `MockMvc` em [`TESTES_INTEGRACAO.md`](TESTES_INTEGRACAO.md).

---

## 4. Estrutura

Sem pasta `fakes/` — Mockito substitui. Cada `*ServiceTest` declara seus próprios `@Mock` na própria classe.

```
src/test/java/com/bancodigital/
├── signup/SignupServiceTest.java       ✅ 18 casos (validações + register)
├── investment/InvestmentServiceTest.java ✅ 25 casos (puros + query + execute)
├── account/AccountServiceTest.java     ⚠ só validações puras (23 casos)
└── auth/                                🚧 CustomUserDetailsService, CurrentUser
```

---

## 5. Convenções

- Nome do teste: `metodoCondicaoResultado` em camelCase.
- `@ExtendWith(MockitoExtension.class)` na classe. `@BeforeEach` instancia o service via construtor com os mocks.
- Money sempre via `new BigDecimal("100.00")`, nunca `BigDecimal.valueOf(double)`.
- Asserções em mensagens de erro via constantes de `Messages` — nunca string literal.
- `Clock.fixed(...)` para tempo — não mockar `Clock`.
- Comparar `BigDecimal` com `.compareTo(...) == 0`, não `.equals` (scale-sensitive).

---

## 6. Critério de pronto

- [x] Mockito habilitado no pom (`spring-boot-starter-test` sem exclusions).
- [x] `InvestmentServiceTest` com cobertura de `query` e `execute` isolando deps.
- [x] `SignupServiceTest` com cobertura de `register` isolando deps.
- [ ] `AccountService` (`withdraw`/`deposit`/`transfer`) coberto com Mockito.
- [ ] `CustomUserDetailsService` e `CurrentUser` cobertos.
- [x] README atualizado na seção "Cobertura atual".
