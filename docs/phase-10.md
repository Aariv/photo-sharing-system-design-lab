# Phase-10: Kubernetes Platform Components

## Overview

In Phase-09, the `photo-sharing-api` was successfully containerized and deployed into a Kubernetes cluster using Kind.

At that point, the application was running inside Kubernetes, but the core infrastructure dependencies were still running outside the cluster.

The external dependencies were:

```text
PostgreSQL
Redis
Kafka / Redpanda
MinIO
```

The application accessed these services using:

```text
host.docker.internal
```

Phase-10 focuses on moving key platform components into Kubernetes and learning how Kubernetes manages configuration, secrets, service discovery, and persistent state.

This phase marks the transition from:

```text
Running an application on Kubernetes
```

to:

```text
Running a platform inside Kubernetes
```

---

## Current State Before Phase-10

### Kubernetes Cluster

The Kind cluster currently runs:

```text
photo-sharing-api
```

The API is deployed using:

```text
Deployment
Service
Port Forwarding
Rolling Updates
```

### Infrastructure Dependencies

The following services are still running outside the Kubernetes cluster:

```text
PostgreSQL
Redis
Kafka / Redpanda
MinIO
```

### Current Communication Model

```text
photo-sharing-api pod
        |
        v
host.docker.internal
        |
        v
PostgreSQL / Redis / Kafka / MinIO
```

This setup works for local learning, but it does not represent how applications usually communicate inside Kubernetes.

---

## Goal of Phase-10

The goal of Phase-10 is to bring core platform components under Kubernetes management.

By the end of this phase, the application should communicate with infrastructure services using Kubernetes Services instead of `host.docker.internal`.

The target state is:

```text
Kind Cluster
|
|-- photo-sharing-api
|-- PostgreSQL
|-- Redis
|-- MinIO
|-- ConfigMaps
|-- Secrets
|-- Persistent Volumes
|-- Persistent Volume Claims
```

Kafka / Redpanda will intentionally remain outside the cluster for now to keep the learning focused.

---

## Scope of Phase-10

### Included in This Phase

Phase-10 includes the following topics:

```text
ConfigMaps
Secrets
Persistent Volumes
Persistent Volume Claims
PostgreSQL StatefulSet
Redis Deployment
MinIO Deployment
Kubernetes Service Discovery
```

### Excluded from This Phase

The following topics are intentionally postponed:

```text
Kafka inside Kubernetes
Ingress
Helm
Autoscaling
Service Mesh
Microservice Extraction
Production-grade storage classes
Multi-node database setup
```

These topics will be handled in later phases.

---

## Learning Objectives

By completing this phase, I should understand:

1. How Kubernetes externalizes configuration using ConfigMaps
2. How Kubernetes stores sensitive values using Secrets
3. Why pod storage is temporary by default
4. How Persistent Volumes and Persistent Volume Claims preserve data
5. Why databases usually need StatefulSets
6. How Kubernetes Services provide stable endpoints
7. How applications communicate inside a cluster without using host networking
8. How to move platform dependencies into Kubernetes incrementally

---

## Key Concepts

## 1. ConfigMaps

ConfigMaps are used to externalize non-sensitive application configuration.

Instead of baking configuration directly into the Docker image, configuration can be provided by Kubernetes at runtime.

Example configuration values:

```text
DATABASE_HOST
DATABASE_PORT
REDIS_HOST
REDIS_PORT
MINIO_ENDPOINT
KAFKA_BOOTSTRAP_SERVERS
APP_ENVIRONMENT
```

### Key Lesson

```text
Configuration should not require rebuilding the application image.
```

This supports the principle:

```text
Build once, deploy anywhere.
```

---

## 2. Secrets

Secrets are used to manage sensitive values.

Examples:

```text
POSTGRES_USER
POSTGRES_PASSWORD
MINIO_ACCESS_KEY
MINIO_SECRET_KEY
```

These values should not be stored directly in:

```text
Source code
Docker images
Plain application properties
Git repositories
```

### Key Lesson

```text
Secrets are different from normal configuration.
```

Configuration describes how the application should run.

Secrets provide sensitive credentials required to access protected systems.

---

## 3. Persistent Volumes

