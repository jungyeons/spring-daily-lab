ALTER TABLE growth_tasks ADD COLUMN recurrence_frequency VARCHAR(16);
ALTER TABLE growth_tasks ADD COLUMN recurrence_generated_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE growth_tasks ADD CONSTRAINT chk_growth_tasks_recurrence_frequency CHECK (
    recurrence_frequency IS NULL OR recurrence_frequency IN ('DAILY', 'WEEKLY', 'MONTHLY')
);
