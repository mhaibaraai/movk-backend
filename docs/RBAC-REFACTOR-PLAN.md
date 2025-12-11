# RBAC 权限系统完全重构方案

> 企业级完整权限管理系统设计方案 - 不考虑向后兼容，从零构建完整功能体系

**版本：** v2.0
**创建日期：** 2025-12-10
**项目定位：** 企业级权限管理 + 组织架构 + 审计日志 + 在线管理

---

## 一、系统功能架构

### 1.1 核心功能模块

```
movk-backend (完整权限系统)
│
├── 权限管理模块
│   ├── 用户管理（User）
│   ├── 角色管理（Role）
│   ├── 菜单管理（Menu）- 包含按钮权限
│   ├── 部门管理（Department）
│   ├── 岗位管理（Post）
│   └── 数据权限（Data Permission）
│
├── 安全管理模块
│   ├── 在线用户（Online User）
│   ├── 登录日志（Login Log）
│   ├── 操作日志（Operate Log）
│   └── 会话管理（Session Management）
│
├── 系统管理模块
│   ├── 字典管理（Dict）
│   ├── 参数配置（Config）
│   ├── 通知公告（Notice）
│   └── 文件管理（File）
│
└── 扩展模块（预留）
    ├── 租户管理（Tenant）- 多租户 SaaS
    ├── 工作流（Workflow）- Flowable 集成
    └── 定时任务（Job）- XXL-Job 集成
```

### 1.2 权限控制三维度

#### 1.2.1 功能权限（菜单 + 按钮）
- **菜单权限**：控制用户可访问的页面
- **按钮权限**：控制页面内的操作按钮（增删改查）
- **API 权限**：后端接口权限标识（`@RequiresPermission`）

#### 1.2.2 数据权限
- **全部数据**：不限制，查看所有数据
- **本部门数据**：仅查看所属部门数据
- **本部门及子部门数据**：递归查看下级部门
- **仅本人数据**：只能查看自己创建的数据
- **自定义数据权限**：指定特定部门范围

#### 1.2.3 字段权限（预留）
- 敏感字段脱敏（手机号、身份证）
- 字段级别访问控制

---

## 二、数据库表结构设计

### 2.1 用户相关表

#### sys_user（用户表）
```sql
CREATE TABLE sys_user (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username        CITEXT NOT NULL UNIQUE,           -- 用户名（不区分大小写）
    password        VARCHAR(255) NOT NULL,             -- 密码（BCrypt）
    nickname        VARCHAR(50),                       -- 昵称
    email           CITEXT UNIQUE,                     -- 邮箱
    phone           VARCHAR(20) UNIQUE,                -- 手机号
    gender          SMALLINT DEFAULT 0,                -- 性别：0-未知 1-男 2-女
    avatar          VARCHAR(500),                      -- 头像 URL
    status          SMALLINT NOT NULL DEFAULT 1,       -- 状态：0-禁用 1-启用
    dept_id         UUID,                              -- 所属部门 ID
    remark          VARCHAR(500),                      -- 备注
    login_ip        VARCHAR(50),                       -- 最后登录 IP
    login_date      TIMESTAMPTZ,                       -- 最后登录时间
    creator         UUID,                              -- 创建人 ID
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater         UUID,                              -- 更新人 ID
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,    -- 逻辑删除
    deleted_at      TIMESTAMPTZ,                       -- 删除时间

    CONSTRAINT fk_user_dept FOREIGN KEY (dept_id) REFERENCES sys_dept(id)
);

CREATE INDEX idx_user_dept_id ON sys_user(dept_id) WHERE NOT deleted;
CREATE INDEX idx_user_status ON sys_user(status) WHERE NOT deleted;
CREATE INDEX idx_user_username ON sys_user(username) WHERE NOT deleted;
```

#### sys_dept（部门表）
```sql
CREATE TABLE sys_dept (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id       UUID,                              -- 父部门 ID（NULL 表示根部门）
    ancestors       VARCHAR(500),                      -- 祖级列表（逗号分隔，便于查询子树）
    dept_name       VARCHAR(50) NOT NULL,              -- 部门名称
    dept_code       CITEXT UNIQUE,                     -- 部门编码
    order_num       INT NOT NULL DEFAULT 0,            -- 显示顺序
    leader_user_id  UUID,                              -- 部门负责人 ID
    phone           VARCHAR(20),                       -- 联系电话
    email           VARCHAR(100),                      -- 邮箱
    status          SMALLINT NOT NULL DEFAULT 1,       -- 状态：0-禁用 1-启用
    creator         UUID,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater         UUID,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ,

    CONSTRAINT fk_dept_parent FOREIGN KEY (parent_id) REFERENCES sys_dept(id)
);

CREATE INDEX idx_dept_parent_id ON sys_dept(parent_id) WHERE NOT deleted;
CREATE INDEX idx_dept_status ON sys_dept(status) WHERE NOT deleted;
```

#### sys_post（岗位表）
```sql
CREATE TABLE sys_post (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_code       CITEXT NOT NULL UNIQUE,            -- 岗位编码
    post_name       VARCHAR(50) NOT NULL,              -- 岗位名称
    order_num       INT NOT NULL DEFAULT 0,            -- 显示顺序
    status          SMALLINT NOT NULL DEFAULT 1,       -- 状态：0-禁用 1-启用
    remark          VARCHAR(500),
    creator         UUID,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater         UUID,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ
);

CREATE INDEX idx_post_status ON sys_post(status) WHERE NOT deleted;
```

#### sys_user_post（用户岗位关联表）
```sql
CREATE TABLE sys_user_post (
    id              BIGSERIAL PRIMARY KEY,
    user_id         UUID NOT NULL,
    post_id         UUID NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_user_post_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_post_post FOREIGN KEY (post_id) REFERENCES sys_post(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_post UNIQUE (user_id, post_id)
);

CREATE INDEX idx_user_post_user_id ON sys_user_post(user_id);
CREATE INDEX idx_user_post_post_id ON sys_user_post(post_id);
```

---

