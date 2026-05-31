# User Service

A Spring Boot microservice providing functionalities for user identity management, including user registration and authentication via JWT (JSON Web Token) issuance.

## Technical Stack

This project is built using Java 21 and the Spring Boot framework. Key dependencies include:

- **Spring Boot Version:** 4.0.6
- **Java Version:** 21
- **Web:** Spring Web MVC for creating RESTful APIs.
- **Database:** Spring Data JPA with the H2 in-memory database.
- **Security:** Spring Security for authentication and authorization, including password encoding.
- **JWT:** `jjwt` library for handling JSON Web Tokens.
- **Development:** Spring Boot DevTools, Lombok for reducing boilerplate code.

## Getting Started

### Prerequisites
- Java 21 or later
- Maven

### Running the Application

To run the service, execute the following command from the project's root directory:

```zsh
./mvnw spring-boot:run
```

The application will start on the default port `8080`.

### Building the Application

To build a JAR file of the application, run:

```zsh
./mvnw clean install
```

## API Endpoints

All endpoints are prefixed with `/api/v1/auth`.

### Register a New User

- **Endpoint:** `POST /register`
- **Description:** Creates a new user in the system.
- **Success Response:** `201 Created`
- **Failure Response:** `400 Bad Request` if validation fails.

#### Request Body

| Field       | Type   | Constraints                                       | Description                |
|-------------|--------|---------------------------------------------------|----------------------------|
| `email`     | String | Not Blank, Valid Email, Max 255 chars             | User's email address       |
| `password`  | String | Not Blank, Min 8 chars, Max 255 chars             | User's password            |
| `firstName` | String | Not Blank, Max 100 chars                          | User's first name          |
| `lastName`  | String | Not Blank, Max 100 chars                          | User's last name           |
| `role`      | String | Not Null, one of `ADMIN`, `DEV`, `PO`, `SM`, `MA` | User's role in the system  |

**Example Request:**
```json
{
  "email": "new.user@agile.local",
  "password": "password123",
  "firstName": "New",
  "lastName": "User",
  "role": "DEV"
}
```

#### Response Body

On successful registration, the response will be a JSON object containing the user's role, and a `Set-Cookie` header with the JWT.

**Example Response:**
```json
{
  "role": "DEV"
}
```

### User Login

- **Endpoint:** `POST /login`
- **Description:** Authenticates a user and returns a JWT upon successful login.
- **Success Response:** `200 OK`
- **Failure Response:** `400 Bad Request` if validation fails, `401 Unauthorized` for invalid credentials.

#### Request Body

| Field      | Type   | Constraints                               | Description          |
|------------|--------|-------------------------------------------|----------------------|
| `email`    | String | Not Blank, Valid Email, Max 255 chars     | User's email address |
| `password` | String | Not Blank, Min 8 chars, Max 255 chars     | User's password      |

**Example Request:**
```json
{
  "email": "admin@agile.local",
  "password": "password123"
}
```

#### Response Body

On successful login, the response will be a JSON object containing the user's role, and a `Set-Cookie` header with the JWT.

**Example Response:**
```json
{
  "role": "ADMIN"
}
```
