# Plano — Inspeção do código-fonte com análise estática

> Escopo da **Entrega 2 (parte E)**. Rodar uma ferramenta de inspeção (SonarQube), gerar relatório, corrigir os problemas de **uma classe não-CRUD por membro do grupo** e gerar relatório novamente para evidenciar a melhoria.
>
> Complementa as partes A/B/C/D listadas em [TESTES_UNITARIOS.md](TESTES_UNITARIOS.md), [TESTES_INTEGRACAO.md](TESTES_INTEGRACAO.md), [QUALIDADE_ISO25010.md](QUALIDADE_ISO25010.md), [TECNICAS_TESTE.md](TECNICAS_TESTE.md).

---

## 1. O que é "inspeção" aqui

Inspeção (no sentido da disciplina) ≈ **análise estática automatizada**: rodar uma ferramenta que lê o código-fonte **sem executá-lo** e aponta:

- **Bugs** — defeitos prováveis (NPE, recursos não fechados, comparação `==` em String, etc.).
- **Vulnerabilidades** — riscos de segurança (SQL injection, XSS, criptografia fraca, etc.).
- **Code Smells** — qualidade/manutenibilidade (método muito longo, complexidade cognitiva alta, duplicação, etc.).
- **Security Hotspots** — pontos que precisam de revisão humana (não necessariamente bug, mas merece olhar).

Análise estática complementa os testes: pega problemas que os testes não pegam (ex.: senha logada em texto plano, recurso vazado em caminho de erro), porque os testes verificam **comportamento**, e a inspeção verifica **padrões no código**.

---

## 2. Ferramenta escolhida: SonarQube Community Edition

### 2.1 Por que Sonar

| Ferramenta | Prós | Contras |
|---|---|---|
| **SonarQube Community** | Padrão da indústria, UI rica, histórico de scans, regras Java maduras, gratuito | Requer um container rodando |
| SonarCloud | Zero infra, integra com GitHub | Exige repo público ou plano pago |
| SonarLint (IDE) | Feedback em tempo real | Não gera relatório/print de servidor |
| SpotBugs + PMD + Checkstyle | Gratuitos, simples | Três relatórios separados, sem UI unificada |

→ **SonarQube Community local via Docker** é a escolha: roda em 1 comando, gera prints bonitos, mesmo workflow das partes B e D (que já usam Docker), e usa o `sonar-maven-plugin` para o scan.

> SonarLint no IDE pode (e deve) ser usado **durante** o trabalho de correção — feedback instantâneo. Mas **os prints da entrega vêm do SonarQube server**, não do Lint.

### 2.2 Versões

| Componente | Versão sugerida |
|---|---|
| SonarQube Community | `sonarqube:10.6-community` |
| sonar-scanner via Maven | `sonar-maven-plugin:5.0.0.4389` |
| Java do scanner | 17 (mesmo do projeto) |

---

## 3. Setup local

### 3.1 Subir o SonarQube

Criar `docker-compose.sonar.yml` na raiz (separado do compose principal para não poluir o `up` da aplicação):

```yaml
services:
  sonarqube:
    image: sonarqube:10.6-community
    container_name: sonarqube
    ports:
      - "9000:9000"
    environment:
      SONAR_ES_BOOTSTRAP_CHECKS_DISABLE: "true"
    volumes:
      - sonarqube_data:/opt/sonarqube/data
      - sonarqube_logs:/opt/sonarqube/logs
      - sonarqube_extensions:/opt/sonarqube/extensions

volumes:
  sonarqube_data:
  sonarqube_logs:
  sonarqube_extensions:
```

Subir:

```bash
docker compose -f docker-compose.sonar.yml up -d
# esperar ~30 s
open http://localhost:9000
```

**Login inicial**: `admin` / `admin` → forçará trocar a senha.

### 3.2 Gerar um token de projeto

1. Logar no Sonar → **My Account** → **Security** → **Generate Token** (tipo *User Token*).
2. Copiar o valor (só aparece uma vez).
3. Exportar como variável de ambiente:

