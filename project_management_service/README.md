# Agile Project Management Service

A greenfield Spring Boot microservice for managing Agile projects, sprints, team membership, and task-to-sprint assignment within the Agile platform. It implements a CQRS-inspired architecture — PostgreSQL for writes, MongoDB for reads — and integrates with the User Service, Task Service, and Notification Service via synchronous HTTP and asynchronous RabbitMQ events.

---

## Key Features

- **Full Project Lifecycle:** Create, update, list, and archive Agile projects with support for SCRUM, KANBAN, and HYBRID methodologies.
- **Sprint Management:** Plan, start, close, and delete sprints. Assign and remove tasks from sprints with synchronous Task Service validation.
- **Team Membership:** Add members to projects using their email. If a user is marked as new (`isNew: true`), the PM service securely registers them on-the-fly via synchronous calls to the User Service. If they are existing users, the service fetches their identity by email. Includes hydration for listing and precise custom error handling for edge cases.
- **CQRS Read Model:** Consumes RabbitMQ `TASK_*` events from the Task Service to maintain a local MongoDB read model of task projections for fast querying.
- **Agile Metrics:** Real-time burndown charts and velocity reports computed from the local read model.
- **Header-Based Authentication:** Trusts identity headers (`X-User-Id`, `X-User-Role`) set by the API Gateway. No JWT parsing, no token libraries.
- **Role + Membership Authorization:** Fine-grained access control combining global role checks with per-project membership verification.
- **RFC 7807 Error Standards:** Consistent and descriptive error responses using Spring's native `ProblemDetail` specification.

---

## Technical Stack

This project is built using modern Java standards and the Spring Boot framework:

- **Java Version:** 21 (LTS)
- **Spring Boot Version:** 4.0.6
- **Web MVC:** Spring Boot Web Starter for RESTful API routing and controllers.
- **Data Persistence (Write):** Spring Data JPA with Hibernate ORM, backed by PostgreSQL 16.
- **Data Persistence (Read):** Spring Data MongoDB 7 for the CQRS task projection read model.
- **Messaging:** Spring AMQP (RabbitMQ) — consumer only, the service never publishes events.
- **Security:** Spring Security with a custom `OncePerRequestFilter` for header-based identity extraction. **No JWT libraries.**
- **Database Migrations:** Flyway for versioned DDL schema management.
- **Inter-Service Communication:** Spring `RestClient` for synchronous HTTP calls to User, Task, and Notification services.
- **Development Tooling:** Lombok for boilerplate reduction.

---

## Architecture Overview

> [!IMPORTANT]
> **Authentication Model**
> Authentication happens exclusively at the **API Gateway** level. The gateway validates the JWT, extracts claims, and forwards them as `X-User-Id` and `X-User-Role` HTTP headers. This service **never decodes JWTs** and trusts the incoming headers unconditionally.

> [!IMPORTANT]
> **Task Write Model**
> Any operation that mutates task data (sprint assignment) is executed as a **synchronous HTTP call** to the Task Service. RabbitMQ is used **only** to populate the MongoDB read model — the PM Service **never publishes** to RabbitMQ.

```
                    ┌──────────────┐
                    │  API Gateway │
                    │  (JWT → Hdr) │
                    └──────┬───────┘
                           │ X-User-Id / X-User-Role
                    ┌──────▼───────┐
                    │  PM Service  │ :8082
                    │──────────────│
                    │ Controllers  │
                    │ Services     │
                    └──┬──────┬────┘
              Write ▼  │      │  ▼ Read
        ┌──────────────┐  ┌──────────────┐
        │  PostgreSQL   │  │   MongoDB    │
        │  pm_schema    │  │  agile_read  │
        └──────────────┘  └──────▲───────┘
                                 │ TASK_* events
                          ┌──────┴───────┐
                          │   RabbitMQ   │
                          └──────▲───────┘
                                 │ publishes
                          ┌──────┴───────┐
                          │ Task Service │ :8083
                          └──────────────┘
```

---

## Configuration Reference

All configuration is environment-aware, using the `${ENV_VAR:default}` pattern. Defaults are suitable for local development. Key settings are in [application.yml](src/main/resources/application.yml):

