# AGENTS.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

This is the **Ordering Service**, a Spring Boot microservice that manages the complete order lifecycle for the Algashop platform. It handles customer management, shopping cart operations, order creation, order status transitions, and integrations with external services (Product Catalog, Shipping, Billing).

**Key Responsibilities:**
- Customer account management (registration, update, archival)
- Shopping cart operations (add items, remove items, checkout)
- Order management (create, cancel, change status)
- Loyalty points calculation and management
- Shipping cost calculation and delivery date estimation
- Integration with Product Catalog service (product availability, pricing)
- Integration with Shipping service (RapiDex) for cost and delivery estimates
- Payment processing coordination

**Technology Stack:**
- Java 25, Gradle 9.2.1, Spring Boot 4.0.x
- PostgreSQL (with Flyway migrations)
- Spring Security OAuth2 (Resource Server)
- Redis (caching, session storage)
- Spring Cloud Circuit Breaker (resilience for external calls)
- Spring Cloud Contract (contract-driven testing)
- TestContainers (integration test databases)
- Event sourcing pattern with domain events

## Architecture

### Domain-Driven Design with Hexagonal Architecture

The codebase follows **hexagonal (ports & adapters)** architecture with strong domain layer using DDD principles:

```
core/
├── domain/model/           → Business logic (aggregates, value objects, domain services)
├── application/            → Use cases (application services, domain event handlers)
├── ports/                  → Ports (input/output contracts)
└── adapters/               → Adapters (REST controllers, database, external services, event publishing)
```

**Key Layers:**

1. **Domain Layer** (`core/domain/model/`)
    - **Aggregates:** Business entities with encapsulated logic
        - `Customer` — User account with personal info, loyalty points, archived status
        - `Order` — Complete order with items, shipping, payment info, status transitions
        - `ShoppingCart` — Temporary cart for collecting items before checkout
    - **Value Objects:** Immutable objects representing domain concepts
        - `Money` — Price with decimal precision
        - `Address`, `Phone`, `Email`, `Document` (CPF) — Validated personal data
        - `CustomerId`, `OrderId`, `ShoppingCartId` — Typed IDs (TSID)
        - `Quantity`, `ZipCode`, `BirthDate` — Domain-specific values with validation
    - **Domain Services:** Cross-aggregate logic
        - `ProductCatalogService` — Fetches product info from Product Catalog service
        - `ShippingCostService` — Calculates shipping costs via RapiDex
        - `OriginAddressService` — Determines origin address for shipping
        - `ShoppingCartProductAdjustmentService` — Validates product availability when adding to cart
    - **Domain Events:** Published when important state changes occur
        - `CustomerRegisteredEvent` — New customer registered
        - `CustomerArchivedEvent` — Customer account archived
        - `ShoppingCartCreatedEvent` — New cart created
        - `ShoppingCartItemAddedEvent` — Item added to cart
        - `ShoppingCartItemRemovedEvent` — Item removed from cart
        - `ShoppingCartEmptiedEvent` — Cart emptied
        - `OrderPlacedEvent` — Order created and placed
        - `OrderPaidEvent` — Payment confirmed
        - `OrderReadyEvent` — Order ready for shipment
        - `OrderCanceledEvent` — Order canceled
    - **Specifications:** Business rule validators
        - `CustomerHasEnoughLoyaltyPointsSpecification` — Checks loyalty point balance
        - `CustomerHasOrderedEnoughAtYearSpecification` — Checks annual spending threshold
        - `CustomerHaveFreeShippingSpecification` — Determines free shipping eligibility
    - **Repositories (Ports):** Persistence contracts (JPA implementations in adapters)
        - `Customers` — Customer persistence
        - `Orders` — Order persistence
        - `ShoppingCarts` — Shopping cart persistence

2. **Application Layer** (`core/application/`)
    - **Application Services:** Orchestrate use cases, handle domain events
        - `CustomerManagementApplicationService` — Create, update, archive customers
        - `CustomerQueryService` — Query customers with filters and pagination
        - `CustomerRegistrationConfirmationApplicationService` — Confirm email/registration
        - `CustomerLoyaltyPointsApplicationService` — Manage loyalty points
        - `ShoppingCartManagementApplicationService` — Add/remove items, proceed to checkout
        - `ShoppingCartQueryService` — Query cart contents
        - `OrderManagementApplicationService` — Create, cancel, change order status
        - `OrderQueryService` — Query orders with filters
    - **Domain Event Handlers:** Respond to domain events
    - **DTOs & Mappers:** Data transfer and transformation