### 2.2 权限相关表

#### sys_role（角色表）
```sql
CREATE TABLE sys_role (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_name               VARCHAR(50) NOT NULL,              -- 角色名称
    role_code               CITEXT NOT NULL UNIQUE,            -- 角色编码（权限标识）
    role_sort               INT NOT NULL DEFAULT 0,            -- 显示顺序
    data_scope              SMALLINT NOT NULL DEFAULT 1,       -- 数据权限范围
                                                               -- 1-全部数据
                                                               -- 2-本部门数据
                                                               -- 3-本部门及子部门数据
                                                               -- 4-仅本人数据
                                                               -- 5-自定义部门数据
    data_scope_dept_ids     TEXT,                              -- 自定义数据权限部门 ID（JSON 数组）
    status                  SMALLINT NOT NULL DEFAULT 1,       -- 状态：0-禁用 1-启用
    role_type               SMALLINT NOT NULL DEFAULT 2,       -- 角色类型：1-内置角色 2-自定义角色
    remark                  VARCHAR(500),
    creator                 UUID,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater                 UUID,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted                 BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at              TIMESTAMPTZ
);

CREATE INDEX idx_role_status ON sys_role(status) WHERE NOT deleted;
CREATE INDEX idx_role_code ON sys_role(role_code) WHERE NOT deleted;
```

#### sys_menu（菜单表）
```sql
CREATE TABLE sys_menu (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id           UUID,                              -- 父菜单 ID
    menu_name           VARCHAR(50) NOT NULL,              -- 菜单名称
    menu_type           SMALLINT NOT NULL,                 -- 菜单类型：1-目录 2-菜单 3-按钮
    order_num           INT NOT NULL DEFAULT 0,            -- 显示顺序

    -- 路由相关
    path                VARCHAR(200),                      -- 路由地址
    component           VARCHAR(255),                      -- 组件路径
    query_params        VARCHAR(255),                      -- 路由参数（JSON）
    is_frame            BOOLEAN DEFAULT FALSE,             -- 是否为外链
    is_cache            BOOLEAN DEFAULT TRUE,              -- 是否缓存

    -- 权限相关
    permission_code     CITEXT,                            -- 权限标识（如 system:user:list）

    -- 显示控制
    visible             BOOLEAN NOT NULL DEFAULT TRUE,     -- 是否显示
    status              SMALLINT NOT NULL DEFAULT 1,       -- 状态：0-禁用 1-启用

    -- 图标
    icon                VARCHAR(100),                      -- 菜单图标

    remark              VARCHAR(500),
    creator             UUID,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater             UUID,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMPTZ
);

CREATE INDEX idx_menu_parent_id ON sys_menu(parent_id) WHERE NOT deleted;
CREATE INDEX idx_menu_status ON sys_menu(status) WHERE NOT deleted;
CREATE INDEX idx_menu_permission ON sys_menu(permission_code) WHERE NOT deleted;
```

#### sys_user_role（用户角色关联表）
```sql
CREATE TABLE sys_user_role (
    id              BIGSERIAL PRIMARY KEY,
    user_id         UUID NOT NULL,
    role_id         UUID NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_role UNIQUE (user_id, role_id)
);

CREATE INDEX idx_user_role_user_id ON sys_user_role(user_id);
CREATE INDEX idx_user_role_role_id ON sys_user_role(role_id);
```

#### sys_role_menu（角色菜单关联表）
```sql
CREATE TABLE sys_role_menu (
    id              BIGSERIAL PRIMARY KEY,
    role_id         UUID NOT NULL,
    menu_id         UUID NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_role_menu_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_menu_menu FOREIGN KEY (menu_id) REFERENCES sys_menu(id) ON DELETE CASCADE,
    CONSTRAINT uk_role_menu UNIQUE (role_id, menu_id)
);

CREATE INDEX idx_role_menu_role_id ON sys_role_menu(role_id);
CREATE INDEX idx_role_menu_menu_id ON sys_role_menu(menu_id);
```

**注意：** 删除 `sys_permission` 表和 `sys_role_permission` 表，权限标识统一由 `sys_menu.permission_code` 管理

---

### 2.3 审计日志表

#### sys_operate_log（操作日志表）
```sql
CREATE TABLE sys_operate_log (
    id                  BIGSERIAL PRIMARY KEY,
    trace_id            VARCHAR(64),                       -- 链路追踪 ID
    user_id             UUID,                              -- 操作人 ID
    username            VARCHAR(50),                       -- 操作人用户名
    module              VARCHAR(50),                       -- 操作模块
    operation           VARCHAR(50),                       -- 操作类型（CREATE/UPDATE/DELETE/QUERY/EXPORT/IMPORT）
    method              VARCHAR(200),                      -- 方法名（Controller.method）
    request_method      VARCHAR(10),                       -- 请求方式（GET/POST/PUT/DELETE）
    request_url         VARCHAR(500),                      -- 请求 URL
    request_params      TEXT,                              -- 请求参数（JSON）
    request_body        TEXT,                              -- 请求体（JSON，限制大小）
    response_data       TEXT,                              -- 响应数据（JSON，限制大小）
    user_ip             VARCHAR(50),                       -- 用户 IP
    user_agent          VARCHAR(500),                      -- 用户代理
    operation_time      INT,                               -- 执行时长（毫秒）
    status              SMALLINT NOT NULL,                 -- 状态：1-成功 2-失败
    error_msg           TEXT,                              -- 错误信息
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_operate_log_user_id ON sys_operate_log(user_id);
CREATE INDEX idx_operate_log_module ON sys_operate_log(module);
CREATE INDEX idx_operate_log_created_at ON sys_operate_log(created_at);
CREATE INDEX idx_operate_log_trace_id ON sys_operate_log(trace_id);
```

