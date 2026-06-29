# Relatorio de inspecao SonarQube - pos-correcao

## 1. Escopo

Este relatorio registra a correcao dos apontamentos encontrados pelo SonarQube
na classe `AccountService`. O resultado anterior esta preservado em
[RELATORIO_INSPECAO_SONAR_BASELINE.md](RELATORIO_INSPECAO_SONAR_BASELINE.md).

Data da nova analise: 22 de junho de 2026.

## 2. Problemas corrigidos

| Regra | Tipo | Problema | Correcao |
|---|---|---|---|
| `java:S1192` | Code smell | A mensagem `Conta nao encontrada.` estava repetida cinco vezes | A mensagem foi centralizada em `Messages.ACCOUNT_NOT_FOUND`, mantendo o texto consistente e evitando duplicacao |
| `java:S2259` | Bug | O destino da transferencia poderia ser nulo antes de ser validado | A busca passou a usar `Optional<Account>`, tornando a ausencia explicita e impedindo acesso inseguro ao objeto |

O `Optional` foi limitado ao fluxo interno do servico. O contrato publico e o
comportamento esperado da transferencia nao foram alterados.

## 3. Validacao por testes

Primeiro foram executados apenas os testes relacionados a `AccountService`:

```powershell
mvn "-Dtest=AccountServiceTest,AccountServiceTransferTest,AccountServiceWithdrawTest" test
```

Resultado:

```text
Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Depois foi executada a verificacao geral usada no baseline:

```powershell
mvn clean verify -DskipITs
```

Resultado:

```text
Tests run: 153, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

O parametro `-DskipITs` foi mantido porque os testes E2E de transferencia e
saque dependem do Firefox instalado na maquina. Essa restricao nao interfere
nos testes unitarios e de integracao utilizados para validar a correcao.

## 4. Nova analise do SonarQube

O projeto foi enviado novamente ao SonarQube:

```powershell
mvn sonar:sonar "-Dsonar.token=$env:SONAR_TOKEN"
```

Resultado:

```text
ANALYSIS SUCCESSFUL
```

Identificador da tarefa de analise:
`2982d19c-7679-4c65-9fd4-f24bbc7530d8`.

## 5. Comparativo

| Metrica | Antes | Depois | Variacao |
|---|---:|---:|---:|
| Bugs | 2 | 1 | -1 |
| Vulnerabilidades | 1 | 1 | 0 |
| Code smells | 32 | 31 | -1 |
| Security hotspots | 1 | 1 | 0 |
| Cobertura | 86,7% | 86,7% | 0 |
| Duplicacao de linhas | 2,9% | 2,9% | 0 |
| Issues em `AccountService` | 2 | **0** | **-2** |

Os contadores gerais ainda incluem apontamentos de outras classes. Para o
escopo desta correcao, a `AccountService` ficou sem issues abertas.

## 6. Reanalise apos a atualizacao da branch

Depois da integracao dos cinco novos commits de `origin/main` na branch
`Mainoth`, o projeto foi validado novamente:

```powershell
mvn clean verify -DskipITs
```

Resultado atualizado:

