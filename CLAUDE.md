# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

### Backend (from `erp-server/`)
```bash
mvn clean package -DskipTests           # Build all modules
mvn -q -DskipTests package              # Quick compile check (silent)
cd erp-admin && mvn spring-boot:run     # Run dev server on :8080
```

### Frontend (from `erp-frontend/`)
```bash
pnpm install                            # Install all workspace deps
pnpm dev:web                            # Vite dev server on :5173
pnpm dev:desktop                        # Tauri dev window on :5174
pnpm dev:mobile                         # Expo start (Expo Go scan)
pnpm build:web                          # Production build → apps/web/dist
pnpm build:desktop                      # Tauri production build
```

### Compile Checks (run after every change)
```bash
cd erp-server && mvn -q -DskipTests package
cd erp-frontend && pnpm --filter @erp/web exec tsc -p tsconfig.json --noEmit
```

### Infrastructure
PostgreSQL `localhost:5432` (db: `erp_db`, user: `erp_user`), Redis `localhost:6379`, RabbitMQ `localhost:5672`, RustFS (S3-compatible) `localhost:9000` (bucket: `erp-dev`). JWT: access 2h, refresh 7d.

## Architecture

Monorepo with two top-level directories:
- **`erp-server/`** — Spring Boot 3.2 + Maven multi-module backend (Java 17)
- **`erp-frontend/`** — pnpm monorepo with 3 client apps + shared packages

### Backend Module Layout

`erp-admin` is the runnable Spring Boot entry point (`@SpringBootApplication(scanBasePackages = "com.erp")`) that aggregates all modules. Common infrastructure lives in `erp-common/` sub-modules. Business modules each follow the same internal package structure:

```
com.erp.<module>/
  controller/        REST controllers
  domain/dto/        Request DTOs
  domain/entity/     MyBatis-Plus entities (extend BaseEntity)
  domain/vo/         Response VOs
  mapper/            MyBatis-Plus mapper interfaces
  service/           Service interfaces
  service/impl/      Service implementations
  permission/        PermissionCodes + PermissionDefinition + PermissionRegistry
```

**Business modules:** `erp-system`, `erp-product`, `erp-material`, `erp-purchase`, `erp-inventory`, `erp-sales`, `erp-production`, `erp-finance` (placeholder), `erp-hr` (placeholder), `erp-integration` (placeholder).

**Common infrastructure** (`erp-common/`): `erp-common-core` (BaseEntity, R\<T\>, PageVO, PermissionDefinition, GlobalExceptionHandler), `erp-common-security` (JWT filter, TokenService, SecurityUtils), `erp-common-mybatis` (type handlers), `erp-common-redis`, `erp-common-storage`, `erp-common-workflow` (Flowable), `erp-common-job` (XXL-Job).

### Frontend Workspace

- `apps/web` — React 18 + Vite + Ant Design 5 + ProComponents (main web client)
- `apps/desktop` — Tauri 2.x wrapping a React UI
- `apps/mobile` — Expo SDK 54 + React Native
- `packages/shared` — `@erp/shared` with shared types, constants, permissions

Frontend auth uses a manual reactive store (module-level state + pub/sub listeners), not Redux/Zustand. HTTP client is Axios with `baseURL: http://localhost:8080/api/v1`, request interceptor adds Bearer token + refresh token headers, response interceptor checks `code !== 200` and throws.

### API Conventions

- Base path: `/api/v1/...`
- Response: `R<T>` with `{ code, message, data, timestamp }`. Code 200 = success, 10004 = validation error, 10006 = unhandled server error.
- Pagination: `pageNum`/`pageSize` query params → `PageVO<T>`
- Auth: JWT Bearer in `Authorization`, refresh token in `X-Refresh-Token`
- Method security: `@PreAuthorize("hasAuthority(T(com.erp.<module>.permission.<Module>PermissionCodes).FIELD)")`
- Validation: `@Valid` on request bodies, `MethodArgumentNotValidException` handled by `GlobalExceptionHandler`

### Database

PostgreSQL with UUID primary keys. Flyway migrations in `erp-admin/src/main/resources/db/migration/` — version ranges: V1.0.x (system), V1.1.x (product), V1.2.x (material), V1.3.x (purchase/inventory), V1.4.x (sales), V1.5.x (production). MyBatis-Plus with underscore-to-camel mapping. Custom type handlers for `jsonb` (`JsonbStringTypeHandler`) and `text[]` (`StringArrayTypeHandler`) — entities must declare `@TableField(typeHandler = ...)`.

### Permission System

Permissions are **code-defined, not SQL-seeded**. Each module has:
- `*PermissionCodes.java` — `public static final String` constants using colon-delimited hierarchy: `module[:entity]:action` (e.g. `"product:category:list"`, `"product:export"`)
- `*PermissionDefinition.java` — enum implementing `PermissionDefinition` interface, defining a tree with `code`, `name`, `type` (1=directory, 2=menu, 3=button), `path`, `icon`, `sortOrder`, `parentCode`, `grantToAdminByDefault`
- `*PermissionRegistry.java` — Spring bean auto-wired by `PermissionBootstrap`

`PermissionBootstrap` (an `ApplicationRunner`) syncs code definitions → `sys_permission` table at startup: inserts new, updates existing, resolves parent links, auto-grants to ADMIN role where `grantToAdminByDefault=true`. `PermissionDefinitionValidator` ensures all enabled DB permissions exist in code.

Frontend permissions are in `packages/shared/src/constants/permissions.ts` — exported as `const` objects with string values matching backend codes exactly.

## Critical Development Rules

These rules come from AGENT_RULES.md and represent hard-won conventions:

1. **Read existing code before modifying** — don't imagine implementations from docs alone.
2. **Run compile checks after every change** — backend `mvn -q -DskipTests package`, frontend `tsc --noEmit`.
3. **Never add Flyway permission seed SQL** — permissions are code-defined and auto-synced at startup. Flyway is only for DDL (tables, indexes, constraints) and non-permission business seed data.
4. **Never hardcode permission strings** — use `*PermissionCodes` constants in backend and `permissions.ts` in frontend.
5. **Cross-module shared types go in `erp-common-core`** — if a type is used by two business modules, it must be in `erp-common/erp-common-core`.
6. **PostgreSQL `jsonb` and `text[]` columns** must use explicit type handlers with `@TableField(typeHandler = ...)`.
7. **Never write unvalidated strings to `jsonb` fields** — empty string → null; non-empty → validate JSON first.
8. **Never swallow exceptions** — `GlobalExceptionHandler` must log the real exception; frontend must show `message.error(...)` instead of empty state.
9. **Immutable fields** (`createdBy`, `createdAt`, `code`) must not be overwritten on updates.
10. **New module development order**: table structure → permissions → entity/DTO/VO/mapper → service/controller → frontend types/API → list+detail pages → CRUD → permission guards → JSON validation → compile check.
11. **Product detail pages** use top-bottom layout: list above, detail tabs below. Fixed heights with internal scroll — data should not push the page taller.
12. **Multi-SKU editing** uses independent table + single-row modal editing, never unconstrained nested forms.

## Commit Convention

Conventional Commits: `feat(scope): summary`, e.g. `feat(material): add Material and Supplier management modules`.

## Design Documents

- `AGENT_RULES.md` — mandatory development rules and prohibitions
- `dev-design.md` — DB schema, API specs, detailed technical design
- `prd.md` — product requirements document
- `tech-architecture.md` — overall technical architecture
- `TASK_TODO.md` — task checklist with completion status
