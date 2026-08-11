# Phase-10 Progress Log: Kubernetes Platform Components

## Date

2026-08-11

## Project

`photo-sharing-system-design-lab`

## Phase

`Phase-10: Kubernetes Platform Components`

## Today's Focus

Today we progressed from simply running the `photo-sharing-api` inside Kubernetes to introducing core Kubernetes platform components.

The main focus was:

```text
ConfigMap
Secret
PostgreSQL StatefulSet
Persistent Volume Claim
Redis Deployment
Kubernetes Service Discovery
```

---

## Starting State

Before today's changes, the application was running inside the Kind Kubernetes cluster, but most platform dependencies were still running outside the cluster.

```text
Kind Cluster
|
|-- photoshare-api

Host Machine / Docker
|
|-- PostgreSQL
|-- Redis
|-- Kafka / Redpanda
|-- MinIO
```

The application was communicating with external services using:

```text
host.docker.internal
```

This worked for local learning, but it was not Kubernetes-native.

---

# Step-1: ConfigMap and Secret

## Goal

Externalize application configuration and credentials from `application.properties` into Kubernetes-managed resources.

The goal was to move from:

```text
Hardcoded application config
```

to:

```text
Kubernetes ConfigMap + Secret
```

---

## ConfigMap

A ConfigMap was introduced to store non-sensitive runtime configuration.

Example values managed by ConfigMap:

```text
DB_URL
MINIO_URL
KAFKA_BOOTSTRAP_SERVERS
REDIS_HOST
REDIS_PORT
```

## Secret

A Kubernetes Secret was introduced to store sensitive values.

Example values managed by Secret:

```text
DB_USERNAME
DB_PASSWORD
MINIO_ACCESS_KEY
MINIO_SECRET_KEY
```

---

## Application Properties Refactor

The application was updated so that Quarkus reads runtime values from environment variables.

Example:

```properties
quarkus.datasource.jdbc.url=${DB_URL}
quarkus.datasource.username=${DB_USERNAME}
quarkus.datasource.password=${DB_PASSWORD}
```

This allowed Kubernetes to inject configuration into the application pod at runtime.

---

## Validation

Environment variables were verified from inside the running pod using:

```bash
kubectl exec -it <photoshare-api-pod> -- env
```

Verified values included:

```text
DB_URL=jdbc:postgresql://host.docker.internal:5432/photoshare
DB_USERNAME=postgres
DB_PASSWORD=postgres
MINIO_URL=http://host.docker.internal:9000
KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092
```

The application started successfully.

Quarkus logs confirmed:

```text
photo-sharing-api 1.0.0-SNAPSHOT started
Listening on: http://0.0.0.0:8080
Profile prod activated
```

Flyway also confirmed database connectivity:

```text
Successfully validated 7 migrations
Schema public is up to date
```

---

## Step-1 Status

```text
Phase-10 Step-1 = COMPLETE
```

Completed:

```text
ConfigMap created
Secret created
Deployment updated
Environment variables injected
Application started successfully
Database connectivity verified
Flyway migration validation completed
```

---

# Step-2: PostgreSQL StatefulSet

## Goal

Deploy PostgreSQL inside Kubernetes and learn how Kubernetes handles stateful workloads.

This introduced the following Kubernetes concepts:

```text
StatefulSet
Persistent Volume Claim
Headless Service
Stable Pod Identity
Persistent Storage
```

---

## Why StatefulSet?

PostgreSQL is a stateful workload.

Unlike stateless application pods, database pods need:

```text
Stable identity
Stable storage
Data persistence across pod recreation
```

A StatefulSet provides these characteristics.

---

## PostgreSQL Resources Created

The following Kubernetes resources were created:

```text
postgres-secret
postgres-service
postgres StatefulSet
postgres-storage-postgres-0 PVC
```

The PostgreSQL pod was created as:

```text
postgres-0
```

This stable name is an important StatefulSet behavior.

---

## Validation

Pods were checked using:

```bash
kubectl get pods
```

Output confirmed:

```text
postgres-0   1/1   Running
```

PVC was checked using:

```bash
kubectl get pvc
```

Output confirmed:

```text
postgres-storage-postgres-0   Bound   2Gi   RWO   standard
```

This confirmed that Kubernetes successfully provisioned and attached persistent storage to PostgreSQL.

---

## Persistence Test

A test table and row were created inside the Kubernetes PostgreSQL instance.

Example:

```sql
create table demo (
    id int primary key,
    name varchar(100)
);

insert into demo values (1, 'statefulset-test');
```

Then the pod was deleted:

```bash
kubectl delete pod postgres-0
```

Kubernetes recreated the pod with the same identity:

```text
postgres-0
```

After reconnecting, the data was still available.

This confirmed:

```text
Pod lifecycle is not equal to data lifecycle
```

---

## Step-2 Status

