#!/usr/bin/env bash

set -euo pipefail

migration_directory="${1:-src/main/resources/db/migration}"
name_pattern='^(V[1-9][0-9]*|R)__[a-z0-9]+(_[a-z0-9]+)*\.sql$'
invalid=0

while IFS= read -r -d '' migration; do
	name="$(basename "$migration")"
	if [[ ! "$name" =~ $name_pattern ]]; then
		echo "Invalid Flyway migration name: $name" >&2
		echo "Expected V<positive-number>__lowercase_words.sql or R__lowercase_words.sql" >&2
		invalid=1
	fi
done < <(find "$migration_directory" -type f -name '*.sql' -print0)

if [[ "$invalid" -ne 0 ]]; then
	exit 1
fi

echo "Flyway migration names are valid."
