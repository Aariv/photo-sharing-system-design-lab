package com.ariv.photoshare.comment.resource;

import com.ariv.photoshare.comment.dto.CommentResponse;
import com.ariv.photoshare.comment.dto.CommentsResponse;
import com.ariv.photoshare.comment.dto.CreateCommentRequest;
import com.ariv.photoshare.comment.service.CommentService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;

@Path("/api/v1/posts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CommentResource {

    @Inject
    CommentService commentService;

    @POST
    @Path("/{postId}/comments")
    public CommentResponse create(
            @PathParam("postId")
            UUID postId,

            CreateCommentRequest request) {

        return commentService.create(
                postId,
                request
        );
    }

    @GET
    @Path("/{postId}/comments")
    public CommentsResponse comments(
            @PathParam("postId")
            UUID postId) {

        return commentService
                .getComments(postId);
    }

    @GET
    @Path("/{postId}/comments/count")
    public long count(
            @PathParam("postId")
            UUID postId) {

        return commentService.count(postId);
    }
}