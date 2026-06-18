# Testes E2E de Transferência

## Objetivo

Os testes desta entrega validam o fluxo completo de **Transferência Bancária** pelo ponto de vista do usuário final. O objetivo do E2E (End-to-End) é verificar o comportamento no navegador, garantindo que o frontend (Thymeleaf e HTML) capture os dados corretamente, envie para a API, passe pelas validações de segurança (Spring Security e regras de negócio) e exiba o feedback visual correto na tela.

## Abordagem e bibliotecas

### Selenium WebDriver

O Selenium foi escolhido porque ele assume o controle de um navegador real. No nosso caso, o robô foi programado para localizar os elementos exatos da tela de transferência utilizando seletores do DOM (Document Object Model), preencher os valores e simular o clique humano no botão de envio.

* **Seletores utilizados:** O robô identifica os campos de entrada através de `By.id("destination")` e `By.id("amount")`. O botão de submit é localizado via `By.xpath("//button[text()='Transferir']")`.

### WebDriverManager

O WebDriverManager é responsável por baixar e gerenciar automaticamente a versão correta do binário do navegador (como o `chromedriver`) de acordo com o sistema operacional da máquina, eliminando a necessidade de versionar executáveis pesados no repositório do projeto.

### Spring Boot com porta aleatória

A classe mãe `AbstractE2ETest` utiliza a anotação `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)`. Isso faz com que o Spring suba o servidor em uma porta livre do sistema, evitando o clássico erro de "Porta 8080 já em uso" quando rodamos a suíte de testes inteira.

## Cenários Validados

A classe principal de testes é a `TransferE2ETest`. Para chegar na tela de transferência, o robô primeiro realiza o fluxo de autenticação inserindo e-mail e senha.

| Caso | Ação do Robô | Resultado esperado no Frontend |
|---|---|---|
| **Transferência com Sucesso** | Preenche conta destino válida (`C00002`) e valor dentro do limite do saldo (R$ 200). | O formulário é processado, a página recarrega e o teste busca na árvore HTML renderizada a classe CSS `alert success` (caixa verde). |
| **Saldo Insuficiente** | Preenche conta destino válida (`C00002`), mas tenta transferir um valor maior que o saldo (R$ 600). | O backend barra a operação via `AccountService`, a página recarrega e o teste confirma a presença da classe `alert error` (caixa vermelha) na tela. |

## Isolamento de Estado (Prevenção de Falsos Positivos)

Em testes visuais, um teste que altera o saldo da conta pode fazer o teste seguinte quebrar. Para garantir isolamento e repetibilidade, a nossa arquitetura implementa três etapas a cada novo cenário:

1. **Limpeza Profunda:** O método `@BeforeEach` na classe mãe dispara um comando nativo `TRUNCATE TABLE transactions, investments, accounts, users RESTART IDENTITY CASCADE`, apagando todos os rastros de testes anteriores em milissegundos.
2. **Semeando Dados:** Imediatamente após a limpeza, inserimos via `JdbcTemplate` dois usuários frescos (Origem e Destino), garantindo que a conta origem sempre comece o teste com R$ 500,00 de saldo.
3. **Sessão Limpa:** O WebDriver inicia uma nova sessão de navegação, sem cookies residuais.

## Como executar

O PostgreSQL de teste deve estar ativo e a base de dados `bancodigital_test` deve existir e estar acessível.

Executar o E2E visualizando o robô controlar o navegador:
```bash
mvn verify -Dheadless=false "-Dit.test=TransferE2ETest"