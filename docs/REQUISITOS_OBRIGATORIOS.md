# Verificação — Requisitos obrigatórios do projeto

> **Parte F da Entrega 2** — checklist de conformidade do projeto com os requisitos **obrigatórios** e **desejáveis** do enunciado da disciplina. Não é entregável de teste; é o documento que **trava ou libera** a entrega das outras partes (A–E).
>
> Atualizar a cada mudança que possa alterar a complexidade ciclomática ou o escopo das classes-alvo.

---

## 1. Requisitos do enunciado

### 1.1 Obrigatórios

1. **O código-fonte precisa estar disponível.**
2. **Ao menos um dos projetos não deve ser composto apenas por funcionalidades simples**, como formulários de cadastro sem algoritmos mais elaborados. Um dos projetos deve conter classes com complexidade razoável — comandos com desvios, laços ou estruturas de controle.
3. **Um dos projetos deve possuir ao menos uma classe com alta complexidade para cada membro do grupo.** Mínimo: **complexidade ciclomática de cada classe sob teste ≥ 10**.

### 1.2 Desejáveis

4. Um dos projetos ser um sistema web.
5. Projeto com complexidade mais alta ser desenvolvido em **Java** (para aplicar todas as ferramentas do curso).

---

## 2. Verificação item a item

### 2.1 ✓ Requisito 1 — Código-fonte disponível

**Status: ATENDIDO.**

