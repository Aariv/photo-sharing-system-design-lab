package com.ariv.photoshare.comment.repository;

import com.ariv.photoshare.comment.entity.CommentEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CommentRepository
        implements PanacheRepositoryBase<CommentEntity, UUID> {

    // Equivalent to: SELECT * FROM comments WHERE post_id = :postId ORDER BY created_at DESC
    public List<CommentEntity> findByPost(
            UUID postId) {

        return find(
                "postId =?1 order by createdAt desc",
                postId
        ).list();
    }

    // Equivalent to: SELECT COUNT(*) FROM comments WHERE post_id = :postId
    public long countComments(
            UUID postId) {

        return count(
                "postId",
                postId
        );
    }
}