# Engineering Retrospectives

This document records key lessons, engineering decisions, challenges, and solutions across all phases of the **RohithMart** project.

---

## Phase 1: Architecture, Skeleton & Database Modeling

- **What Worked Well**:
  - Adhering to the Anna University R2025 Capstone specification from day one ensured clean layered package design (`controller`, `service`, `dao`, `model`, `dto`, `filter`, `listener`, `util`).
  - Utilizing H2 embedded mode with `AUTO_SERVER=TRUE` allowed simultaneous application execution and external database inspection without external DBMS dependencies.
  - Designing a custom migration runner (`DatabaseMigrationManager`) prevented schema divergence between local runs and CI pipelines.
- **Challenges & Solutions**:
  - *Challenge*: Balancing Java 17 LTS language features with legacy Servlet 4.0 / Tomcat 9 container conventions.
  - *Solution*: Set Maven compiler source/target/release to 17 while targeting standard `javax.servlet.*` interfaces.
- **Key Takeaway**: Establishing strict architectural boundaries early prevents downstream refactoring debt.

---

## Phase 2: DAO Layer & Connection Pool Management

- **What Worked Well**:
  - Consolidating the HikariCP connection pool lifecycle into `AppContextListener` guaranteed zero connection starvation and proper resource reclamation on context destruction.
  - Using try-with-resources across every JDBC call eliminated connection and cursor leaks.
- **Challenges & Solutions**:
  - *Challenge*: Dynamic search filtering with variable parameters.
  - *Solution*: Built parameterized `StringBuilder` queries paired with a `List<Object>` parameter list, completely avoiding string concatenation and SQL injection vulnerabilities.
- **Key Takeaway**: Never compromise on parameterized queries, even for search filters.

---

## Phase 3: Service Layer & Transactional Boundaries

- **What Worked Well**:
  - Centralizing business rules and transactional checkout boundaries inside `OrderServiceImpl` kept servlets thin and declarative.
  - The 10-step atomic checkout algorithm (verifying stock, reloading prices, updating inventory atomically, creating order items, and clearing cart) was proven solid against edge cases.
- **Challenges & Solutions**:
  - *Challenge*: Preventing race conditions in inventory deduction under concurrent purchases.
  - *Solution*: Implemented atomic conditional updates in SQL (`UPDATE products SET stock_qty = stock_qty - ? WHERE id = ? AND stock_qty >= ?`) ensuring the database engine guarantees non-negative inventory.
- **Key Takeaway**: Database-level atomic conditional updates are more reliable than memory-only synchronized locks in multi-threaded web applications.

---

## Phase 4: Authentication, Authorization & Security Hardening

- **What Worked Well**:
  - Password hashing with salted BCrypt (`jBCrypt`) and session fixation defense via `req.changeSessionId()` upon login.
  - Custom `CsrfFilter` verifying cryptographically random session tokens for state-modifying requests.
- **Challenges & Solutions**:
  - *Challenge*: Insecure Direct Object References (IDOR) where a seller might manipulate another seller's products or orders.
  - *Solution*: Added rigorous seller ownership checks in `ProductService` and `OrderService`. Any attempt by an unauthorized seller to update another seller's product or change the status of an unrelated order immediately throws a `ForbiddenException`.
- **Key Takeaway**: Authorization checks must always be validated at both the presentation and domain service levels.

---

## Phase 5: Controller & Servlet Layer

- **What Worked Well**:
  - Creating thin Servlets delegating to Services kept controllers easy to test with Mockito.
  - Standardizing all REST API envelopes via `ApiResponse<T>` with standard HTTP status codes (200, 201, 400, 401, 403, 404, 409, 500) gave the frontend predictable response semantics.
- **Challenges & Solutions**:
  - *Challenge*: Exception handling leaking stack traces to API consumers.
  - *Solution*: Implemented centralized exception-to-response mapping in `BaseServlet.handleException`, masking internal server errors into safe generic messages while logging details to Logback.
- **Key Takeaway**: Standardized error envelopes save enormous amounts of client-side error handling code.

---

## Phase 6: Frontend UI & Main Business Flows

- **What Worked Well**:
  - Designing a bespoke classical CSS system (`main.css`) without bloated CSS frameworks (Bootstrap/Tailwind) ensured lightning-fast load times, semantic HTML5, and strict visual restraint (no neon/3D/glowing effects).
  - Vanilla JavaScript for AJAX interactions provided clean, responsive behavior with zero framework overhead.
- **Challenges & Solutions**:
  - *Challenge*: Untrusted client calculations for shopping cart and checkout totals.
  - *Solution*: Strictly enforced server-authoritative calculations. The frontend never computes prices; it renders authoritative values returned by the backend.
- **Key Takeaway**: Simple, well-spaced typography and accessible color palettes create a more professional, premium feel than heavy animation libraries.

---

## Phase 7: QA Audit & Performance Optimization

- **What Worked Well**:
  - Comprehensive end-to-end integration testing (`MarketplaceFlowsEndToEndTest`) executed all user personas (Buyer, Seller, Admin) in a single realistic lifecycle.
  - Identifying and resolving the N+1 query pattern in product catalog retrieval by introducing `ReviewDAO.getRatingStatsForProducts` reduced database queries per page from 41 to 1.
- **Final Metrics**:
  - **172 tests executed with 100% pass rate**.
  - **Zero compiler warnings or test failures**.
  - **Clean WAR deployment verified**.
