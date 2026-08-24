# Phase-15: Service Decomposition Strategy

## Overview

The `photo-sharing-system-design-lab` has evolved from a simple monolith into a sophisticated modular, event-driven platform.

The platform now includes:

```text
Users
Posts
Followers
Likes
Comments
Feed
Timeline
Search
Notifications
Media
Redis Caching
Kafka / Redpanda Messaging
Transactional Outbox
CQRS Read Models
Full-Text Search
Observability
Kubernetes Foundations
```

All these capabilities currently run inside a single deployable Quarkus application and primarily share one PostgreSQL database.

Phase-15 does not begin by creating microservices.

Instead, Phase-15 focuses on the architectural question:

```text
When should a modular monolith evolve into independently deployable services?
```

The objective is to learn how to identify service boundaries using scalability, ownership, failure isolation, data ownership, and organizational concerns.

This phase is intentionally documentation-first. The main deliverable is an evidence-based decomposition strategy, not a large code rewrite.

---

## Learning Objectives

By the end of this phase, I should understand:

```text
Why microservices should not be the default starting architecture
How bounded contexts differ from Java packages
How independent scaling influences service boundaries
How data ownership influences service boundaries
How failure isolation influences extraction decisions
How team ownership affects architecture
Why database-per-service matters
When to use synchronous or asynchronous communication
Why service extraction creates new distributed-systems problems
How to evolve incrementally instead of performing a big-bang rewrite
```

---

# Why Not Start With Microservices?

A common misconception is:

```text
Many Modules
      ↓
Many Microservices
```

This is incorrect.

A module is a logical code boundary. A microservice is an independently deployable, independently operable, and independently owned system boundary.

Splitting every package into a separate service can create:

```text
More network calls
More deployment pipelines
More operational overhead
More failure modes
More difficult debugging
Distributed transactions
Eventual consistency
Versioned API contracts
Cross-service observability requirements
```

Microservices should solve a demonstrated architectural problem such as:

```text
Independent scaling
Independent team ownership
Independent deployment cadence
Failure isolation
Technology specialization
Data ownership
Compliance isolation
```

The guiding principle for this project is:

```text
Do not extract a service because a module exists.
Extract a service because an independent operational boundary is justified.
```

---

# Current Architecture

## Current Deployment Model

```text
Photo Sharing Platform

├── User Module
├── Post Module
├── Follow Module
├── Like Module
├── Comment Module
├── Feed Module
├── Timeline Module
├── Search Module
├── Notification Module
├── Media Module
├── Cache Module
├── Outbox Module
└── Event System

Deployment Model

One Quarkus Application
One Primary Deployment Unit
One Primary PostgreSQL Database
Shared Runtime Resources
```

## Current Strengths

```text
Simple local development
Simple transactional boundaries
Strong consistency inside one database
Easy code navigation
Low operational overhead
Straightforward debugging
Simple deployment lifecycle
Low network-call overhead
Fast refactoring across modules
```

## Emerging Constraints

```text
All modules scale together
One deployment affects every capability
Shared database ownership is becoming ambiguous
Failure blast radius covers the entire application
Independent team ownership is difficult
Resource-heavy modules cannot be isolated
Different workload characteristics compete in one runtime
```

---

# Existing Architectural Boundaries

The application already contains several logical boundaries.

```text
Core Transactional Domain
    Users
    Posts
    Follows
    Likes
    Comments

Derived Read Models
    Timeline
    Search Documents

Asynchronous Capabilities
    Notifications
    Timeline Generation
    Search Indexing

Infrastructure and Reliability
    Outbox
    Kafka / Redpanda
    Redis
    Object Storage
```

The project has also already introduced patterns that prepare some modules for extraction:

```text
Event-driven communication
Transactional outbox
CQRS projections
Idempotent consumers
Dedicated APIs
Dedicated read models
Eventual consistency
```

---

# Service Decomposition Evaluation Framework

Every potential service boundary should be evaluated against the same criteria.

## 1. Independent Scaling

Question:

```text
Does this capability experience a workload that is materially different from the rest of the platform?
```

