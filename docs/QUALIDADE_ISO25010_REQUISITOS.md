# Requisitos de qualidade segundo a ISO/IEC 25010

> Este documento define **como o sistema Banco Digital deveria ser**, conforme
> solicitado pela atividade. As medidas abaixo representam metas e critérios de
> aceitação. Elas não afirmam que a implementação atual já atende a todos os
> valores.

## 1. Objetivo

Definir o nível de qualidade esperado para o Banco Digital utilizando as oito
características do modelo de qualidade de produto da ISO/IEC 25010:2011,
adotada no Brasil como ABNT NBR ISO/IEC 25010:2016.

O documento responde, para cada característica:

1. Qual deve ser o nível de exigência.
2. O que deve ser medido.
3. Qual valor deve ser alcançado.
4. Como a medida deve ser validada.
5. Por que esse nível foi escolhido.

## 2. Escala de exigência

A escala indica a importância que cada atributo deve possuir no sistema.

| Nota | Nível | Interpretação |
|---:|---|---|
| **1** | Não necessário | O atributo não é relevante para os objetivos do sistema. |
| **2** | Baixa exigência | O atributo é desejável, mas pequenas falhas têm impacto limitado. |
| **3** | Exigência moderada | O atributo deve atender ao mínimo definido e suas falhas causam impacto controlável. |
| **4** | Exigência alta | O atributo é importante e precisa de evidências objetivas de atendimento. |
| **5** | Exigência crítica | Uma falha compromete o propósito, a segurança ou a confiabilidade do sistema. |

As notas não representam o estado atual. Uma nota **5**, por exemplo, significa
que o atributo deve receber prioridade crítica e possuir critérios rigorosos de
aceitação.

## 3. Resumo das medidas esperadas

| Característica | Nota desejada | Principal medida de aceitação |
|---|---:|---|
| Adequação funcional | **5** | 100% dos requisitos obrigatórios implementados e aprovados |
| Eficiência de desempenho | **4** | 95% das páginas em até 2 s e operações em até 3 s |
| Compatibilidade | **4** | Funcionamento nos principais navegadores e serviços sem conflito |
| Usabilidade | **4** | 90% das tarefas concluídas sem ajuda e atendimento à WCAG 2.1 AA |
| Confiabilidade | **5** | Nenhuma inconsistência ou perda de dados em operações financeiras |
| Segurança | **5** | Autenticação, autorização, proteção de credenciais, CSRF e auditoria |
| Manutenibilidade | **4** | 80% de arestas e 80% de mutação nas classes-alvo |
| Portabilidade | **4** | Execução reproduzível em Windows e Linux por Docker Compose |

## 4. Adequação funcional

**Nível desejado: 5 - Exigência crítica.**

### Medidas

| Subcaracterística | Medida esperada | Critério de aceitação | Validação |
|---|---|---|---|
| Completude funcional | Cobertura dos requisitos obrigatórios | **100%** dos requisitos possuem implementação e caso de teste associado | Matriz de rastreabilidade entre requisito e teste |
| Correção funcional | Resultados corretos das operações | **100%** dos casos críticos aprovados, sem erro de saldo ou transação | Testes unitários, integração e E2E |
| Adequação funcional | Capacidade de concluir os objetivos do usuário | Todos os fluxos principais podem ser concluídos sem etapas desnecessárias | Testes de sistema com Selenium |

### Justificativa

O Banco Digital deve executar corretamente cadastro, login, consulta de saldo,
depósito, saque, transferência, investimento e extrato. Um resultado incorreto
em qualquer operação financeira pode produzir perda ou inconsistência de dados.
Por isso, a adequação funcional deve ser tratada como requisito crítico.

## 5. Eficiência de desempenho

**Nível desejado: 4 - Exigência alta.**

### Medidas

| Subcaracterística | Medida esperada | Critério de aceitação | Validação |
|---|---|---|---|
| Comportamento temporal | Tempo de carregamento de páginas | Pelo menos **95%** dos carregamentos em até **2 segundos** | Selenium com medição de tempo |
| Comportamento temporal | Tempo de conclusão de operações | Pelo menos **95%** dos envios respondem em até **3 segundos** | `PerformanceE2ETest` |
| Utilização de recursos | Consumo de CPU e memória | Aplicação permanece estável sem crescimento contínuo de memória | `docker stats` durante teste de carga |
| Capacidade | Usuários simultâneos | Suportar pelo menos **50 usuários simultâneos** sem erro funcional | JMeter ou k6 |

### Justificativa

O usuário precisa consultar e movimentar sua conta sem atrasos perceptíveis.
Entretanto, por ser um sistema acadêmico, não é necessário estabelecer a mesma
capacidade de uma instituição bancária de produção. Por isso, o atributo recebe
nível alto, mas não crítico.

