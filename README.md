# 全渠道 ERP 项目

> 智能体开发入口：优先按顺序阅读 [AGENT_RULES.md](AGENT_RULES.md) → [dev-design.md](dev-design.md) → [prd.md](prd.md) → [tech-architecture.md](tech-architecture.md)。

本仓库包含以下内容：

- `erp-server`：Spring Boot 3 + Maven 多模块后端
- `erp-frontend`：pnpm monorepo 前端
  - `apps/web`：React + Vite + Ant Design + ProComponents
  - `apps/desktop`：Tauri 2.x 桌面端
  - `apps/mobile`：React Native + Expo

## 环境要求

### 后端

- JDK 17
- Maven 3.9+
- PostgreSQL 15
- Redis 7
- RabbitMQ 3.12
- RustFS（S3 兼容对象存储）

### 前端

- Node.js 24+
- pnpm 11+
- Rust / Cargo（桌面端）
- Expo Go（移动端扫码调试）

## 目录结构

```text
.
├── erp-server
│   ├── erp-admin
│   ├── erp-common
│   ├── erp-system
│   ├── erp-product
│   ├── erp-material
│   ├── erp-inventory
│   ├── erp-sales
│   ├── erp-purchase
│   ├── erp-production
│   ├── erp-finance
│   ├── erp-hr
│   └── erp-integration
└── erp-frontend
    ├── apps
    │   ├── web
    │   ├── desktop
    │   └── mobile
    └── packages
        ├── shared
        ├── ui-web
        └── ui-mobile
```

## 开发环境启动

### 1. 启动基础依赖

开发环境至少需要启动：

- PostgreSQL：`localhost:5432`
- Redis：`localhost:6379`
- RabbitMQ：`localhost:5672`
- RustFS：`localhost:9000`

### 2. 启动后端

```powershell
cd erp-server
mvn clean package -DskipTests
cd erp-admin
mvn spring-boot:run
```

默认接口：

- 后端服务：[http://localhost:8080](http://localhost:8080)
- 健康检查：[http://localhost:8080/api/v1/ping](http://localhost:8080/api/v1/ping)
- Swagger：[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### 3. 启动 Web 端

```powershell
cd erp-frontend
pnpm install
pnpm dev:web
```

默认地址：

- Web：[http://localhost:5173](http://localhost:5173)

### 4. 启动桌面端

```powershell
cd erp-frontend
pnpm install
pnpm dev:desktop
```

说明：

- `dev:desktop` 会先自动启动 `apps/desktop` 的 Vite dev server
- 然后由 Tauri 2.x 启动桌面窗口

### 5. 启动移动端

在前端根目录启动：

```powershell
cd erp-frontend
pnpm install
pnpm dev:mobile
```

或者在移动端子目录启动：

```powershell
cd erp-frontend/apps/mobile
pnpm dev:mobile
```

说明：

- 使用 Expo Go 扫码调试
- 当前项目已对齐 Expo SDK 54

## 测试环境启动

测试环境建议与开发环境区分配置、端口和外部资源。

### 后端测试环境

```powershell
cd erp-server
mvn clean package -DskipTests
cd erp-admin
mvn spring-boot:run "-Dspring-boot.run.profiles=staging"
```

说明：

- 使用 `application-staging.yml`（若后续补齐）
- 建议连接独立测试库、测试 Redis、测试 RabbitMQ、测试 RustFS bucket

### 前端测试环境

Web 测试构建：

```powershell
cd erp-frontend
pnpm install
pnpm build:web
```

桌面端测试构建：

```powershell
cd erp-frontend
pnpm build:desktop
```

移动端测试：

```powershell
cd erp-frontend/apps/mobile
pnpm start
```

说明：

- 移动端测试可继续用 Expo Go
- 真机提测时建议通过 EAS Build / 内测包分发

## 生产环境启动

### 后端生产启动

先打包：

```powershell
cd erp-server
mvn clean package -DskipTests
```

然后运行可执行 Jar：

```powershell
java -jar erp-server/erp-admin/target/erp-admin-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

生产环境要求：

- 不直接使用开发库账号和默认密码
- 数据库、Redis、RabbitMQ、RustFS 地址通过生产配置或环境变量注入
- `application-prod.yml` 中的敏感配置必须外置

### Web 生产部署

构建：

```powershell
cd erp-frontend
pnpm install
pnpm build:web
```

产物目录：

- `erp-frontend/apps/web/dist`

建议部署到：

- Nginx
- 或其他静态资源服务

### 桌面端生产构建

```powershell
cd erp-frontend
pnpm install
pnpm build:desktop
```

说明：

- Tauri 会生成对应平台的安装包
- Windows 产物通常位于 `apps/desktop/src-tauri/target/release/bundle`

### 移动端生产构建

当前仓库已完成 Expo 骨架验证。生产环境建议：

- Android：使用 EAS Build 生成安装包
- iOS：使用 EAS Build / TestFlight 分发

## 常用命令速查

### 后端

```powershell
cd erp-server
mvn clean package -DskipTests
```

### 前端

```powershell
cd erp-frontend
pnpm install
pnpm dev:web
pnpm dev:desktop
pnpm dev:mobile
pnpm build:web
pnpm build:desktop
```

## 当前状态

已验证成功：

- 后端 Maven 多模块骨架
- Web 端骨架
- Tauri 桌面端骨架
- Expo 移动端骨架

后续可继续推进：

- 后端模块实体 / Mapper / Service / Controller
- Web 路由、登录、业务页面
- 桌面端打印、通知、离线能力
- 移动端扫码、待办、离线队列
