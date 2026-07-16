# Spring Daily Lab

[한국어](README.md) | English

A Spring Boot REST API for recording daily learning and development tasks, then managing their status and due dates. It starts as a small CRUD example while leaving room for the security, observability, and data-quality capabilities required in production.

## Technology

- Java 21
- Spring Boot 4.1.0
- Gradle 9.5.1 Wrapper
- Spring Web MVC, Validation, Data JPA, Actuator
- Flyway
- H2 (default local runtime), PostgreSQL (Docker Compose)
- JUnit 5, AssertJ, Spring Boot Test

## Run

Java 21 is the only prerequisite. The default configuration uses an in-memory H2 database.

```bash
./gradlew bootRun
```

After the application starts, use these URLs:

- API: `http://localhost:8080/api/v1/tasks`
- Health: `http://localhost:8080/actuator/health`
- H2 console: `http://localhost:8080/h2-console`

To run PostgreSQL and the application together:

```bash
docker compose up --build
```

## API examples

Create a task:

```bash
curl -i http://localhost:8080/api/v1/tasks \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Add service tests",
    "description": "Verify status changes and summary statistics",
    "category": "TEST",
    "priority": 4,
    "dueDate": "2030-12-31"
  }'
```

List and filter tasks:

```bash
curl 'http://localhost:8080/api/v1/tasks?status=TODO&category=TEST&page=0&size=20'
```

Change status:

```bash
curl -X PATCH http://localhost:8080/api/v1/tasks/1/status \
  -H 'Content-Type: application/json' \
  -d '{"status":"DONE"}'
```

Get the summary:

```bash
curl http://localhost:8080/api/v1/tasks/summary
```

## Endpoints

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/api/v1/tasks` | Create a task |
| `GET` | `/api/v1/tasks` | Paginated list with status and category filters |
| `GET` | `/api/v1/tasks/{id}` | Get one task |
| `PUT` | `/api/v1/tasks/{id}` | Replace a task |
| `PATCH` | `/api/v1/tasks/{id}/status` | Change task status |
| `DELETE` | `/api/v1/tasks/{id}` | Delete a task |
| `GET` | `/api/v1/tasks/summary` | Status and overdue summary |

Invalid requests use RFC 9457 Problem Details responses.

## Verification

```bash
./gradlew check
```

CI runs the same checks for pull requests and pushes to `main`.

## Database configuration

Connect an external database, such as PostgreSQL, with environment variables.

| Variable | Default |
| --- | --- |
| `DB_URL` | `jdbc:h2:mem:dailylab;...` |
| `DB_USERNAME` | `sa` |
| `DB_PASSWORD` | Empty |
| `H2_CONSOLE_ENABLED` | `true` |

Only [Flyway migrations](src/main/resources/db/migration/V1__create_growth_tasks.sql) may change the schema.

## Daily automation rules

Daily automation reviews unchecked [roadmap items](ROADMAP.md), issues, pull requests, and the current code before proposing up to ten independent draft pull requests.

- It never creates empty commits, date-only changes, or meaningless formatting churn.
- Each pull request contains one explainable improvement.
- It does not duplicate work already covered by an open pull request.
- Every behavior change updates relevant tests and documentation and passes `./gradlew check`.
- If no worthwhile work is available, it stops instead of manufacturing contributions.

See [AGENTS.md](AGENTS.md) for the full working rules.

## License

[MIT License](LICENSE)
