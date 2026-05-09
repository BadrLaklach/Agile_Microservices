# User Service

Spring Boot microservice for user identity, registration, and JWT issuance.

## Quick start

```zsh
./mvnw spring-boot:run
```

## Register endpoint

- `POST /api/v1/auth/register`
- Body: JSON with `email`, `password`, `firstName`, `lastName`, `role`
- Invalid `role` values return `400` with validation errors.

Example curl:

```zsh
curl -i -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"new.user@agile.local","password":"password123","firstName":"New","lastName":"User","role":"DEV"}'
```

## Login endpoint

- `POST /api/v1/auth/login`
- Body: JSON with `email`, `password`

Example curl:

```zsh
curl -i -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@agile.local","password":"password123"}'
```