Examples:

```text
Search may be read-heavy and CPU-intensive.
Timeline may be read-heavy and fan-out intensive.
Post creation may have a lower write rate.
Notification delivery may be bursty and asynchronous.
Media transfer may be bandwidth-heavy.
```

A capability with unique scaling characteristics is a stronger extraction candidate.

---

## 2. Clear Data Ownership

Question:

```text
Can one service exclusively own the data required for this capability?
```

Strong ownership examples:

```text
Search Service owns the search index or search projection.
Timeline Service owns timeline entries.
Notification Service owns notification delivery state.
```

Weak ownership example:

```text
A Like Service that continuously reaches into the Post Service database.
```

A clean data boundary makes extraction safer.

---

## 3. Failure Isolation

Question:

```text
If this capability fails, should the rest of the platform continue operating?
```

Examples:

```text
Search failure should not prevent post creation.
Notification failure should not prevent likes.
Timeline lag should not prevent a post from being stored.
Media processing failure should not crash profile reads.
```

The stronger the desired failure isolation, the stronger the service candidate.

---

## 4. Independent Deployment Cadence

Question:

```text
Does this capability need to change and deploy independently?
```

Examples:

```text
Search relevance logic may evolve frequently.
Notification providers may change independently.
Timeline algorithms may require independent experiments.
```

Independent change frequency can justify a separate deployment boundary.

---

## 5. Team Ownership

Question:

```text
Could an independent team own the capability end to end?
```

End-to-end ownership includes:

```text
API
Data
Deployment
Operations
On-call support
SLOs
Capacity planning
```

Technical boundaries become more valuable when they align with organizational ownership.

---

## 6. Coupling and Dependency Direction

Question:

```text
Can the capability consume stable contracts without accessing internal implementation details of other modules?
```

A good candidate consumes:

```text
Domain events
Stable APIs
Owned data
```

A weak candidate depends on:

```text
Shared tables
Cross-module transactions
Internal entity classes
Frequent synchronous callbacks
```

---

## 7. Consistency Requirements

Question:

```text
Can this capability tolerate eventual consistency?
```

Examples:

```text
Search can usually tolerate short indexing delay.
Notifications can usually tolerate delivery delay.
Timeline projections can usually tolerate short lag.
Core post creation requires strong local consistency.
```

Capabilities tolerant of eventual consistency are generally easier to extract.

---

## 8. Operational Value Versus Cost

Question:

```text
Does extraction create enough benefit to justify its operational cost?
```

Costs include:

```text
New deployment pipeline
New database
New dashboards
New alerts
New runbooks
New access policies
New network dependencies
New contract-versioning responsibilities
```

A technically valid boundary is not automatically worth extracting.

---

# Service Boundary Scorecard

The following assessment uses three qualitative ratings:

```text
Strong
Medium
Weak
```

## Search Module

```text
Independent Scaling       Strong
Clear Data Ownership      Strong
Failure Isolation         Strong
Independent Deployment    Strong
Team Ownership            Strong
Eventual Consistency      Acceptable
Coupling                  Low to Medium
Operational Value         Strong
```

Verdict:

```text
Strongest first extraction candidate
```

---

## Timeline and Feed Module

```text
Independent Scaling       Strong
Clear Data Ownership      Strong
Failure Isolation         Strong
Independent Deployment    Strong
Team Ownership            Strong
Eventual Consistency      Acceptable
Coupling                  Medium
Operational Value         Strong
```

Verdict:

```text
Strong second extraction candidate
```

---

## Notification Module

```text
Independent Scaling       Strong
Clear Data Ownership      Strong
Failure Isolation         Strong
Independent Deployment    Strong
Team Ownership            Strong
Eventual Consistency      Acceptable
Coupling                  Low
Operational Value         Medium to Strong
```

Verdict:

```text
Strong third extraction candidate
```

---

## Media Module

```text
Independent Scaling       Strong
Clear Data Ownership      Medium to Strong
Failure Isolation         Strong
Independent Deployment    Medium
Team Ownership            Medium
Eventual Consistency      Usually Acceptable
Coupling                  Medium
Operational Value         Medium
```

