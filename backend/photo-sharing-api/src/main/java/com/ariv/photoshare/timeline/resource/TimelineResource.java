package com.ariv.photoshare.timeline.resource;

import com.ariv.photoshare.timeline.dto.TimelineFeedResponse;
import com.ariv.photoshare.timeline.dto.TimelineResponse;
import com.ariv.photoshare.timeline.service.TimelineService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/timeline")
@Produces(MediaType.APPLICATION_JSON)
public class TimelineResource {

    @Inject
    TimelineService timelineService;

    @GET
    @Path("/{userId}")
    public List<TimelineFeedResponse> getTimeline(
            @PathParam("userId") UUID userId) {
        return timelineService.getFeed(userId);
    }
}
