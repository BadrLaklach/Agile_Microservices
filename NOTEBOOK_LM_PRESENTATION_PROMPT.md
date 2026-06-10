You are an expert Presentation Designer and Technical Communicator. I am providing you with a master architectural reference document (`PROJECT_ARCHITECTURE_AND_DEVOPS_REFERENCE.md`) and a system architecture illustration for an enterprise-grade "Agile Microservices" project. 

Your task is to generate the exact content, layout instructions, and illustration prompts for a **15-slide presentation** showcasing this project to stakeholders.

### 🎨 Design & Aesthetic Requirements
You must strictly enforce the following design language for every single slide to ensure a **clean, bright, basic, and incredibly smooth UI design**:

*   **Color Palette:**
    *   **Primary Backgrounds & Whitespace:** Light Gray/White `#EAEAEA` (rgb 234, 234, 234). The presentation should feel incredibly bright and airy.
    *   **Primary Text & Structural Lines:** Dark Charcoal `#252A34` (rgb 37, 42, 52). Used for strong readability and high contrast.
    *   **Primary Accent & Clean Shapes:** Bright Cyan `#08D9D6` (rgb 8, 217, 214). Used for headers, active elements, and UI borders.
    *   **Secondary Accent & Highlights:** Vibrant Pink `#FF2E63` (rgb 255, 46, 99). Used sparingly to draw attention to critical events (like errors, alerts, or messaging events).
*   **Vibe:** "Clean & Bright Modern UI." Use flat, smooth vector illustrations, plenty of negative space, soft rounded corners, and minimalist iconography. No dark-mode.
*   **Layout:** Keep layouts airy and uncluttered. Use a crisp two-column approach where possible (Detailed text on the left, clean and bright vector illustration on the right).

---

### 📝 Presentation Structure (Exactly 15 Slides)
Generate the detailed text content, speaker notes, and visual layout instructions for the following 15 slides, using the attached Markdown document as your source of truth.

**Slide 1: Title Slide**
*   **Visual:** A massive, bright `#EAEAEA` background. Crisp Charcoal `#252A34` text. A clean, minimalist geometric illustration using Cyan `#08D9D6` and Pink `#FF2E63` interlocking shapes representing microservices.
*   **Content:** "Agile Microservices Ecosystem" / "An Enterprise-Grade, Event-Driven Architecture."

**Slide 2: The Problem & The Solution**
*   **Visual:** Split screen. Left is a tangled knot (legacy monolith) in charcoal. Right is perfectly spaced, smooth Cyan squares connected by clean, straight lines.
*   **Content:** Moving from brittle monoliths to scalable, decoupled Spring Boot microservices orchestrated by Kubernetes.

**Slide 3: Core Technology Stack**
*   **Visual:** A grid of clean, bright UI cards with soft rounded corners and subtle drop shadows. Each card holds a minimalist tech logo (Java 21, Spring Boot 3, Postgres, Mongo, RabbitMQ, K8s).
*   **Content:** Highlight the "Best-in-Class" tooling used to build the system with specific versions.

**Slide 4: API Gateway (The Front Door)**
*   **Visual:** A smooth, flat vector illustration of an entry gate passing clean Cyan `#08D9D6` data packets to various destinations.
*   **Content:** Port 8080. Explain Spring Cloud Gateway, routing logic, security enforcement, and Swagger UI aggregation.

**Slide 5: User Service (Identity & Auth)**
*   **Visual:** A bright, minimalist ID badge or padlock illustration in Vibrant Pink `#FF2E63` against the light gray background.
*   **Content:** PostgreSQL backed. Stateless JWT issuance. Role-based access control (PO, DEV).

**Slide 6: Project Management Service (The Core)**
*   **Visual:** Two clean cylinder icons (databases) side-by-side. One Charcoal (Postgres), one Cyan (MongoDB).
*   **Content:** Manages Projects and Sprints. Emphasize the **CQRS Architecture** (PostgreSQL for strict writes, MongoDB for lightning-fast metric reads).

**Slide 7: Task Service (The Engine)**
*   **Visual:** A bright, flat-UI Kanban board with clean, rounded ticket rectangles.
*   **Content:** Manages task states. Introduce how it acts as the event producer when task statuses change.

**Slide 8: RabbitMQ & Event-Driven Architecture**
*   **Visual:** A central router icon radiating smooth Pink `#FF2E63` dashed lines to other services.
*   **Content:** Fully decoupled asynchronous communication. The Task Service publishes events, completely unaware of who consumes them.

**Slide 9: Notification Service (The Worker)**
*   **Visual:** A clean, flat vector of a flying envelope with a Cyan `#08D9D6` trail.
*   **Content:** Headless consumer. Listens for RabbitMQ events and dispatches asynchronous emails without blocking the UI.

**Slide 10: DevOps & Kubernetes Orchestration**
*   **Visual:** The K8s wheel icon inside a clean, structured grid representing pods and nodes.
*   **Content:** MicroK8s cluster. Auto-scaling stateless apps, Liveness/Readiness probes, ConfigMap/Secret injection.

**Slide 11: Stateful Data Persistence**
*   **Visual:** Clean database icons anchored to a solid Charcoal `#252A34` base line, illustrating permanence.
*   **Content:** PersistentVolumeClaims (PVCs) for Postgres, MongoDB, and RabbitMQ using `microk8s-hostpath`. Data survives restarts!

**Slide 12: Observability - The "Zero-Code" Monitoring Stack**
*   **Visual:** A bright UI magnifying glass hovering over structured, colorful logs.
*   **Content:** Utilizing Spring Boot Actuator. Introducing the Loki + Promtail + Grafana stack.

**Slide 13: Centralized Logging with Promtail & Loki**
*   **Visual:** Funnel illustration gathering clean blocks of data from various sources into one central, ordered list.
*   **Content:** Promtail as a K8s DaemonSet scraping `/var/log`. Loki indexing by K8s labels (namespace, service).

**Slide 14: Grafana & Cross-Service Tracing**
*   **Visual:** A clean, bright-UI mockup of a dashboard. Soft Cyan `#08D9D6` area charts and a crisp Pink `#FF2E63` line for errors.
*   **Content:** Showcasing LogQL capabilities. Tracing an event from the Task Service REST call all the way to the Notification Service RabbitMQ consumption in real-time.

**Slide 15: Developer Experience (Postman & Swagger)**
*   **Visual:** Minimalist Postman and Swagger UI icons inside clean, rounded UI windows.
*   **Content:** Automated Postman test collections with dynamic ID/JWT injection. The Unified Swagger UI contract.

---
**Instructions for Output:**
For each of the 15 slides, provide:
1. **Slide Title**
2. **Visual Instructions** (Describe exactly how to build the clean illustration using the `#EAEAEA` background, `#252A34` text, and `#08D9D6` / `#FF2E63` UI accents).
3. **Bullet Points** (Detailed, highly-technical text to place on the slide, sourced strictly from the uploaded architecture document).
4. **Speaker Notes** (What the presenter should say to elaborate on the bullet points).
