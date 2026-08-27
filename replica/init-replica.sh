#!/bin/bash

set -e

PRIMARY_HOST="postgres-primary"
PRIMARY_PORT="5432"
PRIMARY_DATABASE="photoshare"
PRIMARY_USER="admin"

REPLICATION_USER="replicator"
REPLICATION_PASSWORD="replicator"

PGDATA="${PGDATA:-/var/lib/postgresql/data}"

echo "Waiting for PostgreSQL primary..."

until pg_isready \
  -h "$PRIMARY_HOST" \
  -p "$PRIMARY_PORT" \
  -U "$PRIMARY_USER" \
  -d "$PRIMARY_DATABASE"
do
  echo "Primary is not ready. Retrying..."
  sleep 2
done

echo "PostgreSQL primary is available."

if [ ! -s "$PGDATA/PG_VERSION" ]; then

  echo "Replica data directory is empty."
  echo "Starting base backup from primary..."

  rm -rf "${PGDATA:?}"/*

  export PGPASSWORD="$REPLICATION_PASSWORD"

  gosu postgres pg_basebackup \
    -h "$PRIMARY_HOST" \
    -p "$PRIMARY_PORT" \
    -U "$REPLICATION_USER" \
    -D "$PGDATA" \
    -Fp \
    -Xs \
    -P \
    -R

  chown -R postgres:postgres "$PGDATA"
  chmod 700 "$PGDATA"

  echo "Base backup completed."

else

  echo "Replica data directory already initialized."
  echo "Skipping base backup."

fi

echo "Starting PostgreSQL replica..."

exec docker-entrypoint.sh postgres