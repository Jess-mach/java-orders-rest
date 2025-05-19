# Sistema de Pedidos

Este é um sistema CRUD completo para gerenciamento de pedidoEntities, itens de pedidoEntities e produtoEntities.

## Tecnologias Utilizadas

- Java 17
- Spring Boot 3.1.5
- Spring Data JPA
- MySQL (em produção)
- H2 Database (para testes)
- Spring Doc OpenAPI (Swagger UI)
- Maven

## Características do Projeto

- API RESTful para gerenciamento de produtoEntities, pedidoEntities e itens de pedidoEntity
- Persistência de dados com JPA/Hibernate
- Tratamento de exceções global
- Validação de dados com Bean Validation
- Documentação da API com Swagger/OpenAPI
- Testes unitários e de integração
- Sem uso de Lombok ou MapStruct (conforme solicitado)

## Estrutura do Projeto

```
src/main/java/com/sistema/pedidoEntities/
├── SistemaPedidosApplication.java
├── config/
│   └── SwaggerConfig.java
├── controller/
│   ├── ProdutoController.java
│   ├── PedidoController.java
│   └── ItemPedidoController.java
├── model/
│   ├── Produto.java
│   ├── Pedido.java
│   └── ItemPedido.java
├── repository/
│   ├── ProdutoRepository.java
│   ├── PedidoRepository.java
│   └── ItemPedidoRepository.java
├── service/
│   ├── ProdutoService.java
│   ├── PedidoService.java
│   └── ItemPedidoService.java
└── exception/
    ├── ResourceNotFoundException.java
    ├── BadRequestException.java
    └── GlobalExceptionHandler.java
```

## Modelo de Dados

### Produto
- ID (Long)
- Nome (String)
- Descrição (String)
- Preço (BigDecimal)
- Quantidade em Estoque (Integer)

### Pedido
- ID (Long)
- Cliente (String)
- Data do Pedido (LocalDateTime)
- Observação (String)
- Valor Total (BigDecimal)
- Status (Enum: PENDENTE, APROVADO, CANCELADO, ENTREGUE)
- Itens (List\<ItemPedido\>)

### Item de Pedido
- ID (Long)
- Pedido (Pedido)
- Produto (Produto)
- Quantidade (Integer)
- Preço Unitário (BigDecimal)
- Valor Total (BigDecimal)

## Endpoints da API

### Produtos
- GET /api/produtoEntities - Lista todos os produtoEntities
- GET /api/produtoEntities/{id} - Busca um produtoEntity pelo ID
- GET /api/produtoEntities/buscar?nome={nome} - Busca produtoEntities pelo nome
- POST /api/produtoEntities - Cria um novo produtoEntity
- PUT /api/produtoEntities/{id} - Atualiza um produtoEntity existente
- DELETE /api/produtoEntities/{id} - Remove um produtoEntity

### Pedidos
- GET /api/pedidoEntities - Lista todos os pedidoEntities
- GET /api/pedidoEntities/{id} - Busca um pedidoEntity pelo ID
- GET /api/pedidoEntities/cliente?cliente={cliente} - Busca pedidoEntities por cliente
- GET /api/pedidoEntities/periodo?inicio={data-inicio}&fim={data-fim} - Busca pedidoEntities por período
- GET /api/pedidoEntities/status/{status} - Busca pedidoEntities por status
- POST /api/pedidoEntities - Cria um novo pedidoEntity
- PUT /api/pedidoEntities/{id} - Atualiza um pedidoEntity existente
- PATCH /api/pedidoEntities/{id}/status?status={status} - Atualiza apenas o status de um pedidoEntity
- DELETE /api/pedidoEntities/{id} - Remove um pedidoEntity (apenas se estiver pendente)

### Itens de Pedido
- GET /api/itens-pedidoEntity - Lista todos os itens de pedidoEntity
- GET /api/itens-pedidoEntity/{id} - Busca um item de pedidoEntity pelo ID
- GET /api/itens-pedidoEntity/pedidoEntity/{pedidoId} - Busca itens por pedidoEntity
- GET /api/itens-pedidoEntity/produtoEntity/{produtoId} - Busca itens por produtoEntity
- POST /api/itens-pedidoEntity - Cria um novo item de pedidoEntity
- PUT /api/itens-pedidoEntity/{id} - Atualiza um item de pedidoEntity existente
- DELETE /api/itens-pedidoEntity/{id} - Remove um item de pedidoEntity (apenas se o pedidoEntity estiver pendente)

## Regras de Negócio

1. **Produtos**
    - O preço deve ser sempre maior que zero
    - A quantidade em estoque não pode ser negativa

2. **Pedidos**
    - Um pedidoEntity deve ter pelo menos um item
    - O status inicial é PENDENTE
    - A data do pedidoEntity é a data atual se não for informada
    - O valor total é calculado automaticamente com base nos itens
    - Regras de transição de status:
        - De PENDENTE pode ir para APROVADO ou CANCELADO
        - De APROVADO pode ir para ENTREGUE ou CANCELADO
        - ENTREGUE e CANCELADO são estados finais
    - Só é possível excluir um pedidoEntity com status PENDENTE

3. **Itens de Pedido**
    - A quantidade deve ser maior que zero
    - O preço unitário é obtido do produtoEntity no momento da criação
    - O valor total é calculado automaticamente (quantidade * preço unitário)
    - Só é possível alterar a quantidade de um item se o pedidoEntity estiver PENDENTE
    - Só é possível excluir um item se o pedidoEntity estiver PENDENTE

## Instruções para Execução

### Pré-requisitos

- JDK 17 ou superior
- Maven
- MySQL (ou outro banco de dados compatível)

### Configuração do Banco de Dados

Edite o arquivo `src/main/resources/application.properties` com as informações do seu banco de dados:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sistema_pedidos?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
```

### Executando o Projeto

1. Clone o repositório:
   ```bash
   git clone https://github.com/seu-usuario/sistema-pedidoEntities.git
   cd sistema-pedidoEntities
   ```

2. Compile o projeto com Maven:
   ```bash
   mvn clean install
   ```

3. Execute a aplicação:
   ```bash
   mvn spring-boot:run
   ```

4. Acesse a aplicação:
    - API: http://localhost:8080/api/
    - Documentação Swagger: http://localhost:8080/swagger-ui.html

### Executando os Testes

Para executar os testes unitários e de integração:

```bash
mvn test
```

Os testes utilizam o banco de dados em memória H2, então não é necessário configurar um banco de dados específico para os testes.

## Exemplos de Uso

### Criar um Produto

```bash
curl -X POST http://localhost:8080/api/produtoEntities \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Smartphone XYZ",
    "descricao": "Smartphone de última geração",
    "preco": 1999.90,
    "quantidadeEstoque": 50
  }'
```

### Criar um Pedido

```bash
curl -X POST http://localhost:8080/api/pedidoEntities \
  -H "Content-Type: application/json" \
  -d '{
    "cliente": "João Silva",
    "observacao": "Entregar no período da tarde",
    "itens": [
      {
        "produtoEntity": {
          "id": 1
        },
        "quantidade": 2
      }
    ]
  }'
```

### Atualizar Status do Pedido

```bash
curl -X PATCH "http://localhost:8080/api/pedidoEntities/1/status?status=APROVADO"
```

## Informações Adicionais

- O projeto utiliza DTOs implícitos (as próprias entidades são usadas como DTOs)
- A segurança não foi implementada neste exemplo
- Em um ambiente de produção, considere adicionar segurança, logging, monitoramento, etc.