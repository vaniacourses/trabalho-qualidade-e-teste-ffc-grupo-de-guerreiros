# Plano — Projeto de casos de teste por técnicas (funcional + estrutural + mutação)

> Escopo da **Entrega 2 (parte D)**. Aplicar **três técnicas complementares** de projeto de teste sobre **classes de alta complexidade não-CRUD**, atingindo:
>
> - **80 % de cobertura no critério "todas-arestas"** (branch coverage) na técnica estrutural.
> - **80 % de escore de mutação** na técnica baseada em defeitos.
>
> Complementa [TESTES_UNITARIOS.md](TESTES_UNITARIOS.md) (parte A), [TESTES_INTEGRACAO.md](TESTES_INTEGRACAO.md) (parte B) e [QUALIDADE_ISO25010.md](QUALIDADE_ISO25010.md) (parte C).

---

## 1. O que cada técnica significa

### 1.1 Funcional (caixa-preta)

Projeta casos sem olhar o código — apenas pela **especificação**. Subtécnicas:

| Subtécnica | Quando usar | Saída |
|---|---|---|
| **Particionamento de equivalência** (PE) | Entrada divisível em classes homogêneas | 1 caso por classe válida + 1 por inválida |
| **Análise de valor limite** (AVL) | Quando há fronteiras numéricas/temporais | Casos em `min-1`, `min`, `min+1`, `max-1`, `max`, `max+1` |
| **Tabela de decisão** | Combinação de N condições com regras | 1 caso por linha da tabela |
| **Transição de estados** | Objeto com estados claros (ex.: investimento existe/não existe) | 1 caso por transição válida + 1 por inválida |

### 1.2 Estrutural (caixa-branca)

Projeta casos para **exercitar todas as arestas** do grafo de fluxo de controle. Critérios em ordem crescente de rigor:

```
Todos-Nós ⊂ Todos-Arestas ⊂ Todos-Caminhos
```

**"Todas-arestas"** (branch coverage) = cada decisão do código testada com `true` e com `false`. É o critério pedido pelo enunciado.

Métrica: **80 % das arestas executadas por algum teste**.
Ferramenta: **JaCoCo** (`Branch coverage` no relatório).

### 1.3 Baseada em defeitos — mutação

Insere pequenas alterações artificiais no código (mutantes) — ex.: trocar `>` por `>=`, `&&` por `||`, remover um `return`. Para cada mutante:

- Se **algum teste falha** → mutante "morto" ✓
- Se **todos os testes passam** → mutante "vivo" ✗ (significa que seu teste não cobre essa decisão)

**Escore de mutação** = `mutantes mortos / (mutantes totais - equivalentes)`.

Meta: **80 % de escore**.
Ferramenta: **PIT (pitest-maven)** — padrão da indústria para Java.

---

## 2. Por que as 3 técnicas se complementam

Cada técnica pega defeitos que as outras não pegam:

| Técnica | Pega bem | Pega mal |
|---|---|---|
| Funcional | Casos que faltam por **má especificação** ou requisito esquecido | Caminhos extras criados pelo código |
| Estrutural | Caminhos do código não exercitados | Especificação incorreta (o código erra, o teste passa) |
| Mutação | **Asserções fracas** (teste executa mas não verifica nada) | Defeitos fora do escopo dos mutadores |

A ordem correta de aplicação é **funcional → estrutural → mutação**:
1. Funcional define **o que o sistema deve fazer**.
2. Estrutural fecha **lacunas de caminho** que a funcional não viu.
3. Mutação valida que **as asserções realmente verificam** o resultado.

---

## 3. Ferramentas e configuração

### 3.1 JaCoCo (cobertura todas-arestas)

