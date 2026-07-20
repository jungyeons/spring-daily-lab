CREATE TABLE task_creation_idempotency (
    idempotency_key VARCHAR(128) PRIMARY KEY,
    request_fingerprint VARCHAR(64) NOT NULL,
    task_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_task_creation_idempotency_task
        FOREIGN KEY (task_id) REFERENCES growth_tasks (id)
);
