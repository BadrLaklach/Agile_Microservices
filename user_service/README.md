# Agile User Service

A robust, enterprise-ready Spring Boot microservice designed for secure user identity management, registration, and authentication. It handles secure password hashing, issues standard JSON Web Tokens (JWT), and manages roles for access control within the Agile microservices ecosystem.

---

## Key Features

- **Robust Authentication:** Seamless user registration and login endpoints.
- **Enhanced Cookie-Based Security:** Authentication tokens are delivered strictly via secure, HTTP-only cookies (`Set-Cookie` header), protecting the client from Cross-Site Scripting (XSS) attacks.
- **Role-Based Access Control:** Pre-configured roles (`ADMIN`, `DEV`, `PO`, `SM`, `MA`) to support diverse user permissions.
- **In-Memory Storage & Seeding:** Utilizes H2 Database (configured with PostgreSQL compatibility) pre-seeded with multi-role test accounts.
- **RFC 7807 Error Standards:** Consistent and descriptive error responses using Spring's native `ProblemDetail` specification.

---

## Technical Stack

This project is built using modern Java standards and the Spring Boot framework:

- **Java Version:** 21 (LTS)
- **Spring Boot Version:** 4.0.6
- **Web MVC:** Spring Boot Web Starter for RESTful API routing and controllers.
- **Data Persistence:** Spring Data JPA with the Hibernate ORM provider.
- **Database:** In-memory H2 database (simulating a PostgreSQL environment).
- **Security & Cryptography:** Spring Security Crypto module (`BCryptPasswordEncoder`) for password hashing.
- **Token Management:** `jjwt` (Java JWT) library version `0.12.5` for parsing and signing tokens.
- **Development Tooling:** Lombok for boilerplate reduction, and Spring Boot DevTools for rapid local iteration.

---

## Configuration Reference

