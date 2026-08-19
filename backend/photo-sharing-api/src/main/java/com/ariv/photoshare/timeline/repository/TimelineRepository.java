package com.ariv.photoshare.timeline.repository;

import com.ariv.photoshare.timeline.entity.TimelineEntry;
import com.ariv.photoshare.timeline.entity.TimelineId;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TimelineRepository
        implements PanacheRepositoryBase<TimelineEntry, TimelineId> {

        public List<TimelineEntry> findByUserId(UUID userId) {
            return find(
                    "id.userId = ?1 order by createdAt desc",
                    userId)
                    .list();
        }

}