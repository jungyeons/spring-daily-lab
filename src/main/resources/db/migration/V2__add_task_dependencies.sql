CREATE TABLE task_dependencies (
    task_id BIGINT NOT NULL,
    depends_on_task_id BIGINT NOT NULL,
    PRIMARY KEY (task_id, depends_on_task_id),
    CONSTRAINT chk_task_dependencies_not_self CHECK (task_id <> depends_on_task_id),
    CONSTRAINT fk_task_dependencies_task
        FOREIGN KEY (task_id) REFERENCES growth_tasks (id) ON DELETE CASCADE,
    CONSTRAINT fk_task_dependencies_prerequisite
        FOREIGN KEY (depends_on_task_id) REFERENCES growth_tasks (id) ON DELETE CASCADE
);

CREATE INDEX idx_task_dependencies_prerequisite ON task_dependencies (depends_on_task_id);
