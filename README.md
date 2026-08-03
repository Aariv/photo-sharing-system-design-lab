# Photo Sharing System Design Lab

A hands-on system design project built with Quarkus,
PostgreSQL, Docker, and Flyway.

The goal is to evolve a simple social-media application
from a monolith to a large-scale distributed system.

## Current Phase

✅ Phase-01 Complete

Features:

- User Signup
- User Profile
- Create Post
- User Timeline
- Follow / Unfollow
- Feed
- Likes
- Comments

## Tech Stack

- Java 21
- Quarkus
- PostgreSQL
- Flyway
- Docker

## Architecture

Client
|
Quarkus API
|
PostgreSQL

## Run Locally

docker compose up -d

./mvnw quarkus:dev

## APIs

Swagger:

http://localhost:8080/q/swagger-ui

## Roadmap

✅ Phase-02 Redis Cache
- Phase-03 Kafka
- Phase-04 Notifications
- Phase-05 Kubernetes