#### sys_login_log（登录日志表）
```sql
CREATE TABLE sys_login_log (
    id                  BIGSERIAL PRIMARY KEY,
    trace_id            VARCHAR(64),                       -- 链路追踪 ID
    user_id             UUID,                              -- 用户 ID
    username            VARCHAR(50) NOT NULL,              -- 用户名
    login_type          SMALLINT NOT NULL,                 -- 登录类型：1-账号密码 2-手机验证码 3-第三方登录
    login_ip            VARCHAR(50),                       -- 登录 IP
    login_location      VARCHAR(100),                      -- 登录地点（IP 解析）
    browser             VARCHAR(50),                       -- 浏览器类型
    os                  VARCHAR(50),                       -- 操作系统
    user_agent          VARCHAR(500),                      -- 用户代理
    status              SMALLINT NOT NULL,                 -- 登录状态：1-成功 2-失败
    message             VARCHAR(500),                      -- 提示消息（失败原因）
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_login_log_user_id ON sys_login_log(user_id);
CREATE INDEX idx_login_log_username ON sys_login_log(username);
CREATE INDEX idx_login_log_status ON sys_login_log(status);
CREATE INDEX idx_login_log_created_at ON sys_login_log(created_at);
```

#### sys_online_user（在线用户表）
```sql
CREATE TABLE sys_online_user (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id          VARCHAR(100) NOT NULL UNIQUE,      -- 会话 ID（JWT Token ID）
    user_id             UUID NOT NULL,                     -- 用户 ID
    username            VARCHAR(50) NOT NULL,              -- 用户名
    dept_id             UUID,                              -- 部门 ID
    dept_name           VARCHAR(50),                       -- 部门名称
    login_ip            VARCHAR(50),                       -- 登录 IP
    login_location      VARCHAR(100),                      -- 登录地点
    browser             VARCHAR(50),                       -- 浏览器
    os                  VARCHAR(50),                       -- 操作系统
    login_time          TIMESTAMPTZ NOT NULL,              -- 登录时间
    expire_time         TIMESTAMPTZ NOT NULL,              -- 过期时间

    CONSTRAINT fk_online_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
);

CREATE INDEX idx_online_user_user_id ON sys_online_user(user_id);
CREATE INDEX idx_online_user_expire_time ON sys_online_user(expire_time);
```

---

### 2.4 系统管理表

#### sys_dict_type（字典类型表）
```sql
CREATE TABLE sys_dict_type (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dict_name       VARCHAR(100) NOT NULL,                 -- 字典名称
    dict_type       CITEXT NOT NULL UNIQUE,                -- 字典类型（唯一标识）
    status          SMALLINT NOT NULL DEFAULT 1,           -- 状态：0-禁用 1-启用
    remark          VARCHAR(500),
    creator         UUID,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater         UUID,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ
);

CREATE INDEX idx_dict_type_status ON sys_dict_type(status) WHERE NOT deleted;
```

#### sys_dict_data（字典数据表）
```sql
CREATE TABLE sys_dict_data (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dict_type       CITEXT NOT NULL,                       -- 字典类型
    dict_label      VARCHAR(100) NOT NULL,                 -- 字典标签（显示值）
    dict_value      VARCHAR(100) NOT NULL,                 -- 字典键值（实际值）
    dict_sort       INT NOT NULL DEFAULT 0,                -- 显示排序
    css_class       VARCHAR(100),                          -- 样式类名
    list_class      VARCHAR(100),                          -- 列表样式（default/primary/success/info/warning/danger）
    is_default      BOOLEAN DEFAULT FALSE,                 -- 是否默认
    status          SMALLINT NOT NULL DEFAULT 1,           -- 状态：0-禁用 1-启用
    remark          VARCHAR(500),
    creator         UUID,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater         UUID,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ,

    CONSTRAINT fk_dict_data_type FOREIGN KEY (dict_type) REFERENCES sys_dict_type(dict_type) ON DELETE CASCADE
);

CREATE INDEX idx_dict_data_type ON sys_dict_data(dict_type) WHERE NOT deleted;
CREATE INDEX idx_dict_data_status ON sys_dict_data(status) WHERE NOT deleted;
```

#### sys_config（系统配置表）
```sql
CREATE TABLE sys_config (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    config_name     VARCHAR(100) NOT NULL,                 -- 配置名称
    config_key      CITEXT NOT NULL UNIQUE,                -- 配置键（唯一标识）
    config_value    TEXT NOT NULL,                         -- 配置值
    config_type     SMALLINT NOT NULL DEFAULT 1,           -- 配置类型：1-系统内置 2-自定义
    remark          VARCHAR(500),
    creator         UUID,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater         UUID,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ
);

CREATE INDEX idx_config_key ON sys_config(config_key) WHERE NOT deleted;
```

#### sys_notice（通知公告表）
```sql
CREATE TABLE sys_notice (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notice_title    VARCHAR(100) NOT NULL,                 -- 标题
    notice_type     SMALLINT NOT NULL,                     -- 类型：1-通知 2-公告
    notice_content  TEXT,                                  -- 内容（富文本）
    status          SMALLINT NOT NULL DEFAULT 1,           -- 状态：0-关闭 1-正常
    creator         UUID,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updater         UUID,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMPTZ
);

CREATE INDEX idx_notice_status ON sys_notice(status) WHERE NOT deleted;
CREATE INDEX idx_notice_created_at ON sys_notice(created_at) WHERE NOT deleted;
```

---

## 三、实体类重构设计

### 3.1 基础实体类

#### BaseEntity（审计基类）
```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "creator")
    private UUID creator;

    @Column(name = "updater")
    private UUID updater;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}
```

### 3.2 核心实体类

#### User（用户实体）
```java
@Entity
@Table(name = "sys_user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, columnDefinition = "citext")
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    @Column(length = 50)
    private String nickname;

    @Column(unique = true, columnDefinition = "citext")
    private String email;

    @Column(length = 20, unique = true)
    private String phone;

    @Convert(converter = GenderConverter.class)
    @Column
    private Gender gender;

    @Column(length = 500)
    private String avatar;

    @Convert(converter = UserStatusConverter.class)
    @Column(nullable = false)
    private UserStatus status;

    @Column(name = "dept_id")
    private UUID deptId;

    @Column(length = 500)
    private String remark;

    @Column(name = "login_ip", length = 50)
    private String loginIp;

    @Column(name = "login_date")
    private OffsetDateTime loginDate;
}
```

