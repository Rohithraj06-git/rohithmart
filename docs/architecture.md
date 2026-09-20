# RohithMart Architecture Specification

## 1. High-Level Architectural Pattern
RohithMart is architected strictly adhering to the **Layered Model-View-Controller (MVC) over Java Servlets** pattern, following the Anna University R2025 Semester 3 Capstone Project specification.

```
Browser (HTML5, Classical CSS, Vanilla JS Fetch)
    │
    ▼
HTTP Request
    │
    ▼
Filter Layer (EncodingFilter -> LoggingFilter with MDC UUID -> AuthFilter RBAC)
    │
    ▼
Controller Layer (Thin Servlets: HomeServlet, ProductServlet, CartServlet, CheckoutServlet, etc.)
    │
    ▼
Service Layer (Business rules, validations, orchestrations, transaction demarcations)
    │
    ▼
DAO Layer (ProductDAO, OrderDAO, UserDAO, CartItemDAO, ReviewDAO - PreparedStatements only)
    │
    ▼
Connection Pool (HikariCP managed strictly by AppContextListener)
    │
    ▼
H2 Database (Embedded for tests / TCP Server mode for deployment)
```

## 2. Core Architectural Rules
1. **Thin Controllers**: Servlets are strictly restricted to HTTP orchestration, session binding, DTO parsing, and delegating to services. No SQL or domain rules exist in servlets.
2. **Business Rules in Service**: Validation, authorization checks, and calculations (totals, rate-limits) are contained entirely within the `service` package.
3. **PreparedStatements Only**: Under no circumstance are SQL strings concatenated. Every query in the DAO layer uses `PreparedStatement` with parameterized placeholders.
4. **Lifecycle Ownership**: Connection pool (`HikariDataSource`) lifecycle is owned solely by `AppContextListener`. No `DriverManager.getConnection()` is executed outside this listener.
5. **DTO Separation**: Data Transfer Objects (e.g. `UserResponseDTO`, `ProductDTO`) are strictly decoupled from DB entity models. Sensitive values (`password_hash`) are never exposed in DTOs.
6. **Transactional Checkout**: Order placement, order item creation, stock reservation, and cart clearance occur within an atomic database transaction.