## 6. Compatibilidade

**Nível desejado: 4 - Exigência alta.**

### Medidas

| Subcaracterística | Medida esperada | Critério de aceitação | Validação |
|---|---|---|---|
| Coexistência | Execução simultânea dos serviços | Aplicação, PostgreSQL e ferramentas auxiliares executam sem conflito | Docker Compose e verificação de saúde |
| Interoperabilidade | Uso de padrões de comunicação | Comunicação por HTTP, JDBC e formatos documentados | Testes de integração |
| Compatibilidade de navegador | Execução da interface web | Fluxos principais funcionam em Chrome, Edge e Firefox atuais | Testes E2E parametrizados por navegador |

### Justificativa

O sistema deve funcionar no ambiente dos integrantes, dos avaliadores e em
navegadores comuns. A comunicação deve utilizar protocolos conhecidos para
reduzir dependência de uma plataforma ou fornecedor específico.

## 7. Usabilidade

**Nível desejado: 4 - Exigência alta.**

### Medidas

| Subcaracterística | Medida esperada | Critério de aceitação | Validação |
|---|---|---|---|
| Reconhecibilidade | Compreensão das funções | Usuário identifica a finalidade das telas e comandos sem documentação externa | Avaliação com roteiro de tarefas |
| Apreensibilidade | Facilidade de aprendizado | Pelo menos **90%** das tarefas são concluídas sem ajuda | Teste com usuários ou avaliação heurística |
| Operabilidade | Facilidade de realizar operações | Fluxos possuem labels, feedback e navegação consistentes | Testes E2E e inspeção da interface |
| Proteção contra erros | Prevenção e comunicação de falhas | Entradas inválidas não alteram dados e exibem mensagens claras | Testes funcionais negativos |
| Acessibilidade | Atendimento a padrões de acessibilidade | Ausência de violações críticas da **WCAG 2.1 nível AA** | Lighthouse ou axe-core |

### Justificativa

Operações financeiras precisam ser compreensíveis e fornecer confirmação clara.
Erros de preenchimento não podem resultar em movimentações inesperadas. A
acessibilidade também deve ser considerada para que diferentes usuários possam
utilizar o sistema.

## 8. Confiabilidade

**Nível desejado: 5 - Exigência crítica.**

### Medidas

| Subcaracterística | Medida esperada | Critério de aceitação | Validação |
|---|---|---|---|
| Maturidade | Frequência de falhas | Nenhuma falha conhecida de severidade crítica antes da entrega | Relatório de testes e inspeção |
| Disponibilidade | Continuidade do serviço | Disponibilidade mínima de **99%** durante o período de avaliação | Monitoramento ou teste de disponibilidade |
| Tolerância a falhas | Preservação dos dados após erro | Falhas durante operações não deixam atualizações parciais | Testes de rollback e integração |
| Recuperabilidade | Restauração após falha | Dados podem ser restaurados por procedimento documentado | Teste de backup e restauração |
| Consistência transacional | Integridade das operações financeiras | **Zero** divergência entre saldo e transações registradas | Consultas de conferência no PostgreSQL |

### Justificativa

Depósitos, saques, transferências e investimentos precisam ser atômicos. Uma
falha não pode debitar uma conta sem creditar a outra ou registrar uma
transação sem atualizar o saldo. Por isso, confiabilidade é requisito crítico.

## 9. Segurança

**Nível desejado: 5 - Exigência crítica.**

### Medidas

| Subcaracterística | Medida esperada | Critério de aceitação | Validação |
|---|---|---|---|
| Confidencialidade | Proteção de credenciais e sessões | Nenhuma senha armazenada em texto puro; cookies protegidos | Inspeção do banco e testes de autenticação |
| Integridade | Proteção contra alteração indevida | CSRF ativo, SQL parametrizado e validação no servidor | Testes de integração e análise estática |
| Autenticidade | Verificação da identidade | Rotas privadas acessíveis somente após autenticação válida | Testes E2E de login e autorização |
| Responsabilização | Associação de ações a usuários | Toda operação financeira pode ser associada ao usuário responsável | Consulta das transações e logs |
| Não repúdio | Registro de operações relevantes | Operações possuem data, tipo, conta e valor registrados | Testes de integração com PostgreSQL |
| Vulnerabilidades | Ausência de vulnerabilidades conhecidas | Nenhuma vulnerabilidade crítica ou alta sem mitigação | OWASP Dependency-Check e SonarQube |

### Justificativa

O sistema trata credenciais, saldos e movimentações financeiras. Acesso
indevido, exposição de senha ou alteração não autorizada comprometeriam todo o
propósito da aplicação. Segurança deve, portanto, possuir prioridade crítica.

## 10. Manutenibilidade

