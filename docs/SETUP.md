# 🚀 Setup — Como rodar o Banco Digital

Guia completo para subir o projeto do zero, **mesmo que você nunca tenha usado Docker**. Funciona em **macOS, Linux e Windows (WSL2)**.

> ⏱ **Tempo total**: ~5 min na primeira vez (download de imagens + build), ~10 s nas seguintes.

---

## 📑 Sumário

1. [Pré-requisitos](#-pré-requisitos)
2. [Instalar o Docker](#-1-instalar-o-docker)
   - [macOS](#-macos-intel-ou-apple-silicon)
   - [Ubuntu / Debian](#-linux--ubuntu--debian)
   - [Fedora / RHEL / CentOS](#-linux--fedora--rhel--centos-stream)
   - [Windows (WSL2)](#-windows-via-wsl2)
3. [Clonar o repositório](#-2-clonar-o-repositório)
4. [Subir a aplicação](#-3-subir-a-aplicação)
5. [Acessar](#-4-acessar)
6. [Usuários de teste](#-usuários-de-teste)
7. [Comandos úteis](#-comandos-úteis)
8. [Resetar dados](#-resetar-dados)
9. [Troubleshooting](#-troubleshooting)

---

## 📋 Pré-requisitos

**Só Docker.** Você **não precisa** instalar nada disso no seu sistema:
- ❌ Java
- ❌ Maven
- ❌ PostgreSQL
- ❌ `psql` ou outros clients

Tudo roda dentro de containers. O Docker baixa o Java, o Maven e o Postgres conforme necessário.

---

## 🐳 1. Instalar o Docker

### 🍎 macOS (Intel ou Apple Silicon)

**Opção A — Docker Desktop** (recomendado, tem UI gráfica):

1. Baixe em <https://www.docker.com/products/docker-desktop/>. Escolha:
   - **Apple Chip** se for Mac M1/M2/M3/M4
   - **Intel Chip** caso contrário
2. Abra o `.dmg` e arraste o ícone do Docker para a pasta `Applications`.
3. Abra o **Docker Desktop** pelo Launchpad. Aceite os termos.
4. Aguarde o ícone 🐳 aparecer na barra de menu (canto superior direito da tela) e ficar **verde** — significa que o engine está pronto.

**Opção B — Homebrew** (linha de comando):

```bash
brew install --cask docker
open -a Docker          # abre o Docker Desktop
```

**Verificar a instalação:**

```bash
docker --version           # Docker version 24.x ou superior
docker compose version     # Docker Compose version v2.x
docker info                # deve conectar sem erro
```

---

### 🐧 Linux — Ubuntu / Debian

```bash
# 1. Adicionar o repositório oficial do Docker
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | \
    sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
    https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
    sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 2. Instalar Docker Engine + Compose plugin
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 3. (Opcional, recomendado) rodar sem sudo
sudo usermod -aG docker $USER
newgrp docker

# 4. Verificar
docker --version
docker compose version
```

Detalhes oficiais: <https://docs.docker.com/engine/install/ubuntu/>

---

### 🎩 Linux — Fedora / RHEL / CentOS Stream

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

### 🪟 Windows (via WSL2)

1. **Habilite o WSL2** — abra o PowerShell **como Administrador** e rode:
   ```powershell
   wsl --install
   ```
   Reinicie o computador.
2. Baixe e instale o **Docker Desktop**: <https://www.docker.com/products/docker-desktop/>.
3. No Docker Desktop: **Settings → General →** marque "Use the WSL 2 based engine".
4. Abra um terminal WSL (ex.: Ubuntu) e rode `docker --version` para confirmar.

> 💡 Daqui pra frente, rode os comandos do projeto dentro do **terminal WSL**, não no PowerShell.

---

## 📦 2. Clonar o repositório

Primeiro, saia da pasta protegida no WSL:

```bash
cd ~
```

Depois clone o repositório:

```bash
git clone <url-do-repo>
cd trabalho-qualidade-e-teste-ffc-grupo-de-guerreiros
```

> ⚠ Caso o GitHub solicite login e senha:
>
> O GitHub não aceita mais senha da conta para operações Git.
>
> Será necessário utilizar um **Personal Access Token (PAT)**.
>
> Caminho para gerar:
>
> `GitHub → Settings → Developer settings → Personal access tokens → Tokens classic → Generate new token`
>
> Permissão necessária:
>
> `repo` ou adicione todas
>
> Ao solicitar a senha durante o clone, utilize o token gerado.(Lembre-se de anotar o token gerado)

---

## ⬆ 3. Subir a aplicação

Antes de executar o Docker Compose, verifique se o Docker Desktop está aberto no Windows e com integração WSL habilitada.

Execute:

```bash
docker compose up -d --build
```

Caso apareça erro de permissão ao executar o Docker:

```bash
permission denied while trying to connect to the docker API
```
Execute:

```bash
sudo usermod -aG docker $USER
newgrp docker
```

Depois execute novamente:

```bash
docker compose up -d --build
```

**O que acontece**, em ordem:

1. 🔨 Docker baixa as imagens base: `maven:3.9.9-eclipse-temurin-17`, `eclipse-temurin:17-jre`, `postgres:16-alpine`, `adminer:latest`.
2. 🏗 Builda a imagem da aplicação dentro do container Maven (baixa dependências, compila, empacota o JAR com Spring Boot embedded Tomcat).
3. 🐘 Sobe o **Postgres** e aguarda `pg_isready` ficar verde (~5 s).
4. 📜 A app sobe e o **Flyway aplica automaticamente** `V1__init_schema.sql` + `V2__seed_data.sql` — cria as tabelas e insere 5 usuários de teste.
5. 🟢 Spring Boot fica pronto em ~1,5 s após o boot.
6. 🔍 Adminer sobe em paralelo.

**Acompanhar:**

```bash
docker compose logs -f app          # logs do Spring Boot ao vivo (Ctrl+C para sair)
docker compose ps                   # status dos 3 containers
```

Quando os 3 estiverem `Up (healthy)`, pode usar.

---

## 🌐 4. Acessar

| Serviço | URL | Credenciais |
|---|---|---|
| 🏦 **Aplicação** | <http://localhost:8080> | `joao@email.com` / `senha123` |
| 🗄 **Adminer** (UI do banco) | <http://localhost:8081> | Sistema: `PostgreSQL` · Servidor: `postgres` · User: `bancodigital` · Senha: `bancodigital` · Base: `bancodigital` |
| ❤️ **Health check** | <http://localhost:8080/actuator/health> | público |
| 🐘 **PostgreSQL direto** | `localhost:5432` | `bancodigital` / `bancodigital` |

Veja os [usuários de teste](#-usuários-de-teste) abaixo para outras credenciais.

---

## 👥 Usuários de teste

Os seeds (em `V2__seed_data.sql`) criam 5 usuários, **todos com a senha `senha123`** (já hashed com BCrypt no banco):

| E-mail | Conta | Saldo inicial | Histórico |
|---|---|---|---|
| `joao@email.com` | `C00001` | R$ 1.500,00 | 1 depósito, 1 saque, 1 transferência recebida |
| `maria@email.com` | `C00002` | R$ 9.999,99 | 1 depósito, investimento ativo de R$ 500 |
| `pedro@email.com` | `C00003` | R$ 0,00 | conta nova, sem histórico (útil para testar saldo insuficiente) |
| `ana@email.com` | `C00004` | R$ 25.000,00 | depósito, saque, 2 transferências enviadas, investimento de R$ 1.500 |
| `carlos@email.com` | `C00005` | R$ 100,00 | depósito, transferência recebida |

> 💡 **Resetar tudo aos seeds**: `docker compose down -v && docker compose up -d` (apaga o volume `pgdata`, Flyway re-aplica V1 e V2 do zero).

---

## ⚙️ Comandos úteis

```bash
# ▶️ Subir / derrubar
docker compose up -d --build         # builda imagem e sobe em background
docker compose stop                  # pausa (estado preservado)
docker compose start                 # retoma
docker compose down                  # remove containers (mantém o volume → dados preservados)
docker compose down -v               # remove containers E apaga volume (perde dados!)

# 🔍 Observar
docker compose ps                    # status dos 3 containers
docker compose logs -f app           # tail dos logs da app
docker compose logs postgres         # logs do Postgres
docker compose top                   # processos rodando

# 🐘 Inspecionar o banco via CLI
docker compose exec postgres psql -U bancodigital -d bancodigital
# dentro do psql:
#   \dt                              -- listar tabelas
#   SELECT * FROM users;
#   SELECT id, number, balance FROM accounts;
#   \q                               -- sair

# 🔄 Rebuildar só a app (após mudar código Java/templates)
docker compose up -d --build app

# 🧹 Limpar tudo (containers, imagens, volumes, networks)
docker compose down -v --rmi all --remove-orphans
```

---

## 🔄 Resetar dados

Por padrão, os dados (saldos, transações, novos cadastros) **persistem** entre execuções porque ficam no volume Docker `bancodigital-pgdata`.

Para **voltar ao estado inicial dos seeds** (5 usuários, saldos originais, ~10 transações de exemplo):

```bash
docker compose down -v               # apaga o volume
docker compose up -d                 # sobe de novo (Flyway re-aplica V1 + V2)
```

Leva ~10 s.

> ⚠️ Não edite arquivos `V*__*.sql` que já foram aplicados — o Flyway armazena o checksum na tabela `flyway_schema_history` e vai falhar com `Migration checksum mismatch`. Para alterar dados existentes, crie uma **nova migration** (`V3__minha_alteracao.sql`).

---

## 🛟 Troubleshooting

<details>
<summary><strong>"Cannot connect to the Docker daemon"</strong></summary>

O Docker não está rodando.

- **macOS / Windows**: abra o **Docker Desktop** e aguarde o ícone 🐳 ficar verde.
- **Linux**: `sudo systemctl start docker` (e `sudo systemctl enable docker` para iniciar automaticamente no boot).

</details>

<details>
<summary><strong>Porta 8080, 8081 ou 5432 já em uso</strong></summary>

Outro processo está usando a porta. Descubra qual:

```bash
lsof -i :8080                 # macOS / Linux
# Windows (PowerShell):
Get-NetTCPConnection -LocalPort 8080
```

Mate o processo, **ou** edite `docker-compose.yml` para mapear outra porta. Exemplo:

```yaml
ports:
  - "8090:8080"               # acessa em http://localhost:8090
```

</details>

<details>
<summary><strong>O container `app` reinicia em loop</strong></summary>

Veja o motivo:

```bash
docker compose logs app | tail -50
```

Erros comuns:

- **`Migration checksum mismatch`** — você editou um `V*__*.sql` já aplicado. Em dev: `docker compose down -v && docker compose up -d`.
- **`Connection refused` para postgres** — o Postgres ainda subindo. O `depends_on: condition: service_healthy` cobre isso, mas se o primeiro boot falhar tente `docker compose restart app`.
- **`Address already in use`** — porta 8080 ocupada (veja o item anterior).

</details>

<details>
<summary><strong>Build muito lento na 1ª vez</strong></summary>

Normal — o Docker precisa baixar:

- `maven:3.9.9-eclipse-temurin-17` (~700 MB)
- `eclipse-temurin:17-jre` (~250 MB)
- `postgres:16-alpine` (~80 MB)
- `adminer:latest` (~50 MB)
- Dependências Maven do Spring Boot (~150 MB)

São ~1,2 GB de download na primeira execução. As próximas usam o cache local e levam ~10 s.

</details>

<details>
<summary><strong>"Esqueci a senha"</strong></summary>

Os 5 usuários de seed usam **`senha123`**. Se você criou um novo usuário e esqueceu a senha, ou prefere começar do zero:

```bash
docker compose down -v && docker compose up -d
```

Tudo volta ao estado dos seeds.

</details>

<details>
<summary><strong>Erros na IDE: "record cannot be resolved", "Syntax error on token"</strong></summary>

Sua IDE não está configurada com JDK 17+. Mesmo que o `docker compose` funcione (o JDK fica dentro do container), o Language Server da IDE precisa de um JDK local ≥ 17.

**VSCode** (mais comum):

1. Instale o JDK 17 ou superior:
   ```bash
   # macOS
   brew install openjdk@21

   # Ubuntu / Debian
   sudo apt install openjdk-17-jdk
   ```
2. Crie `.vscode/settings.json` no projeto:
   ```json
   {
     "java.configuration.runtimes": [
       { "name": "JavaSE-17", "path": "/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home" }
     ]
   }
   ```
   Ajuste o `path`. No Mac: `/usr/libexec/java_home -v 21` mostra o caminho.
3. `Cmd+Shift+P` → **Java: Clean Java Language Server Workspace** → "Restart and delete".
4. Aguarde a re-importação (~30 s).

**IntelliJ**: `File → Project Structure → Project SDK → JDK 17+` e depois `Maven → Reload All Projects`.

</details>

<details>
<summary><strong>No Mac Apple Silicon: erro "no match for platform in manifest"</strong></summary>

Imagem Docker antiga sem suporte a ARM. A versão atual do `Dockerfile` usa `eclipse-temurin:17-jre` (multi-arch), então isso não deve acontecer. Se ainda assim ocorrer:

```bash
docker compose build --pull          # força rebaixar a imagem base
docker compose up -d
```

</details>

---

## 🛑 Derrubar o ambiente

Quando terminar de usar:

```bash
docker compose down              # remove containers (dados preservados)
docker compose down -v           # remove containers + apaga dados
```

---

⬅ Voltar para o [README principal](../README.md).
