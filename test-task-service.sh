#!/bin/bash
set -e

echo "1. Registering user..."
USER1_ID=$(curl -s -X POST http://localhost:8081/api/v1/auth/register -H "Content-Type: application/json" -d '{"firstName": "Task", "lastName": "Admin", "email": "taskadmin2@test.com", "password": "password", "role": "ADMIN"}' | grep -o '"id":"[^"]*' | cut -d'"' -f4)
echo "User 1 ID: $USER1_ID"

echo "2. Creating project via PM-Service..."
PROJECT_ID=$(curl -s -X POST http://localhost:8082/api/v1/projects -H "Content-Type: application/json" -H "X-User-Id: $USER1_ID" -H "X-User-Role: ADMIN" -d '{"name": "Task Test Project 2", "description": "Test", "methodology": "SCRUM", "startDate": "2026-06-01", "endDate": "2026-12-31"}' | grep -o '"id":"[^"]*' | cut -d'"' -f4)
echo "Project ID: $PROJECT_ID"

echo "Sleeping for 3 seconds to let RabbitMQ sync member to task-service..."
sleep 3

echo "3. Creating a Task in Task-Service..."
CREATE_TASK_RES=$(curl -s -X POST http://localhost:8083/api/v1/tasks \
  -H "Content-Type: application/json" \
  -H "X-User-Id: $USER1_ID" \
  -H "X-User-Role: ADMIN" \
  -d '{"title": "First Task", "description": "Task description", "type": "USER_STORY", "priority": "HIGH", "projectId": "'$PROJECT_ID'", "estimate": 5}')
echo "Create Task Response: $CREATE_TASK_RES"
TASK_ID=$(echo $CREATE_TASK_RES | grep -o '"id":"[^"]*' | cut -d'"' -f4)
echo "Task ID: $TASK_ID"

echo "4. Getting the Task..."
curl -s -X GET http://localhost:8083/api/v1/tasks/$TASK_ID \
  -H "X-User-Id: $USER1_ID" \
  -H "X-User-Role: ADMIN"
echo ""

echo "5. Updating the Task..."
curl -s -X PUT http://localhost:8083/api/v1/tasks/$TASK_ID \
  -H "Content-Type: application/json" \
  -H "X-User-Id: $USER1_ID" \
  -H "X-User-Role: ADMIN" \
  -d '{"title": "Updated Task", "description": "Updated desc", "type": "USER_STORY", "priority": "MEDIUM", "projectId": "'$PROJECT_ID'", "estimate": 8}'
echo ""

echo "6. Updating the Task Status..."
curl -s -X PATCH http://localhost:8083/api/v1/tasks/$TASK_ID/status \
  -H "Content-Type: application/json" \
  -H "X-User-Id: $USER1_ID" \
  -H "X-User-Role: ADMIN" \
  -d '{"status": "IN_PROGRESS"}'
echo ""

echo "7. Listing Tasks for Project..."
curl -s -X GET "http://localhost:8083/api/v1/tasks?projectId=$PROJECT_ID" \
  -H "X-User-Id: $USER1_ID" \
  -H "X-User-Role: ADMIN"
echo ""

echo "8. Getting Task Summary..."
curl -s -X GET "http://localhost:8083/api/v1/tasks/summary?projectId=$PROJECT_ID" \
  -H "X-User-Id: $USER1_ID" \
  -H "X-User-Role: ADMIN"
echo ""

echo "9. Check Task-Service RabbitMQ logs for published events..."
docker logs agile_microservices_task-service_1 2>&1 | grep "PUBLISHING RABBITMQ EVENT" | tail -n 5

echo "10. Deleting the Task..."
curl -s -i -X DELETE http://localhost:8083/api/v1/tasks/$TASK_ID \
  -H "X-User-Id: $USER1_ID" \
  -H "X-User-Role: ADMIN"
echo ""

