package com.ariv.photoshare.events;

import com.ariv.photoshare.metrics.KafkaMetrics;
import com.ariv.photoshare.notifications.entity.NotificationEntity;
import com.ariv.photoshare.notifications.repository.NotificationRepository;
import com.ariv.photoshare.post.entity.PostEntity;
import com.ariv.photoshare.post.repository.PostRepository;
import com.ariv.photoshare.user.entity.UserEntity;
import com.ariv.photoshare.user.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class NotificationConsumer {

    @Inject
    NotificationRepository repository;

    @Inject
    PostRepository postRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    KafkaMetrics metrics;

    @Transactional
    @Incoming("post-liked-in")
    public void consume(
            PostLikedEvent event) {

        PostEntity post =
                postRepository.findById(
                        event.postId());

        UserEntity user =
                userRepository.findById(event.userId());

        NotificationEntity notification =
                new NotificationEntity();

        notification.id =
                UUID.randomUUID();

        notification.userId = post.userId;

//        notification.message =
//                event.userId()
//                + " liked your post";

        notification.message =
                user.username + " liked your post";

        notification.read =
                false;

        notification.createdAt =
                Instant.now();

        repository.persist(
                notification);

        metrics.notificationCreated();
        System.out.println(
                "NOTIFICATION CREATED -> "
                + notification.message);
    }
}