Adicionar ao `pom.xml`:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <executions>
        <execution>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals><goal>report</goal></goals>
        </execution>
        <execution>
            <id>check</id>
            <goals><goal>check</goal></goals>
            <configuration>
                <rules>
                    <rule>
                        <element>CLASS</element>
                        <includes>
                            <include>com.bancodigital.account.AccountService</include>
                            <include>com.bancodigital.investment.InvestmentService</include>
                            <include>com.bancodigital.signup.SignupService</include>
                            <include>com.bancodigital.shared.money.Money</include>
                            <include>com.bancodigital.transaction.StatementLine</include>
                        </includes>
                        <limits>
                            <limit>
                                <counter>BRANCH</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Comandos:

```bash
mvn test                                       # gera target/site/jacoco/index.html
mvn verify                                     # falha o build se < 80 % branch nas classes alvo
open target/site/jacoco/index.html             # abrir relatório
```

### 3.2 PIT / pitest (mutação)

```xml
<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
    <version>1.17.0</version>
    <dependencies>
        <dependency>
            <groupId>org.pitest</groupId>
            <artifactId>pitest-junit5-plugin</artifactId>
            <version>1.2.1</version>
        </dependency>
    </dependencies>
    <configuration>
        <targetClasses>
            <param>com.bancodigital.account.AccountService</param>
            <param>com.bancodigital.investment.InvestmentService</param>
            <param>com.bancodigital.signup.SignupService</param>
            <param>com.bancodigital.shared.money.Money</param>
            <param>com.bancodigital.transaction.StatementLine</param>
        </targetClasses>
        <targetTests>
            <param>com.bancodigital.*</param>
        </targetTests>
        <mutationThreshold>80</mutationThreshold>
        <coverageThreshold>80</coverageThreshold>
        <outputFormats>
            <param>HTML</param>
            <param>XML</param>
        </outputFormats>
    </configuration>
</plugin>
```

Comandos:

```bash
mvn org.pitest:pitest-maven:mutationCoverage
open target/pit-reports/*/index.html
```

> PIT precisa dos testes verdes para rodar. Rode parte A primeiro.

---

## 4. Seleção das classes-alvo

Critérios do enunciado: **não-CRUD** + **alta complexidade** + **1 classe por membro do grupo**.

### 4.1 Filtro: classes excluídas e por quê

| Categoria | Exclui | Motivo |
|---|---|---|
| `JdbcXxxRepository` | `JdbcUserRepository`, `JdbcAccountRepository`, `JdbcTransactionRepository`, `JdbcInvestmentRepository` | **São CRUD por entidade** — exatamente o que o enunciado proíbe |
| Records / entidades | `User`, `Account`, `Transaction`, `Investment` | Sem lógica, complexidade ciclomática = 1 |
| Glue Spring | Controllers, `CurrentUser`, `CustomUserDetailsService`, `GlobalExceptionHandler` | Complexidade baixa, lógica delegada |
| Constantes | `Messages` | Sem lógica |

### 4.2 Candidatas (ordenadas por complexidade)

> Complexidade ciclomática (CC) estimada via contagem de pontos de decisão (`if`, `&&`, `||`, `case`, ternário, `?:`, loops).

| # | Classe | Métodos relevantes | CC estimada | Por que é boa candidata |
|---|---|---|---|---|
| 1 | [`AccountService`](../src/main/java/com/bancodigital/account/AccountService.java) | `validateWithdraw`, `validateTransfer`, `withdraw`, `deposit`, `transfer` | ~18 | Ordenação de locks (`Math.min/Math.max`), validações encadeadas, 3 fluxos com efeito colateral |
| 2 | [`InvestmentService`](../src/main/java/com/bancodigital/investment/InvestmentService.java) | `calculateInterest`, `validateOperation`, `query`, `execute`, `applyInterestIfNeeded`, `Operation.parse` | ~16 | Lógica de juros compostos, branching por operação, fuso horário, lazy update |
| 3 | [`SignupService`](../src/main/java/com/bancodigital/signup/SignupService.java) | `validateSignup`, `register` | ~10 | Regex de e-mail, validação encadeada, fluxo atômico, e-mail duplicado |
| 4 | [`Money`](../src/main/java/com/bancodigital/shared/money/Money.java) | `parseOrNull`, `normalize`, `isPositive`, `format` | ~9 | Parsing pt-BR vs en-US, tratamento de null, escala HALF_UP |
| 5 | [`StatementLine`](../src/main/java/com/bancodigital/transaction/StatementLine.java) | `from`, `colorFor`, `descriptionFor` | ~8 | Despacho por tipo de transação, formatação |

