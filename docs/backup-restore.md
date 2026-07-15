# PostgreSQL backup and restore runbook

This runbook covers the PostgreSQL database started by `compose.yaml`. The JSON API export is useful for data portability, but it does not replace a database backup because it omits migration metadata and database-level state.

## Recovery objectives

- Define the production RPO and RTO before launch. A reasonable starting point for this lab is a 24-hour RPO and a 60-minute RTO.
- Keep at least seven daily and four weekly backups in storage outside the application host.
- Encrypt backups at rest, restrict read access, and never commit dumps to Git.
- A backup is not considered usable until an isolated restore succeeds.

## Create and verify a backup

On a fresh environment, start the application once so Flyway initializes the database. For an existing environment, confirm PostgreSQL is running and healthy:

```bash
docker compose up -d
docker compose ps
```

Create a custom-format dump and restore it into an isolated temporary database in the same PostgreSQL container:

```bash
mkdir -p backups
./scripts/verify-postgres-backup.sh "backups/daily-lab-$(date -u +%Y%m%dT%H%M%SZ).dump"
```

The script uses `pg_dump`'s consistent snapshot, restores with `--exit-on-error`, verifies the `growth_tasks` table and successful Flyway history, compares source and restored task counts, then removes the temporary verification database. Supplying no path performs the same drill with a temporary dump that is deleted afterward.

Copy the verified dump to durable off-host storage and record its SHA-256 checksum:

```bash
shasum -a 256 backups/daily-lab-*.dump
```

## Restore after an incident

1. Stop application writes: `docker compose stop app`.
2. Preserve the damaged database volume or take a final dump when possible. Do not overwrite the only copy.
3. Verify the selected backup checksum and run `./scripts/verify-postgres-backup.sh /path/to/backup.dump` against a non-production PostgreSQL instance first.
4. Create a clean target database. Do not restore over a partially initialized schema.
5. Restore with the PostgreSQL client version matching or newer than the source:

```bash
docker compose exec -T postgres dropdb --if-exists --force -U daily_lab daily_lab
docker compose exec -T postgres createdb -U daily_lab --template=template0 daily_lab
docker compose exec -T postgres pg_restore \
  -U daily_lab -d daily_lab --exit-on-error --no-owner --no-privileges \
  </path/to/backup.dump
```

6. Check migration history and task count:

```bash
docker compose exec -T postgres psql -U daily_lab -d daily_lab \
  -c 'TABLE flyway_schema_history' \
  -c 'SELECT count(*) AS task_count FROM growth_tasks'
```

7. Start the application, confirm `/actuator/health`, exercise a read and a temporary write/delete, then reopen traffic.
8. Record the backup identifier, recovery duration, row count, application version, operator, and any errors in the incident log.

## Restore drill cadence

Run the verification script at least monthly and after PostgreSQL, Flyway, or schema changes. Alert on a failed dump, failed restore, checksum mismatch, missing migration history, or task-count mismatch. Review retention and recovery objectives quarterly.
