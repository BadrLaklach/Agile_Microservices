#!/usr/bin/env bash

# Exit immediately if a command exits with a non-zero status
set -e

echo "=== Starting Agile Microservices Kubernetes Deployment ==="

# 1. Create Namespace
echo "Creating namespace..."
kubectl apply -f namespace.yaml

# 2. Create ConfigMap & Secret
echo "Applying configurations and secrets..."
kubectl apply -f configmap.yaml
kubectl apply -f secret.yaml

# 3. Deploy Databases and RabbitMQ
echo "Deploying databases and message broker..."
kubectl apply -f databases/postgres.yaml
kubectl apply -f databases/mongodb.yaml
kubectl apply -f databases/rabbitmq.yaml

# Wait for databases to be ready
echo "Waiting for databases to be ready (health checks)..."
kubectl rollout status statefulset/postgres -n agile-app --timeout=90s || true
kubectl rollout status statefulset/mongodb -n agile-app --timeout=90s || true
kubectl rollout status statefulset/rabbitmq -n agile-app --timeout=90s || true

# 4. Deploy Microservices
echo "Deploying application microservices..."
kubectl apply -f services/user-service.yaml
kubectl apply -f services/pm-service.yaml
kubectl apply -f services/task-service.yaml
kubectl apply -f services/notification-service.yaml
kubectl apply -f services/api-gateway.yaml

# Wait for microservices rollout
echo "Waiting for microservices rollout..."
kubectl rollout status deployment/user-service -n agile-app --timeout=120s || true
kubectl rollout status deployment/pm-service -n agile-app --timeout=120s || true
kubectl rollout status deployment/task-service -n agile-app --timeout=120s || true
kubectl rollout status deployment/notification-service -n agile-app --timeout=120s || true
kubectl rollout status deployment/api-gateway -n agile-app --timeout=120s || true

# 5. Apply Ingress
echo "Applying ingress configuration..."
kubectl apply -f ingress.yaml

echo "=== Deployment Completed Successfully! ==="
echo "Access the app through your ingress controller IP/host."
echo "Use 'kubectl get pods -n agile-app' to monitor the status."
