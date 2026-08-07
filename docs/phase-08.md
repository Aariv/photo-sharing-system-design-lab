# Phase-08 Distributed Tracing with OpenTelemetry

## Goal

Extend the observability stack beyond logs and metrics by introducing distributed tracing.

Before this phase:

```text
Logs
✅ Available

Metrics
✅ Available

Traces
❌ Not Available
```

After this phase:

```text
Logs
✅ Available

Metrics
✅ Available

Traces
✅ Available
```

The goal was to understand how requests flow through the system and identify where time is spent.

---

# Problem

Prometheus and Grafana provide metrics such as:

- Feed Requests
- Cache Hits
- Cache Misses
- Notifications Created
- Events Published

These metrics answer:

```text
What happened?
```

However they do not answer:

```text
Why did it happen?

Which component caused latency?

Where is the bottleneck?
```

---

# Solution

Added:

- OpenTelemetry
- Jaeger
- Custom Business Spans

The application now exports traces to Jaeger and allows visualization of complete request execution paths.

---

# Architecture

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
             OpenTelemetry
                   |
                   v
                Jaeger

                   |
                   v
              Prometheus
                   |
                   v
                Grafana
```

---

# Infrastructure

## Jaeger

Container:

```yaml
jaeger:
  image: jaegertracing/all-in-one:latest

  container_name: photoshare-jaeger

  ports:
    - "16686:16686"
    - "4317:4317"
    - "4318:4318"
```

UI:

```text
http://localhost:16686
```

---

# OpenTelemetry Configuration

```properties
quarkus.application.name=photo-sharing-api

quarkus.otel.enabled=true

quarkus.otel.exporter.otlp.endpoint=http://localhost:4318
```

---

# Automatic Tracing

Out-of-the-box OpenTelemetry traces:

```text
GET /api/v1/feed

POST /api/v1/posts/{postId}/like

POST /api/v1/uploads
```

---

# Custom Spans

Implemented using:

```java
@WithSpan
```

---

## Feed Flow

### feed-service

Responsible for:

```text
Feed Construction
Feed Response Generation
```

---

### redis-cache-check

Responsible for:

```text
Cache Lookup

Cache Hit Detection

Cache Miss Detection
```

---

### generate-presigned-url

Responsible for:

```text
MinIO URL Generation

Object Access
```

---

## Like Flow

### like-post

Responsible for:

```text
Like Creation

Like Workflow Processing
```

---

### publish-post-liked-event

Responsible for:

```text
Kafka Publish
```

---

### post-liked publish

Responsible for:

```text
Producer Execution
```

---

### post-liked receive

Responsible for:

```text
Kafka Consumer Reception
```

---

## Notification Flow

### notification-consumer

Responsible for:

```text
Notification Creation
Notification Processing
```

---

# Example Feed Trace

```text
GET /api/v1/feed

    |
    +-- feed-service

            |
            +-- redis-cache-check

            |
            +-- generate-presigned-url
```

---

# Example Notification Trace

```text
POST /api/v1/posts/{postId}/like

        |
        +-- like-post

                |
                +-- publish-post-liked-event

                        |
                        +-- post-liked publish

                                |
                                +-- post-liked receive

                                        |
                                        +-- notification-consumer
```

---

# Benefits

## Request Visibility

Understand the complete lifecycle of a request.

Example:

```text
Request

Cache

Database

Object Storage

Response
```

---

## Bottleneck Identification

Identify which component contributes most to latency.

Examples:

```text
Redis

Database

Kafka

MinIO
```

---

## Faster Troubleshooting

Instead of:

```text
Feed is slow
```

we can see:

```text
Feed Query = 40ms

Redis = 2ms

Presigned URLs = 12ms
```

---

# Learning Outcomes

### OpenTelemetry

Learned:

- Trace Fundamentals
- Instrumentation
- Span Design

---

### Distributed Tracing

Learned:

- Request Flow Visualization
- Service Timing
- Dependency Analysis

---

### Observability

Completed the three pillars:

```text
Logs
Metrics
Traces
```

---

# Current Observability Stack

```text
Micrometer
    |
Prometheus
    |
Grafana

OpenTelemetry
    |
Jaeger
```

---

# Future Enhancements

## Database Tracing

Add:

```text
PostgreSQL Query Tracing
```

to identify slow queries.

---

## Redis Tracing

Add:

```text
Redis Command Tracing
```

for cache investigations.

---

## Kafka Context Propagation

Trace:

```text
Producer
     |
Kafka
     |
Consumer
```

within a single trace tree.

---

## MinIO Tracing

Trace:

```text
Upload

Download

Presigned URL Generation
```

with timing details.

---

# Success Criteria

Completed:

- ✅ Jaeger Running
- ✅ OpenTelemetry Configured
- ✅ HTTP Traces
- ✅ Feed Traces
- ✅ Redis Traces
- ✅ Kafka Producer Traces
- ✅ Kafka Consumer Traces
- ✅ Notification Traces
- ✅ MinIO Traces
- ✅ Business-Level Spans

---

# Phase Summary

Phase-08 introduced distributed tracing into the platform using OpenTelemetry and Jaeger.

The application evolved from monitoring system metrics to understanding complete request execution flows across API endpoints, cache layers, messaging infrastructure, notification processing, and object storage integrations.

The platform now supports the three pillars of observability:

```text
Logs
Metrics
Traces
```

forming the foundation for future microservice extraction and distributed system operation.
