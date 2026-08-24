# Phase-15: Service Decomposition Strategy

## Overview

Up to this point, the Photo Sharing Platform has evolved into a sophisticated modular monolith.

The platform now includes:

- User Management
- Posts
- Followers
- Likes
- Comments
- Notifications
- Timeline Generation
- Feed Ranking
- Search
- Transactional Outbox
- Event-Driven Messaging
- CQRS Read Models

All of these capabilities currently run inside a single deployable application.

This phase focuses on understanding:

```text
When should a system stop being a monolith?