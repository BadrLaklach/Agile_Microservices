#!/bin/bash
# 1. Register a user in user-service
USER1_ID=$(curl -s -X POST http://localhost:8081/api/v1/auth/register -H "Content-Type: application/json" -d '{"firstName": "Admin", "lastName": "User", "email": "admin5@test.com", "password": "password", "role": "ADMIN"}' | grep -o '"id":"[^"]*' | cut -d'"' -f4)
echo "Registered user 1: $USER1_ID"

# 2. Register another user in user-service
USER2_ID=$(curl -s -X POST http://localhost:8081/api/v1/auth/register -H "Content-Type: application/json" -d '{"firstName": "Normal", "lastName": "User", "email": "dev5@test.com", "password": "password", "role": "DEV"}' | grep -o '"id":"[^"]*' | cut -d'"' -f4)
echo "Registered user 2: $USER2_ID"

# 3. Create a project via pm-service using user 1
PROJECT_ID=$(curl -s -X POST http://localhost:8082/api/v1/projects -H "Content-Type: application/json" -H "X-User-Id: $USER1_ID" -H "X-User-Role: ADMIN" -d '{"name": "Test Project", "description": "Test", "methodology": "SCRUM", "startDate": "2026-06-01", "endDate": "2026-12-31"}' | grep -o '"id":"[^"]*' | cut -d'"' -f4)
echo "Created project: $PROJECT_ID"

# 4. Add existing user (user 2) to project
echo "Adding existing member..."
curl -s -i -X POST http://localhost:8082/api/v1/projects/$PROJECT_ID/members \
  -H "Content-Type: application/json" \
  -H "X-User-Id: $USER1_ID" \
  -H "X-User-Role: ADMIN" \
  -d '{"email": "dev5@test.com", "role": "DEV", "isNew": false}'
echo ""

# 5. Add new user to project
echo "Adding new member..."
curl -s -i -X POST http://localhost:8082/api/v1/projects/$PROJECT_ID/members \
  -H "Content-Type: application/json" \
  -H "X-User-Id: $USER1_ID" \
  -H "X-User-Role: ADMIN" \
  -d '{"email": "new5@test.com", "firstName": "New", "lastName": "User", "password": "password123", "role": "PO", "isNew": true}'
echo ""

# 6. Check pm-service rabbitmq logs
echo "Checking pm-service rabbitmq logs..."
docker logs agile_microservices_pm-service_1 2>&1 | grep "PUBLISHING EVENT" | tail -n 5