| Property | Env Variable | Default Value | Description |
|---|---|---|---|
| `server.port` | `SERVER_PORT` | `8082` | Port on which the PM Service runs. |
| `spring.datasource.url` | `DB_URL` | `jdbc:postgresql://localhost:5432/agiledb` | JDBC URL for PostgreSQL. |
| `spring.datasource.username` | `DB_USERNAME` | `pm_service_role` | PostgreSQL username. |
| `spring.datasource.password` | `DB_PASSWORD` | `secret` | PostgreSQL password. |
| `spring.data.mongodb.uri` | `MONGO_URI` | `mongodb://localhost:27017/agile_read` | MongoDB connection string. |
| `spring.rabbitmq.host` | `RABBITMQ_HOST` | `localhost` | RabbitMQ broker hostname. |
| `spring.rabbitmq.port` | `RABBITMQ_PORT` | `5672` | RabbitMQ broker port. |
| `spring.rabbitmq.username` | `RABBITMQ_USERNAME` | `guest` | RabbitMQ username. |
| `spring.rabbitmq.password` | `RABBITMQ_PASSWORD` | `guest` | RabbitMQ password. |
| `app.user-service.url` | `USER_SERVICE_URL` | `http://localhost:8080` | Base URL of the User Service. |
| `app.task-service.url` | `TASK_SERVICE_URL` | `http://localhost:8083` | Base URL of the Task Service. |
| `app.notification-service.url` | `NOTIFICATION_SERVICE_URL` | `http://localhost:8084` | Base URL of the Notification Service. |

---

## Database Architecture

### PostgreSQL — Write Model

The write model is managed via Flyway migrations under [db/migration/](src/main/resources/db/migration/). All tables live in the `pm_schema` schema.

| Table | Description | Primary Key |
|---|---|---|
| `projects` | Agile projects with methodology, status, dates | `id` (UUID) |
| `project_members` | User-to-project membership with role | `(project_id, user_id)` |
| `sprints` | Sprints linked to projects with lifecycle status | `id` (UUID) |
| `sprint_tasks` | Task-to-sprint assignments | `(sprint_id, task_id)` |

### MongoDB — Read Model

The `task_projections` collection in the `agile_read` database mirrors task state from the Task Service, populated exclusively via RabbitMQ event consumption.

| Field | Type | Description |
|---|---|---|
| `_id` | String | Task UUID (same as Task Service) |
| `projectId` | String | Owning project UUID |
| `sprintId` | String | Assigned sprint UUID (nullable) |
| `title` | String | Task title |
| `type` | String | Task type (e.g., STORY, BUG, TASK) |
| `status` | String | Task status (e.g., TODO, IN_PROGRESS, DONE) |
| `estimate` | Integer | Story point estimate (nullable) |
| `assigneeId` | String | Assigned user UUID (nullable) |
| `createdAt` | Instant | Original creation timestamp |
| `updatedAt` | Instant | Last update timestamp |

---

## Authentication & Authorization

### Identity Headers

Every request to this service must include these headers, set by the API Gateway:

| Header | Type | Description |
|---|---|---|
| `X-User-Id` | UUID | The authenticated user's unique identifier. |
| `X-User-Role` | String | The authenticated user's system role (`ADMIN`, `PO`, `SM`, `DEV`, `MA`). |

If either header is missing or blank, the service returns `401 Unauthorized`.

### Authorization Matrix

Authorization is enforced in the **service layer** with two checks:
1. **Role check** — the caller's role must be in the allowed set for the action.
2. **Membership check** — for non-ADMIN users, a `project_members` row must exist.

