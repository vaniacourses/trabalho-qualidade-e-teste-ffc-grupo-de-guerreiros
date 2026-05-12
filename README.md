<div align="center">

# 🏦 Banco Digital

Sistema bancário acadêmico full-stack, com autenticação segura, depósitos, saques, transferências, investimentos com juros compostos e extrato.
Construído com **Spring Boot 3 + Thymeleaf + PostgreSQL**, totalmente empacotado em **Docker Compose**: sobe em um comando.

[![Java](https://img.shields.io/badge/Java-17_LTS-007396?style=for-the-badge&logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://docs.docker.com/compose/)

[![Maven](https://img.shields.io/badge/Maven-3.9-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-6-6DB33F?logo=springsecurity&logoColor=white)](https://docs.spring.io/spring-security/)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3-005F0F?logo=thymeleaf&logoColor=white)](https://www.thymeleaf.org/)
[![Flyway](https://img.shields.io/badge/Flyway-10-CC0200?logo=flyway&logoColor=white)](https://flywaydb.org/)
[![JUnit 5](https://img.shields.io/badge/JUnit-5-25A162?logo=junit5&logoColor=white)](https://junit.org/junit5/)

</div>

---

## 📑 Sumário

1. [O que o projeto faz](#-o-que-o-projeto-faz)
2. [Quick start](#-quick-start)
3. [Usuários de teste](#-usuários-de-teste)
4. [Testes](#-testes)
5. [Documentação](#-documentação)

---

## 🎯 O que o projeto faz

O **Banco Digital** simula as operações fundamentais de um banco para fins acadêmicos. Cada usuário cadastrado recebe uma conta única e interage com ela através de uma interface web.

### Fluxos disponíveis

| Fluxo | Descrição | Endpoint |
|---|---|---|
| 🔐 **Cadastro** | Cria um novo usuário e conta zerada (atomicamente). Senha armazenada como hash BCrypt. | `GET / POST /signup` |
| 🔑 **Login** | Autenticação via form (Spring Security, CSRF, session fixation protection). | `GET / POST /login` |
| 🏠 **Painel** | Página inicial com atalhos para todos os serviços. | `GET /dashboard` |
| 💰 **Saldo** | Consulta o saldo atual da conta. | `GET /balance` |
| ⬇ **Depósito** | Credita um valor na conta (registra uma transação). | `GET / POST /deposit` |
| ⬆ **Saque** | Debita um valor (limite diário de R$ 10.000 por operação). | `GET / POST /withdraw` |
| ↔ **Transferência** | Movimenta saldo para outra conta pelo número (`C00001`, ...). Atômica, com row-level locking. | `GET / POST /transfer` |
| 📈 **Investimento** | Aplica e resgata valores num investimento de **1 % de juros compostos por minuto** (lazy update). | `GET / POST /investment` |
| 📜 **Extrato** | Lista todas as transações da conta com cor por tipo e formatação BR. | `GET /statement` |
| 🚪 **Logout** | Encerra a sessão. | `POST /logout` |

Stack completa, camadas, schema do banco e decisões de design em [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

---

## 🚀 Quick start

> Só precisa de **Docker** instalado. Java, Maven e Postgres rodam em containers.

```bash
git clone <url-do-repo>
cd trabalho-qualidade-e-teste-ffc-grupo-de-guerreiros
docker compose up -d --build
```

| Serviço | URL |
|---|---|
| 🏦 **Aplicação** | <http://localhost:8080> (login `joao@email.com` / `senha123`) |
| 🗄 **Adminer** | <http://localhost:8081> (server `postgres`, base/user/pass `bancodigital`) |

Guia completo de instalação (macOS/Linux/Windows, comandos úteis, troubleshooting) em [docs/SETUP.md](docs/SETUP.md).

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

## 🧪 Testes

```bash
mvn test
```

**89 testes unitários puros**, todos JUnit 5 sem dependências externas.

---

## 📚 Documentação

| Documento | Conteúdo |
|---|---|
| [docs/SETUP.md](docs/SETUP.md) | Instalação de Docker (macOS/Linux/Windows), comandos úteis, troubleshooting |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Diagrama de fluxo, camadas, schema do banco, decisões de design e segurança |
| [docs/RESPONSABILIDADES.md](docs/RESPONSABILIDADES.md) | Membros do grupo, contribuições da Entrega 1 e distribuição da Entrega 2 |

---

<div align="center">
<sub>Trabalho acadêmico de Qualidade e Teste de Software · Grupo de Guerreiros 🛡</sub>
</div>
