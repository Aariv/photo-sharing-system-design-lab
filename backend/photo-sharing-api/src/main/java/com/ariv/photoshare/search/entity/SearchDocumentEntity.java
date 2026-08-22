package com.ariv.photoshare.search.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "search_documents")
public class SearchDocumentEntity {

    @Id
    @Column(name = "post_id")
    public UUID postId;

    @Column(name = "author_id", nullable = false)
    public UUID authorId;

    @Column(name = "caption", nullable = false)
    public String caption;

    @Column(name = "search_text", nullable = false)
    public String searchText;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;
}