-- RBAC 权限系统 Schema
-- Author: yixuanmiao

CREATE EXTENSION IF NOT EXISTS citext;

-- 1. 部门表
CREATE TABLE sys_dept (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id       UUID,
    ancestors       VARCHAR(500),
    dept_name       VARCHAR(50) NOT NULL,
    dept_code       CITEXT UNIQUE,
    order_num       INT NOT NULL DEFAULT 0,
    leader_user_id  UUID,
    phone           VARCHAR(20),
    email           VARCHAR(100),
    status          SMALLINT NOT NULL DEFAULT 1,
    creator         UUID,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater         UUID,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ,
    CONSTRAINT fk_dept_parent FOREIGN KEY (parent_id) REFERENCES sys_dept(id) ON DELETE RESTRICT
);

CREATE INDEX idx_dept_parent_id ON sys_dept(parent_id) WHERE NOT deleted;
CREATE INDEX idx_dept_tree ON sys_dept(parent_id, order_num) WHERE NOT deleted AND status = 1;
CREATE INDEX idx_dept_ancestors ON sys_dept(ancestors) WHERE NOT deleted;

-- 2. 用户表
CREATE TABLE sys_user (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username        CITEXT NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    nickname        VARCHAR(50),
    email           CITEXT UNIQUE,
    phone           VARCHAR(20) UNIQUE,
    gender          SMALLINT DEFAULT 0,
    avatar          VARCHAR(500),
    status          SMALLINT NOT NULL DEFAULT 1,
    dept_id         UUID,
    remark          VARCHAR(500),
    login_ip        VARCHAR(50),
    login_date      TIMESTAMPTZ,
    creator         UUID,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater         UUID,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ,
    CONSTRAINT fk_user_dept FOREIGN KEY (dept_id) REFERENCES sys_dept(id) ON DELETE SET NULL
);

CREATE INDEX idx_user_dept_status ON sys_user(dept_id, status) WHERE NOT deleted;
CREATE INDEX idx_user_login ON sys_user(username, status) WHERE NOT deleted;

-- 3. 岗位表
CREATE TABLE sys_post (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_code       CITEXT NOT NULL UNIQUE,
    post_name       VARCHAR(50) NOT NULL,
    order_num       INT NOT NULL DEFAULT 0,
    status          SMALLINT NOT NULL DEFAULT 1,
    remark          VARCHAR(500),
    creator         UUID,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater         UUID,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ
);

