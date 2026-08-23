# Phase-14: Search Architecture

## Overview

Until this phase, content in the Photo Sharing System could primarily be discovered through:

```text
Feed
Timeline
Direct Post Links
```

Users did not have an efficient way to discover posts based on keywords or content.

The goal of Phase-14 was to introduce a dedicated search architecture and understand how modern systems evolve from simple transactional database queries into specialized search read models.

This phase focused on system design concepts rather than merely adding search functionality.

The major concepts explored were:

```text
CQRS
Search Projections
Event-Driven Indexing
Eventual Consistency
Full-Text Search
Tokenization
Stemming
Inverted Indexes
GIN Indexes
Search Relevance
```

---

## Starting Point

The initial search implementation queried the transactional `posts` table directly.

```sql
SELECT *
FROM posts
WHERE caption ILIKE '%kafka%'
ORDER BY created_at DESC;
```

This approach worked correctly with the initial validation dataset of approximately 1,000 posts.

However, the design had important limitations.

```text
Search traffic competes with transactional traffic
Substring matching requires expensive scans
The transactional schema is not optimized for discovery
Search concerns are tightly coupled to the posts table
Independent search scaling is difficult
Search relevance is limited
```

---

# Step-1: PostgreSQL-Backed Search API

## Goal

Establish the search API contract before introducing a dedicated search model.

The first endpoint was:

```http
GET /api/v1/search?q=kafka
```

The API returned matching posts using caption text.

Example response:

```json
[
  {
    "postId": "566e8262-c9b3-46ac-ab59-704a94bf298a",
    "authorId": "6c34b9b7-fb47-4c29-badb-c8fb48750962",
    "caption": "Kafka Streams Deep Dive",
    "imageUrl": null,
    "createdAt": "2026-08-23T15:58:42.352037Z"
  }
]
```

## Initial Architecture

```text
Client
  │
  ▼
Search API
  │
  ▼
Search Service
  │
  ▼
Posts Table
```

## Learning

The first implementation deliberately used PostgreSQL substring matching.

```text
Start with the simplest working implementation.
Validate the API contract.
Experience the architectural limitation.
Introduce a specialized model only when justified.
```

---

# Step-2: CQRS Search Projection

## Problem

The `posts` table is the source of truth for post creation and modification.

It is optimized for transactional operations, not for search and discovery.

Using the same table for both commands and search queries couples two different workloads:

```text
Transactional Writes
Search Reads
```

## Solution

Introduce a dedicated search projection named:

```text
search_documents
```

The architecture evolved into:

```text
Write Model
    posts

Read Model
    search_documents
```

This introduced the CQRS principle:

```text
Command Model
    ≠
Query Model
```

---

## Search Projection Migration

Migration:

```text
V10__create_search_documents.sql
```

```sql
CREATE TABLE search_documents (
    post_id UUID PRIMARY KEY,
    author_id UUID NOT NULL,
    caption TEXT NOT NULL,
    search_text TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_search_documents_created_at
    ON search_documents(created_at DESC);
```

## Projection Responsibilities

The `search_documents` table stores a representation of a post optimized for search.

```text
post_id       → source post identifier
author_id     → post author
caption       → displayable content
search_text   → normalized searchable content
created_at    → ordering and filtering
```

The projection intentionally duplicates selected information from the source model.

This duplication is acceptable because the projection has a different responsibility.

```text
posts            → transactional source of truth
search_documents → search-oriented read model
```

---

# Step-3: Event-Driven Search Indexing

## Goal

Populate the search projection asynchronously instead of coupling post creation directly to search indexing.

The system already produced:

```text
PostCreatedEvent
```

through the reliable event pipeline:

```text
Post Transaction
      │
      ▼
Transactional Outbox
      │
      ▼
Outbox Publisher
      │
      ▼
Kafka / Redpanda
```

A new consumer was introduced:

```text
SearchIndexerConsumer
```

## Event-Driven Architecture

```text
Create Post
      │
      ▼
Posts Table
      │
      ▼
Outbox Event
      │
      ▼
Kafka / Redpanda
      │
      ├───────────────────┐
      ▼                   ▼
Timeline Consumer    Search Indexer Consumer
      │                   │
      ▼                   ▼
Timeline Projection  Search Projection
```

