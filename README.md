# Spring Boot JWT + OAuth2 Authentication Template

A production-ready, reusable Spring Boot authentication starter that combines **JWT-based stateless security** with **Google OAuth2 / OpenID Connect login**. Use it as a solid foundation for any REST API that needs secure authentication out of the box.

---

## ✨ Features

- **JWT Authentication** — Stateless token-based auth using HMAC-SHA512 signed JWTs with a 2-hour expiration. Tokens are delivered via `HttpOnly` secure cookies and validated on every request by a custom filter.
- **Google OAuth2 / OIDC Login** — Full OpenID Connect integration with Google. On first login, the user is automatically created in the database; subsequent logins are recognised by provider ID.
- **Local Registration & Login** — Classic email/password flow with BCrypt (strength 12) password encoding and Bean Validation on inputs.
- **Provider Conflict Detection** — Users who registered locally cannot log in via OAuth2 (and vice versa), preventing account hijacking.
- **Role-Based Access Control** — `roles` field on the `User` entity ready for `@PreAuthorize` / security rule expansion.
- **Global Exception Handling** — A single `@RestControllerAdvice` maps validation errors → 400, duplicate users → 409, not-found → 404, and unhandled exceptions → 500, all as structured JSON.
- **Stateless REST Security** — CSRF disabled, sessions never created; the filter chain is fully stateless and API-friendly.
- **Auto Schema Management** — Hibernate `ddl-auto=update` keeps the MySQL schema in sync with your entities during development.

---

## 🏗️ Architecture Overview

```
com.example.BuildingManagement/
├── auth/                   # AuthController  – /auth/** public endpoints
├── config/                 # SecurityConfig  – filter chain & OAuth2 setup
├── common/                 # ErrorResponse   – shared error DTO
├── exception/              # GlobalExceptionHandler + custom exceptions
├── security/
│   ├── jwt/                # JwtUtils, JwtAuthFilter
│   └── oauth/              # CustomOidcUserService, OAuthSuccessHandler
└── user/                   # User entity, repo, service, controllers
```

### Authentication Flows

| Flow | Steps |
|------|-------|
| **Register** | `POST /auth/register` → validate → BCrypt encode → persist → 200 OK |
| **Login** | `POST /auth/login` → authenticate → issue JWT cookie → 200 OK |
| **Google OAuth2** | `GET /login/oauth2/…` → Google OIDC → auto-create user → generate JWT → redirect to `/auth/?token=<jwt>` |
| **Protected request** | `Bearer <token>` header → `JwtAuthFilter` validates → load `UserPrincipal` → proceed |

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 4.x |
| Security | Spring Security 6, JJWT 0.11.5 |
| OAuth2 | Spring OAuth2 Client (Google OIDC) |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL 8 |
| Utilities | Lombok, Bean Validation |
| Build | Maven |
| Java | 17 |

---

## ⚙️ Configuration

All sensitive values are read from **environment variables** — never hard-code secrets.

| Variable | Description |
|----------|-------------|
| `DB_URL` | JDBC URL, e.g. `jdbc:mysql://localhost:3306/mydb` |
| `DB_USERNAME` | MySQL username |
| `DB_PASSWORD` | MySQL password |
| `JWT_SECRET` | Base64-encoded HMAC-SHA512 secret key |
| `GOOGLE_CLIENT_ID` | Google OAuth2 client ID |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 client secret |
| `GOOGLE_CLIENT_SCOPE` | OAuth2 scopes, e.g. `openid,email,profile` |

---

## 🚀 How to Run

### Prerequisites
- Java 17+
- MySQL 8 database (create the schema; Hibernate will create the tables automatically)
- A Google OAuth2 application with the redirect URI `http://localhost:8081/login/oauth2/code/google` *(development only — use HTTPS in production)*

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/paraskamdar700/spring-jwt-oauth2-template.git
   cd spring-jwt-oauth2-template
   ```

2. **Set environment variables** (or export them in your shell / IDE run config):
   ```bash
   export DB_URL=jdbc:mysql://localhost:3306/auth_db
   export DB_USERNAME=root
   export DB_PASSWORD=secret
   export JWT_SECRET=<your-base64-secret>
   export GOOGLE_CLIENT_ID=<your-google-client-id>
   export GOOGLE_CLIENT_SECRET=<your-google-client-secret>
   export GOOGLE_CLIENT_SCOPE=openid,email,profile
   ```

3. **Build and run**
   ```bash
   ./mvnw spring-boot:run
   ```
   The server starts on **port 8081**.

---

## 📡 API Endpoints

### Public (`/auth/**`)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/auth/register` | Register a new local user |
| `POST` | `/auth/login` | Login and receive a JWT cookie |
| `GET` | `/auth/` | Health-check / welcome |

### Protected (`/api/**`) — requires `Authorization: Bearer <token>`

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/user/profile` | Authenticated user profile |
| `GET` | `/api/user/dashboard` | Authenticated user dashboard |

### OAuth2

| Path | Description |
|------|-------------|
| `/oauth2/authorization/google` | Initiate Google login |
| `/login/oauth2/code/google` | Google OAuth2 callback (handled internally) |

---

## 🔐 Security Notes

- JWT tokens use **HMAC-SHA512**; keep `JWT_SECRET` long and random.
- Passwords are hashed with **BCrypt (cost 12)** — never stored in plaintext.
- Cookies are `HttpOnly` and `Secure` to prevent XSS token theft.
- Mixing local and OAuth2 accounts for the same email is explicitly blocked.
- For production, replace `ddl-auto=update` with `validate` and manage schema with a migration tool (e.g. Flyway).
