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

- API RESTful para gerenciamento de produtos, pedidos e itens de pedido
- Persistência de dados com JPA/Hibernate
- Tratamento de exceções global
- Validação de dados com Bean Validation
- Documentação da API com Swagger/OpenAPI
- Testes unitários e de integração
- Sem uso de Lombok ou MapStruct (conforme solicitado)

## Estrutura do Projeto

```
src/main/java/com/sistema/pedidos/
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
- GET /api/produtos - Lista todos os produtos
- GET /api/produtos/{id} - Busca um produto pelo ID
- GET /api/produtos/buscar?nome={nome} - Busca produtos pelo nome
- POST /api/produtos - Cria um novo produto
- PUT /api/produtos/{id} - Atualiza um produto existente
- DELETE /api/produtos/{id} - Remove um produto

### Pedidos
- GET /api/pedidos - Lista todos os pedidos
- GET /api/pedidos/{id} - Busca um pedido pelo ID
- GET /api/pedidos/cliente?cliente={cliente} - Busca pedidos por cliente
- GET /api/pedidos/periodo?inicio={data-inicio}&fim={data-fim} - Busca pedidos por período
- GET /api/pedidos/status/{status} - Busca pedidos por status
- POST /api/pedidos - Cria um novo pedido
- PUT /api/pedidos/{id} - Atualiza um pedido existente
- PATCH /api/pedidos/{id}/status?status={status} - Atualiza apenas o status de um pedido
- DELETE /api/pedidos/{id} - Remove um pedido (apenas se estiver pendente)

### Itens de Pedido
- GET /api/itens-pedido - Lista todos os itens de pedido
- GET /api/itens-pedido/{id} - Busca um item de pedido pelo ID
- GET /api/itens-pedido/pedido/{pedidoId} - Busca itens por pedido
- GET /api/itens-pedido/produto/{produtoId} - Busca itens por produto
- POST /api/itens-pedido - Cria um novo item de pedido
- PUT /api/itens-pedido/{id} - Atualiza um item de pedido existente
- DELETE /api/itens-pedido/{id} - Remove um item de pedido (apenas se o pedido estiver pendente)

## Regras de Negócio

1. **Produtos**
    - O preço deve ser sempre maior que zero
    - A quantidade em estoque não pode ser negativa

2. **Pedidos**
    - Um pedido deve ter pelo menos um item
    - O status inicial é PENDENTE
    - A data do pedido é a data atual se não for informada
    - O valor total é calculado automaticamente com base nos itens
    - Regras de transição de status:
        - De PENDENTE pode ir para APROVADO ou CANCELADO
        - De APROVADO pode ir para ENTREGUE ou CANCELADO
        - ENTREGUE e CANCELADO são estados finais
    - Só é possível excluir um pedido com status PENDENTE

3. **Itens de Pedido**
    - A quantidade deve ser maior que zero
    - O preço unitário é obtido do produto no momento da criação
    - O valor total é calculado automaticamente (quantidade * preço unitário)
    - Só é possível alterar a quantidade de um item se o pedido estiver PENDENTE
    - Só é possível excluir um item se o pedido estiver PENDENTE

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
   git clone https://github.com/seu-usuario/sistema-pedidos.git
   cd sistema-pedidos
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
curl -X POST http://localhost:8080/api/produtos \
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
curl -X POST http://localhost:8080/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "cliente": "João Silva",
    "observacao": "Entregar no período da tarde",
    "itens": [
      {
        "produto": {
          "id": 1
        },
        "quantidade": 2
      }
    ]
  }'
```

### Atualizar Status do Pedido

```bash
curl -X PATCH "http://localhost:8080/api/pedidos/1/status?status=APROVADO"
```

## Informações Adicionais

- O projeto utiliza DTOs implícitos (as próprias entidades são usadas como DTOs)
- A segurança não foi implementada neste exemplo
- Em um ambiente de produção, considere adicionar segurança, logging, monitoramento, etc.