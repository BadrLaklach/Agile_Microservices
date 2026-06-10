# 🏛️ Agile Microservices Ecosystem: Master Architecture & DevOps Reference

This document serves as the absolute master reference for the Agile Microservices project. It details the granular technical decisions, folder structures, microservice boundaries, event-driven integrations, DevOps infrastructure, and observability tooling implemented in this ecosystem.

---

## 📚 1. Core Technology Stack & Versions

The project relies on a modern, enterprise-grade technology stack ensuring high performance, fault tolerance, and developer productivity.

| Component | Technology | Version / Tag | Purpose |
| :--- | :--- | :--- | :--- |
| **Language** | Java | 21 | Core programming language |
| **Framework** | Spring Boot | 3.3.0 | Application framework |
| **Gateway** | Spring Cloud Gateway | 4.1.4 | Single entry point, routing, edge security |
| **Relational DB** | PostgreSQL | 15.3 | Write-heavy operations, ACID transactions |
| **NoSQL DB** | MongoDB | 6.0 | Read-heavy operations, CQRS read models |
| **Message Broker** | RabbitMQ | 3-management | Asynchronous event-driven communication |
| **API Docs** | SpringDoc OpenAPI | 2.5.0 | Automated Swagger UI generation |
| **Containerization**| Docker | N/A | Application packaging (`v6` tags) |
| **Orchestration** | Kubernetes | 1.30+ | Container orchestration, self-healing, scaling |
| **Observability** | Grafana / Loki / Promtail | 10.4.2 / 2.9.4 | Log aggregation and metric visualization |

---

## 📂 2. Complete Directory Architecture & Logical Flow

The repository is structured as a mono-repo containing multiple independent microservices and their shared DevOps configurations.

```text
Agile_Microservices/
│
├── api_gateway/                   # Edge router & Swagger aggregator
├── user_service/                  # Handles identity, JWT issuing, & profiles
├── project_management_service/    # Handles Projects, Sprints, Members, & CQRS Metrics
├── task_service/                  # Handles Task lifecycle & pushes RabbitMQ events
├── notification_service/          # Headless worker, consumes RabbitMQ events for emails
│
├── k8s/                           # Complete Kubernetes Infrastructure
│   ├── databases/                 # StatefulSets & PVCs for Postgres, Mongo, RabbitMQ
│   ├── monitoring/                # Observability Stack (Loki, Grafana, Promtail)
│   ├── services/                  # Deployments & Services for Java apps
│   ├── configmap.yaml             # Shared environment variables
│   ├── secret.yaml                # Base64 encoded passwords & JWT_SECRET
│   ├── deploy.sh                  # Automated zero-to-hero startup script
│   └── undeploy.sh                # Automated teardown script
│
├── Agile_Microservices_Postman_Collection.json     # Full endpoint testing suite
├── Workflow_Simulation_Postman_Collection.json     # Streamlined 11-step scenario test
├── API_TESTING_SCENARIOS.md                        # Raw curl command guide
├── SWAGGER_UI_GUIDE.md                             # Guide for interacting with OpenAPI
├── STARTUP_SETUP.md                                # Boot-up / Tear-down cheat sheet
└── WORKFLOW_SIMULATION_GUIDE.md                    # Event-driven scenario documentation
```

---

## ⚙️ 3. Microservice Logical Boundaries

The application logic is decoupled into 5 distinct services, communicating via HTTP REST (synchronous) and RabbitMQ (asynchronous).

### 1. API Gateway (`api-gateway` - Port 8080)
*   **Logic:** Acts as the single point of entry for the outside world. It routes requests based on URL predicates (e.g., `/api/v1/tasks/**` goes to Task Service). 
*   **Security:** Offloads CORS configurations. While JWTs are issued by the User Service, the Gateway routes them seamlessly to internal services where Role-Based Access Control (RBAC) validates them.
*   **Swagger Aggregation:** It collects OpenAPI definitions from all underlying microservices and presents them in a single, unified Swagger UI.

### 2. User Service (`user-service` - Port 8081)
*   **Logic:** Owns the `user_schema` in PostgreSQL. Handles registration and authentication.
*   **Auth Flow:** Validates credentials and issues a stateless JSON Web Token (JWT) signed with `APP_JWT_SECRET`. Contains claims for `userId` and `role` (PO, DEV, ADMIN).

### 3. Project Management Service (`pm-service` - Port 8082)
*   **Logic:** Manages the Agile hierarchy: `Projects -> Sprints`. Manages project memberships.
*   **CQRS Architecture:** 
    *   **Writes:** Uses PostgreSQL for strict ACID compliance when creating projects or sprints.
    *   **Reads:** As tasks are completed, metrics (like Sprint Burndown or Project Velocity) are projected into **MongoDB** documents, allowing analytical queries to run blazing fast without locking the relational database.

