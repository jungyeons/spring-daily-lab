#!/usr/bin/env bash
set -euo pipefail

POSTGRES_SERVICE="${POSTGRES_SERVICE:-postgres}"
DB_NAME="${DB_NAME:-daily_lab}"
DB_USER="${DB_USER:-daily_lab}"
VERIFY_DB="${DB_NAME}_restore_check_$$"
BACKUP_FILE="${1:-}"
TEMP_BACKUP=false
VERIFY_DB_CREATED=false

if [[ -z "$BACKUP_FILE" ]]; then
  BACKUP_FILE="$(mktemp "${TMPDIR:-/tmp}/daily-lab-backup.XXXXXX")"
  TEMP_BACKUP=true
else
  mkdir -p "$(dirname "$BACKUP_FILE")"
fi

cleanup() {
  if [[ "$VERIFY_DB_CREATED" == true ]]; then
    docker compose exec -T "$POSTGRES_SERVICE" dropdb --if-exists --force -U "$DB_USER" "$VERIFY_DB" >/dev/null
  fi
  if [[ "$TEMP_BACKUP" == true ]]; then
    rm -f "$BACKUP_FILE"
  fi
}
trap cleanup EXIT

if ! command -v docker >/dev/null 2>&1; then
  echo "docker is required" >&2
  exit 1
fi

if ! docker compose ps --status running --services | grep -Fxq "$POSTGRES_SERVICE"; then
  echo "docker compose service '$POSTGRES_SERVICE' must be running" >&2
  exit 1
fi

echo "Creating a consistent custom-format backup at $BACKUP_FILE"
docker compose exec -T "$POSTGRES_SERVICE" \
  pg_dump -U "$DB_USER" -d "$DB_NAME" --format=custom --no-owner --no-privileges \
  >"$BACKUP_FILE"

if [[ ! -s "$BACKUP_FILE" ]]; then
  echo "backup file is empty" >&2
  exit 1
fi

SOURCE_COUNT="$(docker compose exec -T "$POSTGRES_SERVICE" \
  psql -U "$DB_USER" -d "$DB_NAME" -Atc 'SELECT count(*) FROM growth_tasks')"

docker compose exec -T "$POSTGRES_SERVICE" \
  createdb -U "$DB_USER" --template=template0 "$VERIFY_DB"
VERIFY_DB_CREATED=true

docker compose exec -T "$POSTGRES_SERVICE" \
  pg_restore -U "$DB_USER" -d "$VERIFY_DB" --exit-on-error --no-owner --no-privileges \
  <"$BACKUP_FILE"

RESTORED_TABLE="$(docker compose exec -T "$POSTGRES_SERVICE" \
  psql -U "$DB_USER" -d "$VERIFY_DB" -Atc "SELECT to_regclass('public.growth_tasks')")"
RESTORED_COUNT="$(docker compose exec -T "$POSTGRES_SERVICE" \
  psql -U "$DB_USER" -d "$VERIFY_DB" -Atc 'SELECT count(*) FROM growth_tasks')"
MIGRATION_COUNT="$(docker compose exec -T "$POSTGRES_SERVICE" \
  psql -U "$DB_USER" -d "$VERIFY_DB" -Atc 'SELECT count(*) FROM flyway_schema_history WHERE success')"

if [[ "$RESTORED_TABLE" != "growth_tasks" ]]; then
  echo "restored database is missing growth_tasks" >&2
  exit 1
fi
if [[ "$SOURCE_COUNT" != "$RESTORED_COUNT" ]]; then
  echo "row count mismatch: source=$SOURCE_COUNT restored=$RESTORED_COUNT" >&2
  exit 1
fi
if [[ "$MIGRATION_COUNT" -lt 1 ]]; then
  echo "restored database has no successful Flyway migrations" >&2
  exit 1
fi

echo "Backup verified: tasks=$RESTORED_COUNT migrations=$MIGRATION_COUNT"