## Search Indexing Flow

```text
PostCreatedEvent
      │
      ▼
Load source post
      │
      ▼
Build SearchDocumentEntity
      │
      ▼
Persist search_documents row
```

## Multiple Projections

The same domain event now updates multiple read models:

```text
PostCreatedEvent
      │
      ├── Timeline Projection
      └── Search Projection
```

This is an important CQRS architecture pattern.

A single event stream can produce multiple independently optimized projections.

Future projections could include:

```text
Analytics Projection
Recommendation Projection
Trending Projection
Reporting Projection
```

---

# Reactive Messaging Fan-Out

Initially, both consumers listened to the same incoming channel:

```text
post-created-in
```

SmallRye Reactive Messaging reported multiple downstream consumers for a single channel.

The channel was configured for broadcast:

```properties
mp.messaging.incoming.post-created-in.broadcast=true
```

This enabled the same event to be delivered to:

```text
TimelineEventConsumer
SearchIndexerConsumer
```

## Learning

```text
One domain event may update multiple projections.
Messaging topology must explicitly support fan-out.
Consumer fan-out is different from consumer-group load balancing.
```

---

# Eventual Consistency

Because search indexing is asynchronous, post creation and search visibility are not part of the same transaction.

Possible sequence:

```text
Post Created
      │
      ▼
Post Available Through Direct Read
      │
      ▼
Kafka Event Processing Pending
      │
      ▼
Search Projection Updated
```

During the short interval before projection update:

```text
The post exists in the source model.
The post may not yet appear in search.
```

This is eventual consistency.

## Trade-Off

The architecture accepts temporary search staleness in exchange for:

```text
Loose coupling
Independent scaling
Simpler transactional boundaries
Projection rebuild capability
Failure isolation
```

---

# Step-4: PostgreSQL Full-Text Search

## Problem with Substring Search

The first search query used:

```sql
LIKE '%kafka%'
```

Although functionally correct, substring search does not provide real search-engine behavior such as:

```text
Tokenization
Language-aware normalization
Stemming
Inverted indexing
Relevance scoring
```

## Solution

PostgreSQL Full-Text Search was introduced using:

```text
tsvector
tsquery
GIN index
plainto_tsquery
ts_rank
```

---

# Search Vector Migration

Migration:

```text
V11__add_search_vector.sql
```

```sql
ALTER TABLE search_documents
    ADD COLUMN search_vector tsvector;

CREATE INDEX idx_search_vector
    ON search_documents
    USING GIN (search_vector);

UPDATE search_documents
SET search_vector =
        to_tsvector(
                'english',
                search_text
        );

CREATE FUNCTION update_search_vector()
RETURNS trigger AS
$$
BEGIN
    NEW.search_vector :=
        to_tsvector(
            'english',
            COALESCE(NEW.search_text, '')
        );

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_search_vector
    BEFORE INSERT OR UPDATE
    ON search_documents
    FOR EACH ROW
    EXECUTE FUNCTION update_search_vector();
```

---

# Tokenization and Stemming

Example input:

```text
Learning Kafka and Distributed Systems
```

The English text-search configuration creates normalized lexemes similar to:

```text
'distribut'
'kafka'
'learn'
'system'
```

This enables searches to match normalized word forms instead of relying only on exact substring matches.

Examples:

```text
learning    → learn
distributed → distribut
systems     → system
```

---

# Inverted Index and GIN

A traditional row-oriented view starts with a document and looks at its words.

```text
Document
   └── Words
```

An inverted index starts with a token and tracks matching documents.

```text
Token
   └── Matching Documents
```

PostgreSQL's GIN index supports efficient lookup of the tokens stored in the `tsvector` column.

## Why the GIN Index Matters

Without an appropriate index, full-text search may need to evaluate many rows.

With the GIN index, PostgreSQL can use the indexed token-to-document relationships to find candidate rows more efficiently.

---

# Trigger-Managed Search Vector

The `search_vector` is database generated by a trigger.

Application code writes:

```text
search_text
```

The database trigger computes:

```text
search_vector
```

Therefore, the Java entity must not attempt to write the `tsvector` column as a normal `String`.

## Incorrect Mapping

```java
@Column(name = "search_vector")
public String searchVector;
```

