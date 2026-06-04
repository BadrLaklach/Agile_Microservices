# Kubernetes Deployment Guide

This directory contains the Kubernetes manifests to orchestrate, configure, and route the Agile Microservices application.

---

## 📁 Directory Structure

```
k8s/
├── namespace.yaml         # Isolated namespace 'agile-app'
├── configmap.yaml         # Shared non-sensitive configuration keys
├── secret.yaml            # Sensitive database & message broker passwords
├── ingress.yaml           # Nginx Ingress routing for the API Gateway
├── deploy.sh              # Sequential orchestration shell script
├── undeploy.sh            # Safe cleanup script
├── databases/             # Stateful data services
│   ├── postgres.yaml      # Postgres StatefulSet + Init SQL ConfigMap + Service
│   ├── mongodb.yaml       # MongoDB StatefulSet + Service
│   └── rabbitmq.yaml      # RabbitMQ StatefulSet + Service
└── services/              # Stateless Spring Boot microservices
    ├── user-service.yaml  # Deployment + Service
    ├── pm-service.yaml    # Deployment + Service
    ├── task-service.yaml  # Deployment + Service
    ├── api-gateway.yaml   # Deployment + Service
    └── notification-service.yaml # Deployment + Service
```

---

## 🚀 How to Deploy

### 1. Prerequisites
- A running Kubernetes cluster (e.g., [Minikube](https://minikube.sigs.k8s.io/) or [Kind](https://kind.sigs.k8s.io/)).
- `kubectl` installed and configured.
- Nginx Ingress Controller enabled (required for Ingress routing).
  - *For Minikube:* `minikube addons enable ingress`

### 2. Quick Deploy
Run the automated deployment script:
```bash
./deploy.sh
```
This script ensures resources are applied in the correct dependency order:
`Namespace` ➡️ `ConfigMap/Secret` ➡️ `Databases (wait until healthy)` ➡️ `Services` ➡️ `Ingress`.

---

## ⚙️ Customizing Configurations

- **Non-sensitive settings** (ports, hostnames, mail hosts): Modify [configmap.yaml](configmap.yaml).
- **Passwords & Secrets** (database, mail, rabbitmq): Modify [secret.yaml](secret.yaml). Plaintext values under `stringData` will be encoded automatically when sent to the cluster.

---

## 🔍 Verification & Troubleshooting

### Check Pod Status
```bash
kubectl get pods -n agile-app
```

### View Pod Logs
For example, to debug database connections on Project Management:
```bash
kubectl logs -f deployment/pm-service -c pm-service -n agile-app
```

### Accessing the Management Consoles
- **RabbitMQ Dashboard:** Port-forward port `15672`:
  ```bash
  kubectl port-forward statefulset/rabbitmq 15672:15672 -n agile-app
  ```
  Open your browser to `http://localhost:15672` (default username/password is `guest`/`guest`).

---

## 🧹 Undeploying
To completely tear down the stack and delete all resources:
```bash
./undeploy.sh
```
*(Note: This deletes persistent volume claims, deleting database data).*
