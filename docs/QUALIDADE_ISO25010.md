# Plano — Avaliação de qualidade pela ISO/IEC 25010

> Escopo da **Entrega 2 (parte C)**. Atribuir uma **medida em escala** para cada atributo de qualidade definido pela norma **ISO/IEC 25010**, com **justificativa** baseada em evidências do código.
>
> Complementa [TESTES_UNITARIOS.md](TESTES_UNITARIOS.md) (parte A) e [TESTES_INTEGRACAO.md](TESTES_INTEGRACAO.md) (parte B).

---

## 1. O que é a ISO/IEC 25010 (rápido)

**ISO/IEC 25010** é a norma internacional do modelo de qualidade de produto de software. Faz parte da família **SQuaRE** (Software product Quality Requirements and Evaluation). No Brasil, a tradução é **NBR ISO/IEC 25010:2016** (espelho da versão 2011 da ISO).

A norma **não dita métricas concretas** — ela define *o que* avaliar (8 características, ~30 subcaracterísticas), e cabe ao avaliador escolher *como* medir e em *qual* escala. Isso é exatamente o que o trabalho pede: indicar a medida e justificar a escolha.

### Versões

| Versão | Características | Quando usar |
|---|---|---|
| **2011** (NBR 2016) | 8 | Padrão acadêmico no Brasil. **É a versão usada neste plano.** |
| **2023** | 9 (acrescenta *Safety*; renomeia/move subcaracterísticas) | Citar como nota de rodapé; só adotar se a disciplina exigir |

### As 8 características (versão 2011)

```
1. Adequação Funcional   (Functional Suitability)
2. Eficiência de Desempenho (Performance Efficiency)
3. Compatibilidade        (Compatibility)
4. Usabilidade            (Usability)
5. Confiabilidade         (Reliability)
6. Segurança              (Security)
7. Manutenibilidade       (Maintainability)
8. Portabilidade          (Portability)
```

Cada uma tem 2–6 subcaracterísticas (detalhadas na seção 4).

---

## 2. Escala proposta (1 a 5)

Escala ordinal de cinco níveis. Cada nível tem critério objetivo — não é "achismo".

| Nível | Rótulo | Critério |
|---|---|---|
| **1** | Inadequado | Atributo ausente ou com falha clara; risco alto em produção |
| **2** | Marginal | Coberto parcialmente; lacunas relevantes documentadas |
| **3** | Aceitável | Atende ao mínimo esperado para o contexto acadêmico |
| **4** | Bom | Atende com evidências concretas (código, testes, configuração) |
| **5** | Excelente | Atende com folga e há **métrica objetiva** que comprova |

### Por que 1–5 e não outra escala?

- **0–10 dilui** — vira "deu uns 7" sem critério.
- **Binário (atende/não atende)** perde nuance — esconde lacunas.
- **1–5** força o avaliador a posicionar entre "marginal" e "bom", o que é o intervalo real de um sistema acadêmico funcional.
- A escala é coerente com modelos consagrados (CMMI usa 5 níveis; Likert também).

### Como combinar subcaracterísticas

A nota da característica é a **média aritmética arredondada para baixo** das subcaracterísticas, com a regra: se **qualquer subcaracterística for 1**, a característica não passa de **2**. Justificativa: uma falha grave em parte do atributo compromete o todo (uma falha de segurança não é "compensada" por outro pilar).

---

## 3. Como evidenciar uma medida

Toda nota precisa de **três campos**:

1. **O que é medido** — subcaracterística da norma.
2. **Como é medido** — métrica concreta (contagem, % cobertura, presença de configuração, etc.).
3. **Evidência** — arquivo + linha do repo, comando que reproduz, ou artefato externo (relatório de ferramenta).

Notas sem evidência viram chute. Quando a evidência não existir, registrar como **lacuna** e propor coleta (ferramenta + comando).

---

## 4. Avaliação por característica

> Notas marcadas como `(preliminar)` são uma **leitura inicial** feita lendo o código. A entrega final deve **revalidar** cada uma com a métrica indicada na coluna *Como medir*.

---

### 4.1 Adequação Funcional (Functional Suitability)

> Mede se o software faz o que promete fazer.

