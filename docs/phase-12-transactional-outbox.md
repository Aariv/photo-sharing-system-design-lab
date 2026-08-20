# Phase-12: Transactional Outbox Pattern

## Overview

In Phase-11, we evolved feed generation from synchronous processing to an asynchronous event-driven architecture using Kafka.

Current architecture:

```text
Create Post
      │
      ▼
Persist Post
      │
      ▼
Publish PostCreatedEvent
      │
      ▼
Kafka
      │
      ▼
Timeline Consumer
```

This significantly improved application responsiveness and enabled scalable feed generation.

However, the architecture still contains a critical reliability problem known as the Dual Write Problem.

---

# Current Architecture

## Current Flow

```text
POST /posts
      │
      ▼
Save Post To Database
      │
      ▼
Publish Kafka Event
```

Example:

```java
postRepository.persist(post);

postCreatedEventPublisher.publish(
        new PostCreatedEvent(...)
);
```

Database writes and Kafka publishes are two separate operations.

---

# The Dual Write Problem

A dual write occurs when a single business operation writes to two different systems independently.

Current operation writes to:

```text
1. PostgreSQL
2. Kafka
```

These writes are not coordinated.

---

# Failure Scenario 1

## Post Saved, Event Lost

```text
Save Post
      ✅

Publish Event
      ❌
```

Result:

```text
Post Exists
Timeline Never Generated
Followers Never See Post
```

---

# Failure Scenario 2

## Event Published, Transaction Rolled Back

```text
Publish Event
      ✅

Database Commit
      ❌
```

Result:

```text
Timeline Event Exists
Post Does Not Exist
```

---

# Industry Solution

The standard solution is:

```text
Transactional Outbox Pattern
```

Instead of publishing directly to Kafka during the request:

```text
Database
     +
Kafka
```

we only write to the database.

---

# New Architecture

```text
POST /posts

      │

      ▼

Database Transaction

┌──────────────────────┐
│ posts                │
│ outbox_events        │
└──────────────────────┘

      │

      ▼

Transaction Commit

      │

      ▼

Outbox Publisher

      │

      ▼

Kafka

      │

      ▼

Timeline Consumer
```

---

# Key Idea

A single transaction persists:

```text
Post
+
Outbox Event
```

together.

Example:

```java
@Transactional
public void createPost(...) {

    postRepository.persist(post);

    outboxRepository.persist(
            new OutboxEvent(...)
    );
}
```

If the transaction succeeds:

```text
Post Exists
Outbox Event Exists
```

If the transaction fails:

```text
Nothing Exists
```

---

# Outbox Table

```sql
CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(100),
    aggregate_id UUID,
    event_type VARCHAR(100),
    payload TEXT,
    status VARCHAR(50),
    created_at TIMESTAMP,
    processed_at TIMESTAMP
);
```

---

# Example Row

```text
id               = 123
aggregate_type   = POST
aggregate_id     = post-id
event_type       = POST_CREATED
payload          = {...}
status           = PENDING
created_at       = now()
```

---

# Outbox Publisher

A background process periodically checks:

```sql
SELECT *
FROM outbox_events
WHERE status = 'PENDING'
LIMIT 100;
```

For each event:

```text
Publish To Kafka
```

If successful:

```sql
UPDATE outbox_events
SET status='PROCESSED';
```

---

# Benefits

## Reliability

```text
No Lost Events
```

## Consistency

```text
Post And Event Always Match
```

## Retry Support

```text
Kafka Failure
      ↓
Retry Later
```

## Recoverability

```text
Events remain in outbox table
until successfully processed.
```

---

# Duplicate Event Delivery

The Outbox Pattern solves:

```text
Dual Writes
```

but introduces:

```text
Duplicate Event Delivery
```

Consumers must be idempotent.

Current Timeline Service already has protection using:

```text
(user_id, post_id)
```

as a composite key.

---

# Phase-12 Implementation Plan

## Step-1

```text
Create outbox_events table
```

## Step-2

```text
Create OutboxEntity
```

## Step-3

```text
Create OutboxRepository
```

## Step-4

```text
Store PostCreatedEvent in outbox
```

## Step-5

```text
Create OutboxPublisher
```

## Step-6

```text
Publish pending events to Kafka
```

## Step-7

```text
Mark processed events
```

## Step-8

```text
Add retry support
```

---

# Key Learnings

## Lesson 1

```text
Asynchronous systems introduce consistency challenges.
```

## Lesson 2

```text
Database + Kafka = Dual Write Problem.
```

## Lesson 3

```text
Transactional Outbox is the industry-standard solution.
```

## Lesson 4

```text
Reliable systems prioritize correctness over simplicity.
```