By default, pod storage is temporary.

If a pod is deleted or recreated, data written inside the pod can be lost.

This is acceptable for stateless applications, but not for stateful systems like databases or object storage.

Examples of services that require persistence:

```text
PostgreSQL
MinIO
```

### Key Lesson

```text
Pod lifecycle is not the same as data lifecycle.
```

Pods are disposable.

Data must survive pod recreation.

---

## 4. Persistent Volume Claims

Persistent Volume Claims allow workloads to request storage from Kubernetes.

Instead of a pod directly managing disk storage, the pod requests storage through a PVC.

Example:

```text
Request 5Gi storage
Mount it into PostgreSQL
Keep data across pod restarts
```

### Key Lesson

```text
Applications request storage.
Kubernetes provides storage.
```

---

## 5. StatefulSets

StatefulSets are used for workloads that need stable identity and persistent storage.

PostgreSQL is a good example.

Unlike a Deployment, a StatefulSet provides predictable pod names such as:

```text
postgres-0
```

This identity remains stable even if the pod is recreated.

### Deployment vs StatefulSet

```text
Deployment  = Stateless workload
StatefulSet = Stateful workload
```

### Key Lesson

```text
Databases need stable identity and persistent storage.
```

---

## 6. Kubernetes Service Discovery

Inside Kubernetes, applications should communicate using Services.

Instead of using:

```text
host.docker.internal
```

the application should use service names such as:

```text
postgres-service
redis-service
minio-service
```

Kubernetes Services provide stable endpoints even when pods are recreated.

### Key Lesson

```text
Pods are dynamic.
Services are stable.
```

---

## Target Architecture After Phase-10

```text
                         Kind Cluster
                              |
        -------------------------------------------------
        |                     |                         |
        v                     v                         v
 photo-sharing-api       PostgreSQL                   Redis
        |                 StatefulSet              Deployment
        |                     |                         |
        |                     v                         v
        |                    PVC                    Service
        |
        v
      MinIO
   Deployment
        |
        v
       PVC
```

Kafka / Redpanda remains external for now:

```text
Kafka / Redpanda
Outside Cluster
```

---

## Expected Communication After Phase-10

### Before Phase-10

```text
photo-sharing-api
        |
        v
host.docker.internal
        |
        v
PostgreSQL / Redis / MinIO
```

### After Phase-10

```text
photo-sharing-api
        |
        v
postgres-service

photo-sharing-api
        |
        v
redis-service

photo-sharing-api
        |
        v
minio-service
```

The application will communicate through Kubernetes service names.

---

## Planned Kubernetes Resources

## API Resources

```text
Deployment
Service
ConfigMap
Secret
```

## PostgreSQL Resources

```text
StatefulSet
Service
PersistentVolumeClaim
Secret
```

## Redis Resources

```text
Deployment
Service
```

## MinIO Resources

```text
Deployment
Service
PersistentVolumeClaim
Secret
```

---

## Recommended Folder Structure

```text
k8s/
├── namespace.yaml
│
├── api/
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── configmap.yaml
│   └── secret.yaml
│
├── postgres/
│   ├── statefulset.yaml
│   ├── service.yaml
│   └── pvc.yaml
│
├── redis/
│   ├── deployment.yaml
│   └── service.yaml
│
└── minio/
    ├── deployment.yaml
    ├── service.yaml
    └── pvc.yaml
```

---

## Implementation Plan

## Step 1: Add ConfigMap for API

Create a ConfigMap for non-sensitive application configuration.

Example values:

```text
APP_ENVIRONMENT
POSTGRES_HOST
POSTGRES_PORT
REDIS_HOST
REDIS_PORT
MINIO_ENDPOINT
KAFKA_BOOTSTRAP_SERVERS
```

Objective:

```text
Move environment-specific values out of the Docker image.
```

---

## Step 2: Add Secrets for API

Create Kubernetes Secrets for sensitive values.

Example values:

```text
POSTGRES_USER
POSTGRES_PASSWORD
MINIO_ACCESS_KEY
MINIO_SECRET_KEY
```

Objective:

```text
Separate credentials from normal configuration.
```

---

## Step 3: Deploy PostgreSQL Inside Kubernetes

