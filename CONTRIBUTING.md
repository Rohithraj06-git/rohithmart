# Contributing to RohithMart

Thank you for your interest in contributing to **RohithMart**! This document provides detailed guidelines for local setup, architectural conventions, coding standards, and testing requirements.

---

## 1. Development Prerequisites

- **Java Development Kit (JDK)**: Version 17 LTS (OpenJDK, Eclipse Temurin, or Amazon Corretto).
- **Build Tool**: Apache Maven 3.8.0 or higher.
- **Servlet Container**: Apache Tomcat 9.0.x (`javax.servlet.*` compliant).
- **IDE**: IntelliJ IDEA, Eclipse, or Visual Studio Code with Java extension pack.

---

## 2. Environment Setup

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/rohithraj/rohithmart.git
   cd rohithmart
   ```

2. **Database Configuration**:
   - By default, the application runs against an embedded H2 database located at `./data/rohithmart`.
   - Automated migrations execute on startup from `src/main/resources/db/migrations/`.
   - Seed data is loaded automatically from `src/main/resources/db/seed.sql` on first boot.

3. **Verify Build & Tests**:
   ```bash
   mvn clean verify
   ```

---

## 3. Architectural Rules & Standards

To preserve the integrity of the codebase, all contributions must adhere to the following principles:

### A. Layered MVC Boundaries
- **Filters**: Handle cross-cutting concerns only (encoding, security headers, logging MDC, CSRF, authentication).
- **Servlets**: Must remain thin. Extract request parameters, validate basic format, call the service layer, and serialize JSON or forward to JSP. Never execute SQL queries or business logic in Servlets.
- **Services**: Own all business logic, authorization rules, and transactional boundaries.
- **DAOs**: Use `PreparedStatement` exclusively. Every connection, statement, and result set must use try-with-resources. Direct calls to `DriverManager.getConnection()` are prohibited; use `DatabaseUtil.getConnection()`.

### B. Security & Data Validation
- **SQL Injection**: Never concatenate strings into SQL queries. Dynamic `ORDER BY` clauses must be strictly whitelisted.
- **XSS**: Always escape untrusted output in JSPs using JSTL `<c:out value="..."/>` or `fn:escapeXml`. Avoid embedding raw strings into inline JavaScript handlers; use HTML5 `data-*` attributes.
- **CSRF**: All state-modifying requests (`POST`, `PUT`, `DELETE`) must include a valid CSRF token.
- **Authoritative Server Pricing**: Never trust client-side prices or totals. All calculations must be performed on the server.
- **Passwords**: Never log, expose, or return raw passwords or password hashes in DTOs or logs.

---

## 4. Testing Requirements

- Every new service method or DAO query must be accompanied by comprehensive unit or integration tests.
- All tests must pass cleanly before submitting changes:
  ```bash
  mvn test
  ```
- To run the complete multi-flow end-to-end audit test:
  ```bash
  mvn test -Dtest=MarketplaceFlowsEndToEndTest
  ```
- Static analysis checks (Checkstyle) must pass with zero violations:
  ```bash
  mvn checkstyle:check
  ```

---

## 5. Pull Request Checklist

Before submitting a pull request, ensure that:
- [ ] Code compiles without warnings (`mvn clean compile`).
- [ ] All 172+ unit and integration tests pass (`mvn test`).
- [ ] The production WAR packages cleanly (`mvn clean package`).
- [ ] No temporary files, logs, or IDE metadata are committed.
- [ ] No secrets, passwords, or API keys are committed.
- [ ] Documentation and diagrams are updated if functional changes were introduced.
