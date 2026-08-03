# Phase-02 Redis Feed Cache

## Problem

Every feed request was hitting PostgreSQL.

Feed API also executed additional queries for:

- Likes Count
- Comments Count
- likedByMe

This resulted in multiple database queries per request.

## Solution

Implemented Redis Cache using Cache Aside Pattern.

Feed Flow:

Client
|
Feed API
|
Redis
|
Cache Hit -> Return Response

or

Cache Miss
|
PostgreSQL
|
Populate Cache
|
Return Response

## Cache Key Design

feed:user:{userId}

Example:

feed:user:1bf931f4-ae75-46b0-be11-3d510b4cf9ab

## Features

✅ Redis Integration

✅ Feed Caching

✅ Cache Hit

✅ Cache Miss

✅ TTL

✅ Cache Eviction