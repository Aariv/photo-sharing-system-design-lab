# Phase-16A: PostgreSQL Read Replicas and Read/Write Splitting

## Overview

The Photo Sharing System currently uses a single PostgreSQL database for both reads and writes.

```text
Application
    │
    ▼
PostgreSQL Primary

Reads  ✅
Writes ✅
```

This design is simple and correct, but read-heavy workloads such as the Home Feed can eventually compete with transactional writes for database resources.

Phase-16A introduces:

```text
PostgreSQL Primary
PostgreSQL Streaming Replica
Write-to-Primary Routing
Read-from-Replica Routing
Replica Lag
Read-Your-Writes Consistency
Read-Only Database Credentials
Failure and Fallback Considerations
```

The goal is not merely to run two PostgreSQL containers. The goal is to understand the consistency and reliability trade-offs created by read replicas.

---

# Learning Objectives

By completing this phase, I should understand:

```text
Why read replicas are introduced
Why writes must remain on the primary
How PostgreSQL WAL streaming replication works conceptually
How to verify primary and replica roles
How replica lag creates eventual consistency
Why a user may not immediately see a newly created post
How read-your-writes routing works
Why uncontrolled fallback can overload the primary
How separate connection pools isolate read and write workloads
```

---

# Current Architecture

```text
Home Feed API
Create Post API
Like API
Comment API
Follow API
      │
      ▼
PostgreSQL Primary
```

All traffic reaches one database instance.

Potential future pressure:

```text
10 writes/sec
100,000 reads/sec
```

Read-intensive endpoints can consume database CPU, memory, connections, and I/O needed by writes.

---

# Target Architecture

```text
                         PhotoShare API
                                │
                ┌───────────────┴────────────────┐
                │                                │
                ▼                                ▼
         Write DataSource                Feed Read DataSource
                │                                │
                ▼                                ▼
      PostgreSQL Primary  ───────────► PostgreSQL Replica
                           WAL streaming
                           replication
```

## Primary Responsibilities

```text
INSERT
UPDATE
DELETE
Flyway migrations
Post creation
Likes
Comments
Follows
Outbox persistence
Timeline projection writes
Search projection writes
```

## Replica Responsibilities

```text
Home Feed reads
Timeline reads
Batch post reads
Read-only follower queries
Read-heavy engagement aggregation
Other explicitly approved stale-tolerant reads
```

---

# Important Concepts

## Primary

The primary accepts both reads and writes.

```sql
SELECT pg_is_in_recovery();
```

Expected result:

```text
f
```

`f` means the database is not replaying WAL as a standby and is operating as the primary.

---

## Replica

The replica continuously receives and replays Write-Ahead Log records from the primary.

```sql
SELECT pg_is_in_recovery();
```

Expected result:

```text
t
```

`t` means PostgreSQL is in recovery mode and operating as a standby replica.

---

## Replica Lag

Replication is not guaranteed to be instant.

```text
Write committed on primary
        │
        ▼
WAL transmitted
        │
        ▼
Replica replays WAL
```

During this interval:

```text
Primary state ≠ Replica state
```

Example:

```text
User creates a post.
The post exists on the primary.
The immediate Home Feed read goes to the replica.
The replica has not replayed the post yet.
The post appears missing briefly.
```

This is eventual consistency.

---

# Lab Directory Structure

Create the following directory structure:

```text
postgres-replication-lab/
├── docker-compose.yml
├── primary/
│   └── init-primary.sh
└── replica/
    └── init-replica.sh
```

If this is part of the existing repository, a suitable location is:

```text
infra/postgres-replication/
```

---

# Docker Compose

Create:

```text
infra/postgres-replication/docker-compose.yml
```