| Evidência | Detalhe |
|---|---|
| Repositório GitHub | `vaniacourses/trabalho-qualidade-e-teste-ffc-grupo-de-guerreiros` |
| Branch principal | `main` (37 commits, 5 contribuidores) |
| Build reprodutível | `docker compose up -d --build` em um único comando ([README §Quick start](../README.md#-quick-start)) |
| Histórico íntegro | `git log main` mostra autoria por commit, sem squash que apague trabalho de membro ([RESPONSABILIDADES.md](RESPONSABILIDADES.md)) |

---

### 2.2 ✓ Requisito 2 — Funcionalidades não-triviais

**Status: ATENDIDO.**

Evidências concretas de algoritmos com **desvios, laços e estruturas de controle** além de CRUD/formulário:

| Algoritmo | Classe | Tipo de complexidade |
|---|---|---|
| **Juros compostos por minuto** com `Clock` injetável e cálculo lazy | [`InvestmentService.applyInterestIfNeeded`](../src/main/java/com/bancodigital/investment/InvestmentService.java#L100-L110) | Cálculo de `pow`, conversão de fuso horário, `Duration.between`, `setScale(2, HALF_UP)` |
| **Ordenação determinística de locks** com `Math.min/Math.max` para prevenir deadlock | [`AccountService.transfer`](../src/main/java/com/bancodigital/account/AccountService.java#L91-L96) | Concorrência + `SELECT ... FOR UPDATE` + ordenação de aquisição de recursos |
| **Parsing monetário** pt-BR/en-US com `BigDecimal` HALF_UP | [`Money.parseOrNull`](../src/main/java/com/bancodigital/shared/money/Money.java#L18-L27) | Tratamento de exceção + branch de formato |
| **Cadastro atômico** com `@Transactional` + FK + sequence Postgres (resolveu issue #13) | [`SignupService.register`](../src/main/java/com/bancodigital/signup/SignupService.java#L41-L55) | Validação encadeada + transação ACID + atomicidade |
| **Despacho por enum** com renderização condicional baseada no papel da conta na transação | [`StatementLine.descriptionFor`](../src/main/java/com/bancodigital/transaction/StatementLine.java#L37-L49) | `switch` por tipo + ternário com `&&` |
| **`ON CONFLICT DO NOTHING`** para idempotência em concorrência (issue #15) | [`JdbcInvestmentRepository.ensureExists`](../src/main/java/com/bancodigital/investment/JdbcInvestmentRepository.java#L36-L41) | Padrão de concorrência otimista |

> A descrição "formulário de cadastro sem algoritmos elaborados" do enunciado é explicitamente o que o projeto **não é** — o cadastro é só 1 dos 9 fluxos, e mesmo ele tem regex de validação, hash BCrypt e criação atômica de conta com sequence.

---

### 2.3 ⚠ Requisito 3 — Classe de alta complexidade por membro (CC ≥ 10)

**Status: PARCIALMENTE ATENDIDO — 1 pendência ativa (ver §3).**

Distribuição combinada em [RESPONSABILIDADES.md §3.1](RESPONSABILIDADES.md#31-classes-alvo-por-membro-parte-d--parte-e):

| Membro | Classe-alvo | CC estimada (McCabe estendido) | Margem vs. mínimo (10) | Status |
|---|---|---|---|---|
| Erivelton Campos | [`AccountService`](../src/main/java/com/bancodigital/account/AccountService.java) | **~34** | +24 | ✓ folgado |
| Yuri Coutinho | [`InvestmentService`](../src/main/java/com/bancodigital/investment/InvestmentService.java) | **~23** | +13 | ✓ folgado |
| Gleytton | [`StatementLine`](../src/main/java/com/bancodigital/transaction/StatementLine.java) | **~15** | +5 | ✓ ok |
| João Mainoth | [`SignupService`](../src/main/java/com/bancodigital/signup/SignupService.java) | **~11** | +1 | ✓ no limite |
| Gabriel Ferraz | [`Money`](../src/main/java/com/bancodigital/shared/money/Money.java) | **~10** | 0 | ⚠ **risco** |

#### Detalhe da contagem (Money) — método a método

| Método | Decisões contadas | CC |
|---|---|---|
| `normalize` | 1 ternário (`value == null ? null : ...`) | 2 |
| `parseOrNull` | 2 `if` + 1 `catch (NumberFormatException)` | 4 |
| `isPositive` | 1 `&&` (`value != null && value.signum() > 0`) | 2 |
| `format` | 1 ternário (`value == null ? BigDecimal.ZERO : value`) | 2 |
| **Total da classe** | | **10** |

#### Por que isso é risco real, não falso positivo

Ferramentas de análise estática contam complexidade **diferente** entre si:

| Ferramenta | Conta `&&` / `||`? | Conta `catch`? | Conta ternário? | CC esperada para `Money` |
|---|---|---|---|---|
| McCabe estrito | Não | Sim | Sim | **8** |
| JaCoCo (cyclomatic) | Sim | Sim | Sim | **10** |
| SonarQube (cyclomatic) | Sim | Sim | Sim | **10** |
| SonarQube (cognitive) | Sim, com peso aninhado | Sim | Sim | **~7** (medida diferente) |
| PMD | Configurável | Sim | Sim | **8–10** |

→ Se o relatório oficial vier do **McCabe estrito** ou do **PMD com `cyclomaticComplexity.reportLevel=10`**, a classe pode **reprovar** o critério. O risco é não-nulo e merece mitigação **antes** de submeter as partes D e E (que dependem desse relatório).

---

### 2.4 ✓ Requisito 4 (desejável) — Sistema web

**Status: ATENDIDO.**

- **Spring Boot 3.3.4** com **Spring MVC** (`@Controller`).
- **Thymeleaf 3.x** server-side rendering ([`src/main/resources/templates/`](../src/main/resources/templates/)).
- **Spring Security 6** com form login + CSRF + session fixation protection ([`SecurityConfig.java`](../src/main/java/com/bancodigital/config/SecurityConfig.java)).
- 9 endpoints HTTP funcionais (`/login`, `/signup`, `/dashboard`, `/balance`, `/deposit`, `/withdraw`, `/transfer`, `/investment`, `/statement`).
- UI navegável em `http://localhost:8080` após `docker compose up`.

---

### 2.5 ✓ Requisito 5 (desejável) — Java para o projeto complexo

**Status: ATENDIDO.**

- **Java 17 LTS** ([`pom.xml`](../pom.xml#L22-L25)).
- Maven 3.9+ como build tool.
- Permite usar a stack completa do curso:
  - **JaCoCo** para cobertura branch (parte D).
  - **PIT (pitest-maven)** para mutação (parte D).
  - **SonarQube** com plugin Java maduro (parte E).
  - **Testcontainers** para integração (parte B).

---

## 3. Pendência ativa — subir a CC do `Money`

Única pendência que **trava** o requisito 3.

### 3.1 Diagnóstico

`Money` está exatamente em CC = 10 (McCabe estendido), o que é **frágil**: qualquer tool com configuração mais conservadora reporta 8 ou 9, **reprovando o critério**.

### 3.2 Plano de ação (Opção 1 — recomendada)

**Adicionar complexidade controlada ao `Money` sem perder regra de negócio.** Alinhado com a permissão explícita do CLAUDE.md local: *"Se a classe estiver simples demais, **adicionar complexidade controlada sem perder regra de negócio**."*

Extensões propostas (todas têm uso real — não são complexidade inventada):

| # | Extensão | Justificativa de negócio | Decisões adicionadas | Δ CC |
|---|---|---|---|---|
| 1 | Aceitar prefixo `R$` / `$` / `US$` em `parseOrNull` | Usuário cola valores copiados de outro sistema com prefixo — não é razoável quebrar | `if` para detectar e remover prefixo | +1 |
| 2 | Aceitar parênteses no estilo contábil (`(100,00)` → `-100,00`) | Notação padrão em planilhas e relatórios financeiros | `if` para detectar parênteses + cálculo negativo | +2 |
| 3 | Validar scale máximo (≤ 4 casas decimais) — `null` se for `100,12345` | Evita salvar valores com precisão maior que a coluna do banco aceita | `if` no scale | +1 |
| 4 | `compare(BigDecimal a, BigDecimal b)` null-safe | Comparações com nulls hoje são repetidas em vários services | `if null a`, `if null b` | +2 |
| 5 | `add(BigDecimal a, BigDecimal b)` que aplica normalize | Encapsula `setScale` repetido | `if null` em a e b | +2 |
| | | | **CC final esperada** | **~18–20** |

Margem confortável para qualquer ferramenta, mesmo a mais conservadora.

### 3.3 Plano de ação alternativo (Opção 2)

Trocar a classe do Gabriel: combinar `Money` + `StatementLine` como escopo dele (CC combinada ~25). Custo: quebra a continuidade Saque → Money e desloca Gleytton para outra classe.

**Não recomendada** — não há outro candidato com CC ≥ 10 fora dos 5 já mapeados.

### 3.4 Critério para fechar a pendência

- [ ] Implementação da Opção 1 no `Money` (extensões 1–5 ou subconjunto que atinja CC ≥ 14).
- [ ] Testes unitários cobrindo as extensões novas (mínimo 1 caso por extensão, mais valor-limite).
- [ ] Relatório do SonarQube (parte E) mostrando `Money` com **CC ≥ 12** *após* a mudança.
- [ ] Atualizar este documento marcando a pendência como **RESOLVIDA**.

> A implementação **só vale** se for acompanhada de **caso de uso real** — não pode ser código morto. Se uma extensão não é exercitada por nenhum chamador do `Money`, **não conta** para o critério, mesmo elevando a CC artificialmente.

---

## 4. Verificação rápida (comandos)

Para reconferir os números a qualquer momento:

```bash
# Quantidade de commits e contribuidores
git shortlog -sne main

# Linhas de código por classe-alvo
wc -l src/main/java/com/bancodigital/account/AccountService.java \
      src/main/java/com/bancodigital/investment/InvestmentService.java \
      src/main/java/com/bancodigital/signup/SignupService.java \
      src/main/java/com/bancodigital/shared/money/Money.java \
      src/main/java/com/bancodigital/transaction/StatementLine.java

# CC oficial via Sonar (depois de configurar parte E)
mvn clean verify sonar:sonar -Dsonar.token=$SONAR_TOKEN
# → abrir http://localhost:9000/component_measures?metric=complexity&id=bancodigital
```

---

## 5. Tabela-resumo

| # | Requisito | Categoria | Status | Bloqueia entrega? |
|---|---|---|---|---|
| 1 | Código-fonte disponível | Obrigatório | ✓ Atendido | Não |
| 2 | Funcionalidades não-triviais | Obrigatório | ✓ Atendido | Não |
| 3 | CC ≥ 10 por classe-alvo por membro | Obrigatório | ⚠ 4/5 (Money no limite) | **Sim** até resolver §3 |
| 4 | Sistema web | Desejável | ✓ Atendido | Não |
| 5 | Java | Desejável | ✓ Atendido | Não |

---

## 6. Critério de pronto desta verificação

- [ ] Pendência §3 resolvida (Money com CC ≥ 12 conferida via Sonar).
- [ ] Tabela §2.3 atualizada com a CC final pós-extensão.
- [ ] Tabela §5 com **todos** os itens em ✓.
- [ ] Linha "RESOLVIDA em YYYY-MM-DD" anotada ao lado da pendência §3.

---

## 7. Histórico

| Data | Mudança |
|---|---|
| 2026-05-12 | Criação do documento; pendência aberta para subir CC do `Money` |
