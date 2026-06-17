# 已知局限改进 — 设计文档

日期：2026-06-17 | 状态：已批准

## 范围

改进 CLAUDE.md / README.md 中记录的 4 项已知局限（#2 已过时，仅更新文档）：

| # | 项目 | 类型 |
|---|------|------|
| 1 | 核心链路测试 | 新增 ~12 个测试类 |
| 3 | @CurrentUser → UserContext | 重构 |
| 4 | OrderPaidMessage → common-mq | 重构 |
| 5 | 缓存精确驱逐 | 增强 |

---

## #3 — @CurrentUser → UserContext

### 现状

- `@CurrentUser` 注解在 common-security 中已定义，无参数解析器
- 51 处控制器方法使用 `@RequestAttribute("userId")` / `@RequestAttribute("role")` 硬编码字符串
- 无 `UserContext` 或等价承载对象

### 设计

**新建文件：**

1. `common/common-security/src/main/java/com/learning/common/security/context/UserContext.java`
   - `Long userId` (final)
   - `Integer role` (final)
   - `boolean isAdmin()` → `role != null && role == 1`

2. `common/common-security/src/main/java/com/learning/common/security/resolver/CurrentUserArgumentResolver.java`
   - 实现 `HandlerMethodArgumentResolver`
   - `supportsParameter`: 参数上有 `@CurrentUser` 且类型为 `UserContext`
   - `resolveArgument`: 从 `NativeWebRequest` 读取 `"userId"` / `"role"` attribute，构造 `UserContext`

**修改文件：**

3. `common/common-web/src/main/java/com/learning/common/web/config/WebMvcConfig.java`
   - `addArgumentResolvers()` 注册 `CurrentUserArgumentResolver`

4. 全部控制器（~51 处方法签名）：
   - `@RequestAttribute("userId") Long userId, @RequestAttribute("role") Integer role` → `@CurrentUser UserContext userContext`
   - 方法体内 `userId` → `userContext.getUserId()`
   - 管理端 `role == 1` 检查 → `userContext.isAdmin()`
   - `AdminAuthService.checkAdmin(role)` → `checkAdmin(userContext)` 或保留按需传入 role

### 兼容性

- JWT → Gateway header → Interceptor → request attribute 链路不变
- 仅改变控制器层获取用户信息的方式
- Feign 内部调用（不经过网关）的 `required=false` 模式需保留：解析器对空值返回 `UserContext(null, null)`，`isAdmin()` 返回 false

---

## #4 — OrderPaidMessage → common-mq

### 现状

三个服务各自定义，字段差异：

| 服务 | 字段数 | 额外字段 |
|------|--------|----------|
| payment-service | 5 | amount (BigDecimal) |
| learning-service | 4 | — |
| order-service | 4 | — |

### 设计

1. 在 `common-mq` 新建 `OrderPaidMessage`（5 字段，含 amount），继承 `BaseMessage`
2. 删除 3 个服务中的本地 `OrderPaidMessage.java`
3. 更新 import：
   - `payment-service`: `PaymentServiceImpl.java`, `PaymentEventProducer.java`
   - `learning-service`: `EnrollmentConsumer.java`
   - `order-service`: `OrderEventProducer.java`

### 兼容性

- JSON 反序列化默认忽略未知字段 — order/learning 消费 payment 发来的消息时，`amount` 存在但反序列化正常工作
- 类路径变更后需全量重新编译（`mvn clean compile`）

---

## #5 — 缓存精确驱逐

### 现状

`CacheRefreshConsumer` 已监听 `course.updated.cache` 队列，但：
- 将消息解析为通用 `Map` 而非 `CourseUpdatedMessage`
- 无差别调用 `refreshAllCaches()` 清除全部缓存
- 一个视频标题修改也会清除所有课程详情 + 分类树 + 热门

### 设计

**改造 CacheRefreshConsumer：**

```
收到消息 → 尝试反序列化为 CourseUpdatedMessage
         ├─ 成功 → 按 courseId 精确驱逐
         │        ├─ operation=1(更新)/2(下线) → evict(courseId) + evictHotTop10()
         │        └─ operation=3(删除)          → evict(courseId) + evictHotTop10() + evictCategoryTree()
         ├─ 尝试反序列化为 CategoryUpdatedMessage
         │        └─ evictCategoryTree()
         └─ 未知格式 → 全量 refreshAllCaches()（安全兜底）
```

**新增 CourseCacheService 方法：**
- `evictHotTop10()` — 已存在，删除 `course:hot:top10` ZSet
- `evictCategoryTree()` — 已存在，删除分类树缓存
- 无需新增方法，仅需在 consumer 中按操作类型组合调用

**需将 admin-service 的 `CourseUpdatedMessage` 和 `CategoryUpdatedMessage` 也抽取到 common-mq**（消息类定义在生产者和消费者之间共享才能正确反序列化）。两个消息类从 admin-service/mq/message 移动到 common-mq/message。

### 兼容性

- 精确驱逐后缓存 miss 率显著降低，行为更优
- 未知消息格式回退到全量清除，保证安全

---

## #1 — 核心链路测试

### 技术选型

- **JUnit 5** (Spring Boot 默认)
- **Mockito** (`mockito-core`, Spring Boot 默认)
- **MockitoExtension** — 纯单元测试，不启动 Spring 容器
- 按模块分别在 `src/test/java` 下创建

### 测试类清单

| 模块 | 测试类 | 覆盖核心逻辑 |
|------|--------|-------------|
| common-security | `JwtUtilTest` | token 签发、解析、过期校验 |
| user-service | `UserServiceImplTest` | 注册(手机号重复)、登录(密码校验)、信息更新 |
| course-service | `CourseCacheServiceTest` | 多级缓存读取、空值穿透防护、分布式锁击穿防护、精确驱逐 |
| cart-service | `CartServiceImplTest` | 添加(重复加入累加)、删除、清空 |
| order-service | `OrderServiceImplTest` | 创建订单(幂等)、状态流转 |
| order-service | `OrderEventProducerTest` | 消息发送、Confirm 回调 |
| payment-service | `PaymentServiceImplTest` | 支付幂等、状态更新 |
| learning-service | `EnrollmentConsumerTest` | 消费 order.paid → 自动报名(幂等 SETNX) |
| learning-service | `LearningServiceImplTest` | 进度上报防刷(只进不退) |
| admin-service | `DashboardServiceImplTest` | 多服务聚合、空数据异常处理 |
| admin-service | `AdminEventProducerTest` | course.updated MQ 发送 |
| course-service | `CacheRefreshConsumerTest` | 精确驱逐、兜底全量清除 |

共 12 个测试类，覆盖核心业务链路。

### 测试模式

```java
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock private UserMapper userMapper;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @InjectMocks private UserServiceImpl userService;

    @Test
    void shouldThrowBizExceptionWhenPhoneExists() { ... }

    @Test
    void shouldReturnTokenOnSuccessfulLogin() { ... }
}
```

---

## 实施顺序

```
#3 @CurrentUser  ← 先做（影响面最广，其他任务可能引用 UserContext）
    ↓
#4 OrderPaidMessage + #5 消息类抽取  ← 同一模块联动
    ↓
#5 缓存精确驱逐
    ↓
#1 核心链路测试  ← 最后（可引用已重构的代码）
    ↓
更新 README.md / CLAUDE.md
```
