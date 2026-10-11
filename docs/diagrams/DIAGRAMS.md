# System Diagrams

This document contains native GitHub-rendered Mermaid diagrams matching the actual implementation of **RohithMart**.

---

## 1. Entity-Relationship (ER) Diagram

```mermaid
erDiagram
    users ||--o{ products : "sells"
    users ||--o{ orders : "places"
    users ||--o{ cart_items : "owns"
    users ||--o{ reviews : "writes"
    products ||--o{ order_items : "referenced in"
    products ||--o{ cart_items : "contained in"
    products ||--o{ reviews : "reviewed in"
    orders ||--|{ order_items : "composed of"

    users {
        BIGINT id PK "auto_increment"
        VARCHAR name "NOT NULL"
        VARCHAR email "UNIQUE, NOT NULL"
        VARCHAR password_hash "NOT NULL (BCrypt)"
        VARCHAR role "BUYER, SELLER, ADMIN"
        TIMESTAMP created_at "CURRENT_TIMESTAMP"
    }

    products {
        BIGINT id PK "auto_increment"
        BIGINT seller_id FK "REFERENCES users(id)"
        VARCHAR name "NOT NULL"
        CLOB description "Nullable"
        DECIMAL price "NOT NULL, >= 0"
        INT stock_qty "NOT NULL, >= 0"
        VARCHAR category "NOT NULL"
        VARCHAR image_url "Nullable"
        TIMESTAMP created_at "CURRENT_TIMESTAMP"
    }

    orders {
        BIGINT id PK "auto_increment"
        BIGINT buyer_id FK "REFERENCES users(id)"
        VARCHAR status "PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED"
        DECIMAL total_amount "NOT NULL, >= 0"
        TIMESTAMP created_at "CURRENT_TIMESTAMP"
    }

    order_items {
        BIGINT id PK "auto_increment"
        BIGINT order_id FK "REFERENCES orders(id) ON DELETE CASCADE"
        BIGINT product_id FK "REFERENCES products(id) ON DELETE RESTRICT"
        INT quantity "NOT NULL, > 0"
        DECIMAL unit_price "NOT NULL, >= 0"
        TIMESTAMP created_at "CURRENT_TIMESTAMP"
    }

    cart_items {
        BIGINT id PK "auto_increment"
        BIGINT user_id FK "REFERENCES users(id) ON DELETE CASCADE"
        BIGINT product_id FK "REFERENCES products(id) ON DELETE CASCADE"
        INT quantity "NOT NULL, > 0"
        TIMESTAMP created_at "CURRENT_TIMESTAMP"
    }

    reviews {
        BIGINT id PK "auto_increment"
        BIGINT product_id FK "REFERENCES products(id) ON DELETE CASCADE"
        BIGINT user_id FK "REFERENCES users(id) ON DELETE CASCADE"
        INT rating "NOT NULL, 1..5"
        CLOB comment "Nullable"
        TIMESTAMP created_at "CURRENT_TIMESTAMP"
    }
```

---

## 2. Use Case Diagram

```mermaid
flowchart LR
    subgraph Actors
        Guest["Guest / Visitor"]
        Buyer["Buyer (Authenticated)"]
        Seller["Seller (Authenticated)"]
        Admin["Administrator"]
    end

    subgraph "RohithMart System"
        UC1["Register & Sign In"]
        UC2["Browse, Search & Filter Catalog"]
        UC3["Ask AI Shopping Assistant"]
        UC4["Manage Persistent Cart"]
        UC5["Execute Atomic Checkout"]
        UC6["Track Order History & Cancel Order"]
        UC7["Submit Product Reviews & Star Ratings"]
        UC8["Manage Product Listings (CRUD)"]
        UC9["View Incoming Orders & Update Status"]
        UC10["Seller Analytics & Low-Stock Alerts"]
        UC11["Admin Platform Metrics & Stats"]
        UC12["User Role Management"]
        UC13["Moderate & Remove Listings"]
    end

    Guest --> UC1
    Guest --> UC2
    Guest --> UC3

    Buyer --> UC1
    Buyer --> UC2
    Buyer --> UC3
    Buyer --> UC4
    Buyer --> UC5
    Buyer --> UC6
    Buyer --> UC7

    Seller --> UC1
    Seller --> UC8
    Seller --> UC9
    Seller --> UC10

    Admin --> UC1
    Admin --> UC11
    Admin --> UC12
    Admin --> UC13
```

---

## 3. Place Order Sequence Diagram (10-Step Transactional Checkout)

