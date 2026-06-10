# Agile Microservices: Comprehensive API Testing Scenarios

This guide provides end-to-end `curl` commands to fully test the Agile Microservices ecosystem from your terminal. It covers the complete lifecycle: User Onboarding, Project Management, Sprint Planning, Task Execution, and Metrics Tracking.

## Prerequisites

To interact with the APIs from your local machine, we will route all traffic through the API Gateway. Port-forward the API Gateway service to your local machine:

```bash
# Open a separate terminal and run:
kubectl port-forward svc/api-gateway -n agile-app 8080:8080
```

> **Note:** All requests in this guide are directed to `http://localhost:8080`, which is the entry point managed by the API Gateway.

---

## Scenario 1: User Onboarding & Authentication (User Service)

First, we need to create users and authenticate them to retrieve a JWT token, which is required for all subsequent requests.

### 1.1 Register a Product Owner
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Alice",
    "lastName": "Manager",
    "email": "alice.manager@example.com",
    "password": "SecurePassword123",
    "role": "PRODUCT_OWNER"
  }'
```

### 1.2 Register a Developer
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Bob",
    "lastName": "Developer",
    "email": "bob.dev@example.com",
    "password": "SecurePassword123",
    "role": "DEVELOPER"
  }'
```

### 1.3 Login as the Product Owner (Get JWT)
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice.manager@example.com",
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
> **Action Required:** Note your `userId` and Bob's `userId` (you can log in as Bob to get his ID, or check the database). For the following steps, we will assume:
> - Alice ID: `1`
> - Bob ID: `2`

---

## Scenario 2: Project Management (PM Service)

Alice (Product Owner) creates a new project and adds Bob to the team.

### 2.1 Create a New Project
```bash
curl -X POST http://localhost:8080/api/v1/projects \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "E-Commerce Replatforming",
    "description": "Migrating legacy monolithic store to microservices.",
    "startDate": "2026-06-15",
    "endDate": "2026-12-31"
  }'
```
> **Action Required:** Note the `projectId` from the response (e.g., `1`).

### 2.2 Add Developer to the Project
```bash
# Assuming Project ID is 1, and Bob's User ID is 2
curl -X POST http://localhost:8080/api/v1/projects/1/members \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "role": "DEVELOPER"
  }'
```

### 2.3 List Project Members
```bash
curl -X GET http://localhost:8080/api/v1/projects/1/members \
  -H "Authorization: Bearer $TOKEN"
```

---

## Scenario 3: Sprint Planning (PM Service)

Create a Sprint within the Project to organize the upcoming workload.

### 3.1 Create a Sprint
```bash
curl -X POST http://localhost:8080/api/v1/projects/1/sprints \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Sprint 1: Checkout API",
    "goal": "Deliver fully functional checkout microservice",
    "startDate": "2026-06-15T00:00:00Z",
    "endDate": "2026-06-29T23:59:59Z"
  }'
```
> **Action Required:** Note the `sprintId` from the response (e.g., `1`).

### 3.2 Fetch All Sprints for Project
```bash
curl -X GET http://localhost:8080/api/v1/projects/1/sprints \
  -H "Authorization: Bearer $TOKEN"
```

---

## Scenario 4: Task Execution (Task Service)

Create a task, assign it to Bob, and add it to the Sprint.

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
    "status": "TODO",
    "storyPoints": 5,
    "projectId": 1,
    "assigneeId": 2,
    "reporterId": 1
  }'
```
> **Action Required:** Note the `taskId` from the response (e.g., `1`).

### 4.2 Add Task to the Sprint
```bash
# Add Task ID 1 to Sprint ID 1
curl -X POST "http://localhost:8080/api/v1/projects/1/sprints/1/tasks?taskId=1" \
  -H "Authorization: Bearer $TOKEN"
```

### 4.3 Update Task Status (Triggers RabbitMQ Notification)
When the status is updated, the **Task Service** emits an event to **RabbitMQ**. The headless **Notification Service** will consume it and generate a notification/email asynchronously.
```bash
# Bob moves the task to IN_PROGRESS
curl -X PUT http://localhost:8080/api/v1/tasks/1 \
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
# Assuming Project 1, Sprint 1
curl -X GET http://localhost:8080/api/v1/projects/1/sprints/1/metrics/burndown \
  -H "Authorization: Bearer $TOKEN"
```

### 5.2 Fetch Project Velocity Metrics
```bash
curl -X GET http://localhost:8080/api/v1/projects/1/metrics/velocity \
  -H "Authorization: Bearer $TOKEN"
```

### 5.3 Fetch General Task Summary (Task Service)
```bash
# Get task summaries across the project
curl -X GET "http://localhost:8080/api/v1/tasks/summary?projectId=1" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🛑 Troubleshooting

- **Connection Refused:** Ensure you ran the `kubectl port-forward` command successfully.
- **Unauthorized (401):** Ensure your `$TOKEN` variable is set correctly and the token has not expired.
- **Not Found (404):** Ensure you are using the correct `projectId`, `taskId`, and `sprintId` that you received from your previous POST requests. ID sequences auto-increment and might be different on your machine.
