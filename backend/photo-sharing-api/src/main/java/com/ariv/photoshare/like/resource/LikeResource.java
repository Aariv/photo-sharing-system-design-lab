package com.ariv.photoshare.like.resource;

import com.ariv.photoshare.like.dto.LikeCommandResponse;
import com.ariv.photoshare.like.service.LikeService;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/api/v1/posts")
@Produces(MediaType.APPLICATION_JSON)
public class LikeResource {

    @Inject
    LikeService likeService;

    @PUT
    @Path("/{postId}/likes/{userId}")
    @WithSpan("like-post")
    public Response like(
            @PathParam("postId") UUID postId,
            @PathParam("userId") UUID userId) {

        LikeCommandResponse result =
                likeService.like(userId, postId);

        if (result.created()) {
            return Response.status(
                            Response.Status.CREATED
                    )
                    .entity(result)
                    .build();
        }

        return Response.ok(result).build();
    }

    @DELETE
    @Path("/{postId}/likes/{userId}")
    @WithSpan("unlike-post")
    public Response unlike(
            @PathParam("postId") UUID postId,
            @PathParam("userId") UUID userId) {

        likeService.unlike(userId, postId);

        return Response.noContent().build();
    }

    @GET
    @Path("/{postId}/likes/count")
    public LikeCountResponse count(
            @PathParam("postId") UUID postId) {

        return new LikeCountResponse(
                postId,
                likeService.countLikes(postId)
        );
    }

    public record LikeCountResponse(
            UUID postId,
            long likeCount
    ) {
    }
}
