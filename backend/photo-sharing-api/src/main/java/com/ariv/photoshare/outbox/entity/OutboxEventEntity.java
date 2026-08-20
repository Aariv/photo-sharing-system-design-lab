package com.ariv.photoshare.outbox.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity {

    @Id
    public UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    public String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    public UUID aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    public String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    public String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    public OutboxStatus status;

    @Column(name = "attempts", nullable = false)
    public int attempts;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "processed_at")
    public Instant processedAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    public String lastError;

    @Column(name = "next_attempt_at")
    public Instant nextAttemptAt;
}