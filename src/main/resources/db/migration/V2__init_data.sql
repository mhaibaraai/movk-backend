-- =============================================
-- RBAC 权限系统数据初始化脚本 V2
-- 初始化基础数据：部门、角色、菜单、用户等
-- Author: yixuanmiao
-- Date: 2025-12-10
-- =============================================

-- =============================================
-- 1. 初始化部门数据
-- =============================================
INSERT INTO sys_dept (id, parent_id, ancestors, dept_name, dept_code, order_num, status, creator, created_at, updated_at, deleted)
VALUES
    ('00000000-0000-0000-0000-000000000001', NULL, '0', '总公司', 'ROOT', 0, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', '0,1', '研发部', 'DEV', 1, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000001', '0,1', '市场部', 'MARKET', 2, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000001', '0,1', '财务部', 'FINANCE', 3, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000002', '0,1,2', '后端组', 'BACKEND', 1, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000002', '0,1,2', '前端组', 'FRONTEND', 2, 1, NULL, NOW(), NOW(), FALSE);

-- =============================================
-- 2. 初始化岗位数据
-- =============================================
INSERT INTO sys_post (id, post_code, post_name, order_num, status, creator, created_at, updated_at, deleted)
VALUES
    ('00000000-0000-0000-0000-000000000101', 'CEO', '董事长', 1, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000102', 'CTO', '技术总监', 2, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000103', 'MANAGER', '部门经理', 3, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000104', 'STAFF', '普通员工', 4, 1, NULL, NOW(), NOW(), FALSE);

-- =============================================
-- 3. 初始化用户数据
-- =============================================
-- 密码均为：Admin@123（BCrypt 加密，strength=10）
INSERT INTO sys_user (id, username, password, nickname, email, phone, gender, status, dept_id, creator, created_at, updated_at, deleted)
VALUES
    ('00000000-0000-0000-0000-000000000201', 'admin', '$2a$10$YJDXekyXEzN3Dj8K8Ry3V.jYwF8H8cPp5eZJc4wZqKZ8nRJ5X7zJy', '超级管理员', 'admin@movk.com', '13800000000', 1, 1, '00000000-0000-0000-0000-000000000001', NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000202', 'user', '$2a$10$YJDXekyXEzN3Dj8K8Ry3V.jYwF8H8cPp5eZJc4wZqKZ8nRJ5X7zJy', '普通用户', 'user@movk.com', '13800000001', 1, 1, '00000000-0000-0000-0000-000000000002', NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000203', 'test', '$2a$10$YJDXekyXEzN3Dj8K8Ry3V.jYwF8H8cPp5eZJc4wZqKZ8nRJ5X7zJy', '测试用户', 'test@movk.com', '13800000002', 2, 1, '00000000-0000-0000-0000-000000000005', NULL, NOW(), NOW(), FALSE);

-- =============================================
-- 4. 初始化角色数据
-- =============================================
INSERT INTO sys_role (id, role_name, role_code, role_sort, data_scope, status, role_type, remark, creator, created_at, updated_at, deleted)
VALUES
    ('00000000-0000-0000-0000-000000000301', '超级管理员', 'admin', 1, 1, 1, 1, '超级管理员，拥有所有权限', NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000302', '普通角色', 'user', 2, 2, 1, 2, '普通用户角色', NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000303', '部门经理', 'dept_manager', 3, 3, 1, 2, '部门经理，可查看本部门及子部门数据', NULL, NOW(), NOW(), FALSE);

-- =============================================
-- 5. 初始化菜单数据
-- =============================================
-- 一级菜单（目录）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, order_num, path, component, icon, visible, status, creator, created_at, updated_at, deleted)
VALUES
    -- 系统管理
    ('00000000-0000-0000-0000-000000000401', NULL, '系统管理', 1, 1, '/system', NULL, 'system', TRUE, 1, NULL, NOW(), NOW(), FALSE),
    -- 系统监控
    ('00000000-0000-0000-0000-000000000402', NULL, '系统监控', 1, 2, '/monitor', NULL, 'monitor', TRUE, 1, NULL, NOW(), NOW(), FALSE);

-- 二级菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, order_num, path, component, permission_code, icon, visible, status, creator, created_at, updated_at, deleted)
VALUES
    -- 系统管理 - 用户管理
    ('00000000-0000-0000-0000-000000000501', '00000000-0000-0000-0000-000000000401', '用户管理', 2, 1, '/system/user', 'system/user/index', 'system:user:list', 'user', TRUE, 1, NULL, NOW(), NOW(), FALSE),
    -- 系统管理 - 角色管理
    ('00000000-0000-0000-0000-000000000502', '00000000-0000-0000-0000-000000000401', '角色管理', 2, 2, '/system/role', 'system/role/index', 'system:role:list', 'peoples', TRUE, 1, NULL, NOW(), NOW(), FALSE),
    -- 系统管理 - 菜单管理
    ('00000000-0000-0000-0000-000000000503', '00000000-0000-0000-0000-000000000401', '菜单管理', 2, 3, '/system/menu', 'system/menu/index', 'system:menu:list', 'tree-table', TRUE, 1, NULL, NOW(), NOW(), FALSE),
    -- 系统管理 - 部门管理
    ('00000000-0000-0000-0000-000000000504', '00000000-0000-0000-0000-000000000401', '部门管理', 2, 4, '/system/dept', 'system/dept/index', 'system:dept:list', 'tree', TRUE, 1, NULL, NOW(), NOW(), FALSE),
    -- 系统管理 - 岗位管理
    ('00000000-0000-0000-0000-000000000505', '00000000-0000-0000-0000-000000000401', '岗位管理', 2, 5, '/system/post', 'system/post/index', 'system:post:list', 'post', TRUE, 1, NULL, NOW(), NOW(), FALSE),
    -- 系统管理 - 字典管理
    ('00000000-0000-0000-0000-000000000506', '00000000-0000-0000-0000-000000000401', '字典管理', 2, 6, '/system/dict', 'system/dict/index', 'system:dict:list', 'dict', TRUE, 1, NULL, NOW(), NOW(), FALSE),
    -- 系统管理 - 通知公告
    ('00000000-0000-0000-0000-000000000507', '00000000-0000-0000-0000-000000000401', '通知公告', 2, 7, '/system/notice', 'system/notice/index', 'system:notice:list', 'message', TRUE, 1, NULL, NOW(), NOW(), FALSE),

    -- 系统监控 - 在线用户
    ('00000000-0000-0000-0000-000000000508', '00000000-0000-0000-0000-000000000402', '在线用户', 2, 1, '/monitor/online', 'monitor/online/index', 'monitor:online:list', 'online', TRUE, 1, NULL, NOW(), NOW(), FALSE),
    -- 系统监控 - 登录日志
    ('00000000-0000-0000-0000-000000000509', '00000000-0000-0000-0000-000000000402', '登录日志', 2, 2, '/monitor/login-log', 'monitor/loginLog/index', 'monitor:loginLog:list', 'log', TRUE, 1, NULL, NOW(), NOW(), FALSE),
    -- 系统监控 - 操作日志
    ('00000000-0000-0000-0000-000000000510', '00000000-0000-0000-0000-000000000402', '操作日志', 2, 3, '/monitor/operate-log', 'monitor/operateLog/index', 'monitor:operateLog:list', 'form', TRUE, 1, NULL, NOW(), NOW(), FALSE);

