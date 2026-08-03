# Phase-03 Event Driven Architecture With Kafka

## Problem

After a user likes a post, additional workflows needed to happen:

- Notification Creation
- Analytics
- Activity Tracking

Doing this synchronously would tightly couple services.

## Solution

Introduced Kafka (Redpanda) and asynchronous events.

Like API publishes:

PostLikedEvent

Consumer handles:

Notification Creation

## Architecture

Like API
|
v
PostLikedEvent
|
v
Kafka / Redpanda
|
v
Notification Consumer
|
v
PostgreSQL
``

## Events

PostLikedEvent

``
{
"postId": "...",
"userId": "...",
"createdAt": "..."
}
``

## Features

✅ Event Producer

✅ Event Consumer

✅ Notification Creation

✅ Asynchronous Workflow
`