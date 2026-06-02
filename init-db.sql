-- Create PM Service role
DO
$$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'pm_service_role') THEN
        CREATE ROLE pm_service_role WITH LOGIN PASSWORD 'pm_secret';
    END IF;
END
$$;

-- Create schema and grant access
CREATE SCHEMA IF NOT EXISTS pm_schema;
GRANT ALL PRIVILEGES ON SCHEMA pm_schema TO pm_service_role;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA pm_schema TO pm_service_role;
ALTER DEFAULT PRIVILEGES IN SCHEMA pm_schema GRANT ALL PRIVILEGES ON TABLES TO pm_service_role;
ALTER DEFAULT PRIVILEGES IN SCHEMA pm_schema GRANT ALL PRIVILEGES ON SEQUENCES TO pm_service_role;

-- Create Task Service role
DO
$$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'task_service_role') THEN
        CREATE ROLE task_service_role WITH LOGIN PASSWORD 'secret';
    END IF;
END
$$;

-- Create schema and grant access
CREATE SCHEMA IF NOT EXISTS task_schema;
GRANT ALL PRIVILEGES ON SCHEMA task_schema TO task_service_role;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA task_schema TO task_service_role;
ALTER DEFAULT PRIVILEGES IN SCHEMA task_schema GRANT ALL PRIVILEGES ON TABLES TO task_service_role;
ALTER DEFAULT PRIVILEGES IN SCHEMA task_schema GRANT ALL PRIVILEGES ON SEQUENCES TO task_service_role;

-- Create User Service role
DO
$$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'user_service_role') THEN
        CREATE ROLE user_service_role WITH LOGIN PASSWORD 'user_secret';
    END IF;
END
$$;

-- Create schema and grant access
CREATE SCHEMA IF NOT EXISTS user_schema;
GRANT ALL PRIVILEGES ON SCHEMA user_schema TO user_service_role;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA user_schema TO user_service_role;
ALTER DEFAULT PRIVILEGES IN SCHEMA user_schema GRANT ALL PRIVILEGES ON TABLES TO user_service_role;
ALTER DEFAULT PRIVILEGES IN SCHEMA user_schema GRANT ALL PRIVILEGES ON SEQUENCES TO user_service_role;
