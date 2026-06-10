# 🚀 Agile Microservices: Complete Startup & Teardown Guide

This document is your single source of truth for booting up and tearing down the entire Agile Microservices ecosystem. If you just rebooted your PC, follow these steps in order to get the backend, the databases, and the observability stack running perfectly.

---

## 🟢 Part 1: Booting Up the System

### Step 1.1: Deploy the Microservices & Databases
Your project includes an automated deployment script that spins up the `agile-app` namespace, applies the configuration/secrets, creates the databases (PostgreSQL, MongoDB, RabbitMQ), and launches the 5 Java microservices.

Open your terminal in the root of the project and run:
```bash
cd k8s
chmod +x deploy.sh
./deploy.sh
```
> **Note:** The script will automatically wait for the databases to become healthy before rolling out the Java microservices. This takes about 1-2 minutes.

### Step 1.2: Deploy the Observability Stack (Loki + Grafana)
Next, deploy the monitoring tools that aggregate all your logs. These live in their own dedicated `monitoring` namespace.

From the root of the project, run:
```bash
kubectl apply -f k8s/monitoring/namespace.yaml
kubectl apply -f k8s/monitoring/loki.yaml
kubectl apply -f k8s/monitoring/promtail.yaml
kubectl apply -f k8s/monitoring/grafana.yaml
```

### Step 1.3: Verify Everything is Running
Before trying to use the app, ensure all pods are in the `Running` state:

Check the application pods:
```bash
kubectl get pods -n agile-app
```
*(You should see 8 pods: api-gateway, mongodb, notification, pm, postgres, rabbitmq, task, user)*

Check the monitoring pods:
```bash
kubectl get pods -n monitoring
```
*(You should see 3 pods: grafana, loki, promtail)*

---

## 🌐 Part 2: Port-Forwarding (Accessing the Services)

Because Kubernetes runs inside a virtual network, you need to open ports to your local machine so your browser and Postman can reach them. 

**Open two separate terminal windows** and leave them running in the background:

### Terminal 1: Expose the API Gateway
This exposes the single entry point for all your microservices on port `8080`. This is where Postman will send requests.
```bash
kubectl port-forward svc/api-gateway -n agile-app 8080:8080
```
> **Test it:** Open `http://localhost:8080/swagger-ui.html` in your browser.

### Terminal 2: Expose Grafana
This exposes your logging dashboard on port `3000`.
```bash
kubectl port-forward svc/grafana -n monitoring 3000:3000
```
> **Test it:** Open `http://localhost:3000` in your browser.
> **Login credentials:** Username: `admin` | Password: `agile-admin`

---

## 🧪 Part 3: Using the Application

With the system booted and ports exposed, you are ready to work!

1. **API Testing:** Open Postman and use the `Agile_Microservices_Postman_Collection.json` or `Workflow_Simulation_Postman_Collection.json`.
2. **Read the Guides:** 
   * Check `SWAGGER_UI_GUIDE.md` for OpenAPI specs.
   * Check `WORKFLOW_SIMULATION_GUIDE.md` for a step-by-step roleplay scenario.

---

## 🔴 Part 4: Tearing Down the System Safely

When you are done working for the day, it is highly recommended to tear down the Kubernetes resources to save battery, RAM, and CPU.

### Step 4.1: Kill the Port-Forwards
Go to the two terminal windows where you ran `kubectl port-forward` and press `Ctrl+C` to stop them.

### Step 4.2: Undeploy the Observability Stack
Delete the monitoring resources to free up space:
```bash
kubectl delete -f k8s/monitoring/grafana.yaml
kubectl delete -f k8s/monitoring/promtail.yaml
kubectl delete -f k8s/monitoring/loki.yaml
kubectl delete namespace monitoring
```

### Step 4.3: Undeploy the Microservices & Databases
Run your automated teardown script. This safely removes all services, pods, and configurations from the `agile-app` namespace.
```bash
cd k8s
chmod +x undeploy.sh
./undeploy.sh
```

### ✅ Clean State Achieved!
Your cluster is now clean. The `PersistentVolumeClaims` for your databases (Postgres/Mongo) will retain your data on your disk, so the next time you run `deploy.sh`, your users and projects will still be there!
