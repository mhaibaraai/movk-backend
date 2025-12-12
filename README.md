# Movk Backend

基于 Spring Boot 3.5 的后台管理系统 API 服务。

## 技术栈

- **框架**: Spring Boot 3.5 + Spring Security + Spring Data JPA
- **数据库**: PostgreSQL + Redis
- **认证**: JWT
- **文档**: SpringDoc OpenAPI（开发环境）
- **部署**: Docker + Nginx

## 项目结构

```
src/main/java/com/movk/
├── controller/          # API 控制器
│   ├── AuthController       # 认证：登录/登出/用户信息
│   ├── UserController       # 用户管理
│   ├── RoleController       # 角色管理
│   ├── MenuController       # 菜单权限
│   ├── DepartmentController # 部门管理
│   ├── PostController       # 岗位管理
│   ├── DictController       # 字典管理
│   ├── NoticeController     # 通知公告
│   ├── ConfigController     # 系统配置
│   ├── LoginLogController   # 登录日志
│   ├── OperateLogController # 操作日志
│   └── OnlineUserController # 在线用户
├── service/             # 业务逻辑
├── repository/          # 数据访问
├── entity/              # 数据实体
├── dto/                 # 数据传输对象
├── security/            # 安全配置与过滤器
└── base/                # 基础设施（异常、响应封装等）
```

## 本地开发

### 环境要求

- JDK 17+
- Maven 3.9+

### 配置

创建 `.env.dev` 文件：

```bash
# 必填：私密配置
DB_PASSWORD=xxx
REDIS_PASSWORD=xxx
JWT_SECRET=xxx（至少64字符）

# SSH 隧道（连接服务器数据库）
SSH_HOST=xxx
SSH_USERNAME=xxx
SSH_PASSWORD=xxx
```

### 启动

```bash
mvn spring-boot:run
```

API 文档：http://localhost:36600/movk-backend/swagger-ui.html

## API 路径

所有接口前缀：`/movk-backend`

| 模块 | 路径 | 说明 |
|------|------|------|
| 认证 | `/auth/*` | 登录、登出、当前用户 |
| 用户 | `/api/system/user/*` | 用户 CRUD |
| 角色 | `/api/system/role/*` | 角色 CRUD、权限分配 |
| 菜单 | `/api/system/menu/*` | 菜单树、权限配置 |
| 部门 | `/api/system/dept/*` | 部门树 |
| 岗位 | `/api/system/post/*` | 岗位管理 |
| 字典 | `/api/system/dict/*` | 字典类型与数据 |
| 配置 | `/api/system/config/*` | 系统参数 |
| 通知 | `/api/system/notice/*` | 公告管理 |
| 日志 | `/api/monitor/login-log/*` | 登录日志 |
| 日志 | `/api/monitor/operate-log/*` | 操作日志 |
| 在线 | `/api/monitor/online/*` | 在线用户 |

## 生产部署

### 架构

```
用户 → Nginx (443) → movk-backend (36600) → PostgreSQL / Redis
```

### 部署文件

- `Dockerfile` - 镜像构建
- `deploy/server/docker-compose.app.yml` - 容器编排

### GitHub Actions

推送到 `main` 分支自动构建镜像到 GHCR，配置以下 secrets 启用自动部署：

| 类型 | 名称 | 说明 |
|------|------|------|
| Secret | `PROD_SSH_HOST` | 服务器 IP |
| Secret | `PROD_SSH_USER` | SSH 用户名 |
| Secret | `PROD_SSH_KEY` | SSH 私钥 |
