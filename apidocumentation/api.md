# PowerSync API Documentation

This document outlines the REST APIs for the PowerSync application.

## Authentication Overview

Most endpoints require authentication. The application uses JWT (JSON Web Tokens) for authentication. The `POST /api/v1/auth/login` endpoint issues an HttpOnly secure cookie containing the JWT, which should be included in subsequent requests.

Some APIs additionally require specific roles (e.g., `LANDLORD`).

---

## 1. Authentication API (`/api/v1/auth`)

### `GET /api/v1/auth/`
- **Description:** Index route for authentication.
- **Response:** `200 OK`
  ```text
  index welcome
  ```

### `POST /api/v1/auth/login`
- **Description:** Authenticates a user and sets a secure `jwt` cookie.
- **Request Body:**
  ```json
  {
    "email": "user@example.com",
    "passwordHash": "your_password"
  }
  ```
- **Response:** `200 OK`
  ```text
  Login successful
  ```
- **Headers:** `Set-Cookie: jwt=...; HttpOnly; Secure; Path=/; Max-Age=86400`

### `POST /api/v1/auth/register`
- **Description:** Register a new user.
- **Request Body:** User object
- **Response:** `200 OK` (Returns the created `User` object)

### `GET /api/v1/auth/verify`
- **Description:** Validates an invitation code (used for tenant onboarding).
- **Parameters:** `code` (Query param, e.g., `?code=ABCDEF`)
- **Response:** `200 OK`
  ```json
  {
    "email": "tenant@example.com",
    "landlordName": "John Doe",
    "roomNumber": "101"
  }
  ```

### `POST /api/v1/auth/register-tenant`
- **Description:** Completes the registration and onboarding process for an invited tenant.
- **Request Body:** `TenantRegistrationRequest` object
- **Response:** `200 OK`
  ```text
  Registration successful! You are now linked to your room.
  ```

### `GET /oauth2/authorization/google`
- **Description:** Triggers the Google OAuth2 login flow.
- **Usage:** Redirect the user's browser directly to this endpoint. They will authenticate with Google, and upon success, the application's `OAuthSuccessHandler` will automatically issue the HttpOnly `jwt` cookie.
- **Response:** `302 Redirect` to the Google login consent screen.

---

## 2. Apartment API (`/api/v1/properties/apartments`)
*These endpoints require the `LANDLORD` role.*

### `POST /api/v1/properties/apartments`
- **Description:** Create a new apartment/property.
- **Request Body:** `ApartmentRequest` object
- **Response:** `200 OK`
  ```json
  {
    "id": 1,
    "name": "Skyline PG",
    "address": "221B Baker St",
    "createdAt": "2026-04-21T22:39:09"
  }
  ```

### `GET /api/v1/properties/apartments/my-apartments`
- **Description:** Fetch all apartments owned by the currently authenticated landlord.
- **Response:** `200 OK`
  ```json
  [
    {
      "id": 1,
      "name": "Skyline PG",
      "address": "221B Baker St",
      "createdAt": "2026-04-21T22:39:09"
    }
  ]
  ```

### `PUT /api/v1/properties/apartments/{id}`
- **Description:** Update an existing apartment's details.
- **Path Variable:** `id` (Long) - Apartment ID
- **Request Body:** `ApartmentRequest` object
- **Response:** `200 OK`
  ```json
  {
    "id": 1,
    "name": "Skyline DLX",
    "address": "New Address",
    "createdAt": "2026-04-21T22:39:09"
  }
  ```

### `DELETE /api/v1/properties/apartments/{id}`
- **Description:** Delete an apartment and all its associated rooms.
- **Path Variable:** `id` (Long) - Apartment ID
- **Response:** `200 OK`
  ```text
  Apartment and all associated rooms deleted successfully
  ```

---

## 3. Room API (`/api/v1/properties/apartments/{apartmentId}/rooms`)
*These endpoints require the `LANDLORD` role.*

