#!/usr/bin/env bash

echo "=== Tearing Down Agile Microservices Kubernetes Deployment ==="

# Delete ingress
kubectl delete -f ingress.yaml --ignore-not-found=true

# Delete deployments
kubectl delete -f services/api-gateway.yaml --ignore-not-found=true
kubectl delete -f services/notification-service.yaml --ignore-not-found=true
kubectl delete -f services/task-service.yaml --ignore-not-found=true
kubectl delete -f services/pm-service.yaml --ignore-not-found=true
kubectl delete -f services/user-service.yaml --ignore-not-found=true

# Delete databases
kubectl delete -f databases/rabbitmq.yaml --ignore-not-found=true
kubectl delete -f databases/mongodb.yaml --ignore-not-found=true
kubectl delete -f databases/postgres.yaml --ignore-not-found=true

# Delete configs and secrets
kubectl delete -f secret.yaml --ignore-not-found=true
kubectl delete -f configmap.yaml --ignore-not-found=true

# Delete namespace
kubectl delete -f namespace.yaml --ignore-not-found=true

echo "=== Teardown Completed ==="