| Action | Endpoint | ADMIN | PO | SM | DEV | MA | Membership Required |
|---|---|---|---|---|---|---|---|
| Create project | `POST /projects` | ✓ | ✓ | ✗ | ✗ | ✗ | No |
| List my projects | `GET /projects` | ✓ | ✓ | ✓ | ✓ | ✓ | No (filtered) |
| Get project | `GET /projects/{id}` | ✓ | ✓ | ✓ | ✓ | ✓ | Yes (ADMIN exempt) |
| Update project | `PUT /projects/{id}` | ✓ | ✓ | ✗ | ✗ | ✗ | Yes |
| Archive project | `PATCH /projects/{id}/archive` | ✓ | ✓ | ✗ | ✗ | ✗ | Yes |
| Invite member | `POST /projects/{id}/members` | ✓ | ✓ | ✗ | ✗ | ✗ | Yes |
| List members | `GET /projects/{id}/members` | ✓ | ✓ | ✓ | ✓ | ✓ | Yes |
| Remove member | `DELETE /projects/{id}/members/{uid}` | ✓ | ✓ | ✗ | ✗ | ✗ | Yes |
| Create sprint | `POST /…/sprints` | ✓ | ✓ | ✓ | ✗ | ✗ | Yes |
| List sprints | `GET /…/sprints` | ✓ | ✓ | ✓ | ✓ | ✓ | Yes |
| Get sprint | `GET /…/sprints/{id}` | ✓ | ✓ | ✓ | ✓ | ✓ | Yes |
| Update sprint | `PUT /…/sprints/{id}` | ✓ | ✓ | ✓ | ✗ | ✗ | Yes |
| Delete sprint | `DELETE /…/sprints/{id}` | ✓ | ✓ | ✓ | ✗ | ✗ | Yes |
| Start sprint | `PATCH /…/sprints/{id}/start` | ✓ | ✓ | ✓ | ✗ | ✗ | Yes |
| Close sprint | `PATCH /…/sprints/{id}/close` | ✓ | ✓ | ✓ | ✗ | ✗ | Yes |
| Assign task | `POST /…/sprints/{id}/tasks` | ✓ | ✓ | ✓ | ✗ | ✗ | Yes |
| Remove task | `DELETE /…/sprints/{id}/tasks/{tid}` | ✓ | ✓ | ✓ | ✗ | ✗ | Yes |
| List sprint tasks | `GET /…/sprints/{id}/tasks` | ✓ | ✓ | ✓ | ✓ | ✓ | Yes |
| Burndown chart | `GET /…/sprints/{id}/metrics/burndown` | ✓ | ✓ | ✓ | ✓ | ✓ | Yes |
| Velocity report | `GET /…/metrics/velocity` | ✓ | ✓ | ✓ | ✓ | ✓ | Yes |

---

## API Endpoints

> [!NOTE]
> All endpoints are prefixed with `/api/v1`. All requests require `X-User-Id` and `X-User-Role` headers.

### 1. Create a Project

- **Endpoint:** `POST /api/v1/projects`
- **Description:** Creates a new Agile project. The creator is automatically added as a project member with their current role.
- **Allowed Roles:** `ADMIN`, `PO`
- **Success Status:** `201 Created`

#### Request Body
```json
{
  "name": "Sprint Platform v2",
  "description": "Next-gen sprint management platform",
  "methodology": "SCRUM",
  "startDate": "2026-06-01",
  "endDate": "2026-12-31"
}
```

#### Field Constraints
- `name`: Not blank, max 255 characters.
- `description`: Optional text.
- `methodology`: Required, must be one of: `SCRUM`, `KANBAN`, `HYBRID`.
- `startDate`: Required, ISO date format (`YYYY-MM-DD`).
- `endDate`: Required, ISO date format, must be after `startDate`.

