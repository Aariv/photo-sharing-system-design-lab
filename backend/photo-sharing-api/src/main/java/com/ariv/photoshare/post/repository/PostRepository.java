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

    public long countPosts(UUID userId) {

        return count(
                "userId",
                userId
        );
    }

    public List<PostEntity> findByIds(List<?> postIds) {

        return list(
                "id in ?1",
                postIds
        );
    }

    public List<PostEntity> findPostsByAuthors(
            List<UUID> authorIds) {

        return list(
                "userId in ?1 order by createdAt desc",
                authorIds
        );
    }

}
