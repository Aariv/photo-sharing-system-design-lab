package com.ariv.photoshare.admin.resource;

import com.ariv.photoshare.admin.dto.SeedRequest;
import com.ariv.photoshare.admin.service.SeedDataService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/admin")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AdminResource {

    @Inject
    SeedDataService seedDataService;

    @POST
    @Path("/seed")
    public String seed(
            SeedRequest request) {

        seedDataService.seed(request);

        return "Seed completed successfully";
    }
}