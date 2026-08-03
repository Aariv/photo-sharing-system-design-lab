package com.ariv.photoshare.post.resource;

import com.ariv.photoshare.post.dto.CreatePostRequest;
import com.ariv.photoshare.post.dto.CreatePostResponse;
import com.ariv.photoshare.post.dto.PostResponse;
import com.ariv.photoshare.post.service.PostService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;

@Path("/api/v1/posts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PostResource {

    @Inject
    PostService postService;

    @POST
    public CreatePostResponse create(
            CreatePostRequest request) {
        return postService.create(request);
    }

    @GET
    @Path("/{id}")
    public PostResponse get(
            @PathParam("id") UUID id) {

        return postService.get(id);
    }
}