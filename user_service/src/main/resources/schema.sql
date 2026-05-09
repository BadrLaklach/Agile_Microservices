CREATE SCHEMA IF NOT EXISTS user_schema;

CREATE TYPE user_schema.role_enum AS ENUM ('ADMIN', 'DEV', 'PO', 'SM', 'MA');

CREATE TABLE user_schema.users (
    id         UUID DEFAULT random_uuid() PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    role       user_schema.role_enum NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT now()
);
