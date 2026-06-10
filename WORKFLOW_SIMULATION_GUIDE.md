# Agile Microservices: Real-World Workflow Simulation

This guide is designed to help you execute a realistic, end-to-end simulation of the Agile Microservices ecosystem. It combines **Postman API execution** with **Grafana Observability**, allowing you to see both *what* the system does and *how* it does it behind the scenes.

---

## 🏗️ System Flow & Architecture Illustration

Here is the textual flow of the actions in this simulation:

```text
[ Postman Client ] 
       │
       ▼ (1. HTTP Request)
[ API Gateway (Port 8080) ]
       │
       ├──► [ User Service ]       (Validates login & issues JWT)
       │
       ├──► [ PM Service ]         (Creates Projects, Sprints, Metrics)
       │            │
       │            └──► [ MongoDB (CQRS Read Models) & PostgreSQL (Writes) ]
       │
       └──► [ Task Service ]       (Manages Task state)
                    │
                    ▼ (2. Publishes Event on Status Change)
            [ RabbitMQ Message Broker ]
                    │
                    ▼ (3. Consumes Event)
         [ Notification Service ]  (Sends async emails)

========================================================================
[ Promtail ] ---(4. Scrapes logs from all pods above)---> [ Loki ] ---> [ Grafana ]
```

---

## 🎬 The Scenario Setup: Meet the Team

For this simulation, we have two actors:
1. **Fatima (Product Owner - PO):** Responsible for creating projects, planning sprints, and assigning tasks.
2. **Tariq (Developer - DEV):** Responsible for executing tasks and updating statuses.

> **Grafana Prep:** Before you begin, open Grafana (`http://localhost:3000`), go to the **Explore** tab, select **Loki**, and run this query to watch the whole system live:
> `{namespace="agile-app"}`

---

## 🏃‍♂️ Act 1: Identity & Authentication (User Service)

### Action 1.1: Fatima & Tariq Register
*   **Postman Action:** Run `Register User (PO)` (Fatima) and `Register User (DEV)` (Tariq).
*   **System Flow:** API Gateway routes to `user-service`. Passwords are encrypted, and records are saved to PostgreSQL.
*   **Grafana Check:** You will see `Hibernate: insert into user_schema.users...` in the `user-service` logs.

### Action 1.2: Fatima Logs In
*   **Postman Action:** Run `Login` (as Fatima).
*   **System Flow:** User service authenticates the credentials and generates a stateless JWT token. Postman automatically saves this to the `{{token}}` variable.
*   **What you can do now:** With this token, Fatima is authorized to hit protected routes on the `pm-service` and `task-service`.

---

## 🏗️ Act 2: Building the Workspace (PM Service)

### Action 2.1: Fatima Creates a Project
*   **Postman Action:** Run `Create Project`.
*   **System Flow:** API Gateway validates Fatima's JWT token (checking if she has PO/ADMIN rights). It routes to `pm-service`, which creates the project in PostgreSQL and syncs it to MongoDB for fast reading. Postman saves `{{project_id}}`.
*   **What you can do now:** 
    *   Run `Get All Projects` to verify it exists.
    *   Run `Update Project` if she made a typo.
    *   **Action 2.2:** Run `Add Member to Project` to invite Tariq (`tariq.dev@example.com`) to the project.

### Action 2.3: Fatima Plans the Sprint
*   **Postman Action:** Run `Create Sprint`.
*   **System Flow:** `pm-service` verifies the project exists and Fatima's permissions, then creates the sprint. Postman saves `{{sprint_id}}`.
*   **What you can do now:**
    *   Run `Start Sprint` to change its state from PLANNED to ACTIVE.
    *   Create tasks for this sprint.

---

## 📝 Act 3: Delegation (Task Service)

### Action 3.1: Fatima Creates a Task for Tariq
*   **Postman Action:** Run `Create Task`.
*   **System Flow:** API Gateway routes to `task-service`. It creates a task assigned to Tariq (`assigneeId`) belonging to the E-Commerce project (`projectId`). Postman saves `{{task_id}}`.
*   **What you can do now:**
    *   Run `Link Task to Sprint` to assign it to the sprint you just created.
    *   Run `Get Tasks by Project` to see the backlog filling up.

---

## ⚡ Act 4: The Event-Driven Climax (RabbitMQ Integration)

Now it is Tariq's turn. Tariq logs in, looks at his task, and begins working. 

> **Grafana Prep:** This is the most important observability test. In Grafana, change your query to exactly this and hit "Live" (top right corner):
> `{service=~"task-service|notification-service"}`

### Action 4.1: Tariq Updates the Task Status
*   **Postman Action:** Run `Update Task Status` (changing status to `IN_PROGRESS`).
*   **System Flow:** 
    1. `task-service` updates the database record.
    2. `task-service` fires a `TaskUpdatedEvent` to RabbitMQ.
    3. `notification-service` is listening, consumes the message, and triggers an email to Fatima (the reporter) telling her Tariq started the task.
*   **Grafana Check:** Watch your Grafana screen! You will see a log from `task-service` announcing the status change, immediately followed by a log from `notification-service` saying `"Processing task notification... Sending email"`.

*   **What you can do now:**
    *   Update the status again to `DONE` and watch the async events fire a second time.

---

## 📊 Act 5: Analytics & Metrics (CQRS Flow)

Now that work is being completed, Fatima wants to check on the project's health.

### Action 5.1: Check Burndown & Velocity
*   **Postman Action:** Run `Get Sprint Burndown Metrics` and `Get Project Velocity Metrics`.
*   **System Flow:** The `pm-service` does NOT query the heavy PostgreSQL write database. Instead, it queries the optimized **MongoDB** read-models that have been asynchronously updated in the background.
*   **Grafana Check:** In Grafana, change your query to `{service="pm-service"} |= "mongo"`. You will see queries executing against the NoSQL database for analytical performance.

---

## 🎯 Wrap Up & Next Steps

Congratulations! You have just simulated a full Agile lifecycle. 

**Further Experiments you can run:**
1. **Security Test:** Try to run `Create Project` without logging in. Watch Grafana for the `401 Unauthorized` logs at the `api-gateway`.
2. **Role Test:** Log in as Tariq (Developer) and try to run `Create Sprint`. Watch the `pm-service` throw a `403 Forbidden` error because Developers cannot create sprints.
3. **Scale Test:** Open a terminal and run `kubectl scale deploy notification-service --replicas=3 -n agile-app`. Then rapidly update task statuses. In Grafana, you will see the logs load-balanced across the 3 different pods seamlessly!
