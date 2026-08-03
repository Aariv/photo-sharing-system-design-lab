package com.ariv.photoshare.post.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "posts")
public class PostEntity {

    @Id
    public UUID id;

    @Column(name = "user_id")
    public UUID userId;

    @Column(name = "image_url")
    public String imageUrl;

    public String caption;

    @Column(name = "created_at")
    public Instant createdAt;
}