Verdict:

```text
Good later extraction candidate, especially when media processing becomes expensive
```

---

## User Module

```text
Independent Scaling       Weak to Medium
Clear Data Ownership      Medium
Failure Isolation         Medium
Independent Deployment    Weak
Team Ownership            Medium
Eventual Consistency      Depends on operation
Coupling                  High
Operational Value         Weak initially
```

Verdict:

```text
Remain in the core service initially
```

---

## Post Module

```text
Independent Scaling       Medium
Clear Data Ownership      Strong
Failure Isolation         Weak
Independent Deployment    Medium
Team Ownership            Strong
Eventual Consistency      Limited for core writes
Coupling                  High
Operational Value         Medium
```

Verdict:

```text
Remain as the center of the core service during early extraction
```

---

## Follow Module

```text
Independent Scaling       Medium
Clear Data Ownership      Medium
Failure Isolation         Medium
Independent Deployment    Medium
Team Ownership            Medium
Eventual Consistency      Depends on use case
Coupling                  High with feed and ranking
Operational Value         Weak to Medium initially
```

Verdict:

```text
Keep with the core domain initially; reevaluate when the social graph requires specialized scaling
```

---

## Like Module

```text
Independent Scaling       Medium to Strong for viral traffic
Clear Data Ownership      Medium
Failure Isolation         Medium
Independent Deployment    Medium
Team Ownership            Medium
Eventual Consistency      Count may be eventual; user action should be durable
Coupling                  High with posts and engagement
Operational Value         Medium later
```

Verdict:

```text
Keep inside the core initially; revisit when concurrent engagement and counter scaling justify a dedicated boundary
```

---

## Comment Module

```text
Independent Scaling       Medium
Clear Data Ownership      Medium
Failure Isolation         Medium
Independent Deployment    Medium
Team Ownership            Medium
Eventual Consistency      Limited for comment creation
Coupling                  High with posts
Operational Value         Weak to Medium initially
```

Verdict:

```text
Remain in the core service initially
```

---

## Outbox Module

The outbox is not a business service.

It is a reliability mechanism owned by the service that performs the corresponding business transaction.

Verdict:

```text
Do not extract into a centralized outbox service.
Each service should own its own outbox table or reliable publication mechanism.
```

---

# Candidate 1: Search Service

## Current Search Architecture

```text
Post Transaction
      │
      ▼
PostCreatedEvent
      │
      ▼
Kafka / Redpanda
      │
      ▼
SearchIndexerConsumer
      │
      ▼
search_documents
      │
      ▼
Search API
```

## Why Search Is the Best First Candidate

Search already has:

```text
A dedicated API
A dedicated read model
An event-driven ingestion path
Eventual consistency
Independent scaling characteristics
Independent relevance logic
A natural future move to OpenSearch or Elasticsearch
```

Search does not need to participate in the post-creation transaction.

If Search Service is temporarily unavailable:

```text
Posts can still be created.
Likes and comments can continue.
Timeline generation can continue.
Search results may become stale until recovery.
```

This is a clean failure boundary.

## Proposed Ownership

```text
Search Service owns:

Search API
Search indexing consumer
Search document schema
Full-text search configuration
Search relevance logic
Search availability and latency SLOs
Projection rebuild process
```

## Proposed Inputs

```text
PostCreatedEvent
Future PostUpdatedEvent
Future PostDeletedEvent
Future UserUpdatedEvent
```

## Proposed Outputs

```text
Search result APIs
Search health metrics
Projection lag metrics
Indexing failure metrics
```

---

# Candidate 2: Timeline Service

## Current Timeline Architecture

```text
PostCreatedEvent
      │
      ▼
TimelineEventConsumer
      │
      ▼
Celebrity Decision
      │
      ▼
Timeline Projection
      │
      ▼
Hybrid Feed Read
```

## Why Timeline Is a Strong Candidate

Timeline already has:

