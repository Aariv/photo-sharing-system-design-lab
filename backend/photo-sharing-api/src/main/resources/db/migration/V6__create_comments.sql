-- V6__create_comments.sql

CREATE TABLE comments
(
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL,

    post_id UUID NOT NULL,

    comment TEXT NOT NULL,

    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_comments_user
        FOREIGN KEY(user_id)
            REFERENCES users(id),

    CONSTRAINT fk_comments_post
        FOREIGN KEY(post_id)
            REFERENCES posts(id)
);

CREATE INDEX idx_comments_post
    ON comments(post_id);

CREATE INDEX idx_comments_user
    ON comments(user_id);