3. **Ports Layer** (`core/ports/`)
    - **Input Ports (in/):** Use case interfaces, request/response DTOs
        - `ForManagingCustomers`, `ForQueryingCustomers`, `ForConfirmCustomerRegistration`
        - `ForManagingOrders`, `ForQueryingOrders`
        - `ForManagingShoppingCarts`, `ForQueryingShoppingCarts`
    - **Output Ports (out/):** Outbound service contracts, response DTOs
        - `FindProductPort` — Product Catalog integration
        - `CalculateShippingPort` — Shipping service integration
        - `EmailNotificationPort` — Email sending
        - Event publishing port

4. **Adapters Layer** (`adapters/`)
    - **In (Inbound):**
        - REST Controllers — HTTP endpoints
        - gRPC adapters (if applicable)
    - **Out (Outbound):**
        - **Persistence**: JPA repositories for Customer, Order, ShoppingCart
        - **HTTP Clients**: REST clients for Product Catalog, RapiDex (Shipping)
            - `ProductCatalogAPIClient` with Circuit Breaker
            - `RapiDexAPIClient` with Circuit Breaker & Retry
        - **Event Publishing**: Kafka/RabbitMQ for domain events
        - **Email Notifications**: Email service adapter

### Security Model

- **OAuth2 Resource Server:** Validates incoming JWT tokens from authorization-server
- **Client Credentials:** Uses client credentials flow to call other services (Product Catalog, Shipping)
- **Scope-based access:** Endpoints protected by OAuth2 scopes (e.g., `SCOPE_orders:read`)
- **Spring Security method-level annotations** for fine-grained access control

### Event Sourcing & Domain Events

- Domain events are published when aggregates transition state (Order placed, payment confirmed, etc.)
- Application services listen to domain events and trigger side effects (email notifications, updates to other services)
- Event handlers are located in `core/application/` and subscribed via Spring event listeners
- Example: When `OrderPlacedEvent` fires, a handler may invoke the billing service

### Spring Profiles

The application uses layered profiles:
- `base` — Common configuration
- `development-env` — Local development overrides
- `docker-env` — Docker Compose overrides (DB URLs, service URLs, etc.)
- `production-env` — Production settings

Activate via `SPRING_PROFILES_ACTIVE=docker` or in application.yml.

## Build Commands

```bash
cd microservices/ordering

# Compile and run all tests (unit + integration)
./gradlew build

# Compile only
./gradlew classes

# Run unit tests only
./gradlew test

# Run integration tests (marked with *IT.java)
./gradlew integrationTest

# Run Spring Cloud Contract tests
./gradlew contractTest

# Run all test types (unit + integration + contract)
./gradlew check

# Build runnable JAR
./gradlew bootJar

# Build multi-platform Docker image (linux/arm64, linux/amd64)
./gradlew dockerBuild

# Run a single test class
./gradlew test --tests "com.algaworks.algashop.ordering.presentation.OrderControllerTest"

# Run with specific Spring profile
./gradlew build -Pprofile=docker-env
```

## Running Locally

**Start infrastructure (Postgres, Redis, WireMock, etc.):**
```bash
cd ../..  # Go to monorepo root
docker compose -f docker-compose.tools.yml up -d
```

**Run the application:**
```bash
# From ordering directory
./gradlew bootRun

# With specific profile
SPRING_PROFILES_ACTIVE=docker ./gradlew bootRun
```

The server starts on **port 8080** (configured in application.yml).

**Required /etc/hosts entries** (if not already set):
```
127.0.0.1 algashop-mongodb-1 algashop-mongodb-2 algashop-mongodb-3
127.0.0.1 algashop-localstack s3.algashop-localstack algashop-product-image.algashop-localstack
127.0.0.1 authorization-server
```

## Project Structure

