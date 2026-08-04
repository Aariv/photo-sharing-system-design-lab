# Phase-04 Observability

## Goal

Introduce observability into the platform to monitor
application behavior, cache effectiveness, event throughput,
and runtime health.

## Problem

The platform now contains:

- PostgreSQL
- Redis Cache
- Kafka / Redpanda
- Notifications

Without observability there is no visibility into:

- Request volume
- Cache utilization
- Event throughput
- System health

## Solution

Added:

- Micrometer
- Prometheus
- Grafana

## Architecture

                 Client
                    |
                    v
              Quarkus API
                    |
    ---------------------------------
    |              |               |
PostgreSQL      Redis         Redpanda
|
Notifications

                    |
                    v
               Micrometer
                    |
                    v
               Prometheus
                    |
                    v
                 Grafana

## Metrics

### Feed Metrics

- feed_requests_total
- feed_cache_hits_total
- feed_cache_misses_total

### Kafka Metrics

- events_published_total

### Notification Metrics

- notifications_created_total

## Dashboard Panels

### Feed

- Feed Requests
- Feed Cache Hits
- Feed Cache Misses
- Feed Cache Hit Ratio

### Events

- Events Published
- Notifications Created

### Runtime

- JVM Memory
- JVM Threads
- Process Uptime

## Learning Outcomes

- Observability
- Metrics Collection
- Prometheus Scraping
- Grafana Dashboards
- Cache Monitoring
- Event Monitoring