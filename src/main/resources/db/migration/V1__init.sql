-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS citext;

-- Create table sys_user
CREATE TABLE IF NOT EXISTS sys_user (
    id UUID PRIMARY KEY,
    username CITEXT NOT NULL,
    display_name CITEXT NULL,
    email CITEXT NULL,
    phone VARCHAR(30) NULL,
    password VARCHAR(255) NOT NULL,
    status SMALLINT NOT NULL,
    github_id VARCHAR(64) NULL,
    last_login_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_sys_user_status CHECK (status IN (1,2,3,4))
);

-- Unique constraints and indexes
CREATE UNIQUE INDEX IF NOT EXISTS ux_sys_user_username ON sys_user (username);
CREATE UNIQUE INDEX IF NOT EXISTS ux_sys_user_email ON sys_user (email);
CREATE UNIQUE INDEX IF NOT EXISTS ux_sys_user_github_id ON sys_user (github_id) WHERE github_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS ix_sys_user_created_at ON sys_user (created_at);
CREATE INDEX IF NOT EXISTS ix_sys_user_status ON sys_user (status);


