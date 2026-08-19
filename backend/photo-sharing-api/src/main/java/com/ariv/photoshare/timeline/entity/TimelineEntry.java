package com.ariv.photoshare.timeline.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "timeline")
public class TimelineEntry extends PanacheEntityBase {

    @EmbeddedId
    private TimelineId id;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public TimelineEntry(TimelineId timelineId, UUID authorId, Instant createdAt) {
        this.id = timelineId;
        this.authorId = authorId;
        this.createdAt = createdAt;
    }

    public TimelineId getId() {
        return id;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}