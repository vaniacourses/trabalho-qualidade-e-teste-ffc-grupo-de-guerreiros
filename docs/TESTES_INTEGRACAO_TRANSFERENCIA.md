# Testes de integração: Transferência

## Objetivo

Este documento descreve os testes de integração criados para o fluxo de transferências bancárias, cobrindo a comunicação entre `TransferController`, `AccountService`, `TransactionRepository` e o banco de dados.

## Abordagem usada

Foi utilizado `@SpringBootTest` com `MockMvc`, herdando da configuração base `AbstractIntegrationTest`. Essa abordagem sobe o contexto real do Spring Boot, aplica as migrations do Flyway, passa pelos filtros do Spring Security e permite testar a rota HTTP de forma integrada.

O grande diferencial deste escopo é a validação de operações **ACID** no banco PostgreSQL de teste. Como a transferência envolve debitar de uma conta e creditar em outra, o teste foca em comprovar o funcionamento da anotação `@Transactional`, garantindo o *rollback* em caso de regras de negócio violadas.

`JdbcTemplate` foi utilizado para consultar o banco de dados fisicamente após os requests, garantindo que o saldo (`balance`) e o histórico (`transactions`) se mantiveram consistentes.

## Classes criadas

### `TransferIntegrationTest`

Valida os 6 cenários exigidos no planejamento da Issue de Integração:

- **POST `/transfer` com sucesso:** Confirma que o saldo da origem diminuiu, o do destino aumentou e a transação foi registrada no extrato.
- **POST `/transfer` com valor exato do saldo:** Valida a transferência de 100% dos fundos (limite superior).
- **POST `/transfer` com saldo insuficiente (Rollback):** Confirma o redirecionamento de erro e prova via SQL (`SELECT COUNT(*) FROM transactions`) que nenhuma sujeira foi gravada no banco.
- **POST `/transfer` para a própria conta:** Valida a trava de segurança de destino e origem iguais.
- **POST `/transfer` para destino inexistente:** Garante que o sistema não quebra com exceções não tratadas e protege os fundos.
- **POST `/transfer` com valor zero:** Rejeita valores não positivos na camada de serviço.

Bibliotecas/recursos usados:

- `MockMvc`: simula requests POST validando as rotas protegidas.
- `spring-security-test`: fornece a injeção do token `.with(csrf())` obrigatório do Thymeleaf e o usuário via `@WithMockUser`.
- `JdbcTemplate`: atesta o estado real das tabelas `accounts` e `transactions` pós-request.

## Como executar

Com o PostgreSQL de teste ativo, execute diretamente a suíte de transferência:

```bash
mvn test -Dtest='TransferIntegrationTest'