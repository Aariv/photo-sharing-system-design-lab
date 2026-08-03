package com.ariv.photoshare.follow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "followers")
public class FollowEntity {

    @Id
    public UUID id;

    @Column(name = "follower_id", nullable = false)
    public UUID followerId;

    @Column(name = "following_id", nullable = false)
    public UUID followingId;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
}