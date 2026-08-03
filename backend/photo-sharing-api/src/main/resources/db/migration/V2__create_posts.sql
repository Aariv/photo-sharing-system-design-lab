CREATE TABLE posts
(
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL,

    image_url TEXT NOT NULL,

    caption TEXT,

    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_posts_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
);