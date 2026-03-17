# AGENTS.md

## Project Overview

**sistema-pedidos** is a CRUD REST API for managing orders, order items, and products. Built with Java 17 and Spring Boot 3.1.5, using Spring Data JPA with MySQL (production) and H2 (tests).

## Tech Stack

- Java 17
- Spring Boot 3.1.5
- Spring Data JPA / Hibernate
- MySQL 8 (runtime) / H2 (test)
- Bean Validation (Jakarta)
- SpringDoc OpenAPI 2.2.0 (Swagger UI)
- Maven (build)
- JaCoCo (code coverage, minimum 70% line coverage)

## Build & Test Commands

| Action | Command |
|---|---|
| Full build + tests | `mvn clean install` |
| Run tests only | `mvn test` |
| Run application | `mvn spring-boot:run` |
| Skip tests on build | `mvn clean install -DskipTests` |

Tests use an in-memory H2 database — no external database setup is needed for testing.

## Project Structure

```
src/main/java/com/sistema/pedidos/
├── SistemaPedidosApplication.java          # Spring Boot entry point
├── config/
│   └── SwaggerConfig.java                  # OpenAPI/Swagger configuration
├── controller/
│   ├── ProdutoController.java              # /api/produtos endpoints
│   └── PedidoController.java              # /api/pedidos endpoints
├── entity/
│   ├── ProdutoEntity.java                  # JPA entity — products table
│   ├── PedidoEntity.java                   # JPA entity — orders table (includes StatusPedido enum)
│   └── ItemPedidoEntity.java              # JPA entity — order items table
├── model/
│   ├── PedidoRequest.java                  # DTO for order creation
│   └── ItemPedidoRequest.java             # DTO for order item within a request
├── repository/
│   ├── ProdutoRepository.java
│   ├── PedidoRepository.java
│   └── ItemPedidoRepository.java
├── service/
│   ├── ProdutoService.java
│   ├── PedidoService.java
│   └── ItemPedidoService.java
└── exception/
    ├── ResourceNotFoundException.java      # 404 exception
    ├── BadRequestException.java            # 400 exception
    └── GlobalExceptionHandler.java         # @RestControllerAdvice — centralized error handling

src/test/java/com/sistema/pedidos/
├── controller/
│   ├── ProdutoEntityControllerTest.java    # @WebMvcTest for ProdutoController
│   └── PedidoEntityControllerTest.java    # @WebMvcTest for PedidoController
└── service/
    ├── ProdutoEntityServiceTest.java       # Unit tests for ProdutoService
    └── PedidoEntityServiceTest.java       # Unit tests for PedidoService
```

## Architecture & Patterns

### Layered Architecture

`Controller → Service → Repository`

- **Controllers** handle HTTP requests/responses, use `@Valid` for input validation, and delegate to services.
- **Services** contain business logic, validation rules, and are annotated with `@Transactional`.
- **Repositories** extend `JpaRepository` for data access.

### Key Design Decisions

- **No Lombok / No MapStruct** — all getters, setters, constructors, equals, hashCode, and toString are written manually.
- **Entities used as DTOs** for responses — the project uses `PedidoRequest`/`ItemPedidoRequest` only for order creation input; entities (`ProdutoEntity`, `PedidoEntity`) are returned directly in API responses.
- **No dedicated ItemPedidoController** — despite what the README states, order items are managed through `PedidoController` and `PedidoService`. There is no `/api/itens-pedido` controller.
- **StatusPedido enum** is defined as an inner enum inside `PedidoEntity`.
- **ItemPedidoEntity** references orders via a `Long pedidoId` column (not a `@ManyToOne` relationship to `PedidoEntity`). The `PedidoEntity` has a `@OneToMany` with `insertable = false, updatable = false`.

### Exception Handling

`GlobalExceptionHandler` catches:
- `ResourceNotFoundException` → 404
- `BadRequestException` → 400
- `MethodArgumentNotValidException` → 400 (validation errors)
- `Exception` → 500 (fallback)

