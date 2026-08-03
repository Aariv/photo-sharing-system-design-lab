package com.ariv.photoshare.notifications.resource;

import com.ariv.photoshare.notifications.dto.MarkReadResponse;
import com.ariv.photoshare.notifications.dto.NotificationResponse;
import com.ariv.photoshare.notifications.service.NotificationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/notifications")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class NotificationResource {

    @Inject
    NotificationService service;

    @GET
    @Path("/user/{userId}")
    public List<NotificationResponse>
    notifications(
            @PathParam("userId")
            UUID userId) {

        return service.getNotifications(
                userId
        );
    }

    @PATCH
    @Path("/{id}/read")
    public MarkReadResponse markRead(
            @PathParam("id")
            UUID notificationId) {

        return service.markRead(
                notificationId
        );
    }
}