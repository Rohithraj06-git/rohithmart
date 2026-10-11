# Changelog

All notable changes to the **RohithMart** multi-seller marketplace project are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-09-20 (Production Release & Audit Finalization)

### Added
- Comprehensive multi-flow end-to-end integration test suite (`MarketplaceFlowsEndToEndTest`) verifying 10 sequential stages across Buyer, Seller, and Administrator journeys.
- Bulk rating statistics aggregation method (`ReviewDAO.getRatingStatsForProducts`) to eliminate N+1 database queries during catalog browsing and search.
- Administrator override permissions for `ProductService.updateProduct` and `ProductService.deleteProduct` allowing platform content moderation.
- Automated CSRF protection filter (`CsrfFilter`) verifying tokens on all state-modifying requests (`POST`, `PUT`, `DELETE`).
- Native Mermaid architectural diagrams (`docs/diagrams/DIAGRAMS.md`) for ER model, Use Case boundaries, Place Order sequence flow, and Layered Architecture.

### Changed
- Refactored `ProductServiceImpl.toDTOList` to batch-load average ratings and review counts in a single query, reducing catalog DB roundtrips from 41 to 1.
- Enhanced `OrderServiceImpl.updateOrderStatus` with seller inventory verification (IDOR protection), rejecting status modifications on orders from unrelated sellers.
- Upgraded `SellerDashboardServlet.showEditForm` with seller ownership verification to prevent cross-seller edit view disclosures.
- Expanded `AuthFilter` URL patterns in `web.xml` to cover `/cart`, `/cart/*`, and `/orders/*`.
- Replaced inline single-quoted JavaScript event handlers in `seller/manage-products.jsp` and `admin/listings.jsp` with unobtrusive DOM event listeners and `data-*` attributes.

### Fixed
- Fixed IDOR vulnerability where non-admin sellers could manipulate the order status of orders containing items from other vendors.
- Fixed potential quote-escaping syntax issues in product deletion modal dialog triggers.

---

## [0.6.0] - 2026-09-20 (Frontend UI & Main Business Flows)

### Added
- Classical, elegant design system in `static/css/main.css` adhering to strict typography, spacing tokens, accessible color contrasts, and subtle transitions (no neon, glowing, or heavy glassmorphism).
- Product Catalog view with sidebar multi-criteria filtering, price ranges, rating filters, sorting, and server-side pagination.
- Product Details view with interactive thumbnail gallery switcher, stepper quantity selector, verified reviews list, and review submission form.
- Shopping Cart with dynamic quantity steppers, server-authoritative calculations, and accessible modal confirmation for deletions.
- Checkout flow with customer information, address validation, mock payment methods (Credit/Debit Card, Cash on Delivery), and loading spinner protection against double submission.
- Buyer Order History with itemized breakdowns and single-click cancellation for pending orders.
- Seller Portal with dashboard metric cards, low-stock alerts (`stock <= 5`), inventory management table, and order fulfillment status updates.
- Admin Portal with platform metrics, order status distribution chart, and user role management.

---

## [0.5.0] - 2026-09-20 (Controller & Servlet Layer)

### Added
- Implemented thin Servlets (`AuthApiServlet`, `ProductApiServlet`, `CartApiServlet`, `OrderApiServlet`, `ReviewApiServlet`, `SellerApiServlet`, `AdminApiServlet`, `ChatServlet`).
- Standardized REST JSON envelopes (`ApiResponse<T>`) with correct HTTP status codes (200, 201, 400, 401, 403, 404, 409, 500).
- Centralized exception-to-response mapping in `BaseServlet.handleException` masking internal server errors.
- Session fixation defense (`req.changeSessionId()`) integrated upon login.

---

## [0.4.0] - 2026-09-20 (Security & Interceptor Infrastructure)

### Added
- `EncodingFilter` enforcing UTF-8 request and response encoding.
- `SecurityHeadersFilter` injecting `X-Content-Type-Options`, `X-Frame-Options`, `X-XSS-Protection`, and `Content-Security-Policy`.
- `LoggingFilter` injecting MDC request correlation IDs for end-to-end request tracing.
- `AuthFilter` enforcing Role-Based Access Control (RBAC) across `BUYER`, `SELLER`, and `ADMIN`.

---

## [0.3.0] - 2026-09-20 (Service Layer & Transactional Checkout)

### Added
- `AuthService` with salted BCrypt password hashing and verification.
- `ProductService` with catalog management, input validation, and search filtering.
- `CartService` with server-side stock validation and authoritative total calculation.
- `OrderService` with 10-step atomic checkout transaction executing inventory deduction and rollback on failure.
- `ReviewService` with verified review eligibility and duplicate review prevention.
- `UserService` with profile management and administrative role updating.
- `ChatService` with session caching and rate-limiting (10 requests/minute).

---

## [0.2.0] - 2026-09-20 (DAO Layer & Database Persistence)

### Added
- Pure JDBC Data Access Objects: `UserDAO`, `ProductDAO`, `CartItemDAO`, `OrderDAO`, `OrderItemDAO`, `ReviewDAO`.
- `AppContextListener` managing HikariCP connection pool lifecycle and automated database migrations.
- 100% parameterized `PreparedStatement` queries with try-with-resources.

---

## [0.1.0] - 2026-09-20 (Initial Architecture & Skeleton)

### Added
- Initial project structure, Maven `pom.xml`, Java 17 configuration.
- Database schema scripts (`schema.sql`, `seed.sql`) and Flyway-style migrations (`V1__init_schema.sql`).
- Architecture documentation and PlantUML diagrams (`D1`, `D2`, `D3`).
