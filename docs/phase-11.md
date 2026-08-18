# Phase-11: Feed Generation at Scale

## Overview

Until now, the Photo Sharing Platform has focused on functional correctness, caching, event-driven communication, observability, and Kubernetes deployment.

The next challenge is scalability.

The current feed implementation works well for a small number of users but may become expensive as the number of users, posts, and follower relationships grows.

This phase explores how large social platforms such as Instagram, Facebook, LinkedIn, X (Twitter), and Threads design and scale feed generation systems.

---

# Current Feed Architecture

## Current Query

The current feed is generated dynamically during a user request.

```sql
SELECT p.*
FROM posts p
JOIN followers f
  ON p.user_id = f.following_id
WHERE f.follower_id = :userId
ORDER BY p.created_at DESC
LIMIT 50;
```

## Architecture

```text
User
  |
  v
Feed API
  |
  v
PostgreSQL
  |
  v
Posts + Followers
  |
  v
Sort + Return Feed
```

---

# Advantages of the Current Design

```text
No duplicate feed data
Simple implementation
Easy maintenance
Minimal storage overhead
Realtime feed generation
```

---

# Fan-Out on Read

## Definition

Feed entries are generated when the user reads the feed.

Work happens during:

```text
READ OPERATIONS
```

## Request Flow

```text
User Requests Feed
        |
        v
Get Following List
        |
        v
Fetch Posts
        |
        v
Merge Results
        |
        v
Sort Results
        |
        v
Return Feed
```

---

# Why Fan-Out on Read Works Initially

Assume:

```text
Users: 1,000
Posts: 50,000
Average Following Count: 100
```

At this scale there is no need for a more complicated design.

---

# Scaling Challenges

As the platform grows, feed generation becomes increasingly expensive.

Example:

```text
Users: 10,000,000
Posts Per Day: 50,000,000
Average Following Count: 500
```

Every feed request now requires:

```text
Finding hundreds of followed users
Fetching their posts
Merging thousands of records
Sorting them
Returning a small subset
```

---

# The Core Problem

PostgreSQL repeatedly performs:

```text
Large Joins
Large Sorts
Large Reads
```

for every feed request.

---

# Fan-Out on Write

## Definition

Work is performed during post creation.

When a user publishes a post, the system immediately distributes that post to follower timelines.

## Request Flow

```text
Create Post
      |
      v
Find Followers
      |
      v
Generate Timeline Entries
      |
      v
Store Timeline Data
```

When readers request their feed:

```text
Read Precomputed Timeline
```

---

# Timeline Table

```sql
CREATE TABLE timeline (
    user_id UUID NOT NULL,
    post_id UUID NOT NULL,
    author_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (user_id, post_id)
);
```

Purpose:

```text
Store precomputed feed entries.
```

---

# Feed Retrieval with Timeline Table

```sql
SELECT *
FROM timeline
WHERE user_id = ?
ORDER BY created_at DESC
LIMIT 50;
```

Benefits:

```text
No joins
No large merge operations
Fast reads
Predictable latency
```

---

# Benefits of Fan-Out on Write

```text
Extremely fast feed retrieval
Simple read path
Predictable feed latency
Reduced database load during reads
```

---

# Drawbacks of Fan-Out on Write

Example:

```text
User posts once
Followers = 10,000
```

The system performs:

```text
10,000 timeline insertions
```

for a single post.

---

# The Celebrity Problem

Example:

```text
Celebrity: 100 Million Followers
```

One post may require:

```text
100 Million timeline insertions
```

This is not practical.

---

# Hybrid Feed Architecture

## Normal Users

Use:

```text
Fan-Out on Write
```

## Celebrity Accounts

Use:

```text
Fan-Out on Read
```

## Hybrid Design

```text
Normal User Post
        |
        v
Fan-Out on Write

Celebrity Post
        |
        v
Fan-Out on Read
```

This balances:

```text
Write Cost
Read Cost
Storage Cost
Scalability
```

---

# Role of Kafka

Current usage:

```text
PostLikedEvent
```

Future usage:

```text
PostCreatedEvent
```

Architecture:

```text
Post Created
      |
      v
Kafka
      |
      v
Timeline Consumer
      |
      v
Timeline Table
```

Kafka enables asynchronous feed generation and removes heavy work from the request path.

---

# Architecture Evolution

## Current

```text
Feed API
      |
      v
Posts Table
+
Followers Table
      |
      v
Dynamic Feed Generation
```

## Future

```text
Post Service
      |
      v
PostCreatedEvent
      |
      v
Timeline Service
      |
      v
Timeline Table
      |
      v
Fast Feed Retrieval
```

---

# Key Learnings

## Fan-Out on Read

```text
Writes are cheap
Reads are expensive
```

## Fan-Out on Write

```text
Writes are expensive
Reads are cheap
```

## Celebrity Problem

```text
A single feed strategy does not work for all users.
```

## Hybrid Feed Architecture

```text
Use the right strategy for the right user profile.
```

---

# Next Steps

```text
Step-2 Timeline Service Design
Step-3 PostCreatedEvent
Step-4 Timeline Generation Flow
Step-5 Hybrid Feed Architecture
Step-6 Feed Service Extraction
Step-7 Instagram-Style Feed Architecture
```
