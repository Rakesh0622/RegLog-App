# RegLog — Full-Stack Authentication

RegLog is a complete registration/login application built from two independent projects:

- **`reglog-frontend`** — React (Vite) frontend
- **`reglog-backendapp`** — Spring Boot backend (single app with two services: User + Authentication)

Passwords are stored as **BCrypt hashes**, sessions use a **signed JWT** delivered through an **HTTP-only cookie**, and JWT metadata is persisted in MySQL.

```
                 ┌───────────────┐
                 │ React Signup  │ ─────────► POST /api/users/register
                 └───────────────┘                    │
                                                      ▼
                                           User Service (BCrypt hash)
                                                      │
                                                      ▼
                                                     MySQL
                                          ┌───────────────────────┐
                                          │  users / jwt_tokens   │
                                          └───────────────────────┘
                 ┌──────────────┐
                 │ React Login  │ ─────────► POST /api/auth/login
                 └──────────────┘                    │
                                                      ▼
                                           Authentication Service
                                           (verify BCrypt → sign JWT
                                            → store jwt_tokens row
                                            → set HTTP-only cookie)
                                                      │
                                                      ▼
                                                 React /home
                                                      │
                                          GET /api/auth/me (JWT cookie)
                                                      │
                                                      ▼
                                            JwtAuthenticationFilter
                                            (signature + expiry + DB check)
```

---

## 1. Project overview

| Project | Tech | Purpose |
| --- | --- | --- |
| `reglog-frontend` | React 18, Vite, React Router, Axios | Signup / Login / Home UI |
| `reglog-backendapp` | Java 17, Spring Boot 3.5, Spring Security, Spring Data JPA, MySQL, JWT (jjwt), Maven | REST API, BCrypt, JWT cookie auth |

---

## 2. Technologies used

- Java 17+, Spring Boot 3.5.x, Spring Web, Spring Data JPA, Spring Security, Bean Validation
- MySQL 8 (localhost:3306, database `reglog`, tables `users` and `jwt_tokens`)
- BCrypt (`BCryptPasswordEncoder`)
- JWT via `io.jsonwebtoken` (jjwt)
- React 18 + Vite + React Router 6 + Axios

---

## 3. Project structure

```text
RegLog/
├── database/
│   └── schema.sql                      # optional manual DB setup
├── reglog-backendapp/
│   ├── pom.xml
│   ├── mvnw.cmd                        # Maven wrapper (Windows)
│   └── src/main/
│       ├── resources/application.properties
│       └── java/com/reglog/
│           ├── ReglogBackendApplication.java
│           ├── user/                   # User Service
│           │   ├── entity/User.java
│           │   ├── repository/UserRepository.java
│           │   ├── service/UserService.java
│           │   └── controller/UserController.java
│           ├── authentication/         # Authentication Service
│           │   ├── entity/JwtToken.java
│           │   ├── repository/JwtTokenRepository.java
│           │   ├── service/AuthenticationService.java
│           │   └── controller/AuthenticationController.java
│           ├── security/               # JWT + Spring Security
│           │   ├── JwtService.java
│           │   ├── JwtAuthenticationFilter.java
│           │   ├── CookieUtil.java
│           │   └── SecurityConfig.java
│           ├── dto/                    # RegisterRequest, LoginRequest, UserResponse, ApiResponse
│           └── exception/              # custom exceptions + GlobalExceptionHandler
└── reglog-frontend/
    ├── package.json
    ├── index.html
    ├── vite.config.js
    └── src/
        ├── main.jsx
        ├── App.jsx                     # routes
        ├── api/axios.js                # axios instance (baseURL + withCredentials)
        ├── styles/index.css
        └── pages/
            ├── signup.jsx
            ├── login.jsx
            └── home.jsx
```

---

## 4. MySQL setup

MySQL must run on `localhost:3306`.

**Option A — automatic:** start the backend. The JDBC URL includes
`createDatabaseIfNotExist=true` and `spring.jpa.hibernate.ddl-auto=update` creates
the `users` and `jwt_tokens` tables on first run.

**Option B — manual:** run `database/schema.sql` (or execute from a MySQL client):

