# Repository Guidelines

## Project Structure & Module Organization
`apps/web` contains the Vite + React admin client. Put domain pages under `src/pages/<domain>`, shared layouts in `src/layouts`, routes in `src/router`, and HTTP modules in `src/api`.

`apps/desktop` is a React desktop shell with native code in `src-tauri`. Keep browser-side code in `src/` and Rust/Tauri changes isolated to `src-tauri/src`.

`apps/mobile` is the Expo client. `App.tsx` currently hosts the main flow, while shared mobile concerns live in `src/api`, `src/store`, and `src/types`.

`packages/shared` holds cross-platform types, constants, and utilities. `packages/ui-web` and `packages/ui-mobile` are reusable UI package entry points.

## Build, Test, and Development Commands
`pnpm install` installs all workspace dependencies.

`pnpm dev:web` starts the web client with Vite.

`pnpm dev:desktop` launches the Tauri desktop app.

`pnpm dev:mobile` starts the Expo development server.

`pnpm build:web` produces the web production bundle.

`pnpm build:desktop` builds the desktop package.

`pnpm lint` runs workspace lint scripts; they are placeholder checks today, so do not treat this as full static analysis.

## Coding Style & Naming Conventions
Use TypeScript strict mode, 2-space indentation, double quotes, and the semicolon-free style already present in the repo.

Use PascalCase for components and pages such as `LoginPage.tsx`, camelCase for functions and variables, and domain-driven filenames such as `PurchaseOrderPage.tsx` or `src/api/system.ts`.

Keep reusable contracts in `packages/shared` and import them through workspace aliases like `@erp/shared`. Save edited files as UTF-8; some existing CJK strings are already misencoded, so avoid introducing more encoding drift.

## Testing Guidelines
No automated test runner is configured yet. Until one is added, smoke-test the specific target you changed with the relevant `pnpm dev:*` command and validate the affected login, navigation, and API flows manually.

When adding tests, prefer colocated `*.test.ts` or `*.test.tsx` files beside the module they cover.

## Commit & Pull Request Guidelines
Recent history follows Conventional Commits, usually `feat(scope): summary`, for example `feat(material): add Material and Supplier management modules`.

Keep commit subjects imperative and scoped by app or business domain when useful. Pull requests should name the affected targets (`web`, `desktop`, `mobile`, `shared`), describe backend/API assumptions, include screenshots for UI changes, and list manual verification steps.

## Configuration & Security Tips
Desktop and mobile support environment-based API endpoints via `VITE_API_BASE_URL` and `EXPO_PUBLIC_API_BASE_URL`. Keep environment-specific values out of source control, and document any deliberate endpoint changes in the PR.