```text
Its own projection
Its own event consumer
Its own read behavior
Independent read scaling
Independent caching needs
Feed-specific failure modes
```

Timeline workloads differ from core post writes:

```text
Post Service is write-oriented.
Timeline Service is read-heavy and fan-out-oriented.
```

## Proposed Ownership

```text
Timeline Service owns:

Timeline projection
Feed retrieval API
Fan-out-on-write processing
Celebrity and hybrid-feed logic
Feed cache
Feed latency SLOs
Timeline lag monitoring
```

## Key Dependency Challenge

The timeline currently requires follower information.

Possible approaches:

```text
1. Synchronous call to Core Service
2. Replicated follower projection in Timeline Service
3. Follow events consumed asynchronously
```

Recommended future direction:

```text
Timeline Service owns a local follower read model populated by FollowCreatedEvent and FollowRemovedEvent.
```

This avoids a synchronous dependency on the core database during fan-out.

---

# Candidate 3: Notification Service

## Current Notification Architecture

```text
Domain Event
      │
      ▼
Kafka / Redpanda
      │
      ▼
Notification Consumer
      │
      ▼
notifications
```

## Why Notification Is a Strong Candidate

Notification processing is already asynchronous.

It can fail independently without rolling back the originating action.

It may also evolve into provider-specific delivery channels:

```text
In-App
Push
Email
SMS
```

## Proposed Ownership

```text
Notification Service owns:

Notification preferences
Notification state
Template selection
Delivery attempts
Provider integrations
Retry policies
Notification APIs
Delivery SLOs
```

---

# Recommended Extraction Order

## Stage 0: Preserve the Modular Monolith

Before extraction:

```text
Strengthen package boundaries
Remove internal cross-module entity sharing
Define API and event contracts
Add architecture tests
Measure current workload
Document ownership
```

No service should be extracted until the boundary can be enforced inside the monolith.

---

## Stage 1: Extract Search Service

```text
Core Service
      │
      ▼
PostCreatedEvent
      │
      ▼
Search Service
```

Why first:

```text
Low transactional coupling
Independent read model
Natural eventual consistency
Easy rollback
Clear success criteria
Independent scaling value
```

---

## Stage 2: Extract Timeline Service

```text
Core Service
      │
      ▼
Post and Follow Events
      │
      ▼
Timeline Service
```

Why second:

```text
Dedicated projection exists
Read-heavy scaling is independent
Kafka integration already exists
Feed behavior can evolve independently
```

---

## Stage 3: Extract Notification Service

```text
Domain Events
      │
      ▼
Notification Service
```

Why third:

```text
Naturally asynchronous
Failure isolation is valuable
Provider integrations can evolve independently
```

---

## Stage 4: Reevaluate Media Service

Extract Media Service when one or more of these appear:

```text
Image transcoding
Thumbnail generation
Content moderation
Large upload volume
Independent storage lifecycle
Bandwidth isolation
Dedicated media team
```

---

## Stage 5: Reevaluate Core Domain Boundaries

Only after the first independent services are stable should the platform reevaluate:

```text
User Service
Social Graph Service
Engagement Service
Comment Service
Post Service
```

These boundaries are more coupled and involve stronger consistency requirements.

---

# Proposed Future Architecture

```text
                           Clients
                              │
                              ▼
                         API Gateway
                              │
          ┌───────────────────┼────────────────────┐
          │                   │                    │
          ▼                   ▼                    ▼
     Core Service        Search Service       Timeline Service
          │                   │                    │
          ▼                   ▼                    ▼
      Core DB            Search Store          Timeline DB
          │                   ▲                    ▲
          │                   │                    │
          └────────────── Kafka / Redpanda ────────┘
                              │
                              ▼
                     Notification Service
                              │
                              ▼
                     Notification Database
```

The diagram represents a target direction, not an instruction to immediately create every service.

---

# Database Ownership Strategy

## Current State

```text
One PostgreSQL database
Multiple modules
Shared schema
```

## Transitional State

During extraction, a service may temporarily read from existing data through a controlled interface, but shared-table writes must be avoided.