All error responses return a JSON map with `timestamp`, `message`, and `status`.

## Business Rules

### Products (ProdutoEntity)
- Price must be > 0 (`@Positive`)
- Stock quantity cannot be negative
- Name is required (`@NotBlank`)

### Orders (PedidoEntity)
- Must have at least one item
- Initial status is `PENDENTE`
- Order date defaults to `LocalDateTime.now()` if not provided
- Total value (`valorTotal`) is auto-calculated from items
- Status transitions:
  - `PENDENTE` → `APROVADO` or `CANCELADO`
  - `APROVADO` → `ENTREGUE` or `CANCELADO`
  - `ENTREGUE` and `CANCELADO` are final states
- Only orders with status `PENDENTE` can be updated or deleted
- Deleting an order restores product stock

### Order Items (ItemPedidoEntity)
- Quantity must be > 0
- Unit price is taken from the product at creation time
- Total value = quantity × unit price (auto-calculated)
- Stock is decremented when an order is created
- Stock is restored when an order is deleted or items are replaced

## API Endpoints

| Method | Path | Description |
|---|---|---|
| GET | `/api/produtos` | List all products |
| GET | `/api/produtos/{id}` | Get product by ID |
| GET | `/api/produtos/buscar?nome={nome}` | Search products by name |
| POST | `/api/produtos` | Create product |
| PUT | `/api/produtos/{id}` | Update product |
| DELETE | `/api/produtos/{id}` | Delete product |
| GET | `/api/pedidos` | List all orders |
| GET | `/api/pedidos/{id}` | Get order by ID |
| GET | `/api/pedidos/cliente?cliente={cliente}` | Search orders by client |
| GET | `/api/pedidos/periodo?inicio={datetime}&fim={datetime}` | Search orders by date range |
| GET | `/api/pedidos/status/{status}` | Search orders by status |
| POST | `/api/pedidos` | Create order (uses `PedidoRequest` body) |
| PUT | `/api/pedidos/{id}` | Update order |
| PATCH | `/api/pedidos/{id}/status?status={status}` | Update order status |
| DELETE | `/api/pedidos/{id}` | Delete order (only if PENDENTE) |

Swagger UI available at: `/swagger-ui.html`

## Code Style Conventions

- **Language**: All source code (namespaces, variables, functions) in Portuguese naming convention (matching the domain).
- **No code comments**: Code should be self-explanatory. Do not add line comments or docstrings.
- **No Lombok**: Write getters, setters, constructors, equals, hashCode, and toString manually.
- **Validation**: Use Jakarta Bean Validation annotations (`@NotBlank`, `@NotNull`, `@Positive`) on entity fields.
- **Transactions**: Service methods that write data use `@Transactional`; read-only methods use `@Transactional(readOnly = true)`.
- **Constructor injection**: Controllers and services use `@Autowired` constructor injection.
- **Test style**: Tests use `@WebMvcTest` with `@MockBean` for controller tests, and direct mocking with Mockito for service tests.

## Testing Guidelines

- Add new test cases to the existing test files — do not create new test files unless absolutely necessary.
- Controller tests: `ProdutoEntityControllerTest.java`, `PedidoEntityControllerTest.java`
- Service tests: `ProdutoEntityServiceTest.java`, `PedidoEntityServiceTest.java`
- Tests use H2 in-memory database (no external dependencies required).
- JaCoCo enforces a minimum of 70% line coverage at the bundle level.

## Configuration

- Main config: `src/main/resources/application.properties`
- Database: MySQL on `localhost:3306/sistema_pedidos` (auto-creates database)
- JPA: `ddl-auto=update`, SQL logging enabled
- No test-specific `application.properties` — tests rely on `@WebMvcTest` and mocked beans

## Dependencies

All dependencies are managed in `pom.xml`. Do not add new dependencies without explicit approval.
