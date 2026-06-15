# Repository Guidelines

## Project Structure & Module Organization
This repository contains a Spring Boot backend and a pnpm frontend workspace.

`erp-server` is a Maven multi-module backend. `erp-admin` is the runnable admin service and owns Liquibase changelogs in `erp-admin/src/main/resources/db/changelog`. Domain modules include `erp-system`, `erp-product`, `erp-material`, `erp-inventory`, `erp-sales`, `erp-purchase`, and `erp-production`; keep controllers, services, mappers, and entities in the matching domain module.

`erp-frontend` is a pnpm workspace. `apps/web` is the React + Vite admin client, `apps/desktop` is the Tauri desktop app, and `apps/mobile` is the Expo app. Shared contracts and constants belong in `packages/shared`; reusable UI packages live in `packages/ui-web` and `packages/ui-mobile`.

## Build, Test, and Development Commands
Backend:

```powershell
cd erp-server
mvn clean package -DskipTests
cd erp-admin
mvn spring-boot:run
```

Frontend:

```powershell
cd erp-frontend
pnpm install
pnpm dev:web
pnpm dev:desktop
pnpm dev:mobile
pnpm build:web
pnpm build:desktop
pnpm lint
```

`pnpm lint` currently delegates to workspace lint scripts; some are placeholder checks, so pair it with a targeted manual smoke test.

## Coding Style & Naming Conventions
Use Java 17 conventions in the backend, with package names under `com.erp`. Prefer domain-aligned class names such as `ProductionBatchController`, `ProductService`, and `MaterialMapper`.

Use TypeScript for frontend code, PascalCase for React components/pages, camelCase for variables and functions, and domain-driven file names such as `ProductionReportPage.tsx` or `src/api/production.ts`. Keep shared permission keys, types, and constants in `@erp/shared`.

## Testing Guidelines
No comprehensive automated test suite is configured yet. Build the affected target and manually verify the changed workflow in the matching app. When adding tests, use colocated `*.test.ts`, `*.test.tsx`, or Java module tests under `src/test/java`.

## Database Migration Guidelines
Liquibase uses `erp-admin/src/main/resources/db/changelog/db.changelog-master.yaml` to include table-level formatted SQL files under `db/changelog/tables`. Add each logical SQL statement as its own `--changeset`, and place it in the file for the affected table, such as `041_production_batch.sql`. Avoid editing changesets that have already been shared or executed.

## Commit & Pull Request Guidelines
Recent commits follow Conventional Commits, for example `feat(material): add Material and Supplier management modules`. Use concise imperative subjects, scoped by app or business domain when useful.

Pull requests should describe changed modules, list backend/API assumptions, include screenshots for UI changes, and document verification commands or manual test steps.
