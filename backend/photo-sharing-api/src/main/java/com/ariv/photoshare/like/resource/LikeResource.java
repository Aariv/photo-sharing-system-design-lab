package com.ariv.photoshare.like.resource;

import com.ariv.photoshare.like.dto.LikeRequest;
import com.ariv.photoshare.like.dto.LikeResponse;
import com.ariv.photoshare.like.service.LikeService;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/api/v1/posts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LikeResource {

    @Inject
    LikeService likeService;

    @POST
    @Path("/{postId}/like")
    @WithSpan("like-post")
    public LikeResponse like(
            @PathParam("postId")
            UUID postId,

            LikeRequest request) {

        return likeService.like(
                request.userId(),
                postId);
    }

    @DELETE
    @Path("/{postId}/like")
    public Response unlike(
            @PathParam("postId")
            UUID postId,

            @QueryParam("userId")
            UUID userId) {

        likeService.unlike(
                userId,
                postId);

        return Response.noContent().build();
    }

    @GET
    @Path("/{postId}/likes/count")
    public long count(
            @PathParam("postId")
            UUID postId) {

        return likeService.countLikes(postId);
    }
}