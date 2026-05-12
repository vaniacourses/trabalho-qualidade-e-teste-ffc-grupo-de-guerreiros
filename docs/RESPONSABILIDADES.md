# Responsabilidades do grupo

> **Parte G da Entrega 2.** Registra **quem é responsável pelo quê**, com evidência do que cada membro já entregou (via histórico do Git na branch `main`) e a distribuição combinada para as 6 partes técnicas da Entrega 2 (A–F).
>
> Embora exigido pela Entrega 2, o documento é **persistente** — continua válido após a entrega, e deve ser atualizado sempre que houver mudança de escopo. Cada membro tem direito (e dever) de revisar a tabela que descreve seu próprio trabalho.

---

## 1. Membros do grupo

> Identificação extraída do `git shortlog main` (excluindo bots). Ordenado por volume de commits.

| Membro | Identificação no Git | Commits na `main` |
|---|---|---|
| **Yuri Coutinho** | `Yuri <1yuricoutinho@gmail.com>` | 19 |
| **Erivelton Campos** | `Erivelton Campos <erivelton.de.campos@fazenda.rj.gov.br>` | 7 |
| **Gleytton** | `Gleytton <gleytton@gmail.com>` | 4 |
| **Gabriel Ferraz** | `ferraz-gab <gabrielfe87.gf@gmail.com>` | 4 |
| **João Mainoth** | `João Mainoth <jvmainoth@hotmail.com>` / `<93680672+JvMainoth@users.noreply.github.com>` | 2 |

Para conferir a qualquer momento:

```bash
git shortlog -sne main
git log main --author="<nome>" --oneline
```

---

## 2. Contribuição na Entrega 1 (evidência em commits)

A coluna "Evidência" lista hashes reais — qualquer commit pode ser auditado com `git show <hash>`.

### 2.1 Yuri Coutinho

| Área | Entrega | Evidência (commits) |
|---|---|---|
| Documentação e infraestrutura | README profissional, ARCHITECTURE, templates de issue/PR | `9cd4118`, `9917a9e`, `80d04fb` |
| Segurança e configuração | Externalização de credenciais, `.gitignore` + `db.properties.example` | `c41e552`, `9a07738`, `7ed44b2` |
| Domínio **Investimento** | Refactor para testabilidade + testes de juros e operação | `8f1cb48`, `63454c6`, `c5a9bb6`, `35beda0`, `2629eff` |
| Domínio **Cadastro** | Validação de e-mail e senha mínima + testes + erros no form | `380bbb7`, `3df06a9`, `4570ee9`, `3eaeb38`, `1dcba07`, `fa3bc86` |
| Domínio **Extrato** | Refactor + cobertura de mapeamento de tipo | `57fe53e`, `7c39eb0` |
| Padronização do código | Rename para PascalCase, deposito test no diretório padrão | `9a07738`, `380bbb7` |

Total: **19 commits** entre 2026-04-27.

### 2.2 Erivelton Campos

| Área | Entrega | Evidência (commits) |
|---|---|---|
| Bootstrap do projeto | Initial commit + JUnit no `pom.xml` | `47b7e32`, `2c90675` |
| Domínio **Transferência** | Isolamento da lógica matemática do JavaDB + 4 cenários de teste | `0030594`, `5ffdf58`, `7cc626e`, `7d2faf4` |
| Manutenção | Correção de importação | `e1d5cd9` |

Cenários de transferência cobertos: **sucesso**, **saldo insuficiente**, **transferir para mesma conta**, **conta inexistente**, **valores negativos**.

Total: **7 commits** entre 2026-04-19 e 2026-04-23.

### 2.3 Gleytton

| Área | Entrega | Evidência (commits) |
|---|---|---|
| Domínio **Depósito** | Criação e iteração do `DepositoTest` + resolução de conflito de merge | `50b5e29`, `720490e`, `bdb6a49`, `a5ca1e7` |

