# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

基于 Spring Boot 3.5.4 的 RBAC 后台管理系统，使用 JWT 认证、Spring Security、JPA + PostgreSQL，支持细粒度的功能权限和数据权限控制。

## 核心开发命令

### 本地开发

```bash
# 启动应用（需要先配置 .env.dev）
mvn spring-boot:run

# 编译检查
mvn clean compile

# 运行测试
mvn test

# 运行单个测试类
mvn test -Dtest=PermissionServiceTest

# 运行单个测试方法
mvn test -Dtest=PermissionServiceTest#testHasPermission
```

### 数据库迁移

```bash
# 修复 Flyway 状态（遇到迁移失败时）
mvn flyway:repair

# 查看迁移状态
mvn flyway:info
```

### 生产部署

```bash
# 构建 Docker 镜像
docker build -t movk-backend .

# GitHub Actions 自动部署到 main 分支后触发
# 需要配置 secrets: PROD_SSH_HOST, PROD_SSH_USER, PROD_SSH_PASSWORD
```

## 架构设计

### 分层架构

```
Controller (API 层)
    ↓ 调用
AppService (编排层) - 协调多个 Service，处理跨领域逻辑
    ↓ 调用
Service (业务层) - 单一领域业务逻辑
    ↓ 调用
Repository (数据层) - JPA 数据访问
```

**关键规则**：
- Controller 只处理 HTTP 相关逻辑（参数校验、响应封装），复杂业务逻辑放在 AppService
- AppService 负责编排多个 Service 完成复杂业务流程（如登录需要调用用户查询、Token 生成、日志记录等多个 Service）
- Service 保持单一职责，一个 Service 对应一个领域实体
- Repository 使用 Spring Data JPA，优先使用方法命名查询，复杂查询使用 Specification

### 权限控制系统

本项目实现了双层权限控制：

#### 1. 功能权限（Method 级别）

使用 `@RequiresPermission` 注解控制接口访问：

```java
@RequiresPermission("system:user:list")
public R<List<User>> list() { ... }
```

权限标识格式：`模块:资源:操作`，例如：
- `system:user:list` - 用户列表查询
- `system:role:edit` - 角色编辑
- `system:menu:delete` - 菜单删除

权限数据存储在 `sys_menu` 表的 `permission` 字段，通过 `sys_role` → `sys_role_menu` → `sys_menu` 关联到用户。

#### 2. 数据权限（Row 级别）

使用 `@DataPermission` 注解控制数据范围：

```java
@DataPermission(deptAlias = "d", userAlias = "u")
public List<User> findAll(Specification<User> spec) { ... }
```

数据权限范围（由角色的 `data_scope` 字段决定）：
- `ALL` - 全部数据
- `DEPT` - 仅本部门数据
- `DEPT_AND_CHILD` - 本部门及子部门数据
- `SELF` - 仅本人数据
- `CUSTOM` - 自定义部门数据权限（通过 `sys_role_dept` 关联）

实现机制：通过 AOP 拦截 Repository 方法，动态拼接 JPA Specification，在 SQL 层面过滤数据。

### 认证流程

```
1. 用户登录 → AuthController.login()
2. 调用 AuthAppService.loginAndIssueTokens()
   - 验证用户名密码
   - 加载用户角色和权限
   - 生成 Access Token (2小时) 和 Refresh Token (7天)
   - 记录登录日志
3. 请求携带 Token → JwtAuthenticationFilter
   - 解析 Token，提取用户信息
   - 设置 SecurityContext
4. 权限检查 → PermissionAspect / DataPermissionAspect
   - 检查功能权限和数据权限
```

### 统一响应格式

所有 API 返回 `R<T>` 对象：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "traceId": "uuid",
  "timestamp": 1234567890
}
```

- `code`: 状态码（定义在 `RCode` 枚举）
- `message`: 提示信息
- `data`: 业务数据
- `traceId`: 请求追踪 ID（通过 `TraceIdFilter` 自动注入到 MDC）
- `timestamp`: 响应时间戳

## 开发规范

### API 版本控制

所有 Controller 的 `@RequestMapping` 使用 SpEL 表达式引用统一配置的 API 版本：

```java
@RestController
@RequestMapping("/${api.version}/system/users")
public class UserController { ... }
```

版本配置位于 [application.yml:19](src/main/resources/application.yml#L19)：

```yaml
api:
  version: v1
