# Security Checklist (Capstone Section 9)

- [x] **Every query parameterized**: Verified across all DAO implementations. Every SQL query utilizes `PreparedStatement` with bind variables (`?`). String concatenation is strictly prohibited.
- [x] **Passwords bcrypt-hashed, never logged**: Passwords are encrypted with salt rounds via `jBCrypt` (`PasswordUtil`). No plaintexts are logged or persisted.
- [x] **RBAC and Session enforcement**: `AuthFilter` protects `/buyer/*`, `/seller/*`, `/admin/*`, `/checkout`, and protected API routes. Session fixation is prevented via `req.changeSessionId()` on authentication.
- [x] **Output escaping**: User-supplied input rendered in JSPs uses JSTL `<c:out>` and `fn:escapeXml` to prevent Cross-Site Scripting (XSS).
- [x] **Custom Error Pages**: Configured in `web.xml` for 400, 401, 403, 404, and 500 error codes. Internal exception stack traces are never exposed to the client.
- [x] **Secrets & Credentials Management**: Sensitive credentials, database connection strings, and `.env` files are excluded via `.gitignore`. `.env.example` provides reproducible templates.