#### Response Body
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "Sprint Platform v2",
  "description": "Next-gen sprint management platform",
  "methodology": "SCRUM",
  "status": "ACTIVE",
  "startDate": "2026-06-01",
  "endDate": "2026-12-31",
  "createdBy": "550e8400-e29b-41d4-a716-446655440000",
  "createdAt": "2026-06-01T10:00:00Z",
  "updatedAt": "2026-06-01T10:00:00Z"
}
```

---

### 2. List My Projects

- **Endpoint:** `GET /api/v1/projects`
- **Description:** Returns all projects the authenticated user is a member of. ADMINs see all projects.
- **Allowed Roles:** All
- **Success Status:** `200 OK`

#### Response Body
```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "Sprint Platform v2",
    "description": "Next-gen sprint management platform",
    "methodology": "SCRUM",
    "status": "ACTIVE",
    "startDate": "2026-06-01",
    "endDate": "2026-12-31",
    "createdBy": "550e8400-e29b-41d4-a716-446655440000",
    "createdAt": "2026-06-01T10:00:00Z",
    "updatedAt": "2026-06-01T10:00:00Z"
  }
]
```

---

### 3. Get Project by ID

- **Endpoint:** `GET /api/v1/projects/{projectId}`
- **Description:** Returns a single project by UUID. Requires membership (ADMIN exempt).
- **Allowed Roles:** All (with membership)
- **Success Status:** `200 OK`
- **Failure Status:** `403 Forbidden` (not a member), `404 Not Found` (project does not exist)

---

### 4. Update a Project

- **Endpoint:** `PUT /api/v1/projects/{projectId}`
- **Description:** Partially updates project fields. Only non-null fields in the request body are applied.
- **Allowed Roles:** `ADMIN`, `PO` (with membership)
- **Success Status:** `200 OK`

#### Request Body
```json
{
  "name": "Sprint Platform v3",
  "methodology": "HYBRID"
}
```

#### Field Constraints
- `name`: Optional, max 255 characters.
- `description`: Optional text.
- `methodology`: Optional, must be one of: `SCRUM`, `KANBAN`, `HYBRID`.
- `startDate`: Optional, ISO date format.
- `endDate`: Optional, ISO date format.

---

### 5. Archive a Project

- **Endpoint:** `PATCH /api/v1/projects/{projectId}/archive`
- **Description:** Sets the project status to `ARCHIVED`. Archived projects cannot have new sprints created.
- **Allowed Roles:** `ADMIN`, `PO` (with membership)
- **Success Status:** `200 OK`
- **Request Body:** None

---

### 6. Invite a Member

- **Endpoint:** `POST /api/v1/projects/{projectId}/members`
- **Description:** Adds a user to the project using their email. If `isNew` is true, the PM service securely registers them on-the-fly in the User Service. If false, it fetches their identity by email. A fire-and-forget notification is sent.
- **Allowed Roles:** `ADMIN`, `PO` (with membership)
- **Success Status:** `201 Created`
- **Failure Status:** `400 Bad Request` (missing registration fields), `404 Not Found` (user does not exist), `409 Conflict` (user already a member)

#### Request Body
```json
{
  "email": "dev@agile.local",
  "role": "DEV",
  "isNew": true,
  "firstName": "New",
  "lastName": "Dev",
  "password": "securePassword123"
}
```

#### Field Constraints
- `email`: Required, valid email format.
- `role`: Required, must be one of: `ADMIN`, `PO`, `SM`, `DEV`, `MA`.
- `isNew`: Required boolean. If true, initiates user registration.
- `firstName`, `lastName`, `password`: Required only if `isNew` is true.

#### Response Body
```json
{
  "userId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "email": "omar.fassi@agile.local",
  "firstName": "Omar",
  "lastName": "Fassi",
  "role": "DEV",
  "joinedAt": "2026-06-01T10:30:00Z"
}
```

---

### 7. List Project Members

- **Endpoint:** `GET /api/v1/projects/{projectId}/members`
- **Description:** Returns all members of the project with their full profile (hydrated from User Service).
- **Allowed Roles:** All (with membership)
- **Success Status:** `200 OK`

---

### 8. Remove a Member

- **Endpoint:** `DELETE /api/v1/projects/{projectId}/members/{userId}`
- **Description:** Removes a user from the project. The project creator cannot be removed.
- **Allowed Roles:** `ADMIN`, `PO` (with membership)
- **Success Status:** `204 No Content`
- **Failure Status:** `400 Bad Request` (attempting to remove the project creator), `404 Not Found` (member not found)

---

### 9. Create a Sprint

- **Endpoint:** `POST /api/v1/projects/{projectId}/sprints`
- **Description:** Creates a new sprint in `PLANNED` status. Cannot create sprints on archived projects.
- **Allowed Roles:** `ADMIN`, `PO`, `SM` (with membership)
- **Success Status:** `201 Created`
- **Failure Status:** `400 Bad Request` (project is archived)

#### Request Body
```json
{
  "name": "Sprint 1 — Foundation",
  "goal": "Establish core data model and API skeleton",
  "capacity": 40,
  "startDate": "2026-06-01",
  "endDate": "2026-06-14"
}
```

#### Field Constraints
- `name`: Not blank, max 255 characters.
- `goal`: Optional text.
- `capacity`: Optional, must be a positive integer.
- `startDate`: Required, ISO date format.
- `endDate`: Required, ISO date format.

#### Response Body
```json
{
  "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "projectId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "Sprint 1 — Foundation",
  "goal": "Establish core data model and API skeleton",
  "status": "PLANNED",
  "capacity": 40,
  "startDate": "2026-06-01",
  "endDate": "2026-06-14",
  "createdAt": "2026-06-01T10:15:00Z",
  "updatedAt": "2026-06-01T10:15:00Z"
}
```

---

### 10. List Sprints

- **Endpoint:** `GET /api/v1/projects/{projectId}/sprints`
- **Description:** Returns all sprints for a project, optionally filtered by status.
- **Query Parameters:** `status` (optional) — `PLANNED`, `ACTIVE`, or `COMPLETED`.
- **Allowed Roles:** All (with membership)
- **Success Status:** `200 OK`

---

### 11. Get Sprint by ID

- **Endpoint:** `GET /api/v1/projects/{projectId}/sprints/{sprintId}`
- **Allowed Roles:** All (with membership)
- **Success Status:** `200 OK`
- **Failure Status:** `404 Not Found`

---

### 12. Update a Sprint

- **Endpoint:** `PUT /api/v1/projects/{projectId}/sprints/{sprintId}`
- **Description:** Updates a sprint's details. Completed sprints cannot be updated.
- **Allowed Roles:** `ADMIN`, `PO`, `SM` (with membership)
- **Success Status:** `200 OK`
- **Failure Status:** `400 Bad Request` (sprint is completed)

#### Request Body
```json
{
  "name": "Sprint 1 — Extended",
  "capacity": 50,
  "endDate": "2026-06-21"
}
```

---

### 13. Delete a Sprint

- **Endpoint:** `DELETE /api/v1/projects/{projectId}/sprints/{sprintId}`
- **Description:** Deletes a sprint. Only sprints in `PLANNED` status can be deleted.
- **Allowed Roles:** `ADMIN`, `PO`, `SM` (with membership)
- **Success Status:** `204 No Content`
- **Failure Status:** `400 Bad Request` (sprint is not in PLANNED status)

---

### 14. Start a Sprint

- **Endpoint:** `PATCH /api/v1/projects/{projectId}/sprints/{sprintId}/start`
- **Description:** Transitions a sprint from `PLANNED` to `ACTIVE`. Only one active sprint is allowed per project at a time.
- **Allowed Roles:** `ADMIN`, `PO`, `SM` (with membership)
- **Success Status:** `200 OK`
- **Failure Status:** `400 Bad Request` (sprint is not PLANNED), `409 Conflict` (another active sprint already exists)
- **Request Body:** None

---

### 15. Close a Sprint

- **Endpoint:** `PATCH /api/v1/projects/{projectId}/sprints/{sprintId}/close`
- **Description:** Transitions a sprint from `ACTIVE` to `COMPLETED`.
- **Allowed Roles:** `ADMIN`, `PO`, `SM` (with membership)
- **Success Status:** `200 OK`
- **Failure Status:** `400 Bad Request` (sprint is not ACTIVE)
- **Request Body:** None

---

### 16. Assign a Task to a Sprint

- **Endpoint:** `POST /api/v1/projects/{projectId}/sprints/{sprintId}/tasks`
- **Description:** Assigns a task to the sprint. Sends a synchronous HTTP PATCH to the Task Service to update the task's sprint reference *before* persisting locally.
- **Allowed Roles:** `ADMIN`, `PO`, `SM` (with membership)
- **Success Status:** `201 Created`
- **Failure Status:** `400 Bad Request` (sprint is completed), `404 Not Found` (task not found in Task Service), `503 Service Unavailable` (Task Service unreachable)

#### Request Body
```json
{
  "taskId": "c3d4e5f6-a7b8-9012-cdef-123456789012"
}
```

#### Response Body
```json
{
  "taskId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "sprintId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "assignedAt": "2026-06-02T09:00:00Z"
}
```

---

### 17. Remove a Task from a Sprint

- **Endpoint:** `DELETE /api/v1/projects/{projectId}/sprints/{sprintId}/tasks/{taskId}`
- **Description:** Removes a task from the sprint. Sends a synchronous HTTP call to the Task Service to clear the task's sprint reference *before* deleting locally.
- **Allowed Roles:** `ADMIN`, `PO`, `SM` (with membership)
- **Success Status:** `204 No Content`
- **Failure Status:** `400 Bad Request` (sprint is completed), `404 Not Found`

---

### 18. List Sprint Tasks

- **Endpoint:** `GET /api/v1/projects/{projectId}/sprints/{sprintId}/tasks`
- **Description:** Returns all task projections assigned to the sprint, read from the MongoDB CQRS read model.
- **Allowed Roles:** All (with membership)
- **Success Status:** `200 OK`

#### Response Body
```json
[
  {
    "taskId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
    "title": "Implement login page",
    "type": "STORY",
    "status": "IN_PROGRESS",
    "estimate": 5,
    "assigneeId": "f47ac10b-58cc-4372-a567-0e02b2c3d479"
  }
]
```

---

### 19. Sprint Burndown Chart

- **Endpoint:** `GET /api/v1/projects/{projectId}/sprints/{sprintId}/metrics/burndown`
- **Description:** Computes a real-time burndown chart for the sprint using task projections from the MongoDB read model. Includes an ideal burn rate curve.
- **Allowed Roles:** All (with membership)
- **Success Status:** `200 OK`

#### Response Body
```json
{
  "sprintId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "sprintName": "Sprint 1 — Foundation",
  "startDate": "2026-06-01",
  "endDate": "2026-06-14",
  "totalEstimate": 40,
  "remainingEstimate": 25,
  "completedEstimate": 15,
  "idealBurndown": [
    { "date": "2026-06-01", "ideal": 40.0 },
    { "date": "2026-06-02", "ideal": 37.07 },
    { "date": "2026-06-03", "ideal": 34.15 },
    { "date": "2026-06-14", "ideal": 0.0 }
  ]
}
```

---

### 20. Project Velocity Report

- **Endpoint:** `GET /api/v1/projects/{projectId}/metrics/velocity`
- **Description:** Computes velocity (sum of completed story point estimates) across all completed sprints in the project, with an average.
- **Allowed Roles:** All (with membership)
- **Success Status:** `200 OK`

#### Response Body
```json
{
  "projectId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "averageVelocity": 35.5,
  "sprints": [
    {
      "sprintId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "sprintName": "Sprint 1 — Foundation",
      "velocity": 38
    },
    {
      "sprintId": "d4e5f6a7-b8c9-0123-defg-234567890123",
      "sprintName": "Sprint 2 — Core Features",
      "velocity": 33
    }
  ]
}
```

---

## RabbitMQ Event Consumption

The PM Service consumes task lifecycle events published by the Task Service to maintain the MongoDB read model. It **never publishes** events.

### Queue Configuration

| Resource | Name | Purpose |
|---|---|---|
| Exchange | `task.events` | Topic exchange for Task Service events |
| Queue | `pm-service.task-events` | PM Service consumer queue |
| Routing Key | `task.#` | Matches all `task.*` routing keys |
| DLX | `task.events.dlx` | Dead-letter exchange for failed messages |
| DLQ | `pm-service.task-events.dlq` | Dead-letter queue for manual inspection |

