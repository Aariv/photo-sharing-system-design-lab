CREATE TABLE search_documents (

                                  post_id UUID PRIMARY KEY,

                                  author_id UUID NOT NULL,

                                  caption TEXT NOT NULL,

                                  search_text TEXT NOT NULL,

                                  created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_search_documents_created_at
    ON search_documents(created_at DESC);