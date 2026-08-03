package com.ariv.photoshare.like.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "likes")
public class LikeEntity extends PanacheEntityBase {

    @Id
    public UUID id;

    @Column(name = "user_id")
    public UUID userId;

    @Column(name = "post_id")
    public UUID postId;

    @Column(name = "created_at")
    public Instant createdAt;
}