### Supported Events

| Event Type | Action |
|---|---|
| `TASK_CREATED` | Upserts a new `TaskProjection` document in MongoDB |
| `TASK_UPDATED` | Updates existing projection, or creates one if missing (eventual consistency) |
| `TASK_STATUS_CHANGED` | Updates the `status` and `updatedAt` fields on the projection |
| `TASK_DELETED` | Deletes the projection document from MongoDB |

> [!NOTE]
> Event processing errors are logged but **not re-thrown** to prevent infinite requeue loops. Failed messages are routed to the DLQ for manual review.

---

## Inter-Service Communication

| Target Service | Client Class | Protocol | Purpose |
|---|---|---|---|
| User Service `:8080` | `UserServiceClient` | HTTP GET | Verify user existence, hydrate member profiles |
| Task Service `:8083` | `TaskServiceClient` | HTTP PATCH | Update task sprint assignment |
| Notification Service | `NotificationServiceClient` | HTTP POST | Fire-and-forget notifications (e.g., member invited) |

All inter-service calls forward `X-User-Id` and `X-User-Role` headers for downstream authentication.

---

## Error Handling Specification

When errors occur, this service returns standard RFC 7807 `ProblemDetail` structures.

### Error Response Map

| Exception | HTTP Status | Title |
|---|---|---|
| `ProjectNotFoundException` | `404` | Project not found |
| `SprintNotFoundException` | `404` | Sprint not found |
| `MemberNotFoundException` | `404` | Member not found |
| `UserNotFoundException` | `404` | User not found |
| `TaskNotFoundException` | `404` | Task not found |
| `AccessDeniedException` | `403` | Access denied |
| `AlreadyMemberException` | `409` | Already a member |
| `ActiveSprintExistsException` | `409` | Active sprint exists |
| `InvalidSprintStateException` | `400` | Invalid sprint state |
| `ArchivedProjectException` | `400` | Project is archived |
| `MethodArgumentNotValidException` | `400` | Constraint violation |
| `HttpMessageNotReadableException` | `400` | Invalid request body |
| `MethodArgumentTypeMismatchException` | `400` | Invalid path variable |
| `UserServiceUnavailableException` | `503` | User Service unavailable |
| `TaskServiceUnavailableException` | `503` | Task Service unavailable |
| Generic `Exception` | `500` | Internal server error |

