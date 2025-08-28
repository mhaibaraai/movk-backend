/*
 * @Author yixuanmiao
 * @Date 2025/08/28 21:48
 */

-- 必需扩展（大小写不敏感 & 加密）
CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 创建表 sys_user
CREATE TABLE IF NOT EXISTS sys_user
(
    id            UUID PRIMARY KEY,
    username      CITEXT       NOT NULL,
    display_name  CITEXT       NULL,
    email         CITEXT       NULL,
    phone         VARCHAR(30)  NULL,
    password      VARCHAR(255) NOT NULL,
    status        SMALLINT     NOT NULL,
    github_id     VARCHAR(64)  NULL,
    last_login_at TIMESTAMPTZ  NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_sys_user_status CHECK (status IN (1, 2, 3, 4))
);

-- 注释
COMMENT ON TABLE sys_user IS '系统用户表';
COMMENT ON COLUMN sys_user.id IS '主键ID';
COMMENT ON COLUMN sys_user.username IS '用户名，区分大小写不敏感';
COMMENT ON COLUMN sys_user.display_name IS '显示名称，区分大小写不敏感';
COMMENT ON COLUMN sys_user.email IS '邮箱，区分大小写不敏感';
COMMENT ON COLUMN sys_user.phone IS '手机号';
COMMENT ON COLUMN sys_user.password IS '密码（建议存储哈希）';
COMMENT ON COLUMN sys_user.status IS '用户状态(1:Active, 2:Inactive, 3:Pending, 4:Suspended)';
COMMENT ON COLUMN sys_user.github_id IS 'GitHub ID';
COMMENT ON COLUMN sys_user.last_login_at IS '最后登录时间';
COMMENT ON COLUMN sys_user.created_at IS '创建时间';
COMMENT ON COLUMN sys_user.updated_at IS '更新时间';

-- 唯一约束与索引
CREATE UNIQUE INDEX IF NOT EXISTS ux_sys_user_username ON sys_user (username);
CREATE UNIQUE INDEX IF NOT EXISTS ux_sys_user_email ON sys_user (email);
CREATE UNIQUE INDEX IF NOT EXISTS ux_sys_user_github_id ON sys_user (github_id) WHERE github_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS ix_sys_user_created_at ON sys_user (created_at);
CREATE INDEX IF NOT EXISTS ix_sys_user_status ON sys_user (status);

INSERT INTO sys_user (id, username, display_name, email, phone, password, status, github_id)
VALUES (gen_random_uuid(), 'Admin', 'Admin', NULL, NULL, crypt('ChangeMe_123!', gen_salt('bf')), 1, NULL)
ON CONFLICT (username) DO NOTHING;

INSERT INTO sys_user (id, username, display_name, email, phone, password, status, github_id)
VALUES (gen_random_uuid(), 'yixuan', '亦旋', 'mhaibaraai@gmail.com', '18367493064',
        crypt('ChangeMe_123!', gen_salt('bf')), 1, NULL)
ON CONFLICT (username) DO NOTHING;

INSERT INTO sys_user (id, username, display_name, email, phone, password, status, github_id)
VALUES (gen_random_uuid(), 'gaoyangyang', '高洋洋', '1079666783@qq.com', '18968258104',
        crypt('ChangeMe_123!', gen_salt('bf')), 1, NULL)
ON CONFLICT (username) DO NOTHING;
