package com.ariv.photoshare.follow.repository;

import com.ariv.photoshare.follow.entity.FollowEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class FollowRepository
        implements PanacheRepositoryBase<FollowEntity, UUID> {

    public boolean exists(UUID followerId,
                          UUID followingId) {

        return count(
                "followerId = ?1 and followingId = ?2",
                followerId,
                followingId
        ) > 0;
    }

    public FollowEntity findFollow(
            UUID followerId,
            UUID followingId) {

        return find(
                "followerId = ?1 and followingId = ?2",
                followerId,
                followingId
        ).firstResult();
    }

    public long countFollowers(
            UUID userId) {

        return count(
                "followingId",
                userId
        );
    }

    public long countFollowing(
            UUID userId) {

        return count(
                "followerId",
                userId
        );
    }

    public List<FollowEntity> findFollowersOf(UUID userId) {

        return find(
                "followingId = ?1",
                userId
        ).list();
    }

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