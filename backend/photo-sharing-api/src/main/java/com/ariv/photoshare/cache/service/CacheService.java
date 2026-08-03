package com.ariv.photoshare.cache.service;

import com.ariv.photoshare.cache.CacheKeys;
import io.quarkus.redis.datasource.RedisDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class CacheService {

    @Inject
    RedisDataSource redis;

    public void evictFeed(UUID userId) {

        String key =
                CacheKeys.feed(userId);

        redis.key().del(key);

        System.out.println(
                "CACHE EVICT -> " + key);
    }
}