Total: **4 commits** entre 2026-04-23 e 2026-04-27.

### 2.4 Gabriel Ferraz

| Área | Entrega | Evidência (commits) |
|---|---|---|
| Domínio **Saque** | Aumento de complexidade da classe + criação dos testes + correções e ajuste de validação | `8989adf`, `250fa6d`, `e81d225` |
| Manutenção | Merge da `main` na branch própria | `28b5190` |

Total: **4 commits** entre 2026-04-23 e 2026-04-24.

### 2.5 João Mainoth

| Área | Entrega | Evidência (commits) |
|---|---|---|
| Domínio **Login** | Isolamento da classe Login + testes de autenticação | `5f086f3` |
| Integração | Merge do PR #1 (`vaniacourses/Mainoth`) na main | `d4cd8dc` |

Total: **2 commits** em 2026-04-25.

---

## 3. Responsabilidades para a Entrega 2

A Entrega 2 está dividida em 5 partes (A–E) — ver planos em `docs/`. A distribuição respeita o **domínio que cada membro já dominou na Entrega 1** sempre que possível, e equilibra as partes que pedem **"1 classe não-CRUD por membro"** (parte D e parte E) entre as 5 classes-alvo selecionadas em [`TECNICAS_TESTE.md §4.2`](TECNICAS_TESTE.md#42-candidatas-ordenadas-por-complexidade).

### 3.1 Classes-alvo por membro (parte D + parte E)

| Membro | Classe-alvo | Por que essa | Documentos |
|---|---|---|---|
| **Yuri Coutinho** | [`InvestmentService`](../src/main/java/com/bancodigital/investment/InvestmentService.java) | Refatorou a classe original e escreveu seus testes na Entrega 1 (`8f1cb48`, `63454c6`) — continuidade natural | [D](TECNICAS_TESTE.md#52-investmentservice) · [E](INSPECAO_CODIGO.md#82-investmentservice) |
| **Erivelton Campos** | [`AccountService`](../src/main/java/com/bancodigital/account/AccountService.java) | Implementou a transferência e seus 4 testes na Entrega 1 — é o dono do domínio | [D](TECNICAS_TESTE.md#51-accountservice) · [E](INSPECAO_CODIGO.md#81-accountservice) |
| **João Mainoth** | [`SignupService`](../src/main/java/com/bancodigital/signup/SignupService.java) | Trabalhou na classe Login (`5f086f3`); cadastro é o par natural do fluxo de autenticação | [D](TECNICAS_TESTE.md#53-signupservice) · [E](INSPECAO_CODIGO.md#83-signupservice) |
| **Gabriel Ferraz** | [`Money`](../src/main/java/com/bancodigital/shared/money/Money.java) | O Saque (que ele cobriu na Entrega 1) é o maior consumidor de `Money.normalize/parseOrNull` — conhece bem o comportamento esperado | [D](TECNICAS_TESTE.md#54-money) · [E](INSPECAO_CODIGO.md#84-money) |
| **Gleytton** | [`StatementLine`](../src/main/java/com/bancodigital/transaction/StatementLine.java) | Cada depósito (sua área) gera 1 linha de extrato — entender a renderização do extrato fecha o ciclo | [D](TECNICAS_TESTE.md#55-statementline) · [E](INSPECAO_CODIGO.md#85-statementline) |

### 3.2 Responsabilidade nas partes A, B, C

| Parte | Tarefa | Responsável principal | Co-responsáveis |
|---|---|---|---|
| **A — Testes unitários com fakes** ([TESTES_UNITARIOS.md](TESTES_UNITARIOS.md)) | Cada um implementa os fakes e testes da **sua** classe (3.1) | *cada membro pela sua classe* | Yuri revisa convenção dos fakes |
| **B — Testes de integração com Testcontainers** ([TESTES_INTEGRACAO.md](TESTES_INTEGRACAO.md)) | Suíte de integração da **sua** classe + repositório associado | *cada membro pela sua classe* | Yuri faz a classe-base `PostgresIntegrationTest` |
| **C — Avaliação ISO 25010** ([QUALIDADE_ISO25010.md](QUALIDADE_ISO25010.md)) | Documento único, consolidado | Yuri Coutinho | João Mainoth (revisão dos itens de Segurança) |
| **D — Técnicas de teste (funcional + estrutural + mutação)** ([TECNICAS_TESTE.md](TECNICAS_TESTE.md)) | 1 classe por membro com **≥ 80 % arestas** e **≥ 80 % mutação** | *cada membro pela sua classe* | Yuri configura JaCoCo e PIT no `pom.xml` |
| **E — Inspeção com Sonar** ([INSPECAO_CODIGO.md](INSPECAO_CODIGO.md)) | 1 classe por membro com prints antes/depois | *cada membro pela sua classe* | Yuri sobe o `docker-compose.sonar.yml` |

### 3.3 Responsabilidades transversais

| Responsabilidade | Quem | Detalhe |
|---|---|---|
| Manutenção do README e do `docs/` | Yuri Coutinho | Atualizar seção "Próximas entregas" quando cada parte fechar |
| Configuração de build / Maven (JaCoCo, PIT, Failsafe, Sonar plugin) | Yuri Coutinho | Mudanças no `pom.xml` passam por PR para revisão |
| Configuração de Docker (Postgres, Sonar, Adminer) | Yuri Coutinho | Já feito; manter |
| Code review (PRs) | **Todos** | Mínimo 1 aprovação além do autor antes de merge na `main` |
| Consolidação dos relatórios finais (PDFs, prints, slides) | A combinar | Definir na semana da entrega |

---

## 4. Convenções

### 4.1 Autoria nos commits

- **Cada membro commita com sua própria identidade Git** (nome + e-mail). Se um pair programming acontecer, usar `Co-authored-by: Nome <email>` no rodapé do commit — **nunca atribuir trabalho a quem não fez**.
- Identidade Git padrão para o repo: `git config user.name "Seu Nome"` + `git config user.email "seu@email.com"`.
- Estilo da mensagem: **conventional commits** em inglês, minúsculas, 1 frase no título (padrão já em uso na `main`).

### 4.2 Pull Requests

- Um PR por feature/correção. Sem PRs gigantes "tudo de uma vez".
- **Assignee** do PR = o autor; **Reviewer** = outro membro do grupo.
- Linkar a issue do GitHub quando houver (`Closes #N`).

### 4.3 Atualização deste documento

- Mudou responsabilidade? Editar a tabela da seção 3 **antes** de começar a tarefa, não depois.
- Mudou a composição do grupo? Editar a seção 1 e registrar o motivo na seção 5 (histórico).

### 4.4 O que NÃO atribuir errado

- **Nunca colocar `Co-authored-by: Claude`** ou similar em commits — viola a regra do `CLAUDE.md` local e mascara a autoria real.
- **Nunca usar conta compartilhada** para commitar pelo colega — quebra a rastreabilidade que sustenta este documento.

---

## 5. Histórico

| Data | Mudança |
|---|---|
| 2026-05-12 | Criação do documento; distribuição inicial da Entrega 2 |

---

## 6. Como auditar este documento

Tudo nele é verificável com comandos `git`:

```bash
# Quem fez o quê (por autor)
git log main --author="Yuri" --oneline
git log main --author="Erivelton" --oneline
git log main --author="Gleytton" --oneline
git log main --author="ferraz" --oneline
git log main --author="Mainoth" --oneline

# Volume por contribuidor
git shortlog -sne main

# Quem mexeu em uma classe específica
git log --follow src/main/java/com/bancodigital/investment/InvestmentService.java

# Linhas por autor em um arquivo
git blame src/main/java/com/bancodigital/account/AccountService.java
```

Se houver divergência entre o que está aqui e o que o `git log` mostra, **o `git log` vence** — atualize o documento.