### `POST /api/v1/properties/apartments/{apartmentId}/rooms`
- **Description:** Add a new room to a specific apartment.
- **Path Variable:** `apartmentId` (Long)
- **Request Body:** `RoomRequest` object
- **Response:** `200 OK`
  ```json
  {
    "id": 2,
    "roomNumber": "102",
    "floorNo": 1,
    "createdAt": "2026-04-21T22:45:00",
    "currentTenant": null
  }
  ```

### `GET /api/v1/properties/apartments/{apartmentId}/rooms`
- **Description:** Get all rooms within a specific apartment.
- **Path Variable:** `apartmentId` (Long)
- **Response:** `200 OK`
  ```json
  [
    {
      "id": 1,
      "roomNumber": "101",
      "floorNo": 1,
      "createdAt": "2026-04-21T22:39:09",
      "currentTenant": {
        "id": 1,
        "fullname": "paras90",
        "email": "thisthis2@gmail.com",
        "contactNo": "7000164915",
        "role": "LANDLORD",
        "isActive": true
      }
    }
  ]
  ```

### `GET /api/v1/properties/apartments/{apartmentId}/rooms/{roomId}`
- **Description:** Get details of a specific room.
- **Path Variables:** `apartmentId` (Long), `roomId` (Long)
- **Response:** `200 OK`
  ```json
  {
    "id": 1,
    "roomNumber": "101",
    "floorNo": 1,
    "createdAt": "2026-04-21T22:39:09",
    "currentTenant": {
      "id": 1,
      "fullname": "paras90",
      "email": "thisthis2@gmail.com",
      "contactNo": "7000164915",
      "role": "LANDLORD",
      "isActive": true
    }
  }
  ```

### `PUT /api/v1/properties/apartments/{apartmentId}/rooms/{roomId}`
- **Description:** Update details of a specific room.
- **Path Variables:** `apartmentId` (Long), `roomId` (Long)
- **Request Body:** `RoomRequest` object
- **Response:** `200 OK`
  ```json
  {
    "id": 2,
    "roomNumber": "102",
    "floorNo": 2,
    "createdAt": "2026-04-21T22:45:00",
    "currentTenant": null
  }
  ```

### `DELETE /api/v1/properties/apartments/{apartmentId}/rooms/{roomId}`
- **Description:** Delete a specific room.
- **Path Variables:** `apartmentId` (Long), `roomId` (Long)
- **Response:** `200 OK`
  ```text
  Room deleted successfully within the context of Apartment {apartmentId}
  ```

---

## 4. User API (`/api/v1/user`)

### `GET /api/v1/user/profile`
- **Description:** Fetch the currently authenticated user's profile.
- **Response:** `200 OK`
  ```text
  This is protected profile data
  ```

### `GET /api/v1/user/dashboard`
- **Description:** Fetch the user's dashboard data.
- **Response:** `200 OK`
  ```text
  Protected dashboard
  ```

---

## 5. Invitation API (`/api/v1/invitations`)
*These endpoints require the `LANDLORD` role.*

### `POST /api/v1/invitations/send`
- **Description:** Send an invitation to a prospective tenant for a specific room.
- **Request Body:**
  ```json
  {
    "email": "tenant@example.com",
    "roomId": 1
  }
  ```
- **Response:** `200 OK`
  ```text
  Invitation sent successfully to {email}
  ```

---

## 6. IoT Telemetry API (`/esp`)

### `POST /esp`
- **Description:** Endpoint used by ESP/IoT microcontrollers to send real-time power metrics. The data is **persisted to the database** and automatically broadcasted to all connected frontend clients via WebSockets.
- **Authentication:** None (public — ESP has no JWT)
- **Request Body:** (Matches the `Power` object model)
  ```json
  {
    "nodeId": "Room_101",
    "voltage": 230.5,
    "current": 0.21,
    "power": 30.80,
    "energy": 0.08
  }
  ```
- **Response:** `200 OK`
  ```json
  {
    "status": "success",
    "message": "Telemetry received and broadcasted"
  }
  ```

