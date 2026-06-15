# 学途 — 在线课程平台

前后端分离的微服务在线教育平台。用户可浏览课程、下单购买、在线学习；管理员可管理课程、分类和用户。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2 + Spring Cloud 2023.0.2 |
| 网关 | Spring Cloud Gateway |
| 服务发现 | Nacos（当前禁用，使用静态路由） |
| 数据库 | MySQL 8.0（每服务独立数据库） |
| ORM | MyBatis Plus 3.5 |
| 缓存 | Redis 7 + Caffeine（两级缓存） |
| 消息队列 | RabbitMQ 3.x |
| 认证 | JWT（jjwt 0.12）+ BCrypt |
| 前端 | Vue 3 + Vite + Element Plus + Pinia |
| 构建 | Maven（后端）/ npm（前端） |

## 项目结构

```
online-course-platform/
├── common/                   # 公共模块
│   ├── common-core/          # 统一响应 R、异常、分页
│   ├── common-security/      # JWT 工具、用户信息拦截器
│   ├── common-web/           # CORS、WebMvc 配置
│   ├── common-cache/         # Redis + Caffeine 配置
│   └── common-mq/            # RabbitMQ 公共配置
├── gateway-service/          # API 网关（8080）
├── user-service/             # 用户服务（8081）
├── course-service/           # 课程服务（8082）
├── cart-service/             # 购物车服务（8083）
├── order-service/            # 订单服务（8084）
├── payment-service/          # 支付服务（8085）
├── learning-service/         # 学习服务（8086）
├── admin-service/            # 管理服务（8087）
├── course-platform-web/      # 用户端前端（Vue 3）
├── course-platform-admin/    # 管理后台前端（Vue 3）
├── shared/                   # 前端共享类型定义
├── docker/                   # Docker 初始化脚本
│   └── init/init.sql         # 数据库自动创建
└── docker-compose.yml        # 基础设施容器编排
```

## 微服务架构

```
                    ┌─────────────┐
                    │   Gateway   │  :8080  (JWT 鉴权 + 路由)
                    └──────┬──────┘
       ┌───────┬───────┬───┼───┬───────┬───────┬───────┐
       ▼       ▼       ▼   │   ▼       ▼       ▼       ▼
    user   course   cart   │ order  payment learning admin
    :8081  :8082   :8083   │ :8084  :8085    :8086    :8087
       │       │       │   │   │       │       │       │
       ▼       ▼       ▼   │   ▼       ▼       ▼       │
    MySQL   MySQL   MySQL   │ MySQL   MySQL   MySQL      │
    Redis   Redis   Redis   │  MQ     Redis    Redis     │
            Caffeine        │ Redis    MQ       MQ      │
                             └──────────────────────────┘
```

每个服务拥有独立的数据库：

| 服务 | 数据库 |
|------|--------|
| user-service | learning_user |
| course-service | learning_course |
| cart-service | learning_cart |
| order-service | learning_order |
| payment-service | learning_payment |
| learning-service | learning_learning |

## 快速开始

### 环境要求

| 工具 | 版本 |
|------|------|
| JDK | 17+ |
| Maven | 3.8+ |
| Docker | 20+（运行基础设施） |
| Node.js | 18+（运行前端） |

### 1. 启动基础设施

```bash
docker compose up -d
```

启动 MySQL（3306）、Redis（6379）、RabbitMQ（5672 + 管理界面 15672），自动创建 6 个数据库。

### 2. 编译打包

```bash
mvn clean package -DskipTests
```

### 3. 启动后端服务

```bash
# 一键启动全部服务
mkdir -p logs
for svc in gateway user course cart order payment learning admin; do
  nohup java -jar ${svc}-service/target/${svc}-service-1.0.0-SNAPSHOT.jar > logs/${svc}.log 2>&1 &
  echo "Started ${svc}-service"
done
```

### 4. 启动前端（可选）

```bash
# 用户端（http://localhost:5173）
cd course-platform-web
npm install && npm run dev

# 管理后台（http://localhost:5174）
cd course-platform-admin
npm install && npm run dev
```

