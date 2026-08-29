package com.ariv.photoshare.like.repository;

import com.ariv.photoshare.like.entity.LikeEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class LikeRepository
        implements PanacheRepositoryBase<LikeEntity, UUID> {

    /**
     * Returns true only when this call created the like.
     * Returns false when the user had already liked the post.
     */
    public boolean insertIfAbsent(
            UUID likeId,
            UUID userId,
            UUID postId) {

        int inserted = getEntityManager()
                .createNativeQuery("""
                    INSERT INTO likes (
                        id,
                        user_id,
                        post_id,
                        created_at
                    )
                    VALUES (
                        :id,
                        :userId,
                        :postId,
                        now()
                    )
                    ON CONFLICT (post_id, user_id)
                    DO NOTHING
                    """)
                .setParameter("id", likeId)
                .setParameter("userId", userId)
                .setParameter("postId", postId)
                .executeUpdate();

        return inserted == 1;
    }

    /**
     * Idempotent deletion.
     *
     * Returns true when a like was deleted.
     * Returns false when the like did not exist.
     */
    public boolean deleteIfPresent(
            UUID userId,
            UUID postId) {

        int deleted = getEntityManager()
                .createNativeQuery("""
                    DELETE FROM likes
                    WHERE user_id = :userId
                      AND post_id = :postId
                    """)
                .setParameter("userId", userId)
                .setParameter("postId", postId)
                .executeUpdate();

        return deleted == 1;
    }

    public LikeEntity findLike(
            UUID userId,
            UUID postId) {

        return find(
                "userId = ?1 and postId = ?2",
                userId,
                postId
        ).firstResult();
    }

    public boolean likedByUser(
            UUID userId,
            UUID postId) {

        return count(
                "userId = ?1 and postId = ?2",
                userId,
                postId
        ) > 0;
    }

    public long countLikes(UUID postId) {

        return count(
                "postId",
                postId
        );
    }

    public Map<UUID, Long> countByPostIds(
            List<UUID> postIds) {

        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }

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
                                postIds
                        )
                        .getResultList();

        return rows.stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]
                ));
    }
}