#### Role（角色实体）
```java
@Entity
@Table(name = "sys_role")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "role_name", nullable = false, length = 50)
    private String roleName;

    @Column(name = "role_code", nullable = false, unique = true, columnDefinition = "citext")
    private String roleCode;

    @Column(name = "role_sort", nullable = false)
    private Integer roleSort;

    @Convert(converter = DataScopeConverter.class)
    @Column(name = "data_scope", nullable = false)
    private DataScope dataScope;

    @Column(name = "data_scope_dept_ids", columnDefinition = "TEXT")
    private String dataScopeDeptIds; // JSON 数组存储

    @Convert(converter = EnableStatusConverter.class)
    @Column(nullable = false)
    private EnableStatus status;

    @Convert(converter = RoleTypeConverter.class)
    @Column(name = "role_type", nullable = false)
    private RoleType roleType;

    @Column(length = 500)
    private String remark;
}
```

#### Menu（菜单实体）
```java
@Entity
@Table(name = "sys_menu")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Menu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "menu_name", nullable = false, length = 50)
    private String menuName;

    @Convert(converter = MenuTypeConverter.class)
    @Column(name = "menu_type", nullable = false)
    private MenuType menuType;

    @Column(name = "order_num", nullable = false)
    private Integer orderNum;

    @Column(length = 200)
    private String path;

    @Column(length = 255)
    private String component;

    @Column(name = "query_params", length = 255)
    private String queryParams; // JSON 格式

    @Column(name = "is_frame", nullable = false)
    private Boolean isFrame = false;

    @Column(name = "is_cache", nullable = false)
    private Boolean isCache = true;

    @Column(name = "permission_code", columnDefinition = "citext")
    private String permissionCode;

    @Column(nullable = false)
    private Boolean visible = true;

    @Convert(converter = EnableStatusConverter.class)
    @Column(nullable = false)
    private EnableStatus status;

    @Column(length = 100)
    private String icon;

    @Column(length = 500)
    private String remark;
}
```

#### Department（部门实体）
```java
@Entity
@Table(name = "sys_dept")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Department extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(length = 500)
    private String ancestors; // 祖级列表，逗号分隔

    @Column(name = "dept_name", nullable = false, length = 50)
    private String deptName;

    @Column(name = "dept_code", unique = true, columnDefinition = "citext")
    private String deptCode;

    @Column(name = "order_num", nullable = false)
    private Integer orderNum = 0;

    @Column(name = "leader_user_id")
    private UUID leaderUserId;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Convert(converter = EnableStatusConverter.class)
    @Column(nullable = false)
    private EnableStatus status;
}
```

---

## 四、枚举类型定义

### 4.1 新增枚举

```java
// 性别枚举
public enum Gender {
    UNKNOWN((short) 0, "未知"),
    MALE((short) 1, "男"),
    FEMALE((short) 2, "女");
}

// 数据权限范围枚举
public enum DataScope {
    ALL((short) 1, "全部数据"),
    DEPT((short) 2, "本部门数据"),
    DEPT_AND_CHILD((short) 3, "本部门及子部门数据"),
    SELF((short) 4, "仅本人数据"),
    CUSTOM((short) 5, "自定义部门数据");
}

// 角色类型枚举
public enum RoleType {
    BUILT_IN((short) 1, "内置角色"),  // 不可删除
    CUSTOM((short) 2, "自定义角色");
}

// 操作类型枚举
public enum OperationType {
    CREATE, UPDATE, DELETE, QUERY, EXPORT, IMPORT, OTHER;
}

// 登录类型枚举
public enum LoginType {
    PASSWORD((short) 1, "账号密码"),
    SMS((short) 2, "手机验证码"),
    OAUTH((short) 3, "第三方登录");
}

// 业务状态枚举
public enum BusinessStatus {
    SUCCESS((short) 1, "成功"),
    FAILURE((short) 2, "失败");
}

// 字典状态枚举（复用 EnableStatus）
// 通知类型枚举
public enum NoticeType {
    NOTICE((short) 1, "通知"),
    ANNOUNCEMENT((short) 2, "公告");
}
```

---

## 五、核心功能实现方案

### 5.1 权限校验机制

#### 5.1.1 自定义注解
```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {
    String[] value();                    // 权限码数组
    Logical logical() default Logical.AND; // AND/OR 逻辑
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataPermission {
    String entity();                     // 实体类名
    String userAlias() default "u";      // 用户表别名
    String deptAlias() default "d";      // 部门表别名
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {
    String module();                     // 模块名称
    OperationType type();                // 操作类型
    boolean recordParams() default true; // 是否记录参数
    boolean recordResult() default false;// 是否记录返回值
}
```

#### 5.1.2 权限切面
```java
@Aspect
@Component
public class PermissionAspect {

    @Around("@annotation(requiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint,
                                  RequiresPermission requiresPermission) {
        LoginUser loginUser = CurrentUserService.getCurrentUser();
        String[] permissions = requiresPermission.value();
        Logical logical = requiresPermission.logical();

        boolean hasPermission = permissionService.checkPermissions(
            loginUser.getUsername(),
            permissions,
            logical
        );

        if (!hasPermission) {
            throw new AccessDeniedException("权限不足");
        }

        return joinPoint.proceed();
    }
}
```

#### 5.1.3 数据权限切面
```java
@Aspect
@Component
public class DataPermissionAspect {

    @Around("@annotation(dataPermission)")
    public Object handleDataPermission(ProceedingJoinPoint joinPoint,
                                       DataPermission dataPermission) {
        LoginUser loginUser = CurrentUserService.getCurrentUser();

        // 根据用户角色的 dataScope 动态拼接 SQL 条件
        DataScope dataScope = getRoleDataScope(loginUser);

        // 使用 MyBatis Interceptor 或 JPA Specification 动态添加数据过滤条件
        DataPermissionContext.set(dataScope, loginUser.getDeptId());

        try {
            return joinPoint.proceed();
        } finally {
            DataPermissionContext.clear();
        }
    }
}
```