```
src/main/java/com/algaworks/algashop/ordering/
├── core/
│   ├── domain/model/
│   │   ├── commons/                # Shared value objects
│   │   │   ├── Address.java
│   │   │   ├── Document.java       # CPF validation
│   │   │   ├── Email.java
│   │   │   ├── FullName.java
│   │   │   ├── Money.java          # Price/currency
│   │   │   ├── Phone.java
│   │   │   ├── Quantity.java
│   │   │   ├── ZipCode.java
│   │   │   └── ...
│   │   ├── customer/               # Customer aggregate
│   │   │   ├── Customer.java       # Aggregate root
│   │   │   ├── CustomerId.java
│   │   │   ├── Customers.java      # Repository port
│   │   │   ├── BirthDate.java
│   │   │   ├── LoyaltyPoints.java
│   │   │   ├── CustomerRegisteredEvent.java
│   │   │   ├── CustomerArchivedEvent.java
│   │   │   └── [exceptions]
│   │   ├── order/                  # Order aggregate
│   │   │   ├── Order.java          # Aggregate root
│   │   │   ├── OrderId.java
│   │   │   ├── Orders.java         # Repository port
│   │   │   ├── OrderStatus.java    # Enum
│   │   │   ├── PaymentMethod.java  # Enum
│   │   │   ├── Shipping.java
│   │   │   ├── CreditCardId.java
│   │   │   ├── OrderPlacedEvent.java
│   │   │   ├── OrderPaidEvent.java
│   │   │   ├── OrderReadyEvent.java
│   │   │   ├── OrderCanceledEvent.java
│   │   │   ├── [specifications]    # Business rules
│   │   │   └── [exceptions]
│   │   ├── shoppingcart/           # ShoppingCart aggregate
│   │   │   ├── ShoppingCart.java   # Aggregate root
│   │   │   ├── ShoppingCartId.java
│   │   │   ├── ShoppingCartItem.java
│   │   │   ├── ShoppingCarts.java  # Repository port
│   │   │   ├── ShoppingCartCreatedEvent.java
│   │   │   ├── ShoppingCartItemAddedEvent.java
│   │   │   ├── ShoppingCartItemRemovedEvent.java
│   │   │   ├── ShoppingCartEmptiedEvent.java
│   │   │   └── [exceptions]
│   │   ├── product/                # External product reference
│   │   │   ├── Product.java        # DTO from Product Catalog
│   │   │   ├── ProductId.java
│   │   │   ├── ProductCatalogService.java  # Domain service
│   │   │   └── [exceptions]
│   │   ├── order/shipping/         # Shipping services
│   │   │   ├── ShippingCostService.java
│   │   │   └── OriginAddressService.java
│   │   ├── AggregateRoot.java      # Base class
│   │   ├── DomainException.java    # Base exception
│   │   ├── DomainService.java      # Base interface
│   │   └── ...
│   ├── application/
│   │   ├── customer/
│   │   │   ├── CustomerManagementApplicationService.java
│   │   │   ├── CustomerQueryService.java
│   │   │   ├── CustomerRegistrationConfirmationApplicationService.java
│   │   │   ├── CustomerLoyaltyPointsApplicationService.java
│   │   │   └── [DTOs, handlers]
│   │   ├── order/
│   │   │   ├── OrderManagementApplicationService.java
│   │   │   ├── OrderQueryService.java
│   │   │   └── [DTOs, handlers]
│   │   ├── shoppingcart/
│   │   │   ├── ShoppingCartManagementApplicationService.java
│   │   │   ├── ShoppingCartQueryService.java
│   │   │   └── [DTOs, handlers]
│   │   └── utility/
│   │       ├── Mapper.java
│   │       └── PageFilter.java
│   └── ports/
│       ├── in/
│       │   ├── customer/
│       │   │   ├── ForManagingCustomers.java
│       │   │   ├── ForQueryingCustomers.java
│       │   │   ├── CustomerInput.java
│       │   │   ├── CustomerOutput.java
│       │   │   └── ...
│       │   ├── order/
│       │   │   ├── ForManagingOrders.java
│       │   │   ├── ForQueryingOrders.java
│       │   │   └── ...
│       │   └── checkout/
│       │       ├── CheckoutInput.java
│       │       └── ShippingInput.java
│       └── out/
│           ├── FindProductPort.java
│           ├── CalculateShippingPort.java
│           └── EmailNotificationPort.java
├── adapters/
│   ├── in/
│   │   ├── rest/
│   │   │   ├── CustomerController.java    # GET/POST /api/v1/customers
│   │   │   ├── OrderController.java       # GET/POST/PATCH /api/v1/orders
│   │   │   ├── ShoppingCartController.java # GET/POST /api/v1/shopping-carts
│   │   │   └── CheckoutController.java    # POST /api/v1/checkout
│   │   └── ApiExceptionHandler.java       # Exception handling
│   └── out/
│       ├── persistence/
│       │   ├── CustomerJpaRepository.java
│       │   ├── CustomerRepositoryAdapter.java
│       │   ├── OrderJpaRepository.java
│       │   ├── OrderRepositoryAdapter.java
│       │   ├── ShoppingCartJpaRepository.java
│       │   └── ShoppingCartRepositoryAdapter.java
│       ├── http/
│       │   ├── ProductCatalogAPIClient.java  # Product Catalog integration
│       │   └── RapiDexAPIClient.java         # Shipping service integration
│       ├── event/
│       │   └── DomainEventPublisher.java
│       └── ...
└── OrderingApplication.java
```