```yaml
services:
  postgres-primary:
    image: postgres:16
    container_name: photoshare-postgres-primary
    environment:
      POSTGRES_DB: photoshare
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: admin
    ports:
      - "5432:5432"
    volumes:
      - primary-data:/var/lib/postgresql/data
      - ./primary/init-primary.sh:/docker-entrypoint-initdb.d/init-primary.sh:ro
    command:
      - postgres
      - -c
      - wal_level=replica
      - -c
      - max_wal_senders=10
      - -c
      - max_replication_slots=10
      - -c
      - hot_standby=on
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U admin -d photoshare"]
      interval: 5s
      timeout: 5s
      retries: 20

  postgres-replica:
    image: postgres:16
    container_name: photoshare-postgres-replica
    environment:
      POSTGRES_DB: photoshare
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: admin
      PGPASSWORD: replicator
    ports:
      - "5433:5432"
    volumes:
      - replica-data:/var/lib/postgresql/data
      - ./replica/init-replica.sh:/init-replica.sh:ro
    depends_on:
      postgres-primary:
        condition: service_healthy
    entrypoint: ["/bin/bash", "/init-replica.sh"]
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U admin -d photoshare"]
      interval: 5s
      timeout: 5s
      retries: 20

volumes:
  primary-data:
  replica-data:
```

## Port Mapping

```text
Primary  → localhost:5432
Replica  → localhost:5433
```

---

# Primary Initialization Script

Create:

```text
infra/postgres-replication/primary/init-primary.sh
```

```bash
#!/bin/bash

set -e

psql \
  -v ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" <<'SQL'

CREATE ROLE replicator
WITH REPLICATION
LOGIN
PASSWORD 'replicator';

SQL

cat >> "$PGDATA/pg_hba.conf" <<'HBA'

# Permit the replication user to connect from the Docker network.
host replication replicator all scram-sha-256
HBA
```

Make it executable:

```bash
chmod +x infra/postgres-replication/primary/init-primary.sh
```

## What This Script Does

```text
Creates a dedicated replication role
Grants the REPLICATION attribute
Allows replication connections through pg_hba.conf
```

The script runs only when the primary data directory is initialized for the first time.

---

# Replica Initialization Script

Create:

```text
infra/postgres-replication/replica/init-replica.sh
```

```bash
#!/bin/bash

set -e

PRIMARY_HOST="postgres-primary"
PRIMARY_PORT="5432"
REPLICATION_USER="replicator"
PGDATA="${PGDATA:-/var/lib/postgresql/data}"

until pg_isready \
  -h "$PRIMARY_HOST" \
  -p "$PRIMARY_PORT" \
  -U admin \
  -d photoshare
do
  echo "Waiting for PostgreSQL primary..."
  sleep 2
done

if [ ! -s "$PGDATA/PG_VERSION" ]; then
  echo "Initializing replica from primary..."

  rm -rf "${PGDATA:?}"/*

  export PGPASSWORD="replicator"

  pg_basebackup \
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
fi

exec docker-entrypoint.sh postgres
```

Make it executable:

```bash
chmod +x infra/postgres-replication/replica/init-replica.sh
```

## What This Script Does

```text
Waits for the primary to become available
Uses pg_basebackup to copy the primary data directory
Uses -R to create standby connection configuration
Starts PostgreSQL as a standby
Does not overwrite an already initialized replica volume
```

---

# Important Existing-Container Warning

The current project may already have a PostgreSQL container bound to:

```text
localhost:5432
```

For example:

```text
photoshare-postgres
```

Two containers cannot bind the same host port.

Before starting this lab, inspect running containers:

```bash
docker ps
```

If the old primary is still using port `5432`, choose one of these approaches.

## Option A: Stop the Existing Container

```bash
docker stop photoshare-postgres
```

This is appropriate only after confirming that existing data is backed up or preserved in a Docker volume.

## Option B: Use Different Lab Ports

Change Compose mappings to:

```yaml
postgres-primary:
  ports:
    - "55432:5432"

postgres-replica:
  ports:
    - "55433:5432"
```

Then configure the application accordingly.

## Recommended Safety Step

Back up the current local database before replacing or recreating the primary:

