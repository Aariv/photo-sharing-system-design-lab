package com.ariv.photoshare.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class KafkaMetrics {

    @Inject
    MeterRegistry registry;

    private Counter eventsPublished;

    private Counter notificationsCreated;

    @PostConstruct
    void init() {

        eventsPublished =
                registry.counter(
                        "events.published");

        notificationsCreated =
                registry.counter(
                        "notifications.created");
    }

    public void eventPublished() {
        eventsPublished.increment();
    }

    public void notificationCreated() {
        notificationsCreated.increment();
    }
}