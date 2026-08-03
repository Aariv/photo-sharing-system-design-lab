package com.ariv.photoshare.comment.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "comments")
public class CommentEntity extends PanacheEntityBase {

    @Id
    public UUID id;

    @Column(name = "user_id")
    public UUID userId;

    @Column(name = "post_id")
    public UUID postId;

    public String comment;

    @Column(name = "created_at")
    public Instant createdAt;
}