```bash
export SONAR_TOKEN=squ_xxxxxxxxxxxxxxxxxxxx
```

### 3.3 Criar o projeto no Sonar

Pela UI: **Projects → Create Project → Manually** → key `bancodigital`, name `Banco Digital`. Ou deixar o scanner criar via primeiro envio (default).

### 3.4 Configurar o scanner no `pom.xml`

```xml
<properties>
    <!-- ... outras propriedades ... -->
    <sonar.host.url>http://localhost:9000</sonar.host.url>
    <sonar.projectKey>bancodigital</sonar.projectKey>
    <sonar.projectName>Banco Digital</sonar.projectName>
    <sonar.java.binaries>target/classes</sonar.java.binaries>
    <sonar.coverage.jacoco.xmlReportPaths>
        target/site/jacoco/jacoco.xml
    </sonar.coverage.jacoco.xmlReportPaths>
</properties>
```

> O `sonar-maven-plugin` está declarado com versão fixa no `pom.xml` para que
> todos os integrantes executem a mesma versão do scanner.

### 3.5 Rodar o scan

```bash
mvn clean verify sonar:sonar -Dsonar.token=$SONAR_TOKEN
```

Após ~30 s o relatório aparece em <http://localhost:9000/dashboard?id=bancodigital>.

> **Pré-requisito**: build precisa passar (`mvn verify`). Se houver teste vermelho, o Sonar não roda.

---

## 4. Workflow de inspeção e correção

```
1. Scan inicial (baseline)         → PRINT #1 (dashboard) + PRINT #2 (issues da classe-alvo)
2. Triagem por classe-alvo:
   - Filtrar issues por "Component contains: ClasseAlvo"
   - Marcar False Positive os que não fazem sentido (com justificativa)
3. Correção:
   - Bugs primeiro (maior severidade)
   - Depois Vulnerabilidades
   - Depois Code Smells (Critical → Major → Minor → Info)
   - Security Hotspots → revisar e marcar como Reviewed/Safe
4. Re-scan (mvn sonar:sonar)        → PRINT #3 (dashboard) + PRINT #4 (issues da classe-alvo após fix)
5. Documentar issues que ficaram (motivo + plano)
```

---

## 5. Categorias e severidades do Sonar

| Categoria | Severidade (mais grave → menos) | Significado |
|---|---|---|
| **Bug** | Blocker, Critical, Major, Minor, Info | Defeito provável; vai falhar em runtime |
| **Vulnerability** | idem | Risco de exploração (SQLi, XSS, hash fraco) |
| **Code Smell** | idem | Manutenibilidade |
| **Security Hotspot** | High, Medium, Low | Padrão sensível que **precisa revisão humana** |

**Quality Gate** default (Sonar way):
- 0 Bugs novos
- 0 Vulnerabilidades novas
- ≥ 80 % cobertura no código novo
- ≤ 3 % duplicação

→ Para a entrega, **passar o Quality Gate na classe-alvo** já é uma meta clara.

---

## 6. O que provavelmente vai aparecer neste projeto

Predição com base no código atual (estimativa, vai variar):