-- 4. 用户岗位关联表（复合主键）
CREATE TABLE sys_user_post (
    user_id         UUID NOT NULL,
    post_id         UUID NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, post_id),
    CONSTRAINT fk_user_post_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_post_post FOREIGN KEY (post_id) REFERENCES sys_post(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_post_post_id ON sys_user_post(post_id);

-- 5. 角色表
CREATE TABLE sys_role (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_name               VARCHAR(50) NOT NULL,
    role_code               CITEXT NOT NULL UNIQUE,
    role_sort               INT NOT NULL DEFAULT 0,
    data_scope              SMALLINT NOT NULL DEFAULT 1,
    data_scope_dept_ids     TEXT,
    status                  SMALLINT NOT NULL DEFAULT 1,
    role_type               SMALLINT NOT NULL DEFAULT 2,
    remark                  VARCHAR(500),
    creator                 UUID,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater                 UUID,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted                 BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at              TIMESTAMPTZ
);

CREATE INDEX idx_role_code ON sys_role(role_code) WHERE NOT deleted;
CREATE INDEX idx_role_data_scope ON sys_role(data_scope, status) WHERE NOT deleted;

-- 6. 菜单表
CREATE TABLE sys_menu (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id           UUID,
    menu_name           VARCHAR(50) NOT NULL,
    menu_type           SMALLINT NOT NULL,
    order_num           INT NOT NULL DEFAULT 0,
    path                VARCHAR(200),
    component           VARCHAR(255),
    query_params        VARCHAR(255),
    is_frame            BOOLEAN DEFAULT FALSE,
    is_cache            BOOLEAN DEFAULT TRUE,
    permission_code     CITEXT,
    visible             BOOLEAN NOT NULL DEFAULT TRUE,
    status              SMALLINT NOT NULL DEFAULT 1,
    icon                VARCHAR(100),
    remark              VARCHAR(500),
    creator             UUID,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater             UUID,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMPTZ
);

CREATE INDEX idx_menu_parent_id ON sys_menu(parent_id) WHERE NOT deleted;
CREATE INDEX idx_menu_permission ON sys_menu(permission_code) WHERE NOT deleted;
CREATE INDEX idx_menu_tree ON sys_menu(parent_id, order_num) WHERE NOT deleted AND status = 1;
CREATE INDEX idx_menu_type_status ON sys_menu(menu_type, status) WHERE NOT deleted;

-- 7. 用户角色关联表（复合主键）
CREATE TABLE sys_user_role (
    user_id         UUID NOT NULL,
    role_id         UUID NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_role_role_id ON sys_user_role(role_id);

-- 8. 角色菜单关联表（复合主键）
CREATE TABLE sys_role_menu (
    role_id         UUID NOT NULL,
    menu_id         UUID NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (role_id, menu_id),
    CONSTRAINT fk_role_menu_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_menu_menu FOREIGN KEY (menu_id) REFERENCES sys_menu(id) ON DELETE CASCADE
);

CREATE INDEX idx_role_menu_menu_id ON sys_role_menu(menu_id);

-- 9. 操作日志表
CREATE TABLE sys_operate_log (
    id                  BIGSERIAL PRIMARY KEY,
    trace_id            VARCHAR(64),
    user_id             UUID,
    username            VARCHAR(50),
    module              VARCHAR(50),
    operation           VARCHAR(50),
    method              VARCHAR(200),
    request_method      VARCHAR(10),
    request_url         VARCHAR(500),
    request_params      TEXT,
    request_body        TEXT,
    response_data       TEXT,
    user_ip             VARCHAR(50),
    user_agent          VARCHAR(500),
    operation_time      INT,
    status              SMALLINT NOT NULL,
    error_msg           TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_operate_log_user_id ON sys_operate_log(user_id);
CREATE INDEX idx_operate_log_trace_id ON sys_operate_log(trace_id);
CREATE INDEX idx_operate_log_time_range ON sys_operate_log(created_at DESC, user_id);
CREATE INDEX idx_operate_log_filter ON sys_operate_log(module, status, created_at DESC);

-- 10. 登录日志表
CREATE TABLE sys_login_log (
    id                  BIGSERIAL PRIMARY KEY,
    trace_id            VARCHAR(64),
    user_id             UUID,
    username            VARCHAR(50) NOT NULL,
    login_type          SMALLINT NOT NULL,
    login_ip            VARCHAR(50),
    login_location      VARCHAR(100),
    browser             VARCHAR(50),
    os                  VARCHAR(50),
    user_agent          VARCHAR(500),
    status              SMALLINT NOT NULL,
    message             VARCHAR(500),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_login_log_user_id ON sys_login_log(user_id);
CREATE INDEX idx_login_log_time_range ON sys_login_log(created_at DESC, user_id);
CREATE INDEX idx_login_log_filter ON sys_login_log(username, status, created_at DESC);

-- 11. 字典类型表
CREATE TABLE sys_dict_type (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dict_name       VARCHAR(100) NOT NULL,
    dict_type       CITEXT NOT NULL UNIQUE,
    status          SMALLINT NOT NULL DEFAULT 1,
    remark          VARCHAR(500),
    creator         UUID,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater         UUID,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ
);

-- 12. 字典数据表
CREATE TABLE sys_dict_data (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dict_type       CITEXT NOT NULL,
    dict_label      VARCHAR(100) NOT NULL,
    dict_value      VARCHAR(100) NOT NULL,
    dict_sort       INT NOT NULL DEFAULT 0,
    css_class       VARCHAR(100),
    list_class      VARCHAR(100),
    is_default      BOOLEAN DEFAULT FALSE,
    status          SMALLINT NOT NULL DEFAULT 1,
    remark          VARCHAR(500),
    creator         UUID,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater         UUID,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ,
    CONSTRAINT fk_dict_data_type FOREIGN KEY (dict_type) REFERENCES sys_dict_type(dict_type) ON DELETE CASCADE
);

CREATE INDEX idx_dict_data_type_sort ON sys_dict_data(dict_type, dict_sort) WHERE NOT deleted AND status = 1;

-- 13. 系统配置表
CREATE TABLE sys_config (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    config_name     VARCHAR(100) NOT NULL,
    config_key      CITEXT NOT NULL UNIQUE,
    config_value    TEXT NOT NULL,
    config_type     SMALLINT NOT NULL DEFAULT 1,
    remark          VARCHAR(500),
    creator         UUID,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater         UUID,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ
);

-- 14. 通知公告表
CREATE TABLE sys_notice (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notice_title    VARCHAR(100) NOT NULL,
    notice_type     SMALLINT NOT NULL,
    notice_content  TEXT,
    status          SMALLINT NOT NULL DEFAULT 1,
    creator         UUID,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater         UUID,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ
);

CREATE INDEX idx_notice_created_at ON sys_notice(created_at DESC) WHERE NOT deleted;

-- 15. 系统文件表
CREATE TABLE sys_file (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    original_name   VARCHAR(255) NOT NULL,
    storage_name    VARCHAR(100) NOT NULL,
    extension       VARCHAR(50),
    content_type    VARCHAR(100),
    size            BIGINT NOT NULL,
    path            VARCHAR(500) NOT NULL,
    md5             VARCHAR(32),
    storage_type    VARCHAR(20) NOT NULL DEFAULT 'local',
    category        VARCHAR(50),
    remark          VARCHAR(500),
    creator         UUID,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater         UUID,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ
);

CREATE INDEX idx_file_storage_name ON sys_file(storage_name) WHERE NOT deleted;
CREATE INDEX idx_file_md5 ON sys_file(md5) WHERE NOT deleted;
CREATE INDEX idx_file_created_at ON sys_file(created_at DESC) WHERE NOT deleted;

-- 16. RefreshToken 表
CREATE TABLE sys_refresh_token (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token           VARCHAR(64) NOT NULL UNIQUE,
    user_id         UUID NOT NULL,
    username        VARCHAR(50) NOT NULL,
    device_info     VARCHAR(200),
    client_ip       VARCHAR(50),
    issued_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMPTZ NOT NULL,
    last_used_at    TIMESTAMPTZ,
    revoked         BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at      TIMESTAMPTZ,
    revoked_reason  VARCHAR(100),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token_user_id ON sys_refresh_token(user_id);
CREATE INDEX idx_refresh_token_expires_at ON sys_refresh_token(expires_at);
CREATE INDEX idx_refresh_token_active ON sys_refresh_token(user_id, revoked, expires_at);
