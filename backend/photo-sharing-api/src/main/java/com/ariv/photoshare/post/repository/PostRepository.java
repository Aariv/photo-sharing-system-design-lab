package com.ariv.photoshare.post.repository;

import com.ariv.photoshare.post.entity.PostEntity;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PostRepository
        implements PanacheRepositoryBase<PostEntity, UUID> {

    // Equivalent to:
    // SELECT * FROM posts p JOIN followers f ON p.user_id = f.following_id
    // WHERE f.follower_id = :userId ORDER BY p.created_at DESC
    @WithSpan("feed-query")
    public List<PostEntity> findFeed(
            UUID userId,
            int page,
            int size) {

        return getEntityManager()
                .createQuery("""
                SELECT p
                FROM PostEntity p
                JOIN FollowEntity f
                    ON p.userId = f.followingId
                WHERE f.followerId = :userId
                ORDER BY p.createdAt DESC
            """, PostEntity.class)
                .setParameter("userId", userId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }
//
//    public List<PostEntity> findTimeline(
//            UUID userId,
//            int page,
//            int size) {
//
//        return find(
//                "userId order by createdAt desc",
//                userId
//        )
//                .page(page, size)
//                .list();
//    }

    // Equivalent to: SELECT * FROM posts WHERE user_id = :userId ORDER BY created_at DESC
    public List<PostEntity> findTimeline(
            UUID userId,
            int page,
            int size) {

        return find(
                "userId = ?1 order by createdAt desc",
                userId
        )
                .page(Page.of(page, size))
                .list();
    }

    // Equivalent to: SELECT COUNT(*) FROM posts WHERE user_id = :userId
    public long countPosts(UUID userId) {

        return count(
                "userId",
                userId
        );
    }

    // Equivalent to: SELECT * FROM posts WHERE id IN :postIds
    public List<PostEntity> findByIds(List<?> postIds) {

        return list(
                "id in ?1",
                postIds
        );
    }

    // Equivalent to: SELECT * FROM posts WHERE user_id IN :authorIds ORDER BY created_at DESC
    public List<PostEntity> findPostsByAuthors(
            List<UUID> authorIds) {

        return list(
                "userId in ?1 order by createdAt desc",
                authorIds
        );
    }

    // Equivalent to: SELECT * FROM posts WHERE lower(caption) LIKE lower(:query) ORDER BY created_at DESC LIMIT :limit
    public List<PostEntity> searchByCaption(
            String query,
            int limit) {

        return find(
                """
                lower(caption)
                like lower(?1)
                order by createdAt desc
                """,
                "%" + query + "%"
        )
                .page(0, limit)
                .list();
    }

}
