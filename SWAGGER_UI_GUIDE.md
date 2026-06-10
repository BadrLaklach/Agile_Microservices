# Interactive API Testing with Swagger UI

This guide provides a comprehensive, step-by-step walkthrough for accessing and testing the Agile Microservices ecosystem using the built-in, interactive **Swagger UI** (OpenAPI 3.0).

Unlike traditional `curl` testing, Swagger provides a beautifully rendered web interface where you can visualize API endpoints, inspect request/response schemas, and execute live HTTP requests directly from your browser.

---

## 🏗️ Architecture Context

Because the Agile Microservices are secured behind an **API Gateway** and an **Ingress Controller**, internal documentation routes (like `/swagger-ui.html`) are not exposed to the public internet for security reasons. 

To access these interfaces locally, we utilize **Kubernetes Port-Forwarding** to securely tunnel traffic from your local machine directly into the internal microservice pods.

There are three microservices that expose REST APIs and have Swagger UI enabled:
1. **User Service** (Port `8081`)
2. **Project Management (PM) Service** (Port `8082`)
3. **Task Service** (Port `8083`)

*(Note: The API Gateway and Notification Service do not have Swagger UIs, as the Gateway is a router and the Notification Service is a headless RabbitMQ consumer).*

---

## 🚀 Step 1: Establish Secure Port-Forwards

Open a new terminal window. To access a service's Swagger UI, you must first open a tunnel to it.

> **Pro-Tip:** If you want to test multiple services simultaneously, open a separate terminal tab for each command and let them run in the background.

**To access the User Service (Authentication & Users):**
```bash
kubectl port-forward svc/user-service -n agile-app 8081:8081
```

**To access the PM Service (Projects & Sprints):**
```bash
kubectl port-forward svc/pm-service -n agile-app 8082:8082
```

**To access the Task Service (Tasks & Summaries):**
```bash
kubectl port-forward svc/task-service -n agile-app 8083:8083
```

---

## 🌐 Step 2: Navigate to the Swagger Dashboard

Once your port-forward command is running and says `Forwarding from 127.0.0.1...`, open your favorite web browser and navigate to the corresponding URL:

*   **User Service UI:** [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
*   **PM Service UI:** [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
*   **Task Service UI:** [http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html)

---

## 🔐 Step 3: Obtain Your JWT Token

Almost all endpoints in the Agile Microservices require a valid **JSON Web Token (JWT)**. You must generate this token in the **User Service** before testing the other services.

1. Navigate to the **User Service UI** (`http://localhost:8081/swagger-ui.html`).
2. Scroll down to the `auth-controller` section and click on the **`POST /api/v1/auth/login`** endpoint to expand it.
3. Click the **"Try it out"** button in the top right corner of the expanded panel.
4. Modify the `Request body` with your credentials:
   ```json
   {
     "email": "alice.manager@example.com",
     "password": "SecurePassword123"
   }
   ```
5. Click the large blue **"Execute"** button.
6. Scroll down to the **Server response** section. Inside the `Response body`, highlight and **Copy the `token` string** (do not copy the quotes).

---

## 🛡️ Step 4: Authorize Swagger UI

Now that you have your JWT token, you need to tell Swagger to attach it to all your future requests automatically.

1. At the very top of **ANY** Swagger UI page, look for the green **"Authorize 🔓"** button.
2. Click the button. A modal window will appear.
3. In the **`Value`** field under `bearerAuth`, **Paste** your JWT token.
4. Click **Authorize**, and then click **Close**.

> **Note:** The padlock icons next to the endpoints will now appear locked (🔒), indicating that Swagger will automatically inject your `Authorization: Bearer <token>` header into every request you make from this browser tab!
>
> *You must repeat this authorization step if you open a different service's Swagger UI in another tab.*

---

## 🧪 Step 5: Execute Endpoints Live

With your token injected, you are fully authorized to test the ecosystem!

### Example: Creating a Project
1. Ensure the **PM Service** is port-forwarded (`8082`) and you have clicked **"Authorize 🔓"** and pasted your token.
2. Expand **`POST /api/v1/projects`**.
3. Click **"Try it out"**.
4. Fill in the JSON body:
   ```json
   {
     "name": "Frontend Redesign",
     "description": "Moving from React to Next.js",
     "startDate": "2026-07-01",
     "endDate": "2026-08-01"
   }
   ```
5. Click **Execute**.
6. Check the **Server response** for a `201 Created` status!

### Seamless Ecosystem Testing
Because the microservices share the same underlying PostgreSQL databases and JWT secret keys, the data you create in one Swagger UI tab will immediately be recognized by the others. 

*   Create a project in the **PM Service UI**.
*   Grab the returned `projectId`.
*   Switch to the **Task Service UI**, paste that `projectId` into the `POST /api/v1/tasks` body, and create a task!

---

## 🛑 Troubleshooting Guide

*   **Page Won't Load / Connection Refused:** Make sure your `kubectl port-forward` command is actively running in the terminal. If it crashed or you closed the terminal, rerun it.
*   **401 Unauthorized:** Your JWT token may have expired, or you forgot to click the **"Authorize 🔓"** button at the top of the page.
*   **404 Not Found / 500 Internal Error:** Ensure you are passing the correct IDs (e.g., trying to add a task to a `projectId` that doesn't exist yet).