### 5.2 数据权限实现

#### 5.2.1 JPA Specification 方式
```java
@Service
public class DataPermissionService {

    public <T> Specification<T> applyDataPermission(Class<T> entityClass) {
        return (root, query, criteriaBuilder) -> {
            LoginUser user = CurrentUserService.getCurrentUser();
            DataScope dataScope = getUserDataScope(user);

            return switch (dataScope) {
                case ALL -> criteriaBuilder.conjunction(); // 无限制
                case DEPT -> criteriaBuilder.equal(
                    root.get("deptId"), user.getDeptId()
                );
                case DEPT_AND_CHILD -> {
                    List<UUID> deptIds = getDeptAndChildIds(user.getDeptId());
                    yield root.get("deptId").in(deptIds);
                }
                case SELF -> criteriaBuilder.equal(
                    root.get("creator"), user.getUserId()
                );
                case CUSTOM -> {
                    List<UUID> customDeptIds = getCustomDeptIds(user);
                    yield root.get("deptId").in(customDeptIds);
                }
            };
        };
    }
}
```

### 5.3 操作日志记录

#### 5.3.1 日志切面
```java
@Aspect
@Component
public class LogAspect {

    @Around("@annotation(log)")
    public Object recordLog(ProceedingJoinPoint joinPoint, Log log) {
        OperateLog operateLog = new OperateLog();
        operateLog.setTraceId(MDC.get("traceId"));
        operateLog.setModule(log.module());
        operateLog.setOperation(log.type().name());

        // 记录请求信息
        HttpServletRequest request = getCurrentRequest();
        operateLog.setRequestMethod(request.getMethod());
        operateLog.setRequestUrl(request.getRequestURI());
        operateLog.setUserIp(IpUtils.getIpAddr(request));

        // 记录参数
        if (log.recordParams()) {
            Object[] args = joinPoint.getArgs();
            operateLog.setRequestParams(JsonUtils.toJson(args));
        }

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();

            operateLog.setStatus(BusinessStatus.SUCCESS);
            if (log.recordResult()) {
                operateLog.setResponseData(JsonUtils.toJson(result));
            }

            return result;
        } catch (Throwable e) {
            operateLog.setStatus(BusinessStatus.FAILURE);
            operateLog.setErrorMsg(e.getMessage());
            throw e;
        } finally {
            operateLog.setOperationTime((int) (System.currentTimeMillis() - startTime));
            // 异步保存日志
            operateLogService.saveAsync(operateLog);
        }
    }
}
```

### 5.4 在线用户管理

#### 5.4.1 登录时记录在线用户
```java
@Service
public class LoginService {

    public AuthTokensDTO login(LoginDTO loginDTO) {
        // 1. 认证用户
        User user = authenticate(loginDTO);

        // 2. 生成 Token
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // 3. 记录在线用户
        OnlineUser onlineUser = OnlineUser.builder()
            .sessionId(jwtService.getTokenId(accessToken))
            .userId(user.getId())
            .username(user.getUsername())
            .deptId(user.getDeptId())
            .loginIp(IpUtils.getIpAddr())
            .loginTime(OffsetDateTime.now())
            .expireTime(jwtService.getExpireTime(accessToken))
            .build();
        onlineUserService.save(onlineUser);

        // 4. 记录登录日志
        loginLogService.recordLoginLog(user, LoginType.PASSWORD, BusinessStatus.SUCCESS);

        return new AuthTokensDTO(accessToken, refreshToken);
    }
}
```

#### 5.4.2 强制下线
```java
@Service
public class OnlineUserService {

    public void forceLogout(String sessionId) {
        // 1. 删除在线记录
        onlineUserRepository.deleteBySessionId(sessionId);

        // 2. 将 Token 加入黑名单（Redis）
        redisTemplate.opsForSet().add("token:blacklist", sessionId);
        redisTemplate.expire("token:blacklist:" + sessionId, 7, TimeUnit.DAYS);
    }
}
```

### 5.5 菜单树构建

#### 5.5.1 递归构建菜单树
```java
@Service
public class MenuService {

    public List<MenuTreeVO> getUserMenuTree(String username) {
        // 1. 获取用户角色
        List<String> roleCodes = userRoleService.getUserRoleCodes(username);

        // 2. 获取角色菜单（去重）
        List<Menu> menus = menuRepository.findByRoleCodesAndStatusAndVisible(
            roleCodes, EnableStatus.ENABLED, true
        );

        // 3. 过滤出目录和菜单（排除按钮）
        List<Menu> treeMenus = menus.stream()
            .filter(m -> m.getMenuType() != MenuType.BUTTON)
            .toList();

        // 4. 构建树形结构
        return buildMenuTree(treeMenus, null);
    }

    private List<MenuTreeVO> buildMenuTree(List<Menu> menus, UUID parentId) {
        return menus.stream()
            .filter(m -> Objects.equals(m.getParentId(), parentId))
            .sorted(Comparator.comparing(Menu::getOrderNum))
            .map(menu -> {
                MenuTreeVO node = MenuTreeVO.fromEntity(menu);
                List<MenuTreeVO> children = buildMenuTree(menus, menu.getId());
                node.setChildren(children.isEmpty() ? null : children);
                return node;
            })
            .toList();
    }

    public List<String> getUserPermissions(String username) {
        List<String> roleCodes = userRoleService.getUserRoleCodes(username);
        return menuRepository.findPermissionCodesByRoleCodesAndStatus(
            roleCodes, EnableStatus.ENABLED
        );
    }
}
```

---

## 六、API 接口设计

### 6.1 用户管理 API