### 4.3 Sugestão de atribuição (ajustar conforme tamanho do grupo)

| Membro | Classe-alvo | Esforço relativo |
|---|---|---|
| Membro 1 | `AccountService` | Alto — concorrência + locks |
| Membro 2 | `InvestmentService` | Alto — `Clock` + juros |
| Membro 3 | `SignupService` | Médio — regex + atomicidade |
| Membro 4 | `Money` | Médio — muitos limites |
| Membro 5 (se houver) | `StatementLine` | Baixo — boa entrada |

> **Regra inegociável**: cada membro precisa atingir **80 % branch + 80 % mutação** **na sua classe**. A meta agregada não compensa lacuna individual.

---

## 5. Plano detalhado por classe

> Convenção: arestas numeradas (`A1`, `A2`, …) referenciam o **grafo de fluxo de controle (CFG)** que cada membro deve desenhar antes de escrever os testes. O desenho do CFG entra como anexo da entrega.

---

### 5.1 `AccountService`

**Métodos no escopo**: `validateWithdraw` (CC 4), `validateDeposit` (CC 2), `validateTransfer` (CC 6), `withdraw` (CC 3), `deposit` (CC 3), `transfer` (CC 5).

#### Funcional

**Particionamento de equivalência — `validateTransfer`** (5 entradas):

| Entrada | Classes válidas | Classes inválidas |
|---|---|---|
| `amount` | `> 0` | `≤ 0`, `null` |
| `destinationNumber` | string não vazia | vazia, `null`, igual a `sourceNumber` |
| `destinationExists` | `true` | `false` |
| `sourceBalance` | `≥ amount` | `< amount`, `null` |

**Análise de valor limite — `validateWithdraw`** sobre `DAILY_WITHDRAW_LIMIT = 10.000,00`:

| Valor | Resultado esperado |
|---|---|
| `9.999,99` | `OK` |
| `10.000,00` | `OK` (limite inclusivo) |
| `10.000,01` | `WITHDRAW_LIMIT_EXCEEDED` |
| `0,01` | `OK` |
| `0,00` | `INVALID_AMOUNT` |
| `-0,01` | `INVALID_AMOUNT` |

**Tabela de decisão — `transfer`** (4 condições):

| # | amount > 0 | dest válido | source ≠ dest | saldo ≥ amount | Resultado |
|---|---|---|---|---|---|
| 1 | T | T | T | T | sucesso |
| 2 | F | – | – | – | `INVALID_AMOUNT_OR_ACCOUNT` |
| 3 | T | F | – | – | `INVALID_DESTINATION_ACCOUNT` |
| 4 | T | T | F | – | `SAME_ACCOUNT` |
| 5 | T | T | T | F | `INSUFFICIENT_BALANCE` |

→ **5 casos mínimos pelo critério funcional**.

#### Estrutural (todas-arestas)

Em `validateTransfer` (CFG simplificado):

```
[A1: amount > 0?] --F--> retorna INVALID_AMOUNT_OR_ACCOUNT
       |T
[A2: dest vazio?] --T--> retorna INVALID_AMOUNT_OR_ACCOUNT
       |F
[A3: source == dest?] --T--> retorna SAME_ACCOUNT
       |F
[A4: destExists?] --F--> retorna INVALID_DESTINATION
       |T
[A5: balance >= amount?] --F--> retorna INSUFFICIENT_BALANCE
       |T
retorna OK
```

