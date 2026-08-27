#!/bin/bash

set -e

echo "Creating PostgreSQL replication user..."

psql \
  -v ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" <<'SQL'

CREATE ROLE replicator
WITH
    REPLICATION
    LOGIN
    PASSWORD 'replicator';

SQL

echo "Allowing replication connections..."

cat >> "$PGDATA/pg_hba.conf" <<'HBA'

# Allow the replicator role to connect from the Docker network.
host replication replicator all scram-sha-256
HBA

echo "Primary replication configuration completed."