### WebSocket: `ws://localhost:8081/ws/telemetry`
- **Description:** WebSocket endpoint for frontend dashboards to receive real-time telemetry data. Connect and listen for messages — every ESP POST triggers a broadcast.
- **Authentication:** None (public)
- **Message Format (received by client):**
  ```json
  {
    "nodeId": "Room_101",
    "voltage": 230.50,
    "current": 0.21,
    "power": 30.80,
    "energy": 0.08,
    "timestamp": "2026-04-21 20:02:22"
  }
  ```

---

## 7. Power Metrics API (`/api/v1/power`)
*These endpoints require authentication (JWT).*

### `GET /api/v1/power/room/{roomId}/latest`
- **Description:** Get the most recent power reading for a specific room.
- **Path Variable:** `roomId` (Long) — Room ID
- **Response:** `200 OK` (Returns `PowerMetric` object)
  ```json
  {
    "id": 142,
    "iotDevice": { "id": 1, "deviceSerial": "Room_101", ... },
    "voltage": 230.10,
    "amperes": 0.21,
    "wattage": 30.80,
    "unitsConsumedTotal": 0.08,
    "recordedAt": "2026-04-21T20:02:22"
  }
  ```

### `GET /api/v1/power/room/{roomId}/history`
- **Description:** Get historical power metrics for a room within a date range.
- **Path Variable:** `roomId` (Long)
- **Query Parameters:**
  - `startDate` (optional, `yyyy-MM-dd`) — defaults to 1st of current month
  - `endDate` (optional, `yyyy-MM-dd`) — defaults to today
- **Response:** `200 OK`
  ```json
  [
    {
      "id": 840,
      "amperes": 0.13,
      "voltage": 230.10,
      "wattage": 30.80,
      "unitsConsumedTotal": 0.08,
      "recordedAt": "2026-04-22T00:42:39.439141",
      "iotDevice": {
        "id": 1,
        "deviceSerial": "Room_101",
        "status": "ON",
        "unitRatePerKwh": 8.00,
        "room": {
          "id": 1,
          "roomNumber": "101",
          "floorNo": 1
          // ... (Nested room & tenant omitted for brevity)
        }
      }
    }
    // ... (additional historical logs)
  ]
  ```

### `GET /api/v1/power/room/{roomId}/usage`
- **Description:** Get cumulative energy usage for the current month for a room. Includes estimated cost.
- **Path Variable:** `roomId` (Long)
- **Response:** `200 OK`
  ```json
  {
    "roomId": 1,
    "deviceSerial": "Room_101",
    "unitsConsumed": 45.20,
    "startReading": 100.00,
    "latestReading": 145.20,
    "unitRate": 8.00,
    "estimatedCost": 361.60,
    "monthStart": "2026-04-01T00:00:00",
    "currentTime": "2026-04-21T21:00:00"
  }
  ```

---

## 8. Bills API (`/api/v1/bills`)
*These endpoints require authentication (JWT).*

