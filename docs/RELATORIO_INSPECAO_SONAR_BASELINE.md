# Relatorio de inspecao SonarQube - baseline

## 1. Identificacao

| Campo | Valor |
|---|---|
| Projeto | Banco Digital |
| Chave no SonarQube | `bancodigital` |
| SonarQube | Community 10.6 |
| Perfil de qualidade | Sonar way |
| Scanner Maven | 5.0.0.4389 |
| Data da analise | 22 de junho de 2026 |
| Revisao analisada | `4d7e7aa59aa09df23e6bd3172810c5a24d22c850` |

## 2. Procedimento executado

O SonarQube foi iniciado com:

```powershell
docker compose -f docker-compose.sonar.yml up -d
```

Como os testes E2E de `TransferE2ETest` e `WithdrawE2ETest` exigem Firefox
instalado, o baseline de inspecao foi preparado com os testes unitarios e de
integracao:

```powershell
mvn clean verify -DskipITs
```

Resultado do build:

```text
Tests run: 153, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

O envio ao SonarQube foi feito com:

```powershell
mvn sonar:sonar "-Dsonar.token=$env:SONAR_TOKEN"
```

Resultado:

```text
ANALYSIS SUCCESSFUL
```

## 3. Resultado geral antes das correcoes

| Metrica | Resultado |
|---|---:|
| Bugs | **2** |
| Vulnerabilidades | **1** |
| Code smells | **32** |
| Security hotspots | **1** |
| Cobertura | **86,7%** |
| Duplicacao de linhas | **2,9%** |
| Linhas de codigo | **1.280** |
| Complexidade ciclomatica | **196** |
| Complexidade cognitiva | **85** |

Dashboard:

<http://localhost:9000/dashboard?id=bancodigital>

## 4. Classes-alvo originalmente definidas

| Classe | Cobertura | Complexidade | Cognitiva | Issues abertas |
|---|---:|---:|---:|---:|
| `AccountService` | 85,0% | 35 | 22 | **2** |
| `InvestmentService` | 98,8% | 24 | 15 | **0** |
| `SignupService` | 100,0% | 12 | 9 | **0** |
| `Money` | 97,9% | 17 | 13 | **0** |
| `StatementLine` | 87,5% | 15 | 5 | **0** |

### Issues de `AccountService`

| Severidade | Tipo | Regra | Linha | Descricao |
|---|---|---|---:|---|
| Critical | Code smell | `java:S1192` | 28 | Extrair constante para a mensagem de conta nao encontrada repetida cinco vezes |
| Major | Bug | `java:S2259` | 90 | Possivel `NullPointerException` porque `destination` pode ser nulo |

## 5. Outras issues de producao

| Classe | Severidade | Tipo | Regra | Descricao |
|---|---|---|---|---|
| `SecurityConfig` | Critical | Code smell | `java:S1192` | Literal `/login` repetido |
| `SecurityConfig` | Blocker | Vulnerability | `java:S6437` | Credencial identificada como comprometida |
| `SignupController` | Critical | Code smell | `java:S1192` | Literal `signupForm` repetido |
| `JdbcUserRepository` | Critical | Code smell | `java:S1192` | Literal `email` repetido |
| `JdbcUserRepository` | Major | Bug | `java:S2259` | `getKey()` pode retornar nulo |
| `JdbcAccountRepository` | Critical | Code smell | `java:S1192` | Literal `number` repetido |
| `Messages` | Security hotspot | `java:S2068` | Nome de constante contendo `PASSWORD` requer revisao manual |

## 6. Restricao encontrada para a divisao por integrante

O enunciado exige corrigir problemas de pelo menos uma classe nao CRUD e com
complexidade razoavel para cada integrante. Entretanto, o perfil padrao
**Sonar way** encontrou issues em apenas tres classes nao CRUD:

1. `AccountService`;
2. `SecurityConfig`;
3. `SignupController`.

As classes `InvestmentService`, `SignupService`, `Money` e `StatementLine`
possuem complexidade suficiente para a atividade, mas nao apresentam issues no
baseline atual. Nao e correto introduzir defeitos artificialmente apenas para
depois remove-los.

O grupo deve adotar uma das seguintes decisoes:

1. Ativar regras adicionais em um perfil de qualidade proprio e executar novo
   baseline antes das correcoes.
2. Utilizar outra ferramenta de inspecao complementar, como PMD, para localizar
   melhorias nas classes sem issues no Sonar.
3. Validar com o professor se classes sem issues podem ser apresentadas como
   evidencia de conformidade, mantendo as correcoes nas classes que possuem
   achados reais.

## 7. Prints pendentes do baseline

Antes de qualquer correcao, capturar:

- `docs/img/sonar-01-dashboard-baseline.png`;
- `docs/img/sonar-02-account-service-antes.png`;
- prints das demais classes-alvo mostrando zero issues, caso sejam usados como
  evidencia de conformidade.

## 8. Proximos passos

1. Salvar os prints do baseline.
2. Corrigir primeiro o bug e o code smell de `AccountService`.
3. Corrigir as issues de `SecurityConfig` e `SignupController`.
4. Definir com o grupo a estrategia para completar cinco classes.
5. Executar testes e novo scan.
6. Registrar as metricas e prints posteriores.

