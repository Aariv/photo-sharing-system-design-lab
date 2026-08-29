package com.ariv.photoshare.like.repository;

import com.ariv.photoshare.like.entity.LikeEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class LikeRepositoryV1
        implements PanacheRepositoryBase<LikeEntity, UUID> {

    // Equivalent to: SELECT * FROM likes WHERE user_id = :userId AND post_id = :postId
    public LikeEntity findLike(
            UUID userId,
            UUID postId) {

        return find(
                "userId = ?1 and postId = ?2",
                userId,
                postId)
                .firstResult();
    }

    // Equivalent to: SELECT COUNT(*) FROM likes WHERE post_id = :postId
    public long countLikes(UUID postId) {

        return count(
                "postId",
                postId
        );
    }

    // Equivalent to: SELECT COUNT(*) FROM likes WHERE user_id = :userId AND post_id = :postId
    public boolean exists(
            UUID userId,
            UUID postId) {

        return count(
                "userId = ?1 and postId = ?2",
                userId,
                postId
        ) > 0;
    }

    // Equivalent to: SELECT COUNT(*) FROM likes WHERE user_id = :userId AND post_id = :postId
    public boolean likedByUser(
        UUID userId,
        UUID postId)
    {
                return count(
                        "userId = ?1 and postId = ?2",
                        userId,
                        postId
                ) > 0;
    }

    // Equivalent to: SELECT COUNT(*) FROM likes WHERE post_id = :postId
    public long countByPostId(UUID postId) {

        return count(
                "postId",
                postId
        );
    }

    public Map<UUID, Long> countByPostIds(
            List<UUID> postIds) {

        List<Object[]> rows =
                getEntityManager()
                        .createQuery("""
                        select l.postId,
                               count(l)
                        from LikeEntity l
                        where l.postId in :postIds
                        group by l.postId
                    """, Object[].class)
                        .setParameter(
                                "postIds",
                                postIds)
                        .getResultList();

        return rows.stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]
                ));
    }
}