### `GET /api/v1/bills/my-bills`
- **Description:** Tenant fetches their own bills (uses authenticated user's ID).
- **Response:** `200 OK` (Returns list of `Bill` objects, ordered by latest first)
  ```json
  [
    {
      "id": 4,
      "billingPeriodStart": "2026-04-21",
      "billingPeriodEnd": "2026-04-30",
      "unitsConsumed": 0.07,
      "unitRate": 8.00,
      "totalAmount": 0.56,
      "paymentStatus": "UNPAID",
      "createdAt": "2026-04-22T08:59:06.133554",
      "room": {
        "id": 1,
        "roomNumber": "101",
        "floorNo": 1,
        "createdAt": "2026-04-21T22:39:09",
        "currentTenant": {
          "id": 1,
          "fullname": "paras90",
          "email": "thisthis2@gmail.com",
          "contactNo": "7000164915",
          "role": "LANDLORD",
          "isActive": true
        }
      },
      "tenant": {
        "id": 1,
        "fullname": "paras90",
        "email": "thisthis2@gmail.com",
        "contactNo": "7000164915",
        "role": "LANDLORD",
        "isActive": true
      }
    }
  ]
  ```

### `GET /api/v1/bills/room/{roomId}`
- **Description:** Landlord fetches all bills for a specific room.
- **Required Role:** `LANDLORD`
- **Path Variable:** `roomId` (Long)
- **Response:** `200 OK`
  ```json
  [
    {
      "id": 4,
      "billingPeriodStart": "2026-04-21",
      "billingPeriodEnd": "2026-04-30",
      "unitsConsumed": 0.07,
      "unitRate": 8.00,
      "totalAmount": 0.56,
      "paymentStatus": "UNPAID",
      "createdAt": "2026-04-22T08:59:06.133554",
      "room": {
        "id": 1,
        "roomNumber": "101"
        // ... (Nested room & current tenant omitted for brevity)
      },
      "tenant": {
        "id": 1,
        "fullname": "paras90",
        "email": "thisthis2@gmail.com"
        // ... (Nested tenant properties omitted for brevity)
      }
    }
  ]
  ```

### `GET /api/v1/bills/{billId}`
- **Description:** Get a single bill detail by ID.
- **Path Variable:** `billId` (Long)
- **Response:** `200 OK`
  ```json
  {
    "id": 4,
    "billingPeriodStart": "2026-04-21",
    "billingPeriodEnd": "2026-04-30",
    "unitsConsumed": 0.07,
    "unitRate": 8.00,
    "totalAmount": 0.56,
    "paymentStatus": "UNPAID",
    "createdAt": "2026-04-22T08:59:06.133554",
    "room": {
      "id": 1,
      "roomNumber": "101"
      // ... (Nested room & current tenant omitted for brevity)
    },
    "tenant": {
      "id": 1,
      "fullname": "paras90",
      "email": "thisthis2@gmail.com"
      // ... (Nested tenant properties omitted for brevity)
    }
  }
  ```

### `GET /api/v1/bills/unpaid`
- **Description:** Get all unpaid bills across all tenants.
- **Required Role:** `LANDLORD`
- **Response:** `200 OK`
  ```json
  [
    {
      "id": 4,
      "billingPeriodStart": "2026-04-21",
      "billingPeriodEnd": "2026-04-30",
      "unitsConsumed": 0.07,
      "unitRate": 8.00,
      "totalAmount": 0.56,
      "paymentStatus": "UNPAID",
      "createdAt": "2026-04-22T08:59:06.133554",
      "room": {
        "id": 1,
        "roomNumber": "101"
        // ... (Nested room & current tenant omitted for brevity)
      },
      "tenant": {
        "id": 1,
        "fullname": "paras90",
        "email": "thisthis2@gmail.com"
        // ... (Nested tenant properties omitted for brevity)
      }
    }
  ]
  ```

### `POST /api/v1/bills/generate`
- **Description:** Manually trigger bill generation for a specific month. Useful for testing or if the scheduler missed a run. Bills are auto-generated on the 1st of each month via a scheduler.
- **Required Role:** `LANDLORD`
- **Query Parameters:**
  - `month` (optional, `yyyy-MM-dd`, day is ignored) — defaults to previous month
- **Response:** `200 OK`
  ```json
  {
    "status": "success",
    "message": "Bill generation completed",
    "billsGenerated": 1,
    "billingMonth": "APRIL 2026",
    "bills": [
      {
        "id": 4,
        "billingPeriodStart": "2026-04-21",
        "billingPeriodEnd": "2026-04-30",
        "unitsConsumed": 0.07,
        "unitRate": 8.00,
        "totalAmount": 0.56,
        "paymentStatus": "UNPAID",
        "createdAt": "2026-04-22T08:59:06.133554",
        "room": {
          "id": 1,
          "roomNumber": "101"
          // omitting nested data for brevity
        },
        "tenant": {
          "id": 1,
          "fullname": "paras90"
          // omitting nested data for brevity
        }
      }
    ]
  }
  ```

### Bill Calculation Logic
- **Formula:** `total_amount = units_consumed × unit_rate_per_kwh`
- **Units consumed:** `last_energy_reading - first_energy_reading` within the billing period
- **Partial months:** If a tenant moved in mid-month, billing starts from their actual occupancy date
- **Auto-generation:** Scheduler runs at `00:00 on the 1st` of every month for the previous month
- **Duplicate prevention:** If a bill already exists for a tenant + period, it is skipped