### 5. 验证

```bash
# 测试网关
curl http://localhost:8080/api/course/hot

# 注册用户
curl -X POST http://localhost:8080/api/user/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"test","password":"123456","phone":"13800138000"}'

# 登录
curl -X POST http://localhost:8080/api/user/login \
  -H 'Content-Type: application/json' \
  -d '{"phone":"13800138000","password":"123456"}'
```

## API 概览

### 公开接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/user/register` | 注册 |
| POST | `/api/user/login` | 登录 |
| GET | `/api/course/list` | 课程列表（分页+筛选） |
| GET | `/api/course/detail/{id}` | 课程详情 |
| GET | `/api/course/category/tree` | 分类树 |
| GET | `/api/course/hot` | 热门课程 |
| GET | `/api/learning/course/{id}/reviews` | 课程评价 |

### 需登录（Bearer Token）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/user/info` | 个人信息 |
| PUT | `/api/user/info` | 修改信息 |
| GET | `/api/cart/list` | 购物车 |
| POST | `/api/cart/add` | 加入购物车 |
| DELETE | `/api/cart/remove/{courseId}` | 移出购物车 |
| POST | `/api/order/create` | 创建订单 |
| GET | `/api/order/list` | 订单列表 |
| POST | `/api/payment/pay/{orderId}` | 支付 |
| GET | `/api/learning/my-courses` | 我的课程 |
| PUT | `/api/learning/progress/report` | 上报学习进度 |
| POST | `/api/learning/review` | 提交评价 |

### 管理员接口（role=1）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/admin/course` | 创建课程 |
| PUT | `/api/admin/course/{id}` | 编辑课程 |
| DELETE | `/api/admin/course/{id}` | 删除课程 |
| POST | `/api/admin/category` | 创建分类 |
| PUT | `/api/admin/user/{id}/status` | 禁用/启用用户 |

## 认证机制

```
客户端 → Gateway（AuthGlobalFilter 校验 JWT）
       → 解析 userId / role
       → 写入 X-User-Id / X-User-Role header
       → 转发到下游服务
       → UserInfoInterceptor 读取 header
       → Controller 通过 @RequestAttribute 获取
```

- Token 有效期：7 天
- 签发方式：HMAC-SHA256
- 请求头：`Authorization: Bearer <token>`

## 业务核心链路

```
浏览课程 → 加入购物车 → 创建订单 → 模拟支付 → 自动报名 → 视频学习 → 提交评价
                                    │                    │
                                    ▼                    ▼
                              订单超时取消(30min)      进度防作弊(只进不退)
```

## 消息队列

```
order.topic (TopicExchange)
├── order.created → 订单创建通知 / 延迟队列
└── order.paid → 自动报名

order.dlx (Dead Letter Exchange)
└── order.timeout → 30min 超时取消
```

## 缓存策略

course-service 实现两级缓存：

```
请求 → Caffeine 本地缓存 (L1, 10min)
     → Redis 分布式缓存 (L2, 30min+随机偏移)
     → 分布式锁(SETNX) 防止击穿
     → MySQL
     → 空值缓存 防止穿透(2min)
```

## 配置说明

所有中间件连接通过环境变量配置，均有默认值：

| 环境变量 | 默认值 | 说明 |
|----------|--------|------|
| `MYSQL_HOST` | `localhost` | MySQL 地址 |
| `MYSQL_USER` | `root` | MySQL 用户名 |
| `MYSQL_PWD` | `root` | MySQL 密码 |
| `REDIS_HOST` | `localhost` | Redis 地址 |
| `REDIS_PWD` | (空) | Redis 密码 |
| `RABBITMQ_HOST` | `localhost` | RabbitMQ 地址 |
| `RABBITMQ_USER` | `guest` | RabbitMQ 用户名 |
| `RABBITMQ_PWD` | `guest` | RabbitMQ 密码 |
| `NACOS_SERVER` | `localhost:8848` | Nacos 地址（当前未启用） |

使用 Docker Compose 启动基础设施时，默认值即可直接使用，无需额外配置。

## License

MIT
