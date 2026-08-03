package com.ariv.photoshare.notifications.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class NotificationEntity
        extends PanacheEntityBase {

    @Id
    public UUID id;

    @Column(name = "user_id")
    public UUID userId;

    public String message;

    @Column(name = "is_read")
    public boolean read;

    @Column(name = "created_at")
    public Instant createdAt;
}