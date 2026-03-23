# PowerSync — Building Management System

A full-stack Building Management System built with **Spring Boot 4**, providing landlords with tools to manage properties, rooms, and tenants, and enabling **IoT-based power monitoring** and **automated billing** per room. Tenants are onboarded via a secure email invitation flow.

---

## ✨ Features

- **JWT Authentication** — Stateless token-based auth using HMAC-SHA512 signed JWTs delivered via `HttpOnly` secure cookies and validated on every request by a custom filter.
- **Google OAuth2 / OIDC Login** — Full OpenID Connect integration with Google. Tenants can complete onboarding via Google if they were invited with a matching email address.
- **Local Registration & Login** — Classic email/password flow with BCrypt (strength 12) password encoding and Bean Validation on inputs.
- **Role-Based Access Control** — Two roles: `LANDLORD` and `TENANT`. Apartment and room management endpoints are restricted to `LANDLORD`; tenant-facing endpoints enforce `TENANT` access.
- **Apartment Management** — Landlords can create, view, update, and delete apartments. Each apartment can contain multiple rooms.
- **Room Management** — Landlords can add, update, and remove rooms within their apartments. Each room tracks an optional current tenant.
- **Tenant Invitation System** — Landlords invite prospective tenants by email. A unique invite link (UUID code, 48-hour expiry) is sent automatically. Tenants register via the invite link and are linked to their room upon completion.
- **IoT Device Tracking** — Each room can have a single IoT device (identified by a serial number) that reports its status (`ON` / `OFF`) and unit rate per kWh.
- **Power Metrics** — IoT devices push periodic readings (voltage, amperes, wattage, cumulative units consumed) stored as timestamped `PowerMetric` records.
- **Billing** — Bills are generated per tenant per room, covering a billing period with a calculated total amount and a payment status (`UNPAID`, `PAID`, `OVERDUE`).
- **Global Exception Handling** — A single `@RestControllerAdvice` maps validation errors → 400, duplicate users → 409, not-found → 404, and unhandled exceptions → 500, all as structured JSON.
- **Stateless REST Security** — CSRF disabled, sessions never created; the filter chain is fully stateless and API-friendly.
- **Auto Schema Management** — Hibernate `ddl-auto=update` keeps the MySQL schema in sync with your entities during development.
- **Async Email Dispatch** — Invitation emails are sent asynchronously via Spring Mail (Gmail SMTP) to avoid blocking the request thread.

---

## 🏗️ Architecture Overview

```
com.example.BuildingManagement/
├── auth/                   # AuthController  – /api/v1/auth/** public endpoints
├── apartment/              # Apartment entity, repo, service, controller
├── room/                   # Room entity, repo, service, controller
├── invitation/             # Invitation entity, repo, service, controller
├── device/                 # IotDevice entity (power meter per room)
├── power/                  # PowerMetric entity (periodic IoT readings)
├── bill/                   # Bill entity (tenant billing records)
├── user/                   # User entity, repo, service, controller, security
└── common/
    ├── config/             # SecurityConfig  – filter chain & OAuth2 setup
    ├── enums/              # UserRole, DeviceStatus, PaymentStatus, InvitationStatus
    ├── exception/          # GlobalExceptionHandler, ErrorResponse, custom exceptions
    └── security/
        ├── jwt/            # JwtUtils, JwtAuthFilter
        └── oauth/          # CustomOidcUserService, OAuthSuccessHandler
```

### Domain Model

| Entity | Key Fields |
|--------|-----------|
| `User` | `id`, `fullname`, `email`, `contactNo`, `passwordHash`, `role` (`LANDLORD`/`TENANT`), `landlord` (self-ref FK), `provider`, `isActive` |
| `Apartment` | `id`, `name`, `address`, `landlord` (FK → User), `rooms`, `createdAt` |
| `Room` | `id`, `roomNumber`, `floorNo`, `apartment` (FK), `currentTenant` (FK → User), `createdAt` |
| `IotDevice` | `id`, `deviceSerial`, `room` (FK), `status` (`ON`/`OFF`), `unitRatePerKwh` |
| `PowerMetric` | `id`, `iotDevice` (FK), `voltage`, `amperes`, `wattage`, `unitsConsumedTotal`, `recordedAt` |
| `Bill` | `id`, `tenant` (FK → User), `room` (FK), `billingPeriodStart`, `billingPeriodEnd`, `totalAmount`, `paymentStatus` |
| `Invitation` | `id`, `email`, `inviteCode` (UUID), `status` (`PENDING`/`ACCEPTED`/`EXPIRED`), `room` (FK), `landlord` (FK), `expiresAt` |

### Authentication & Onboarding Flows