```bash
docker exec photoshare-postgres \
  pg_dump \
  -U admin \
  -d photoshare \
  > photoshare-before-replication.sql
```

---

# Starting the Environment

From the replication directory:

```bash
cd infra/postgres-replication

docker compose up -d
```

Verify containers:

```bash
docker ps
```

Expected names:

```text
photoshare-postgres-primary
photoshare-postgres-replica
```

Check Compose status:

```bash
docker compose ps
```

---

# Inspecting Logs

## Primary Logs

```bash
docker logs -f photoshare-postgres-primary
```

## Replica Logs

```bash
docker logs -f photoshare-postgres-replica
```

The replica logs should show successful startup after the base backup has completed.

---

# Connecting to the Primary

## Through Docker Exec

```bash
docker exec -it \
  photoshare-postgres-primary \
  psql \
  -U admin \
  -d photoshare
```

## From the Host

```bash
PGPASSWORD=admin \
psql \
  -h localhost \
  -p 5432 \
  -U admin \
  -d photoshare
```

Verify role:

```sql
SELECT pg_is_in_recovery();
```

Expected:

```text
f
```

---

# Connecting to the Replica

## Through Docker Exec

```bash
docker exec -it \
  photoshare-postgres-replica \
  psql \
  -U admin \
  -d photoshare
```

## From the Host

```bash
PGPASSWORD=admin \
psql \
  -h localhost \
  -p 5433 \
  -U admin \
  -d photoshare
```

Verify role:

```sql
SELECT pg_is_in_recovery();
```

Expected:

```text
t
```

If the result is `f`, the connection is not reaching a standby replica.

---

# Validate Streaming Replication

## Create Data on the Primary

Connect to the primary and run:

```sql
CREATE TABLE replication_test (
    id INTEGER PRIMARY KEY,
    message VARCHAR(200) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

INSERT INTO replication_test (
    id,
    message
)
VALUES (
    1,
    'replicated from primary'
);
```

## Read Data from the Replica

Connect to the replica and run:

```sql
SELECT *
FROM replication_test;
```

Expected result:

```text
1 | replicated from primary | ...
```

## Verify Replica Is Read-Only

On the replica:

```sql
INSERT INTO replication_test (
    id,
    message
)
VALUES (
    2,
    'must fail'
);
```

Expected behavior:

```text
The write is rejected because the standby is read-only.
```

---

# Inspect Replication from the Primary

On the primary:

```sql
SELECT
    application_name,
    client_addr,
    state,
    sync_state,
    sent_lsn,
    write_lsn,
    flush_lsn,
    replay_lsn
FROM pg_stat_replication;
```

A connected replica should appear in this view.

The exact address and application name depend on the container network and standby configuration.

---

# Inspect Replay State from the Replica

On the replica:

```sql
SELECT
    pg_is_in_recovery(),
    pg_last_wal_receive_lsn(),
    pg_last_wal_replay_lsn(),
    pg_last_xact_replay_timestamp();
```

A basic estimate of replay delay can be observed with:

```sql
SELECT
    now() - pg_last_xact_replay_timestamp()
        AS estimated_replay_delay;
```

This value must be interpreted carefully. If the primary has had no recent transactions, the timestamp can appear old even when the replica is fully caught up.

---

# Application DataSource Design

## Primary DataSource

The existing default datasource remains the primary and continues to serve Hibernate, Panache, Flyway, and all writes.

```properties
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=${DB_URL:jdbc:postgresql://localhost:5432/photoshare}
quarkus.datasource.username=${DB_USERNAME:admin}
quarkus.datasource.password=${DB_PASSWORD:admin}
quarkus.datasource.jdbc.max-size=16
```

## Replica DataSource

Add a named datasource:

