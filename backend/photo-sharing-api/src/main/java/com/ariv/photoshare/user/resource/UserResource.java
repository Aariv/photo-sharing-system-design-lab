package com.ariv.photoshare.user.resource;

import com.ariv.photoshare.feed.dto.FeedResponse;
import com.ariv.photoshare.timeline.service.TimelineService;
import com.ariv.photoshare.user.dto.ProfileResponse;
import com.ariv.photoshare.user.dto.SignupRequest;
import com.ariv.photoshare.user.dto.SignupResponse;
import com.ariv.photoshare.user.service.UserService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;

@Path("/api/v1/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserService userService;

    @Inject
    TimelineService timelineService;


    @POST
    @Path("/signup")
    public SignupResponse signup(SignupRequest request) {
        return userService.signup(request);
    }

    @GET
    @Path("/{userId}/profile")
    public ProfileResponse profile(
            @PathParam("userId")
            UUID userId) {

        return userService.profile(
                userId
        );
    }

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