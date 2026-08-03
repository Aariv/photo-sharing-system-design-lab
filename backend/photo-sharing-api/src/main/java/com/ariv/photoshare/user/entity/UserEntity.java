package com.ariv.photoshare.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    public UUID id;

    public String username;

    public String email;

    @Column(name = "password_hash")
    public String passwordHash;

    @Column(name = "created_at")
    public Instant createdAt;
}