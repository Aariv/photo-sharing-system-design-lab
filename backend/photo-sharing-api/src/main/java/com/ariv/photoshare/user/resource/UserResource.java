package com.ariv.photoshare.user.resource;

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
}