# RohithMart — Multi-Seller E-Commerce Marketplace

[![build-and-test](https://github.com/your-username/rohithmart/actions/workflows/build.yml/badge.svg)](https://github.com/your-username/rohithmart/actions/workflows/build.yml)

> Java Semester 3 Capstone Project — Anna University R2025  
> **Author**: Rohithraj S.K. (`com.rohith.rohithmart`)  
> **Platform**: Java Servlets · JSP + JSTL · JDBC · HikariCP · H2 Database · Apache Tomcat 9.0.x

---

## 1. Problem Statement
Traditional monolithic single-vendor online stores lack the extensibility and vendor isolation needed for multi-tenant retail. **RohithMart** is a comprehensive, production-grade multi-seller e-commerce marketplace web application. It enables:
- **Sellers** to onboard, list, catalog, update inventory, and fulfill customer orders.
- **Buyers** to search, filter by category, manage a persistent shopping cart, checkout via simulated payment confirmation, review purchases, and converse with an AI assistant.
- **Administrators** to oversee users, enforce marketplace policies, review listings, and inspect all platform transactions.

---

## 2. System Architecture

```
Browser (HTML5 / Classical CSS / Vanilla JS Fetch)
       │ HTTP Requests
       ▼
Filter Layer (EncodingFilter ➔ LoggingFilter [MDC Request-ID] ➔ AuthFilter [RBAC])
       │
       ▼
Front Controller / Servlets (HomeServlet, ProductServlet, CartServlet, CheckoutServlet, etc.)
       │ (Thin, HTTP orchestration only)
       ▼
Service Layer (AuthService, ProductService, CartService, OrderService, ReviewService, ChatService)
       │ (Business rules, validations, transaction management)
       ▼
DAO Layer (UserDAO, ProductDAO, OrderDAO, CartItemDAO, ReviewDAO)
       │ (PreparedStatement only, try-with-resources)
       ▼
Connection Pool (HikariCP initialized via AppContextListener)
       │
       ▼
H2 Database (TCP Server Mode / In-Memory Embedded Mode)
```

---

## 3. Technology Stack

| Component | Specification |
|---|---|
| **JDK** | 17 (LTS) |
| **Servlet Container** | Apache Tomcat 9.0.x (`javax.servlet.*`) |
| **Build Tool** | Apache Maven 3.8+ |
| **Database** | H2 Database (Server mode & Embedded) |
| **Connection Pool** | HikariCP (`com.zaxxer:HikariCP:5.1.0`) |
| **View Layer** | JSP 2.3 + JSTL 1.2 + Vanilla JS |
| **JSON Serialization**| Google Gson (`com.google.code.gson:gson:2.10.1`) |
| **Password Hashing** | jBCrypt (`org.mindrot:jbcrypt:0.4`) |
| **Logging** | SLF4J 2.0.x + Logback Classic (with MDC Request ID) |
| **Testing** | JUnit 5 + Mockito 5 (Embedded H2 tests) |
| **CI / CD** | GitHub Actions (`.github/workflows/build.yml`) |

---

## 4. Quick Setup & Local Execution

### Prerequisites
- JDK 17 installed and configured in `JAVA_HOME`
- Maven 3.8+ installed
- Apache Tomcat 9.0.x

### Build & Run
```bash
# 1. Clone repository
git clone https://github.com/your-username/rohithmart.git
cd rohithmart

# 2. Build and run unit / DAO tests
mvn clean verify

# 3. Package WAR file
mvn clean package
```
Deploy `target/rohithmart.war` to Tomcat's `webapps/` folder and navigate to `http://localhost:8080/rohithmart`.

---

## 5. Seed Accounts

| Role | Email | Default Password |
|---|---|---|
| **Admin** | `admin@rohithmart.com` | `Password@123` |
| **Seller** | `seller@rohithmart.com` | `Password@123` |
| **Buyer** | `buyer@rohithmart.com` | `Password@123` |

---

## 6. Architecture & Design Diagrams
All design diagrams are version-controlled in `docs/diagrams/`:
- **D1**: [ER Diagram](docs/diagrams/D1_ER_Diagram.puml)
- **D2**: [Use Case Diagram](docs/diagrams/D2_UseCase_Diagram.puml)
- **D3**: [Sequence Diagram (Place-Order Flow)](docs/diagrams/D3_Sequence_Diagram.puml)

---

## 7. Security & Compliance
- PreparedStatements strictly enforced throughout all DAO queries.
- Passwords hashed using standard BCrypt (`jBCrypt`).
- Session fixation protection using `req.changeSessionId()` upon login.
- Output escaped via JSTL `<c:out>` and `fn:escapeXml`.
- Custom error pages in `web.xml` conceal stack traces from end users.
