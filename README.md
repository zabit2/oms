# Order Management System (OMS)

Backend order processing system built with Spring Boot and Java, using an in-memory H2 database. It supports creating orders with multiple items, listing and querying orders, updating statuses (including a scheduled status promotion), and cancelling pending orders.

## Running the application

- **Prerequisites**: Java 25 (or compatible) and Maven installed.
- **Run**:

```bash
mvn spring-boot:run
```

The API will start on `http://localhost:8080`.

You can access the H2 console at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:omsdb`, user: `sa`, password empty).

## Core API endpoints

Base path: `/api/orders`

- **Create order**
  - **POST** `/api/orders`
  - **Request body**:

```json
{
  "customerId": "customer-123",
  "items": [
    {
      "productId": "P1",
      "productName": "Product 1",
      "quantity": 2,
      "unitPrice": 10.5
    }
  ]
}
```

- **Get order by id**
  - **GET** `/api/orders/{id}`

- **List orders (optional status filter)**
  - **GET** `/api/orders`
  - Optional query parameter: `status` (`PENDING`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`)

- **Update order status**
  - **PATCH** `/api/orders/{id}/status`
  - **Request body**:

```json
{
  "status": "SHIPPED"
}
```

- **Cancel order (only when PENDING)**
  - **POST** `/api/orders/{id}/cancel`

## Background processing

A scheduled job runs every 5 minutes and automatically promotes all orders in `PENDING` status to `PROCESSING`. This is implemented using Spring's scheduling support and operates via the `OrderService` to reuse business rules.

## Database schema

The application uses JPA/Hibernate to generate the schema in an in-memory H2 database. The key tables are:

- **`orders`**
  - `id` (BIGINT, PK, auto-increment)
  - `customer_id` (VARCHAR, NOT NULL)
  - `status` (VARCHAR, NOT NULL) – one of `PENDING`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`
  - `created_at` (TIMESTAMP WITH TIME ZONE, NOT NULL) – set automatically on insert
  - `updated_at` (TIMESTAMP WITH TIME ZONE, NOT NULL) – updated automatically on update
  - `total_amount` (DECIMAL(19,2), NOT NULL)

- **`order_items`**
  - `id` (BIGINT, PK, auto-increment)
  - `order_id` (BIGINT, FK → `orders.id`, NOT NULL, with cascade from `orders`)
  - `product_id` (VARCHAR, NOT NULL)
  - `product_name` (VARCHAR, NOT NULL)
  - `quantity` (INT, NOT NULL)
  - `unit_price` (DECIMAL(19,2), NOT NULL)
  - `total_price` (DECIMAL(19,2), NOT NULL)

Relationships:

- One `orders` row can have many `order_items` rows (`orders.id` → `order_items.order_id`).
- Deleting an order cascades to its items due to `cascade = CascadeType.ALL` and `orphanRemoval = true` on the JPA mapping.

## Implementation details

- **Domain & persistence**
  - `Order`, `OrderItem`, and `OrderStatus` in `domain` model the core entities.
  - `OrderRepository` (Spring Data JPA) provides CRUD operations and `findByStatus` for filtering by order status.

- **Service layer**
  - `OrderService` contains all business logic:
    - `createOrder` validates that at least one item is present, calculates `totalPrice` per item and `totalAmount` per order, sets initial status to `PENDING`, and persists the aggregate.
    - `getOrder` and `listOrders` fetch orders (optionally filtered by status).
    - `updateStatus` enforces valid transitions (no changes allowed from `CANCELLED`/`DELIVERED`, and `CANCELLED` must go through the dedicated cancel API).
    - `cancelOrder` only allows cancelling orders currently in `PENDING` status.
    - `autoPromotePendingToProcessing` finds all `PENDING` orders and moves them to `PROCESSING`, returning the number of updated rows.

- **Web/API layer**
  - `OrderController` exposes REST endpoints under `/api/orders`.
  - Request/response DTOs live under `web/dto`:
    - `CreateOrderRequest` and `OrderItemRequest` (with bean validation constraints) are used for incoming payloads.
    - `OrderResponse` and `OrderItemResponse` are used to return order data to clients.
  - `OrderMapper` converts entities to response DTOs, including nested items.

- **Error handling**
  - Custom exceptions `OrderNotFoundException` and `InvalidOrderStatusException` represent domain errors.
  - `GlobalExceptionHandler` (a `@RestControllerAdvice`) maps these to structured JSON with appropriate HTTP status codes and handles validation errors (`MethodArgumentNotValidException`) by returning field-level error messages.

- **Scheduling**
  - `OmsApplication` is annotated with `@EnableScheduling`.
  - `OrderStatusScheduler` has a `@Scheduled(fixedRate = 5 * 60 * 1000)` method that calls `OrderService.autoPromotePendingToProcessing()` and logs how many orders were updated.

## Running tests

Execute all unit and integration tests with:

```bash
mvn test
```

