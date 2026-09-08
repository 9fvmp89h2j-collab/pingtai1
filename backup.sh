#!/usr/bin/env bash
set -Eeuo pipefail
cd "$(dirname "$0")"
stamp="$(date +%Y%m%d-%H%M%S)"
target="backups/$stamp"
mkdir -p "$target"
docker compose exec -T mysql sh -c \
  'exec mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --single-transaction --routines --triggers "$MYSQL_DATABASE"' \
  > "$target/heritage_db.sql"
tar -czf "$target/files.tar.gz" -C runtime files
echo "Backup created: $target"