-- 按钮权限（三级）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, order_num, permission_code, visible, status, creator, created_at, updated_at, deleted)
VALUES
    -- 用户管理按钮
    ('00000000-0000-0000-0000-000000000601', '00000000-0000-0000-0000-000000000501', '用户新增', 3, 1, 'system:user:create', TRUE, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000602', '00000000-0000-0000-0000-000000000501', '用户修改', 3, 2, 'system:user:update', TRUE, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000603', '00000000-0000-0000-0000-000000000501', '用户删除', 3, 3, 'system:user:delete', TRUE, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000604', '00000000-0000-0000-0000-000000000501', '重置密码', 3, 4, 'system:user:resetPwd', TRUE, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000605', '00000000-0000-0000-0000-000000000501', '用户导出', 3, 5, 'system:user:export', TRUE, 1, NULL, NOW(), NOW(), FALSE),

    -- 角色管理按钮
    ('00000000-0000-0000-0000-000000000606', '00000000-0000-0000-0000-000000000502', '角色新增', 3, 1, 'system:role:create', TRUE, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000607', '00000000-0000-0000-0000-000000000502', '角色修改', 3, 2, 'system:role:update', TRUE, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000608', '00000000-0000-0000-0000-000000000502', '角色删除', 3, 3, 'system:role:delete', TRUE, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000609', '00000000-0000-0000-0000-000000000502', '分配权限', 3, 4, 'system:role:menu', TRUE, 1, NULL, NOW(), NOW(), FALSE),

    -- 菜单管理按钮
    ('00000000-0000-0000-0000-000000000610', '00000000-0000-0000-0000-000000000503', '菜单新增', 3, 1, 'system:menu:create', TRUE, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000611', '00000000-0000-0000-0000-000000000503', '菜单修改', 3, 2, 'system:menu:update', TRUE, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000612', '00000000-0000-0000-0000-000000000503', '菜单删除', 3, 3, 'system:menu:delete', TRUE, 1, NULL, NOW(), NOW(), FALSE),

    -- 部门管理按钮
    ('00000000-0000-0000-0000-000000000613', '00000000-0000-0000-0000-000000000504', '部门新增', 3, 1, 'system:dept:create', TRUE, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000614', '00000000-0000-0000-0000-000000000504', '部门修改', 3, 2, 'system:dept:update', TRUE, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000615', '00000000-0000-0000-0000-000000000504', '部门删除', 3, 3, 'system:dept:delete', TRUE, 1, NULL, NOW(), NOW(), FALSE),

    -- 岗位管理按钮
    ('00000000-0000-0000-0000-000000000616', '00000000-0000-0000-0000-000000000505', '岗位新增', 3, 1, 'system:post:create', TRUE, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000617', '00000000-0000-0000-0000-000000000505', '岗位修改', 3, 2, 'system:post:update', TRUE, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000618', '00000000-0000-0000-0000-000000000505', '岗位删除', 3, 3, 'system:post:delete', TRUE, 1, NULL, NOW(), NOW(), FALSE);

-- =============================================
-- 6. 用户角色关联
-- =============================================
INSERT INTO sys_user_role (user_id, role_id, created_at)
VALUES
    ('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000301', NOW()), -- admin -> 超级管理员
    ('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000302', NOW()), -- user -> 普通角色
    ('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000302', NOW()); -- test -> 普通角色

-- =============================================
-- 7. 角色菜单关联（超级管理员拥有所有菜单）
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id, created_at)
SELECT
    '00000000-0000-0000-0000-000000000301',
    id,
    NOW()
