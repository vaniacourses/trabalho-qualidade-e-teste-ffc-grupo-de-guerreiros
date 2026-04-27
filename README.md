# Sistema Bancário — Java/Jakarta EE

Aplicação web acadêmica que simula operações básicas de um sistema bancário (transferência, saque, investimento, depósito e login), construída com Servlets e JSP sobre Jakarta EE 10.

![Java](https://img.shields.io/badge/Java-11-007396?logo=openjdk&logoColor=white)
![Jakarta EE](https://img.shields.io/badge/Jakarta%20EE-10-FF6F00?logo=eclipse&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.8%2B-C71A36?logo=apachemaven&logoColor=white)
![JUnit 5](https://img.shields.io/badge/JUnit-5.10.0-25A162?logo=junit5&logoColor=white)
![Apache Derby](https://img.shields.io/badge/Apache%20Derby-10.16%2B-D22128?logo=apache&logoColor=white)
![Glassfish](https://img.shields.io/badge/Glassfish-7-F89A2D?logo=eclipseglassfish&logoColor=white)
![Tomcat](https://img.shields.io/badge/Tomcat-10.1%2B-F8DC75?logo=apachetomcat&logoColor=black)

## Sobre o projeto

O projeto é um trabalho acadêmico das disciplinas de Qualidade e Teste de Software. A aplicação expõe um conjunto de telas JSP que se comunicam com Servlets responsáveis por orquestrar a lógica de negócio e o acesso ao banco de dados Apache Derby. O foco do trabalho é exercitar testes unitários, refatorações e práticas de qualidade sobre uma base de código existente.

A aplicação é organizada em cinco domínios funcionais:

- **Transferência** — movimentação de saldo entre contas.
- **Saque** — retirada de valores da conta.
- **Investimento** — aplicação de valores em produtos de investimento.
- **Depósito** — entrada de valores na conta.
- **Login** — autenticação do usuário e controle de sessão.

## Tecnologias

- ![Java](https://img.shields.io/badge/Java-11-007396?logo=openjdk&logoColor=white) Linguagem principal.
- ![Jakarta EE](https://img.shields.io/badge/Jakarta%20EE-10-FF6F00?logo=eclipse&logoColor=white) Plataforma para Servlets e JSP.
- ![Maven](https://img.shields.io/badge/Maven-3.8%2B-C71A36?logo=apachemaven&logoColor=white) Build e gerenciamento de dependências.
- ![JUnit 5](https://img.shields.io/badge/JUnit-5.10.0-25A162?logo=junit5&logoColor=white) Framework de testes unitários.
- ![Apache Derby](https://img.shields.io/badge/Apache%20Derby-10.16%2B-D22128?logo=apache&logoColor=white) Banco de dados relacional embarcado/em rede.
- ![Glassfish](https://img.shields.io/badge/Glassfish-7-F89A2D?logo=eclipseglassfish&logoColor=white) / ![Tomcat](https://img.shields.io/badge/Tomcat-10.1%2B-F8DC75?logo=apachetomcat&logoColor=black) Servidor de aplicação.

## Estrutura do repositório

```
.
├── docs/
│   └── ARCHITECTURE.md
├── .github/
│   ├── ISSUE_TEMPLATE/
│   │   ├── bug_report.yml
│   │   └── refactor.yml
│   └── pull_request_template.md
├── src/
│   ├── main/
│   │   ├── java/        # Servlets, DAOs e configuração
│   │   ├── resources/   # db.properties.example
│   │   └── webapp/      # JSPs e WEB-INF
│   └── test/
│       └── java/        # Testes JUnit 5
├── pom.xml
└── README.md
```

## Pré-requisitos

- JDK 11 ou superior.
- Maven 3.8 ou superior.
- Glassfish 7 ou Tomcat 10.1+.
- Apache Derby 10.16 ou superior.

## Setup

1. Clone o repositório:
   ```bash
   git clone <url-do-repositorio>
   cd trabalho-qualidade-e-teste-ffc-grupo-de-guerreiros
   ```
2. Suba o servidor de rede do Derby:
   ```bash
   startNetworkServer
   ```
3. Crie o banco de dados `trabalho` (via `ij` ou cliente equivalente):
   ```sql
   CONNECT 'jdbc:derby://localhost:1527/trabalho;create=true';
   ```
4. Copie o arquivo de configuração de exemplo e ajuste credenciais:
   ```bash
   cp src/main/resources/db.properties.example src/main/resources/db.properties
   ```
5. Compile e empacote o projeto:
   ```bash
   mvn clean package
   ```
6. Faça o deploy do `.war` gerado em `target/` no Glassfish 7 ou Tomcat 10.1+.
7. Acesse a aplicação em:
   ```
   http://localhost:8080/a/login.jsp
   ```

## Como rodar os testes

```bash
mvn test
```

Todos os 5 domínios são cobertos por testes unitários JUnit 5.

## Documentação adicional

- [Arquitetura do sistema](docs/ARCHITECTURE.md)

---

[![Open in Visual Studio Code](https://classroom.github.com/assets/open-in-vscode-2e0aaae1b6195c2367325f4f02e2d04e9abb55f0b24a779b69b11b9e10269abc.svg)](https://classroom.github.com/online_ide?assignment_repo_id=23630807&assignment_repo_type=AssignmentRepo)
