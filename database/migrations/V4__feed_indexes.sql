-- V4__feed_indexes.sql

CREATE INDEX idx_posts_user_created
    ON posts(user_id, created_at DESC);

CREATE INDEX idx_followers_follower
    ON followers(follower_id);

CREATE INDEX idx_followers_following
    ON followers(following_id);