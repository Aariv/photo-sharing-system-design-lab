package com.ariv.photoshare.follow.repository;

import com.ariv.photoshare.follow.entity.FollowEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class FollowRepository
        implements PanacheRepositoryBase<FollowEntity, UUID> {

    // Equivalent to: SELECT COUNT(*) FROM followers WHERE follower_id = :followerId AND following_id = :followingId
    public boolean exists(UUID followerId,
                          UUID followingId) {

        return count(
                "followerId = ?1 and followingId = ?2",
                followerId,
                followingId
        ) > 0;
    }

    // Equivalent to: SELECT * FROM followers WHERE follower_id = :followerId AND following_id = :followingId
    public FollowEntity findFollow(
            UUID followerId,
            UUID followingId) {

        return find(
                "followerId = ?1 and followingId = ?2",
                followerId,
                followingId
        ).firstResult();
    }

    // Equivalent to: SELECT COUNT(*) FROM followers WHERE following_id = :userId
    public long countFollowers(
            UUID userId) {

        return count(
                "followingId",
                userId
        );
    }

    // Equivalent to: SELECT COUNT(*) FROM followers WHERE follower_id = :userId
    public long countFollowing(
            UUID userId) {

        return count(
                "followerId",
                userId
        );
    }

    // Equivalent to: SELECT * FROM followers WHERE following_id = :userId
    public List<FollowEntity> findFollowersOf(UUID userId) {

        return find(
                "followingId = ?1",
                userId
        ).list();
    }

    // Equivalent to: SELECT * FROM followers WHERE follower_id = :userId
    public List<UUID> findFollowingIds(UUID userId) {

        return find(
                "followerId = ?1",
                userId
        )
                .stream()
                .map(f -> f.followingId)
                .toList();
    }

}