package com.ariv.photoshare.follow.resource;

import com.ariv.photoshare.follow.dto.FollowRequest;
import com.ariv.photoshare.follow.dto.FollowResponse;
import com.ariv.photoshare.follow.service.FollowService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/api/v1/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FollowResource {

    @Inject
    FollowService followService;

    @POST
    @Path("/{userId}/follow")
    public FollowResponse follow(
            @PathParam("userId")
            UUID userId,

            FollowRequest request) {

        return followService.follow(
                request.followerId(),
                userId
        );
    }

    @DELETE
    @Path("/{userId}/follow")
    public Response unfollow(
            @PathParam("userId")
            UUID userId,

            @QueryParam("followerId")
            UUID followerId) {

        followService.unfollow(
                followerId,
                userId);

        return Response.noContent().build();
    }
}