## Database

Migrations run automatically on startup via **Flyway**.

**Current migrations:**
- V001: Creates `customer` table with loyalty points and archived flag
- V002: Creates `order` table with status, shipping, payment, billing info
- V003: Creates `shopping_cart` table
- V004: Adds unique constraint on shopping_cart
- V005: Adds credit card fields to order table
- V006: Adds billing email to order table

**To add a migration:**
1. Create `src/main/resources/db/migration/V{n}__description.sql`
2. Use snake_case for table/column names
3. Test locally: restart the application (Flyway executes on startup)
4. Ensure migrations are backward-compatible (avoid dropping columns)

## Testing

Test structure mirrors source structure:

```
src/test/java/com/algaworks/algashop/ordering/
├── core/
│   ├── domain/model/
│   │   ├── customer/
│   │   │   └── CustomerTest.java
│   │   ├── order/
│   │   │   └── OrderTest.java
│   │   └── ...
│   └── application/
│       ├── customer/
│       │   └── CustomerManagementApplicationServiceTest.java
│       └── order/
│           └── OrderManagementApplicationServiceTest.java
└── adapters/
    ├── in/rest/
    │   └── CustomerControllerTest.java
    └── out/http/
        └── ProductCatalogAPIClientTest.java
```

### Test Types

- **Unit tests** (`*Test.java`): Test application services, domain logic, value objects
    - Use `@SpringBootTest` for tests needing Spring context
    - Use plain JUnit for pure domain logic
    - Mock external dependencies (HTTP clients, repositories)

- **Integration tests** (`*IT.java`): Use TestContainers for embedded databases
    - Test persistence layer with real database
    - Test application services with real repositories
    - Use `@DataJpaTest` for repository-focused tests

- **Contract tests** (`*ContractTest.java`): Spring Cloud Contract verifier tests
    - Define contracts with external services (Product Catalog, Shipping)
    - Auto-generate stubs for testing

Example:
```java
@SpringBootTest
class CustomerManagementApplicationServiceTest {
    @Test
    void shouldCreateCustomerWithValidData() { ... }
    
    @Test
    void shouldThrowExceptionWhenEmailAlreadyInUse() { ... }
}
```

## Key Concepts

### Domain-Driven Design Patterns

**Aggregates:** Self-contained units with invariants
- `Customer` — Owns personal info, loyalty points, can be archived
- `Order` — Owns order items, shipping address, payment method, status transitions
- `ShoppingCart` — Temporary container for items before checkout

**Value Objects:** Immutable, no identity
- `Money`, `Address`, `Email`, `Phone` — Represent domain concepts with validation
- Custom ID types (`CustomerId`, `OrderId`) — Typed IDs instead of raw strings/longs

**Domain Events:** Important state changes
- Published when aggregate state changes (e.g., order placed, customer registered)
- Application services subscribe and handle side effects
- Enables loose coupling between aggregates

**Specifications:** Encapsulate business rules
- `CustomerHasEnoughLoyaltyPointsSpecification` — Reusable business rule
- Example: `if (customerHasEnoughLoyaltyPoints.isSatisfiedBy(customer)) { ... }`

**Domain Services:** Cross-aggregate logic
- `ProductCatalogService` — Fetches product data from external service
- `ShippingCostService` — Calculates shipping costs

### Order Status Flow