```text
Core Service owns core tables.
Search Service owns search data.
Timeline Service owns timeline data.
Notification Service owns notification data.
```

## Target Principle

```text
A service exclusively owns its database schema or datastore.
Other services access the data through APIs or events.
```

## Shared Database Anti-Pattern

Avoid:

```text
Search Service writes search_documents.
Core Service also writes search_documents.
Timeline Service reads posts tables directly.
Notification Service updates user tables.
```

These patterns create hidden coupling and prevent independent evolution.

---

# Data Duplication Across Services

Service decomposition often requires deliberate duplication.

Examples:

```text
Search Service stores post caption and author information.
Timeline Service stores post references and follower projections.
Notification Service stores recipient display information needed for delivery.
```

This duplication is acceptable when:

```text
A clear source of truth exists.
The duplicate is treated as a projection.
Updates arrive through versioned events.
The projection can be rebuilt.
Staleness expectations are documented.
```

---

# Communication Strategy

## Synchronous Communication

Use synchronous APIs when:

```text
The caller requires an immediate answer.
The operation cannot continue safely without the response.
The dependency is part of the user-facing request path.
```

Possible protocols:

```text
REST
gRPC
```

Risks:

```text
Latency accumulation
Timeouts
Cascading failures
Retry storms
Temporal coupling
```

---

## Asynchronous Communication

Use events when:

```text
The consumer can process later.
Eventual consistency is acceptable.
Multiple consumers need the same fact.
The producer should not know every downstream consumer.
```

Examples:

```text
PostCreatedEvent
PostUpdatedEvent
PostDeletedEvent
FollowCreatedEvent
FollowRemovedEvent
PostLikedEvent
CommentCreatedEvent
```

Benefits:

```text
Loose coupling
Independent scaling
Failure isolation
Replay capability
Fan-out to multiple projections
```

Costs:

```text
Eventual consistency
Duplicate delivery
Schema evolution
Ordering challenges
Consumer lag
Operational complexity
```

---

# Event Contract Ownership

Events become long-lived integration contracts after service extraction.

Each event should have:

```text
Stable event name
Event identifier
Aggregate identifier
Occurred-at timestamp
Schema version
Required payload
Clear producer ownership
Documented ordering expectations
```

Example envelope:

```json
{
  "eventId": "uuid",
  "eventType": "POST_CREATED",
  "eventVersion": 1,
  "aggregateId": "post-id",
  "occurredAt": "timestamp",
  "payload": {
    "postId": "uuid",
    "authorId": "uuid",
    "caption": "text",
    "createdAt": "timestamp"
  }
}
```

The exact payload should be designed according to consumer needs and compatibility requirements.

---

# Distributed Systems Problems Created by Extraction

## Network Failure

A local method call becomes a network dependency.

```text
Caller
  │
  ▼
Timeout
Retry
Circuit Breaker
Fallback
```

---

## Distributed Transactions

A single database transaction cannot span service-owned databases without introducing substantial coordination complexity.

The architecture should prefer:

```text
Local transaction
      +
Reliable event publication
      +
Eventual consistency
```

The existing Transactional Outbox pattern prepares the platform for this model.

---

## Eventual Consistency

After extraction:

```text
Post may exist before Search sees it.
Post may exist before Timeline includes it.
Like may exist before Notification is delivered.
```

Consistency expectations must be explicit in product and API behavior.

---

## Duplicate Delivery

At-least-once event delivery can result in duplicate processing.

Consumers must be idempotent.

Examples:

```text
Search document uses post_id as a stable key.
Timeline entry uses user_id + post_id as a unique key.
Notification may track processed event_id.
```

---

## Ordering

Consumers may observe events in an unexpected order.

Examples:

```text
POST_UPDATED arrives before POST_CREATED.
POST_DELETED arrives while an older update is retried.
FOLLOW_REMOVED races with timeline generation.
```

Possible controls:

```text
Partition by aggregate identifier
Include event version or entity version
Reject stale updates
Design tolerant projection logic
```

---

## Contract Evolution

