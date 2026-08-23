ALTER TABLE search_documents
    ADD COLUMN search_vector tsvector;

CREATE INDEX idx_search_vector
    ON search_documents
    USING GIN (search_vector);

-- Why GIN
-- GIN (Generalized Inverted Index) is a type of index in PostgreSQL that is particularly well-suited for full-text search. It allows for efficient searching of text data by indexing the individual words (or tokens) in the text, making it faster to retrieve results based on search queries.

UPDATE search_documents
SET search_vector =
        to_tsvector(
                'english',
                search_text
        );

CREATE FUNCTION update_search_vector()
    RETURNS trigger AS
    $$
BEGIN
    NEW.search_vector :=
        to_tsvector(
            'english',
            NEW.search_text
        );

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_search_vector
    BEFORE INSERT OR UPDATE
                         ON search_documents
                         FOR EACH ROW
                         EXECUTE FUNCTION update_search_vector();