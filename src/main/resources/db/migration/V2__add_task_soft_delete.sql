ALTER TABLE growth_tasks ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_growth_tasks_deleted_at ON growth_tasks (deleted_at);
