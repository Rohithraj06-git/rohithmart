# RohithMart — Multi-Seller E-Commerce Marketplace

[![Build and Test](https://github.com/rohithraj/rohithmart/actions/workflows/build.yml/badge.svg)](https://github.com/rohithraj/rohithmart/actions/workflows/build.yml)
[![Java Version](https://img.shields.io/badge/Java-17%20LTS-orange.svg)](https://openjdk.org/projects/jdk/17/)
[![Servlet API](https://img.shields.io/badge/Servlet-4.0%20(Java%20EE)-blue.svg)](https://javaee.github.io/servlet-spec/)
[![Server](https://img.shields.io/badge/Apache%20Tomcat-9.0.x-green.svg)](https://tomcat.apache.org/)
[![Database](https://img.shields.io/badge/H2-2.2.224-blue.svg)](https://www.h2database.com/)
[![License](https://img.shields.io/badge/License-Academic-lightgrey.svg)](LICENSE)

> **Anna University R2025 Semester 3 Capstone Project**  
> **Author**: Rohithraj S.K. (`com.rohith.rohithmart`)  
> **Architecture**: Strict MVC Layered Web Architecture (`HTTP` → `Filter` → `Servlet` → `Service` → `DAO` → `H2/HikariCP`)  
> **Frontend**: Semantic HTML5 · Classical Elegant CSS3 · Vanilla JavaScript (No Bootstrap/Tailwind/jQuery bloat)

---

## Table of Contents
1. [Project Overview](#1-project-overview)
2. [Key Features](#2-key-features)
3. [Technology Stack](#3-technology-stack)
4. [System Architecture](#4-system-architecture)
5. [Database Setup & Schema](#5-database-setup--schema)
6. [Local Environment Setup](#6-local-environment-setup)
7. [Apache Tomcat 9 Setup & Deployment](#7-apache-tomcat-9-setup--deployment)
8. [Maven Build & Test Commands](#8-maven-build--test-commands)
9. [REST API Specification](#9-rest-api-specification)
10. [Default Seed Credentials](#10-default-seed-credentials)
11. [Design & Architecture Diagrams](#11-design--architecture-diagrams)
12. [Security Controls](#12-security-controls)

---

## 1. Project Overview

**RohithMart** is a production-grade multi-seller e-commerce marketplace built following modern enterprise software craftsmanship standards. Unlike monolithic single-vendor online storefronts, RohithMart provides isolated vendor workspaces, multi-tenant product cataloging, transactional inventory reservation, server-authoritative checkout, customer reviews, administrative platform moderation, and an integrated AI shopping assistant.

---

## 2. Key Features

### For Buyers:
- **Product Discovery**: Multi-criteria search, sidebar category filtering, minimum customer rating filter, price range bounds, and server-side sorting.
- **Product Details & Media**: Gallery thumbnail switcher, real-time warehouse inventory indicator, and verified customer reviews.
- **Shopping Cart**: Dynamic quantity stepper with server-side stock validation, item removal with accessible modal dialog, and server-authoritative total calculations.
- **Atomic Checkout**: 10-step transactional checkout (price reload, stock deduction, order record generation, and cart clearance) with mock payment options (Credit/Debit Card, Cash on Delivery).
- **Order Tracking & Cancellation**: Itemized order history, lifecycle status tags (`PENDING`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED`), and single-click cancellation for pending orders.
- **Customer Reviews**: 1–5 star rating picker, verified review submission, and duplicate review prevention.
- **AI Shopping Assistant**: Session-cached, rate-limited natural language conversational assistant for product recommendations.

### For Sellers:
- **Seller Dashboard**: Visual statistics cards for Total Listings, Active Orders, Gross Revenue, and Low-Stock Alert Counts.
- **Low-Stock Alert Banner**: Real-time notifications for items nearing depletion (`stock <= 5`).
- **Product Catalog Management**: Create, edit, and delete listings with modal confirmation dialogs.
- **Order Fulfillment**: Track customer purchases containing seller items and transition order fulfillment status (`CONFIRMED` → `SHIPPED` → `DELIVERED`).
- **IDOR Protection**: Strict authorization guardrails preventing sellers from editing other sellers' listings or updating other sellers' orders.

### For Administrators:
- **Administrative Portal**: Platform-wide metrics (Total Users, Buyer/Seller breakdown, Active Listings, Total Orders, Gross Platform Volume).
- **Order Status Distribution Chart**: Visual, accessible horizontal CSS distribution bar chart showing orders by lifecycle status.
- **User Management**: Search user accounts and promote/change roles (`BUYER` ↔ `SELLER`).
- **Content Moderation**: Administrative override capability to review and remove inappropriate product listings platform-wide.

---

## 3. Technology Stack

| Layer | Technology | Details |
|---|---|---|
| **JDK** | Java 17 LTS (Eclipse Temurin / OpenJDK) | Records, Switch Expressions, Streams, java.time |
| **Servlet Container** | Apache Tomcat 9.0.x | Java Servlet 4.0 Specification (`javax.servlet.*`) |
| **Build & Dependency Management** | Apache Maven 3.8+ | Maven Compiler, Surefire, WAR, Checkstyle, SpotBugs |
| **Database** | H2 Database 2.2.224 | Embedded Mode & TCP Server Mode with AUTO_SERVER=TRUE |
| **Connection Pooling** | HikariCP 5.1.0 | Lifecycle managed via `ServletContextListener` |
| **Database Migrations** | Custom Migration Engine | Versioned SQL scripts (`db/migrations/V1..V3`) |
| **View Layer** | JSP 2.3 + JSTL 1.2 | Semantic HTML5, CSS Variables, Vanilla JS |
| **JSON API Serialization** | Google Gson 2.10.1 | Envelope `ApiResponse<T>`, custom type adapters |
| **Security & Hashing** | jBCrypt 0.4 | Salted BCrypt password hashing, session fixation defense |
| **Structured Logging** | SLF4J 2.0.x + Logback Classic 1.4.x | MDC Request ID correlation tracking |
| **Testing** | JUnit 5.10.x + Mockito 5.11.x | 172 automated unit, DAO, service, filter, and E2E tests |
| **CI / CD** | GitHub Actions | `.github/workflows/build.yml` running `mvn -B clean verify` |

---

## 4. System Architecture

RohithMart enforces strict separation of concerns through an enterprise layered MVC architecture:

```
Browser Client / REST Consumers
       │
       ▼
Filter Pipeline (EncodingFilter ➔ SecurityHeadersFilter ➔ LoggingFilter [MDC] ➔ CsrfFilter ➔ AuthFilter [RBAC])
       │
       ▼
Controller Layer (Thin Servlets: ProductServlet, CartServlet, CheckoutServlet, BuyerOrderServlet, SellerDashboardServlet, AdminDashboardServlet, REST APIs)
       │
       ▼
Service Layer (AuthService, ProductService, CartService, OrderService, ReviewService, UserService, ChatService)
       │  [Business Validation, Role Enforcement, Atomic Database Transactions]
       ▼
DAO Layer (UserDAO, ProductDAO, CartItemDAO, OrderDAO, OrderItemDAO, ReviewDAO)
       │  [Parameterized PreparedStatements only, Try-with-resources, Zero connection leaks]
       ▼
Connection Pool (HikariCP 5.1.0 owned exclusively by AppContextListener)
       │
       ▼
H2 Relational Database (Normalized Schema, Foreign Keys, Indexes, Check Constraints)
```

---

## 5. Database Setup & Schema

RohithMart uses an automated, zero-configuration database migration engine (`DatabaseMigrationManager`) that executes upon application startup:

1. **Schema Initialization** (`src/main/resources/db/migrations/V1__init_schema.sql`):
   - `users`: `id`, `name`, `email` (UNIQUE), `password_hash`, `role` (CHECK), `created_at`
   - `products`: `id`, `seller_id` (FK), `name`, `description`, `price` (DECIMAL 10,2), `stock_qty` (CHECK >= 0), `category`, `image_url`, `created_at`
   - `orders`: `id`, `buyer_id` (FK), `status` (CHECK), `total_amount` (DECIMAL 10,2), `created_at`
   - `order_items`: `id`, `order_id` (FK CASCADE), `product_id` (FK RESTRICT), `quantity` (CHECK > 0), `unit_price`, `created_at`
   - `cart_items`: `id`, `user_id` (FK CASCADE), `product_id` (FK CASCADE), `quantity`, `created_at`, UNIQUE(`user_id`, `product_id`)
   - `reviews`: `id`, `product_id` (FK CASCADE), `user_id` (FK CASCADE), `rating` (CHECK 1..5), `comment`, `created_at`, UNIQUE(`user_id`, `product_id`)
2. **Migrations** (`V2__add_reviews_table.sql`, `V3__add_index_orders_status.sql`).
3. **Seed Data** (`src/main/resources/db/seed.sql`): Automatically applied if the `users` table is empty.

---

## 6. Local Environment Setup

### Prerequisites
- **JDK 17** installed (`java -version`)
- **Maven 3.8+** installed (`mvn -v`)
- **Git**

### Clone & Build
```bash
# Clone the repository
git clone https://github.com/your-username/rohithmart.git
cd rohithmart

# Run complete test verification and static analysis
mvn clean verify

# Package production WAR archive
mvn clean package
```

The compiled WAR archive will be located at `target/rohithmart.war`.

---

## 7. Apache Tomcat 9 Setup & Deployment

1. Download and extract **Apache Tomcat 9.0.x** (e.g. `9.0.86`) from [tomcat.apache.org](https://tomcat.apache.org/download-90.cgi).
2. Set environment variable `JAVA_HOME` pointing to your JDK 17 installation.
3. Copy `target/rohithmart.war` into Tomcat's `webapps/` folder:
   ```bash
   # Windows PowerShell
   Copy-Item target\rohithmart.war C:\apache-tomcat-9.0.x\webapps\

   # Linux / macOS
   cp target/rohithmart.war /opt/tomcat/webapps/
   ```
4. Start Apache Tomcat:
   ```bash
   # Windows
   C:\apache-tomcat-9.0.x\bin\startup.bat

   # Linux / macOS
   /opt/tomcat/bin/startup.sh
   ```
5. Open your web browser and navigate to:
   ```
   http://localhost:8080/rohithmart
   ```

---

## 8. Maven Build & Test Commands

| Command | Action |
|---|---|
| `mvn clean compile` | Compiles all main Java sources to `target/classes` |
| `mvn clean test` | Executes all 172 JUnit 5 and Mockito unit & integration tests |
| `mvn test -Dtest=MarketplaceFlowsEndToEndTest` | Runs the full Buyer, Seller, and Admin end-to-end integration test suite |
| `mvn clean verify` | Runs compilation, Checkstyle analysis, tests, and packaging verification |
| `mvn clean package` | Assembles the production-ready `target/rohithmart.war` web archive |

---

## 9. REST API Specification

All REST API endpoints serialize standardized JSON envelopes using Google Gson:

### Standard Response Formats

**Success Response (HTTP 200 / 201):**
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... }
}
```

**Error Response (HTTP 400, 401, 403, 404, 409, 500):**
```json
{
  "success": false,
  "message": "Detailed error explanation",
  "errorCode": "VALIDATION_ERROR"
}
```

### Core API Endpoints

| Method | URI | Description | Auth / Role |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register new user account | Public |
| `POST` | `/api/auth/login` | Authenticate and regenerate session ID | Public |
| `POST` | `/api/auth/logout` | Invalidate session | Authenticated |
| `GET` | `/api/auth/me` | Fetch sanitized profile of logged-in user | Authenticated |
| `GET` | `/api/products` | Browse catalog with `page`, `size`, `category`, `q`, `sort`, `minPrice`, `maxPrice`, `minRating` | Public |
| `GET` | `/api/products/{id}` | Retrieve product details | Public |
| `POST` | `/api/products` | Create product listing | Seller / Admin |
| `PUT` | `/api/products/{id}` | Update product listing | Seller Owner / Admin |
| `DELETE` | `/api/products/{id}` | Delete product listing | Seller Owner / Admin |
| `GET` | `/api/cart` | Retrieve buyer cart with authoritative total | Buyer |
| `POST` | `/api/cart/add` | Add product to cart with inventory check | Buyer |
| `POST` | `/api/cart/update` | Update item quantity with stock validation | Buyer |
| `POST` | `/api/cart/remove` | Remove item from cart | Buyer |
| `DELETE` | `/api/cart` | Clear entire cart | Buyer |
| `GET` | `/api/orders` | List authenticated buyer's order history | Buyer |
| `GET` | `/api/orders/{id}` | Retrieve order details | Buyer Owner / Admin |
| `POST` | `/api/orders` | Execute 10-step atomic checkout transaction | Buyer |
| `POST` | `/api/orders/{id}/cancel` | Cancel order (permitted only if `PENDING`) | Buyer Owner |
| `GET` | `/api/seller/products` | List seller's own product listings | Seller / Admin |
| `GET` | `/api/seller/orders` | List orders containing seller's items | Seller / Admin |
| `POST` | `/api/seller/orders/{id}/status` | Update fulfillment status (`CONFIRMED`, `SHIPPED`, `DELIVERED`) | Seller Owner / Admin |
| `GET` | `/api/admin/users` | List all registered users | Admin |
| `GET` | `/api/admin/stats` | Platform overview statistics | Admin |
| `POST` | `/api/admin/users/{id}/role` | Update user role | Admin |
| `POST` | `/api/chat` | AI Shopping Assistant conversation | Public / Buyer |

---

## 10. Default Seed Credentials

For evaluation and testing, the application seeds pre-configured demo accounts. All seed accounts use the default password: **`Password@123`**

| Role | Name | Email | Permissions |
|---|---|---|---|
| **Administrator** | System Administrator | `admin@rohithmart.com` | User management, platform analytics, listing moderation |
| **Seller 1** | Apex Tech Solutions | `techseller@rohithmart.com` | Electronics seller dashboard, inventory CRUD, order tracking |
| **Seller 2** | Heritage Apparel & Living | `fashionseller@rohithmart.com` | Fashion & Home seller dashboard, inventory CRUD |
| **Buyer 1** | John Buyer | `john.buyer@example.com` | Catalog browsing, persistent cart, checkout, orders, reviews |
| **Buyer 2** | Alice Smith | `alice.smith@example.com` | Additional buyer account |

---

## 11. Design & Architecture Diagrams

Complete architectural diagrams are located in [`docs/diagrams/`](docs/diagrams/):
1. **[Entity-Relationship Diagram](docs/diagrams/D1_ER_Diagram.puml)**: Database tables, primary/foreign keys, cardinality, and constraints.
2. **[Use Case Diagram](docs/diagrams/D2_UseCase_Diagram.puml)**: Actor boundaries across Guest, Buyer, Seller, and Administrator.
3. **[Place Order Sequence Diagram](docs/diagrams/D3_Sequence_Diagram.puml)**: 10-step transactional checkout workflow across Filter, Servlet, Service, DAO, and Database.
4. **[Rendered Markdown Diagrams](docs/diagrams/DIAGRAMS.md)**: Native GitHub-compatible Mermaid diagrams for immediate visual inspection.

---

## 12. Security Controls

- **SQL Injection Defense**: 100% of database queries use parameterized `PreparedStatement` with `?` placeholders. Dynamic `ORDER BY` sorting is strictly whitelisted against safe literals.
- **XSS Defense**: Strict HTML escaping via JSTL `<c:out>` and `fn:escapeXml`. Unobtrusive DOM listeners using `data-*` attributes eliminate quotation breakout.
- **CSRF Protection**: State-modifying requests (`POST`, `PUT`, `DELETE`) require cryptographically random CSRF tokens validated by `CsrfFilter`.
- **Session Fixation Defense**: `req.changeSessionId()` is invoked upon successful authentication. Sessions are completely invalidated on logout.
- **Insecure Direct Object Reference (IDOR)**: Enforced ownership checks in Service and Servlet layers prevent sellers from editing foreign products or modifying foreign orders.
- **Password Protection**: Passwords hashed using industry-standard BCrypt (`jBCrypt`). Zero passwords or hashes exposed in DTOs or logs.
- **Defense in Depth**: `SecurityHeadersFilter` injects `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `X-XSS-Protection: 1; mode=block`, and strict `Content-Security-Policy`.