FROM sys_menu
WHERE NOT deleted;

-- 普通角色只有查看权限
INSERT INTO sys_role_menu (role_id, menu_id, created_at)
VALUES
    ('00000000-0000-0000-0000-000000000302', '00000000-0000-0000-0000-000000000401', NOW()), -- 系统管理目录
    ('00000000-0000-0000-0000-000000000302', '00000000-0000-0000-0000-000000000501', NOW()); -- 用户管理菜单

-- =============================================
-- 8. 用户岗位关联
-- =============================================
INSERT INTO sys_user_post (user_id, post_id, created_at)
VALUES
    ('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000101', NOW()), -- admin -> CEO
    ('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000103', NOW()), -- user -> 部门经理
    ('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000104', NOW()); -- test -> 普通员工

-- =============================================
-- 9. 初始化字典类型
-- =============================================
INSERT INTO sys_dict_type (id, dict_name, dict_type, status, remark, creator, created_at, updated_at, deleted)
VALUES
    ('00000000-0000-0000-0000-000000000701', '用户性别', 'sys_user_gender', 1, '用户性别列表', NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000702', '菜单类型', 'sys_menu_type', 1, '菜单类型列表', NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000703', '系统状态', 'sys_status', 1, '系统通用状态', NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000704', '通知类型', 'sys_notice_type', 1, '通知公告类型', NULL, NOW(), NOW(), FALSE);

-- =============================================
-- 10. 初始化字典数据
-- =============================================
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, dict_sort, list_class, is_default, status, creator, created_at, updated_at, deleted)
VALUES
    -- 用户性别
    ('00000000-0000-0000-0000-000000000801', 'sys_user_gender', '未知', '0', 1, 'info', TRUE, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000802', 'sys_user_gender', '男', '1', 2, 'primary', FALSE, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000803', 'sys_user_gender', '女', '2', 3, 'danger', FALSE, 1, NULL, NOW(), NOW(), FALSE),

    -- 菜单类型
    ('00000000-0000-0000-0000-000000000804', 'sys_menu_type', '目录', '1', 1, 'primary', FALSE, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000805', 'sys_menu_type', '菜单', '2', 2, 'success', FALSE, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000806', 'sys_menu_type', '按钮', '3', 3, 'warning', FALSE, 1, NULL, NOW(), NOW(), FALSE),

    -- 系统状态
    ('00000000-0000-0000-0000-000000000807', 'sys_status', '禁用', '0', 1, 'danger', FALSE, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000808', 'sys_status', '启用', '1', 2, 'success', TRUE, 1, NULL, NOW(), NOW(), FALSE),

    -- 通知类型
    ('00000000-0000-0000-0000-000000000809', 'sys_notice_type', '通知', '1', 1, 'warning', FALSE, 1, NULL, NOW(), NOW(), FALSE),
    ('00000000-0000-0000-0000-000000000810', 'sys_notice_type', '公告', '2', 2, 'success', FALSE, 1, NULL, NOW(), NOW(), FALSE);
