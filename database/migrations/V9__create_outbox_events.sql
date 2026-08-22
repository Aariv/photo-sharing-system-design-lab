CREATE TABLE outbox_events (
                               id UUID PRIMARY KEY,
                               aggregate_type VARCHAR(100) NOT NULL,
                               aggregate_id UUID NOT NULL,
                               event_type VARCHAR(100) NOT NULL,
                               payload TEXT NOT NULL,
                               status VARCHAR(30) NOT NULL,
                               attempts INTEGER NOT NULL DEFAULT 0,
                               created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                               processed_at TIMESTAMP WITH TIME ZONE,
                               last_error TEXT,
                               next_attempt_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_outbox_events_pending
    ON outbox_events(status, created_at);

CREATE INDEX idx_outbox_events_aggregate
    ON outbox_events(aggregate_type, aggregate_id);


-- attempts          → number of publication attempts
-- last_error        → most recent Kafka failure
-- next_attempt_at   → retry scheduling