| Issue provável | Onde | Severidade | Como corrigir |
|---|---|---|---|
| `"OK"` repetido como literal | [`AccountService.java`](../src/main/java/com/bancodigital/account/AccountService.java) (várias linhas) | Minor | Extrair `private static final String OK = "OK";` ou usar enum `ValidationResult` |
| `"investir"` / `"retirar"` literais | [`InvestmentService.java:31-32`](../src/main/java/com/bancodigital/investment/InvestmentService.java#L31-L32) | Minor | Constantes ou enum `Operation` já existe — mover strings para lá |
| Magic number `10` em `BCryptPasswordEncoder(10)` | [`SecurityConfig.java:15`](../src/main/java/com/bancodigital/config/SecurityConfig.java#L15) | Minor | Extrair `BCRYPT_STRENGTH` |
| Complexidade cognitiva alta em `transfer` | [`AccountService.java:77-104`](../src/main/java/com/bancodigital/account/AccountService.java#L77-L104) | Major | Extrair `lockBoth(source, destination)` |
| Métodos com muitos parâmetros (`validateTransfer`, 5) | `AccountService.java:43` | Minor | Aceitar (regra de negócio) ou criar record `TransferContext` |
| Catch genérico `Exception` | — (não vi nenhum) | — | — |
| String concatenation em `String.format` repetido | [`JdbcAccountRepository.java:65`](../src/main/java/com/bancodigital/account/JdbcAccountRepository.java#L65) | Minor | Aceitar (formato `C%05d` é semântico) |
| `@Autowired` em campo | — (projeto usa construtor) | — | — |
| Falta de `@Override` em métodos de interface | — (records têm equivalente) | — | — |
| Logger ausente em `catch` | [`SignupController.java:32-35`](../src/main/java/com/bancodigital/signup/SignupController.java#L32-L35) | Info | Adicionar SLF4J `log.warn(...)` |

→ **Expectativa preliminar**: ~20 a 40 issues no projeto inteiro, sendo a maioria Minor/Info. Bugs reais devem ser raros porque o projeto é pequeno e segue Spring idiomaticamente.

---

## 7. Seleção de classes por membro

Mesmas candidatas da parte D ([TECNICAS_TESTE.md §4.2](TECNICAS_TESTE.md#42-candidatas-ordenadas-por-complexidade)) — não-CRUD, complexidade razoável.

| Membro | Classe-alvo | Complexidade | Por que serve |
|---|---|---|---|
| Membro 1 | [`AccountService`](../src/main/java/com/bancodigital/account/AccountService.java) | Alta | Vários literais `"OK"`, complexidade cognitiva em `transfer` |
| Membro 2 | [`InvestmentService`](../src/main/java/com/bancodigital/investment/InvestmentService.java) | Alta | Literais de operação, `Clock`, fluxo complexo |
| Membro 3 | [`SignupService`](../src/main/java/com/bancodigital/signup/SignupService.java) | Média | Regex, validação encadeada, transação |
| Membro 4 | [`Money`](../src/main/java/com/bancodigital/shared/money/Money.java) | Média | Múltiplos branches de parsing |
| Membro 5 (se houver) | [`StatementLine`](../src/main/java/com/bancodigital/transaction/StatementLine.java) | Média | Switch por tipo, formatação |

> **Recomendação**: usar **as mesmas classes** que a parte D. O esforço se acumula: melhorar testes (D) e melhorar código (E) na mesma classe produz uma "área limpa" demonstrável.
>
> **Alternativa**: cada membro pega uma classe **diferente** de D — distribui a cobertura. Decisão do grupo, mas anotar a escolha.

### Classes explicitamente excluídas

- **`JdbcXxxRepository`** — são CRUD por entidade, vetados pelo enunciado.
- **Records (`User`, `Account`, `Transaction`, `Investment`)** — complexidade 1, nada a inspecionar.
- **Constantes (`Messages`)** — só strings finais.
- **Controllers** — possíveis, mas a complexidade está baixa; deixar como segunda escolha.

---

## 8. Plano de correção por classe

> Cada membro deve preencher esta tabela depois do scan inicial. Estimativas abaixo são o **piso esperado** — o número real só sai do Sonar.

### 8.1 `AccountService` — issues prováveis e correções

| # | Issue (Sonar) | Severidade | Correção planejada |
|---|---|---|---|
| 1 | `"OK"` aparece em 6 locais (literal duplicado) | Minor | Constante `OK` ou enum `ValidationResult` |
| 2 | Complexidade cognitiva de `transfer` > 15 | Major | Extrair `lockBothAccounts(srcId, dstId)` privado |
| 3 | `validateTransfer` recebe 5 parâmetros | Minor | Manter (regra de negócio) ou criar record `TransferRequest` |
| 4 | `Math.min/Math.max` em vez de método dedicado | Info | Extrair `firstLockId / secondLockId` para clareza |

### 8.2 `InvestmentService`

| # | Issue (Sonar) | Severidade | Correção planejada |
|---|---|---|---|
| 1 | `"investir"` / `"retirar"` literais | Minor | Mover para enum `Operation` como campo (`Operation.INVEST.alias`) |
| 2 | Cast `(int) Math.min(minutes, Integer.MAX_VALUE)` | Minor | Aceitar — comportamento intencional, comentar `// guard contra overflow` (caso raro de comentário justificável) |
| 3 | `applyInterestIfNeeded` faz 2 conversões de fuso seguidas | Info | Extrair helper `toUtc(OffsetDateTime)` |
| 4 | Acoplamento alto (3 repositórios + Clock) | Info | Aceitar — é o ponto de orquestração |

### 8.3 `SignupService`

| # | Issue (Sonar) | Severidade | Correção planejada |
|---|---|---|---|
| 1 | Regex de e-mail "complexa" (regra `S5852`) | Minor | Validar que não é vulnerável a ReDoS (a regra alerta, mas o regex atual é seguro) — marcar Safe |
| 2 | Magic number `8` para `PASSWORD_MIN` | Info | Já é constante; conferir nome |
| 3 | `"OK"` literal duplicado com `AccountService` | Minor | Mover para `Messages.OK` |
| 4 | `validateSignup` retorna `String` (anti-pattern) | Minor | Aceitar (padrão do projeto) ou trocar para `Optional<String>` (mudança maior — discutir) |

### 8.4 `Money`

| # | Issue (Sonar) | Severidade | Correção planejada |
|---|---|---|---|
| 1 | `parseOrNull` retorna `null` em vez de `Optional` | Major (regra `S2789`) | Renomear ou trocar tipo — depende do impacto em chamadores |
| 2 | Locale hardcoded? | Minor | Verificar se o `NumberFormat` usa `Locale` correto |
| 3 | `BigDecimal.ZERO` comparação por `compareTo` | Info | Já segue boas práticas — provavelmente sem issue |

### 8.5 `StatementLine`

| # | Issue (Sonar) | Severidade | Correção planejada |
|---|---|---|---|
| 1 | `switch` sem `default` (regra `S131`) | Minor | Adicionar `default -> throw new IllegalStateException(...)` |
| 2 | Strings de descrição duplicadas | Info | Aceitar (são labels distintos) |

---

## 9. Entregáveis: como tirar os prints

### 9.1 Print #1 — Dashboard inicial (baseline do projeto inteiro)

URL: <http://localhost:9000/dashboard?id=bancodigital>

Capturar a tela inteira do dashboard mostrando:
- **Quality Gate** (Passed/Failed)
- Contadores de **Bugs / Vulnerabilities / Code Smells / Security Hotspots**
- **Coverage** e **Duplications** (se JaCoCo foi integrado, virá da parte D)

Salvar como: `docs/img/sonar-01-dashboard-baseline.png`

### 9.2 Print #2 — Issues da classe-alvo (antes)

URL: <http://localhost:9000/project/issues?id=bancodigital&types=BUG,VULNERABILITY,CODE_SMELL&components=bancodigital:src/main/java/com/bancodigital/account/AccountService.java>

(Ajustar `components=...` para a classe de cada membro.)

Capturar mostrando a lista completa de issues + contadores por severidade no topo.

Salvar como: `docs/img/sonar-02-{classe}-antes.png` (um por membro).

### 9.3 Print #3 — Dashboard após correções

Mesmo enquadramento do #1, mas depois do re-scan.

Salvar como: `docs/img/sonar-03-dashboard-pos-fix.png`

### 9.4 Print #4 — Issues da classe-alvo (depois)

Mesma URL do #2, idealmente mostrando **0 issues** ou só as marcadas como **Won't Fix / False Positive / Safe** com justificativa.

Salvar como: `docs/img/sonar-04-{classe}-depois.png`.

### 9.5 Recomendações de captura

- Janela do navegador em **1440x900** ou maior (legibilidade).
- Tema claro (contraste melhor no PDF da entrega).
- **Não cortar** o cabeçalho com o nome do projeto e a data — serve para provar quando foi rodado.
- Se possível, deixar o cursor longe da tela.

---

## 10. O que pode ser marcado como "Won't Fix"

Issue pode ser **descartada com justificativa** sem perder ponto, desde que a justificativa apareça por escrito (na entrega ou no comment do Sonar):

| Issue | Justificativa típica |
|---|---|
| `"OK"` repetido se o grupo decidir não extrair | "Padrão do projeto; trade-off legibilidade vs. acoplamento. Custo do refactor maior que o benefício." |
| Métodos com 5+ parâmetros em `validateTransfer` | "Reflete a regra de negócio; criar record só pra esconder não traz valor." |
| `parseOrNull` retornando `null` | "Convenção explícita no nome do método (`OrNull`)." |
| Regex de e-mail complexa | "Auditada manualmente; não é vulnerável a ReDoS catastrófica." |

→ **Regra**: marcar como False Positive / Won't Fix **só com justificativa escrita**. Sem justificativa = falha.

---

## 11. Convenções

- **Não commitar** os relatórios PDF/HTML do Sonar no repo — pesados e voláteis.
- **Commitar apenas os prints** em `docs/img/` (PNG, < 500 KB cada).
- **Não desabilitar regras globalmente** sem discussão do grupo — preferir marcação por issue.
- **Commit das correções**: 1 commit por classe (ou por issue, se a mudança for grande), mensagem no padrão `fix: <classe> <descrição da issue>` (ex.: `fix: extract OK constant in AccountService (sonar S1192)`).
- **Não misturar refactor com fix de Sonar**: se uma issue do Sonar exige mudança grande, abrir issue separada no GitHub e linkar.

---

## 12. Critério de pronto

- [x] `docker-compose.sonar.yml` versionado.
- [x] `sonar-maven-plugin` e propriedades do projeto configurados no `pom.xml`.
- [ ] Print #1 (dashboard baseline) versionado em `docs/img/`.
- [ ] Print #2 (classe-alvo antes) versionado em `docs/img/` — **1 por membro**.
- [x] Issues da `AccountService` corrigidas e validadas por novo scan.
- [x] Branch atualizada com `origin/main` e reanalisada com 160 testes verdes.
- [x] Todas as issues abertas corrigidas e o hotspot restante revisado como seguro.
- [ ] Print #3 (dashboard pós-fix) versionado.
- [ ] Print #4 (classe-alvo depois) versionado — **1 por membro**.
- [x] **Quality Gate verde**, sem bugs, vulnerabilidades ou code smells abertos.
- [x] Tabela com **delta** da `AccountService` incluída na entrega.
- [x] README atualizado com o guia de execução do SonarQube.

O resultado da primeira classe corrigida esta documentado em
[RELATORIO_INSPECAO_SONAR_POS_CORRECAO.md](RELATORIO_INSPECAO_SONAR_POS_CORRECAO.md).

---

## 13. Referências

- **SonarQube — Try out SonarQube** — <https://docs.sonarsource.com/sonarqube-server/latest/try-out-sonarqube/>
- **sonar-maven-plugin** — <https://docs.sonarsource.com/sonarqube-server/latest/analyzing-source-code/scanners/sonarscanner-for-maven/>
- **Sonar way Java rules** — <https://rules.sonarsource.com/java/>
- **Quality Gates** — <https://docs.sonarsource.com/sonarqube-server/latest/instance-administration/quality-gates/>
- **Cognitive Complexity** (G. Ann Campbell) — paper de referência da métrica usada pelo Sonar.
