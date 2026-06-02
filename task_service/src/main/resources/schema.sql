

CREATE TABLE IF NOT EXISTS task_schema.tasks (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id   UUID,
    sprint_id    UUID,
    title        VARCHAR(255) NOT NULL,
    description  TEXT,
    type         VARCHAR(50)  NOT NULL
                 CHECK (type IN (
                     'USER_STORY','BUG','TECHNICAL_TASK',
                     'SUPPORT','MAINTENANCE','MEETING','TRAINING','ON_CALL','LEAVE'
                 )),
    priority     VARCHAR(50)  NOT NULL DEFAULT 'MEDIUM'
                 CHECK (priority IN ('LOW','MEDIUM','HIGH','CRITICAL')),
    status       VARCHAR(50)  NOT NULL DEFAULT 'TODO'
                 CHECK (status IN ('TODO','IN_PROGRESS','DONE')),
    estimate     INTEGER      CHECK (estimate > 0),
    assignee_id  UUID,
    created_by   UUID         NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT chk_project_task_type CHECK (
        (type IN ('USER_STORY','BUG','TECHNICAL_TASK') AND project_id IS NOT NULL)
        OR
        (type IN ('SUPPORT','MAINTENANCE','MEETING','TRAINING','ON_CALL','LEAVE'))
    )
);

CREATE INDEX IF NOT EXISTS idx_tasks_project_id  ON task_schema.tasks(project_id);
CREATE INDEX IF NOT EXISTS idx_tasks_sprint_id   ON task_schema.tasks(sprint_id);
CREATE INDEX IF NOT EXISTS idx_tasks_assignee_id ON task_schema.tasks(assignee_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status      ON task_schema.tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_type        ON task_schema.tasks(type);
CREATE INDEX IF NOT EXISTS idx_tasks_created_by  ON task_schema.tasks(created_by);