→ **6 arestas verdadeiras + 5 falsas = 11 arestas**. Os 5 casos da tabela de decisão acima já cobrem **9**; faltam dois casos:

- amount = 1, dest = `"   "` (string só de espaços) → cobre `A2-T` separado de `A1-F`.
- amount = 1, dest = `null` → cobre o `A2` quando `destinationNumber == null`.

Em `transfer` (efeito colateral): adicionar testes para:

- `firstId = sourceId` (source.id < destination.id) — cobre uma aresta.
- `firstId = destinationId` (source.id > destination.id) — cobre a aresta inversa do `Math.min/Math.max`.

#### Mutação (PIT mutadores ativos)

Mutadores que provavelmente aparecerão:

| Mutador | Onde provavelmente sobrevive | Estratégia |
|---|---|---|
| `CONDITIONALS_BOUNDARY` (`<` ↔ `<=`) | `amount.compareTo(DAILY_WITHDRAW_LIMIT) > 0` → trocar por `>= 0` | Caso exatamente `10.000,00` deve passar |
| `NEGATE_CONDITIONALS` (`!=` ↔ `==`) | `if (!"OK".equals(error))` | Casos com erro real e com sucesso real |
| `MATH` (`+` ↔ `-`) | `accountRepository.credit(amount)` vs `debit` | Assertar saldo final exato, não só "mudou" |
| `VOID_METHOD_CALLS` (remove chamada) | `transactionRepository.recordTransfer(...)` | Assertar que **uma transação** foi registrada |
| `REMOVE_CONDITIONALS` | qualquer `if` | Caso negativo precisa **falhar**, não só "não passar" |

**Sobreviventes esperados** que podem ser justificados como equivalentes:

- Mutação em `Math.min/Math.max` quando os ids são iguais (impossível por unique constraint) — registrar como equivalente.

→ Meta: **≥ 80 % de escore** após adicionar asserções de saldo exato e contagem de transações.

---

### 5.2 `InvestmentService`

**Métodos no escopo**: `calculateInterest`, `validateOperation`, `Operation.parse`, `query`, `execute`, `applyInterestIfNeeded`.

#### Funcional

**Particionamento + AVL — `calculateInterest(amount, minutes)`**:

| `amount` | `minutes` | Resultado esperado | Classe |
|---|---|---|---|
| `null` | qualquer | `null` | inválido |
| `100.00` | `-1` | `100.00` (sem juros) | limite inferior |
| `100.00` | `0` | `100.00` (sem juros) | limite |
| `100.00` | `1` | `101.00` | nominal |
| `100.00` | `60` | `181.67` | nominal alto |
| `100.00` | `Integer.MAX_VALUE` | finito (não overflow) | limite superior |
| `0.00` | `5` | `0.00` | edge zero |

**Tabela de decisão — `validateOperation`** (4 condições):

| # | op válida | amount > 0 | balance ≥ amount | invested ≥ amount | Operação | Resultado |
|---|---|---|---|---|---|---|
| 1 | T (investir) | T | T | – | INVEST | `null` (ok) |
| 2 | T (retirar) | T | – | T | WITHDRAW | `null` (ok) |
| 3 | F | – | – | – | – | `INVALID_OPERATION` |
| 4 | T | F | – | – | – | `INVALID_AMOUNT` |
| 5 | T (investir) | T | F | – | INVEST | `INSUFFICIENT_ACCOUNT_BALANCE` |
| 6 | T (retirar) | T | – | F | WITHDRAW | `AMOUNT_EXCEEDS_INVESTED` |

**Transição de estados — `query`** (estado do registro de investimento):

| Estado inicial | Ação | Estado final | Verificação |
|---|---|---|---|
| Não existe | `query(userId)` | Existe com amount=0 | `ensureExists` inserts |
| Existe, last_update = agora | `query(userId)` | Mesmo amount | Nenhum update |
| Existe, last_update = agora - 1 min | `query(userId)` | amount × 1.01 | 1 update |
| Existe, last_update = agora - 60 min | `query(userId)` | amount × 1.01^60 | 1 update |