```properties
quarkus.datasource."feed-replica".db-kind=postgresql
quarkus.datasource."feed-replica".jdbc.url=${FEED_REPLICA_DB_URL:jdbc:postgresql://localhost:5433/photoshare}
quarkus.datasource."feed-replica".username=${FEED_REPLICA_DB_USERNAME:feed_reader}
quarkus.datasource."feed-replica".password=${FEED_REPLICA_DB_PASSWORD:feed_reader_password}
quarkus.datasource."feed-replica".jdbc.min-size=2
quarkus.datasource."feed-replica".jdbc.max-size=12
quarkus.datasource."feed-replica".jdbc.acquisition-timeout=3S
```

The named datasource should be used only by explicitly read-only repositories.

---

# Create a Dedicated Read-Only Role

Run on the primary:

```sql
CREATE ROLE feed_reader
LOGIN
PASSWORD 'feed_reader_password';

GRANT CONNECT
ON DATABASE photoshare
TO feed_reader;

GRANT USAGE
ON SCHEMA public
TO feed_reader;

GRANT SELECT
ON ALL TABLES
IN SCHEMA public
TO feed_reader;

ALTER DEFAULT PRIVILEGES
IN SCHEMA public
GRANT SELECT
ON TABLES
TO feed_reader;

ALTER ROLE feed_reader
SET default_transaction_read_only = on;
```

The role and grants replicate to the standby through physical replication.

## Why Use a Read-Only Role?

```text
Prevents accidental writes through the read datasource
Makes routing errors fail visibly
Applies least-privilege access
Creates a clear operational boundary
```

---

# Home Feed Read Model

Create a read-specific DTO rather than reusing writable Hibernate entities.

```java
package com.ariv.photoshare.feed.dto;

import java.time.Instant;
import java.util.UUID;

public record HomeFeedRow(
        UUID postId,
        UUID authorId,
        String caption,
        String imageUrl,
        Instant createdAt,
        long likeCount,
        long commentCount
) {
}
```

This separates:

```text
Write Model → Hibernate and Panache entities
Read Model  → HomeFeedRow
```

---

# Replica-Backed Home Feed Repository

```java
package com.ariv.photoshare.feed.repository;

import com.ariv.photoshare.feed.dto.HomeFeedRow;
import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class HomeFeedReadRepository {

    @Inject
    @DataSource("feed-replica")
    AgroalDataSource replicaDataSource;

    public List<HomeFeedRow> findFeed(
            UUID userId,
            int limit) {

        String sql = """
            SELECT
                p.id AS post_id,
                p.user_id AS author_id,
                p.caption,
                p.image_url,
                p.created_at,
                COALESCE(l.like_count, 0) AS like_count,
                COALESCE(c.comment_count, 0) AS comment_count
            FROM timeline t
            JOIN posts p
              ON p.id = t.post_id
            LEFT JOIN (
                SELECT post_id, COUNT(*) AS like_count
                FROM likes
                GROUP BY post_id
            ) l
              ON l.post_id = p.id
            LEFT JOIN (
                SELECT post_id, COUNT(*) AS comment_count
                FROM comments
                GROUP BY post_id
            ) c
              ON c.post_id = p.id
            WHERE t.user_id = ?
            ORDER BY t.created_at DESC
            LIMIT ?
            """;

        List<HomeFeedRow> feed = new ArrayList<>();

        try (Connection connection =
                     replicaDataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            connection.setReadOnly(true);
            statement.setObject(1, userId);
            statement.setInt(2, limit);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    feed.add(map(resultSet));
                }
            }

            return feed;

        } catch (SQLException exception) {
            throw new FeedReadException(
                    "Unable to read home feed from replica",
                    exception
            );
        }
    }

    private HomeFeedRow map(
            ResultSet resultSet)
            throws SQLException {

        return new HomeFeedRow(
                resultSet.getObject(
                        "post_id",
                        UUID.class
                ),
                resultSet.getObject(
                        "author_id",
                        UUID.class
                ),
                resultSet.getString(
                        "caption"
                ),
                resultSet.getString(
                        "image_url"
                ),
                resultSet.getTimestamp(
                        "created_at"
                ).toInstant(),
                resultSet.getLong(
                        "like_count"
                ),
                resultSet.getLong(
                        "comment_count"
                )
        );
    }
}
```