```mermaid
sequenceDiagram
    autonumber
    actor Buyer as Buyer
    participant Browser as Browser (JSP/JS)
    participant Filter as CsrfFilter & AuthFilter
    participant Servlet as CheckoutServlet
    participant OrderService as OrderService
    participant CartDAO as CartItemDAO
    participant ProductDAO as ProductDAO
    participant OrderDAO as OrderDAO
    participant DB as H2 Database (HikariCP)

    Buyer->>Browser: Clicks "Confirm & Place Order"
    Browser->>Filter: POST /checkout/place (_csrf, Session Cookie)
    Filter->>Filter: Verify active BUYER session & CSRF token
    Filter->>Servlet: Forward authenticated request
    Servlet->>OrderService: checkout(buyerId)

    Note over OrderService,DB: 1. Load Cart & Validate Inventory
    OrderService->>CartDAO: findByUserId(buyerId)
    CartDAO->>DB: SELECT * FROM cart_items WHERE user_id = ?
    DB-->>CartDAO: cartItems
    CartDAO-->>OrderService: cartItems

    loop For each cart item
        OrderService->>ProductDAO: findById(productId)
        ProductDAO->>DB: SELECT * FROM products WHERE id = ?
        DB-->>ProductDAO: product
        ProductDAO-->>OrderService: product (reload current price & stock)
    end

    Note over OrderService,DB: 2. Begin Atomic Database Transaction
    OrderService->>DB: Connection conn = ds.getConnection(); conn.setAutoCommit(false)

    Note over OrderService,DB: 3. Create Order Header
    OrderService->>OrderDAO: createOrder(conn, order)
    OrderDAO->>DB: INSERT INTO orders (buyer_id, status, total_amount) VALUES (...)
    DB-->>OrderDAO: generated orderId

    Note over OrderService,DB: 4. Create Order Items & Deduct Inventory
    loop For each cart item
        OrderService->>OrderDAO: createOrderItem(conn, item)
        OrderDAO->>DB: INSERT INTO order_items (...) VALUES (...)
        OrderService->>ProductDAO: deductStock(conn, productId, qty)
        ProductDAO->>DB: UPDATE products SET stock_qty = stock_qty - ? WHERE id = ? AND stock_qty >= ?
    end

    Note over OrderService,DB: 5. Clear Cart & Commit Transaction
    OrderService->>CartDAO: clearCart(conn, buyerId)
    CartDAO->>DB: DELETE FROM cart_items WHERE user_id = ?
    OrderService->>DB: conn.commit()
    OrderService-->>Servlet: OrderDTO (Created Order Details)

    Servlet-->>Browser: HTTP 302 Redirect to /buyer/orders
    Browser-->>Buyer: Render Order History with Confirmation Toast
```

---

## 4. Layered Architecture Diagram

```mermaid
flowchart TD
    subgraph Client["Presentation & Client Layer"]
        UI["JSP Views (JSTL 1.2, Semantic HTML5, CSS Variables, Vanilla JS)"]
        REST["REST API Consumers (JSON / Fetch)"]
    end

    subgraph Security["Filter & Interceptor Layer"]
        EF["EncodingFilter (UTF-8)"]
        SHF["SecurityHeadersFilter (CSP, X-Frame-Options, X-Content-Type)"]
        LF["LoggingFilter (MDC Correlation ID)"]
        CF["CsrfFilter (Token Verification on POST/PUT/DELETE)"]
        AF["AuthFilter (Session Verification & Role-Based Access Control)"]
    end

    subgraph Web["Controller / Servlet Layer"]
        AuthS["AuthServlet / AuthApiServlet"]
        ProdS["ProductServlet / ProductApiServlet"]
        CartS["CartServlet / CartApiServlet"]
        CheckS["CheckoutServlet"]
        OrderS["BuyerOrderServlet / OrderApiServlet"]
        SellS["SellerDashboardServlet / SellerApiServlet"]
        AdminS["AdminDashboardServlet / AdminApiServlet"]
        ChatS["ChatServlet"]
    end

    subgraph Service["Service Layer (Business Logic & Transactions)"]
        AuthService["AuthService (BCrypt verification, session security)"]
        ProductService["ProductService (Catalog, search, validation, batch stats)"]
        CartService["CartService (Stock validation, authoritative price calculation)"]
        OrderService["OrderService (10-step atomic checkout, order lifecycle)"]
        ReviewService["ReviewService (Eligibility checks, duplicate prevention)"]
        UserService["UserService (Profile updates, role management)"]
        ChatService["ChatService (Rate limiting, session caching, fallback)"]
    end

    subgraph DAO["Data Access Object (DAO) Layer"]
        UserDAO["UserDAO"]
        ProductDAO["ProductDAO"]
        CartDAO["CartItemDAO"]
        OrderDAO["OrderDAO & OrderItemDAO"]
        ReviewDAO["ReviewDAO"]
    end

    subgraph Persistence["Persistence & Connection Infrastructure"]
        CP["HikariCP Connection Pool (Owned by AppContextListener)"]
        DB[("H2 Database (AUTO_SERVER=TRUE / Embedded / In-Memory)")]
    end

    Client --> Security
    Security --> Web
    Web --> Service
    Service --> DAO
    DAO --> CP
    CP --> DB
```
