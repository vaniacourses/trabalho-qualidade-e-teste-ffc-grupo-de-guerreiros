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
2. [Documentação](#-documentação)

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

## 📚 Documentação

| Documento | Conteúdo |
|---|---|
| [docs/SETUP.md](docs/SETUP.md) | Como rodar o projeto, usuários de teste, comandos úteis e troubleshooting |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Diagrama de fluxo, camadas, schema do banco, decisões de design e segurança |
| [docs/RESPONSABILIDADES.md](docs/RESPONSABILIDADES.md) | Membros do grupo, contribuições da Entrega 1 e distribuição da Entrega 2 |

---

<div align="center">
<sub>Trabalho acadêmico de Qualidade e Teste de Software · Grupo de Guerreiros 🛡</sub>
</div>
