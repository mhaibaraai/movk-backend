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

-- 插入文件管理相关菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, order_num, visible, status, cache_flag) VALUES
    (gen_random_uuid(), (SELECT id FROM sys_menu WHERE menu_name = '系统管理' AND deleted = FALSE LIMIT 1), '文件管理', 'C', 'file', 'system/file/index', 'system:file:list', 'upload', 10, TRUE, 1, FALSE);

-- 获取刚插入的文件管理菜单 ID
DO $$
DECLARE
    file_menu_id UUID;
BEGIN
    SELECT id INTO file_menu_id FROM sys_menu WHERE menu_name = '文件管理' AND deleted = FALSE LIMIT 1;

    -- 插入按钮权限
    INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, perms, order_num, visible, status, cache_flag) VALUES
        (gen_random_uuid(), file_menu_id, '文件查询', 'B', 'system:file:query', 1, TRUE, 1, FALSE),
        (gen_random_uuid(), file_menu_id, '文件上传', 'B', 'system:file:upload', 2, TRUE, 1, FALSE),
        (gen_random_uuid(), file_menu_id, '文件下载', 'B', 'system:file:download', 3, TRUE, 1, FALSE),
        (gen_random_uuid(), file_menu_id, '文件删除', 'B', 'system:file:delete', 4, TRUE, 1, FALSE);
END $$;
