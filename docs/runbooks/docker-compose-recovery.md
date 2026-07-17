# Docker Compose failure diagnosis and recovery

This runbook covers the local Docker Compose stack: the `app` service and its PostgreSQL dependency. It assumes commands run from the repository root.

## Before changing state

Capture the current state and logs first. This preserves evidence and prevents an avoidable data-loss recovery.

```bash
docker compose ps
docker compose logs --timestamps --tail=200 app postgres
docker compose config
```

The PostgreSQL data lives in the named `daily-lab-data` volume. Do **not** run `docker compose down -v` until a backup has been taken or the data has been explicitly confirmed disposable.

## Normal startup verification

```bash
docker compose up --build -d
docker compose ps
curl --fail --retry 12 --retry-delay 2 http://localhost:8080/actuator/health
```

Expected state:

- `postgres` is `healthy` after `pg_isready` succeeds.
- `app` starts after PostgreSQL is healthy.
- `/actuator/health` returns HTTP 200.

## Diagnose common failures

### PostgreSQL is unhealthy or exits

```bash
docker compose logs --timestamps postgres
docker compose exec postgres pg_isready -U daily_lab -d daily_lab
docker compose exec postgres psql -U daily_lab -d daily_lab -c 'SELECT 1'
```

Check for a port conflict on `127.0.0.1:5432`, a full Docker disk, or a changed `POSTGRES_*` setting that no longer matches the existing volume. Do not change the database credentials while retaining the volume: PostgreSQL applies those initialization variables only when the data directory is empty.

### Application exits before becoming healthy

```bash
docker compose logs --timestamps app
docker compose exec app env | grep '^DB_'
docker compose exec postgres psql -U daily_lab -d daily_lab -c 'SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank'
```

Look for database connection errors, failed Flyway migrations, or an incompatible application image. Rebuild the application after source or dependency changes:

```bash
docker compose build --no-cache app
docker compose up -d --force-recreate app
```

### Health endpoint is unavailable from the host

```bash
docker compose ps
docker compose port app 8080
curl -i http://localhost:8080/actuator/health
```

The Compose file intentionally binds the application to `127.0.0.1:8080`; it is not reachable from other hosts. If the port is in use, stop the conflicting local process or change the host-side port mapping deliberately.

## Safe recovery sequence

For transient container or network issues, preserve the volume and recreate services:

```bash
docker compose down
docker compose up --build -d
docker compose ps
curl --fail --retry 12 --retry-delay 2 http://localhost:8080/actuator/health
```

For a suspected data problem, follow the backup and restore procedure before resetting anything. After a verified backup, a disposable local environment can be reset with:

```bash
docker compose down -v
docker compose up --build -d
```

This permanently removes `daily-lab-data`. Confirm that the reset application starts, Flyway creates the schema, and the health endpoint returns 200. Record the original logs, recovery decision, and backup location in the incident notes.
