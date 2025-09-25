-- liquibase formatted sql

-- changeset emuratov:1
CREATE TABLE notification_task (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    scheduled_time TIMESTAMP NOT NULL
);

-- changeset emuratov:2
CREATE INDEX idx_notification_task_time ON notification_task(scheduled_time);