#### Estrutural

Em `calculateInterest`:

```
[A1: amount == null?] --T--> return null
       |F
[A2: minutes <= 0?] --T--> return amount
       |F
[A3: Math.min(minutes, Integer.MAX_VALUE)]
       |
return amount * factor (HALF_UP)
```

→ **4 arestas**. Casos: `null`, `-1`, `0`, `1`, `Long.MAX_VALUE` (cobre o `Math.min`).

Em `execute`:

```
[B1: parse(op)] -> INVEST ou WITHDRAW ou null
[B2: operation == INVEST?] --T--> ramo invest
                           --F--> ramo withdraw
```

→ Precisa de pelo menos 1 caso INVEST feliz, 1 WITHDRAW feliz, 1 com operação inválida, 1 com saldo insuficiente em INVEST, 1 com valor > invested em WITHDRAW.

#### Mutação

| Mutador | Onde provavelmente sobrevive | Estratégia |
|---|---|---|
| `CONDITIONALS_BOUNDARY` | `minutes <= 0` → `minutes < 0` | Caso com `minutes = 0` precisa retornar `amount` original |
| `MATH` | `1.01.pow(minutes)` — trocar `pow` por `multiply` | Asserção exata em `100 * 1.01^5 = 105.10` (não aproximada) |
| `INCREMENTS` | nenhum (sem `++`) | — |
| `EMPTY_RETURNS` | `return null` em parse | Caso com op inválida deve **lançar** ou **retornar erro específico** |
| `RETURN_VALS` | `return inv.amount()` | Comparar com valor exato pós-update |

Pontos de atenção:

- **Sem teste de tempo real**: `Clock.fixed` é mandatório, senão `Duration.between` vira flaky e PIT não consegue distinguir mutante.
- **Equivalent mutants prováveis**: `setScale(2, RoundingMode.HALF_UP)` → trocar para `HALF_EVEN` quando o valor não tem `.5` exato é equivalente. Documentar.

→ Meta: 80 % de escore.

---

### 5.3 `SignupService`

**Métodos no escopo**: `validateSignup`, `register`.

#### Funcional

**Particionamento — `validateSignup(name, email, password)`**:

| Campo | Válido | Inválido |
|---|---|---|
| `name` | não vazio após trim | `null`, `""`, `"   "` |
| `email` | bate o regex `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$` | `null`, vazio, sem `@`, sem TLD, TLD com 1 letra |
| `password` | length ≥ 8 | `null`, length < 8 |

**Casos AVL para `password`** (limite `PASSWORD_MIN = 8`):
- 7 caracteres → erro
- 8 caracteres → ok
- 9 caracteres → ok

**Casos para regex de e-mail** (positivos e negativos):

| E-mail | Esperado |
|---|---|
| `a@b.co` | OK (TLD com 2) |
| `a@b.c` | inválido (TLD < 2) |
| `a.b+c@d.com` | OK (permitir `+`, `.`, `-` no local part) |
| `@b.com` | inválido (sem local part) |
| `a@.com` | inválido (sem domínio) |
| `a@b` | inválido (sem TLD) |

#### Estrutural

Em `register`:

```
[A1: result != "OK"?] --T--> throw INVALID_*
       |F
[A2: existsByEmail?] --T--> throw DUPLICATE_EMAIL
       |F
[A3: encode + save user + nextNumber + insert account]
```

