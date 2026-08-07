# Phase-07 Microservice Boundary Design

## Goal

Define service boundaries and ownership within the current modular monolith.

The objective of this phase is not to split the application into multiple deployable services, but to identify the natural boundaries that will support future evolution into a distributed system.

Current architecture remains:

```text
Single Application
Single Database
Single Deployment
```

Future architecture:

```text
Multiple Services
Independent Deployment
Independent Ownership
Event-Driven Communication
```

---

# Current Structure

The application is already organized around business domains.

```text
com.ariv.photoshare

├── admin
├── cache
├── comment
├── events
├── feed
├── follow
├── like
├── notification
├── post
├── upload
└── user
```

Each domain contains:

```text
dto
entity
repository
resource
service
```

This structure naturally supports future microservice extraction.

---

# Service Decomposition

## User Service

### Responsibilities

- User Registration
- User Profile
- User Metadata

### Owns

```text
users
```

### APIs

```text
POST /users/signup

GET /users/{id}/profile
```

### Events

```text
UserCreatedEvent
```

---

## Post Service

### Responsibilities

- Posts
- Comments
- Likes
- Media Metadata

### Owns

```text
posts
comments
likes
```

### APIs

```text
POST /posts

GET /posts/{id}

POST /posts/{id}/likes

POST /posts/{id}/comments
```

### Events

```text
PostCreatedEvent

PostLikedEvent

CommentCreatedEvent
```

---

## Feed Service

### Responsibilities

- Feed Aggregation
- Feed Caching
- Timeline Construction
- Pagination

### Owns

```text
Redis Cache
Feed Logic
```

### Integrations

```text
Redis
```

### Events Consumed

```text
PostCreatedEvent

PostLikedEvent

CommentCreatedEvent
```

---

## Notification Service

### Responsibilities

- Notification Generation
- Notification Delivery
- Event Consumption

### Owns

```text
notifications
```

### Events Consumed

```text
PostLikedEvent

CommentCreatedEvent

UserFollowedEvent
```

---

## Media Service

### Responsibilities

- Image Uploads
- Object Storage
- Presigned URLs

### Owns

```text
Object Storage
```

### Integrations

```text
MinIO
```

### APIs

```text
POST /uploads

GET /uploads/{objectName}
```

---

# Database Ownership

Every table should have a single owner.

| Table | Future Owner |
|---------|---------|
| users | User Service |
| posts | Post Service |
| comments | Post Service |
| likes | Post Service |
| followers | User Service |
| notifications | Notification Service |

---

# Event Catalog

## Existing Events

### PostLikedEvent

Producer:

```text
Post Service
```

Consumers:

```text
Notification Service
```

Payload:

```text
postId
userId
createdAt
```

---

## Future Events

### PostCreatedEvent

Producer:

```text
Post Service
```

Consumers:

```text
Feed Service
Notification Service
```

---

### CommentCreatedEvent

Producer:

```text
Post Service
```

Consumers:

```text
Notification Service
Feed Service
```

---

### UserFollowedEvent

Producer:

```text
User Service
```

Consumers:

```text
Notification Service
Feed Service
```

---

# Current Architecture

```text
                Client
                   |
                   v
             Quarkus API
           /      |       \
          /       |        \
         v        v         v
   PostgreSQL   Redis   Redpanda
                              |
                       Notifications

               MinIO Object Storage

                   |
                   v
              Micrometer
                   |
                   v
              Prometheus
                   |
                   v
                Grafana
```

---

# Future Architecture

```text
                    Client
                       |
                       v
                  API Gateway
                       |
     ----------------------------------------
     |              |            |          |
     v              v            v          v

 User Service  Post Service  Feed Service  Media Service
                                    |
                                  Redis

                       |
                     Kafka
                       |
                       v

              Notification Service

                       |
                  PostgreSQL

                       |
                     MinIO
```

---

# Migration Strategy

## Step 1

Current:

```text
Monolith
```

Status:

```text
Completed
```

---

## Step 2

Current:

```text
Modular Monolith
```

Status:

```text
Completed
```

---

## Step 3

Future:

```text
Extract Notification Service
```

Reason:

```text
Already event-driven
Low coupling
Independent workload
```

---

## Step 4

Future:

```text
Extract Media Service
```

Reason:

```text
Owns MinIO integration
Well-defined boundary
```

---

## Step 5

Future:

```text
Extract Feed Service
```

Reason:

```text
Owns Redis
Independent scaling requirements
```

---

# Learning Outcomes

Through this phase the following concepts were explored:

- Bounded Contexts
- Service Ownership
- Database Ownership
- Event-Driven Architecture
- Modular Monolith Design
- Microservice Extraction Strategy
- Service Boundaries
- Domain Separation

---

# Phase Summary

The application has reached a point where clear business domains exist.

Instead of prematurely splitting the application into multiple deployable services, the architecture was analyzed and future service boundaries were identified.

This phase establishes the foundation for future microservice extraction while preserving the simplicity and productivity of a monolithic deployment.