This caused PostgreSQL to reject the generated insert because:

```text
search_vector is tsvector
Java String is written as varchar
```

## Correct Entity Responsibility

The entity contains application-managed fields only:

```java
@Entity
@Table(name = "search_documents")
public class SearchDocumentEntity {

    @Id
    @Column(name = "post_id")
    public UUID postId;

    @Column(name = "author_id", nullable = false)
    public UUID authorId;

    @Column(name = "caption", nullable = false)
    public String caption;

    @Column(name = "search_text", nullable = false)
    public String searchText;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
}
```

The trigger-owned `search_vector` is omitted from writes.

## Learning

```text
Application-managed fields and database-generated fields need clear ownership.
ORM mappings must respect database-specific types.
Not every database column needs to be writable through the entity.
```

---

# Full-Text Search Query

The search query evolved from substring matching to full-text matching.

## Before

```sql
SELECT *
FROM search_documents
WHERE LOWER(search_text)
      LIKE LOWER('%kafka%')
ORDER BY created_at DESC;
```

## After

```sql
SELECT *
FROM search_documents
WHERE search_vector
      @@ plainto_tsquery('english', :query)
ORDER BY created_at DESC
LIMIT :limit;
```

`plainto_tsquery` converts ordinary user input into a text-search query.

---

# Search Relevance Foundation

PostgreSQL provides `ts_rank` for relevance scoring.

```sql
SELECT *,
       ts_rank(
           search_vector,
           plainto_tsquery('english', :query)
       ) AS rank
FROM search_documents
WHERE search_vector
      @@ plainto_tsquery('english', :query)
ORDER BY rank DESC,
         created_at DESC
LIMIT :limit;
```

This provides a foundation for ordering matching documents by text relevance before using recency as a tie-breaker.

The purpose of this phase was not to build a deep ranking system. The purpose was to understand how a search read model can provide specialized matching and relevance behavior.

---

# Validation

## API Validation

The search API was validated using:

```http
GET /api/v1/search?q=kafka
```

The API returned matching search documents with HTTP `200 OK`.

Validated result captions included:

```text
Kafka Streams Deep Dive
Learning Kafka and Distributed Systems
```

## Projection Validation

```sql
SELECT
    post_id,
    author_id,
    caption,
    search_text,
    created_at
FROM search_documents
ORDER BY created_at DESC;
```

## Search Vector Validation

```sql
SELECT
    caption,
    search_vector
FROM search_documents
ORDER BY created_at DESC;
```

## Full-Text Search Validation

```sql
SELECT caption
FROM search_documents
WHERE search_vector
      @@ plainto_tsquery('english', 'kafka');
```

## Index Validation

```sql
SELECT indexname
FROM pg_indexes
WHERE tablename = 'search_documents';
```

Expected indexes:

```text
idx_search_documents_created_at
idx_search_vector
```

## Trigger Validation

```sql
SELECT tgname
FROM pg_trigger
WHERE tgname = 'trg_search_vector';
```

Expected trigger:

```text
trg_search_vector
```

---

# Migration Discipline Learning

During implementation, Flyway detected a checksum mismatch for migration version 10.

The key lesson was:

```text
Applied migrations are immutable.
```

After a migration has been applied, new schema changes must be introduced through a new migration rather than by editing the applied file.

Correct sequence:

```text
V10__create_search_documents.sql
V11__add_search_vector.sql
```

Flyway migration history acts as an append-only record of schema evolution.

This is similar to the project's treatment of:

```text
Git history
Kafka events
Outbox events
```

---

# Current Search Architecture

```text
                    Write Path

Client
  │
  ▼
Post API
  │
  ▼
Post Transaction
  ├── posts
  └── outbox_events
          │
          ▼
    Outbox Publisher
          │
          ▼
    Kafka / Redpanda
          │
          ├─────────────────────────┐
          ▼                         ▼
 Timeline Consumer          Search Indexer Consumer
          │                         │
          ▼                         ▼
 Timeline Projection        Search Projection
                              search_documents
                                     │
                                     ▼
                              tsvector + GIN

                    Query Path

Client
  │
  ▼
Search API
  │
  ▼
Search Service
  │
  ▼
Search Projection
  │
  ▼
PostgreSQL Full-Text Search
  │
  ▼
Ranked Search Results
```

