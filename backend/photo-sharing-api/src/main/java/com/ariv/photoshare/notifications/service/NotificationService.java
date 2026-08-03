package com.ariv.photoshare.notifications.service;

import com.ariv.photoshare.notifications.dto.MarkReadResponse;
import com.ariv.photoshare.notifications.dto.NotificationResponse;
import com.ariv.photoshare.notifications.entity.NotificationEntity;
import com.ariv.photoshare.notifications.repository.NotificationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class NotificationService {

    @Inject
    private NotificationRepository notificationRepository;

    public List<NotificationResponse> getNotifications(UUID userId) {

        return notificationRepository
                .findByUser(userId)
                .stream()
                .map(notification ->
                        new NotificationResponse(
                                notification.id,
                                notification.message,
                                notification.read,
                                notification.createdAt
                        ))
                .toList();
    }

    @Transactional
    public MarkReadResponse markRead(
            UUID notificationId) {

        NotificationEntity notification =
                notificationRepository.findById(
                        notificationId);

        if(notification == null) {
            throw new NotFoundException();
        }

        notification.read = true;

        return new MarkReadResponse(
                notification.id,
                true
        );
    }
}
