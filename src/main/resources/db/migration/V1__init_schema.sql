-- =============================================
-- RBAC 权限系统数据库迁移脚本 V1
-- 创建所有核心表结构
-- Author: yixuanmiao
-- Date: 2025-12-10
-- =============================================

-- 启用 PostgreSQL 扩展
CREATE EXTENSION IF NOT EXISTS citext;

-- =============================================
-- 1. 部门表
-- =============================================
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

COMMENT ON TABLE sys_dept IS '部门表';
COMMENT ON COLUMN sys_dept.ancestors IS '祖级列表（逗号分隔）';
COMMENT ON COLUMN sys_dept.status IS '状态：0-禁用 1-启用';

CREATE INDEX idx_dept_parent_id ON sys_dept(parent_id) WHERE NOT deleted;
CREATE INDEX idx_dept_status ON sys_dept(status) WHERE NOT deleted;

-- =============================================
-- 2. 用户表（重构）
-- =============================================
-- 先删除旧表（如果存在关联，需要先处理）
-- DROP TABLE IF EXISTS sys_user CASCADE;

-- 由于已存在 sys_user 表，这里使用 ALTER TABLE 添加新字段
-- 注意：实际迁移时需要根据现有数据情况调整

-- 临时方案：创建新表结构，后续迁移数据
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

COMMENT ON TABLE sys_user IS '用户表';
COMMENT ON COLUMN sys_user.gender IS '性别：0-未知 1-男 2-女';
COMMENT ON COLUMN sys_user.status IS '状态：0-禁用 1-启用';

CREATE INDEX idx_user_dept_id ON sys_user(dept_id) WHERE NOT deleted;
CREATE INDEX idx_user_status ON sys_user(status) WHERE NOT deleted;
CREATE INDEX idx_user_username ON sys_user(username) WHERE NOT deleted;

-- =============================================
-- 3. 岗位表
-- =============================================
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

COMMENT ON TABLE sys_post IS '岗位表';
COMMENT ON COLUMN sys_post.status IS '状态：0-禁用 1-启用';

CREATE INDEX idx_post_status ON sys_post(status) WHERE NOT deleted;

