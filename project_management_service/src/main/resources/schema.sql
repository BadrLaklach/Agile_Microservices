

-- Projects
CREATE TABLE IF NOT EXISTS pm_schema.projects (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    methodology   VARCHAR(50)  NOT NULL CHECK (methodology IN ('SCRUM','KANBAN','HYBRID')),
    status        VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE'
                               CHECK (status IN ('ACTIVE','ARCHIVED')),
    start_date    DATE         NOT NULL,
    end_date      DATE         NOT NULL,
    created_by    UUID         NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT chk_project_dates CHECK (end_date > start_date)
);

-- Project members
CREATE TABLE IF NOT EXISTS pm_schema.project_members (
    project_id  UUID        NOT NULL REFERENCES pm_schema.projects(id) ON DELETE CASCADE,
    user_id     UUID        NOT NULL,
    role        VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN','PO','SM','DEV','MA')),
    joined_at   TIMESTAMP   NOT NULL DEFAULT now(),
    PRIMARY KEY (project_id, user_id)
);

-- Sprints
CREATE TABLE IF NOT EXISTS pm_schema.sprints (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id  UUID         NOT NULL REFERENCES pm_schema.projects(id) ON DELETE CASCADE,
    name        VARCHAR(255) NOT NULL,
    goal        TEXT,
    status      VARCHAR(50)  NOT NULL DEFAULT 'PLANNED'
                             CHECK (status IN ('PLANNED','ACTIVE','COMPLETED')),
    capacity    INTEGER,
    start_date  DATE         NOT NULL,
    end_date    DATE         NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT chk_sprint_dates CHECK (end_date > start_date)
);

-- Sprint-task assignments
CREATE TABLE IF NOT EXISTS pm_schema.sprint_tasks (
    sprint_id    UUID      NOT NULL REFERENCES pm_schema.sprints(id) ON DELETE CASCADE,
    task_id      UUID      NOT NULL,
    assigned_at  TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (sprint_id, task_id)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_projects_created_by  ON pm_schema.projects(created_by);
CREATE INDEX IF NOT EXISTS idx_sprints_project_id   ON pm_schema.sprints(project_id);
CREATE INDEX IF NOT EXISTS idx_sprint_tasks_sprint  ON pm_schema.sprint_tasks(sprint_id);
CREATE INDEX IF NOT EXISTS idx_members_user_id      ON pm_schema.project_members(user_id);