Exception:

```java
package com.ariv.photoshare.feed.repository;

public class FeedReadException
        extends RuntimeException {

    public FeedReadException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}
```

---

# Home Feed Service Routing

```java
package com.ariv.photoshare.feed.service;

import com.ariv.photoshare.feed.dto.HomeFeedRow;
import com.ariv.photoshare.feed.repository.HomeFeedReadRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class HomeFeedService {

    private static final int DEFAULT_LIMIT = 50;

    @Inject
    HomeFeedReadRepository readRepository;

    public List<HomeFeedRow> getHomeFeed(
            UUID userId) {

        return readRepository.findFeed(
                userId,
                DEFAULT_LIMIT
        );
    }
}
```

Only the Home Feed read path should use the replica initially.

All mutations continue using the primary.

---

# Verify Application Routing

A diagnostic query can prove that the Home Feed datasource reaches a standby:

```sql
SELECT pg_is_in_recovery();
```

Temporary diagnostic method:

```java
public boolean isConnectedToReplica() {

    String sql = "SELECT pg_is_in_recovery()";

    try (Connection connection =
                 replicaDataSource.getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(sql);
         ResultSet resultSet =
                 statement.executeQuery()) {

        resultSet.next();
        return resultSet.getBoolean(1);

    } catch (SQLException exception) {
        throw new FeedReadException(
                "Unable to verify replica connection",
                exception
        );
    }
}
```

Expected:

```text
connectedToReplica=true
```

Do not execute this diagnostic query on every production request.

---

# Read-Your-Writes Consistency

## Problem

A user creates a post on the primary and immediately requests the Home Feed from the replica.

```text
Create Post
      │
      ▼
Primary Commit
      │
      ▼
Immediate Feed Read
      │
      ▼
Replica Has Not Caught Up
```

The new post may appear missing.

## Simple Lab Strategy

For a short window after a write, route that user's feed reads to the primary.

```java
@ApplicationScoped
public class RecentWriteTracker {

    private final ConcurrentHashMap<UUID, Instant>
            recentWriters =
            new ConcurrentHashMap<>();

    public void recordWrite(UUID userId) {
        recentWriters.put(
                userId,
                Instant.now()
        );
    }

    public boolean shouldReadPrimary(
            UUID userId) {

        Instant writtenAt =
                recentWriters.get(userId);

        if (writtenAt == null) {
            return false;
        }

        return writtenAt.isAfter(
                Instant.now()
                        .minusSeconds(5)
        );
    }
}
```

Routing:

```java
public List<HomeFeedRow> getHomeFeed(
        UUID userId) {

    if (recentWriteTracker
            .shouldReadPrimary(userId)) {

        return primaryReadRepository
                .findFeed(userId, 50);
    }

    return replicaReadRepository
            .findFeed(userId, 50);
}
```

## Limitation

In-memory write tracking works only for a single application instance.

A production-oriented design could use:

```text
Sticky routing
A short-lived Redis consistency marker
A replication position token
Explicit consistency preference from the client
Primary reads immediately after mutation
```

---

# Controlled Primary Fallback

If the replica fails, automatically sending every read to the primary can overload the primary.

```text
Replica Failure
      │
      ▼
All Read Traffic Moves to Primary
      │
      ▼
Primary Saturation
      │
      ▼
Writes Fail
```

This turns a partial outage into a total outage.

Use controlled fallback with:

```text
Short replica timeout
Circuit breaker
Rate-limited fallback
Capacity-aware load shedding
Metrics and alerts
Explicit configuration
```

Example property:

```properties
feed.replica.primary-fallback-enabled=true
```

Fallback should be narrowly scoped and observable.

---

# Failure Experiments

## Experiment 1: Stop the Replica

```bash
docker stop photoshare-postgres-replica
```

Expected behavior:

```text
Home Feed replica reads fail or use controlled fallback.
Post creation continues against the primary.
Likes and comments continue against the primary.
```

Restart:

```bash
docker start photoshare-postgres-replica
```

Observe recovery carefully. Depending on volume state, the standby should reconnect and replay missing WAL.

---

## Experiment 2: Verify Writes Do Not Reach Replica

Use the `feed_reader` account and attempt an update.

Expected:

```text
Write rejected.
```

---

## Experiment 3: Observe Replica Lag

1. Create a post on the primary.
2. Immediately query the corresponding row on the replica.
3. Repeat until the row becomes visible.
4. Record the observed delay.

The local Docker environment may have very low lag, so an observable delay is not guaranteed under normal conditions.

---

## Experiment 4: Pause WAL Replay

For a controlled learning experiment, pause replay on the replica:

```sql
SELECT pg_wal_replay_pause();
```

Create a post on the primary, then query the replica.

The new row should remain absent while replay is paused.

Resume:

```sql
SELECT pg_wal_replay_resume();
```

Then verify the row appears.

Use this only in the local learning environment.

---

# Troubleshooting

## Replica Container Does Not Appear

```bash
docker compose ps -a
```

Inspect logs:

```bash
docker logs photoshare-postgres-replica
```

Common causes:

```text
Primary not ready
Replication role missing
pg_hba.conf does not permit replication
Wrong replication password
Replica volume contains an invalid partial initialization
Script is not executable
Port 5433 already in use
```

---

## Replica Returns `pg_is_in_recovery() = f`

Possible causes:

```text
Connected to the primary by mistake
Host port mapping is incorrect
Replica started as an independent primary
pg_basebackup did not create standby configuration
```

Check:

```bash
docker port photoshare-postgres-replica
```

Expected host mapping:

```text
5432/tcp → 0.0.0.0:5433
```

---

## Replication User Does Not Exist

The primary initialization script runs only on a new, empty primary volume.

If the primary volume already existed, create the role manually:

```sql
CREATE ROLE replicator
WITH REPLICATION
LOGIN
PASSWORD 'replicator';
```

Then update `pg_hba.conf` and reload configuration or recreate the learning environment.

---

## Replica Initialization Failed and Volume Is Dirty

For the lab only, remove the replica volume and retry:

```bash
docker compose down

docker volume ls
```

Remove only the replication lab's replica volume:

```bash
docker volume rm <replica-volume-name>
```

Then:

```bash
docker compose up -d
```

Do not remove the primary volume unless the data is disposable or backed up.

---

## Primary Port Conflict

Check:

```bash
lsof -i :5432
```

or:

```bash
docker ps --format "table {{.Names}}\t{{.Ports}}"
```

Stop the conflicting container or use alternate lab ports.

---

## Permission Error on Scripts

```bash
chmod +x primary/init-primary.sh
chmod +x replica/init-replica.sh
```

Also ensure the scripts use Unix line endings.

---

# Validation Checklist

## Infrastructure

```text
✅ Primary container is running
✅ Replica container is running
✅ Primary listens on host port 5432
✅ Replica listens on host port 5433
```

## Role Verification

```text
✅ Primary pg_is_in_recovery() returns false
✅ Replica pg_is_in_recovery() returns true
```

## Replication

```text
✅ Table created on primary appears on replica
✅ Row inserted on primary appears on replica
✅ Write attempted on replica is rejected
✅ Primary shows a connected standby in pg_stat_replication
```

## Application Routing

```text
✅ Mutations use the primary datasource
✅ Home Feed uses the feed-replica datasource
✅ Flyway runs only through the primary datasource
✅ Replica uses read-only credentials
```

## Consistency

```text
✅ Replica lag is understood
✅ Immediate read-after-write behavior is tested
✅ Read-your-writes routing is designed
✅ Consistency expectations are documented
```

## Failure Behavior

```text
✅ Replica failure does not prevent writes
✅ Fallback behavior is controlled
✅ Primary is protected from an uncontrolled read flood
✅ Replica recovery is observed
```

