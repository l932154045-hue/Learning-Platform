# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 构建与运行

```bash
# 编译全部模块
mvn compile -q

# 编译单个模块（跳过测试）
mvn compile -pl user-service -am -q

# 运行单个服务（spring-boot-maven-plugin）
mvn spring-boot:run -pl gateway-service

# 打包全部模块
mvn package -DskipTests -q
```

项目需要 Java 17+、MySQL 8.0、Redis、RabbitMQ 3.x。每个服务有独立的 `application.yml`，数据库/中间件连接通过环境变量注入（默认值见 README 配置表，凭据通过 `application-local.yml` 覆盖）。

## 项目架构

Spring Boot 3.2 + Spring Cloud 2023.0.2（Gateway + OpenFeign）+ Spring Cloud Alibaba + MyBatis Plus 微服务项目。

```
gateway (8080) ──→ user-service      (8081, learning_user)
                → course-service    (8082, learning_course)
                → cart-service      (8083, learning_cart)
                → order-service     (8084, learning_order)
                → payment-service   (8085, learning_payment)
                → learning-service  (8086, learning_learning)
                → admin-service     (8087, 无数据库，纯代理)
```

**模块依赖链**: `common-core` → `common-security` → `common-web`。业务服务全部依赖这三个 common 模块。`common-cache`（Redis+Caffeine）和 `common-mq`（RabbitMQ）按需引入。`gateway-service` 只依赖 `common-security`（JWT 解析）。

## 认证流程

三层链路，不可绕过：

1. **Gateway `AuthGlobalFilter`**（order=-100）— 校验 JWT → 解析 userId/role → 写入 `X-User-Id`/`X-User-Role` header 转发给下游
2. **`UserInfoInterceptor`**（common-web）— 读取 header → `request.setAttribute("userId", ...)` / `request.setAttribute("role", ...)`
3. **Controller** — `@CurrentUser UserContext userContext` 通过 `CurrentUserArgumentResolver`（common-security）自动注入，提供 `getUserId()`/`getRole()`/`isAdmin()`

公开路径（跳过认证）：`/api/user/register`、`/api/user/login`、`/api/course/list`、`/api/course/detail`、`/api/course/category/tree`、`/api/course/hot`、`/api/learning/course`（startsWith 匹配）。

管理员接口通过 `userContext.isAdmin()` 校验（UserContext.ROLE_ADMIN == 1），admin-service 额外封装 `AdminAuthService.checkAdmin(role)`。

## 关键约定

- **统一响应**: `R<T>`（code/message/data/timestamp），静态工厂 `R.ok()` / `R.fail(code, msg)`
- **错误码**: `ResultCode` 枚举，4xx 业务错误（40001-40015），5xx 系统错误（50001-50005）
- **业务异常**: `throw new BizException(ResultCode.XXX)`，由 `GlobalExceptionHandler` 统一处理
- **分页**: 请求继承 `PageReq`（pageNum/pageSize/sort），响应用 `PageResp<T>`
- **实体基类**: 各服务自己定义 entity，无公共基类；ID 使用 `@TableId(type = IdType.AUTO)` 自增
- **DTO 分层**: `dto.req` 放请求体，`dto.resp` 放响应 VO，`dto.req` 中的 DTO 加 `@Valid` 注解
- **Feign 接口**: 放在调用方服务中（如 payment-service 中的 `OrderClient`），不在 common 中共享
- **服务发现**: Nacos 2.3.x，Gateway 通过 `lb://service-name` 路由，Feign 基于服务名调用

## 消息队列拓扑

```
order.topic (order-service 声明)
├── order.created → order.created.course / order.created.notify / order.payment.delay
└── order.paid   → order.paid.enrollment

order.dlx (Dead Letter Exchange, order-service 声明)
└── order.timeout → order.timeout.cancel (30min TTL 延迟取消)

course.topic (course-service + admin-service 双声明)
└── course.updated → course.updated.cache (CacheRefreshConsumer 精确驱逐)
```

**消费者**: `OrderTimeoutConsumer`(order) | `EnrollmentConsumer`(learning) | `CacheRefreshConsumer`(course)
**生产者**: `OrderEventProducer`(order) | `PaymentEventProducer`(payment) | `AdminEventProducer`(admin)

关键可靠性机制：Publisher Confirm + Return Callback（common-mq `RabbitCommonConfig`）| 消费者手动 ACK | 幂等消费 Redis SETNX | 分布式事务：`TransactionSynchronization.afterCommit()` 后发 MQ

## 缓存策略（course-service 两级缓存）

```
请求 → Caffeine(L1, 10min) → Redis(L2, 30min+随机偏移) → 分布式锁(SETNX) → MySQL
                             ↑ 空值缓存(2min) 防止穿透
```

热点课程用 Redis ZSet `course:hot:top10`；分类树 Redis 缓存 1h。admin-service 修改课程后发 `course.updated` MQ 通知，`CacheRefreshConsumer` 按 courseId 精确驱逐缓存，异常/未知事件兜底全量刷新。

