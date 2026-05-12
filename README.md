# Banco Digital — Sistema Bancário Acadêmico

Aplicação web acadêmica que simula operações básicas de um sistema bancário (login, cadastro, saldo, depósito, saque, transferência, investimento e extrato), construída com **Spring Boot 3 + Thymeleaf** sobre **PostgreSQL**, empacotada em **Docker Compose**.

![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9%2B-C71A36?logo=apachemaven&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-10-CC0200?logo=flyway&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3-005F0F?logo=thymeleaf&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)
![JUnit 5](https://img.shields.io/badge/JUnit-5-25A162?logo=junit5&logoColor=white)

## Sobre o projeto

Trabalho das disciplinas de Qualidade e Teste de Software. Foi profissionalizado a partir de uma base original em Servlets+JSP+Derby para uma stack moderna **sem perder regra de negócio**: hashing BCrypt para senhas, transações JDBC atômicas com row-level locking, constraints de unicidade no schema, mensagens de erro centralizadas e templates Thymeleaf com escape automático contra XSS.

Domínios funcionais:

| Domínio | Endpoints |
|---|---|
| Login / Painel | `GET /login`, `POST /login`, `GET /painel`, `POST /logout` |
| Cadastro | `GET /cadastro`, `POST /cadastro` |
| Saldo | `GET /saldo` |
| Depósito | `GET /deposito`, `POST /deposito` |
| Saque | `GET /saque`, `POST /saque` |
| Transferência | `GET /transferencia`, `POST /transferencia` |
| Investimento | `GET /investimento`, `POST /investimento` |
| Extrato | `GET /extrato` |

## Stack

- **Java 17 LTS**
- **Spring Boot 3.3** (web, jdbc, security, thymeleaf, validation, actuator)
- **PostgreSQL 16** (substitui Derby)
- **HikariCP** (pool de conexões — vem com `spring-boot-starter-jdbc`)
- **Flyway** (migrações versionadas em `src/main/resources/db/migration`)
- **Spring Security** com form login, CSRF e BCrypt
- **Thymeleaf** (templates HTML server-side)
- **JUnit 5** (testes unitários — sem Mockito, sem Testcontainers nesta etapa)
- **Docker Compose** (app + postgres + adminer)

## Estrutura

```
.
├── docker-compose.yml          # postgres + app + adminer
├── Dockerfile                  # multi-stage maven → jre alpine
├── .env.example                # credenciais p/ desenvolvimento
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/bancodigital/
│   │   │   ├── BancodigitalApplication.java
│   │   │   ├── config/         # SecurityConfig, AppConfig, HomeController
│   │   │   ├── shared/         # Mensagens, Money, DomainException, GlobalExceptionHandler
│   │   │   ├── login/          # LoginService, Usuario, UsuarioRepository, CustomUserDetailsService
│   │   │   ├── cadastro/       # CadastroService, CadastroForm, CadastroController
│   │   │   ├── conta/          # ContaService, ContaRepository, controllers (Saldo/Saque/Deposito/Transferencia)
│   │   │   ├── transacao/      # TransacaoRepository, ExtratoController, ExtratoLinha
│   │   │   └── investimento/   # InvestimentoService, InvestimentoRepository, controller
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-docker.yml
│   │       ├── db/migration/   # V1__init_schema.sql, V2__seed_data.sql
│   │       ├── static/css/
│   │       └── templates/      # *.html Thymeleaf
│   └── test/java/com/bancodigital/   # 68 testes unitários JUnit 5
└── docs/ARCHITECTURE.md
```

## Pré-requisitos

- **Docker** e **Docker Compose** (caminho recomendado), ou
- **JDK 17** + **Maven 3.9+** + **PostgreSQL 16** rodando localmente.

## Executar com Docker (recomendado)

```bash
cp .env.example .env       # opcionalmente edite POSTGRES_PASSWORD
docker compose up -d --build
```

A aplicação fica disponível em:

- **App**: <http://localhost:8080> (redireciona para `/login`)
- **Adminer** (UI para o banco): <http://localhost:8081> (servidor: `postgres`, base: `bancodigital`, usuário/senha vindos do `.env`)
- **Postgres**: `localhost:5432`

Para derrubar:

```bash
docker compose down            # mantém os dados
docker compose down -v         # apaga o volume pgdata
```

## Usuários de teste (seed)

Todos com senha `senha123`:

| E-mail | Conta | Saldo | Observação |
|---|---|---|---|
| `joao@email.com`   | C00001 | R$ 1.500,00   | Histórico simples de depósito/saque |
| `maria@email.com`  | C00002 | R$ 9.999,99   | Tem investimento ativo de R$ 500 |
| `pedro@email.com`  | C00003 | R$ 0,00       | Conta zerada para testar saldo insuficiente |
| `ana@email.com`    | C00004 | R$ 25.000,00  | Histórico variado, investimento de R$ 1.500 |
| `carlos@email.com` | C00005 | R$ 100,00     | Saldo baixo |

## Executar localmente sem Docker

1. Suba um Postgres local com a base/usuário desejados.
2. Exporte variáveis de ambiente:
   ```bash
   export DB_URL=jdbc:postgresql://localhost:5432/bancodigital
   export DB_USER=bancodigital
   export DB_PASSWORD=bancodigital
   ```
3. Rode com:
   ```bash
   mvn spring-boot:run
   ```

Flyway aplica `V1__init_schema.sql` e `V2__seed_data.sql` automaticamente na primeira execução.

## Testes

```bash
mvn test
```

Cobertura unitária atual (89 testes JUnit 5 puros — sem mocks, sem fakes, sem dependências externas):

| Suite | Testes | Cobre |
|---|---|---|
| `MoneyTest` | 16 | helpers `normalize` / `parseOrNull` / `isPositive` / `format` |
| `TipoTransacaoTest` | 9 | enum e parsing por valor do banco |
| `ExtratoLinhaTest` | 15 | factory `de()`, `corPara`, `descricaoPara` |
| `CadastroServiceTest` | 12 | validação pura de nome/email/senha |
| `ContaServiceTest` | 23 | validações `validaSaque`/`validarDeposito`/`validarTransferencia` |
| `InvestimentoServiceTest` | 14 | `calcularValorComJuros` (juros compostos), `validarOperacao` |

Todos os testes exercitam **métodos puros** que não dependem de banco, HTTP, sessão ou Spring context. Cenários que precisam atravessar repositórios (`cadastrar`, `sacar`, `depositar`, `transferir`, `consultar`, `executar` do Investimento, fluxo completo de login) ficam para a próxima etapa, junto com os testes de integração via Testcontainers + PostgreSQL.

## Troubleshooting da IDE

Se a IDE acusar **"Syntax error on token(s), misplaced construct(s)"** em `record`, `switch` com `->`, ou **"X cannot be resolved to a type"** mesmo com `mvn verify` passando, é cache stale do Eclipse JDT Language Server. Resolva com:

**VSCode:**
1. Crie `.vscode/settings.json` apontando para um JDK ≥ 17:
   ```json
   {
     "java.configuration.runtimes": [
       { "name": "JavaSE-17", "path": "/caminho/para/jdk-17-ou-superior" }
     ]
   }
   ```
2. `Cmd+Shift+P` → **Java: Clean Java Language Server Workspace** → "Restart and delete".
3. Aguarde o Java extension reimportar o `pom.xml`.

**IntelliJ:**
1. `File` → `Project Structure` → `Project SDK` → selecione JDK 17+.
2. `Maven` panel → "Reload All Maven Projects".

**Eclipse:**
1. `Project` → `Properties` → `Java Build Path` → confirme JRE System Library [JavaSE-17].
2. `Project` → `Clean...` → "Clean all projects".

Verifique o JDK ativo com `mvn -version` (precisa ser ≥ 17).

## Issues resolvidas nesta etapa

- **#4** — migração Derby → PostgreSQL + Docker Compose
- **#12** — senhas com BCrypt (`spring-security-crypto`)
- **#13** — cadastro atômico (`@Transactional` em `CadastroService`, FK `usuario_id NOT NULL`)
- **#14** — conta com `UNIQUE(numero)` + `nextval('conta_numero_seq')` (sem `Math.random`)
- **#15** — `investimento.usuario_id UNIQUE` + `INSERT ... ON CONFLICT DO NOTHING` para upsert idempotente
- **#16** — mensagens centralizadas em `com.bancodigital.shared.Mensagens` (mesma string em validação e UI)
- **#6, #7, #8, #9, #10** — Parte A (unitários) implementada; Parte B (integração) fica para próxima etapa

## Documentação adicional

- [Arquitetura do sistema](docs/ARCHITECTURE.md)