→ Precisa cobrir os 3 ramos com fakes (já planejado em [TESTES_UNITARIOS.md](TESTES_UNITARIOS.md#31-signupservice---srcmainjavacombancodigitalsignupsignupservicejava)).

#### Mutação

| Mutador | Onde provavelmente sobrevive | Estratégia |
|---|---|---|
| `CONDITIONALS_BOUNDARY` | `password.length() < PASSWORD_MIN` → `<=` | AVL em 7/8/9 caracteres já mata isto |
| `VOID_METHOD_CALLS` | remover `accountRepository.insert(...)` | Asserção: fake recebeu `insert` exatamente 1× |
| `RETURN_VALS` | `return "OK"` → `return ""` | Comparar contra `"OK"` literal |
| `INVERT_NEGS` | `!"OK".equals(result)` | Casos felizes e infelizes alternados |

→ Meta: 80 % de escore.

---

### 5.4 `Money`

**Métodos no escopo**: `parseOrNull(String)`, `normalize(BigDecimal)`, `isPositive(BigDecimal)`, `format(BigDecimal)`.

> Não-CRUD: classe utilitária pura. Alta densidade de branches por causa de parsing internacional (vírgula vs ponto).

#### Funcional

**Particionamento — `parseOrNull(String)`**:

| Entrada | Classe | Resultado |
|---|---|---|
| `null` | inválido | `null` |
| `""` | inválido | `null` |
| `"   "` | inválido (após trim) | `null` |
| `"100"` | inteiro válido | `100.00` |
| `"100,50"` | pt-BR | `100.50` |
| `"100.50"` | en-US | `100.50` |
| `"1.234,56"` | pt-BR com separador de milhar | `1234.56` |
| `"1,234.56"` | en-US com separador de milhar | `1234.56` |
| `"abc"` | não numérico | `null` |
| `"-10"` | negativo | `-10.00` |

**AVL** sobre `isPositive`:

| Valor | Esperado |
|---|---|
| `-0.01` | `false` |
| `0` | `false` |
| `0.01` | `true` |
| `null` | `false` |

#### Estrutural

→ Mapear o `if/else` interno de `parseOrNull` (provavelmente um `try/catch` com tentativa pt-BR primeiro). Casos acima cobrem todas as arestas se a implementação tiver branch para vírgula vs ponto.

#### Mutação

| Mutador | Onde provavelmente sobrevive | Estratégia |
|---|---|---|
| `CONDITIONALS_BOUNDARY` | `amount.compareTo(ZERO) > 0` em `isPositive` | Caso `0` exato |
| `RETURN_VALS` | `return null` em `parseOrNull` | Comparar com `null` explicitamente |
| `MATH` | `setScale(2, HALF_UP)` — alterar scale | Asserções com valor exato `100.50` (não `100.5`) |
| `INVERT_NEGS` | `compareTo(ZERO) > 0` → `< 0` | Casos positivos, zero e negativos |

→ Meta: 80 % de escore. **Esperar atingir > 90 %** — classe pura, sem dependências.

---

### 5.5 `StatementLine`

**Métodos no escopo**: `from(Transaction, accountId)`, `colorFor(TransactionType, ...)`, `descriptionFor(...)`.

#### Funcional

**Tabela de decisão — `from`** (combina tipo da transação × papel da conta):

| Tipo | accountId = source | accountId = destination | Resultado |
|---|---|---|---|
| `withdraw` | T | F | sinal negativo, cor vermelha |
| `deposit` | F | T | sinal positivo, cor verde |
| `transfer` | T | F | "Transferência enviada", negativo |
| `transfer` | F | T | "Transferência recebida", positivo |
| `investment` | T | F | "Investimento aplicado", negativo |
| `redemption` | F | T | "Resgate", positivo |

→ **6 casos** mínimos pela tabela.

#### Estrutural

`descriptionFor` provavelmente é um `switch` por `TransactionType`. Todas as arestas exigem 1 caso por valor do enum + 1 caso para o `default` (se houver).

#### Mutação

| Mutador | Estratégia |
|---|---|
| `RETURN_VALS` | Comparar strings de descrição literais |
| `SWITCH` (se aplicável) | Caso para cada valor do enum |
| `RETURN_VALS` em `colorFor` | Assert na cor exata, não só "não null" |

→ Meta: 80 %.

---

## 6. Workflow recomendado

```
1. Para cada classe-alvo, o responsável:
   a. Lê o método e desenha o CFG (à mão ou via PlantUML).
   b. Escreve a tabela de decisão / partições / AVL — testes FUNCIONAIS.
   c. Roda mvn test + abre target/site/jacoco/index.html.
   d. Identifica arestas vermelhas — adiciona testes ESTRUTURAIS para fechar até ≥ 80 % branch.
   e. Roda mvn org.pitest:pitest-maven:mutationCoverage.
   f. Para cada mutante VIVO:
      - Verifica se é equivalente → documenta no anexo.
      - Caso contrário, fortalece asserção ou adiciona caso novo.
   g. Re-roda PIT até ≥ 80 % de escore.

2. Code review cruzado: cada membro revisa a classe do colega.

3. Consolidação:
   - mvn verify → falha se alguma classe-alvo < 80 % branch.
   - mvn pitest:mutationCoverage → relatório agregado.
   - Anexar PDFs/HTMLs dos relatórios à entrega.
```

---

## 7. Artefatos a entregar

- [ ] `docs/img/cfg-AccountService-transfer.png` (e similares para cada classe) — CFG anotado.
- [ ] `docs/img/decision-table-*.md` ou planilha — tabelas de decisão.
- [ ] Suítes de teste novas seguindo padrão `XxxFuncionalTest`, `XxxEstruturalTest`, `XxxMutacaoTest` (ou suíte única bem comentada — discutir convenção).
- [ ] Relatório JaCoCo HTML em `target/site/jacoco/` (não commitar; gerar no CI ou anexar como ZIP).
- [ ] Relatório PIT HTML em `target/pit-reports/` (idem).
- [ ] Lista de **mutantes equivalentes** com justificativa por classe.

---

## 8. Convenções

- **Naming**: `class AccountServiceTransferTest`, métodos `transferWhenInsufficientBalanceThenThrows` (já é o padrão do projeto).
- **Sem Mockito**: usar fakes da parte A. Reaproveitar `AccountRepositoryFake`, `TransactionRepositoryFake`, etc.
- **Asserções específicas**: `assertEquals(new BigDecimal("100.00"), saldo)`, nunca `assertNotNull` solto. Mutação **quebra** asserções vagas.
- **`Clock` controlado** em todos os testes que tocam `InvestmentService`.
- **1 teste, 1 cenário**: PIT atribui mortes por teste; testes monolíticos confundem o relatório.
- **Mutantes equivalentes documentados**: PIT não detecta equivalência sozinho; cabe ao avaliador justificar caso a caso.

---

## 9. Critério de pronto

- [ ] `pom.xml` com `jacoco-maven-plugin` e `pitest-maven` configurados.
- [ ] `mvn verify` passa com **branch coverage ≥ 80 %** em cada classe-alvo (regra do JaCoCo `check`).
- [ ] `mvn pitest:mutationCoverage` reporta **escore ≥ 80 %** em cada classe-alvo.
- [ ] CFG desenhado e versionado por classe.
- [ ] Tabelas de decisão / partições documentadas (markdown ou planilha).
- [ ] Anexo com mutantes equivalentes (≤ 5 por classe, com justificativa).
- [ ] README atualizado na seção "Próximas entregas" listando esta entrega como concluída.

---

## 10. Referências

- **Pressman, Engenharia de Software** — capítulo de teste baseado em especificação (PE, AVL, tabela de decisão).
- **Myers, *The Art of Software Testing*** — origem do particionamento e valor limite.
- **Maldonado, *Critérios Potenciais Usos*** — critérios estruturais (todos-nós, todas-arestas, todos-usos) no contexto brasileiro.
- **Offutt & Ammann, *Introduction to Software Testing*** — teste de mutação e critérios estruturais com rigor.
- **PIT documentation** — <https://pitest.org/> (mutadores ativos por default, configurações avançadas).
- **JaCoCo** — <https://www.jacoco.org/jacoco/trunk/doc/> (relatório de branch coverage).