### Example: 400 Bad Request (Validation Failure)
```json
{
  "type": "about:blank",
  "title": "Constraint violation",
  "status": 400,
  "detail": "Validation failed",
  "instance": "/api/v1/projects",
  "errors": {
    "name": "must not be blank",
    "methodology": "must match \"SCRUM|KANBAN|HYBRID\""
  }
}
```

### Example: 403 Forbidden (Access Denied)
```json
{
  "type": "about:blank",
  "title": "Access denied",
  "status": 403,
  "detail": "Access denied",
  "instance": "/api/v1/projects/a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

### Example: 404 Not Found
```json
{
  "type": "about:blank",
  "title": "Project not found",
  "status": 404,
  "detail": "Project not found: a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "instance": "/api/v1/projects/a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

### Example: 409 Conflict (Active Sprint Exists)
```json
{
  "type": "about:blank",
  "title": "Active sprint exists",
  "status": 409,
  "detail": "An active sprint already exists in project a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "instance": "/api/v1/projects/a1b2c3d4-e5f6-7890-abcd-ef1234567890/sprints/b2c3d4e5-f6a7-8901-bcde-f12345678901/start"
}
```

### Example: 503 Service Unavailable
```json
{
  "type": "about:blank",
  "title": "Task Service unavailable",
  "status": 503,
  "detail": "Task Service is currently unavailable",
  "instance": "/api/v1/projects/a1b2c3d4/sprints/b2c3d4e5/tasks"
}
```

---

## Getting Started

### Prerequisites
- **Java:** JDK 21 or later
- **Maven:** installed locally (or run via the included wrapper `mvnw`)
- **PostgreSQL 16:** running with the `pm_schema` schema and `pm_service_role` role created (see [init-db.sql](../init-db.sql))
- **MongoDB 7:** running on default port `27017`
- **RabbitMQ 3:** running on default port `5672`

### Running with Docker Compose (Recommended)

The easiest way to run the entire stack is via the root-level Docker Compose file:
```bash
cd /path/to/Agile_Microservices
docker-compose up --build
```
This starts PostgreSQL, MongoDB, RabbitMQ, User Service (`:8080`), and PM Service (`:8082`).

### Running the Application Locally

Ensure PostgreSQL, MongoDB, and RabbitMQ are running locally, then:
```bash
./mvnw spring-boot:run
```
The application will boot on port `8082` (unless configured otherwise).

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

---

## Project Structure

```
project_management/
├── pom.xml
├── Dockerfile
├── src/main/
│   ├── java/sahmoudi/agile/project_management/
│   │   ├── ProjectManagementApplication.java
│   │   ├── client/
│   │   │   ├── NotificationServiceClient.java
│   │   │   ├── TaskServiceClient.java
│   │   │   └── UserServiceClient.java
│   │   ├── config/
│   │   │   ├── MongoConfig.java
│   │   │   └── RabbitMQConfig.java
│   │   ├── controller/
│   │   │   ├── MetricsController.java
│   │   │   ├── ProjectController.java
│   │   │   └── SprintController.java
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   │   ├── AssignTaskRequest.java
│   │   │   │   ├── CreateProjectRequest.java
│   │   │   │   ├── CreateSprintRequest.java
│   │   │   │   ├── InviteMemberRequest.java
│   │   │   │   ├── NotificationRequest.java
│   │   │   │   ├── UpdateProjectRequest.java
│   │   │   │   ├── UpdateSprintRequest.java
│   │   │   │   └── UpdateTaskSprintRequest.java
│   │   │   └── response/
│   │   │       ├── BurndownPoint.java
│   │   │       ├── BurndownResponse.java
│   │   │       ├── MemberResponse.java
│   │   │       ├── ProjectResponse.java
│   │   │       ├── SprintResponse.java
│   │   │       ├── SprintTaskResponse.java
│   │   │       ├── SprintVelocity.java
│   │   │       ├── TaskProjectionResponse.java
│   │   │       ├── UserResponse.java
│   │   │       └── VelocityResponse.java
│   │   ├── event/
│   │   │   ├── consumer/
│   │   │   │   └── TaskEventConsumer.java
│   │   │   └── payload/
│   │   │       ├── TaskCreatedEvent.java
│   │   │       ├── TaskDeletedEvent.java
│   │   │       ├── TaskStatusChangedEvent.java
│   │   │       └── TaskUpdatedEvent.java
│   │   ├── exception/
│   │   │   ├── AccessDeniedException.java
│   │   │   ├── ActiveSprintExistsException.java
│   │   │   ├── AlreadyMemberException.java
│   │   │   ├── ArchivedProjectException.java
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── InvalidSprintStateException.java
│   │   │   ├── MemberNotFoundException.java
│   │   │   ├── ProjectNotFoundException.java
│   │   │   ├── SprintNotFoundException.java
│   │   │   ├── TaskNotFoundException.java
│   │   │   ├── TaskServiceUnavailableException.java
│   │   │   ├── UserNotFoundException.java
│   │   │   └── UserServiceUnavailableException.java
│   │   ├── model/
│   │   │   ├── Project.java
│   │   │   ├── ProjectMember.java
│   │   │   ├── ProjectMemberId.java
│   │   │   ├── Sprint.java
│   │   │   ├── SprintTask.java
│   │   │   ├── SprintTaskId.java
│   │   │   └── TaskProjection.java
│   │   ├── repository/
│   │   │   ├── ProjectMemberRepository.java
│   │   │   ├── ProjectRepository.java
│   │   │   ├── SprintRepository.java
│   │   │   ├── SprintTaskRepository.java
│   │   │   └── TaskProjectionRepository.java
│   │   ├── security/
│   │   │   ├── RequestHeaderAuthFilter.java
│   │   │   └── SecurityConfig.java
│   │   └── service/
│   │       ├── MemberService.java
│   │       ├── MetricsService.java
│   │       ├── ProjectService.java
│   │       └── SprintService.java
│   └── resources/
│       ├── application.yml
│       └── db/migration/
│           └── V1__init_pm_schema.sql
└── src/test/
    └── java/...
```
