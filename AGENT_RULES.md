# AGENT_RULES

本文件是本仓库给 Codex / 其他智能体的高优先级开发规则。  
目标：减少重复踩坑，优先保证“实现方式正确”，再追求“实现速度”。

## 1. 通用原则

- 改代码前先读当前模块的真实实现，不要只按文档想象。
- 优先做“最小可闭环”能力，不要一次性铺太深。
- 每次改动后必须做对应编译校验：
  - 后端：`mvn -q -DskipTests package`
  - Web：`pnpm --filter @erp/web exec tsc -p tsconfig.json --noEmit`
- 遇到 `10006` 这类泛化错误时，优先补日志和真实异常输出，不要继续猜。

## 2. 权限规则

### 2.1 权限来源

- `sys_permission` / `sys_role_permission` 不再通过 Flyway 的 DML 做种子维护。
- 新增系统管理权限，必须修改：
  - `erp-server/erp-system/src/main/java/com/erp/system/permission/SystemPermissionCodes.java`
  - `erp-server/erp-system/src/main/java/com/erp/system/permission/SystemPermissionDefinition.java`
- 新增产品权限，必须修改：
  - `erp-server/erp-product/src/main/java/com/erp/product/permission/ProductPermissionCodes.java`
  - `erp-server/erp-product/src/main/java/com/erp/product/permission/ProductPermissionDefinition.java`

### 2.2 权限同步

- 权限写库靠启动同步器：
  - `PermissionBootstrap`
- 启用中的系统权限必须在代码定义中存在：
  - `PermissionDefinitionValidator`
- 后端 `@PreAuthorize` 必须引用常量类，不允许手写权限字符串。
- 前端权限判断必须优先引用：
  - `erp-frontend/packages/shared/src/constants/permissions.ts`

### 2.3 权限分层

- 功能权限：放 `sys_permission`，必须是树形结构
  - 目录 `type=1`
  - 菜单 `type=2`
  - 按钮/字段权限 `type=3`
- 数据权限：放在角色属性 `dataScope`，不要塞进权限树
- 字段权限：走权限码，但挂在对应菜单下面，不要做根节点平铺

## 3. Flyway 规则

- Flyway 只负责：
  - 建表
  - 外键 / 索引 / 分区
  - 必要基础业务种子
  - 历史数据迁移
- 不再新增“只为插权限”的迁移文件。
- 若当前仍处于开发期且允许换库，可以删除尚未稳定使用的权限 DML 迁移；但建表/结构迁移不能删。

## 4. 模块依赖规则

- 跨模块通用类型不得放在业务模块里反向引用。
- 已验证必须下沉公共层的内容：
  - `PageVO`
  - `PermissionDefinition`
  - `PermissionRegistry`
- 放置位置：
  - `erp-server/erp-common/erp-common-core`
- 如果一个类型会被 `erp-system` 和 `erp-product` 同时依赖，优先考虑下沉。

## 5. PostgreSQL 类型映射规则

- 遇到 PostgreSQL 特有字段，不能直接用普通 Java 类型想当然落库。
- 以下类型必须显式 type handler：
  - `jsonb`
  - `text[]`
- 当前可直接复用：
  - `JsonbStringTypeHandler`
  - `StringArrayTypeHandler`
- 实体字段必须显式声明：
  - `@TableField(typeHandler = ...)`

## 6. JSON 输入规则

- 后端不得直接把“任意字符串”写入 `jsonb` 字段。
- 空字符串写入 `jsonb` 前必须转 `null`。
- 非空字符串必须先做 JSON 合法性校验，再落库。
- 前端优先使用结构化编辑器，不要让用户手输 JSON 作为唯一交互：
  - 产品规格 `specifications`
  - SKU 属性 `attributes`
  - 包装尺寸 `dimensions`
  - 标签模板配置 `templateConfig` 可暂时保留 JSON 文本，但应有校验

## 7. 产品模块规则

### 7.1 产品基础信息

- 更新产品时，不要把 `createdBy` / `createdAt` / `code` 等不可变字段一起回写。
- 产品图片数组为空时写 `null`，不要写脏数组值。

### 7.2 SKU 规则

- 多 SKU 编辑必须用“独立表格 + 单条弹窗编辑”，不要用无约束的大块嵌套表单。
- 规格定义自动生成 SKU 时：
  - 默认策略必须是“补全缺失组合”
  - 覆盖重建必须二次确认

### 7.3 页面结构

- 产品详情页优先采用：
  - 上方列表
  - 下方详情 tabs
- 上下区域固定高度，内部滚动，不能让数据量撑长整页。

### 7.4 包装规格

- 同一产品的包装层级 `1/2/3` 只允许各一条，前后端都要校验。
- 包装规格 tab 必须展示层级换算摘要。
- 包装规格关联标签模板失败时，前端不得静默吞错。

## 8. 异常与日志规则

- `GlobalExceptionHandler` 必须打印真实异常。
- 前端加载失败时必须给出 `message.error(...)`，不要直接展示空状态冒充成功。
- 遇到“后端只返回 10006 / 前端空白无提示”时，优先补日志，而不是继续堆业务代码。

## 9. 交互实现优先级

- 先做最小业务闭环：
  - 列表
  - 详情
  - 创建
  - 编辑
  - 状态切换 / 删除
- 再做增强能力：
  - 自动生成
  - 可视化设计器
  - 真打印 / 真导出

## 10. 后续新增模块时的默认顺序

1. 表结构和权限定义
2. 实体 / DTO / VO / Mapper
3. Service / Controller
4. 前端类型和 API
5. 列表页 + 详情区
6. 创建 / 编辑
7. 权限控制
8. JSON / 特殊类型校验
9. 编译校验

## 11. 禁止项

- 不要新增 Flyway 权限种子 SQL
- 不要在前后端散落硬编码权限字符串
- 不要把跨模块公共类型放到业务模块里
- 不要无校验写入 `jsonb`
- 不要让多 SKU 用大块嵌套表单硬堆
- 不要吞掉异常后只显示空数据
