# Agile Microservices: Comprehensive API Testing Scenarios

This guide provides end-to-end `curl` commands to fully test the Agile Microservices ecosystem from your terminal. It covers the complete lifecycle: User Onboarding, Project Management, Sprint Planning, Task Execution, and Metrics Tracking.

## Prerequisites

To interact with the APIs from your local machine, we will route all traffic through the API Gateway. Port-forward the API Gateway service to your local machine:

```bash
# Open a separate terminal and run:
kubectl port-forward svc/api-gateway -n agile-app 8080:8080
```

> **Note:** All requests in this guide are directed to `http://localhost:8080`, which is the entry point managed by the API Gateway.
> **Important:** The microservices use `UUID`s for IDs. When running these commands, you MUST replace placeholder values (like `$FATIMA_ID`, `$PROJECT_ID`) with the actual UUIDs returned in previous responses!

---

## Scenario 1: User Onboarding & Authentication (User Service)

First, we need to create users and authenticate them to retrieve a JWT token, which is required for all subsequent requests.

### 1.1 Register a Product Owner (PO)
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Fatima",
    "lastName": "Manager",
    "email": "fatima.manager@example.com",
    "password": "SecurePassword123",
    "role": "PO"
  }'
```

### 1.2 Register a Developer (DEV)
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Tariq",
    "lastName": "Developer",
    "email": "tariq.dev@example.com",
    "password": "SecurePassword123",
    "role": "DEV"
  }'
```

### 1.3 Login as the Product Owner (Get JWT)
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "fatima.manager@example.com",
    "password": "SecurePassword123"
  }'
```
> **Action Required:** Copy the `token` from the response. Export it as an environment variable in your terminal for easy use:
> ```bash
> export TOKEN="your.jwt.token.here"
> ```

### 1.4 View My Profile
```bash
curl -X GET http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer $TOKEN"
```
> **Action Required:** Note your `userId` (Fatima) and Tariq's `userId`. You will need to substitute `$FATIMA_ID` and `$TARIQ_ID` below.

---

## Scenario 2: Project Management (PM Service)

Fatima (Product Owner) creates a new project and adds Tariq to the team.

### 2.1 Create a New Project
```bash
curl -X POST http://localhost:8080/api/v1/projects \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "E-Commerce Replatforming",
    "description": "Migrating legacy monolithic store to microservices.",
    "methodology": "SCRUM",
    "startDate": "2026-06-15",
    "endDate": "2026-12-31"
  }'
```
> **Action Required:** Note the `id` from the response and export it as `$PROJECT_ID`.

### 2.2 Add Developer to the Project
```bash
curl -X POST http://localhost:8080/api/v1/projects/$PROJECT_ID/members \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "tariq.dev@example.com",
    "role": "DEV",
    "isNew": false
  }'
```

### 2.3 List Project Members
```bash
curl -X GET http://localhost:8080/api/v1/projects/$PROJECT_ID/members \
  -H "Authorization: Bearer $TOKEN"
```

---

## Scenario 3: Sprint Planning (PM Service)

Create a Sprint within the Project to organize the upcoming workload.

### 3.1 Create a Sprint
```bash
curl -X POST http://localhost:8080/api/v1/projects/$PROJECT_ID/sprints \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Sprint 1: Checkout API",
    "goal": "Deliver fully functional checkout microservice",
    "capacity": 40,
    "startDate": "2026-06-15",
    "endDate": "2026-06-29"
  }'
```
> **Action Required:** Note the `id` from the response and export it as `$SPRINT_ID`.

### 3.2 Fetch All Sprints for Project
```bash
curl -X GET http://localhost:8080/api/v1/projects/$PROJECT_ID/sprints \
  -H "Authorization: Bearer $TOKEN"
```

---

## Scenario 4: Task Execution (Task Service)

Create a task, assign it to Tariq, and add it to the Sprint.

### 4.1 Create a Task
```bash
curl -X POST http://localhost:8080/api/v1/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Implement Stripe Payment Gateway",
    "description": "Integrate Stripe SDK for processing credit cards.",
    "type": "STORY",
    "priority": "HIGH",
    "estimate": 5,
    "projectId": "'$PROJECT_ID'",
    "assigneeId": "'$TARIQ_ID'"
  }'
```
> **Action Required:** Note the `id` from the response and export it as `$TASK_ID`.

### 4.2 Add Task to the Sprint
```bash
curl -X PATCH http://localhost:8080/api/v1/tasks/$TASK_ID/sprint \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sprintId": "'$SPRINT_ID'"
  }'
```

### 4.3 Update Task Status (Triggers RabbitMQ Notification)
When the status is updated, the **Task Service** emits an event to **RabbitMQ**. The headless **Notification Service** will consume it and generate a notification/email asynchronously.
```bash
# Tariq moves the task to IN_PROGRESS
curl -X PATCH http://localhost:8080/api/v1/tasks/$TASK_ID/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "IN_PROGRESS"
  }'
```
> **Check Notification Service:** To verify event-driven messaging works, check the logs of the Notification Service pod:
> `kubectl logs -l app=notification-service -n agile-app -f`

---

## Scenario 5: Metrics & Analytics (PM Service / CQRS View)

As tasks are completed, the Project Management Service projects this data into MongoDB for analytical querying.

### 5.1 Fetch Sprint Burndown Chart Data
```bash
curl -X GET http://localhost:8080/api/v1/projects/$PROJECT_ID/sprints/$SPRINT_ID/metrics/burndown \
  -H "Authorization: Bearer $TOKEN"
```

### 5.2 Fetch Project Velocity Metrics
```bash
curl -X GET http://localhost:8080/api/v1/projects/$PROJECT_ID/metrics/velocity \
  -H "Authorization: Bearer $TOKEN"
```

### 5.3 Fetch General Task Summary (Task Service)
```bash
curl -X GET "http://localhost:8080/api/v1/tasks/summary?projectId=$PROJECT_ID" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🛑 Troubleshooting

- **Connection Refused:** Ensure you ran the `kubectl port-forward` command successfully.
- **Unauthorized (401):** Ensure your `$TOKEN` variable is set correctly and the token has not expired.
- **Not Found (404) or Bad Request (400):** Ensure you are substituting the `$UUID` placeholders with the correct, valid UUIDs returned from previous responses! Using `1` or `2` will result in errors.