```sql
CREATE DATABASE IF NOT EXISTS reglog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 5. Database tables

### `users`

| column | type | notes |
| --- | --- | --- |
| `id` | BIGINT PK AUTO_INCREMENT | |
| `name` | VARCHAR(100) NOT NULL UNIQUE | username |
| `password` | VARCHAR(255) NOT NULL | **BCrypt hash**, never plaintext |
| `email` | VARCHAR(150) NOT NULL UNIQUE | |
| `phone` | VARCHAR(20) NOT NULL | |

### `jwt_tokens`   (users 1 ──── M jwt_tokens, via `uid → users.id`)

| column | type | notes |
| --- | --- | --- |
| `tid` | BIGINT PK AUTO_INCREMENT | token id |
| `uid` | BIGINT NOT NULL | FK → `users(id)` |
| `token` | TEXT NOT NULL | the JWT string |
| `cat` | TIMESTAMP NOT NULL | created-at |
| `eat` | TIMESTAMP NOT NULL | expiry-at |

---

## 6. Backend setup

Requirements: **JDK 17+**, Maven wrapper (no global Maven needed).

```bash
cd reglog-backendapp
```

Set the environment variables (see below), then:

```bash
.\mvnw.cmd spring-boot:run
```

Backend runs at **http://localhost:8080**.

## 7. Frontend setup

Requirements: **Node.js 18+**.

```bash
cd reglog-frontend
npm install
npm run dev
```

Frontend runs at **http://localhost:5173**.

---

## 8. Environment variables

Create these (any shell) before starting the backend:

| Variable | Default | Purpose |
| --- | --- | --- |
| `MYSQL_PASSWORD` | `YOUR_MYSQL_PASSWORD` (placeholder) | MySQL `root` password. **Must be set.** |
| `JWT_SECRET` | dev-only fallback string | HMAC-SHA signing key, **≥ 32 bytes**. Set a strong random value in production. |
| `JWT_EXPIRATION_MS` | `3600000` (1 h) | JWT lifetime in ms. |
| `SECURE_COOKIE` | `false` | Set `true` when serving over HTTPS. |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Comma-separated allowed frontend origins. |

PowerShell example:

```powershell
$env:MYSQL_PASSWORD  = "your-mysql-password"
$env:JWT_SECRET      = "a-very-long-random-secret-at-least-32-bytes-long!"
```

> The JWT secret and DB password are never hardcoded in Java source. Only
> development-only fallbacks exist in `application.properties`.

---

## 9. How to run the backend

```bash
cd reglog-backendapp
$env:MYSQL_PASSWORD = "your-mysql-password"      # PowerShell
.\mvnw.cmd spring-boot:run
```

## 10. How to run the frontend

```bash
cd reglog-frontend
npm install
npm run dev
```

Open **http://localhost:5173** — you are redirected to `/login`.

---

## 11. API endpoints

| Method | Path | Auth | Body / Notes | Success |
| --- | --- | --- | --- | --- |
| POST | `/api/users/register` | public | `{name, password, email, phone}` | 201 |
| GET | `/api/users/{name}` | JWT cookie | view user by username | 200 |
| POST | `/api/auth/login` | public | `{name, password}` → sets `JWT` cookie | 200 |
| GET | `/api/auth/me` | JWT cookie | currently authenticated user | 200 |
| POST | `/api/auth/logout` | JWT cookie | invalidates JWT row + clears cookie | 200 |

All responses use a consistent envelope:

```json
{ "success": true, "message": "Login successful", "data": { "id":1, "name":"Rakesh", "email":"rakesh@gmail.com", "phone":"9876543210" } }
```

### Error responses

| Code | Example message |
| --- | --- |
| 400 | `Invalid input`, `Email already exists` validation errors |
| 401 | `Invalid username or password`, `Authentication required` |
| 404 | `User not found` |
| 409 | `Username already exists` / `Email already exists` |
| 500 | `Internal server error` |

---

## 12. Authentication flow

1. **Signup** — `signup.jsx` POSTs to `/api/users/register`. `UserService` validates,
   checks duplicate username/email, BCrypt-encodes the password, saves the user, and
   returns a `UserResponse` (never the password). Frontend redirects to `/login` with
   a success message.
2. **Login** — `login.jsx` POSTs to `/api/auth/login`. `AuthenticationService` finds the
   user by name, verifies the raw password with `BCryptPasswordEncoder.matches` (never a
   plaintext comparison), signs a JWT, writes a row into `jwt_tokens` with
   `uid/token/cat/eat`, and sets an HTTP-only cookie. Frontend redirects to `/home`.
3. **Home** — `home.jsx` calls `/api/auth/me`. The browser attaches the cookie
   automatically. `JwtAuthenticationFilter` validates signature + expiry and confirms the
   token still exists in the DB, then places the username in the SecurityContext.
4. **Logout** — `home.jsx` POSTs `/api/auth/logout`, which deletes the JWT row and
   clears the cookie. Frontend redirects to `/login`.

---

## 13. JWT cookie flow

- Cookie name: **`JWT`**, value: signed token.
- Attributes: `HttpOnly`, `Path=/`, `SameSite=Lax`, `Max-Age` aligned with the JWT
  expiry, `Secure` configurable via `SECURE_COOKIE` (default `false` for local dev).
- The token is never exposed to JavaScript; the frontend relies on the browser sending
  the cookie automatically (Axios configured with `withCredentials: true`).
- Protected endpoints reject missing, expired, malformed, or revoked (DB-deleted) tokens.

---

## 14. CORS

Backend allow-lists `http://localhost:5173` (configurable via `CORS_ALLOWED_ORIGINS`).
`allowCredentials=true` is set and a wildcard origin is **not** used, so cookie-based
auth works from the dev server.

---

## 15. Troubleshooting

**`Access denied for user 'root'@'localhost'`** — set `MYSQL_PASSWORD` correctly before starting.

**`Unable to determine Dialect without JDBC metadata`** — the backend could not reach
MySQL. Confirm MySQL is running on port 3306 and the database exists.

**Login works but `/home` bounces back to `/login`** — the JWT cookie is missing or the
expiry cookie mismatches the token. Check `JWT_EXPIRATION_MS` and that the browser
accepts cookies for `localhost`. If the backend was restarted with a different
`JWT_SECRET`, old cookies become invalid — clear cookies and log in again.

**CORS error in the browser console** — ensure you only access the app via
`http://localhost:5173` and that `CORS_ALLOWED_ORIGINS` includes that exact origin
(no trailing slash). Credentials cannot be combined with `*` origins.

**Passwords stored in plain text?** — they should look like
`$2a$10$...` BCrypt hashes. If not, the `PasswordEncoder` bean is not the BCrypt one.

**Cookie not sent** — every Axios call in this project uses the shared instance from
`src/api/axios.js`, which sets `withCredentials: true`. Do not create new `axios`
instances for logins.

**Port already in use** — change `server.port` in `application.properties` (or set
`SERVER_PORT`), then update `baseURL` in `src/api/axios.js` and
`CORS_ALLOWED_ORIGINS` accordingly.