---

# System Design Patterns Learned

## 1. CQRS

```text
Write Model
    posts

Query Model
    search_documents
```

Commands and queries are served by models optimized for different responsibilities.

---

## 2. Event-Driven Projection

```text
PostCreatedEvent
      ↓
SearchIndexerConsumer
      ↓
search_documents
```

The read model is derived asynchronously from domain events.

---

## 3. Eventual Consistency

The source model may be updated before the search projection catches up.

```text
Source of truth is current.
Search view may be briefly stale.
```

---

## 4. Read Model Optimization

The search schema is designed for discovery, not transactional writes.

```text
One source model can serve multiple specialized projections.
```

---

## 5. Inverted Index

The GIN index enables efficient token-oriented retrieval.

```text
Token → Matching Documents
```

---

## 6. Database-Generated Data

The trigger owns generation of the `search_vector`.

```text
Application writes search_text.
Database derives search_vector.
```

---

## 7. Migration Immutability

```text
Never rewrite an applied migration.
Append a new migration for every schema evolution.
```

---

# Trade-Offs

## Benefits

```text
Search workload separated from the posts write model
Independent search schema evolution
Event-driven projection updates
Projection rebuild possibility
Full-text tokenization and stemming
GIN-backed inverted indexing
Foundation for relevance scoring
```

## Costs

```text
Duplicated data
Eventual consistency
Additional consumer and projection maintenance
Need for replay or backfill strategies
More failure modes than direct table search
Search projection can drift from the source model
```

---

# Failure Modes and Recovery Considerations

The search projection is derived data.

Potential failures include:

```text
Kafka event delivery delay
Search consumer failure
Projection insert failure
Duplicate event delivery
Projection schema change
Missed historical events
```

Design implications:

```text
Search indexing should be idempotent.
Projection rows should use stable source identifiers.
The projection should be rebuildable from source data or replayable events.
Operational monitoring should detect projection lag and failures.
```

The use of `post_id` as the search document primary key provides a foundation for idempotent indexing.

---

# Why Not OpenSearch Yet?

This phase intentionally used PostgreSQL Full-Text Search before adding a dedicated distributed search engine.

The purpose was to learn the underlying concepts first:

```text
Tokenization
Stemming
Text Queries
Inverted Indexing
Search Relevance
Read Models
Eventual Consistency
```

A dedicated search engine should be introduced when justified by architectural requirements such as:

```text
Independent search scaling
Large search corpus
Distributed indexing
Advanced analyzers
Fuzzy matching
Autocomplete
Complex ranking
Search-specific availability requirements
```

The next system-design decision should be driven by a demonstrated requirement, not by technology adoption alone.

---

# Phase-14 Completion Status

```text
Phase-14: Search Architecture

✅ PostgreSQL-backed Search API
✅ Search API contract validation
✅ CQRS Search Projection
✅ V10 search_documents migration
✅ Event-driven SearchIndexerConsumer
✅ Multiple projection fan-out
✅ Eventual consistency observation
✅ V11 search_vector migration
✅ Trigger-managed tsvector
✅ GIN inverted index
✅ PostgreSQL Full-Text Search
✅ Search relevance foundation
✅ End-to-end API validation
```

---

# Key Takeaways

```text
Transactional databases and search read models serve different workloads.

The source of truth does not need to be the direct query model.

CQRS enables independent optimization of commands and queries.

Event-driven projections introduce eventual consistency.

Full-text search relies on tokenization, normalization, and inverted indexes.

Database-generated columns require explicit ownership boundaries with the ORM.

Applied database migrations are immutable history.

A dedicated search engine should be introduced only when scale or capability requires it.
```

---

# Next System Design Phase

The recommended next phase is:

```text
Phase-15: Service Decomposition Strategy
```

The system is now complex enough to evaluate meaningful service boundaries across:

```text
Users
Posts
Feed and Timeline
Search
Notifications
Media
Outbox and Messaging
```

The next learning objective is not to add another product feature. It is to determine:

```text
Which modules should remain together?
Which modules have independent scaling needs?
Which module should be extracted first?
Who owns each database and read model?
What consistency and communication trade-offs appear after extraction?
```
