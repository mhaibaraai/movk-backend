-- =============================================
-- 性能优化索引 V3
-- 针对高频查询场景添加复合索引
-- Author: yixuanmiao
-- Date: 2025-12-11
-- =============================================

-- =============================================
-- 1. 权限查询优化
-- 高频场景：根据角色编码列表查询菜单权限
-- =============================================

-- 角色菜单关联查询优化（用于权限校验）
CREATE INDEX idx_role_menu_role_menu ON sys_role_menu(role_id, menu_id);

-- 用户角色关联查询优化（用于获取用户角色列表）
CREATE INDEX idx_user_role_user_role ON sys_user_role(user_id, role_id);

-- =============================================
-- 2. 菜单树查询优化
-- 高频场景：构建菜单树、查询可见菜单
-- =============================================

-- 菜单树构建优化（按父ID和排序查询）
CREATE INDEX idx_menu_tree ON sys_menu(parent_id, order_num) WHERE NOT deleted AND status = 1;

-- 菜单类型筛选（按钮权限查询）
CREATE INDEX idx_menu_type_status ON sys_menu(menu_type, status) WHERE NOT deleted;

-- =============================================
-- 3. 用户查询优化
-- 高频场景：用户列表分页、按部门筛选
-- =============================================

-- 用户列表按部门和状态筛选
CREATE INDEX idx_user_dept_status ON sys_user(dept_id, status) WHERE NOT deleted;

-- 用户登录查询优化（按用户名查询）
CREATE INDEX idx_user_login ON sys_user(username, status) WHERE NOT deleted;

-- =============================================
-- 4. 部门查询优化
-- 高频场景：部门树构建、子部门查询
-- =============================================

-- 部门树构建优化
CREATE INDEX idx_dept_tree ON sys_dept(parent_id, order_num) WHERE NOT deleted AND status = 1;

-- 部门祖先路径查询（用于数据权限过滤）
CREATE INDEX idx_dept_ancestors ON sys_dept(ancestors) WHERE NOT deleted;

-- =============================================
-- 5. 日志查询优化
-- 高频场景：日志列表分页查询、按时间范围筛选
-- =============================================

-- 操作日志时间范围查询
CREATE INDEX idx_operate_log_time_range ON sys_operate_log(created_at DESC, user_id);

-- 操作日志按模块和状态筛选
CREATE INDEX idx_operate_log_filter ON sys_operate_log(module, status, created_at DESC);

-- 登录日志时间范围查询
CREATE INDEX idx_login_log_time_range ON sys_login_log(created_at DESC, user_id);

-- 登录日志按用户和状态筛选
CREATE INDEX idx_login_log_filter ON sys_login_log(username, status, created_at DESC);

-- =============================================
-- 6. 字典查询优化
-- 高频场景：根据字典类型查询数据
-- =============================================

-- 字典数据按类型和排序查询
CREATE INDEX idx_dict_data_type_sort ON sys_dict_data(dict_type, dict_sort) WHERE NOT deleted AND status = 1;

-- =============================================
-- 7. 在线用户查询优化
-- =============================================

-- 在线用户会话查询
CREATE INDEX idx_online_user_session ON sys_online_user(session_id, user_id);

-- =============================================
-- 8. 统计分析优化（可选，用于仪表盘）
-- =============================================

-- 按状态统计用户数
CREATE INDEX idx_user_status_count ON sys_user(status) WHERE NOT deleted;

-- 按状态统计角色数
CREATE INDEX idx_role_status_count ON sys_role(status) WHERE NOT deleted;

-- 操作日志按时间排序（用于按日期统计）
CREATE INDEX idx_operate_log_created ON sys_operate_log(created_at DESC, status);

-- 登录日志按时间排序（用于按日期统计）
CREATE INDEX idx_login_log_created ON sys_login_log(created_at DESC, status);

-- =============================================
-- 9. 数据权限查询优化
-- =============================================

-- 角色数据权限查询
CREATE INDEX idx_role_data_scope ON sys_role(data_scope, status) WHERE NOT deleted;