```text
Tests run: 160, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Em seguida, foi executada uma nova inspecao:

```powershell
mvn sonar:sonar "-Dsonar.token=$env:SONAR_TOKEN"
```

Resultado:

```text
ANALYSIS SUCCESSFUL
```

Identificador da tarefa de analise:
`eaa08b79-ed8f-4f7b-b6bc-202e959e5917`.

### 6.1 Comparacao das tres analises

| Metrica | Baseline | Apos corrigir `AccountService` | Apos atualizar a `main` |
|---|---:|---:|---:|
| Bugs | 2 | 1 | 1 |
| Vulnerabilidades | 1 | 1 | 1 |
| Code smells | 32 | 31 | 41 |
| Security hotspots | 1 | 1 | 1 |
| Cobertura | 86,7% | 86,7% | **87,9%** |
| Duplicacao de linhas | 2,9% | 2,9% | 2,9% |
| Linhas de codigo | 1.280 | 1.280 | 1.284 |
| Complexidade ciclomatica | 196 | 196 | 197 |
| Complexidade cognitiva | 85 | 85 | 85 |
| Issues em `AccountService` | 2 | **0** | **0** |

A cobertura aumentou 1,2 ponto percentual com os testes recebidos da `main`.
O aumento de 31 para 41 code smells nao representa regressao na
`AccountService`, que continuou com zero apontamentos. Ele ocorreu apos a
inclusao e reanalise de novos arquivos de teste.

### 6.2 Distribuicao dos apontamentos naquela analise

| Regra | Quantidade | Explicacao resumida |
|---|---:|---|
| `java:S5778` | 16 | Lambdas de `assertThrows` com mais de uma chamada que pode lancar excecao |
| `java:S2925` | 11 | Uso de `Thread.sleep()` em testes |
| `java:S1192` | 4 | Literais repetidos que podem ser extraidos para constantes |
| `java:S5786` | 4 | Modificadores `public` desnecessarios em classes ou metodos de teste |
| `java:S125` | 2 | Blocos de codigo comentado |
| `java:S5976` | 2 | Casos semelhantes que podem virar testes parametrizados |
| `java:S1128` | 2 | Imports nao utilizados |

Naquele momento permaneciam uma vulnerabilidade `java:S6437`, em
`SecurityConfig`, e um bug `java:S2259`, em `JdbcUserRepository`. Esses
apontamentos foram incluidos na rodada final descrita a seguir.

## 7. Rodada final de correcoes

Todas as issues abertas foram avaliadas. As correcoes abrangeram:

- constantes para literais repetidos em repositories, seguranca e cadastro;
- validacao explicita da chave gerada em `JdbcUserRepository`;
- remocao da configuracao redundante do parametro de senha do Spring Security;
- lambdas de `assertThrows` com somente a chamada sob verificacao;
- testes parametrizados para deposito e saque;
- remocao de imports e declaracoes de excecao desnecessarios;
- substituicao de `Thread.sleep()` por `WebDriverWait`;
- sincronizacao do login e dos alertas nos testes E2E.

### 7.1 Validacao completa do sistema

A suite completa foi executada com:

```powershell
mvn clean verify
```

Resultado:

```text
Testes unitarios e de integracao: 160 aprovados
Testes E2E: 30 aprovados
Falhas: 0
Erros: 0
BUILD SUCCESS
```

Para preservar a comparabilidade da cobertura com o baseline, o relatorio
oficial do JaCoCo foi regenerado sem executar novamente os E2E:

```powershell
mvn clean verify -DskipITs
mvn sonar:sonar "-Dsonar.token=$env:SONAR_TOKEN"
```

Identificador da tarefa final:
`a081d384-c040-48db-b681-1d2da8b4adb1`.

### 7.2 Resultado final do SonarQube

| Metrica | Antes da rodada final | Resultado final |
|---|---:|---:|
| Bugs | 1 | **0** |
| Vulnerabilidades | 1 | **0** |
| Code smells | 37 | **0** |
| Issues abertas | 39 | **0** |
| Cobertura oficial | 87,9% | **87,7%** |
| Duplicacao de linhas | 2,9% | **2,9%** |
| Quality Gate | - | **OK** |

A pequena variacao de cobertura ocorreu porque as validacoes defensivas e os
helpers adicionaram linhas executaveis. O resultado continua acima da meta de
80% definida para o trabalho.

### 7.3 Security Hotspot revisado

O hotspot `java:S2068` em `Messages.PASSWORD_TOO_SHORT` foi revisado como
**Safe**. A constante armazena apenas a mensagem de validacao "Senha deve ter
ao menos 8 caracteres"; ela nao contem senha, hash, token ou qualquer
credencial. Depois da revisao, nao restaram hotspots com status `To Review`.

## 8. Evidencias visuais pendentes

Com o SonarQube aberto em <http://localhost:9000>, salvar:

- dashboard posterior em `docs/img/sonar-03-dashboard-pos-fix.png`;
- filtro da `AccountService` com zero issues em
  `docs/img/sonar-04-account-service-depois.png`.

Os prints precisam ser feitos na sessao autenticada do navegador. O token do
scanner nao deve ser publicado nem inserido nas imagens.

