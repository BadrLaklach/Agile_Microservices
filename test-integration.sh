#!/bin/bash
set -e

echo "1. Registering user..."
USER1_ID=$(curl -s -X POST http://localhost:8081/api/v1/auth/register -H "Content-Type: application/json" -d '{"firstName": "Integration", "lastName": "Admin", "email": "integadmin3@test.com", "password": "password", "role": "ADMIN"}' | grep -o '"id":"[^"]*' | cut -d'"' -f4)
echo "User 1 ID: $USER1_ID"

echo "2. Creating project via PM-Service..."
PROJECT_ID=$(curl -s -X POST http://localhost:8082/api/v1/projects -H "Content-Type: application/json" -H "X-User-Id: $USER1_ID" -H "X-User-Role: ADMIN" -d '{"name": "Integration Project", "description": "Test", "methodology": "SCRUM", "startDate": "2026-06-01", "endDate": "2026-12-31"}' | grep -o '"id":"[^"]*' | cut -d'"' -f4)
echo "Project ID: $PROJECT_ID"

echo "Sleeping for 2 seconds to let RabbitMQ sync member..."
sleep 2

echo "3. Creating a Task in Task-Service..."
CREATE_TASK_RES=$(curl -s -X POST http://localhost:8083/api/v1/tasks \
  -H "Content-Type: application/json" \
  -H "X-User-Id: $USER1_ID" \
  -H "X-User-Role: ADMIN" \
  -d '{"title": "Int Task", "description": "Task desc", "type": "USER_STORY", "priority": "HIGH", "projectId": "'$PROJECT_ID'", "estimate": 5}')
echo "Create Task Response: $CREATE_TASK_RES"
TASK_ID=$(echo $CREATE_TASK_RES | grep -o '"id":"[^"]*' | cut -d'"' -f4)
echo "Task ID: $TASK_ID"

echo "Sleeping for 2 seconds to let PM-Service consume TASK_CREATED..."
sleep 2

echo "4. Updating the Task..."
curl -s -X PUT http://localhost:8083/api/v1/tasks/$TASK_ID \
  -H "Content-Type: application/json" \
  -H "X-User-Id: $USER1_ID" \
  -H "X-User-Role: ADMIN" \
  -d '{"title": "Updated Int Task", "description": "Updated desc", "type": "USER_STORY", "priority": "MEDIUM", "projectId": "'$PROJECT_ID'", "estimate": 8}'
echo ""

echo "Sleeping for 2 seconds to let PM-Service consume TASK_UPDATED..."
sleep 2

echo "5. Updating the Task Status..."
curl -s -X PATCH http://localhost:8083/api/v1/tasks/$TASK_ID/status \
  -H "Content-Type: application/json" \
  -H "X-User-Id: $USER1_ID" \
  -H "X-User-Role: ADMIN" \
  -d '{"status": "IN_PROGRESS"}'
echo ""

echo "Sleeping for 2 seconds to let PM-Service consume TASK_STATUS_CHANGED..."
sleep 2

echo "6. Deleting the Task..."
curl -s -X DELETE http://localhost:8083/api/v1/tasks/$TASK_ID \
  -H "X-User-Id: $USER1_ID" \
  -H "X-User-Role: ADMIN"
echo ""

echo "Sleeping for 2 seconds to let PM-Service consume TASK_DELETED..."
sleep 2

echo "7. Checking PM-Service logs for TaskEventConsumer..."
docker logs agile_microservices_pm-service_1 --since 15s 2>&1 | grep "task event" || true
docker logs agile_microservices_pm-service_1 --since 15s 2>&1 | grep "task projection" || true
docker logs agile_microservices_pm-service_1 --since 15s 2>&1 | grep "task status" || true
