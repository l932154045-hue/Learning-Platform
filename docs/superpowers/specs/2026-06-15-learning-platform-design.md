# 企业级在线课程学习平台 — 架构设计文档

> 版本: v1.0  
> 日期: 2026-06-15  
> 状态: 设计完成，待实现

---

## 1. 项目概述

### 1.1 项目定位

企业级在线课程学习平台，对标腾讯课堂、慕课网。面向学员提供课程浏览、购买、学习一站式体验，面向管理员提供课程与用户管理后台。

### 1.2 技术栈

| 层次 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 17 |
| 框架 | Spring Boot | 3.x |
| 微服务 | Spring Cloud Alibaba | 2023.x |
| ORM | MyBatis Plus | 3.5.x |
| 数据库 | MySQL | 8.0 |
| 缓存 | Redis + Caffeine | 7.x / 3.x |
| 消息队列 | RabbitMQ | 3.12 |
| 注册/配置 | Nacos | 2.3.x |
| 网关 | Spring Cloud Gateway | - |
| 流量治理 | Sentinel | 1.8.x |
| 部署 | Docker Compose | - |

### 1.3 核心功能

1. 用户注册登录
2. 课程浏览（分类 / 搜索 / 热门排行）
3. 购物车
4. 下单购买（Mock 支付）
5. 我的课程
6. 视频学习进度
7. 课程评价
8. 后台课程管理

### 1.4 架构决策

| 决策点 | 选择 | 理由 |
|--------|------|------|
| 微服务拆分粒度 | 7 个业务服务（方案 B） | DDD 边界清晰，校招面试标准，复杂度适中 |
| 课程内容模式 | 纯视频课程 | - |
| 视频存储 | 阿里云 OSS + CDN | Spring Cloud Alibaba 生态天然集成 |
| 用户角色 | 学员 + 管理员 | 双角色模型 |
| 并发规模 | 中等（日活 1-5 万，峰值 QPS 500-2000） | - |
| 服务间通信 | OpenFeign（同步查询）+ RabbitMQ（异步命令） | CQRS 雏形，读写分离 |
| 鉴权方案 | JWT 无状态 Token，Gateway 统一解析 | 避免各服务重复鉴权 |
| 分层结构 | Controller → Service → Mapper + DTO/Entity/Config/Cache/MQ | 传统分层 |

---

## 2. 项目总体架构图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          客户端层 (Client)                               │
│                         Web (Vue3)  │  Admin (Vue3)                      │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │  HTTPS
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    网关层 - Spring Cloud Gateway                         │
│   路由转发 │ 统一鉴权(JWT) │ Sentinel限流 │ CORS │ 请求日志               │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │  JWT Token 透传
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                   服务注册 & 配置中心 - Nacos                             │
│         服务发现 │ 配置管理 │ 健康检查 │ 动态路由                           │
└─────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────── 业务服务层 ────────────────────────────────┐
│                                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐                   │
│  │ user-service  │  │course-service│  │ cart-service  │                   │
│  │  用户中心     │  │  课程中心     │  │  购物车       │                   │
│  │  :8081       │  │  :8082       │  │  :8083        │                   │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘                   │
│         │                 │                 │                            │
│  ┌──────┴───────┐  ┌──────┴───────┐  ┌──────┴───────┐                   │
│  │ order-service │  │payment-service│ │learning-service│                 │
│  │  订单中心     │  │  支付中心      │ │  学习中心      │                 │
│  │  :8084       │  │  :8085        │ │  :8086         │                 │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘                   │
│         │                 │                 │                            │
│         │  ┌──────────────┴──────────────┐  │                            │
│         │  │     admin-service           │  │                            │
│         │  │     管理后台 :8087          │  │                            │
│         │  └────────────────────────────┘  │                            │
└─────────┼──────────────────────────────────┼────────────────────────────┘
          │                                  │
          ▼                                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        中间件层 (Middleware)                             │
│                                                                         │
│  ┌─────────┐  ┌──────────┐  ┌──────────┐  ┌─────────────┐              │
│  │ MySQL 8 │  │  Redis   │  │ RabbitMQ │  │ 阿里云 OSS  │              │
│  │ 读写分离 │  │ 二级缓存  │  │ 消息队列  │  │ 视频存储+CDN│              │
│  └─────────┘  └──────────┘  └──────────┘  └─────────────┘              │
│                                                                         │
│  ┌──────────────────────────────────────────┐                           │
│  │         Sentinel Dashboard               │                           │
│  │         流量监控 │ 熔断降级               │                           │
│  └──────────────────────────────────────────┘                           │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                      基础设施层 - Docker Compose                         │
│  Nacos │ MySQL │ Redis │ RabbitMQ │ Sentinel Dashboard │ 各微服务        │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 3. 微服务拆分方案

