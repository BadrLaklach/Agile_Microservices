# Agile Microservices: DevOps & Kubernetes Deployment Guide

This directory contains the Kubernetes (K8s) manifests, routing configs, and automation scripts used to orchestrate the Agile Microservices application. It is designed to work in local development clusters like **MicroK8s** or **K3s**.

---

## 📁 Directory Structure & Architecture

```
k8s/
├── namespace.yaml              # Isolated Namespace 'agile-app'
├── configmap.yaml              # Non-sensitive configuration (Database URLs, Hosts)
├── secret.yaml                 # Sensitive parameters (Passwords, JWT secrets)
├── ingress.yaml                # Nginx Ingress routing for the API Gateway
├── deploy.sh                   # Automated deployment orchestration script
├── undeploy.sh                 # Safe teardown script
├── databases/                  # Stateful data services
│   ├── postgres.yaml           # Postgres StatefulSet + Init SQL ConfigMap + Service
│   ├── mongodb.yaml            # MongoDB StatefulSet + Service
│   └── rabbitmq.yaml           # RabbitMQ StatefulSet + Service
└── services/                   # Stateless Spring Boot microservices
    ├── api-gateway.yaml        # Spring Cloud API Gateway (exposes port 8080)
    ├── user-service.yaml       # User Service (port 8081)
    ├── pm-service.yaml         # Project Management Service (port 8082)
    ├── task-service.yaml       # Task Service (port 8083)
    └── notification-service.yaml # Headless background consumer daemon (no port)
```

### How it Works
1. **Namespace Isolation**: All components run in the `agile-app` namespace to prevent conflicts with host services.
2. **Stateful Layer**: Persistent volumes (PVCs) bind storage dynamically for Postgres, MongoDB, and RabbitMQ. Postgres dynamically seeds its schema using an injected init script.
3. **Stateless Microservices**: The 5 microservices read configurations from a shared `ConfigMap` and retrieve sensitive keys (passwords, JWT secrets) from a shared `Secret`.
4. **API Gateway & Routing**: The `api-gateway` routes HTTP requests to the respective services. The Nginx Ingress controller routes external traffic into the `api-gateway`.

---

## 🛠️ DevOps Fixes & Optimizations Implemented

During local cluster setup and testing, we diagnosed and resolved three critical environment issues:

### 1. Kubelet TLS Certificate Verification Bypass
* **Issue**: The host VM's IP address (`192.168.0.100`) was missing from the Kubelet certificate's Subject Alternative Names (SANs). This caused the API Server to fail TLS handshakes when running `kubectl logs` or `kubectl port-forward`.
* **Fix**: Commented out the `--kubelet-certificate-authority` flag in the MicroK8s API Server argument template located at `/var/snap/microk8s/current/args/kube-apiserver`. This forces the API Server to skip verifying the self-signed Kubelet certificate locally.

### 2. Database Liveness/Readiness Probe Timeouts
* **Issue**: By default, exec-based probes timeout after `1s`. On resource-constrained local environments, executing commands like `mongosh` (for MongoDB) or `rabbitmq-diagnostics` (for RabbitMQ) inside a container takes more than `1s` to boot the CLI, causing Kubernetes to repeatedly mark the pods as dead and kill them in an infinite `CrashLoopBackOff`.
* **Fix**: Updated `/k8s/databases/mongodb.yaml` and `/k8s/databases/rabbitmq.yaml` to specify `timeoutSeconds: 10` and `failureThreshold: 6` for both liveness and readiness probes.

### 3. Headless `notification-service` Probe Failures
* **Issue**: The `notification-service` is a background consumer daemon (consuming RabbitMQ messages) and does not bundle `spring-boot-starter-web`. As a result, it does not start a web server or listen on port `8084`. The existing `tcpSocket` probe on port `8084` was failing, causing it to crash-loop.
* **Fix**: Removed the `livenessProbe` and `readinessProbe` blocks from `/k8s/services/notification-service.yaml` since the container is purely headless and does not open a port.

---

## 📖 Complete DevOps Operations Command Guide

Follow this guide to start, monitor, verify, and stop the entire Kubernetes deployment.

### 1. Preparing the Environment (MicroK8s)

First, make sure MicroK8s is running and Ingress is enabled:
```bash
# Check if MicroK8s is running
sudo microk8s status

# If not running, start it:
sudo microk8s start

# Enable required addons (DNS and Nginx Ingress)
sudo microk8s enable dns ingress
```

### 2. Building & Loading Rebuilt Images
If you make code changes, rebuild the Docker images locally and import them directly into MicroK8s' containerd registry:
```bash
# Build the Docker images (multi-stage Dockerfiles will compile the Java code internally)
sudo docker compose build

# Import the rebuilt host Docker images into containerd
sudo docker save user-service:v4 | sudo microk8s ctr images import -
sudo docker save pm-service:v3 | sudo microk8s ctr images import -
sudo docker save task-service:v3 | sudo microk8s ctr images import -
sudo docker save notification-service:v3 | sudo microk8s ctr images import -
sudo docker save api-gateway:v3 | sudo microk8s ctr images import -
```

### 3. Deploying the Application

Trigger the orchestration script to apply configurations in the correct dependency order:
```bash
cd k8s
chmod +x deploy.sh
./deploy.sh
```

### 4. Checking Cluster Status & Monitoring

Monitor the rollout progress and check the status of all pods:
```bash
# Get all pods in the namespace
kubectl get pods -n agile-app

# Monitor rollout logs for deployments
kubectl rollout status deployment/user-service -n agile-app
kubectl rollout status deployment/pm-service -n agile-app
```

### 5. Troubleshooting & Fetching Logs

If a pod is not entering `Running` status or is restarting:
```bash
# Check detailed events and lifecycle states (e.g. for task-service)
kubectl describe pod -l app=task-service -n agile-app

# Stream logs of a running container
kubectl logs -f deployment/user-service -n agile-app

# Check logs of a container BEFORE its last crash/restart
kubectl logs mongodb-0 -n agile-app --previous --tail=50
```

### 6. Verifying API Gateway Routing
Test the routing configuration by forwarding port `8888` on host to port `8080` (API Gateway) in the cluster:
```bash
# Start port-forwarding in the background
kubectl port-forward service/api-gateway 8888:8080 -n agile-app > /dev/null &

# Wait 3 seconds, then test a route (returns 401 Unauthorized because of missing JWT)
curl -i http://127.0.0.1:8888/api/v1/users/me

# Kill the background port-forward process
kill $!
```
*Expected Response:*
`{"detail":"Access token is missing or invalid","instance":"/api/v1/users/me","status":401,"title":"Unauthorized"}`

### 7. Stopping and Tearing Down

To stop all resources and clean up, run:
```bash
# Undeploy all K8s manifests (deletes PVs and configs)
cd k8s
chmod +x undeploy.sh
./undeploy.sh

# Completely stop MicroK8s services to free host CPU/RAM
sudo microk8s stop
```