### 4. Task Service (`task-service` - Port 8083)
*   **Logic:** Manages individual tickets, estimates, and status transitions (`TODO` -> `IN_PROGRESS` -> `DONE`).
*   **Event Publishing:** When a task status is patched, the service persists the change to Postgres, and fires a `TaskUpdatedEvent` to the RabbitMQ exchange.

### 5. Notification Service (`notification-service` - Port 8084)
*   **Logic:** A completely headless service (no REST controllers). It connects directly to RabbitMQ as a consumer.
*   **Event Consuming:** Listens for `TaskUpdatedEvent`. When received, it processes the payload and uses SMTP to dispatch async email notifications to the reporter (e.g., letting Fatima know Tariq started a task).

---

## 🚢 4. DevOps & Kubernetes Strategy

The ecosystem is fully containerized and orchestrated by Kubernetes (`microk8s`), making it production-ready and highly resilient.

### Stateless App Deployments
All Java services are deployed as K8s `Deployment` resources with `replicas: 1`. 
*   **Auto-Scaling Readiness:** Because JWTs are used for authentication (no sticky sessions), and RabbitMQ handles round-robin message distribution, these deployments can be scaled to `replicas: 5` instantly without breaking state or duplicating emails.
*   **Probes:** Every deployment includes `livenessProbe` and `readinessProbe` checking the application's TCP socket to ensure traffic is only routed to healthy JVMs.

### Stateful Database Deployments
PostgreSQL, MongoDB, and RabbitMQ are deployed with `PersistentVolumeClaims` (PVCs) using the `microk8s-hostpath` storage class. This guarantees that if a database pod crashes or is deleted, the data survives on the host disk and automatically reattaches when the pod restarts.

### Configuration Injection
*   **ConfigMap:** Non-sensitive data (DB URLs, RabbitMQ hosts) are stored in `agile-config` and injected as environment variables.
*   **Secrets:** Passwords and the `JWT_SECRET` are base64 encoded in `agile-secret` and securely injected, keeping them out of source control.

---

## 👁️ 5. Observability & Monitoring Stack

The project features a complete, zero-code-change observability stack. By utilizing the `spring-boot-starter-actuator` dependency present in the microservices, K8s agents can seamlessly scrape data.

### The Tools
1. **Promtail (DaemonSet):** Runs exactly one instance on every Kubernetes node. It mounts the node's `/var/log` directory, automatically discovers all pods running in the `agile-app` namespace, and streams their console output.
2. **Loki (Deployment + PVC):** The central log aggregator (the database for logs). It receives streams from Promtail, indexes them by K8s labels (`namespace`, `service`, `pod`), and stores the raw chunks on disk.
3. **Grafana (Deployment + PVC):** The visualization UI (exposed on Port `3000`). It connects to Loki and provides a search interface.

### Advanced Capabilities Implemented
*   **Cross-Service Tracing:** You can query `{service=~"task-service|notification-service"}` in Grafana to watch a REST request hit the Task Service, and milliseconds later watch the Notification Service process the resulting RabbitMQ event.
*   **Error Dashboards:** Querying `{namespace="agile-app"} |= "ERROR"` instantly returns exceptions thrown across any of the 5 microservices.

---

## 🧪 6. Testing & Documentation Interfaces

Interacting with the cluster has been highly optimized through three main interfaces:

### 1. Unified Swagger UI
By visiting `http://localhost:8080/swagger-ui.html`, the API Gateway presents a dropdown menu. You can dynamically switch between the User, PM, and Task service OpenAPI specs. It acts as a live, interactive contract for frontend developers.

### 2. Postman Automation
The project includes two Postman collections built with advanced scripting:
*   **`Agile_Microservices_Postman_Collection.json`:** Covers every single API endpoint.
*   **`Workflow_Simulation_Postman_Collection.json`:** A streamlined 11-step scenario.
*   **Scripting Logic:** Both collections utilize Postman "Tests" scripts. When you hit "Login", Postman extracts the JWT and saves it as a `{{token}}` variable. When you create a Project, it saves the `{{project_id}}` UUID. This allows you to click through requests without ever manually copying and pasting UUIDs or Bearer tokens.

### 3. Markdown Roleplay Scenarios
The `WORKFLOW_SIMULATION_GUIDE.md` localizes the testing experience using mock personas (**Fatima** the PO, **Tariq** the DEV). It guides a user through creating a project, assigning a task, triggering the RabbitMQ climax, and checking the MongoDB analytical charts, effectively proving the architecture's success.
