CREATE TABLE timeline (
                          user_id UUID NOT NULL,
                          post_id UUID NOT NULL,
                          author_id UUID NOT NULL,
                          created_at TIMESTAMP NOT NULL,

                          PRIMARY KEY (user_id, post_id)
);

CREATE INDEX idx_timeline_user_created
    ON timeline(user_id, created_at DESC);