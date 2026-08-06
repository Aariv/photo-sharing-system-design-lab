# Phase-05 Performance Engineering

## Goal

Generate realistic data and benchmark system performance.

## Dataset

- Users: 100+
- Posts: 1000+
- Followers: 3000+
- Likes: 10000+
- Comments: 5000+

## Load Test Tool

k6

## Feed Benchmark

Scenario:
- 10 Virtual Users
- 30 Seconds
- Warm Redis Cache

Results:
- Requests: 219,586
- Throughput: 7,319 req/sec
- Average Latency: 1.33 ms
- P95 Latency: 2.59 ms
- Error Rate: 0%

## Observations

Redis cache significantly reduced response times.

The feed endpoint sustained more than 7k requests per second with no errors.