Once multiple deployments consume a message or API, a breaking change cannot be coordinated through one code commit.

Strategies include:

```text
Backward-compatible changes
Optional fields
Versioned event schemas
Consumer-driven contract tests
Deprecation windows
```

---

## Observability

A request or event now crosses process boundaries.

Required capabilities include:

```text
Distributed tracing
Correlation IDs
Structured logs
Consumer lag metrics
Failure-rate metrics
SLOs per service
End-to-end dashboards
```

The project's OpenTelemetry and Jaeger foundation supports this evolution.

---

## Deployment and Operational Complexity

Each extracted service requires:

```text
Build pipeline
Deployment manifest
Configuration
Secrets
Health checks
Resource limits
Dashboards
Alerts
Runbooks
Ownership
On-call responsibility
```

Service extraction is incomplete unless the operational ownership model is also defined.

---

# API Gateway Considerations

An API Gateway may become valuable when multiple external-facing services exist.

Potential responsibilities:

```text
Request routing
Authentication enforcement
Rate limiting
TLS termination
Request correlation
API version routing
Edge observability
```

The gateway should not become a new business-logic monolith.

Business decisions should remain inside the owning service.

---

# Extraction Strategy: Strangler Pattern

The recommended migration approach is incremental.

## Step 1: Stabilize the Boundary Inside the Monolith

```text
Define interfaces
Remove direct internal field access
Own repository access inside the module
Create explicit DTOs
Document events
Add module-level tests
```

## Step 2: Introduce a Replaceable Adapter

For Search:

```text
SearchResource
      │
      ▼
SearchClient Interface
      │
      ├── LocalSearchAdapter
      └── RemoteSearchAdapter
```

## Step 3: Deploy the New Service

```text
Search Service consumes Post events.
Search Service builds its own projection.
Search Service exposes a remote API.
```

## Step 4: Shadow or Compare Results

Where practical:

```text
Run local and remote search paths.
Compare result correctness.
Measure latency and error rate.
```

## Step 5: Route Traffic Gradually

```text
Small percentage
      ↓
Observe
      ↓
Increase percentage
      ↓
Complete cutover
```

## Step 6: Remove the Old Module

Only after the new service meets correctness, reliability, and performance goals should the old implementation be removed.

---

# Why a Big-Bang Rewrite Is Rejected

A big-bang rewrite would attempt to create all services at once.

Risks:

```text
No incremental validation
Long-running branch divergence
Large operational learning curve
Difficult rollback
Many simultaneous unknowns
Delayed user value
```

Decision:

```text
Use incremental extraction with measurable checkpoints.
```

---

# Architectural Decision Records

## ADR-1: Keep the Modular Monolith as the Starting Architecture

Decision:

```text
Do not decompose all modules into services.
```

Reason:

```text
The current system benefits from local transactions, simple deployment, and low operational cost.
```

---

## ADR-2: Extract Search First

Decision:

```text
Search is the first candidate for service extraction.
```

Reason:

```text
Dedicated API
Dedicated projection
Independent scaling
Eventual consistency is acceptable
Clean failure boundary
```

---

## ADR-3: Extract Timeline Second

Decision:

```text
Timeline is the second candidate.
```

Reason:

```text
Dedicated projection
Read-heavy workload
Event-driven ingestion
Independent cache and scaling requirements
```

---

## ADR-4: Extract Notification Third

Decision:

```text
Notification is the third candidate.
```

Reason:

```text
Naturally asynchronous
Independent delivery lifecycle
Clear operational isolation
```

---

## ADR-5: Do Not Centralize the Outbox

Decision:

```text
Each transactional service owns its own outbox mechanism.
```

Reason:

```text
Outbox persistence must share the local business transaction.
A centralized outbox would recreate cross-service transactional coupling.
```

---

## ADR-6: Prefer Events for Projection Updates

Decision:

```text
Search, timeline, and notification projections consume domain events.
```

Reason:

```text
Loose coupling
Independent scaling
Replay and rebuild capability
Failure isolation
```

---

# Success Criteria for an Extraction