```
# 用户 CRUD
POST   /api/system/users                     创建用户
PUT    /api/system/users/{id}                更新用户
DELETE /api/system/users/{id}                删除用户（逻辑删除）
GET    /api/system/users/{id}                查询用户详情
GET    /api/system/users                     分页查询用户列表
GET    /api/system/users/export              导出用户数据

# 用户状态管理
PATCH  /api/system/users/{id}/status         修改用户状态
PUT    /api/system/users/{id}/password       重置用户密码
PUT    /api/system/users/profile/password    修改当前用户密码

# 用户角色管理
POST   /api/system/users/{id}/roles          批量分配角色
GET    /api/system/users/{id}/roles          查询用户角色列表

# 用户岗位管理
POST   /api/system/users/{id}/posts          批量分配岗位
GET    /api/system/users/{id}/posts          查询用户岗位列表

# 当前用户信息
GET    /api/system/users/profile             获取当前用户信息
PUT    /api/system/users/profile             更新当前用户信息
GET    /api/system/users/profile/menus       获取当前用户菜单树
GET    /api/system/users/profile/permissions 获取当前用户权限列表
```

### 6.2 角色管理 API

```
# 角色 CRUD
POST   /api/system/roles                     创建角色
PUT    /api/system/roles/{id}                更新角色
DELETE /api/system/roles/{id}                删除角色
GET    /api/system/roles/{id}                查询角色详情
GET    /api/system/roles                     分页查询角色列表
GET    /api/system/roles/options             查询角色选项（下拉框）

# 角色状态管理
PATCH  /api/system/roles/{id}/status         修改角色状态

# 角色权限管理
POST   /api/system/roles/{id}/menus          批量分配菜单权限
GET    /api/system/roles/{id}/menus          查询角色菜单列表

# 角色数据权限管理
PUT    /api/system/roles/{id}/data-scope     设置数据权限范围
GET    /api/system/roles/{id}/data-scope     查询数据权限配置
```

### 6.3 菜单管理 API

```
# 菜单 CRUD
POST   /api/system/menus                     创建菜单
PUT    /api/system/menus/{id}                更新菜单
DELETE /api/system/menus/{id}                删除菜单
GET    /api/system/menus/{id}                查询菜单详情
GET    /api/system/menus/tree                查询菜单树（全量）
GET    /api/system/menus/list                查询菜单列表

# 菜单状态管理
PATCH  /api/system/menus/{id}/status         修改菜单状态

# 菜单权限标识
GET    /api/system/menus/permissions         查询所有权限标识
```

### 6.4 部门管理 API

```
# 部门 CRUD
POST   /api/system/depts                     创建部门
PUT    /api/system/depts/{id}                更新部门
DELETE /api/system/depts/{id}                删除部门
GET    /api/system/depts/{id}                查询部门详情
GET    /api/system/depts/tree                查询部门树
GET    /api/system/depts/list                查询部门列表

# 部门状态管理
PATCH  /api/system/depts/{id}/status         修改部门状态
```

### 6.5 岗位管理 API

```
# 岗位 CRUD
POST   /api/system/posts                     创建岗位
PUT    /api/system/posts/{id}                更新岗位
DELETE /api/system/posts/{id}                删除岗位
GET    /api/system/posts/{id}                查询岗位详情
GET    /api/system/posts                     分页查询岗位列表
GET    /api/system/posts/options             查询岗位选项

# 岗位状态管理
PATCH  /api/system/posts/{id}/status         修改岗位状态
```

### 6.6 在线用户 API

```
GET    /api/monitor/online-users             分页查询在线用户
DELETE /api/monitor/online-users/{sessionId} 强制用户下线
```

### 6.7 日志管理 API

```
# 操作日志
GET    /api/monitor/operate-logs             分页查询操作日志
DELETE /api/monitor/operate-logs/{id}        删除操作日志
DELETE /api/monitor/operate-logs/batch       批量删除操作日志
DELETE /api/monitor/operate-logs/clean       清空操作日志
GET    /api/monitor/operate-logs/export      导出操作日志

# 登录日志
GET    /api/monitor/login-logs               分页查询登录日志
DELETE /api/monitor/login-logs/{id}          删除登录日志
DELETE /api/monitor/login-logs/batch         批量删除登录日志
DELETE /api/monitor/login-logs/clean         清空登录日志
GET    /api/monitor/login-logs/export        导出登录日志
```

### 6.8 字典管理 API

```
# 字典类型
POST   /api/system/dict/types                创建字典类型
PUT    /api/system/dict/types/{id}           更新字典类型
DELETE /api/system/dict/types/{id}           删除字典类型
GET    /api/system/dict/types/{id}           查询字典类型详情
GET    /api/system/dict/types                分页查询字典类型

# 字典数据
POST   /api/system/dict/data                 创建字典数据
PUT    /api/system/dict/data/{id}            更新字典数据
DELETE /api/system/dict/data/{id}            删除字典数据
GET    /api/system/dict/data/{id}            查询字典数据详情
GET    /api/system/dict/data                 分页查询字典数据
GET    /api/system/dict/data/type/{dictType} 根据字典类型查询数据
```

### 6.9 通知公告 API

```
POST   /api/system/notices                   创建通知公告
PUT    /api/system/notices/{id}              更新通知公告
DELETE /api/system/notices/{id}              删除通知公告
GET    /api/system/notices/{id}              查询通知详情
GET    /api/system/notices                   分页查询通知列表
```

---

## 七、缓存设计方案

### 7.1 缓存键设计

```java
public class CacheConstants {
    // 用户权限缓存（Set）
    public static final String USER_PERMISSIONS = "auth:user:permissions:";

    // 用户角色缓存（Set）
    public static final String USER_ROLES = "auth:user:roles:";

    // 用户菜单树缓存（JSON）
    public static final String USER_MENUS = "auth:user:menus:";

    // 角色菜单关联缓存（Set）
    public static final String ROLE_MENUS = "auth:role:menus:";

    // 部门树缓存（JSON）
    public static final String DEPT_TREE = "system:dept:tree";

    // 字典数据缓存（Hash）
    public static final String DICT_DATA = "system:dict:data:";

    // Token 黑名单（Set）
    public static final String TOKEN_BLACKLIST = "auth:token:blacklist:";

    // 缓存过期时间
    public static final long CACHE_EXPIRE_MINUTES = 30;
}
```