Orders transition through states:
```
PENDING → PLACED → PAID → READY → SHIPPED → DELIVERED
                ↓
           CANCELED
```

Status transitions are guarded (e.g., cannot move from PAID back to PENDING).

### Loyalty Points

- Customers earn loyalty points on orders
- Points can be spent to reduce order total
- Specification checks if customer has enough points

### Shipping & Delivery

- Shipping cost calculated via RapiDex API based on zip codes
- Free shipping eligibility determined by specification
- Delivery date estimated by shipping service

### Shopping Cart

- Created when customer starts shopping
- Items added/removed before checkout
- Validated against product availability before checkout
- Emptied or converted to Order on successful checkout

## Dependencies & External Integrations

### Internal Services
- **Authorization Server** (OAuth2 token validation) — Port 8081
    - OAuth2 token issuer; all requests validated against tokens

### External Services (via HTTP clients)
- **Product Catalog Service** (port 8083)
    - `GET /api/v1/products/{productId}` — Fetch product details
    - Used when adding items to cart, creating orders
    - Circuit breaker + Retry configured

- **RapiDex Shipping Service** (external)
    - Calculates shipping costs
    - Estimates delivery dates based on zip codes
    - Circuit breaker + Retry configured

### Libraries
- **Spring Cloud Circuit Breaker** — Resilience wrapper for HTTP calls
- **Spring Cloud Retry** — Automatic retry on transient failures
- **TestContainers** — Embedded databases for integration tests
- **Spring Cloud Contract** — Contract-driven testing
- **ModelMapper** / **Mapper** — DTO/Entity mapping
- **Lombok** — Boilerplate reduction (@Getter, @Setter, etc.)

## Recent Changes

- **Event sourcing pattern**: Domain events published on state changes
- **Loyalty points system**: Customers earn/spend points
- **Multi-path checkout**: Direct purchase or via shopping cart
- **Email confirmation**: Async confirmation on customer registration
- **Shipping integration**: RapiDex API for cost/date calculation

## Common Tasks

### Adding a New Customer Endpoint
1. Add method to `CustomerManagementApplicationService` or `CustomerQueryService`
2. Create input/output DTOs (e.g., `CustomerInput`, `CustomerOutput`)
3. Add endpoint to `CustomerController` with appropriate OAuth2 scope
4. Add validation using Bean Validation annotations (`@NotBlank`, `@Email`, etc.)
5. Write unit test in `CustomerManagementApplicationServiceTest`
6. Write integration test if database interaction involved

### Adding a New Order Status
1. Add enum value to `OrderStatus`
2. Add transition method to `Order` aggregate (e.g., `Order#markAsReady()`)
3. Guard the transition with business rules
4. Publish domain event (e.g., `new OrderReadyEvent(this)`)
5. Add event handler in application service if side effects needed
6. Update tests covering status transitions

### Modifying the Customer Entity
1. Add field to `Customer` (with domain logic if needed)
2. Create Flyway migration: `src/main/resources/db/migration/V{n}__description.sql`
3. Update JPA entity and `@Column` annotations
4. Update DTOs (`CustomerInput`, `CustomerOutput`)
5. Update query service to expose new fields
6. Ensure migrations are backward-compatible

### Integrating a New External Service
1. Create HTTP client adapter (e.g., `MyServiceAPIClient`)
2. Wrap with Circuit Breaker via Spring Cloud Circuit Breaker
3. Create port interface in `core/ports/out/`
4. Implement adapter in `adapters/out/http/`
5. Inject into application service
6. Add mock/stub for testing (WireMock or TestContainers)
7. Write contract tests if service is critical

### Publishing a Domain Event
1. Create event class (e.g., `OrderPlacedEvent extends DomainEvent`)
2. Publish from aggregate: `DomainEventPublisher.publishEvent(new OrderPlacedEvent(...))`
3. Create event handler in application layer
4. Subscribe handler via Spring `@EventListener` or `@TransactionalEventListener`
5. Test event is published and handler executes

## Notes for Future Work

- Async email notifications for customer registration confirmation
- Order history and repeat order functionality
- Advanced loyalty points rules (tiered rewards, seasonal bonuses)
- Fraud detection on payment processing
- Audit logging for sensitive operations (order cancellation, status changes)
- GraphQL API layer for checkout
- Real-time order tracking via WebSockets
- Analytics events for business intelligence
