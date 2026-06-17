# 学途 — 在线课程平台

前后端分离的微服务在线教育平台。用户端浏览课程、下单、在线学习；管理后台通过 admin-service 代理管理课程、分类及用户。

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot + Spring Cloud | 3.2 / 2023.0.2 |
| 网关 | Spring Cloud Gateway | — |
| 服务发现 | Nacos（开发环境默认禁用） | — |
| ORM | MyBatis Plus | 3.5 |
| 数据库 | MySQL（每服务独立库） | 8.0 |
| 缓存 | Redis + Caffeine（两级缓存） | 7 / 3.x |
| 消息队列 | RabbitMQ（Topic + DLX） | 3.x |
| 认证 | JWT (jjwt) + BCrypt | 0.12 |
| 前端 | Vue 3 + Vite + Element Plus + Pinia | — |
| 测试 | JUnit 5 + Mockito | — |
| 构建 | Maven（后端）/ npm（前端） | 3.8+ / 18+ |

## 项目结构

```
online-course-platform/
├── common/                         # 公共模块（按依赖链组织）
│   ├── common-core/                #   统一响应 R、ResultCode 枚举、BizException、分页
│   ├── common-security/            #   JWT 签发解析、UserContext、CurrentUser 注解及解析器
│   ├── common-web/                 #   CORS、UserInfoInterceptor、WebMvc 配置
│   ├── common-cache/               #   Redis 模板配置
│   └── common-mq/                  #   RabbitMQ 公共配置 + 共享消息类
├── gateway-service/                # API 网关 :8080 — JWT 鉴权 + 路由转发
├── user-service/                   # 用户服务 :8081 — 注册/登录/信息管理
├── course-service/                 # 课程服务 :8082 — 课程/分类/视频 + 两级缓存
├── cart-service/                   # 购物车服务 :8083 — 购物车 CRUD + Redis 缓存
├── order-service/                  # 订单服务 :8084 — 订单生命周期 + MQ 生产者
├── payment-service/                # 支付服务 :8085 — 模拟支付 + 幂等
├── learning-service/               # 学习服务 :8086 — 报名/进度/评价 + MQ 消费者
├── admin-service/                  # 管理服务 :8087 — 纯代理层（无数据库），Feign 转发到下游
├── course-platform-web/            # 用户端前端（Vue 3）
├── shared/                         # 前端共享 TypeScript 类型定义
├── docker/init/init.sql            # 数据库 DDL + 种子数据
└── docker-compose.yml              # MySQL + Redis + RabbitMQ 容器编排
```

**模块依赖链**：`common-core` → `common-security` → `common-web`，业务服务全部依赖这三个模块。`common-cache` 和 `common-mq` 按需引入；gateway-service 只依赖 `common-security`。

## 微服务拓扑

```
                        ┌─────────────┐
                        │   Gateway   │  :8080
                        │ JWT 鉴权+路由 │
                        └──────┬──────┘
           ┌───────┬───────┬───┼───┬───────┬───────┬───────┐
           ▼       ▼       ▼   │   ▼       ▼       ▼       ▼
        user   course   cart   │ order  payment learning admin
        :8081  :8082   :8083   │ :8084  :8085    :8086    :8087
        ┌───┐   ┌───┐   ┌───┐  │ ┌───┐   ┌───┐   ┌───┐   (无DB)
        │DB │   │DB │   │DB │  │ │DB │   │DB │   │DB │
        │R  │   │R  │   │R  │  │ │MQ │   │R  │   │R  │
        └───┘   │Caf│   └───┘  │ │R  │   │MQ │   │MQ │
                └───┘          │ └───┘   └───┘   └───┘
                               └──────────┬──────────┘
                                          │
                                    RabbitMQ :5672
```

