package com.ariv.photoshare.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class FeedMetrics {

    @Inject
    MeterRegistry meterRegistry;

    private Counter feedRequests;

    private Counter cacheHits;

    private Counter cacheMisses;

    @PostConstruct
    void init() {

        feedRequests =
                meterRegistry.counter(
                        "feed.requests");

        cacheHits =
                meterRegistry.counter(
                        "feed.cache.hits");

        cacheMisses =
                meterRegistry.counter(
                        "feed.cache.misses");
    }

    public void request() {
        feedRequests.increment();
    }

    public void hit() {
        cacheHits.increment();
    }

    public void miss() {
        cacheMisses.increment();
    }
}