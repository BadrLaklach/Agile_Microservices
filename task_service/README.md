# Task Service — Agile Platform

## Overview
The Task Service is a core, greenfield Spring Boot 4 / Java 21 microservice in the Agile Platform. It serves as the single source of truth for all task data. This service relies on an event-driven architecture, publishing events whenever tasks are modified, and consuming project membership events from the Project Management (PM) Service to enforce project-level authorization rules.

## Core Responsibilities
- **Task Management (CRUD):** Directly manages all task lifecycles (Creation, Updates, Sprint assignments, Status transitions, Deletions).
- **Event Publisher:** Acts as a RabbitMQ publisher. Every mutation to a task publishes a domain event to the `task.events` exchange.
- **Event Consumer:** Subscribes to the `pm.events` exchange. Consumes membership events (`member.invited`, `member.removed`) from the PM Service to update its own read-model projection of User-Project memberships.
- **Authorization:** Enforces complex role-based and project-based access controls for tasks. Membership is validated against its local MongoDB read-model (`member_project_view`) to prevent synchronous coupling with the PM Service.

## Technology Stack
- **Language:** Java 21
- **Framework:** Spring Boot 4
- **Web:** Spring Web MVC
- **Database (Write - Relational):** PostgreSQL (`task_schema`) with Spring Data JPA and Hibernate
- **Database (Read - Document):** MongoDB (`member_project_view`) with Spring Data MongoDB
- **Messaging:** RabbitMQ with Spring AMQP
- **Security:** Spring Security (Stateless, Request Header-based Authentication)
- **Build Tool:** Maven

## Architecture & Messaging Model
This microservice adheres strictly to CQRS principles for cross-boundary data dependency. 

### Published Events (`task.events` - Topic Exchange)
All published events include the `eventType` as their first field.
- `TASK_CREATED`: Fired when a new task is successfully persisted.
- `TASK_UPDATED`: Fired upon modifications to existing task fields or sprint assignments.
- `TASK_STATUS_CHANGED`: Fired when a task transitions between statuses (e.g., TODO -> IN_PROGRESS).
- `TASK_DELETED`: Fired when a task is permanently removed.

### Consumed Events (`pm.events` - Topic Exchange)
The service listens to the `task-service.pm-events` queue (Routing Key: `member.#`).
- `MEMBER_INVITED`: Triggers an upsert in the local `member_project_view` MongoDB collection.
- `MEMBER_REMOVED`: Triggers a deletion from the local `member_project_view`.

## Security & Authorization
Authentication occurs at the API Gateway layer, which forwards validated JWT claims to this service via HTTP Headers:
- `X-User-Id` (UUID)
- `X-User-Role` (String)

**Authorization Matrix:**
- **ADMIN:** Unrestricted access. No project membership check.
- **PO / SM:** Can create, read, update, update status, update sprints, and delete tasks within projects they are members of.
- **DEV:** Can create and read tasks in their projects. Can update status. Full updates are restricted to tasks they created or are assigned to.
- **MA:** Read-only access to project tasks.

## Database Schemas
- **PostgreSQL (`task_schema`):** Tasks table, managing pure UUID references to Projects, Sprints, and Users without cross-schema foreign keys. Enforces strict task-type rules.
- **MongoDB (`member_project_view`):** Used strictly for high-performance `O(1)` lookups to verify if a user has access to a particular project's tasks without a synchronous HTTP call to PM Service.

## API Endpoints Reference

All endpoints are prefixed with `/api/v1/tasks` and require `X-User-Id` and `X-User-Role` headers.
Error handling conforms to RFC 7807 (`ProblemDetail`).

### `POST /api/v1/tasks`
Create a new task. (Roles: ADMIN, PO, SM, DEV)
**Request Body:**
```json
{
  "title": "Task title",
  "description": "Task description",
  "type": "USER_STORY", 
  "priority": "HIGH",
  "projectId": "uuid",
  "sprintId": "uuid (optional)",
  "estimate": 5,
  "assigneeId": "uuid (optional)"
}
```
*Note: Valid types include `USER_STORY`, `BUG`, `TECHNICAL_TASK` for projects.*
**Response (201 Created):**
```json
{
  "id": "uuid",
  "projectId": "uuid",
  "sprintId": "uuid",
  "title": "Task title",
  "description": "Task description",
  "type": "USER_STORY",
  "priority": "HIGH",
  "status": "TODO",
  "estimate": 5,
  "assigneeId": "uuid",
  "createdBy": "uuid",
  "createdAt": "2026-06-02T13:46:03Z",
  "updatedAt": "2026-06-02T13:46:03Z"
}
```

### `GET /api/v1/tasks/{id}`
Get task details by ID.
**Response (200 OK):** Returns the Task object as shown above.

### `GET /api/v1/tasks`
List, filter, and paginate tasks. Scope varies by role and query params.
**Query Parameters:** `projectId`, `sprintId`, `assigneeId`, `status`, `type`, `priority`, `page` (default: 0), `size` (default: 20).
**Response (200 OK):**
```json
{
  "content": [
    { /* Task Object */ }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### `GET /api/v1/tasks/summary`
Get aggregated counts of tasks scoped to a project or sprint.
**Query Parameters:** `projectId`, `sprintId`
**Response (200 OK):**
```json
{
  "projectId": "uuid",
  "sprintId": "uuid",
  "total": 10,
  "byStatus": { "TODO": 5, "IN_PROGRESS": 5 },
  "byType": { "USER_STORY": 8, "BUG": 2 }
}
```

### `PUT /api/v1/tasks/{id}`
Full task update (Title, Priority, Estimate, Assignee).
**Request Body:** Same as `POST` request.
**Response (200 OK):** Returns the updated Task object.

### `PATCH /api/v1/tasks/{id}/status`
Update a task's status.
**Request Body:**
```json
{
  "status": "IN_PROGRESS"
}
```
**Response (200 OK):** Empty body.

### `PATCH /api/v1/tasks/{id}/sprint`
Update a task's sprint assignment.
**Request Body:**
```json
{
  "sprintId": "uuid (or null to unassign)"
}
```
**Response (200 OK):** Empty body.

### `DELETE /api/v1/tasks/{id}`
Delete a task.
**Response (204 No Content):** Empty body.

## Running Locally

**Environment Variables Required:**
```env
SERVER_PORT=8083
DB_URL=jdbc:postgresql://localhost:5432/agiledb
DB_USERNAME=task_service_role
DB_PASSWORD=secret
MONGO_URI=mongodb://localhost:27017/agile_tasks
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
```

**Build & Run:**
```bash
./mvnw clean package
java -jar target/taskservice-0.0.1-SNAPSHOT.jar
```
*Note: A `docker-compose.yml` is provided at the root of the project to boot the entire stack.*