A service extraction is successful only when the new service demonstrates:

```text
Exclusive data ownership
No shared-table writes
Stable API or event contracts
Independent deployment
Independent scaling
Independent observability
Defined SLOs
Documented failure behavior
Idempotent event processing
Backfill or rebuild strategy
Rollback strategy
Clear team ownership
```

Creating a new repository or deployment alone does not satisfy these criteria.

---

# Metrics to Collect Before Extracting

Architecture decisions should be informed by evidence.

Recommended measurements:

```text
Request rate per module
Read/write ratio
P50, P95, and P99 latency
CPU and memory contribution
Database query volume
Cache hit rate
Kafka consumer lag
Projection update delay
Error rate
Deployment frequency
Change coupling between modules
Incident blast radius
```

These metrics help determine whether independent scaling or failure isolation is genuinely required.

---

# Risks and Mitigations

## Risk: Distributed Monolith

Description:

```text
Services are deployed separately but remain tightly coupled through synchronous calls and shared data.
```

Mitigation:

```text
Exclusive data ownership
Asynchronous integration where appropriate
Stable contracts
Minimal synchronous dependency chains
```

---

## Risk: Shared Database Coupling

Mitigation:

```text
Database-per-service ownership
Events and APIs for cross-boundary access
No cross-service table updates
```

---

## Risk: Projection Drift

Mitigation:

```text
Idempotent consumers
Reconciliation jobs
Rebuild procedures
Consumer lag monitoring
Dead-letter handling
```

---

## Risk: Cascading Failure

Mitigation:

```text
Timeouts
Circuit breakers
Bulkheads
Fallbacks
Rate limits
Asynchronous communication
```

---

## Risk: Premature Extraction

Mitigation:

```text
Measure first
Document the architectural force
Extract one service at a time
Preserve rollback capability
```

---

# Phase-15 Deliverables

```text
Service decomposition evaluation framework
Current modular-monolith assessment
Boundary scorecard
Search Service extraction rationale
Timeline Service extraction rationale
Notification Service extraction rationale
Database ownership strategy
Communication strategy
Event contract guidance
Distributed-system risk analysis
Incremental extraction roadmap
Architecture decision records
Success criteria and metrics
```

---

# Phase-15 Completion Status

```text
Phase-15: Service Decomposition Strategy

✅ Current architecture reviewed
✅ Decomposition criteria defined
✅ Candidate services evaluated
✅ Search selected as first candidate
✅ Timeline selected as second candidate
✅ Notification selected as third candidate
✅ Core modules intentionally retained
✅ Database ownership model defined
✅ Communication models compared
✅ Distributed-system trade-offs documented
✅ Incremental migration strategy defined
✅ Big-bang rewrite rejected
✅ ADRs documented
✅ Extraction success criteria defined
```

---

# Key Takeaways

```text
Microservices are not the default architecture.

Packages and services are different kinds of boundaries.

Service boundaries should follow ownership, scaling, data, and failure characteristics.

Independent read models are strong extraction candidates.

Search is the best first extraction candidate in the current platform.

Timeline is the best second extraction candidate.

Notification is a natural asynchronous extraction candidate.

Core transactional modules should remain together until evidence justifies separation.

Each service must exclusively own its data.

Events enable loose coupling but introduce eventual consistency, duplicates, ordering, and schema-evolution challenges.

Operational ownership is part of service ownership.

Incremental extraction is safer than a big-bang rewrite.

The right question is not "How many microservices should we have?"
The right question is "Which independent system boundaries are justified?"
```

---

# Recommended Next Phase

The recommended next system-design phase is:

```text
Phase-16: Multi-Region Architecture and Disaster Recovery
```

Potential learning topics:

```text
Single-region failure analysis
Availability zones versus regions
Recovery Point Objective
Recovery Time Objective
Active-passive architecture
Active-active architecture
Database replication
Kafka replication
Object-storage replication
Global routing
CAP theorem trade-offs
Conflict handling
Regional failover testing
```

This continues the system-design learning path by moving from service boundaries to global availability and resilience.