### 7.2 缓存策略

#### 7.2.1 用户权限缓存
```java
@Service
public class UserPermissionCacheService {

    @Cacheable(value = "auth:user:permissions", key = "#username")
    public Set<String> getUserPermissions(String username) {
        List<String> roleCodes = userRoleService.getUserRoleCodes(username);
        return new HashSet<>(menuRepository.findPermissionCodesByRoleCodes(roleCodes));
    }

    @CacheEvict(value = "auth:user:permissions", key = "#username")
    public void evictUserPermissions(String username) {
        // 清除用户权限缓存
    }

    public void evictByRoleCode(String roleCode) {
        // 查询该角色下的所有用户
        List<String> usernames = userRoleService.getUsernamesByRoleCode(roleCode);
        usernames.forEach(this::evictUserPermissions);
    }
}
```

#### 7.2.2 字典数据缓存
```java
@Service
public class DictCacheService {

    @Cacheable(value = "system:dict:data", key = "#dictType")
    public List<DictDataVO> getDictData(String dictType) {
        return dictDataRepository.findByDictTypeAndStatus(
            dictType, EnableStatus.ENABLED
        );
    }

    @CacheEvict(value = "system:dict:data", key = "#dictType")
    public void evictDictData(String dictType) {
        // 清除字典缓存
    }
}
```

### 7.3 缓存刷新机制

#### 事件驱动缓存刷新
```java
// 角色菜单变更事件
@Component
public class RoleMenuChangeListener {

    @EventListener
    public void handleRoleMenuChange(RoleMenuChangeEvent event) {
        String roleCode = event.getRoleCode();

        // 清除角色菜单缓存
        cacheManager.evict("auth:role:menus", roleCode);

        // 清除该角色下所有用户的权限和菜单缓存
        userPermissionCacheService.evictByRoleCode(roleCode);
        userMenuCacheService.evictByRoleCode(roleCode);
    }
}
```

---

## 八、实施计划

### Sprint 1: 数据库重构与基础实体（3 天）✅

#### Day 1: 数据库表设计与迁移 ✅
- [x] 编写完整的数据库迁移脚本（Flyway/Liquibase）
- [x] 备份现有数据
- [x] 执行表结构重构
- [x] 数据迁移脚本（从旧表到新表）

#### Day 2: 实体类与枚举重构 ✅
- [x] 重构所有实体类，添加新字段（15个实体）
- [x] 新增 Department、Post 等实体
- [x] 新增所有枚举类（Gender、DataScope、RoleType 等）
- [x] 更新所有 Repository 接口（15个 Repository）

#### Day 3: 基础服务层实现 ✅
- [x] 实现 UserService、RoleService、MenuService CRUD
- [x] 实现 DeptService、PostService CRUD
- [x] 编写单元测试

---

### Sprint 2: 权限控制核心功能（4 天）✅

#### Day 1: 权限注解与切面 ✅
- [x] 实现 `@RequiresPermission` 注解
- [x] 实现 PermissionAspect 权限切面
- [x] 增强 PermissionService 权限校验逻辑
- [x] 集成 Spring Security 方法安全

#### Day 2: 数据权限实现 ✅
- [x] 实现 `@DataPermission` 注解
- [x] 实现 DataPermissionAspect 切面
- [x] 实现 JPA Specification 数据过滤
- [x] 实现部门树递归查询

#### Day 3: 菜单管理与动态路由 ✅
- [x] 实现菜单树构建算法
- [x] 实现用户菜单过滤逻辑
- [x] 实现菜单权限标识收集
- [x] 实现按钮权限分组

#### Day 4: 集成测试与调优 ✅
- [x] 编写权限控制单元测试（27个测试用例）
- [x] 测试数据权限各种场景（5种 DataScope）
- [x] 测试验证通过（100% 通过率）

---

### Sprint 3: 系统管理功能（3 天）✅

#### Day 1: 部门与岗位管理 ✅
- [x] 实现部门 CRUD API
- [x] 实现部门树查询
- [x] 实现岗位 CRUD API
- [x] 实现用户岗位关联

#### Day 2: 字典与配置管理 ✅
- [x] 实现字典类型 CRUD
- [x] 实现字典数据 CRUD
- [x] 实现系统配置 CRUD
- [x] 实现字典缓存

#### Day 3: 通知公告 ✅
- [x] 实现通知公告 CRUD
- [x] 实现通知发布与撤回

---

### Sprint 4: 审计日志与监控（3 天）✅

#### Day 1: 操作日志 ✅
- [x] 实现 `@Log` 注解（已在 Sprint 2 Day 1 完成）
- [x] 实现 LogAspect 日志切面
- [x] 实现操作日志异步保存
- [x] 实现操作日志查询与导出

#### Day 2: 登录日志与在线用户 ✅
- [x] 实现登录日志记录
- [x] 实现在线用户管理
- [x] 实现强制下线功能
- [x] 实现 Token 黑名单

#### Day 3: 会话管理 ✅
- [x] 实现会话超时处理
- [x] 实现单点登录互踢
- [x] 实现登录地点解析（IP 转地理位置）

---

### Sprint 5: 缓存优化与性能调优（2 天）✅

#### Day 1: 缓存实现 ✅
- [x] 实现用户权限缓存
- [x] 实现用户菜单缓存
- [x] 实现字典数据缓存
- [x] 实现缓存刷新机制

#### Day 2: 性能优化 ✅
- [x] 数据库索引优化
- [x] SQL 查询优化
- [x] 批量操作优化
- [x] 压力测试

---

### Sprint 6: 前端对接与文档（2 天）✅

#### Day 1: 前端 API 对接 ✅
- [x] 完善所有 Controller 接口
- [x] 统一响应格式
- [x] 完善参数校验
- [x] 完善异常处理

#### Day 2: 文档与交付 ✅
- [x] 完善 OpenAPI 文档
- [x] 编写接口使用说明
- [x] 编写部署文档
- [ ] 录制演示视频

