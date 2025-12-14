-- =============================================
-- 系统文件表
-- Author: yixuanmiao
-- Date: 2025-12-14
-- =============================================

-- 系统文件表
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

COMMENT ON TABLE sys_file IS '系统文件表';
COMMENT ON COLUMN sys_file.original_name IS '原始文件名';
COMMENT ON COLUMN sys_file.storage_name IS '存储文件名（UUID + 扩展名）';
COMMENT ON COLUMN sys_file.extension IS '文件扩展名';
COMMENT ON COLUMN sys_file.content_type IS '文件 MIME 类型';
COMMENT ON COLUMN sys_file.size IS '文件大小（字节）';
COMMENT ON COLUMN sys_file.path IS '文件存储路径（相对路径）';
COMMENT ON COLUMN sys_file.md5 IS '文件 MD5 哈希值';
COMMENT ON COLUMN sys_file.storage_type IS '存储类型：local-本地存储，oss-对象存储';
COMMENT ON COLUMN sys_file.category IS '文件分类/业务模块';

-- 索引
CREATE INDEX idx_file_storage_name ON sys_file(storage_name) WHERE NOT deleted;
CREATE INDEX idx_file_md5 ON sys_file(md5) WHERE NOT deleted;
CREATE INDEX idx_file_category ON sys_file(category) WHERE NOT deleted;
CREATE INDEX idx_file_created_at ON sys_file(created_at DESC) WHERE NOT deleted;

-- 插入文件管理相关菜单（二级菜单）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, order_num, path, component, permission_code, icon, visible, status, creator, created_at, updated_at, deleted)
VALUES
    ('00000000-0000-0000-0000-000000000520', '00000000-0000-0000-0000-000000000401', '文件管理', 2, 10, '/system/file', 'system/file/index', 'system:file:list', 'upload', TRUE, 1, NULL, NOW(), NOW(), FALSE);

-- 插入按钮权限（三级）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, order_num, permission_code, visible, status, creator, created_at, updated_at, deleted)
VALUES
    ('00000000-0000-0000-0000-000000000620', '00000000-0000-0000-0000-000000000520', '文件查询', 3, 1, 'system:file:query', TRUE, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000621', '00000000-0000-0000-0000-000000000520', '文件上传', 3, 2, 'system:file:upload', TRUE, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000622', '00000000-0000-0000-0000-000000000520', '文件下载', 3, 3, 'system:file:download', TRUE, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000623', '00000000-0000-0000-0000-000000000520', '文件删除', 3, 4, 'system:file:delete', TRUE, 1, NULL, NOW(), NOW(), FALSE);