| 服务 | 端口 | 数据库 | 中间件 |
|------|------|--------|--------|
| user-service | 8081 | learning_user | Redis |
| course-service | 8082 | learning_course | Redis + Caffeine + MQ |
| cart-service | 8083 | learning_cart | Redis |
| order-service | 8084 | learning_order | RabbitMQ + Redis |
| payment-service | 8085 | learning_payment | RabbitMQ + Redis |
| learning-service | 8086 | learning_learning | RabbitMQ + Redis |
| admin-service | 8087 | —（代理层） | RabbitMQ |

## 快速开始

### 环境要求

- JDK 17+、Maven 3.8+、Docker 20+、Node.js 18+（仅前端）

### 1. 启动基础设施

```bash
docker compose up -d
```

启动 MySQL 8.0（:3306，root/040615）、Redis 7（:6379，密码 040615）、RabbitMQ 3.x（:5672 + 管理面板 :15672，root/040615），并自动创建 6 个独立数据库。

> **注意**：各服务 `application.yml` 中数据库/中间件密码默认值为空，需通过环境变量注入或创建 `application-local.yml`（已 gitignore）覆盖。docker-compose 的凭据与本地配置一致可无缝使用。

### 2. 编译

```bash
mvn compile -q
```

### 3. 启动服务

```bash
# 逐一启动（推荐，便于观察日志）
mvn spring-boot:run -pl gateway-service
mvn spring-boot:run -pl user-service
# ...

# 或一次性启动全部
mvn spring-boot:run -pl gateway-service,user-service,course-service,cart-service,order-service,payment-service,learning-service,admin-service
```

### 4. 启动前端（可选）

```bash
# 用户端 → http://localhost:5173
cd course-platform-web && npm install && npm run dev
```

### 5. 验证

```bash
# 公开接口
curl http://localhost:8080/api/course/hot

# 注册
curl -X POST http://localhost:8080/api/user/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"test","password":"123456","phone":"13800138000"}'

# 登录（返回 token）
curl -X POST http://localhost:8080/api/user/login \
  -H 'Content-Type: application/json' \
  -d '{"phone":"13800138000","password":"123456"}'
```

## API 总览

### 公开接口（无需登录）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/user/register` | 注册 |
| POST | `/api/user/login` | 登录 |
| GET | `/api/course/list` | 课程列表（分页 + 分类筛选） |
| GET | `/api/course/detail/{id}` | 课程详情 |
| GET | `/api/course/category/tree` | 分类树 |
| GET | `/api/course/hot` | 热门课程 Top10 |
| GET | `/api/learning/course/{id}/reviews` | 课程评价 |

### 需登录（`Authorization: Bearer <token>`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/user/info` | 个人信息 |
| PUT | `/api/user/info` | 修改个人信息 |
| PUT | `/api/user/password` | 修改密码 |
| GET | `/api/cart/list` | 购物车列表 |
| POST | `/api/cart/add` | 加入购物车 |
| DELETE | `/api/cart/remove/{courseId}` | 移出购物车 |
| DELETE | `/api/cart/clear` | 清空购物车 |
| POST | `/api/order/create` | 创建订单 |
| GET | `/api/order/list` | 订单列表 |
| GET | `/api/order/detail/{id}` | 订单详情 |
| PUT | `/api/order/cancel/{id}` | 取消订单 |
| POST | `/api/payment/pay/{orderId}` | 模拟支付 |
| GET | `/api/payment/result/{orderId}` | 支付结果查询 |
| GET | `/api/learning/my-courses` | 我的课程 |
| GET | `/api/learning/progress/{courseId}` | 学习进度 |
| PUT | `/api/learning/progress/report` | 上报学习进度 |
| POST | `/api/learning/review` | 提交课程评价 |