---

# System Design Trade-Offs

## Benefits

```text
Read workload can scale independently
Primary has more capacity for writes
Read and write connection pools are isolated
Read-heavy failures can be contained
Additional replicas can be added for more read capacity
```

## Costs

```text
Eventual consistency
Replica lag
More database instances to operate
Complex routing logic
More connection pools
Potential stale reads
Failover and fallback complexity
Monitoring requirements
```

---

# Reads Suitable for Replicas

Good initial candidates:

```text
Home Feed
Public profile reads
Historical notification reads
Follower-list reads where slight staleness is acceptable
Analytics-style reads
```

Avoid replica reads when strong consistency is required:

```text
Immediately verifying a completed mutation
Authorization decisions based on freshly changed data
Uniqueness decisions
Payment or inventory decisions
Workflow transitions
```

---

# Why Search Is Different

The Search module already owns a dedicated CQRS projection:

```text
search_documents
```

That projection is itself a specialized read model.

Search does not necessarily need to query a primary database replica for its normal search path.

This demonstrates an important distinction:

```text
Read Replica
    → Copy of the transactional database

CQRS Projection
    → Purpose-built query model
```

Both scale reads, but they solve different problems.

---

# Production Considerations Not Implemented in This Lab

This lab intentionally avoids production-grade orchestration.

A production PostgreSQL architecture would also consider:

```text
Automatic failover
Leader election
Replication slots
WAL retention
Backup and point-in-time recovery
TLS
Secret management
Monitoring and alerting
Capacity planning
Connection proxying
Replica promotion
Split-brain prevention
High-availability tooling
Managed database services
```

The lab replica is for learning read scaling and consistency behavior, not for serving production traffic.

---

# Recommended Commit Boundaries

## Commit 1: PostgreSQL Replication Lab

```text
Docker Compose primary and replica
Primary initialization script
Replica bootstrap script
Validation documentation
```

```bash
git add infra/postgres-replication
git commit -m "feat(db): add PostgreSQL primary replica lab"
```

## Commit 2: Home Feed Replica Routing

```text
Named replica datasource
Read-only feed repository
Home Feed routing
Replica diagnostics
```

```bash
git add .
git commit -m "feat(feed): route home feed reads to PostgreSQL replica"
```

## Commit 3: Consistency and Failure Handling

```text
Read-your-writes routing
Replica lag experiment
Controlled primary fallback
Metrics and health checks
```

```bash
git add .
git commit -m "feat(feed): handle replica lag and controlled fallback"
```

---

# Phase-16A Completion Criteria

```text
Phase-16A: PostgreSQL Read Replicas and Read/Write Splitting

✅ Real primary and standby containers running
✅ WAL streaming replication validated
✅ Primary and replica roles verified
✅ Writes rejected on replica
✅ Home Feed routed to named replica datasource
✅ Writes preserved on primary
✅ Replica lag observed or simulated
✅ Read-your-writes strategy understood
✅ Controlled fallback behavior designed
✅ Failure experiments completed
```

---

# Key Takeaways

```text
Redundancy must cross failure and workload boundaries to be valuable.

A read replica reduces primary read pressure but introduces stale reads.

Writes must remain on the primary in a single-primary architecture.

Read-your-writes consistency is not automatic when reads use replicas.

A replica failure should not automatically overload the primary.

Read-only credentials provide a practical safety boundary.

Primary and replica connection pools should be explicitly separated.

CQRS projections and read replicas are related but different architectural tools.

A real replica is necessary to learn lag, recovery, and consistency behavior.
```

---

# Next Phase

After Phase-16A, continue with:

```text
Phase-16B: Multi-Region Active-Passive Architecture
```

Topics:

```text
Availability Zones versus Regions
Recovery Point Objective
Recovery Time Objective
Cross-region replication
Regional failover
Database promotion
Kafka and object-storage replication
DNS and global routing
Disaster-recovery drills
```
