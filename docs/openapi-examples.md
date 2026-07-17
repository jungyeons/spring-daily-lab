# OpenAPI request and response examples

The interactive API reference is available at `/swagger-ui/index.html`, and the machine-readable OpenAPI document is available at `/v3/api-docs`.

## Create a task

```http
POST /api/v1/tasks HTTP/1.1
Content-Type: application/json

{
  "title": "Prepare weekly review",
  "description": "Summarize completed work and next priorities",
  "category": "DOCUMENTATION",
  "priority": 3,
  "dueDate": "2030-12-31"
}
```

```http
HTTP/1.1 201 Created
Location: /api/v1/tasks/1
Content-Type: application/json

{
  "id": 1,
  "title": "Prepare weekly review",
  "description": "Summarize completed work and next priorities",
  "category": "DOCUMENTATION",
  "status": "TODO",
  "priority": 3,
  "dueDate": "2030-12-31",
  "createdAt": "2030-01-01T00:00:00Z",
  "updatedAt": "2030-01-01T00:00:00Z",
  "completedAt": null,
  "version": 0
}
```

## Filter task list

```http
GET /api/v1/tasks?status=TODO&category=DOCUMENTATION&page=0&size=20 HTTP/1.1
```

The response is a Spring Data page containing `content`, pagination metadata, and sort metadata. See the Swagger UI for the generated response schema and all current enum values.