### 管理员接口（role=1）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/dashboard/stats` | 仪表盘统计 |
| GET | `/api/admin/course/list` | 课程列表（含状态筛选） |
| POST | `/api/admin/course` | 创建课程 |
| PUT | `/api/admin/course/{id}` | 编辑课程 |
| DELETE | `/api/admin/course/{id}` | 删除课程 |
| PUT | `/api/admin/course/{id}/status` | 上架/下架 |
| POST | `/api/admin/course/{id}/video` | 添加视频章节 |
| PUT | `/api/admin/course/video/{id}` | 编辑视频章节 |
| DELETE | `/api/admin/course/video/{id}` | 删除视频章节 |
| POST | `/api/admin/category` | 创建分类 |
| PUT | `/api/admin/category/{id}` | 编辑分类 |
| DELETE | `/api/admin/category/{id}` | 删除分类 |
| GET | `/api/admin/order/list` | 订单列表 |
| PUT | `/api/admin/order/{id}/status` | 更新订单状态 |
| GET | `/api/admin/user/list` | 用户列表 |
| PUT | `/api/admin/user/{id}/status` | 禁用/启用用户 |

## 认证链路

三层校验，不可绕过：

```
客户端                     Gateway                   下游服务                Controller
  │  Authorization:          │                          │                       │
  │  Bearer <token> ────────▶│ AuthGlobalFilter          │                       │
  │                          │ ├─ 跳过白名单路径           │                       │
  │                          │ ├─ 解析 JWT → userId/role │                       │
  │                          │ └─ 写入 X-User-Id /       │                       │
  │                          │    X-User-Role header ───▶│ UserInfoInterceptor    │
  │                          │                          │ ├─ 读取 header          │
  │                          │                          │ └─ request.setAttribute │
  │                          │                          │    ("userId","role") ──▶│ @CurrentUser
  │                          │                          │                       │ UserContext
```

1. **Gateway `AuthGlobalFilter`**（order=-100）— 校验 JWT，解析 userId/role，写入 `X-User-Id`/`X-User-Role` header 转发下游
2. **`UserInfoInterceptor`**（common-web）— 读取 header → `request.setAttribute("userId", ...)` / `request.setAttribute("role", ...)`
3. **Controller** — `@CurrentUser UserContext userContext` 通过 `CurrentUserArgumentResolver` 自动注入，提供 `getUserId()` / `getRole()` / `isAdmin()`

- **白名单路径**（跳过认证）：`/api/user/register`、`/api/user/login`、`/api/course/list`、`/api/course/detail`、`/api/course/category/tree`、`/api/course/hot`、`/api/learning/course`（前缀匹配）
- **管理员校验**：`userContext.isAdmin()` — UserContext 内置 `ROLE_ADMIN = 1` 常量
- **Token 有效期**：7 天（604800000ms），HMAC-SHA256 签发

## 核心业务链路

```
浏览课程 → 加入购物车 → 创建订单 → 模拟支付 → 自动报名 → 视频学习 → 提交评价
                                      │                      │
                                      ▼                      ▼
                                30min 超时取消            进度防刷
                               (DLX 延迟队列)          (只进不退校验)
```

### 订单超时取消

```
order.created → order.payment.delay 队列 (TTL 30min)
              → 30min 后消息到期 → order.dlx (Dead Letter)
              → order.timeout.cancel 队列
              → OrderTimeoutConsumer → 检查状态 → 标记取消
```

### 进度防刷

`PUT /api/learning/progress/report` — Redis 存储进度，提交值必须 ≥ 已存值，否则抛 `BizException`。使用 Integer 秒数，非视频播放器时间戳。

## 消息队列

### 拓扑

```
order.topic (order-service 声明)
├── order.created ──┬── order.created.course   (课程统计 — 待实现)
│                   ├── order.created.notify   (通知 — 占位)
│                   └── order.payment.delay    (TTL 30min → DLX)
└── order.paid ─────── order.paid.enrollment   (EnrollmentConsumer)

order.dlx (order-service 声明)
└── order.timeout ─── order.timeout.cancel     (OrderTimeoutConsumer)

course.topic (course-service + admin-service 双声明)
└── course.updated ─── course.updated.cache    (CacheRefreshConsumer)
```

### 生产者/消费者

