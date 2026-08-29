package com.ariv.photoshare.feed.resource;

import com.ariv.photoshare.feed.dto.FeedResponse;
import com.ariv.photoshare.feed.dto.HomeFeedRow;
import com.ariv.photoshare.feed.service.FeedService;

import com.ariv.photoshare.feed.service.HomeFeedService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/feed")
@Produces(MediaType.APPLICATION_JSON)
public class FeedResource {

    @Inject
    FeedService feedService;

    @Inject
    HomeFeedService homeFeedService;

    @GET
    @Path("/v1")
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

    @GET
    public List<HomeFeedRow> getHomeFeed(
            @QueryParam("userId")
            UUID userId) {
        return homeFeedService.getHomeFeed(userId);
    }
}