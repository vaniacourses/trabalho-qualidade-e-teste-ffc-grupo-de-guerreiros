<div align="center">

# 🏦 Banco Digital

Sistema bancário acadêmico full-stack, com autenticação segura, depósitos, saques, transferências, investimentos com juros compostos e extrato.
Construído com **Spring Boot 3 + Thymeleaf + PostgreSQL**, totalmente empacotado em **Docker Compose** — sobe em um comando.

[![Java](https://img.shields.io/badge/Java-17_LTS-007396?style=for-the-badge&logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://docs.docker.com/compose/)

[![Maven](https://img.shields.io/badge/Maven-3.9-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-6-6DB33F?logo=springsecurity&logoColor=white)](https://docs.spring.io/spring-security/)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3-005F0F?logo=thymeleaf&logoColor=white)](https://www.thymeleaf.org/)
[![Flyway](https://img.shields.io/badge/Flyway-10-CC0200?logo=flyway&logoColor=white)](https://flywaydb.org/)
[![HikariCP](https://img.shields.io/badge/HikariCP-pool-2C3E50)](https://github.com/brettwooldridge/HikariCP)
[![JUnit 5](https://img.shields.io/badge/JUnit-5-25A162?logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![Adminer](https://img.shields.io/badge/Adminer-DB%20UI-34567C)](https://www.adminer.org/)

</div>

---

## 📑 Sumário

1. [O que o projeto faz](#-o-que-o-projeto-faz)
2. [Tecnologias](#-tecnologias)
3. [Arquitetura](#-arquitetura)
4. [Passo a passo para rodar](#-passo-a-passo-para-rodar)
   - [1. Instalar o Docker](#1-instalar-o-docker)
   - [2. Clonar o repositório](#2-clonar-o-repositório)
   - [3. Subir a aplicação](#3-subir-a-aplicação)
   - [4. Acessar](#4-acessar)
5. [Usuários de teste](#-usuários-de-teste)
6. [Comandos úteis do Docker](#-comandos-úteis-do-docker)
7. [Estrutura de pastas](#-estrutura-de-pastas)
8. [Testes automatizados](#-testes-automatizados)
9. [Troubleshooting](#-troubleshooting)
10. [Issues resolvidas](#-issues-resolvidas-nesta-entrega)
11. [Próximas entregas](#-próximas-entregas)

---

## 🎯 O que o projeto faz

O **Banco Digital** simula as operações fundamentais de um banco para fins acadêmicos. Cada usuário cadastrado recebe uma conta única e pode interagir com ela através de uma interface web.

### Fluxos disponíveis

| Fluxo | Descrição | Endpoint |
|---|---|---|
| 🔐 **Cadastro** | Cria um novo usuário + conta zerada (atomicamente). Senha armazenada como hash BCrypt. | `GET / POST /cadastro` |
| 🔑 **Login** | Autenticação via form (Spring Security + CSRF + session fixation protection). | `GET / POST /login` |
| 🏠 **Painel** | Página inicial com atalhos para todos os serviços. | `GET /painel` |
| 💰 **Saldo** | Consulta o saldo atual da conta. | `GET /saldo` |
| ⬇ **Depósito** | Credita um valor na conta (registra uma transação). | `GET / POST /deposito` |
| ⬆ **Saque** | Debita um valor (limite diário de R$ 10.000 por operação). | `GET / POST /saque` |
| ↔ **Transferência** | Movimenta saldo para outra conta pelo número (`C00001`, ...). Atômica com row-level locking. | `GET / POST /transferencia` |
| 📈 **Investimento** | Aplica/Resgata valores num investimento de **1 % de juros compostos por minuto** (lazy update). | `GET / POST /investimento` |
| 📜 **Extrato** | Lista todas as transações da conta com cor por tipo e formatação BR. | `GET /extrato` |
| 🚪 **Logout** | Encerra a sessão. | `POST /logout` |

### Regras de negócio embutidas

- 💵 Todos os valores monetários usam `BigDecimal` com `scale 2 HALF_UP` (sem erros de ponto flutuante).
- 🔒 Senhas hash BCrypt (strength 10).
- 🛡️ CSRF token automático em todos os forms (Spring Security).
- 🧾 Transações JDBC com `SELECT ... FOR UPDATE` para serializar acesso concorrente.
- 🆔 Números de conta gerados via `SEQUENCE` do Postgres (`C00001`, `C00002`, ... sem colisão).
- 🚫 Validações centralizadas em `com.bancodigital.shared.Mensagens` — mesma string em validação e UI.

---

## 🛠 Tecnologias

### Linguagem & Build

| Tecnologia | Versão | Propósito |
|---|---|---|
| ![Java](https://img.shields.io/badge/-Java-007396?logo=openjdk&logoColor=white) Java | **17 LTS** | Linguagem principal — records, sealed types, switch expressions |
| ![Maven](https://img.shields.io/badge/-Maven-C71A36?logo=apachemaven&logoColor=white) Maven | **3.9+** | Build, dependency management, plugin lifecycle |

### Backend

| Tecnologia | Versão | Propósito |
|---|---|---|
| ![Spring Boot](https://img.shields.io/badge/-Spring%20Boot-6DB33F?logo=springboot&logoColor=white) Spring Boot | **3.3.4** | Framework principal (auto-configuration, embedded Tomcat) |
| ![Spring Security](https://img.shields.io/badge/-Spring%20Security-6DB33F?logo=springsecurity&logoColor=white) Spring Security | **6.x** | Form login, BCrypt, CSRF, sessões |
| ![Tomcat](https://img.shields.io/badge/-Tomcat-F8DC75?logo=apachetomcat&logoColor=black) Tomcat (embedded) | **10.1** | Servlet container — empacotado no JAR |
| ![Spring JDBC](https://img.shields.io/badge/-Spring%20JDBC-6DB33F?logo=spring&logoColor=white) Spring JDBC | 3.3.x | `NamedParameterJdbcTemplate` para SQL controlado |
| ![HikariCP](https://img.shields.io/badge/-HikariCP-2C3E50) HikariCP | embutido | Pool de conexões (default do starter) |
| ![Spring Validation](https://img.shields.io/badge/-Validation-6DB33F?logo=spring&logoColor=white) Bean Validation | 3.3.x | Anotações para inputs HTTP |
| ![Actuator](https://img.shields.io/badge/-Actuator-6DB33F?logo=spring&logoColor=white) Actuator | 3.3.x | `/actuator/health` para healthcheck |

### Persistência

| Tecnologia | Versão | Propósito |
|---|---|---|
| ![PostgreSQL](https://img.shields.io/badge/-PostgreSQL-4169E1?logo=postgresql&logoColor=white) PostgreSQL | **16-alpine** | Banco relacional |
| ![Flyway](https://img.shields.io/badge/-Flyway-CC0200?logo=flyway&logoColor=white) Flyway | **10.x** | Migrations versionadas (`V1__`, `V2__`) aplicadas no boot |

### View

| Tecnologia | Versão | Propósito |
|---|---|---|
| ![Thymeleaf](https://img.shields.io/badge/-Thymeleaf-005F0F?logo=thymeleaf&logoColor=white) Thymeleaf | **3.x** | Template engine server-side (escape automático contra XSS) |
| Thymeleaf Spring Security | 6.x | Diretivas `sec:authorize` em templates |
| CSS3 puro | — | Estilização sem framework externo |

### Testes

| Tecnologia | Versão | Propósito |
|---|---|---|
| ![JUnit 5](https://img.shields.io/badge/-JUnit%205-25A162?logo=junit5&logoColor=white) JUnit | **5.10** | Framework de testes (Mockito removido por restrição acadêmica) |
| Spring Security Test | 6.x | Apoio a testes (futuro: testes Web com `@WithMockUser`) |

### Infra & DevOps

| Tecnologia | Versão | Propósito |
|---|---|---|
| ![Docker](https://img.shields.io/badge/-Docker-2496ED?logo=docker&logoColor=white) Docker | 20+ | Containerização |
| ![Docker Compose](https://img.shields.io/badge/-Compose-2496ED?logo=docker&logoColor=white) Docker Compose | v2 | Orquestração local (app + db + admin UI) |
| ![Adminer](https://img.shields.io/badge/-Adminer-34567C) Adminer | latest | UI web para inspecionar o Postgres (porta 8081) |
| ![Eclipse Temurin](https://img.shields.io/badge/-Temurin%20JRE-FF8C00) Eclipse Temurin | 17-jre | Runtime no container (multi-arch: arm64 + amd64) |

---

## 🏗 Arquitetura

```mermaid
flowchart LR
    Browser([🌐 Browser])
    Browser -->|HTTP| Filter[Spring Security<br/>Filter Chain]
    Filter --> Controller["@Controller<br/>Thymeleaf views"]
    Controller --> Service["@Service<br/>@Transactional"]
    Service --> Repository["@Repository<br/>NamedParameterJdbcTemplate"]
    Repository -->|JDBC + HikariCP| Postgres[(🐘 PostgreSQL 16)]

    style Browser fill:#ffd
    style Postgres fill:#cef
    style Service fill:#cfc
```

| Camada | Responsabilidade | Anotações Spring |
|---|---|---|
| **Controller** | HTTP (request → view), redirects, flash messages | `@Controller`, `@GetMapping`, `@PostMapping` |
| **Service** | Regra de negócio, validação, transações atômicas | `@Service`, `@Transactional` |
| **Repository** | SQL via `JdbcTemplate`, `RowMapper` | `@Repository`, interface + impl JDBC |

Detalhes em [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

---

## 🚀 Passo a passo para rodar

> **TL;DR** se você já tem Docker:
> ```bash
> git clone <url-do-repo>
> cd trabalho-qualidade-e-teste-ffc-grupo-de-guerreiros
> docker compose up -d --build
> # abre http://localhost:8080
> ```

Caso não tenha Docker ainda, siga abaixo. **Você não precisa instalar Java, Maven nem PostgreSQL** — tudo roda dentro de containers.

### 1. Instalar o Docker

#### 🍎 macOS (Intel ou Apple Silicon)

**Opção A — Docker Desktop** (recomendado, tem UI):

1. Baixe em <https://www.docker.com/products/docker-desktop/> (escolha "Apple Chip" se for M1/M2/M3/M4, ou "Intel chip" caso contrário).
2. Abra o `.dmg`, arraste o ícone do Docker para `Applications`.
3. Abra o **Docker Desktop** pelo Launchpad. Aceite os termos, aguarde o ícone 🐳 aparecer na barra de menu e ficar **verde** (engine started).

**Opção B — Homebrew** (CLI puro):

```bash
brew install --cask docker
open -a Docker      # abre o Docker Desktop
```

**Verificar:**
```bash
docker --version            # Docker version 24.x ou superior
docker compose version      # Docker Compose version v2.x
docker info                 # tem que conectar sem erro
```

---

#### 🐧 Linux — Ubuntu / Debian

```bash
# 1. Adicionar repositório oficial
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | \
    sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
    https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
    sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 2. Instalar engine + compose plugin
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 3. Rodar sem sudo (opcional, recomendado)
sudo usermod -aG docker $USER
newgrp docker

# 4. Verificar
docker --version
docker compose version
```

Detalhes oficiais: <https://docs.docker.com/engine/install/ubuntu/>

---

#### 🎩 Linux — Fedora / RHEL / CentOS Stream

```bash
sudo dnf -y install dnf-plugins-core
sudo dnf config-manager --add-repo https://download.docker.com/linux/fedora/docker-ce.repo
sudo dnf install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo systemctl enable --now docker
sudo usermod -aG docker $USER
newgrp docker
docker --version
```

---

#### 🪟 Windows (via WSL2)

1. Habilite o **WSL2**: abra PowerShell como Admin e rode `wsl --install`. Reinicie.
2. Baixe e instale o **Docker Desktop**: <https://www.docker.com/products/docker-desktop/>.
3. Abra o Docker Desktop → **Settings** → **General** → marque "Use the WSL 2 based engine".
4. Abra um terminal WSL (Ubuntu por exemplo) e rode `docker --version`.

---

### 2. Clonar o repositório

```bash
git clone <url-do-repo>
cd trabalho-qualidade-e-teste-ffc-grupo-de-guerreiros
```

### 3. Subir a aplicação

```bash
docker compose up -d --build
```

**O que acontece** (1ª execução leva ~3-5 min, depois ~10s):

1. 🔨 Docker baixa as imagens base (`maven`, `eclipse-temurin`, `postgres:16-alpine`, `adminer`).
2. 🏗 Builda a imagem da app dentro do container (Maven baixa deps, compila, empacota o JAR).
3. 🐘 Sobe o **Postgres** e espera o `pg_isready` ficar verde.
4. 📜 A app sobe e o **Flyway aplica `V1__init_schema.sql` + `V2__seed_data.sql`** (cria tabelas + insere 5 usuários de teste).
5. 🟢 Spring Boot fica disponível em ~1.5 s após o boot.
6. 🔍 Adminer fica disponível em paralelo.

Acompanhe os logs:

```bash
docker compose logs -f app          # logs do Spring Boot (Ctrl+C para sair)
docker compose ps                   # status dos 3 containers
```

Quando os 3 estiverem `Up (healthy)`, está pronto.

### 4. Acessar

| Serviço | URL | Credenciais |
|---|---|---|
| 🏦 **Aplicação** | <http://localhost:8080> | `joao@email.com` / `senha123` (ou cadastre um novo) |
| 🗄 **Adminer** (UI do banco) | <http://localhost:8081> | Sistema: `PostgreSQL` · Servidor: `postgres` · Usuário: `bancodigital` · Senha: `bancodigital` · Base: `bancodigital` |
| ❤️ **Health** | <http://localhost:8080/actuator/health> | público |
| 🐘 **PostgreSQL direto** | `localhost:5432` | `bancodigital` / `bancodigital` |

---

## 👥 Usuários de teste

Os seeds (em `V2__seed_data.sql`) criam 5 usuários, **todos com a senha `senha123`** (já hashed com BCrypt no banco):

| E-mail | Conta | Saldo inicial | Histórico |
|---|---|---|---|
| `joao@email.com` | `C00001` | R$ 1.500,00 | 1 depósito + 1 saque + 1 transferência recebida |
| `maria@email.com` | `C00002` | R$ 9.999,99 | 1 depósito + investimento ativo de R$ 500 |
| `pedro@email.com` | `C00003` | R$ 0,00 | conta nova, sem histórico — útil para testar saldo insuficiente |
| `ana@email.com` | `C00004` | R$ 25.000,00 | depósito + saque + 2 transferências enviadas + investimento de R$ 1.500 |
| `carlos@email.com` | `C00005` | R$ 100,00 | depósito + transferência recebida |

> 💡 **Resetar tudo aos seeds**: `docker compose down -v && docker compose up -d` (apaga o volume `pgdata`, Flyway re-aplica V1 + V2 do zero).

---

## ⚙️ Comandos úteis do Docker

```bash
# Subir / derrubar
docker compose up -d --build         # builda imagem e sobe os 3 serviços em background
docker compose stop                  # pausa os containers (estado preservado)
docker compose start                 # retoma
docker compose down                  # derruba e remove containers (mantém volume → dados preservados)
docker compose down -v               # derruba e APAGA o volume (perde todos os dados!)

# Observar
docker compose ps                    # status dos 3 containers
docker compose logs -f app           # tail dos logs da app
docker compose logs postgres         # logs do Postgres
docker compose top                   # processos rodando

# Inspecionar o banco
docker compose exec postgres psql -U bancodigital -d bancodigital
# dentro do psql:
#   \dt                              -- listar tabelas
#   SELECT * FROM usuario;
#   SELECT * FROM conta;
#   \q                               -- sair

# Rebuildar só a app (após mudar código)
docker compose up -d --build app

# Limpar tudo (containers, imagens, volumes, networks)
docker compose down -v --rmi all --remove-orphans
```

---

## 📂 Estrutura de pastas

```
.
├── docker-compose.yml              # 3 serviços: postgres + app + adminer
├── Dockerfile                      # multi-stage: maven build → temurin jre
├── .env.example                    # template de variáveis (POSTGRES_*)
├── .editorconfig                   # consistência de indentação entre IDEs
├── pom.xml                         # Spring Boot 3.3.4 + Java 17
│
├── src/
│   ├── main/
│   │   ├── java/com/bancodigital/
│   │   │   ├── BancodigitalApplication.java
│   │   │   ├── config/             # SecurityConfig, AppConfig, HomeController
│   │   │   ├── shared/             # Mensagens, Money, DomainException, GlobalExceptionHandler
│   │   │   ├── login/              # Usuario, UsuarioRepository (interface + Jdbc), CustomUserDetailsService
│   │   │   ├── cadastro/           # CadastroForm, CadastroService, CadastroController
│   │   │   ├── conta/              # Conta, ContaRepository, ContaService + 4 controllers
│   │   │   ├── transacao/          # TipoTransacao, Transacao, TransacaoRepository, ExtratoLinha, ExtratoController
│   │   │   └── investimento/       # Investimento, InvestimentoRepository, InvestimentoService, controller
│   │   └── resources/
│   │       ├── application.yml             # configs default (datasource via env vars)
│   │       ├── application-docker.yml      # overrides quando SPRING_PROFILES_ACTIVE=docker
│   │       ├── db/migration/
│   │       │   ├── V1__init_schema.sql     # tabelas, índices, sequences, constraints
│   │       │   └── V2__seed_data.sql       # 5 usuários, 5 contas, 10 transações
│   │       ├── static/css/style.css        # estilos compartilhados
│   │       └── templates/                  # 10 templates Thymeleaf
│   │           ├── fragments/layout.html   # topbar + alertas (reuso)
│   │           ├── login.html, cadastro.html, painel.html
│   │           ├── saldo.html, saque.html, deposito.html
│   │           ├── transferencia.html, extrato.html
│   │           └── investimento.html
│   └── test/java/com/bancodigital/         # 89 testes unitários puros JUnit 5
│
└── docs/
    └── ARCHITECTURE.md             # diagrama + decisões de design
```

---

## 🧪 Testes automatizados

```bash
mvn test                            # roda dentro do container Maven (não precisa Java local)
# ou, dentro do container app:
docker compose exec app sh -c "echo 'use mvn no host ou containerize'"
```

> Para rodar `mvn` no host, instale o JDK 17+ (`brew install openjdk@21` no Mac, ou `apt install openjdk-17-jdk` no Ubuntu) e o Maven (`brew install maven` / `apt install maven`).

### Cobertura atual: **89 testes unitários puros**, todos JUnit 5 sem dependências externas

| Suite | Testes | Componente coberto |
|---|---|---|
| `MoneyTest` | 16 | helpers de `BigDecimal` (parse, normalize, isPositive, format) |
| `TipoTransacaoTest` | 9 | enum + `fromDbValue` |
| `ExtratoLinhaTest` | 15 | factory `de()`, `corPara`, `descricaoPara` |
| `CadastroServiceTest` | 12 | `validarCadastro` (regex e-mail, length de senha, edge cases) |
| `ContaServiceTest` | 23 | `validaSaque` / `validarDeposito` / `validarTransferencia` |
| `InvestimentoServiceTest` | 14 | `calcularValorComJuros` (juros compostos), `validarOperacao` |

> Testes que exigem isolamento de dependências via mocks/fakes e testes de integração com Postgres real ficam para a **próxima entrega**.

---

## 🛟 Troubleshooting

<details>
<summary><strong>"Cannot connect to the Docker daemon"</strong></summary>

O Docker Desktop não está rodando.
- **macOS**: abra o app pelo Launchpad e aguarde o ícone 🐳 ficar verde na barra de menu.
- **Linux**: `sudo systemctl start docker` (e `sudo systemctl enable docker` para iniciar no boot).

</details>

<details>
<summary><strong>Porta 8080, 8081 ou 5432 já em uso</strong></summary>

Alguma outra aplicação está ocupando a porta. Descubra qual:

```bash
lsof -i :8080        # macOS / Linux
```

Mate o processo, ou edite o `docker-compose.yml` para mapear outras portas (ex.: `"8090:8080"` em vez de `"8080:8080"`).

</details>

<details>
<summary><strong>App container reinicia em loop</strong></summary>

Veja o motivo:
```bash
docker compose logs app | tail -50
```

Erros comuns:
- **Flyway checksum mismatch**: você editou um arquivo `V*__*.sql` depois de já ter sido aplicado. Solução em dev: `docker compose down -v && docker compose up -d` (recomeça do zero).
- **Connection refused para postgres**: o Postgres ainda está subindo. O `depends_on: condition: service_healthy` resolve, mas se a primeira subida falhar tente `docker compose restart app`.

</details>

<details>
<summary><strong>"Esqueci a senha" / quero resetar os seeds</strong></summary>

```bash
docker compose down -v
docker compose up -d
```

Tudo volta ao estado dos seeds (`joao@email.com` / `senha123`, etc).

</details>

<details>
<summary><strong>Erros na IDE ("record cannot be resolved", "Syntax error on token")</strong></summary>

Sua IDE não está configurada com JDK 17+. Mesmo que o build do Maven funcione (porque o Maven usa o JDK que ele encontrar), o Eclipse JDT Language Server (usado por VSCode/IntelliJ/Eclipse) precisa ser apontado pra um JDK ≥ 17.

**VSCode** (mais comum):
1. Instale o JDK 17 ou superior (`brew install openjdk@21` no Mac).
2. Crie `.vscode/settings.json`:
   ```json
   {
     "java.configuration.runtimes": [
       { "name": "JavaSE-17", "path": "/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home" }
     ]
   }
   ```
   (ajuste o `path` conforme o JDK instalado — use `/usr/libexec/java_home -v 21` no Mac para descobrir).
3. `Cmd+Shift+P` → **Java: Clean Java Language Server Workspace** → "Restart and delete".

**IntelliJ**: `File → Project Structure → Project SDK → JDK 17+` e depois `Maven → Reload All Projects`.

</details>

---

## ✅ Issues resolvidas nesta entrega

Esta PR fecha as seguintes issues do GitHub:

| # | Título | Onde foi resolvido |
|---|---|---|
| **#4** | Migrar Derby → PostgreSQL com Docker Compose | [`docker-compose.yml`](docker-compose.yml), [`V1__init_schema.sql`](src/main/resources/db/migration/V1__init_schema.sql), [`application.yml`](src/main/resources/application.yml) |
| **#12** | Senha em texto plano no banco | `BCryptPasswordEncoder` em `SecurityConfig`, hashes no seed |
| **#13** | Cadastro pode deixar usuário órfão sem conta | [`CadastroService.cadastrar()`](src/main/java/com/bancodigital/cadastro/CadastroService.java) com `@Transactional` + FK `usuario_id NOT NULL` |
| **#14** | Cadastro pode gerar números de conta duplicados | `UNIQUE(numero)` + `CREATE SEQUENCE conta_numero_seq` (sem `Math.random`) |
| **#15** | `lazyUpdate` de investimento pode duplicar em concorrência | `UNIQUE(usuario_id)` em `investimento` + `INSERT ... ON CONFLICT DO NOTHING` no `ensureExists` |
| **#16** | Mensagem 'valor inválido' inconsistente | [`Mensagens.java`](src/main/java/com/bancodigital/shared/Mensagens.java) centraliza todas as strings |

---

## 🚧 Próximas entregas

Ficam para a **Entrega 2**:

- 🧪 Aumentar cobertura unitária isolando dependências (com **mocks/fakes**).
- 🧫 Testes de integração com **Testcontainers + PostgreSQL real** (Parte B das issues #6, #7, #8, #9, #10).
- 🔄 Pipeline CI/CD via GitHub Actions.

---

## 📚 Documentação adicional

- 🏗 [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — diagramas, decisões de design, mapeamento de camadas.

---

<div align="center">
<sub>Trabalho acadêmico de Qualidade e Teste de Software · Grupo de Guerreiros 🛡</sub>
</div>
