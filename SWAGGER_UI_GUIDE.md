# Interactive API Testing with Swagger UI

This guide provides a comprehensive, step-by-step walkthrough for accessing and testing the Agile Microservices ecosystem using the built-in, interactive **Swagger UI** (OpenAPI 3.0).

Unlike traditional `curl` testing, Swagger provides a beautifully rendered web interface where you can visualize API endpoints, inspect request/response schemas, and execute live HTTP requests directly from your browser.

---

## 🏗️ Architecture Context

The Agile Microservices are secured behind an **API Gateway**, which acts as the single entry point for all traffic. To provide a seamless developer experience, the API Gateway automatically aggregates the Swagger documentation from all downstream microservices!

You no longer need to access each microservice individually. The Gateway provides a centralized dropdown dashboard.

To access the Gateway locally, we utilize **Kubernetes Port-Forwarding** to securely tunnel traffic from your local machine directly into the gateway pod.

---

## 🚀 Step 1: Establish Secure Port-Forward

Open a new terminal window and establish a tunnel to the API Gateway:

```bash
kubectl port-forward svc/api-gateway -n agile-app 8080:8080
```

*Leave this terminal window open and running in the background.*

---

## 🌐 Step 2: Navigate to the Unified Swagger Dashboard

Once your port-forward command is running and says `Forwarding from 127.0.0.1...`, open your favorite web browser and navigate to:

[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

In the top-right corner of the dashboard, you will see a **"Select a definition"** dropdown. You can use this to seamlessly switch between:
1. **User Service** (Authentication & Users)
2. **Project Management Service** (Projects & Sprints)
3. **Task Service** (Tasks & Summaries)

*(Note: The API Gateway itself and the headless Notification Service do not have public APIs of their own).*

---

## 🔐 Step 3: Obtain Your JWT Token

Almost all endpoints in the Agile Microservices require a valid **JSON Web Token (JWT)**. You must generate this token via the User Service before testing the other endpoints.

1. Select **User Service** from the top-right dropdown.
2. Scroll down to the `auth-controller` section and click on the **`POST /api/v1/auth/login`** endpoint to expand it.
   *(Alternatively, use `POST /api/v1/auth/register` to create a new user).*
3. Click the **"Try it out"** button in the top right corner of the expanded panel.
4. Modify the `Request body` with your credentials:
   ```json
   {
     "email": "admin@agile.com",
     "password": "securepassword123"
   }
   ```
5. Click the large blue **"Execute"** button.
6. Scroll down to the **Server response** section. Inside the `Response body`, highlight and **Copy the `token` string** (do not copy the quotes).

---

## 🛡️ Step 4: Authorize Swagger UI

Now that you have your JWT token, you need to tell Swagger to attach it to all your future requests automatically.

1. At the very top of the Swagger UI page, look for the green **"Authorize 🔓"** button.
2. Click the button. A modal window will appear.
3. In the **`Value`** field under `bearerAuth`, **Paste** your JWT token.
4. Click **Authorize**, and then click **Close**.

> **Note:** The padlock icons next to the endpoints will now appear locked (🔒), indicating that Swagger will automatically inject your `Authorization: Bearer <token>` header into every request you make!
>
> **Pro-Tip:** Because you are using the unified API Gateway, your authorization token persists even when you switch definitions in the top-right dropdown!

---

## 🧪 Step 5: Execute Endpoints Live

With your token injected, you are fully authorized to test the ecosystem!

### Example: Creating a Project
1. Select **Project Management Service** from the top-right dropdown.
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
Because the microservices share the same underlying PostgreSQL databases and JWT secret keys, the data you create in one service will immediately be recognized by the others. 

*   Create a project in the **PM Service**.
*   Grab the returned `projectId`.
*   Switch the dropdown to the **Task Service**, paste that `projectId` into the `POST /api/v1/tasks` body, and create a task!

---

## 🛑 Troubleshooting Guide

*   **Page Won't Load / Connection Refused:** Make sure your `kubectl port-forward` command is actively running in the terminal. If it crashed or you closed the terminal, rerun it.
*   **401 Unauthorized:** Your JWT token may have expired, or you forgot to click the **"Authorize 🔓"** button at the top of the page.
*   **404 Not Found / 500 Internal Error:** Ensure you are passing the correct IDs (e.g., trying to add a task to a `projectId` that doesn't exist yet).
