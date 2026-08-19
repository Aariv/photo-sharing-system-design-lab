-- V7__create_notifications.sql

CREATE TABLE notifications
(
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL,

    message TEXT NOT NULL,

    is_read BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_notifications_user
    ON notifications(user_id);