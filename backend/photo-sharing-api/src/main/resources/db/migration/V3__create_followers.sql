CREATE TABLE followers
(
    id UUID PRIMARY KEY,

    follower_id UUID NOT NULL,

    following_id UUID NOT NULL,

    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_followers_follower
        FOREIGN KEY (follower_id)
            REFERENCES users(id),

    CONSTRAINT fk_followers_following
        FOREIGN KEY (following_id)
            REFERENCES users(id),

    CONSTRAINT unique_follow
        UNIQUE(follower_id, following_id)
);