Deploy PostgreSQL using:

```text
StatefulSet
Service
PersistentVolumeClaim
Secret
```

Objective:

```text
Run database inside Kubernetes with persistent storage.
```

Validation:

```text
Create data
Delete PostgreSQL pod
Wait for pod recreation
Verify data still exists
```

---

## Step 4: Update API to Use PostgreSQL Service

Update the API configuration to use:

```text
postgres-service
```

instead of:

```text
host.docker.internal
```

Objective:

```text
Use Kubernetes service discovery for database communication.
```

---

## Step 5: Deploy Redis Inside Kubernetes

Deploy Redis using:

```text
Deployment
Service
```

Objective:

```text
Move Redis cache into Kubernetes.
```

Validation:

```text
API should connect to Redis using redis-service.
Feed caching should continue working.
```

---

## Step 6: Deploy MinIO Inside Kubernetes

Deploy MinIO using:

```text
Deployment
Service
PersistentVolumeClaim
Secret
```

Objective:

```text
Move object storage into Kubernetes with persistent storage.
```

Validation:

```text
Upload image
Restart MinIO pod
Verify uploaded object still exists
```

---

## Step 7: Remove host.docker.internal Dependency

Once PostgreSQL, Redis, and MinIO are running inside Kubernetes, update API configuration to avoid host-based networking.

Before:

```text
host.docker.internal
```

After:

```text
postgres-service
redis-service
minio-service
```

Objective:

```text
Make the application depend on Kubernetes-native networking.
```

---

## Success Criteria

Phase-10 is complete when:

```text
ConfigMap is created for API configuration
Secrets are created for sensitive values
PostgreSQL runs inside Kubernetes
PostgreSQL uses persistent storage
PostgreSQL data survives pod recreation
Redis runs inside Kubernetes
MinIO runs inside Kubernetes
MinIO uses persistent storage
PhotoShare API connects to PostgreSQL using Kubernetes Service
PhotoShare API connects to Redis using Kubernetes Service
PhotoShare API connects to MinIO using Kubernetes Service
host.docker.internal is no longer required for PostgreSQL, Redis, and MinIO
Existing APIs continue to work
Feed API continues to work
Upload API continues to work
Like and notification flow continues to work
```

---

## Important Learnings

## Learning 1: Pods Are Disposable

Pods can be deleted, restarted, or replaced at any time.

Applications should not depend on pod identity unless they are managed through StatefulSets.

```text
Pod = temporary compute unit
```

---

## Learning 2: Services Provide Stability

Pods may change, but Services provide a stable endpoint.

```text
Service = stable network identity
```

---

## Learning 3: Configuration Should Be External

Configuration should change without rebuilding the Docker image.

```text
Application image should be environment-agnostic.
```

---

## Learning 4: Secrets Should Be Isolated

Credentials should be managed separately from application code and configuration.

```text
Sensitive values require a different handling model.
```

---

## Learning 5: Storage Must Outlive Pods

Stateful systems need persistent storage.

```text
If a database pod dies, the data should not die with it.
```

---

## Learning 6: StatefulSets Are for Stateful Workloads

Databases need stable identity and storage.

```text
PostgreSQL is better represented by StatefulSet than Deployment.
```

---

## Learning 7: Kubernetes Encourages Platform Thinking

After this phase, the project is no longer just an application deployed to Kubernetes.

It becomes a small platform running inside Kubernetes.

```text
Application
Configuration
Secrets
Database
Cache
Object Storage
Networking
Storage
```

---

## Phase-10 Milestone

At the end of Phase-10, the project evolves from:

```text
A Kubernetes-hosted application
```

to:

```text
A Kubernetes-hosted platform
```

This is a major milestone because Kubernetes will now manage not just application execution, but also configuration, credentials, storage, networking, and stateful infrastructure.

---

## Next Phase Preview

After Phase-10, the next logical phase is:

```text
Phase-11: Kubernetes Production Readiness
```

Expected topics:

```text
Readiness Probes
Liveness Probes
Startup Probes
Resource Requests
Resource Limits
Horizontal Pod Autoscaler
Graceful Shutdown
Deployment Health
```

Phase-11 will focus on making the Kubernetes deployment more production-like.
