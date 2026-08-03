CREATE TABLE likes
(
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL,

    post_id UUID NOT NULL,

    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_likes_user
        FOREIGN KEY (user_id)
            REFERENCES users(id),

    CONSTRAINT fk_likes_post
        FOREIGN KEY (post_id)
            REFERENCES posts(id),

    CONSTRAINT unique_like
        UNIQUE(user_id, post_id)
);

CREATE INDEX idx_likes_post
    ON likes(post_id);

CREATE INDEX idx_likes_user
    ON likes(user_id);