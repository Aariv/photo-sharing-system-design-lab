package com.ariv.photoshare.feed.repository;

import com.ariv.photoshare.feed.dto.HomeFeedRow;
import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class HomeFeedReadRepository {

    @Inject
    @DataSource("feed-replica")
    AgroalDataSource replicaDataSource;

    public List<HomeFeedRow> findFeed(
            UUID userId,
            int limit) {

        String sql = """
            SELECT
                p.id AS post_id,
                p.user_id AS author_id,
                p.caption,
                p.image_url,
                p.created_at,
                COALESCE(l.like_count, 0) AS like_count,
                COALESCE(c.comment_count, 0) AS comment_count
            FROM timeline t
            JOIN posts p
              ON p.id = t.post_id
            LEFT JOIN (
                SELECT post_id, COUNT(*) AS like_count
                FROM likes
                GROUP BY post_id
            ) l
              ON l.post_id = p.id
            LEFT JOIN (
                SELECT post_id, COUNT(*) AS comment_count
                FROM comments
                GROUP BY post_id
            ) c
              ON c.post_id = p.id
            WHERE t.user_id = ?
            ORDER BY t.created_at DESC
            LIMIT ?
            """;

        List<HomeFeedRow> feed = new ArrayList<>();

        try (Connection connection =
                     replicaDataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            connection.setReadOnly(true);

            statement.setObject(1, userId);
            statement.setInt(2, limit);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    feed.add(map(resultSet));
                }
            }

            return feed;

        } catch (SQLException exception) {
            throw new FeedReadException(
                    "Unable to read home feed from replica",
                    exception
            );
        }
    }

    private HomeFeedRow map(
            ResultSet resultSet)
            throws SQLException {

        return new HomeFeedRow(
                resultSet.getObject(
                        "post_id",
                        UUID.class
                ),
                resultSet.getObject(
                        "author_id",
                        UUID.class
                ),
                resultSet.getString(
                        "caption"
                ),
                resultSet.getString(
                        "image_url"
                ),
                resultSet.getTimestamp(
                        "created_at"
                ).toInstant(),
                resultSet.getLong(
                        "like_count"
                ),
                resultSet.getLong(
                        "comment_count"
                )
        );
    }
}