| 消息 | 生产者 | 消费者 | 说明 |
|------|--------|--------|------|
| `order.created` | OrderEventProducer (order) | delay 队列 / 占位 | 创建订单后发送 |
| `order.paid` | PaymentEventProducer (payment) | EnrollmentConsumer (learning) | 支付成功后发送，触发自动报名 |
| `order.timeout` | DLX 自动路由 | OrderTimeoutConsumer (order) | 30min 未支付自动取消 |
| `course.updated` | AdminEventProducer (admin) | CacheRefreshConsumer (course) | 课程变更后精确驱逐缓存 |

### 可靠性机制

- **Publisher Confirm + Return Callback**（common-mq `RabbitCommonConfig`）
- **消费者手动 ACK**（`acknowledge-mode: manual`）
- **幂等消费**：Redis SETNX（enroll 锁 72h、pay 锁 60s）
- **分布式事务**：`TransactionSynchronization.afterCommit()` 后发 MQ

## 缓存策略

course-service 实现两级缓存，cart-service 使用单级 Redis 缓存：

### course-service 两级缓存

```
请求 → Caffeine 本地缓存 (L1, 10min TTL, max 1000 条)
     → Redis 分布式缓存 (L2, 30min + 1-5min 随机偏移防雪崩)
     → Redis SETNX 分布式锁（防击穿，10s TTL，3 次自旋重试）
     → MySQL
     → 空值缓存 (2min TTL，防穿透)
```

### 缓存 Key

| Key | 类型 | TTL | 说明 |
|-----|------|-----|------|
| `course:detail:{id}` | Value | 30min + 随机偏移 | 课程详情 VO |
| `lock:course:{id}` | Value (SETNX) | 10s | 分布式锁 |
| `course:category:tree` | Value | 1h | 分类树 |
| `course:category:list` | Value | 1h | 分类平铺列表 |
| `course:hot:top10` | ZSet | — | 热门课程排名 |
| `cart:user:{userId}` | Set | 7d | 购物车 |

### 缓存失效

| 触发条件 | 驱逐范围 | 方式 |
|----------|---------|------|
| 课程更新/下架 | 单课程详情 + 热门 | `CacheRefreshConsumer` MQ 消费 |
| 课程删除 | 单课程详情 + 热门 + 分类树 | `CacheRefreshConsumer` MQ 消费 |
| 分类变更 | 分类树 + 分类列表 | `CacheRefreshConsumer` MQ 消费 |
| 未知事件/异常 | 全部缓存 | `refreshAllCaches()` 兜底 |
| 管理后台手动刷新 | 全部缓存 | `POST /api/course/cache/refresh` |

## 配置

所有中间件地址和凭据通过环境变量注入：

| 环境变量 | 默认值 | 说明 |
|----------|--------|------|
| `MYSQL_HOST` | `192.168.100.128` | MySQL 地址 |
| `MYSQL_USER` | `root` | MySQL 用户名 |
| `MYSQL_PWD` | (空) | MySQL 密码 |
| `REDIS_HOST` | `192.168.100.128` | Redis 地址 |
| `REDIS_PWD` | (空) | Redis 密码 |
| `RABBITMQ_HOST` | `192.168.100.128` | RabbitMQ 地址 |
| `RABBITMQ_USER` | `root` | RabbitMQ 用户名 |
| `RABBITMQ_PWD` | (空) | RabbitMQ 密码 |
| `NACOS_SERVER` | `localhost:8848` | Nacos 地址（当前禁用） |

凭据通过 `application-local.yml`（已 gitignore）覆盖，与 docker-compose.yml 中的 `040615` 保持一致即可直连。

## 已知局限

- **测试覆盖率**：已补充 67 个核心链路单元测试（JUnit 5 + Mockito），覆盖 key Service/MQ/缓存，部分模块仍待补充
- **Nacos**：已配置但所有服务 `enabled: false`，服务间走 localhost 直连
- **队列占位**：`order.created.course` 和 `order.created.notify` 已声明队列及绑定，无对应消费者
- **admin-service**：纯代理层，无审计日志、无批量操作、无操作历史

## License

MIT
