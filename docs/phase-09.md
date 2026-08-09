# Phase-09 Kubernetes Foundations with Kind

## Goal

Deploy the Photo Sharing API to Kubernetes and understand the fundamental Kubernetes primitives before moving infrastructure components into the cluster.

Objectives:

- Containerize application
- Deploy application to Kubernetes
- Understand Pods
- Understand Deployments
- Understand Services
- Learn container networking
- Learn configuration externalization
- Experience rolling deployments

---

# Problem Statement

The application was successfully running in:

```text
IntelliJ
     ↓
Quarkus Dev Mode
```

However, the application was not yet cloud-ready.

Goals:

```text
Build Docker Image

Run In Container

Deploy To Kubernetes

Access APIs Through Kubernetes
```

---

# Containerization

## Build Application

```bash
./mvnw clean package -DskipTests
```

Generated:

```text
target/quarkus-app
```

---

## Build Docker Image

```bash
docker build \
  -f src/main/docker/Dockerfile.jvm \
  -t photoshare-api:v1 .
```

Verify:

```bash
docker images
```

Result:

```text
photoshare-api:v1
```

---

# Run Container

## Start Container

```bash
docker run \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/photoshare \
  -p 8080:8080 \
  photoshare-api:v1
```

---

## Results

Successfully verified:

```text
✅ Flyway Migrations

✅ PostgreSQL Connectivity

✅ Quarkus Startup

✅ REST APIs

✅ OpenTelemetry Initialization
```

Log:

```text
photo-sharing-api started

Listening on:
http://0.0.0.0:8080
```

---

# First Containerization Lessons

Discovered that:

```text
localhost
```

inside the container means:

```text
the container itself
```

and NOT the host machine.

Examples:

```text
localhost:5432

localhost:9092

localhost:9000
```

failed when accessed from inside the container.

---

# Configuration Externalization

Refactored infrastructure endpoints to be configurable.

Examples:

```properties
DB_URL

KAFKA_BOOTSTRAP_SERVERS

MINIO_URL

REDIS_HOSTS
```

Learning:

```text
Container Images Should Be Environment Agnostic
```

---

# Kind Cluster

## Install Kind

```bash
brew install kind
```

---

## Create Cluster

```bash
kind create cluster --name photoshare
```

---

## Verify Cluster

```bash
kubectl get nodes
```

Result:

```text
photoshare-control-plane
```

Status:

```text
Ready
```

---

# Load Image Into Kind

```bash
kind load docker-image photoshare-api:v1 \
  --name photoshare
```

Learning:

```text
Kind does not automatically see
host Docker images.

Images must be loaded manually.
```

---

# Kubernetes Deployment

## Deployment

Created:

```yaml
kind: Deployment
```

Responsibilities:

```text
Manage Pods

Manage Replicas

Handle Rollouts
```

---

## Service

Created:

```yaml
kind: Service
```

Responsibilities:

```text
Stable Endpoint

Traffic Routing

Load Balancing
```

---

# Deploy Application

```bash
kubectl apply -f k8s/
```

---

# Validation

Verify:

```bash
kubectl get deployments
```

```bash
kubectl get pods
```

```bash
kubectl get svc
```

Result:

```text
Deployment Running

Pod Running

Service Available
```

---

# Port Forwarding

Used:

```bash
kubectl port-forward \
service/photoshare-api \
8080:8080
```

Successfully accessed:

```text
GET /feed

GET /posts

Other REST APIs
```

directly from Kubernetes.

---

# Feed API Validation

Successfully executed:

```http
GET /api/v1/feed
```

from Kubernetes pod.

Validated:

```text
✅ PostgreSQL

✅ Redis

✅ Feed Service

✅ OpenTelemetry

✅ API Deployment
```

---

# Rolling Deployment

Updated application image.

Performed rollout:

```bash
kubectl apply -f deployment.yaml
```

Observed:

```text
Old Pod Terminated

New Pod Created

Traffic Switched
```

---

# Issue Encountered

While port-forwarding during rollout:

```text
Connection Refused
```

Error:

```text
failed to connect to localhost:8080
inside namespace
```

Root Cause:

```text
Port Forward
    |
Attached To Old Pod
    |
Pod Replaced During Rollout
    |
Connection Lost
```

Resolution:

```bash
kubectl port-forward \
service/photoshare-api \
8080:8080
```

Restarted port-forward after deployment finished.

---

# Kubernetes Concepts Learned

## Pod

```text
Smallest Deployable Unit
```

Pods are:

```text
Ephemeral

Disposable

Replaceable
```

---

## Deployment

Responsible for:

```text
Replica Management

Rollouts

Rollbacks
```

---

## Service

Provides:

```text
Stable Network Endpoint
```

Learning:

```text
Applications should connect
through Services,
not Pods.
```

---

## Rolling Updates

Learned:

```text
Old Pod
    ↓
New Pod
    ↓
Traffic Switch
    ↓
Old Pod Removed
```

without downtime.

---

# Architecture

```text
                Kubernetes Cluster

                        |
                        v

                Photoshare API
                     (Pod)

                        |
        ----------------------------------

              PostgreSQL
              Redis
              Kafka
              MinIO

        ----------------------------------

              OpenTelemetry

                        |
                      Jaeger

                        |
                    Prometheus

                        |
                     Grafana
```

---

# Key Takeaways

### Containerization

Learned:

```text
Container Networking

Environment Variables

Infrastructure Configuration
```

---

### Kubernetes

Learned:

```text
Clusters

Pods

Deployments

Services

Port Forwarding

Rolling Updates
```

---

### Operational Learning

Most valuable lesson:

```text
A Pod is NOT a stable endpoint.

A Service is the stable endpoint.
```

---

# Success Criteria

Completed:

- ✅ Docker Image
- ✅ Container Startup
- ✅ PostgreSQL Connectivity
- ✅ Kubernetes Cluster
- ✅ Deployment
- ✅ Service
- ✅ Pod Running
- ✅ Feed API Access
- ✅ Rolling Deployment
- ✅ Port Forwarding
- ✅ OpenTelemetry Still Functional

---

# Phase Summary

Phase-09 introduced Kubernetes using Kind and successfully deployed the Photo Sharing API into a Kubernetes cluster.

The application evolved from running inside IntelliJ and standalone Docker containers to running as a managed Kubernetes workload using Deployments and Services.

This phase established the foundation required for future Kubernetes-native infrastructure, scaling, configuration management, and microservice deployments.