package com.ariv.photoshare.cache.resource;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/api/v1/cache")
public class CacheResource {

    @Inject
    RedisDataSource redis;

    @GET
    @Path("/test")
    public String test() {

        ValueCommands<String, String> values =
                redis.value(String.class);

        values.set("hello", "redis-working");

        return values.get("hello");
    }
}