| Flow | Steps |
|------|-------|
| **Landlord Register** | `POST /api/v1/auth/register` → validate → BCrypt encode → persist as `LANDLORD` → 200 OK |
| **Login** | `POST /api/v1/auth/login` → authenticate → issue JWT cookie → 200 OK |
| **Google OAuth2** | `/oauth2/authorization/google` → Google OIDC → auto-create / link user → generate JWT cookie → redirect |
| **Invite Tenant** | `POST /api/v1/invitations/send` (LANDLORD) → create `Invitation` → send email with invite link |
| **Verify Invite** | `GET /api/v1/auth/verify?code=<uuid>` → validate code → return pre-fill data (email, landlord name, room number) |
| **Tenant Registration** | `POST /api/v1/auth/register-tenant` → validate invite → create `TENANT` user → link to room → mark invite `ACCEPTED` |
| **Protected request** | JWT cookie → `JwtAuthFilter` validates → load `UserPrincipal` → proceed |

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 4.0.2 |
| Security | Spring Security 6, JJWT 0.11.5 |
| OAuth2 | Spring OAuth2 Client (Google OIDC) |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL 8 |
| Email | Spring Mail (JavaMailSender, Gmail SMTP) |
| Utilities | Lombok, Bean Validation |
| Build | Maven |
| Java | 17 |

---

## ⚙️ Configuration

Most sensitive values are read from **environment variables** — never hard-code secrets.

| Variable | Description |
|----------|-------------|
| `DB_URL` | JDBC URL, e.g. `jdbc:mysql://localhost:3306/springs` |
| `DB_USERNAME` | MySQL username |
| `DB_PASSWORD` | MySQL password |
| `JWT_SECRET` | Base64-encoded HMAC-SHA512 secret key |
| `GOOGLE_CLIENT_ID` | Google OAuth2 client ID |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 client secret |
| `GOOGLE_CLIENT_SCOPE` | OAuth2 scopes, e.g. `openid,email,profile` |

Email credentials (`spring.mail.username` / `spring.mail.password`) are currently set directly in `application.properties`; move them to environment variables or a secrets manager before deploying to production.

---

## 🚀 How to Run

### Prerequisites
- Java 17+
- MySQL 8 database (create the schema; Hibernate will create the tables automatically)
- A Google OAuth2 application with the redirect URI `http://localhost:8081/login/oauth2/code/google` *(development only — use HTTPS in production)*
- A Gmail account (or other SMTP provider) for sending tenant invitation emails

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/paraskamdar700/PowerSync.git
   cd PowerSync
   ```

2. **Set environment variables** (or export them in your shell / IDE run config):
   ```bash
   export DB_URL=jdbc:mysql://localhost:3306/springs
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

### Auth — Public (`/api/v1/auth/**`)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/auth/` | Health-check / welcome |
| `POST` | `/api/v1/auth/register` | Register a new landlord |
| `POST` | `/api/v1/auth/login` | Login and receive a JWT cookie |
| `GET` | `/api/v1/auth/verify?code=<uuid>` | Validate an invitation code and return pre-fill data |
| `POST` | `/api/v1/auth/register-tenant` | Complete tenant onboarding via invite code |

### Apartments — LANDLORD only (`/api/v1/properties/apartments`)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/properties/apartments` | Create a new apartment |
| `GET` | `/api/v1/properties/apartments/my-apartments` | List all apartments owned by the authenticated landlord |
| `PUT` | `/api/v1/properties/apartments/{id}` | Update an apartment |
| `DELETE` | `/api/v1/properties/apartments/{id}` | Delete an apartment and all its rooms |

### Rooms — LANDLORD only (`/api/v1/properties/apartments/{apartmentId}/rooms`)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/properties/apartments/{apartmentId}/rooms` | Add a room to an apartment |
| `GET` | `/api/v1/properties/apartments/{apartmentId}/rooms` | List all rooms in an apartment |
| `GET` | `/api/v1/properties/apartments/{apartmentId}/rooms/{roomId}` | Get a specific room |
| `PUT` | `/api/v1/properties/apartments/{apartmentId}/rooms/{roomId}` | Update a room |
| `DELETE` | `/api/v1/properties/apartments/{apartmentId}/rooms/{roomId}` | Delete a room |

### Invitations — LANDLORD only (`/api/v1/invitations`)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/invitations/send` | Send an email invitation to a prospective tenant for a specific room |

### User — Authenticated (`/api/v1/user`)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/user/profile` | Authenticated user profile |
| `GET` | `/api/v1/user/dashboard` | Authenticated user dashboard |

### OAuth2

| Path | Description |
|------|-------------|
| `/oauth2/authorization/google` | Initiate Google login |
| `/login/oauth2/code/google` | Google OAuth2 callback (handled internally) |

---

## 🔐 Security Notes

- JWT tokens use **HMAC-SHA512**; keep `JWT_SECRET` long and random.
- Passwords are hashed with **BCrypt (cost 12)** — never stored in plaintext. OAuth2 users have no password.
- Cookies are `HttpOnly` and `Secure` to prevent XSS token theft.
- Tenant accounts are always linked to a specific landlord via invitation — self-registration as a tenant is not possible.
- Invitation codes expire after **48 hours** (hardcoded in `InvitationServiceIMPL`) and can only be used once.
- For production, replace `ddl-auto=update` with `validate` and manage schema with a migration tool (e.g. Flyway).
- Move all credentials (DB password, mail password, OAuth2 secrets) out of `application.properties` and into environment variables or a secrets manager.
