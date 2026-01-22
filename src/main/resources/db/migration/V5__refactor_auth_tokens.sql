-- V5: 重构认证系统，支持双 Token 架构
-- 1. 删除旧的 sys_online_user 表（存储完整 JWT 不安全）
-- 2. 创建新的 sys_refresh_token 表（只存储 RefreshToken 元数据）

-- 删除旧表
DROP TABLE IF EXISTS sys_online_user;

-- 创建 RefreshToken 表
CREATE TABLE sys_refresh_token (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token           VARCHAR(64) NOT NULL UNIQUE,        -- RefreshToken（UUID 格式，不是 JWT）
    user_id         UUID NOT NULL,
    username        VARCHAR(50) NOT NULL,

    -- 设备/客户端信息
    device_info     VARCHAR(200),                       -- 设备信息（Browser + OS）
    client_ip       VARCHAR(50),                        -- 客户端 IP

    -- 时间信息
    issued_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(), -- 签发时间
    expires_at      TIMESTAMPTZ NOT NULL,               -- 过期时间
    last_used_at    TIMESTAMPTZ,                        -- 最后使用时间（刷新 AccessToken 时更新）

    -- 撤销信息
    revoked         BOOLEAN NOT NULL DEFAULT FALSE,     -- 是否已撤销
    revoked_at      TIMESTAMPTZ,                        -- 撤销时间
    revoked_reason  VARCHAR(100),                       -- 撤销原因

    -- 外键约束
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
);

-- 索引
CREATE INDEX idx_refresh_token_token ON sys_refresh_token(token);
CREATE INDEX idx_refresh_token_user_id ON sys_refresh_token(user_id);
CREATE INDEX idx_refresh_token_expires_at ON sys_refresh_token(expires_at);
CREATE INDEX idx_refresh_token_active ON sys_refresh_token(user_id, revoked, expires_at);

-- 注释
COMMENT ON TABLE sys_refresh_token IS 'RefreshToken 存储表，用于双 Token 认证架构';
COMMENT ON COLUMN sys_refresh_token.token IS 'RefreshToken 值（UUID 格式，非 JWT）';
COMMENT ON COLUMN sys_refresh_token.device_info IS '设备信息，格式：Browser/OS';
COMMENT ON COLUMN sys_refresh_token.last_used_at IS '最后使用时间，用于识别活跃会话';
COMMENT ON COLUMN sys_refresh_token.revoked IS '撤销标记，登出或踢出时设为 true';
