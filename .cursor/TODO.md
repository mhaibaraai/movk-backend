执行指南（WHY 速览）：
- 选择 Spring Security 6 + JWT（Access/Refresh）：前后端分离友好、易水平扩展、与网关/多端共享会话形态。
- 引入 Redis：验证码、登录失败计数、令牌指纹与黑名单、限流等需低延迟的状态管理。
- 使用 springdoc-openapi：为前端提供稳定契约与 Swagger UI，便于生成 TypeScript 客户端与联调。
- 集成 GitHub OAuth2：降低注册/登录门槛，首登自动本地化账户，后续可做企业组织白名单提权。
- RBAC 以“菜单/按钮”为核心：满足前端动态路由与按钮显隐，权限标识统一 `module:resource:action`。
- Flyway 管理数据库版本：全员可一键拉起同构环境，初始化表结构与种子数据可复现。
- 统一响应/异常：`ApiResponse` 与全局异常处理，保证错误码/结构一致，降低对接成本。
- CORS 与 TraceId：跨域设置与链路追踪头，方便联调与排障。

---

IMPLEMENTATION CHECKLIST:
1. 在 `pom.xml` 新增依赖：`spring-boot-starter-security`、`spring-boot-starter-oauth2-client`、`spring-boot-starter-validation`、`spring-boot-starter-data-redis`、`springdoc-openapi-starter-webmvc-ui`、`io.jsonwebtoken:jjwt-api|impl|jackson`、`spring-boot-starter-mail`、`org.flywaydb:flyway-core`。
2. 在 `src/main/resources/application.yml` 增加基础占位配置项：`security.jwt.{issuer,secret,accessTtlMinutes,refreshTtlDays}`、`spring.redis`、`spring.mail`、`springdoc`、`spring.security.oauth2.client.registration.github`（`client-id`、`client-secret`、`redirect-uri`）与 `provider.github`（`authorization-uri`、`token-uri`、`user-info-uri`）。
3. 在 `src/main/resources/application-dev.yml` 开启 Swagger UI 与 CORS 源（本地前端地址），配置 Redis、Mail 的本地占位；在 `application-prod.yml` 关闭 Swagger UI（仅暴露 JSON 或完全关闭），保留 OAuth2/Redis/Mail 的生产占位。
4. 新增 `src/main/java/com/movk/common/web/ApiResponse.java` 定义统一响应结构（`code,message,data,traceId,timestamp`）。
5. 新增 `src/main/java/com/movk/common/web/ErrorCode.java` 维护统一错误码枚举（如 `OK, UNAUTHORIZED, FORBIDDEN, VALIDATION_FAILED, BUSINESS_ERROR`）。
6. 新增 `src/main/java/com/movk/common/web/GlobalExceptionHandler.java` 统一异常处理（Spring 校验异常、认证/鉴权异常、业务异常），返回 `ApiResponse`。
7. 新增 `src/main/java/com/movk/common/openapi/OpenApiConfig.java`：配置分组、标题、版本、全局 `SecurityScheme`（`bearer-jwt` 与 `oauth2-github`）、全局响应与公共头（`X-Trace-Id`）。
8. 新增 `src/main/java/com/movk/common/redis/RedisConfig.java`：配置 `LettuceConnectionFactory`、`StringRedisTemplate`，统一 Key 前缀（`movk:`）。
9. 新增 `src/main/java/com/movk/security/JwtProperties.java`（`@ConfigurationProperties("security.jwt")`）绑定 JWT 配置。
10. 新增 `src/main/java/com/movk/security/JwtTokenService.java`：生成/校验 Access/Refresh（HS256，含 `jti,sub,roles,ver,iat,exp,iss`），提供轮换刷新与解析。
11. 新增 `src/main/java/com/movk/security/JwtAuthenticationFilter.java`：从 `Authorization: Bearer` 抽取 Access，校验、加载用户与权限，写入 `SecurityContext`。
12. 新增 `src/main/java/com/movk/security/RestAuthenticationEntryPoint.java` 与 `RestAccessDeniedHandler.java`：返回 JSON 风格 401/403。
13. 新增 `src/main/java/com/movk/security/SecurityConfig.java`：定义 `SecurityFilterChain`、`PasswordEncoder(BCrypt)`、CORS、会话无状态、异常处理、白名单（`/auth/**`, `/v3/api-docs/**`, `/swagger-ui/**`, `/oauth2/**`, `/actuator/health`），注册 `JwtAuthenticationFilter` 于 `UsernamePasswordAuthenticationFilter` 前。
14. 新增 `src/main/java/com/movk/security/oauth2/GithubOAuth2UserService.java`：从 GitHub `user-info` 映射本地用户（首登自动创建、授予 `ROLE_USER`，记录 `githubId`）。
15. 新增 `src/main/java/com/movk/security/oauth2/OAuth2SuccessHandler.java`：首登/登录后颁发 Access（响应体/头）与设置 Refresh Cookie（`HttpOnly+Secure+SameSite=Lax`），记录 Redis 指纹/版本。
16. 新增 `src/main/java/com/movk/security/oauth2/OAuth2FailureHandler.java`：标准化 OAuth2 失败返回。
17. 新增 `src/main/java/com/movk/rbac/entity/User.java`：`id,username,email,phone,password,status,githubId,lastLoginAt,createdAt,updatedAt`。
18. 新增 `src/main/java/com/movk/rbac/entity/Role.java`：`id,code,name,builtIn,status,sort,createdAt,updatedAt`。
19. 新增 `src/main/java/com/movk/rbac/entity/Permission.java`：`id,code,name,type(MENU,BUTTON),createdAt,updatedAt`。
20. 新增 `src/main/java/com/movk/rbac/entity/Menu.java`：`id,parentId,type(DIR,MENU,LINK),name,path,component,icon,hidden,sort,externalLink,keepAlive,redirect,createdAt,updatedAt`。
21. 新增多对多关系表映射（JPA 或通过中间实体）：`user_role(user_id,role_id)`、`role_permission(role_id,permission_id)`，菜单与权限的关联字段（`menu.permissionCode?` 或中间表 `menu_permission`，二选一）。
22. 新增 `src/main/java/com/movk/rbac/repository/*Repository.java`：为 User/Role/Permission/Menu 建立 JPA 仓库。
23. 新增 `src/main/java/com/movk/rbac/dto/*`：请求/响应 DTO（用户新增/编辑、角色授权、菜单节点、权限点等）。
24. 新增 `src/main/java/com/movk/rbac/service/UserService.java`：用户 CRUD、查重与密码加密、状态变更、GitHub 绑定。
25. 新增 `src/main/java/com/movk/rbac/service/RoleService.java`：角色 CRUD、分配权限、查询角色权限。
26. 新增 `src/main/java/com/movk/rbac/service/PermissionService.java`：权限 CRUD、批量查询、按角色聚合。
27. 新增 `src/main/java/com/movk/rbac/service/MenuService.java`：菜单 CRUD、树构建、与权限的绑定/解绑。
28. 新增 `src/main/java/com/movk/auth/service/VerificationCodeService.java`：生成/校验 6 位验证码（Redis：`movk:vc:{purpose}:{receiver}`，TTL 5 分钟，限流 1/分、5/时）。
29. 新增 `src/main/java/com/movk/auth/service/EmailService.java` 与 `SmsService.java`：发送验证码（开发环境打印日志/控制台，生产接入 SMTP/短信厂商）。
30. 新增 `src/main/java/com/movk/auth/service/AuthService.java`：登录/刷新/登出流程、失败计数（Redis：`movk:login:fail:{username}`）、令牌黑名单与版本（`movk:jwt:bl:{jti}`、`movk:jwt:ver:{userId}:{clientId}`）。
31. 新增 `src/main/java/com/movk/auth/controller/AuthController.java`：
31.1 `POST /auth/login`（用户名密码，失败计数与可选验证码校验，返回 Access 并设置 Refresh Cookie）。  
31.2 `POST /auth/login/otp`（短信/邮箱验证码登录）。  
31.3 `POST /auth/verification/email|sms`（发送验证码）。  
31.4 `POST /auth/refresh`（轮换 Refresh，发新 Access 并重置 Refresh Cookie）。  
31.5 `POST /auth/logout`（Access 加黑 + 撤销 Refresh 版本）。  
31.6 `GET /auth/me`（返回用户、角色、权限点数组、可见菜单树）。
32. 新增 `src/main/java/com/movk/rbac/controller/UserController.java`：用户 CRUD、重置密码、分配角色（鉴权注解使用 `hasAuthority("system:user:*")` 或细粒度标识）。
33. 新增 `src/main/java/com/movk/rbac/controller/RoleController.java`：角色 CRUD、授权（绑定权限）。
34. 新增 `src/main/java/com/movk/rbac/controller/MenuController.java`：菜单 CRUD、树查询、绑定权限。
35. 新增 `src/main/java/com/movk/rbac/controller/PermissionController.java`：权限 CRUD、列表查询。
36. 在 `SecurityConfig` 的鉴权规则中为各接口声明所需权限点（按钮级），对 `GET /auth/me` 放行经过 JWT（或允许匿名但返回空集）。
37. 在 `src/main/resources/db/migration/V1__init.sql` 编写基础表结构（用户、角色、权限、菜单、关联表），添加唯一索引与必要约束。
38. 在 `src/main/resources/db/migration/V2__seed.sql` 初始化：`admin` 超级管理员（`builtIn=true`）、`ROLE_ADMIN/ROLE_USER`、基础菜单树与常用权限点，关联 `admin -> ROLE_ADMIN -> 全权限`。
39. 在 `src/main/java/com/movk/base/config/DataSourceDependsOnSshTunnel.java` 保持不变，确认 Flyway 在 SSH 就绪后可访问数据库并完成迁移。
40. 在 `src/main/resources/application-*.yml` 增加 Cookie 安全属性开关（开发 `Secure=false`、生产 `Secure=true`）、跨域源、OpenAPI 可见性（开发启用 UI；生产禁用 UI/仅 JSON）。
41. 在 `src/main/java/com/movk/base/web/TraceIdFilter.java` 的响应头基础上，统一在 `GlobalExceptionHandler` 与 `ApiResponse` 返回 `traceId` 字段。
42. 在 `src/main/java/com/movk/common/security/WebCorsCustomizer`（或 `SecurityConfig` 内）配置 CORS：允许本地前端源、允许凭证、暴露 `X-Trace-Id`。
43. 新增登录速率限制与验证码触发：连续失败 ≥3 次强制验证码、≥5 次锁定 15 分钟（Redis 计数与锁定键）。
44. 在 `OpenApiConfig` 中为 `auth/users/roles/menus/permissions` 添加分组与标签，声明 `bearer-jwt` 为默认 `SecurityRequirement`，为 OAuth2 endpoints 单独说明。
45. 在 `AuthController` 登录/刷新/登出接口上添加 OpenAPI 示例请求/响应与 Cookie/Header 说明（Access=Header，Refresh=Cookie）。
46. 在 `MenuService` 输出前端期望的菜单树字段（`id,parentId,name,path,component,icon,hidden,sort,children,meta`），并在 `GET /auth/me` 一并返回 `perms: string[]`。
47. 在 `application-prod.yml` 仅保留 `GET /v3/api-docs`（可选），关闭 `/swagger-ui/**`，或通过网关/白名单限制访问。
48. 编写最小化集成测试：`/auth/login` 成功/失败、`/auth/refresh`、`/auth/me`（携带 Access）、`/users` CRUD 权限校验（403）。
49. 启动应用（dev），验证 Flyway 迁移成功、Swagger UI 可访问、`/auth/login` 与 `/auth/me` 工作正常。
50. 将前端本地地址加入 CORS 白名单后，前端使用 `openapi.json` 生成 TypeScript 客户端，对接登录、菜单树、权限点与用户管理页面。