| Subcaracterística | O que medir | Como medir | Evidência | Nota (preliminar) |
|---|---|---|---|---|
| Completude funcional | % dos requisitos do enunciado implementados | Checklist contra os 9 fluxos do README (cadastro, login, saldo, depósito, saque, transferência, investimento, extrato, logout) | [README.md §Fluxos disponíveis](../README.md) | **5** |
| Correção funcional | Operações produzem resultado correto | Testes unitários verdes + cálculo monetário em `BigDecimal` HALF_UP + lock `FOR UPDATE` em transferência | [`Money.java`](../src/main/java/com/bancodigital/shared/money/Money.java), [`AccountService.transfer`](../src/main/java/com/bancodigital/account/AccountService.java#L77-L104) | **4** |
| Adequação funcional | Funções resolvem o objetivo do usuário sem excesso | Cada operação tem 1 endpoint dedicado; sem features especulativas | [`SecurityConfig.java:22`](../src/main/java/com/bancodigital/config/SecurityConfig.java#L22) (rotas listadas) | **4** |

**Nota da característica: 4 (Bom).**
**Justificativa**: Todos os 9 fluxos do enunciado estão implementados e cobertos por testes pelo menos no nível de validação. O `-1` em relação a "Excelente" se dá porque os métodos `@Transactional` (`withdraw`, `transfer`, `register`, `execute`) ainda não têm teste com banco real — a correção em ambiente integrado é hoje *presumida*, não *medida* (será resolvido na parte B).

---

### 4.2 Eficiência de Desempenho (Performance Efficiency)

> Mede uso de recursos sob carga.

| Subcaracterística | O que medir | Como medir | Evidência | Nota |
|---|---|---|---|---|
| Comportamento temporal | Tempo de resposta por operação chave | SLA medido nos testes E2E: página carrega ≤ 2s, submit responde ≤ 3s | [`PerformanceE2ETest`](../src/test/java/com/bancodigital/e2e/PerformanceE2ETest.java) — 4 casos verdes | **3** |
| Utilização de recursos | RAM/CPU em carga sustentada | `docker stats` durante teste de carga; expor `/actuator/metrics` | Actuator presente, mas só `/health` exposto | **2** |
| Capacidade | Conexões DB simultâneas suportadas | Pool HikariCP default (10 conn); teste com 50 reqs concorrentes | Default do starter, **não tunado** | **2** |

**Nota da característica: 2 (Marginal).**
**Justificativa**: Comportamento temporal subiu para **3** com a adição de `PerformanceE2ETest` (SLAs de 2s e 3s medidos em browser real). Utilização de recursos e capacidade permanecem em **2** — sem teste de carga (k6/JMeter) nem tuning de pool. A nota sobe para **3** na característica assim que `utilização de recursos` for medida; para **4** com `/actuator/prometheus` + dashboard Grafana.

---

### 4.3 Compatibilidade (Compatibility)

> Mede coexistência com outros sistemas.

| Subcaracterística | O que medir | Como medir | Evidência | Nota (preliminar) |
|---|---|---|---|---|
| Coexistência | Roda lado a lado sem conflito | Containers isolados; portas configuráveis via `.env` | [`docker-compose.yml`](../docker-compose.yml) (3 serviços isolados) | **4** |
| Interoperabilidade | Conformidade com protocolos padrão | HTTP/1.1, JDBC padrão, Postgres padrão, BCrypt — nada proprietário | [`pom.xml`](../pom.xml) (só libs open standard) | **3** |

**Nota da característica: 3 (Aceitável).**
**Justificativa**: A app não expõe API pública (só forms HTML), então "interoperabilidade" tem teto natural baixo — não há REST/JSON consumível por terceiros. Coexistência é boa por design (Docker Compose isolado). Para subir para **4**, exporia endpoints REST documentados (OpenAPI), o que está **fora do escopo acadêmico**.

---

### 4.4 Usabilidade (Usability)

> Mede facilidade de uso pelo usuário final.

| Subcaracterística | O que medir | Como medir | Evidência | Nota (preliminar) |
|---|---|---|---|---|
| Reconhecibilidade | Usuário entende a função em < 5 s | Labels em pt-BR, ícones temáticos no dashboard | Templates Thymeleaf em `src/main/resources/templates/` | **4** |
| Apreensibilidade | Curva de aprendizado | README com 5 usuários seed prontos + screenshots/instruções | [README.md §Usuários de teste](../README.md) | **4** |
| Operabilidade | Forms simples, feedback claro | Flash messages de sucesso/erro em todos os POSTs | [`TransferController.java:47-54`](../src/main/java/com/bancodigital/account/TransferController.java#L47-L54) | **3** |
| Proteção contra erros | Validação antes da operação | `Messages` centraliza strings; validação no `Service`, não no controller | [`Messages.java`](../src/main/java/com/bancodigital/shared/Messages.java) | **4** |
| Estética da UI | Coerência visual | CSS puro, sem framework; funcional mas não polido | [`style.css`](../src/main/resources/static/css/style.css) | **3** |
| Acessibilidade | WCAG 2.1 AA | **Não auditado** (sem ARIA explícito, sem teste com axe-core, sem contraste medido) | — | **1** |

**Nota da característica: 2 (Marginal).**
**Justificativa**: Cinco subcaracterísticas entre 3 e 4, mas **acessibilidade é 1** — a regra "qualquer 1 → max 2" se aplica. Para subir para 3, basta uma auditoria com **axe-core** ou **Lighthouse** e correções básicas (alt text, contraste, navegação por teclado). Como o contexto é acadêmico e usuário-alvo é o avaliador, é defensável; **mas a nota deve refletir a lacuna**, não escondê-la.

---

### 4.5 Confiabilidade (Reliability)

> Mede continuidade do serviço sob falha.

| Subcaracterística | O que medir | Como medir | Evidência | Nota (preliminar) |
|---|---|---|---|---|
| Maturidade | Defeitos por release | 89 testes unitários verdes; zero issues abertas após PRs #4, #12–#16 | [README.md §Issues resolvidas](../README.md) | **3** |
| Disponibilidade | Uptime sob restart | `restart: unless-stopped` + healthcheck no Postgres | [`docker-compose.yml:15-19`](../docker-compose.yml#L15-L19) | **3** |
| Tolerância a falhas | App degrada graciosamente | `GlobalExceptionHandler` captura `DomainException`; sem retry/circuit breaker | [`GlobalExceptionHandler.java`](../src/main/java/com/bancodigital/shared/exception/GlobalExceptionHandler.java) | **2** |
| Recuperabilidade | RPO/RTO em caso de queda | Volume `pgdata` persiste; WAL do Postgres. **Sem backup automatizado** | [`docker-compose.yml:11-12`](../docker-compose.yml#L11-L12) | **2** |

**Nota da característica: 2 (Marginal).**
**Justificativa**: Single instance, sem retry, sem backup automatizado. Apropriado para escopo acadêmico, mas **não é confiável em sentido produtivo**. Para subir para 3, basta script de backup do volume `pgdata` documentado. Para 4, replicação ou backup off-site.

---

### 4.6 Segurança (Security)

> Mede proteção da informação.

| Subcaracterística | O que medir | Como medir | Evidência | Nota (preliminar) |
|---|---|---|---|---|
| Confidencialidade | Dados sensíveis cifrados | Senhas com `BCryptPasswordEncoder(10)`; sessão via cookie HttpOnly do Spring Security | [`SecurityConfig.java:13-16`](../src/main/java/com/bancodigital/config/SecurityConfig.java#L13-L16) | **4** |
| Integridade | Impossibilidade de tampering | CSRF token automático (Spring Security default); SQL parametrizado em **todos** os JDBC; `FOR UPDATE` em transferência | [`JdbcAccountRepository.java`](../src/main/java/com/bancodigital/account/JdbcAccountRepository.java) (só `NamedParameterJdbcTemplate`) | **5** |
| Não repúdio | Rastreabilidade de operações | Tabela `transactions` registra cada operação com `date`; **mas não há audit log HTTP** (quem chamou de qual IP) | [`V1__init_schema.sql`](../src/main/resources/db/migration/V1__init_schema.sql) | **3** |
| Responsabilização | Ação ligada a um ator | `transactions.source_account` + `accounts.user_id` permitem rastrear; **logs aplicação não logam ações** | — | **2** |
| Autenticidade | Identidade verificável | Form login + BCrypt; **sem 2FA, sem rotação de credencial** | [`SecurityConfig.java:25-33`](../src/main/java/com/bancodigital/config/SecurityConfig.java#L25-L33) | **3** |

**Nota da característica: 3 (Aceitável).**
**Justificativa**: Integridade é o ponto forte (CSRF + parametrização + lock). Falhas em accountability (sem log de operações) e autenticidade (sem 2FA, sem rate limit no login) impedem nota maior. Como é trabalho acadêmico e o enunciado **destaca segurança como critério** (issues #12, #13, #14, #15 já fechadas), **3 é honesto**. Para 4, adicionar:
- Log estruturado (Logback JSON) com `userId` em cada operação financeira.
- Rate limiting no `/login` (Spring Security `@RateLimiter` ou Bucket4j).
- Headers de segurança (`X-Content-Type-Options`, `Strict-Transport-Security`).

---

### 4.7 Manutenibilidade (Maintainability)

> Mede facilidade de modificar o sistema.

| Subcaracterística | O que medir | Como medir | Evidência | Nota (preliminar) |
|---|---|---|---|---|
| Modularidade | Acoplamento entre módulos | Pacotes por domínio: `auth`, `signup`, `account`, `transaction`, `investment`, `shared` — dependências unidirecionais | [`src/main/java/com/bancodigital/`](../src/main/java/com/bancodigital/) | **4** |
| Reusabilidade | Componentes reutilizáveis | `Money`, `Messages`, `DomainException` compartilhados entre domínios | [`shared/`](../src/main/java/com/bancodigital/shared/) | **4** |
| Analisabilidade | Tempo para entender o código | README + [ARCHITECTURE.md](ARCHITECTURE.md) + nomes em inglês claros + camadas explícitas Controller/Service/Repository | [README.md §Arquitetura](../README.md) | **4** |
| Modificabilidade | Custo de uma mudança | Interfaces de repositório separadas da implementação JDBC; trocar SQL não afeta Service | `AccountRepository` (interface) vs `JdbcAccountRepository` (impl) | **4** |
| Testabilidade | Facilidade de escrever testes | Services com injeção por construtor; `Clock` injetado em `InvestmentService` — preparado para fakes | [`InvestmentService.java:41-49`](../src/main/java/com/bancodigital/investment/InvestmentService.java#L41-L49) | **4** |

**Nota da característica: 4 (Bom).**
**Justificativa**: Esta é a característica mais forte do projeto. Camadas explícitas, interfaces de repositório, `Clock` injetado, regras centralizadas em `Messages`. Para 5, exigiria métricas objetivas (ex.: cobertura JaCoCo ≥ 85 %, complexidade ciclomática média < 5 via SonarQube). Sem ferramenta de análise estática rodando, fica em 4.

---

### 4.8 Portabilidade (Portability)

> Mede facilidade de transferir para outro ambiente.

| Subcaracterística | O que medir | Como medir | Evidência | Nota (preliminar) |
|---|---|---|---|---|
| Adaptabilidade | Funciona em ambientes diferentes | Configs via env vars (`DB_URL`, `DB_USER`, `DB_PASSWORD`); perfil `docker` separado | [`application.yml`](../src/main/resources/application.yml) + [`application-docker.yml`](../src/main/resources/application-docker.yml) | **4** |
| Instalabilidade | Esforço de instalação | `docker compose up -d --build` — **um comando** | [README.md §Quick start](../README.md) | **5** |
| Substituibilidade | Trocar componentes | JDBC padrão + Flyway = trocar Postgres por outro RDBMS é uma migration nova. **Mas:** uso de `ON CONFLICT` (sintaxe Postgres) acopla o repositório | [`JdbcInvestmentRepository.java:36-41`](../src/main/java/com/bancodigital/investment/JdbcInvestmentRepository.java#L36-L41) | **3** |

**Nota da característica: 4 (Bom).**
**Justificativa**: Instalação é destaque do projeto — Docker Compose com healthcheck e Flyway aplicando schema no boot. Substituibilidade tem teto natural por uso de SQL específico do Postgres (intencional, para resolver issue #15 com `ON CONFLICT`).

---

## 5. Quadro resumo

| # | Característica | Nota | Justificativa em 1 linha |
|---|---|---|---|
| 1 | Adequação Funcional | **4** | 9/9 fluxos implementados; correção presumida (sem teste de integração ainda) |
| 2 | Eficiência de Desempenho | **2** | Sem coleta de métricas; default do HikariCP; sem teste de carga |
| 3 | Compatibilidade | **3** | Padrões abertos (JDBC, HTTP); sem REST público por escopo |
| 4 | Usabilidade | **2** | Forms claros em pt-BR, mas acessibilidade não auditada (cai por regra de penalidade) |
| 5 | Confiabilidade | **2** | Single instance, sem backup automatizado, sem retry |
| 6 | Segurança | **3** | BCrypt + CSRF + parametrização fortes; sem audit log, sem 2FA |
| 7 | Manutenibilidade | **4** | Camadas claras, interfaces, `Clock` injetado, código testável |
| 8 | Portabilidade | **4** | `docker compose up` em um comando; SQL específico do Postgres limita substituibilidade |

**Média ponderada simples: 3.0 (Aceitável).**

### Radar (representação textual)

```
              Adequação Funcional
                      4
Portabilidade 4                 2 Eficiência
                  ●─────●
                 ╱       ╲
Manutenibilidade 4         3 Compatibilidade
                 ╲       ╱
                  ●─────●
   Segurança 3                  2 Usabilidade
                      2
                Confiabilidade
```

Sugestão para a entrega: gerar o radar com **Chart.js** ou **mermaid** (`%%{init: {"theme":"forest"}}%% pie/radar`) e incluir como imagem.

---

## 6. O que precisa ser feito (checklist da entrega)

### 6.1 Validar as notas preliminares

- [ ] Revisar cada uma das 8 seções acima — confirmar ou ajustar a nota com evidência fresca.
- [ ] Para cada nota **2 ou menor**, registrar como **risco conhecido** com proposta de mitigação.

### 6.2 Coletar evidências objetivas onde falta

| Atributo | Métrica a coletar | Ferramenta | Comando |
|---|---|---|---|
| Eficiência | p95 de latência em `/transfer` | k6 | `k6 run scripts/load-transfer.js` |
| Eficiência | RAM/CPU em carga | `docker stats` | exportar CSV |
| Usabilidade | WCAG 2.1 AA | axe-core CLI | `axe http://localhost:8080/dashboard` |
| Manutenibilidade | Cobertura | JaCoCo | adicionar plugin no `pom.xml` |
| Manutenibilidade | Complexidade | SonarQube ou checkstyle | `mvn sonar:sonar` |
| Segurança | Dependências vulneráveis | OWASP Dependency-Check | `mvn org.owasp:dependency-check-maven:check` |

> Coletar **só o que cabe no escopo da entrega**. Métricas que não vão ser coletadas devem ficar marcadas como "lacuna conhecida — fora de escopo".

### 6.3 Produzir o artefato final

- [ ] Tabela resumo (seção 5) atualizada com notas validadas.
- [ ] Radar gráfico exportado como `docs/img/radar-iso25010.png` ou mermaid inline.
- [ ] Cada característica com 1 parágrafo de justificativa (≤ 5 linhas) referenciando arquivo/linha.
- [ ] Seção "Riscos e mitigações" com tudo que ficou 2 ou menos.

---

## 7. Convenções para defender as notas

- **Sempre citar arquivo + linha** ou comando reproduzível. "Achismo" não passa.
- **Nunca inflar nota** para parecer melhor — perder ponto por honestidade vale mais que um 5 indefensável.
- **Notas baixas viram tarefas**, não desculpas. Para cada 1 ou 2, definir o que falta para subir um nível.
- **Empate técnico**: se duas subcaracterísticas pesam em sentidos opostos, escolher a menor e justificar. Conservador > otimista.

---

## 8. Critério de pronto

- [ ] Documento `QUALIDADE_ISO25010.md` (este arquivo) revisado com notas **finais**, não preliminares.
- [ ] Quadro resumo (seção 5) confere com as justificativas individuais.
- [ ] Pelo menos 3 métricas objetivas coletadas (uma por atributo de nota baixa).
- [ ] Radar gerado e incluído.
- [ ] README atualizado na seção "Próximas entregas" listando esta avaliação como entregue.
- [ ] Apresentação (slides ou markdown) com 1 slide por característica para defesa oral.

---

## 9. Referências

- **ISO/IEC 25010:2011** — *Systems and software Quality Requirements and Evaluation (SQuaRE) — System and software quality models*.
- **ABNT NBR ISO/IEC 25010:2016** — versão brasileira da norma.
- **ISO/IEC 25023:2016** — *Measurement of system and software product quality* — métricas concretas sugeridas pela norma (consultar para refinar a coluna "Como medir" da seção 4).
- **ISO/IEC 25010:2023** — atualização (acrescenta *Safety*). Citar como nota se a disciplina pedir versão recente.
