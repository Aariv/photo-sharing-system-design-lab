package com.ariv.photoshare.timeline.resource;

import com.ariv.photoshare.feed.dto.FeedResponse;
import com.ariv.photoshare.timeline.service.TimelineService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;

@Path("/api/v1/users")
@Produces(MediaType.APPLICATION_JSON)
public class TimelineResource {

    @Inject
    TimelineService timelineService;

    @GET
    @Path("/{userId}/posts")
    public FeedResponse timeline(

            @PathParam("userId")
            UUID userId,

            @QueryParam("page")
            @DefaultValue("0")
            int page,

            @QueryParam("size")
            @DefaultValue("20")
            int size) {

        return timelineService.getTimeline(
                userId,
                page,
                size
        );
    }
}