-- =============================================
-- 4. 用户岗位关联表
-- =============================================
CREATE TABLE sys_user_post (
    id              BIGSERIAL PRIMARY KEY,
    user_id         UUID NOT NULL,
    post_id         UUID NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_user_post_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_post_post FOREIGN KEY (post_id) REFERENCES sys_post(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_post UNIQUE (user_id, post_id)
);

COMMENT ON TABLE sys_user_post IS '用户岗位关联表';

CREATE INDEX idx_user_post_user_id ON sys_user_post(user_id);
CREATE INDEX idx_user_post_post_id ON sys_user_post(post_id);

-- =============================================
-- 5. 角色表（重构）
-- =============================================
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

COMMENT ON TABLE sys_role IS '角色表';
COMMENT ON COLUMN sys_role.data_scope IS '数据权限范围：1-全部数据 2-本部门数据 3-本部门及子部门数据 4-仅本人数据 5-自定义部门数据';
COMMENT ON COLUMN sys_role.data_scope_dept_ids IS '自定义数据权限部门ID（JSON数组）';
COMMENT ON COLUMN sys_role.status IS '状态：0-禁用 1-启用';
COMMENT ON COLUMN sys_role.role_type IS '角色类型：1-内置角色 2-自定义角色';

CREATE INDEX idx_role_status ON sys_role(status) WHERE NOT deleted;
CREATE INDEX idx_role_code ON sys_role(role_code) WHERE NOT deleted;

-- =============================================
-- 6. 菜单表（重构）
-- =============================================
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

COMMENT ON TABLE sys_menu IS '菜单表';
COMMENT ON COLUMN sys_menu.menu_type IS '菜单类型：1-目录 2-菜单 3-按钮';
COMMENT ON COLUMN sys_menu.is_frame IS '是否为外链';
COMMENT ON COLUMN sys_menu.is_cache IS '是否缓存';
COMMENT ON COLUMN sys_menu.permission_code IS '权限标识';
COMMENT ON COLUMN sys_menu.status IS '状态：0-禁用 1-启用';

CREATE INDEX idx_menu_parent_id ON sys_menu(parent_id) WHERE NOT deleted;
CREATE INDEX idx_menu_status ON sys_menu(status) WHERE NOT deleted;
CREATE INDEX idx_menu_permission ON sys_menu(permission_code) WHERE NOT deleted;

-- =============================================
-- 7. 用户角色关联表（重构）
-- =============================================
CREATE TABLE sys_user_role (
    id              BIGSERIAL PRIMARY KEY,
    user_id         UUID NOT NULL,
    role_id         UUID NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_role UNIQUE (user_id, role_id)
);

COMMENT ON TABLE sys_user_role IS '用户角色关联表';

CREATE INDEX idx_user_role_user_id ON sys_user_role(user_id);
CREATE INDEX idx_user_role_role_id ON sys_user_role(role_id);

-- =============================================
-- 8. 角色菜单关联表（重构）
-- =============================================
CREATE TABLE sys_role_menu (
    id              BIGSERIAL PRIMARY KEY,
    role_id         UUID NOT NULL,
    menu_id         UUID NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_role_menu_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_menu_menu FOREIGN KEY (menu_id) REFERENCES sys_menu(id) ON DELETE CASCADE,
    CONSTRAINT uk_role_menu UNIQUE (role_id, menu_id)
);

COMMENT ON TABLE sys_role_menu IS '角色菜单关联表';

CREATE INDEX idx_role_menu_role_id ON sys_role_menu(role_id);
CREATE INDEX idx_role_menu_menu_id ON sys_role_menu(menu_id);

-- =============================================
-- 9. 操作日志表
-- =============================================
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

COMMENT ON TABLE sys_operate_log IS '操作日志表';
COMMENT ON COLUMN sys_operate_log.operation IS '操作类型：CREATE/UPDATE/DELETE/QUERY/EXPORT/IMPORT';
COMMENT ON COLUMN sys_operate_log.operation_time IS '执行时长（毫秒）';
COMMENT ON COLUMN sys_operate_log.status IS '状态：1-成功 2-失败';

CREATE INDEX idx_operate_log_user_id ON sys_operate_log(user_id);
CREATE INDEX idx_operate_log_module ON sys_operate_log(module);
CREATE INDEX idx_operate_log_created_at ON sys_operate_log(created_at);
CREATE INDEX idx_operate_log_trace_id ON sys_operate_log(trace_id);

-- =============================================
-- 10. 登录日志表
-- =============================================
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

COMMENT ON TABLE sys_login_log IS '登录日志表';
COMMENT ON COLUMN sys_login_log.login_type IS '登录类型：1-账号密码 2-手机验证码 3-第三方登录';
COMMENT ON COLUMN sys_login_log.status IS '登录状态：1-成功 2-失败';

CREATE INDEX idx_login_log_user_id ON sys_login_log(user_id);
CREATE INDEX idx_login_log_username ON sys_login_log(username);
CREATE INDEX idx_login_log_status ON sys_login_log(status);
CREATE INDEX idx_login_log_created_at ON sys_login_log(created_at);

-- =============================================
-- 11. 在线用户表
-- =============================================
CREATE TABLE sys_online_user (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id          VARCHAR(500) NOT NULL UNIQUE,
    user_id             UUID NOT NULL,
    username            VARCHAR(50) NOT NULL,
    dept_id             UUID,
    dept_name           VARCHAR(50),
    login_ip            VARCHAR(50),
    login_location      VARCHAR(100),
    browser             VARCHAR(50),
    os                  VARCHAR(50),
    login_time          TIMESTAMPTZ NOT NULL,
    expire_time         TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_online_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
);

COMMENT ON TABLE sys_online_user IS '在线用户表';
COMMENT ON COLUMN sys_online_user.session_id IS '会话ID（JWT Token，完整令牌）';

CREATE INDEX idx_online_user_user_id ON sys_online_user(user_id);
CREATE INDEX idx_online_user_expire_time ON sys_online_user(expire_time);

-- =============================================
-- 12. 字典类型表
-- =============================================
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

COMMENT ON TABLE sys_dict_type IS '字典类型表';
COMMENT ON COLUMN sys_dict_type.dict_type IS '字典类型（唯一标识）';
COMMENT ON COLUMN sys_dict_type.status IS '状态：0-禁用 1-启用';

CREATE INDEX idx_dict_type_status ON sys_dict_type(status) WHERE NOT deleted;

-- =============================================
-- 13. 字典数据表
-- =============================================
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

COMMENT ON TABLE sys_dict_data IS '字典数据表';
COMMENT ON COLUMN sys_dict_data.dict_label IS '字典标签（显示值）';
COMMENT ON COLUMN sys_dict_data.dict_value IS '字典键值（实际值）';
COMMENT ON COLUMN sys_dict_data.list_class IS '列表样式：default/primary/success/info/warning/danger';
COMMENT ON COLUMN sys_dict_data.status IS '状态：0-禁用 1-启用';

CREATE INDEX idx_dict_data_type ON sys_dict_data(dict_type) WHERE NOT deleted;
CREATE INDEX idx_dict_data_status ON sys_dict_data(status) WHERE NOT deleted;

-- =============================================
-- 14. 系统配置表
-- =============================================
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

COMMENT ON TABLE sys_config IS '系统配置表';
COMMENT ON COLUMN sys_config.config_type IS '配置类型：1-系统内置 2-自定义';

CREATE INDEX idx_config_key ON sys_config(config_key) WHERE NOT deleted;

-- =============================================
-- 15. 通知公告表
-- =============================================
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

COMMENT ON TABLE sys_notice IS '通知公告表';
COMMENT ON COLUMN sys_notice.notice_type IS '类型：1-通知 2-公告';
COMMENT ON COLUMN sys_notice.status IS '状态：0-关闭 1-正常';

CREATE INDEX idx_notice_status ON sys_notice(status) WHERE NOT deleted;
CREATE INDEX idx_notice_created_at ON sys_notice(created_at) WHERE NOT deleted;