Key application configurations can be adjusted inside the [application.properties](file:///home/eayzaid/Projects/Agile_Microservices/user_service/src/main/resources/application.properties) file:

| Property Name | Default Value | Description |
|---|---|---|
| `server.port` | `8080` | Port on which the user service runs. |
| `spring.datasource.url` | `jdbc:h2:mem:userdb;MODE=PostgreSQL...` | JDBC URL for the H2 in-memory DB. |
| `spring.h2.console.path` | `/h2-console` | Context path for the web-based database UI. |
| `app.jwt.secret` | `change-this-secret-to-32-chars-minimum` | HMAC-SHA signing secret for issuing JWTs. |
| `app.jwt.expiration-seconds` | `86400` (24 hours) | Validity duration for generated access tokens. |

---

## Database Architecture

The persistence model is managed via standard DDL and initial seeds:
- **Database Schema:** Defined in [schema.sql](file:///home/eayzaid/Projects/Agile_Microservices/user_service/src/main/resources/schema.sql). It creates the custom PostgreSQL enum type `role_enum` and the `users` table under `user_schema`.
- **Database Console:** H2 Console is enabled by default at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:userdb`, User: `sa`, Password: `[blank]`).

### Pre-Seeded Test Accounts

The service comes pre-populated with default accounts configured with different system roles for convenient testing.
The **default password** for all seeded accounts is **`password123`**.

| Email | First Name | Last Name | Assigned Role |
|---|---|---|---|
| `admin@agile.local` | Amina | Haddad | `ADMIN` |
| `po@agile.local` | Youssef | Bennani | `PO` (Product Owner) |
| `sm@agile.local` | Salma | Khalid | `SM` (Scrum Master) |
| `dev1@agile.local` | Omar | Fassi | `DEV` (Developer) |
| `mgr@agile.local` | Nadia | El Amrani | `MA` (Manager) |

---

## Authentication & Token Transmission

> [!IMPORTANT]
> **Token Security Architecture**
> To prevent Cross-Site Scripting (XSS) attacks, the **only** token issued by this service is an **Access Token** passed to the client via the `Set-Cookie` header.
>
> - The access token is **NOT** included in the JSON response body.
> - The cookie is named `accessToken` and is explicitly configured with `HttpOnly` and `Path=/`.
> - Browsers and HTTP clients must allow cookies to properly persist the session for downstream services.

---

## API Endpoints

### 1. Register a New User

- **Endpoint:** `POST /api/v1/auth/register`
- **Description:** Creates a new user with the specified role.
- **Success Status:** `201 Created`
- **Failure Status:** `400 Bad Request` (Validation errors) or `409 Conflict` (Email already registered)

#### Request Body
```json
{
  "email": "john.doe@agile.local",
  "password": "securePassword123",
  "firstName": "John",
  "lastName": "Doe",
  "role": "DEV"
}
```

#### Field Constraints
- `email`: Not blank, must be a valid email format, max 255 characters.
- `password`: Not blank, minimum 8 characters, max 255 characters.
- `firstName`: Not blank, max 100 characters.
- `lastName`: Not blank, max 100 characters.
- `role`: Must be one of: `ADMIN`, `DEV`, `PO`, `SM`, `MA`.

#### Response Body
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "email": "john.doe@agile.local",
  "firstName": "John",
  "lastName": "Doe",
  "role": "DEV",
  "createdAt": "2025-01-01T00:00:00Z"
}
```
*Note: The `Set-Cookie` header will contain the HTTP-only `accessToken`.*

---

### 2. User Login

- **Endpoint:** `POST /api/v1/auth/login`
- **Description:** Authenticates user credentials and initiates a secure session.
- **Success Status:** `200 OK`
- **Failure Status:** `401 Unauthorized` (Invalid email or password)

#### Request Body
```json
{
  "email": "admin@agile.local",
  "password": "password123"
}
```

#### Response Body
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "email": "admin@agile.local",
  "firstName": "Amina",
  "lastName": "Haddad",
  "role": "ADMIN",
  "createdAt": "2025-01-01T00:00:00Z"
}
```
*Note: The `Set-Cookie` header will contain the HTTP-only `accessToken`.*

---

### 3. Get Current Authenticated User

- **Endpoint:** `GET /api/v1/users/me`
- **Description:** Returns the full profile of the currently authenticated user using their session cookie.
- **Auth:** Requires valid `accessToken` cookie.
- **Success Status:** `200 OK`
- **Failure Status:** `401 Unauthorized` (Missing or invalid cookie)

#### Response Body
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "email": "admin@agile.local",
  "firstName": "Amina",
  "lastName": "Haddad",
  "role": "ADMIN",
  "createdAt": "2025-01-01T00:00:00Z"
}
```

---

### 4. Get User Profile by ID

- **Endpoint:** `GET /api/v1/users/{id}`
- **Description:** Resolves a user's full profile by their UUID.
- **Auth:** Requires valid `accessToken` cookie.
- **Success Status:** `200 OK`
- **Failure Status:** `400 Bad Request` (Invalid UUID format), `401 Unauthorized` (Missing or invalid cookie), or `404 Not Found` (User not found).

#### Response Body
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "email": "john.doe@agile.local",
  "firstName": "John",
  "lastName": "Doe",
  "role": "DEV",
  "createdAt": "2025-01-01T00:00:00Z"
}
```

---

## Error Handling Specification

When errors occur, this service returns Standard RFC 7807 `ProblemDetail` structures.

### Example: 400 Bad Request (Validation Failure)
```json
{
  "type": "about:blank",
  "title": "Constraint violation",
  "status": 400,
  "detail": "Validation failed",
  "instance": "/api/v1/auth/register",
  "errors": {
    "password": "size must be between 8 and 255",
    "email": "must be a well-formed email address"
  }
}
```

### Example: 401 Unauthorized (Invalid Credentials)
```json
{
  "type": "about:blank",
  "title": "Invalid credentials",
  "status": 401,
  "detail": "Invalid email or password",
  "instance": "/api/v1/auth/login"
}
```

### Example: 409 Conflict (Email Already Registered)
```json
{
  "type": "about:blank",
  "title": "Email already exists",
  "status": 409,
  "detail": "Email already registered: admin@agile.local",
  "instance": "/api/v1/auth/register"
}
```

---

## Getting Started

### Prerequisites
- **Java:** JDK 21 or later
- **Maven:** installed locally (or run via the included wrapper `mvnw`)

### Running the Application Locally
To launch the service in development mode, run the following maven command from the repository root:
```bash
./mvnw spring-boot:run
```
The application will boot on standard port `8080` (unless configured otherwise).

### Building and Packaging
To package the project into a runnable JAR file:
```bash
./mvnw clean package
```
The compiled output will be located in the `target/` directory.

### Running Tests
To run unit and integration tests:
```bash
./mvnw test
```

