package com.ariv.photoshare.notifications.repository;

import com.ariv.photoshare.notifications.entity.NotificationEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class NotificationRepository
        implements PanacheRepositoryBase<
                NotificationEntity,
                UUID> {

        public List<NotificationEntity> findByUser(UUID userId) {

            return find(
                    "userId =?1 order by createdAt desc",
                    userId
            ).list();
        }
}