### 3.1 服务矩阵

| 服务名称 | 端口 | 限界上下文 | 核心职责 |
|----------|------|-----------|---------|
| gateway-service | 8080 | 网关 | 路由、鉴权、限流 |
| user-service | 8081 | 用户上下文 | 注册/登录/个人信息 |
| course-service | 8082 | 课程上下文 | 课程CRUD/分类/搜索 |
| cart-service | 8083 | 购物车上下文 | 购物车/暂存 |
| order-service | 8084 | 订单上下文 | 下单/订单状态 |
| payment-service | 8085 | 支付上下文 | Mock支付/支付回调 |
| learning-service | 8086 | 学习上下文 | 学习记录/进度/评价 |
| admin-service | 8087 | 管理上下文 | 课程管理/用户管理 |

### 3.2 项目分包结构（以 course-service 为例）

```
course-service/
├── controller/              ← 接口层
│   └── CourseController.java
├── service/                 ← 业务逻辑层
│   ├── CourseService.java
│   ├── impl/
│   │   └── CourseServiceImpl.java
│   └── CourseSearchService.java
├── mapper/                  ← 数据访问层 (MyBatis Plus)
│   └── CourseMapper.java
├── entity/                  ← 实体类 (对应数据库表)
│   └── Course.java
├── dto/                     ← 数据传输对象
│   ├── req/
│   │   └── CourseSearchReq.java
│   └── resp/
│       └── CourseDetailVO.java
├── enums/                   ← 枚举
│   └── CourseStatusEnum.java
├── config/                  ← 配置类
│   └── MyBatisPlusConfig.java
├── cache/                   ← 缓存模块
│   └── CourseCacheService.java
├── mq/                      ← 消息队列
│   ├── producer/
│   │   └── CourseEventProducer.java
│   └── consumer/
│       └── CourseEventListener.java
├── common/                  ← 服务内部公共类
│   └── CourseConstants.java
└── util/                    ← 工具类
    └── CourseUtil.java
```

所有 7 个业务服务均按此结构组织，保持一致。

### 3.3 服务间通信矩阵

```
                 user    course   cart    order   payment learning admin
user-service      -       F       -       -        -       M        -
course-service    F       -       -       M        -       -        -
cart-service      -       F       -       F        -       -        -
order-service     F       M       -       -        F       M        -
payment-service   -       -       -       M        -       -        -
learning-service  -       F       -       -        -       -        -
admin-service     F       F       -       -        -       -        -

F = OpenFeign (同步调用)
M = RabbitMQ (异步消息)
```

### 3.4 公共模块

```
common/
├── common-core/          ← 通用工具、异常、返回码
├── common-security/      ← JWT 鉴权拦截器、@CurrentUser 注解
├── common-web/           ← 统一响应体、分页封装、全局异常处理
├── common-mq/            ← RabbitMQ 公共配置、消息体基类
└── common-cache/         ← 二级缓存公共组件 (Caffeine + Redis)
```

---

## 4. 数据库设计

### 4.1 数据库总览

```
user-service (1张)     course-service (3张)      cart-service (1张)
├─ user                ├─ course_category        └─ cart
                       ├─ course
                       └─ chapter_video

order-service (2张)    payment-service (1张)      learning-service (3张)
├─ order               └─ payment_record         ├─ enrollment
└─ order_item                                    ├─ video_progress
                                                 └─ course_review
```

### 4.2 user-service

```sql
CREATE TABLE `user` (
  `id`            BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '用户ID',
  `username`      VARCHAR(32)     NOT NULL UNIQUE             COMMENT '用户名',
  `password`      VARCHAR(128)    NOT NULL                    COMMENT 'BCrypt加密',
  `nickname`      VARCHAR(32)     DEFAULT ''                  COMMENT '昵称',
  `email`         VARCHAR(64)     DEFAULT ''                  COMMENT '邮箱',
  `phone`         VARCHAR(16)     DEFAULT ''                  COMMENT '手机号',
  `avatar_url`    VARCHAR(255)    DEFAULT ''                  COMMENT '头像OSS地址',
  `role`          TINYINT         NOT NULL DEFAULT 0          COMMENT '0=学员 1=管理员',
  `status`        TINYINT         NOT NULL DEFAULT 1          COMMENT '0=禁用 1=正常',
  `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_phone` (`phone`),
  INDEX `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 4.3 course-service