## 已知局限

- **测试覆盖率** — 已补充 67 个核心链路单元测试（JUnit 5 + Mockito），仍有部分模块未覆盖
- `order.created.course` / `order.created.notify` 队列已声明但无消费者（课程统计/通知占位）
- admin-service 纯代理层，无审计日志/批量操作/操作历史

<!-- superpowers-zh:begin (do not edit between these markers) -->
# Superpowers-ZH 中文增强版

本项目已安装 superpowers-zh 技能框架（20 个 skills）。

## 核心规则

1. **收到任务时，先检查是否有匹配的 skill** — 哪怕只有 1% 的可能性也要检查
2. **设计先于编码** — 收到功能需求时，先用 brainstorming skill 做需求分析
3. **测试先于实现** — 写代码前先写测试（TDD）
4. **验证先于完成** — 声称完成前必须运行验证命令

## 可用 Skills

Skills 位于 `.claude/skills/` 目录，每个 skill 有独立的 `SKILL.md` 文件。

- **brainstorming**: 在任何创造性工作之前必须使用此技能——创建功能、构建组件、添加功能或修改行为。在实现之前先探索用户意图、需求和设计。
- **chinese-code-review**: 中文 review 沟通参考——话术模板、分级标注（必须修复/建议修改/仅供参考）、国内团队常见反模式应对。仅在用户显式 /chinese-code-review 时调用，不要根据上下文自动触发。
- **chinese-commit-conventions**: 中文 commit 与 changelog 配置参考——Conventional Commits 中文适配、commitlint/husky/commitizen 中文模板、conventional-changelog 中文配置。仅在用户显式 /chinese-commit-conventions 时调用，不要根据上下文自动触发。
- **chinese-documentation**: 中文文档排版参考——中英文空格、全半角标点、术语保留、链接格式、中文文案排版指北约定。仅在用户显式 /chinese-documentation 时调用，不要根据上下文自动触发。
- **chinese-git-workflow**: 国内 Git 平台配置参考——Gitee、Coding.net、极狐 GitLab、CNB 的 SSH/HTTPS/凭据/CI 接入差异与镜像同步配置。仅在用户显式 /chinese-git-workflow 时调用，不要根据上下文自动触发。
- **dispatching-parallel-agents**: 当面对 2 个以上可以独立进行、无共享状态或顺序依赖的任务时使用
- **executing-plans**: 当你有一份书面实现计划需要在单独的会话中执行，并设有审查检查点时使用
- **finishing-a-development-branch**: 当实现完成、所有测试通过、需要决定如何集成工作时使用——通过提供合并、PR 或清理等结构化选项来引导开发工作的收尾
- **mcp-builder**: MCP 服务器构建方法论 — 系统化构建生产级 MCP 工具，让 AI 助手连接外部能力
- **receiving-code-review**: 收到代码审查反馈后、实施建议之前使用，尤其当反馈不明确或技术上有疑问时——需要技术严谨性和验证，而非敷衍附和或盲目执行
- **requesting-code-review**: 完成任务、实现重要功能或合并前使用，用于验证工作成果是否符合要求
- **subagent-driven-development**: 当在当前会话中执行包含独立任务的实现计划时使用
- **systematic-debugging**: 遇到任何 bug、测试失败或异常行为时使用，在提出修复方案之前执行
- **test-driven-development**: 在实现任何功能或修复 bug 时使用，在编写实现代码之前
- **using-git-worktrees**: 当需要开始与当前工作区隔离的功能开发，或在执行实现计划之前使用——通过原生工具或 git worktree 回退机制确保隔离工作区存在
- **using-superpowers**: 在开始任何对话时使用——确立如何查找和使用技能，要求在任何响应（包括澄清性问题）之前调用 Skill 工具
- **verification-before-completion**: 在宣称工作完成、已修复或测试通过之前使用，在提交或创建 PR 之前——必须运行验证命令并确认输出后才能声称成功；始终用证据支撑断言
- **workflow-runner**: 在 Claude Code / OpenClaw / Cursor 中直接运行 agency-orchestrator YAML 工作流——无需 API key，使用当前会话的 LLM 作为执行引擎。当用户提供 .yaml 工作流文件或要求多角色协作完成任务时触发。
- **writing-plans**: 当你有规格说明或需求用于多步骤任务时使用，在动手写代码之前
- **writing-skills**: 当创建新技能、编辑现有技能或在部署前验证技能是否有效时使用

## 如何使用

当任务匹配某个 skill 时，使用 `Skill` 工具加载对应 skill 并严格遵循其流程。绝不要用 Read 工具读取 SKILL.md 文件。

如果你认为哪怕只有 1% 的可能性某个 skill 适用于你正在做的事情，你必须调用该 skill 检查。
<!-- superpowers-zh:end -->