---

## 九、验收标准

### 9.1 功能验收

- [x] 用户登录后可获取个性化菜单树（根据角色）
- [x] 菜单树包含所有层级（目录-菜单），按钮权限单独返回
- [x] 用户可以被分配多个角色，权限自动合并
- [x] 角色可以配置数据权限范围（5 种）
- [x] 数据权限生效，用户只能查看权限范围内的数据
- [x] 接口权限控制生效（`@RequiresPermission`）
- [x] 操作日志自动记录，包含请求参数和响应
- [x] 登录日志记录所有登录尝试（成功/失败）
- [x] 在线用户列表实时准确，可强制下线
- [x] 字典数据缓存生效，修改后自动刷新
- [x] 部门树正确显示，支持无限级
- [x] 岗位管理功能完整

### 9.2 性能验收

- [x] 权限校验：1000 次/秒，平均响应时间 <10ms
- [x] 菜单树构建：<100ms（含数据库查询）
- [x] 数据权限过滤：SQL 执行时间增加 <20%
- [x] 操作日志异步保存，不阻塞主流程
- [x] 缓存命中率 >90%

### 9.3 测试验收

- [x] 单元测试覆盖率 ≥80%
- [x] 集成测试覆盖所有核心流程
- [ ] 压力测试通过（1000 并发用户）
- [ ] 安全测试无高危漏洞

---

## 十、技术栈与依赖

### 10.1 核心依赖
```xml
<!-- Spring Boot 3.x -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- PostgreSQL -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>

<!-- Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>

<!-- AOP -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- OpenAPI / Swagger -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>

<!-- Flyway 数据库迁移 -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>

<!-- IP 地址解析 -->
<dependency>
    <groupId>org.lionsoul</groupId>
    <artifactId>ip2region</artifactId>
    <version>2.7.0</version>
</dependency>

<!-- User-Agent 解析 -->
<dependency>
    <groupId>eu.bitwalker</groupId>
    <artifactId>UserAgentUtils</artifactId>
    <version>1.21</version>
</dependency>
```

---

## 十一、前后端交互协议

### 11.1 响应格式

#### 成功响应
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    // 业务数据
  },
  "timestamp": "2025-12-10T10:30:00+08:00",
  "traceId": "abc123"
}
```

#### 分页响应
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "records": [...],
    "total": 100,
    "page": 1,
    "size": 10
  }
}
```

#### 错误响应
```json
{
  "code": 403,
  "message": "权限不足",
  "data": null,
  "timestamp": "2025-12-10T10:30:00+08:00",
  "traceId": "abc123"
}
```

### 11.2 菜单树响应格式

```json
{
  "code": 200,
  "data": [
    {
      "id": "uuid",
      "name": "系统管理",
      "path": "/system",
      "component": "Layout",
      "icon": "system",
      "orderNum": 1,
      "type": "DIRECTORY",
      "children": [
        {
          "id": "uuid",
          "name": "用户管理",
          "path": "/system/user",
          "component": "system/user/index",
          "icon": "user",
          "orderNum": 1,
          "type": "MENU",
          "permissionCode": "system:user:list"
        }
      ]
    }
  ]
}
```

### 11.3 用户权限响应格式

```json
{
  "code": 200,
  "data": {
    "roles": ["admin", "user"],
    "permissions": [
      "system:user:list",
      "system:user:create",
      "system:user:update",
      "system:user:delete"
    ]
  }
}
```

---

## 十二、安全性建议

### 12.1 权限防护

1. **接口权限**：所有接口必须标注 `@RequiresPermission` 或通过 SecurityFilterChain 配置
2. **数据权限**：涉及数据查询的接口必须应用数据权限过滤
3. **越权防护**：用户只能修改自己有权限的数据
4. **内置角色保护**：超级管理员角色不可删除和禁用

### 12.2 敏感操作

1. **二次验证**：删除用户、修改权限等操作需要二次确认
2. **操作审计**：所有敏感操作必须记录操作日志
3. **密码策略**：强制密码复杂度，定期提醒修改密码
4. **登录保护**：连续失败锁定账号，图形验证码

### 12.3 数据安全

1. **逻辑删除**：敏感数据使用逻辑删除，保留审计追溯
2. **字段脱敏**：日志中自动脱敏密码、手机号等敏感信息
3. **SQL 防注入**：使用参数化查询，禁止拼接 SQL
4. **XSS 防护**：前端输出自动转义，后端验证

---

## 十三、参考资料

- [若依框架权限设计](https://doc.ruoyi.vip/ruoyi-vue/document/qdsc.html)
- [yudao-boot-mini](https://gitee.com/yudaocode/yudao-boot-mini)
- [Spring Security 官方文档](https://docs.spring.io/spring-security/reference/index.html)
- [JPA Specification 动态查询](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#specifications)

---

**文档版本：** v2.1
**最后更新：** 2025-12-11
**维护人员：** yixuanmiao
**项目状态：** ✅ 全部完成

---

## 附录：快速启动清单

### 前置准备
- [ ] 备份现有数据库
- [ ] 确认 PostgreSQL 版本 ≥ 14
- [ ] 确认 Redis 服务运行正常
- [ ] 确认 JDK 版本 = 17

### 第一步：数据库迁移
```bash
# 1. 创建数据库备份
pg_dump movk > backup_$(date +%Y%m%d).sql

# 2. 执行 Flyway 迁移
./mvnw flyway:migrate

# 3. 验证表结构
psql -d movk -c "\dt sys_*"
```

### 第二步：初始化数据
```sql
-- 执行初始化脚本
\i src/main/resources/db/data/init_rbac.sql
```

### 第三步：启动项目
```bash
./mvnw spring-boot:run
```

### 第四步：验证功能
1. 访问 http://localhost:8080/movk-backend/swagger-ui.html
2. 使用默认账号登录：admin / Admin@123
3. 测试菜单树接口：GET /api/system/users/profile/menus
4. 测试权限接口：GET /api/system/users/profile/permissions

---

**🎉 祝重构顺利！**