```sql
-- 课程分类
CREATE TABLE `course_category` (
  `id`            BIGINT          PRIMARY KEY AUTO_INCREMENT,
  `name`          VARCHAR(32)     NOT NULL                    COMMENT '分类名称',
  `parent_id`     BIGINT          NOT NULL DEFAULT 0          COMMENT '0=一级分类',
  `sort_order`    INT             NOT NULL DEFAULT 0          COMMENT '排序',
  PRIMARY KEY (`id`),
  INDEX `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程分类表';

-- 课程主表
CREATE TABLE `course` (
  `id`            BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '课程ID',
  `title`         VARCHAR(128)    NOT NULL                    COMMENT '课程标题',
  `description`   TEXT                                        COMMENT '课程简介',
  `cover_url`     VARCHAR(255)    DEFAULT ''                  COMMENT '封面OSS地址',
  `category_id`   BIGINT          NOT NULL                    COMMENT '分类ID',
  `teacher_name`  VARCHAR(32)     DEFAULT ''                  COMMENT '讲师名',
  `price`         DECIMAL(10,2)   NOT NULL DEFAULT 0.00       COMMENT '价格(元)',
  `sale_count`    INT             NOT NULL DEFAULT 0          COMMENT '销量',
  `status`        TINYINT         NOT NULL DEFAULT 0          COMMENT '0=下架 1=上架',
  `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_category` (`category_id`),
  INDEX `idx_sale_count` (`sale_count`),
  FULLTEXT INDEX `ft_title_desc` (`title`, `description`)     -- 全文索引: 搜索
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 章节+视频（合并为一个表，减少JOIN）
CREATE TABLE `chapter_video` (
  `id`            BIGINT          PRIMARY KEY AUTO_INCREMENT  COMMENT '视频ID',
  `course_id`     BIGINT          NOT NULL                    COMMENT '所属课程ID',
  `chapter_title` VARCHAR(128)    NOT NULL                    COMMENT '章节名(如"第一章")',
  `video_title`   VARCHAR(128)    NOT NULL                    COMMENT '视频名(如"1.1 绪论")',
  `video_url`     VARCHAR(255)    NOT NULL                    COMMENT '视频OSS地址',
  `duration`      INT             NOT NULL DEFAULT 0          COMMENT '视频时长(秒)',
  `sort_order`    INT             NOT NULL DEFAULT 0          COMMENT '排序号',
  `status`        TINYINT         NOT NULL DEFAULT 1          COMMENT '0=隐藏 1=可见',
  PRIMARY KEY (`id`),
  INDEX `idx_course` (`course_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='章节视频表';
```

### 4.4 cart-service

```sql
CREATE TABLE `cart` (
  `id`            BIGINT          PRIMARY KEY AUTO_INCREMENT,
  `user_id`       BIGINT          NOT NULL,
  `course_id`     BIGINT          NOT NULL,
  `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user_course` (`user_id`, `course_id`)        -- 同一课程不可重复加入
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';
```

### 4.5 order-service

```sql
CREATE TABLE `order` (
  `id`            BIGINT          PRIMARY KEY AUTO_INCREMENT,
  `order_no`      VARCHAR(32)     NOT NULL UNIQUE             COMMENT '订单号(雪花ID)',
  `user_id`       BIGINT          NOT NULL                    COMMENT '用户ID',
  `total_amount`  DECIMAL(10,2)   NOT NULL                    COMMENT '订单总金额',
  `status`        TINYINT         NOT NULL DEFAULT 0          COMMENT '0=待支付 1=已支付 2=已取消 3=已退款',
  `paid_at`       DATETIME        DEFAULT NULL                COMMENT '支付时间',
  `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_user` (`user_id`),
  INDEX `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

CREATE TABLE `order_item` (
  `id`            BIGINT          PRIMARY KEY AUTO_INCREMENT,
  `order_id`      BIGINT          NOT NULL,
  `course_id`     BIGINT          NOT NULL,
  `course_title`  VARCHAR(128)    NOT NULL                    COMMENT '课程快照-标题',
  `price`         DECIMAL(10,2)   NOT NULL                    COMMENT '课程快照-价格',
  INDEX `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';
```

### 4.6 payment-service

```sql
CREATE TABLE `payment_record` (
  `id`            BIGINT          PRIMARY KEY AUTO_INCREMENT,
  `payment_no`    VARCHAR(32)     NOT NULL UNIQUE             COMMENT '支付流水号',
  `order_id`      BIGINT          NOT NULL,
  `order_no`      VARCHAR(32)     NOT NULL                    COMMENT '关联订单号',
  `user_id`       BIGINT          NOT NULL,
  `amount`        DECIMAL(10,2)   NOT NULL                    COMMENT '支付金额',
  `pay_method`    TINYINT         NOT NULL DEFAULT 0          COMMENT '0=支付宝模拟 1=微信模拟',
  `status`        TINYINT         NOT NULL DEFAULT 0          COMMENT '0=待支付 1=支付成功 2=支付失败',
  `paid_at`       DATETIME        DEFAULT NULL,
  `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_order` (`order_id`),
  INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';
```

### 4.7 learning-service

```sql
-- 选课记录
CREATE TABLE `enrollment` (
  `id`              BIGINT    PRIMARY KEY AUTO_INCREMENT,
  `user_id`         BIGINT    NOT NULL,
  `course_id`       BIGINT    NOT NULL,
  `status`          TINYINT   NOT NULL DEFAULT 0    COMMENT '0=学习中 1=已完成',
  `enrolled_at`     DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_learned_at` DATETIME  DEFAULT NULL,
  UNIQUE KEY `uk_user_course` (`user_id`, `course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选课记录表';

-- 视频学习进度
CREATE TABLE `video_progress` (
  `id`                BIGINT  PRIMARY KEY AUTO_INCREMENT,
  `user_id`           BIGINT  NOT NULL,
  `video_id`          BIGINT  NOT NULL                  COMMENT 'chapter_video表ID',
  `course_id`         BIGINT  NOT NULL,
  `progress_seconds`  INT     NOT NULL DEFAULT 0        COMMENT '已播放秒数',
  `is_finished`       TINYINT NOT NULL DEFAULT 0        COMMENT '0=未完成 1=已完成',
  `updated_at`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user_video` (`user_id`, `video_id`),
  INDEX `idx_user_course` (`user_id`, `course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频进度表';

-- 课程评价
CREATE TABLE `course_review` (
  `id`          BIGINT      PRIMARY KEY AUTO_INCREMENT,
  `user_id`     BIGINT      NOT NULL,
  `course_id`   BIGINT      NOT NULL,
  `rating`      TINYINT     NOT NULL                    COMMENT '评分 1-5',
  `content`     VARCHAR(500) DEFAULT ''                 COMMENT '评价内容',
  `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user_course` (`user_id`, `course_id`), -- 每个用户对每个课程仅一条评价
  INDEX `idx_course` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程评价表';
```

### 4.8 关键设计说明

| 设计点 | 说明 |
|--------|------|
| 雪花 ID | 订单号/支付流水号使用 MyBatis-Plus 自带雪花算法，分布式唯一 |
| 课程快照 | `order_item.price/course_title` 存下单时快照，课程涨价不影响历史订单 |
| FULLTEXT 索引 | `course` 表 `title+description` 建全文索引，中文需配合 ngram parser |
| chapter_video 合并 | 章节和视频合并为一张表，`chapter_title` 重复存储但避免 JOIN |
| 冗余 course_id | `video_progress.course_id` 冗余，避免回表查课程总进度 |
| 软状态 | order/payment/course 均使用枚举状态而非硬删除 |

---

## 5. 核心业务流程图

### 5.1 用户注册/登录

```
用户输入的密码经 BCrypt 加密存储。登录成功后返回 JWT Token（Payload: userId+role, TTL=7天）。
Gateway 解析 Token 后通过 Header X-User-Id / X-User-Role 透传给下游服务。
```

### 5.2 课程详情页 — 二级缓存流程（Redis + Caffeine）

```
请求 → Controller → Service → CacheService.getCourseDetail(courseId)

Step 1: 查 Caffeine 本地缓存
  命中？ → 直接返回 CourseDetailVO（耗时 < 0.1ms）
  未命中 ↓

Step 2: 查 Redis
  Redis GET "course:detail:{id}"
  命中？ → 写入 Caffeine → 返回（耗时 1-3ms）
  未命中 ↓

Step 3: 加分布式锁，防缓存击穿
  lockKey = "lock:course:{id}"
  Redis SET lockKey NX EX 10
  获取失败？ → sleep(100ms) → 回到 Step 1（自旋重试，最多 3 次）
  获取成功 ↓

Step 4: 查 MySQL
  SELECT c.*, cv.* FROM course c LEFT JOIN chapter_video cv ...
  DB 返回 null？ → 缓存空值 "NULL_MARKER"（TTL=2min）→ 返回 null（防穿透）
  DB 返回数据 ↓

Step 5: 异步写缓存
  Redis SET "course:detail:{id}" JSON TTL=30min+随机(1~5min)
  Caffeine PUT courseId → CourseDetailVO
  释放分布式锁 DEL "lock:course:{id}"

三层防护:
  - 缓存穿透: NULL_MARKER 缓存空值，TTL=2min
  - 缓存击穿: Redis SET NX 分布式锁，仅一人查库
  - 缓存雪崩: TTL 加随机偏移（30min + random 1~5min）
```

**Caffeine 配置:**

| 参数 | 值 | 说明 |
|------|-----|------|
| initialCapacity | 100 | 初始容量 |
| maximumSize | 1000 | 最多缓存 1000 个课程 |
| expireAfterWrite | 10min | 写后 10 分钟过期 |
| recordStats | true | 开启统计，便于调优 |

**缓存更新策略:** Cache Aside 模式 — 先写 DB，再删 Redis + Caffeine。

### 5.3 下单 → MQ → 支付 → 通知（核心交易链）

```
用户点击"立即购买"

Step 1: order-service 创建订单（本地事务）
  @Transactional
  INSERT order (status=0 待支付)
  INSERT order_item (课程快照)
  → 事务提交后发送 "order.created" 消息

Step 2: 发送到 Exchange: order.topic (routingKey: order.created)
  三条路由:
    ├→ Queue: order.created.course    → course-service (扣减销量)
    ├→ Queue: order.created.notify    → 通知服务 (站内信/日志)
    └→ Queue: order.payment.delay     → 延迟队列 (TTL=30min → DLX → 超时取消)

Step 3: 用户点击"去支付" → payment-service
  INSERT payment_record (幂等保护: Redis SETNX "pay:lock:{orderId}")
  模拟支付直接成功:
    UPDATE payment_record SET status=1
    Feign 调用 UPDATE order SET status=1
  发送 "order.paid" 消息

Step 4: 消费 order.paid
  → learning-service: 幂等消费(SETNX) → INSERT enrollment
```

**RabbitMQ 完整拓扑:**

```
Exchange: order.topic (topic 类型)

├─ Queue: order.created.course      (RK: order.created)
│  → course-service 手动ACK 扣减销量
│
├─ Queue: order.created.notify      (RK: order.created)
│  → 通知处理 手动ACK
│
├─ Queue: order.payment.delay       (RK: order.created)
│  TTL=30min → DLX → Exchange: order.dlx
│  → Queue: order.timeout.cancel   (RK: order.timeout)
│  → order-service 取消订单
│
└─ Queue: order.paid.enrollment     (RK: order.paid)
   → learning-service 幂等消费 创建选课
```

**六大机制实现:**

| 能力 | 实现方式 |
|------|---------|
| Publisher Confirm | `publisher-confirm-type: correlated` + `RabbitTemplate.setConfirmCallback()` |
| Return Callback | `publisher-returns: true` + `RabbitTemplate.setReturnsCallback()` — 路由失败写入 `msg_fail_log` |
| 手动 ACK | `acknowledge-mode: manual` — 成功 `basicAck()`，失败 `basicNack(requeue)` |
| 死信队列 (DLX) | 延迟队列 TTL 过期 → `order.dlx` → `order.timeout.cancel` |
| 延迟队列 | TTL + DLX 组合（RabbitMQ 原生不支持延迟） |
| 幂等消费 | Redis SETNX `enroll:lock:{orderId}`，已存在直接 ACK 丢弃 |

**异常处理体系:**

- 消费成功 → basicAck()
- 业务异常 → basicNack(requeue=true)，重试 3 次后 basicNack(requeue=false) → 写入 `msg_dead_log`
- 系统异常 → 同上
- 定时任务扫描死信表，人工处理或自动补偿

### 5.4 视频学习进度

```
播放视频 → 每 10 秒上报进度

防刷策略: 进度只能前进不能后退
  Redis GET "progress:{userId}:{videoId}" → 上次进度
  if (newSeconds <= cachedSeconds) → 忽略
  if (newSeconds >= videoDuration * 0.95) → isFinished=1
  Redis SET → 异步批量写 MySQL
  UPDATE video_progress + UPDATE enrollment.last_learned_at
```

---

## 6. Redis 缓存设计

### 6.1 Key 命名规范

```
{业务域}:{子域}:{标识}
```

### 6.2 缓存全景

| 服务 | Key | 类型 | 内容 | TTL | 说明 |
|------|-----|------|------|-----|------|
| course-service | `course:detail:{id}` | String(JSON) | CourseDetailVO | 30min+随机 | 二级缓存(Redis层) |
| course-service | `course:null:{id}` | String | `"NULL_MARKER"` | 2min | 防穿透空值 |
| course-service | `lock:course:{id}` | String NX | `"1"` | 10s | 防击穿分布式锁 |
| course-service | Caffeine `course:{id}` | 本地 | CourseDetailVO | 10min | 二级缓存(本地层) |
| course-service | `course:category:tree` | String(JSON) | 分类嵌套树 | 1h | Admin 修改后删除 |
| course-service | `course:hot:top10` | ZSet | member=courseId | 10min | 定时刷新 |
| course-service | `course:sale:count:{id}` | String(INT) | 实时销量 | 永不过期 | MQ 消费后 INCR |
| cart-service | `cart:user:{userId}` | Set | courseId 集合 | 7天 | 登录时加载 |
| learning-service | `progress:{userId}:{videoId}` | String(INT) | 已播秒数 | 24h | 防倒退校验 |
| learning-service | `enroll:lock:{orderId}` | String | `"1"` | 永久 | 幂等标记 |
| payment-service | `pay:lock:{orderId}` | String NX | `"1"` | 60s | 支付幂等 |
| user-service | `user:token:blacklist` | Set | 被禁 userId | 7天 | Gateway O(1) 判断 |

### 6.3 缓存一致性策略

Cache Aside 模式: 更新 MySQL → 删除 Redis → 删除 Caffeine（本服内）。跨服务缓存通过 RabbitMQ `course.updated` 消息通知清除。

### 6.4 Redis 配置

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: 6379
      password: ${REDIS_PASSWORD:}
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
          max-wait: 2000ms
      timeout: 3000ms
```

| 配置项 | 值 | 理由 |
|--------|-----|------|
| 连接池 | Lettuce | Spring Boot 3 默认，Netty 异步 |
| max-active | 20 | 中等并发足够 |
| 序列化 | Jackson2JsonRedisSerializer | JSON 可读可调试 |
| Key 序列化 | StringRedisSerializer | 人类可读 Key |

---

## 7. RabbitMQ 消息设计

### 7.1 完整拓扑

```
Exchanges:
  order.topic        (topic 类型)
  course.topic       (topic 类型)
  learning.topic     (topic 类型)
  order.dlx          (direct 类型 — 死信)
  course.dlx         (direct 类型 — 死信)

消息路由全图:

order.topic
├─ order.created → Queue: order.created.course        (→ course-service)
├─ order.created → Queue: order.created.notify        (→ 通知)
├─ order.created → Queue: order.payment.delay         (延迟 30min → DLX)
├─ order.paid    → Queue: order.paid.enrollment       (→ learning-service)
└─ order.paid    → Queue: order.paid.sale             (→ course-service)

course.topic
├─ course.updated → Queue: course.cache.evict         (→ course-service)
└─ course.updated → Queue: course.cache.learning      (→ learning-service)

learning.topic
├─ video.progress  → Queue: video.progress.batch      (→ learning-service)
└─ course.reviewed → Queue: course.review.update      (→ course-service)
```

### 7.2 消息体规范

```java
// 公共消息头 (所有消息继承)
public abstract class BaseMessage {
    private String messageId;      // UUID, 全局唯一
    private Long timestamp;        // 发送时间戳
    private String eventType;      // "order.created" / "order.paid" ...
    private Integer retryCount;    // 当前重试次数, 默认0
}

// 订单创建消息
public class OrderCreatedMessage extends BaseMessage {
    private Long orderId;
    private String orderNo;
    private Long userId;
    private Long courseId;
    private BigDecimal amount;
}

// 订单支付成功消息
public class OrderPaidMessage extends BaseMessage {
    private Long orderId;
    private String orderNo;
    private Long userId;
    private Long courseId;
}

// 课程更新消息
public class CourseUpdatedMessage extends BaseMessage {
    private Long courseId;
    private Integer operation;    // 1=更新 2=下架 3=删除
}

// 视频进度消息
public class VideoProgressMessage extends BaseMessage {
    private Long userId;
    private Long videoId;
    private Long courseId;
    private Integer progressSeconds;
    private Boolean isFinished;
}
```

### 7.3 可靠性保障

| 场景 | 处理方式 |
|------|---------|
| Confirm 成功 | 更新 `msg_send_log.status=1` |
| Confirm 失败 | 更新 `msg_send_log.status=2`，定时任务重试 |
| Return 触发（路由失败） | 写入 `msg_fail_log`，人工介入 |
| 消费端三次重试仍失败 | 写入 `msg_dead_log`，告警 |

### 7.4 消息落库表

```sql
CREATE TABLE `msg_send_log` (
  `id`           BIGINT PRIMARY KEY AUTO_INCREMENT,
  `message_id`   VARCHAR(64) NOT NULL,
  `exchange`     VARCHAR(64) NOT NULL,
  `routing_key`  VARCHAR(64) NOT NULL,
  `body`         TEXT        NOT NULL,
  `status`       TINYINT     NOT NULL DEFAULT 0  COMMENT '0=已发送 1=已确认 2=发送失败',
  `retry_count`  INT         NOT NULL DEFAULT 0,
  `created_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_msg_id` (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 8. Docker 部署方案

### 8.1 容器清单

| 容器名称 | 镜像 | 端口映射 |
|----------|------|---------|
| nacos | nacos/nacos-server:v2.3.0 | 8848:8848 / 9848:9848 |
| mysql | mysql:8.0 | 3306:3306 |
| redis | redis:7-alpine | 6379:6379 |
| rabbitmq | rabbitmq:3.12-management-alpine | 5672:5672 / 15672:15672 |
| sentinel-dashboard | bladex/sentinel-dashboard:1.8.6 | 8858:8858 |
| gateway-service | 自构建 openjdk:17-alpine | 8080:8080 |
| user-service | 自构建 openjdk:17-alpine | 8081:8081 |
| course-service | 自构建 openjdk:17-alpine | 8082:8082 |
| cart-service | 自构建 openjdk:17-alpine | 8083:8083 |
| order-service | 自构建 openjdk:17-alpine | 8084:8084 |
| payment-service | 自构建 openjdk:17-alpine | 8085:8085 |
| learning-service | 自构建 openjdk:17-alpine | 8086:8086 |
| admin-service | 自构建 openjdk:17-alpine | 8087:8087 |

### 8.2 目录结构

```
online-course-platform/
├── docker-compose.yml
├── Dockerfile                     # 多阶段构建模板（maven:3.9 → jre:17-alpine）
├── .env                           # 环境变量（敏感信息）
├── deploy/
│   ├── mysql/init/
│   │   ├── 01-schema.sql
│   │   └── 02-user.sql
│   ├── nacos/conf/
│   │   └── application.properties
│   ├── redis/redis.conf
│   └── rabbitmq/enabled_plugins
├── common/
├── gateway-service/
├── user-service/
├── course-service/
├── cart-service/
├── order-service/
├── payment-service/
├── learning-service/
└── admin-service/
```

### 8.3 Dockerfile（通用多阶段构建）

```dockerfile
# 编译阶段
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /build
COPY common/pom.xml    common/
COPY common/src        common/src/
COPY pom.xml .
RUN mvn install -pl common -DskipTests

ARG SERVICE_NAME
COPY ${SERVICE_NAME}/pom.xml ${SERVICE_NAME}/
COPY ${SERVICE_NAME}/src     ${SERVICE_NAME}/src/
RUN mvn package -pl ${SERVICE_NAME} -DskipTests

# 运行阶段（最小镜像）
FROM eclipse-temurin:17-jre-alpine
ARG SERVICE_NAME
WORKDIR /app
COPY --from=builder /build/${SERVICE_NAME}/target/*.jar app.jar
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
EXPOSE 8080
ENTRYPOINT exec java ${JAVA_OPTS} -jar app.jar
```

### 8.4 启动顺序与资源限制

```
Phase 1: mysql / redis / rabbitmq (并行)
Phase 2: nacos (depends_on mysql)
Phase 3: sentinel-dashboard
Phase 4: 7 个业务服务 + gateway (并行注册到 Nacos)
```

| 服务 | CPU Limit | Memory Limit | 理由 |
|------|-----------|-------------|------|
| MySQL | 2.0 | 1G | 核心数据存储 |
| Redis | 1.0 | 512M | 内存数据库 |
| RabbitMQ | 1.0 | 512M | 消息量不大 |
| 业务服务 ×7 | 1.0/个 | 512M/个 | 统一规格 |

### 8.5 常用运维命令

```bash
docker-compose up -d                          # 启动全部
docker-compose up -d --build course-service   # 重建单个服务
docker-compose logs -f --tail=100 gateway     # 查看日志
docker-compose up -d --scale course-service=3 # 水平扩容
docker-compose down                           # 停止全部
```

---

## 9. 接口规范

### 9.1 统一响应体

```java
public class R<T> {
    private Integer code;       // 业务状态码
    private String message;     // 提示信息
    private T data;             // 响应数据
    private Long timestamp;     // 时间戳
}
```

**成功:** `{"code": 200, "message": "success", "data": {...}, "timestamp": 1718400000000}`  
**失败:** `{"code": 40001, "message": "用户名已存在", "data": null, "timestamp": ...}`

### 9.2 状态码规范

```
200    成功
201    创建成功
40001  参数校验失败
40002  用户名已存在
40003  手机号已注册
40004  登录失败
40005  Token无效或过期
40006  账号已被禁用
40007  课程不存在
40008  课程已购买
40009  库存不足
40010  订单不存在
40011  订单已支付
40012  重复支付
40013  评价已存在
50001  系统内部错误
50002  数据库异常
50003  消息队列异常
50004  缓存异常
50005  远程调用失败
```

### 9.3 鉴权规范

```
请求头: Authorization: Bearer {jwt_token}
公开接口: /api/user/register, /api/user/login
          /api/course/list, /api/course/detail/*, /api/course/category/tree
需登录: 其余所有接口
Token 透传: Gateway 解析 JWT → Header: X-User-Id, X-User-Role
```

### 9.4 接口总览

#### user-service

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/user/register` | 公开 | 注册 |
| POST | `/api/user/login` | 公开 | 登录，返回 JWT |
| GET | `/api/user/info` | 需登录 | 个人信息 |
| PUT | `/api/user/info` | 需登录 | 修改信息 |

#### course-service

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/course/category/tree` | 公开 | 分类树（二级缓存） |
| GET | `/api/course/list` | 公开 | 分页+筛选+搜索 |
| GET | `/api/course/detail/{id}` | 公开 | 详情（含章节视频） |
| GET | `/api/course/hot` | 公开 | 热门 TOP10（ZSet） |

#### cart-service

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/cart/list` | 需登录 | 购物车列表 |
| POST | `/api/cart/add` | 需登录 | 加入购物车 |
| DELETE | `/api/cart/remove/{courseId}` | 需登录 | 移除 |
| DELETE | `/api/cart/clear` | 需登录 | 清空 |

#### order-service

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/order/create` | 需登录 | 下单 → MQ |
| GET | `/api/order/detail/{id}` | 需登录 | 订单详情 |
| GET | `/api/order/list` | 需登录 | 订单列表 |
| PUT | `/api/order/cancel/{id}` | 需登录 | 取消(仅待支付) |

#### payment-service

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/payment/pay/{orderId}` | 需登录 | 发起支付（幂等） |
| GET | `/api/payment/result/{orderId}` | 需登录 | 支付结果 |

#### learning-service

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/learning/my-courses` | 需登录 | 我的课程（含进度） |
| GET | `/api/learning/progress/{courseId}` | 需登录 | 课程学习进度 |
| PUT | `/api/learning/progress/report` | 需登录 | 上报进度（10s防刷） |
| GET | `/api/learning/review/{courseId}` | 需登录 | 我的评价 |
| POST | `/api/learning/review` | 需登录 | 提交评价 |
| GET | `/api/learning/course/{id}/reviews` | 公开 | 课程评价列表 |

#### admin-service

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| CRUD | `/api/admin/course/**` | 管理员 | 课程管理 |
| CRUD | `/api/admin/category/**` | 管理员 | 分类管理 |
| GET/PUT | `/api/admin/user/**` | 管理员 | 用户管理 |

### 9.5 接口矩阵

```
                        user  course  cart  order  payment  learning  admin
/api/user/register       C
/api/user/login          C
/api/user/info           R,U
/api/course/category/tree      R
/api/course/list                R
/api/course/detail/{id}         R
/api/course/hot                 R
/api/cart/*                             C,R,D
/api/order/*                                  C,R,U
/api/payment/*                                       C,R
/api/learning/my-courses                                     R
/api/learning/progress/*                                     R,U
/api/learning/review                                         C,R
/api/learning/course/{id}/reviews                            R
/api/admin/**                                                             CRUD
```

---

## 10. 附录：校招面试要点

本设计方案覆盖以下面试高频考点：

| 考点 | 对应章节 | 关键内容 |
|------|---------|---------|
| 微服务拆分原则 | §3 | DDD 限界上下文、服务通信矩阵 |
| 缓存三大问题 | §5.2、§6 | 穿透(Null Cache) / 击穿(分布式锁) / 雪崩(随机TTL) |
| 二级缓存架构 | §5.2、§6 | Caffeine(本地) + Redis(分布式) |
| MQ 六大机制 | §5.3、§7 | Confirm/Return/手动ACK/死信/延迟/幂等 |
| 分布式事务 | §5.3 | MQ 异步 + 补偿（最终一致性） |
| Cache Aside 模式 | §5.2、§6.3 | 先写DB后删缓存 |
| 分页与搜索 | §4.3、§9 | FULLTEXT 索引 + 多条件筛选 |
| 订单防重 | §5.3、§7 | Redis SETNX 幂等 + DB 唯一约束 |
| 雪花 ID | §4.8 | MyBatis-Plus 分布式 ID |
| 容器化部署 | §8 | Docker 多阶段构建 + Compose 编排 |