```text
Phase-10 Step-2 = COMPLETE
```

Completed:

```text
PostgreSQL StatefulSet created
Headless Service created
PVC created
PostgreSQL pod running
Persistent storage bound
Data persistence validated
```

---

# Step-3: Redis Deployment

## Goal

Deploy Redis inside Kubernetes and use Kubernetes Service Discovery instead of `host.docker.internal`.

Redis was treated as a stateless cache for this learning phase.

Reason:

```text
If Redis cache is lost, the application can rebuild it from PostgreSQL.
```

Therefore Redis was deployed using:

```text
Deployment
Service
```

instead of StatefulSet.

---

## Redis Resources Created

The following Kubernetes resources were created:

```text
redis Deployment
redis-service Service
```

---

## Validation

Pods were checked using:

```bash
kubectl get pods
```

Output confirmed:

```text
redis-56c9d5db58-sps84   1/1   Running
```

Services were checked using:

```bash
kubectl get svc
```

Output confirmed:

```text
redis-service   ClusterIP   10.96.7.250   6379/TCP
```

---

## Service Discovery Learning

The application no longer needs to know the Redis pod IP.

Instead of connecting to:

```text
host.docker.internal
```

it can connect to:

```text
redis-service
```

This demonstrates Kubernetes-native service discovery.

---

## Step-3 Status

```text
Phase-10 Step-3 = COMPLETE
```

Completed:

```text
Redis Deployment created
Redis Service created
Redis pod running
Redis ClusterIP service available
Kubernetes service discovery introduced
```

---

# Current Kubernetes State

## Pods

```text
photoshare-api-86d74cc4f8-642fp   1/1   Running
photoshare-api-86d74cc4f8-g6r9h   1/1   Running
photoshare-api-86d74cc4f8-kwn85   1/1   Running
postgres-0                        1/1   Running
redis-56c9d5db58-sps84            1/1   Running
```

## Services

```text
kubernetes         ClusterIP   10.96.0.1       443/TCP
photoshare-api     NodePort    10.96.150.219   8080:32535/TCP
postgres-service   ClusterIP   None            5432/TCP
redis-service      ClusterIP   10.96.7.250     6379/TCP
```

## Persistent Volume Claims

```text
postgres-storage-postgres-0   Bound   2Gi   RWO   standard
```

---

# Current Architecture

```text
Kind Cluster
|
|-- photoshare-api Deployment
|   |-- 3 replicas
|
|-- postgres StatefulSet
|   |-- postgres-0
|   |-- postgres-storage-postgres-0 PVC
|
|-- redis Deployment
|   |-- redis-service
|
|-- ConfigMap
|-- Secret

External / Host Machine
|
|-- Kafka / Redpanda
|-- MinIO
```

---

# Important Learnings From Today

## 1. ConfigMap Externalizes Configuration

Configuration can be managed outside the application image.

```text
Build once, deploy anywhere
```

---

## 2. Secrets Separate Sensitive Values

Credentials should not be hardcoded inside application properties or Docker images.

```text
Secrets are different from normal configuration
```

---

## 3. StatefulSet Is for Stateful Workloads

PostgreSQL needs stable identity and persistent storage.

```text
Deployment  = Stateless workload
StatefulSet = Stateful workload
```

---

## 4. PVC Preserves Data

Deleting a pod should not delete database data.

```text
Pod lifecycle != Data lifecycle
```

---

## 5. Service Discovery Removes Pod Dependency

Applications should talk to Services, not Pods.

```text
Pods are dynamic
Services are stable
```

---

## 6. Redis Can Be Stateless in This Learning Phase

Since Redis is being used as cache, losing Redis data does not mean losing business data.

```text
Cache can be rebuilt
Database remains source of truth
```

---

# Phase-10 Progress

```text
Phase-10 Kubernetes Platform Components
|
|-- Step-1 ConfigMap and Secret                 COMPLETE
|-- Step-2 PostgreSQL StatefulSet and PVC       COMPLETE
|-- Step-3 Redis Deployment and Service         COMPLETE
|-- Step-4 MinIO Deployment and PVC             NEXT
|-- Step-5 Move API to Kubernetes PostgreSQL    UPCOMING
|-- Step-6 Remove host.docker.internal          UPCOMING
```

---

# Next Step

The next logical step is:

```text
Step-4: Deploy MinIO inside Kubernetes
```

This will introduce persistent object storage inside the cluster using:

```text
MinIO Deployment
MinIO Service
MinIO PVC
MinIO Secret
```

After that, the application can gradually move away from host-based dependencies.

---

# Suggested Commit Message

```text
docs: document phase-10 kubernetes platform progress
```

or

```text
feat(k8s): add postgres statefulset and redis deployment
```

If committing both documentation and Kubernetes manifests together, use:

```text
feat(k8s): add phase-10 platform components
```
