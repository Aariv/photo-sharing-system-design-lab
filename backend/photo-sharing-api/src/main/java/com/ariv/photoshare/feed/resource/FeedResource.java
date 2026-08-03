package com.ariv.photoshare.feed.resource;

import com.ariv.photoshare.feed.dto.FeedResponse;
import com.ariv.photoshare.feed.service.FeedService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;

@Path("/api/v1/feed")
@Produces(MediaType.APPLICATION_JSON)
public class FeedResource {

    @Inject
    FeedService feedService;

    @GET
    public FeedResponse getFeed(

            @QueryParam("userId")
            UUID userId,

            @QueryParam("page")
            @DefaultValue("0")
            int page,

            @QueryParam("size")
            @DefaultValue("20")
            int size) {

        return feedService.getFeed(
                userId,
                page,
                size
        );
    }
}