**Nível desejado: 4 - Exigência alta.**

### Medidas

| Subcaracterística | Medida esperada | Critério de aceitação | Validação |
|---|---|---|---|
| Modularidade | Separação de responsabilidades | Controllers, services e repositories possuem responsabilidades distintas | Inspeção arquitetural |
| Analisabilidade | Facilidade de localizar defeitos | Nenhum problema crítico do SonarQube nas classes-alvo | Relatório do SonarQube |
| Modificabilidade | Alteração sem regressão | Mudanças nas regras mantêm toda a suíte verde | Execução de `mvn verify` |
| Testabilidade | Cobertura estrutural | Pelo menos **80% de cobertura de arestas** em cada classe-alvo | JaCoCo |
| Resistência a defeitos | Escore de mutação | Pelo menos **80% de mutantes mortos** nas mesmas classes | PIT |
| Complexidade | Complexidade das classes-alvo | Classes escolhidas são não CRUD e possuem complexidade ciclomática mínima definida pelo trabalho | JaCoCo ou SonarQube |

### Classes-alvo

| Integrante | Classe |
|---|---|
| Erivelton Campos | `AccountService` |
| Yuri Coutinho | `InvestmentService` |
| João Mainoth | `SignupService` |
| Gabriel Ferraz | `Money` |
| Gleytton | `StatementLine` |

### Justificativa

O projeto é desenvolvido por uma equipe e precisa permitir correções e novas
funcionalidades sem introduzir regressões. Cobertura estrutural e mutação foram
escolhidas porque medem não apenas a execução das linhas, mas a capacidade dos
testes de percorrer decisões e detectar defeitos introduzidos.

## 11. Portabilidade

**Nível desejado: 4 - Exigência alta.**

### Medidas

| Subcaracterística | Medida esperada | Critério de aceitação | Validação |
|---|---|---|---|
| Adaptabilidade | Execução em ambientes diferentes | Sistema executa em Windows e Linux sem alteração no código-fonte | Execução documentada nos dois ambientes |
| Instalabilidade | Esforço necessário para instalação | Ambiente iniciado com até **dois comandos documentados** | Teste seguindo o README em máquina limpa |
| Configurabilidade | Ausência de configuração fixa | Banco, usuário, senha e portas podem ser definidos externamente | Inspeção dos arquivos de configuração |
| Reprodutibilidade | Igualdade entre ambientes | Flyway cria o mesmo schema e os testes produzem os mesmos resultados | Docker Compose e `mvn verify` |
| Substituibilidade | Troca de componentes | Troca de implementação de repositório não altera regras de negócio | Testes unitários com interfaces/fakes |

### Justificativa

Todos os integrantes e avaliadores devem conseguir executar o projeto sem
instalar e configurar manualmente cada componente. Docker Compose, variáveis de
ambiente e migrations tornam a execução reproduzível, embora o uso de
PostgreSQL continue sendo uma decisão arquitetural do projeto.

## 12. Resultado esperado

A média simples dos níveis desejados é:

```text
(5 + 4 + 4 + 4 + 5 + 5 + 4 + 4) / 8 = 4,375
```

Arredondando para duas casas, o sistema possui uma **meta global de qualidade
igual a 4,38 de 5**, classificada como **qualidade esperada alta**.

Essa média não substitui os critérios individuais. Segurança ou confiabilidade
abaixo do nível esperado não devem ser compensadas por uma nota maior em outro
atributo.

## 13. Rastreabilidade das validações

| Característica | Evidência a produzir |
|---|---|
| Adequação funcional | Matriz de requisitos e resultados dos testes |
| Eficiência de desempenho | Relatório do `PerformanceE2ETest` e teste de carga |
| Compatibilidade | Resultado E2E em diferentes navegadores |
| Usabilidade | Relatório Lighthouse/axe-core e roteiro de tarefas |
| Confiabilidade | Testes de transação, rollback, backup e recuperação |
| Segurança | Testes de autenticação/CSRF e relatório de vulnerabilidades |
| Manutenibilidade | Relatórios JaCoCo, PIT e SonarQube |
| Portabilidade | Execução documentada com Docker Compose em ambiente limpo |

## 14. Diferença para a avaliação da implementação

Este documento define **requisitos e metas**. O documento
[`QUALIDADE_ISO25010.md`](QUALIDADE_ISO25010.md) pode ser usado separadamente
para registrar o nível efetivamente alcançado pelo sistema.

Exemplo:

- Meta de segurança: **5**.
- Resultado medido na implementação: pode ser **3** ou **4**.
- Diferença encontrada: deve gerar risco, recomendação ou tarefa de melhoria.

Essa separação impede que limitações atuais sejam utilizadas para reduzir o
nível de qualidade que um sistema bancário deveria possuir.