```

**优势**：
- 集中管理 API 版本，避免拼写错误和不一致
- 便于后续升级到 v2、v3 等版本
- 可以在不同环境使用不同版本（如需要）
- 修改版本时只需改动一处配置

### 命名约定

- 实体类：`User`, `Role`, `Menu`（对应表名 `sys_user`, `sys_role`, `sys_menu`）
- Repository：`UserRepository`, `RoleRepository`
- Service：`UserService`, `RoleService`
- AppService：`AuthAppService`, `UserAppService`（用于复杂业务编排）
- Controller：`UserController`, `AuthController`
- DTO：
  - 请求：`UserQueryReq`, `UserCreateReq`, `UserUpdateReq`
  - 响应：`UserResp`, `UserDetailResp`
- VO：`UserInfoVO`（用于特定场景的视图对象）

### 枚举与 Converter

所有枚举类型（如 `UserStatus`, `MenuType`, `EnableStatus`）都：
1. 在 `com.movk.common.enums` 包定义
2. 实现数据库值和枚举的映射
3. 配置对应的 JPA Converter（在 `com.movk.common.converter`）

示例：
```java
@Converter(autoApply = true)
public class UserStatusConverter implements AttributeConverter<UserStatus, Integer> { ... }
```

### 日志与审计

- **操作日志**：使用 `@Log` 注解自动记录操作（AOP 实现），保存到 `sys_operate_log` 表
- **登录日志**：登录成功/失败时调用 `LoginLogService` 记录，保存到 `sys_login_log` 表
- **请求追踪**：每个请求自动生成 `traceId`（通过 `TraceIdFilter`），可用于日志关联和问题排查

### 数据库迁移（Flyway）

迁移脚本位置：`src/main/resources/db/migration/`

命名规则：`V{version}__{description}.sql`

示例：
- `V1__init_schema.sql` - 初始化表结构
- `V2__init_data.sql` - 初始化数据

**重要**：
- 迁移脚本一旦提交不可修改（Flyway 会校验 checksum）
- 修改错误的迁移脚本需要先 `mvn flyway:repair`，然后创建新的迁移脚本修复
- 测试环境可以删除最后一个迁移脚本重新创建，生产环境必须创建新迁移

### 异常处理

- 业务异常：抛出 `BusinessException(RCode code, String message)`
- 全局异常处理：`GlobalExceptionHandler` 统一捕获并返回 `R<T>` 格式
- 认证异常：`RestAuthenticationEntryPoint`（401）
- 授权异常：`RestAccessDeniedHandler`（403）

## 环境配置

### 本地开发（.env.dev）

```bash
DB_PASSWORD=xxx
REDIS_PASSWORD=xxx
JWT_SECRET=xxx  # 至少 64 字符
SSH_HOST=xxx
SSH_USERNAME=xxx
SSH_PASSWORD=xxx
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

### Profile 切换

- 默认：`dev` (开发环境，启用 Swagger UI，通过 SSH 隧道连接数据库)
- 生产：`prod` (禁用 Swagger，直接连接数据库)

通过环境变量切换：`SPRING_PROFILES_ACTIVE=prod`

## API 文档

开发环境访问：http://localhost:36600/movk-backend/swagger-ui.html

所有接口格式：`/movk-backend/${api.version}/<模块>/<资源>`

当前版本：`v1`

### 公开接口（无需认证）

- `POST /v1/auth/login` - 用户登录
- `POST /v1/auth/refresh` - 刷新 Token
- `/actuator/health` - 健康检查

### 认证接口

所有其他接口需要在 Header 中携带：`Authorization: Bearer {access_token}`

## 测试策略

- 测试类命名：`{ClassName}Test`
- 测试方法命名：`test{MethodName}_{Scenario}`
- 使用 `@SpringBootTest` 进行集成测试
- 使用 `@DataJpaTest` 进行 Repository 测试
- 关键业务逻辑（权限控制、数据权限）必须有测试覆盖

## 常见问题

### Flyway 迁移失败

```bash
mvn flyway:repair  # 修复 Flyway 状态
```

### SSH 隧道连接失败

检查 `.env.dev` 中的 SSH 配置，确保：
- SSH_HOST 可访问
- SSH_USERNAME 和 SSH_PASSWORD 正确
- 本地端口 35432 未被占用

### Token 验证失败

- 检查 JWT_SECRET 配置是否正确（至少 64 字符）
- 检查 Token 是否过期（Access Token 2 小时，Refresh Token 7 天）
- 使用 `/auth/refresh` 刷新 Token

### 权限拒绝（403）

- 检查用户是否有对应的功能权限（通过角色 → 菜单关联）
- 检查数据权限配置（角色的 `data_scope` 字段）
- 查看日志中的详细权限校验信息

## 依赖说明

- **Spring Boot 3.5.4**：核心框架
- **Spring Security**：认证授权
- **Spring Data JPA + Hibernate**：ORM
- **PostgreSQL**：主数据库
- **Redis**：缓存和 Session 存储
- **JJWT 0.12.6**：JWT 实现
- **SpringDoc OpenAPI**：API 文档生成
- **Flyway**：数据库版本管理
- **JSch**：SSH 隧道（用于本地开发连接服务器数据库）
- **Lombok**：减少样板代码

## 代码风格

- 使用 Lombok 减少 getter/setter/constructor
- 优先使用构造器注入（`@RequiredArgsConstructor`）而不是字段注入
- Service 方法添加 `@Transactional` 确保事务一致性
- Controller 方法添加 OpenAPI 注解（`@Operation`, `@Tag`）用于文档生成
- 复杂业务逻辑添加注释说明意图，简单逻辑依赖命名自解释
