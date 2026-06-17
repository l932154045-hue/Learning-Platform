# 已知局限改进 — 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 修复项目 4 项已知局限：@CurrentUser 参数解析器、OrderPaidMessage 抽取到 common-mq、缓存精确驱逐、核心链路测试

**架构：** 创建 UserContext + CurrentUserArgumentResolver 替代硬编码 @RequestAttribute；将 OrderPaidMessage/CourseUpdatedMessage/CategoryUpdatedMessage 抽取到 common-mq 消除重复；改造 CacheRefreshConsumer 按 courseId 精确驱逐；新增 12 个 JUnit 5 + Mockito 测试类

**技术栈：** Java 17, Spring Boot 3.2, MyBatis Plus 3.5, JUnit 5, Mockito, RabbitMQ, Redis

---

### 任务 1：创建 UserContext 类

**文件：**
- 创建：`common/common-security/src/main/java/com/learning/common/security/context/UserContext.java`

- [ ] **步骤 1：编写 UserContext**

```java
package com.learning.common.security.context;

import lombok.Getter;

/**
 * 用户上下文 — 从 JWT 解析的用户信息，通过 @CurrentUser 注解注入 Controller
 */
@Getter
public class UserContext {

    private final Long userId;
    private final Integer role;

    public UserContext(Long userId, Integer role) {
        this.userId = userId;
        this.role = role;
    }

    /** 是否为管理员（role == 1） */
    public boolean isAdmin() {
        return role != null && role == 1;
    }
}
```

- [ ] **步骤 2：编译验证**

```bash
mvn compile -pl common/common-security -am -q
```

- [ ] **步骤 3：Commit**

```bash
git add common/common-security/src/main/java/com/learning/common/security/context/UserContext.java
git commit -m "feat: 新增 UserContext 用户上下文类"
```

---

### 任务 2：创建 CurrentUserArgumentResolver

**文件：**
- 创建：`common/common-security/src/main/java/com/learning/common/security/resolver/CurrentUserArgumentResolver.java`

- [ ] **步骤 1：编写参数解析器**

```java
package com.learning.common.security.resolver;

import com.learning.common.security.annotation.CurrentUser;
import com.learning.common.security.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @CurrentUser 参数解析器 — 从 request attribute 中提取 userId/role 组装 UserContext
 */
@Slf4j
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && UserContext.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            return new UserContext(null, null);
        }

        Long userId = null;
        Integer role = null;

        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr instanceof Long) {
            userId = (Long) userIdAttr;
        } else if (userIdAttr instanceof Number) {
            userId = ((Number) userIdAttr).longValue();
        }

        Object roleAttr = request.getAttribute("role");
        if (roleAttr instanceof Integer) {
            role = (Integer) roleAttr;
        } else if (roleAttr instanceof Number) {
            role = ((Number) roleAttr).intValue();
        }

        return new UserContext(userId, role);
    }
}
```

- [ ] **步骤 2：编译验证**

```bash
mvn compile -pl common/common-security -am -q
```

- [ ] **步骤 3：Commit**

```bash
git add common/common-security/src/main/java/com/learning/common/security/resolver/CurrentUserArgumentResolver.java
git commit -m "feat: 新增 CurrentUserArgumentResolver 参数解析器"
```

---

### 任务 3：在 WebMvcConfig 注册解析器

**文件：**
- 修改：`common/common-web/src/main/java/com/learning/common/web/config/WebMvcConfig.java`

- [ ] **步骤 1：添加 addArgumentResolvers 方法**

修改前（行 1-26）：
```java
package com.learning.common.web.config;

import com.learning.common.security.interceptor.UserInfoInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserInfoInterceptor())
                .addPathPatterns("/api/**");
    }
}
```

修改后：
```java
package com.learning.common.web.config;

import com.learning.common.security.interceptor.UserInfoInterceptor;
import com.learning.common.security.resolver.CurrentUserArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final CurrentUserArgumentResolver currentUserArgumentResolver;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserInfoInterceptor())
                .addPathPatterns("/api/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }
}
```

- [ ] **步骤 2：编译验证**

```bash
mvn compile -pl common/common-web -am -q
```

- [ ] **步骤 3：Commit**

```bash
git add common/common-web/src/main/java/com/learning/common/web/config/WebMvcConfig.java
git commit -m "feat: WebMvcConfig 注册 CurrentUserArgumentResolver"
```

---

### 任务 4：迁移 user-service 控制器

**文件：**
- 修改：`user-service/src/main/java/com/learning/user/controller/UserController.java`
- 修改：`user-service/src/main/java/com/learning/user/controller/UserInternalController.java`

- [ ] **步骤 1：迁移 UserController**

修改前：
```java
package com.learning.user.controller;

import com.learning.common.core.result.R;
import com.learning.user.dto.req.*;
import com.learning.user.dto.resp.*;
import com.learning.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody RegisterReq req) {
        userService.register(req);
        return R.ok();
    }

    @PostMapping("/login")
    public R<LoginResp> login(@Valid @RequestBody LoginReq req) {
        return R.ok(userService.login(req));
    }

    @GetMapping("/info")
    public R<UserInfoResp> getUserInfo(@RequestAttribute("userId") Long userId) {
        return R.ok(userService.getUserInfo(userId));
    }

    @PutMapping("/info")
    public R<Void> updateInfo(@RequestAttribute("userId") Long userId,
                              @Valid @RequestBody UpdateUserInfoReq req) {
        userService.updateUserInfo(userId, req);
        return R.ok();
    }

    @PutMapping("/password")
    public R<Void> changePassword(@RequestAttribute("userId") Long userId,
                                  @RequestBody ChangePasswordReq req) {
        userService.changePassword(userId, req.getOldPassword(), req.getNewPassword());
        return R.ok();
    }
}
```

修改后：
```java
package com.learning.user.controller;

import com.learning.common.core.result.R;
import com.learning.common.security.annotation.CurrentUser;
import com.learning.common.security.context.UserContext;
import com.learning.user.dto.req.*;
import com.learning.user.dto.resp.*;
import com.learning.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody RegisterReq req) {
        userService.register(req);
        return R.ok();
    }

    @PostMapping("/login")
    public R<LoginResp> login(@Valid @RequestBody LoginReq req) {
        return R.ok(userService.login(req));
    }

    @GetMapping("/info")
    public R<UserInfoResp> getUserInfo(@CurrentUser UserContext userContext) {
        return R.ok(userService.getUserInfo(userContext.getUserId()));
    }

    @PutMapping("/info")
    public R<Void> updateInfo(@CurrentUser UserContext userContext,
                              @Valid @RequestBody UpdateUserInfoReq req) {
        userService.updateUserInfo(userContext.getUserId(), req);
        return R.ok();
    }

    @PutMapping("/password")
    public R<Void> changePassword(@CurrentUser UserContext userContext,
                                  @RequestBody ChangePasswordReq req) {
        userService.changePassword(userContext.getUserId(), req.getOldPassword(), req.getNewPassword());
        return R.ok();
    }
}
```

- [ ] **步骤 2：迁移 UserInternalController**

修改前（关键行 18-39）：
```java
    @GetMapping("/list")
    public R<PageResp<UserListResp>> list(PageReq req,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) Integer roleFilter,
                                          @RequestParam(required = false) Integer status,
                                          @RequestAttribute(value = "role", required = false) Integer role) {
        if (role != null && role != 1) {
            return R.fail(40015, "无权访问");
        }
        return R.ok(userService.listUsers(req, keyword, roleFilter, status));
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id,
                                @RequestParam Integer status,
                                @RequestAttribute(value = "role", required = false) Integer role) {
        if (role != null && role != 1) {
            return R.fail(40015, "无权访问");
        }
        userService.updateUserStatus(id, status);
        return R.ok();
    }
```

修改后：
```java
    @GetMapping("/list")
    public R<PageResp<UserListResp>> list(PageReq req,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) Integer roleFilter,
                                          @RequestParam(required = false) Integer status,
                                          @CurrentUser UserContext userContext) {
        if (!userContext.isAdmin()) {
            return R.fail(40015, "无权访问");
        }
        return R.ok(userService.listUsers(req, keyword, roleFilter, status));
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id,
                                @RequestParam Integer status,
                                @CurrentUser UserContext userContext) {
        if (!userContext.isAdmin()) {
            return R.fail(40015, "无权访问");
        }
        userService.updateUserStatus(id, status);
        return R.ok();
    }
```

更新 import：添加 `import com.learning.common.security.annotation.CurrentUser;` 和 `import com.learning.common.security.context.UserContext;`，移除 `import org.springframework.web.bind.annotation.RequestAttribute;`

- [ ] **步骤 3：编译验证**

```bash
mvn compile -pl user-service -am -q
```

- [ ] **步骤 4：Commit**

```bash
git add user-service/src/main/java/com/learning/user/controller/UserController.java user-service/src/main/java/com/learning/user/controller/UserInternalController.java
git commit -m "refactor: user-service 控制器 @RequestAttribute 迁移为 @CurrentUser UserContext"
```

---

### 任务 5：迁移 cart-service、payment-service 控制器

**文件：**
- 修改：`cart-service/src/main/java/com/learning/cart/controller/CartController.java`
- 修改：`payment-service/src/main/java/com/learning/payment/controller/PaymentController.java`

- [ ] **步骤 1：迁移 CartController**

修改后完整文件：
```java
package com.learning.cart.controller;

import com.learning.cart.dto.req.CartAddReq;
import com.learning.cart.dto.resp.CartItemVO;
import com.learning.cart.service.CartService;
import com.learning.common.core.result.R;
import com.learning.common.security.annotation.CurrentUser;
import com.learning.common.security.context.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/list")
    public R<List<CartItemVO>> list(@CurrentUser UserContext userContext) {
        return R.ok(cartService.list(userContext.getUserId()));
    }

    @PostMapping("/add")
    public R<Void> add(@CurrentUser UserContext userContext,
                       @Valid @RequestBody CartAddReq req) {
        cartService.add(userContext.getUserId(), req.getCourseId());
        return R.ok();
    }

    @DeleteMapping("/remove/{courseId}")
    public R<Void> remove(@CurrentUser UserContext userContext,
                          @PathVariable("courseId") Long courseId) {
        cartService.remove(userContext.getUserId(), courseId);
        return R.ok();
    }

    @DeleteMapping("/clear")
    public R<Void> clear(@CurrentUser UserContext userContext) {
        cartService.clear(userContext.getUserId());
        return R.ok();
    }
}
```

- [ ] **步骤 2：迁移 PaymentController**

修改后完整文件：
```java
package com.learning.payment.controller;

import com.learning.common.core.result.R;
import com.learning.common.security.annotation.CurrentUser;
import com.learning.common.security.context.UserContext;
import com.learning.payment.dto.resp.PayResultVO;
import com.learning.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/pay/{orderId}")
    public R<PayResultVO> pay(@CurrentUser UserContext userContext,
                              @PathVariable("orderId") Long orderId) {
        return R.ok(paymentService.pay(userContext.getUserId(), orderId));
    }

    @GetMapping("/result/{orderId}")
    public R<PayResultVO> result(@PathVariable("orderId") Long orderId,
                                 @CurrentUser UserContext userContext) {
        return R.ok(paymentService.queryResult(orderId, userContext.getUserId()));
    }
}
```

- [ ] **步骤 3：编译验证**

```bash
mvn compile -pl cart-service,payment-service -am -q
```

- [ ] **步骤 4：Commit**

```bash
git add cart-service/src/main/java/com/learning/cart/controller/CartController.java payment-service/src/main/java/com/learning/payment/controller/PaymentController.java
git commit -m "refactor: cart/payment 控制器迁移为 @CurrentUser UserContext"
```

---

### 任务 6：迁移 order-service 控制器

**文件：**
- 修改：`order-service/src/main/java/com/learning/order/controller/OrderController.java`

- [ ] **步骤 1：迁移 OrderController — import 和用户端方法**

更新 import（添加 `CurrentUser`, `UserContext`；移除 `RequestAttribute`）：

用户端 4 个方法修改后签名：
```java
// create
public R<Long> create(@CurrentUser UserContext userContext,
                      @Valid @RequestBody CreateOrderReq req) {
    Long orderId = orderService.createOrder(userContext.getUserId(), req);
    ...
}

// detail
public R<OrderDetailVO> detail(@PathVariable("id") Long id,
                                @CurrentUser UserContext userContext) {
    return R.ok(orderService.getDetail(id, userContext.getUserId()));
}

// list
public R<List<OrderDetailVO>> list(@CurrentUser UserContext userContext) {
    return R.ok(orderService.list(userContext.getUserId()));
}

// cancel
public R<Void> cancel(@PathVariable("id") Long id,
                       @CurrentUser UserContext userContext) {
    orderService.cancel(id, userContext.getUserId());
    return R.ok();
}
```

内部 8 个方法签名修改（`@RequestAttribute(value = "role", required = false) Integer role` → `@CurrentUser UserContext userContext`）：

```java
// getOwner — 内部调用，admin 校验
public R<Long> getOwner(@PathVariable("id") Long id,
                         @CurrentUser UserContext userContext) {
    if (!userContext.isAdmin()) {
        throw new BizException(ResultCode.FORBIDDEN);
    }
    ...
}
```

其余 7 个内部方法（getCourseId, getTotalAmount, getSummary, updateStatus, listAll, getOrderCount, getTotalRevenue）同理替换：
- 参数签名：`@RequestAttribute(value = "role", required = false) Integer role` → `@CurrentUser UserContext userContext`
- 方法体内：`if (role != null && role != 1)` → `if (!userContext.isAdmin())`
- 删除方法体内对局部变量 `role` 的引用

- [ ] **步骤 2：编译验证**

```bash
mvn compile -pl order-service -am -q
```

- [ ] **步骤 3：Commit**

```bash
git add order-service/src/main/java/com/learning/order/controller/OrderController.java
git commit -m "refactor: order-service 控制器迁移为 @CurrentUser UserContext"
```

---

### 任务 7：迁移 admin-service 控制器

**文件：**
- 修改：`admin-service/src/main/java/com/learning/admin/controller/CategoryAdminController.java`
- 修改：`admin-service/src/main/java/com/learning/admin/controller/CourseAdminController.java`
- 修改：`admin-service/src/main/java/com/learning/admin/controller/OrderAdminController.java`
- 修改：`admin-service/src/main/java/com/learning/admin/controller/UserAdminController.java`
- 修改：`admin-service/src/main/java/com/learning/admin/controller/DashboardController.java`

- [ ] **步骤 1：迁移全部 5 个 admin 控制器**

所有 admin 控制器方法使用 `@RequestAttribute("role") Integer role` → `@CurrentUser UserContext userContext`

服务层调用 `service.xxx(role, ...)` → `service.xxx(userContext, ...)` 或 `service.xxx(userContext.getRole(), ...)`

具体改动（以 CategoryAdminController 为例）：

修改后：
```java
package com.learning.admin.controller;

import com.learning.admin.service.CategoryAdminService;
import com.learning.common.core.result.R;
import com.learning.common.security.annotation.CurrentUser;
import com.learning.common.security.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/category")
@RequiredArgsConstructor
public class CategoryAdminController {

    private final CategoryAdminService categoryAdminService;

    @PostMapping
    public R<Void> create(@RequestParam String name,
                          @RequestParam(defaultValue = "0") Long parentId,
                          @RequestParam(defaultValue = "0") Integer sortOrder,
                          @CurrentUser UserContext userContext) {
        categoryAdminService.createCategory(userContext.getRole(), name, parentId, sortOrder);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable("id") Long id,
                          @RequestParam String name,
                          @RequestParam Integer sortOrder,
                          @CurrentUser UserContext userContext) {
        categoryAdminService.updateCategory(userContext.getRole(), id, name, sortOrder);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable("id") Long id,
                          @CurrentUser UserContext userContext) {
        categoryAdminService.deleteCategory(userContext.getRole(), id);
        return R.ok();
    }
}
```

其余 4 个控制器迁移：

**DashboardController** — 仅 `getStats` 方法：
```java
@GetMapping("/stats")
public R<DashboardStatsResp> getStats(@CurrentUser UserContext userContext) {
    return R.ok(dashboardService.getStats(userContext.getRole()));
}
```

**CourseAdminController** — 8 个方法，全部 `@RequestAttribute("role") Integer role` → `@CurrentUser UserContext userContext`，服务调用 `xxx(userContext.getRole(), ...)` ：
```java
public R<PageResp<Map<String, Object>>> list(PageReq req,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String teacherName,
        @RequestParam(required = false) Integer status,
        @CurrentUser UserContext userContext) {
    return R.ok(courseAdminService.listCourses(userContext.getRole(), req, keyword, teacherName, status));
}

public R<Void> create(@Valid @RequestBody CourseSaveReq req,
                      @CurrentUser UserContext userContext) { ... }

public R<Void> update(@PathVariable("id") Long id,
                      @Valid @RequestBody CourseSaveReq req,
                      @CurrentUser UserContext userContext) { ... }
// ... 其余 5 个方法同理
```

**OrderAdminController** — 2 个方法：
```java
public R<PageResp<Map<String, Object>>> list(PageReq req,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Integer status,
        @CurrentUser UserContext userContext) {
    return R.ok(orderAdminService.listOrders(userContext.getRole(), req, keyword, status));
}

public R<Void> updateStatus(@PathVariable("id") Long id,
                            @RequestParam Integer status,
                            @CurrentUser UserContext userContext) { ... }
```

**UserAdminController** — 2 个方法：
```java
public R<PageResp<Map<String, Object>>> list(PageReq req,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Integer roleFilter,
        @RequestParam(required = false) Integer status,
        @CurrentUser UserContext userContext) {
    return R.ok(userAdminService.listUsers(userContext.getRole(), req, keyword, roleFilter, status));
}

public R<Void> updateStatus(@PathVariable("id") Long id,
                            @RequestBody UserStatusReq req,
                            @CurrentUser UserContext userContext) { ... }
```

- [ ] **步骤 2：编译验证**

```bash
mvn compile -pl admin-service -am -q
```

- [ ] **步骤 3：Commit**

```bash
git add admin-service/src/main/java/com/learning/admin/controller/
git commit -m "refactor: admin-service 控制器迁移为 @CurrentUser UserContext"
```

---

### 任务 8：迁移 course-service 和 learning-service 控制器

**文件：**
- 修改：`course-service/src/main/java/com/learning/course/controller/CourseController.java`（仅 `refreshCache` 方法）
- 修改：`course-service/src/main/java/com/learning/course/controller/CourseInternalController.java`
- 修改：`learning-service/src/main/java/com/learning/learning/controller/LearningController.java`
- 修改：`learning-service/src/main/java/com/learning/learning/controller/ReviewController.java`

- [ ] **步骤 1：迁移 CourseController.refreshCache()**

`@RequestAttribute(value = "role", required = false) Integer role` → `@CurrentUser UserContext userContext`

`role != null && role != 1` → `!userContext.isAdmin()`

- [ ] **步骤 2：迁移 CourseInternalController**

所有 11 个方法签名：`@RequestAttribute(value = "role", required = false) Integer role` → `@CurrentUser UserContext userContext`

`checkAdmin` 辅助方法修改：
```java
// 修改前
private void checkAdmin(Integer role) {
    if (role != null && role != 1) {
        throw new BizException(ResultCode.FORBIDDEN);
    }
}

// 修改后
private void checkAdmin(UserContext userContext) {
    if (!userContext.isAdmin()) {
        throw new BizException(ResultCode.FORBIDDEN);
    }
}
```

每个方法调用 `checkAdmin(userContext)` 替换 `checkAdmin(role)`。方法体内如有局部变量 `role` 引用，全部替换为 `userContext.getRole()` 或 `userContext.isAdmin()`。

- [ ] **步骤 3：迁移 LearningController 和 ReviewController**

这两个控制器使用 `HttpServletRequest request` + `request.getAttribute("userId")` 模式 — 改为 `@CurrentUser UserContext userContext`

```java
// 修改前
@GetMapping("/my-courses")
public R<List<MyCourseVO>> myCourses(HttpServletRequest request) {
    Long userId = (Long) request.getAttribute("userId");
    return R.ok(enrollmentService.getMyCourses(userId));
}

// 修改后
@GetMapping("/my-courses")
public R<List<MyCourseVO>> myCourses(@CurrentUser UserContext userContext) {
    return R.ok(enrollmentService.getMyCourses(userContext.getUserId()));
}
```

- [ ] **步骤 4：编译验证**

```bash
mvn compile -pl course-service,learning-service -am -q
```

- [ ] **步骤 5：Commit**

```bash
git add course-service/src/main/java/com/learning/course/controller/CourseController.java course-service/src/main/java/com/learning/course/controller/CourseInternalController.java learning-service/src/main/java/com/learning/learning/controller/LearningController.java learning-service/src/main/java/com/learning/learning/controller/ReviewController.java
git commit -m "refactor: course/learning 控制器迁移为 @CurrentUser UserContext"
```

---

### 任务 9：全量编译验证并确认无 @RequestAttribute 残留

- [ ] **步骤 1：全量编译**

```bash
mvn compile -q
```

- [ ] **步骤 2：检查无残留 @RequestAttribute**

```bash
grep -r "@RequestAttribute" --include="*.java" --exclude-dir=target
```

应返回空结果或仅剩注释。如果还有残留，修复后重新编译。

- [ ] **步骤 3：Commit**

```bash
git add -A
git commit -m "chore: 清理 @RequestAttribute 残留引用"
```

---

### 任务 10：将 OrderPaidMessage 抽取到 common-mq

**文件：**
- 创建：`common/common-mq/src/main/java/com/learning/common/mq/message/OrderPaidMessage.java`
- 删除：`payment-service/src/main/java/com/learning/payment/mq/message/OrderPaidMessage.java`
- 删除：`learning-service/src/main/java/com/learning/learning/mq/message/OrderPaidMessage.java`
- 删除：`order-service/src/main/java/com/learning/order/mq/message/OrderPaidMessage.java`

- [ ] **步骤 1：在 common-mq 创建共享 OrderPaidMessage**

```java
package com.learning.common.mq.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单支付成功消息 — 由 payment-service 发送，learning-service/order-service 消费
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderPaidMessage extends BaseMessage {

    private Long orderId;
    private String orderNo;
    private Long userId;
    private Long courseId;

    /** 支付金额 — 仅 payment-service 会填充，消费端 JSON 反序列化自动忽略多余字段 */
    private BigDecimal amount;

    public OrderPaidMessage(Long orderId, String orderNo, Long userId, Long courseId, BigDecimal amount) {
        super();
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.userId = userId;
        this.courseId = courseId;
        this.amount = amount;
        setEventType("order.paid");
    }
}
```

- [ ] **步骤 2：更新 3 个消费/生产端的 import**

4 个文件的 import 语句需更新：

**payment-service/PaymentServiceImpl.java** 行 13：
```java
// 修改前
import com.learning.payment.mq.message.OrderPaidMessage;
// 修改后
import com.learning.common.mq.message.OrderPaidMessage;
```

**payment-service/PaymentEventProducer.java** 行 3：
```java
// 修改前
import com.learning.payment.mq.message.OrderPaidMessage;
// 修改后
import com.learning.common.mq.message.OrderPaidMessage;
```

**learning-service/EnrollmentConsumer.java** 行 4：
```java
// 修改前
import com.learning.learning.mq.message.OrderPaidMessage;
// 修改后
import com.learning.common.mq.message.OrderPaidMessage;
```

**order-service/OrderEventProducer.java** 行 5：
```java
// 修改前
import com.learning.order.mq.message.OrderPaidMessage;
// 修改后
import com.learning.common.mq.message.OrderPaidMessage;
```

- [ ] **步骤 3：删除 3 个本地定义文件**

```bash
rm payment-service/src/main/java/com/learning/payment/mq/message/OrderPaidMessage.java
rm learning-service/src/main/java/com/learning/learning/mq/message/OrderPaidMessage.java
rm order-service/src/main/java/com/learning/order/mq/message/OrderPaidMessage.java
```

- [ ] **步骤 4：检查创建者是否需要调整**

检查 `PaymentEventProducer` 和 `OrderEventProducer` 中构造 `OrderPaidMessage` 的代码是否与新全参构造函数匹配。payment 版本原本设置 amount，order 版本原本无 amount——需调整为：

**OrderEventProducer** (order-service)：调用处改为 `new OrderPaidMessage(orderId, orderNo, userId, courseId, null)`

**PaymentEventProducer** (payment-service)：保持传入 amount

- [ ] **步骤 5：编译验证**

```bash
mvn compile -q
```

- [ ] **步骤 6：Commit**

```bash
git add common/common-mq/src/main/java/com/learning/common/mq/message/OrderPaidMessage.java
git add -u
git commit -m "refactor: OrderPaidMessage 抽取到 common-mq，消除三处重复"
```

---

### 任务 11：将 CourseUpdatedMessage / CategoryUpdatedMessage 抽取到 common-mq

**文件：**
- 创建：`common/common-mq/src/main/java/com/learning/common/mq/message/CourseUpdatedMessage.java`
- 创建：`common/common-mq/src/main/java/com/learning/common/mq/message/CategoryUpdatedMessage.java`
- 删除：`admin-service/src/main/java/com/learning/admin/mq/message/CourseUpdatedMessage.java`
- 删除：`admin-service/src/main/java/com/learning/admin/mq/message/CategoryUpdatedMessage.java`
- 修改：`admin-service/src/main/java/com/learning/admin/mq/producer/AdminEventProducer.java`
- 修改：`course-service/src/main/java/com/learning/course/mq/consumer/CacheRefreshConsumer.java`

- [ ] **步骤 1：在 common-mq 创建 CourseUpdatedMessage**

```java
package com.learning.common.mq.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 课程更新消息 — admin-service 发送，course-service 消费刷新缓存
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CourseUpdatedMessage extends BaseMessage {

    /** 课程 ID */
    private Long courseId;

    /** 操作类型：1=更新，2=下架，3=删除 */
    private Integer operation;

    public CourseUpdatedMessage(Long courseId, Integer operation) {
        super();
        this.courseId = courseId;
        this.operation = operation;
        setEventType("course.updated");
    }
}
```

- [ ] **步骤 2：在 common-mq 创建 CategoryUpdatedMessage**

```java
package com.learning.common.mq.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 分类更新消息 — admin-service 发送，course-service 消费刷新分类缓存
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CategoryUpdatedMessage extends BaseMessage {

    /** 分类 ID */
    private Long categoryId;

    /** 操作类型：1=创建/更新，2=删除 */
    private Integer operation;

    public CategoryUpdatedMessage(Long categoryId, Integer operation) {
        super();
        this.categoryId = categoryId;
        this.operation = operation;
        setEventType("category.updated");
    }
}
```

- [ ] **步骤 3：更新 AdminEventProducer import**

```java
// 修改前
import com.learning.admin.mq.message.CourseUpdatedMessage;
import com.learning.admin.mq.message.CategoryUpdatedMessage;
// 修改后
import com.learning.common.mq.message.CourseUpdatedMessage;
import com.learning.common.mq.message.CategoryUpdatedMessage;
```

- [ ] **步骤 4：删除 admin-service 旧消息类**

```bash
rm admin-service/src/main/java/com/learning/admin/mq/message/CourseUpdatedMessage.java
rm admin-service/src/main/java/com/learning/admin/mq/message/CategoryUpdatedMessage.java
```

- [ ] **步骤 5：编译验证**

```bash
mvn compile -q
```

- [ ] **步骤 6：Commit**

```bash
git add common/common-mq/src/main/java/com/learning/common/mq/message/CourseUpdatedMessage.java common/common-mq/src/main/java/com/learning/common/mq/message/CategoryUpdatedMessage.java
git add -u
git commit -m "refactor: CourseUpdatedMessage/CategoryUpdatedMessage 抽取到 common-mq"
```

---

### 任务 12：改造 CacheRefreshConsumer 实现精确驱逐

**文件：**
- 修改：`course-service/src/main/java/com/learning/course/mq/consumer/CacheRefreshConsumer.java`

- [ ] **步骤 1：重写 CacheRefreshConsumer**

修改前：
```java
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheRefreshConsumer {

    private final CourseCacheService courseCacheService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_COURSE_UPDATED_CACHE)
    public void handleCourseUpdated(@Payload Object msg, Channel channel,
                                     @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            Map<?, ?> map = (Map<?, ?>) msg;
            String eventType = (String) map.get("eventType");
            log.info("收到缓存刷新消息: eventType={}", eventType);
            // TODO 根据eventType分类驱逐
            courseCacheService.refreshAllCaches();
            log.info("缓存已全量刷新");
        } catch (Exception e) {
            log.error("缓存刷新失败", e);
        } finally {
            channel.basicAck(tag, false);
        }
    }
}
```

修改后：
```java
package com.learning.course.mq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.common.mq.message.CategoryUpdatedMessage;
import com.learning.common.mq.message.CourseUpdatedMessage;
import com.learning.course.cache.CourseCacheService;
import com.learning.course.config.RabbitMQConfig;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 缓存刷新消费者 — 监听 course.updated / category.updated 消息，精确驱逐对应缓存
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheRefreshConsumer {

    private final CourseCacheService courseCacheService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_COURSE_UPDATED_CACHE)
    public void handleCourseUpdated(Message message, Channel channel,
                                     @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            byte[] body = message.getBody();
            String eventType = extractEventType(body);

            if ("course.updated".equals(eventType)) {
                handleCourseUpdated(body);
            } else if ("category.updated".equals(eventType)) {
                handleCategoryUpdated(body);
            } else {
                log.warn("未知事件类型: {}, 执行全量刷新兜底", eventType);
                courseCacheService.refreshAllCaches();
            }
        } catch (Exception e) {
            log.error("缓存刷新失败，执行全量刷新兜底", e);
            courseCacheService.refreshAllCaches();
        } finally {
            channel.basicAck(tag, false);
        }
    }

    private void handleCourseUpdated(byte[] body) throws IOException {
        CourseUpdatedMessage msg = objectMapper.readValue(body, CourseUpdatedMessage.class);
        Long courseId = msg.getCourseId();
        Integer operation = msg.getOperation();
        log.info("课程缓存刷新: courseId={}, operation={}", courseId, operation);

        // 精确驱逐对应课程详情
        courseCacheService.evict(courseId);

        // 热门排名可能变化
        courseCacheService.evictHotTop10();

        // 删除课程时还需清除分类树（课程-分类关联变更）
        if (operation != null && operation == 3) {
            courseCacheService.evictCategoryTree();
        }
    }

    private void handleCategoryUpdated(byte[] body) throws IOException {
        CategoryUpdatedMessage msg = objectMapper.readValue(body, CategoryUpdatedMessage.class);
        log.info("分类缓存刷新: categoryId={}, operation={}", msg.getCategoryId(), msg.getOperation());
        courseCacheService.evictCategoryTree();
    }

    private String extractEventType(byte[] body) throws IOException {
        // 从 JSON 中快速提取 eventType 字段，避免完整反序列化
        String json = new String(body, java.nio.charset.StandardCharsets.UTF_8);
        int idx = json.indexOf("\"eventType\"");
        if (idx < 0) return null;
        int colonIdx = json.indexOf(":", idx);
        int startQuote = json.indexOf("\"", colonIdx);
        int endQuote = json.indexOf("\"", startQuote + 1);
        if (startQuote < 0 || endQuote < 0) return null;
        return json.substring(startQuote + 1, endQuote);
    }
}
```

- [ ] **步骤 2：编译验证**

```bash
mvn compile -pl course-service -am -q
```

- [ ] **步骤 3：Commit**

```bash
git add course-service/src/main/java/com/learning/course/mq/consumer/CacheRefreshConsumer.java
git commit -m "feat: CacheRefreshConsumer 改为按 courseId 精确驱逐缓存"
```

---

### 任务 13：创建 common-security 测试 — JwtUtilTest

**文件：**
- 创建：`common/common-security/src/test/java/com/learning/common/security/util/JwtUtilTest.java`

依赖：`spring-boot-starter-test` (JUnit 5 + Mockito)，在 `common-security/pom.xml` 中添加 test scope 依赖（如果尚未存在）

- [ ] **步骤 1：检查并补充测试依赖**

检查 `common-security/pom.xml` 是否已有 `spring-boot-starter-test`，若没有则添加：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

- [ ] **步骤 2：编写 JwtUtilTest**

```java
package com.learning.common.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtUtil 单元测试")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        jwtUtil.setSecret("test-secret-key-for-unit-test-min-256-bits-long!!");
        jwtUtil.setExpiration(3600L);
    }

    @Test
    @DisplayName("签发 token 并成功解析")
    void shouldGenerateAndParseToken() {
        String token = jwtUtil.generateToken(1L, 0);
        assertNotNull(token);

        Claims claims = jwtUtil.parseToken(token);
        assertEquals(1L, claims.get("userId", Long.class));
        assertEquals(0, claims.get("role", Integer.class));
    }

    @Test
    @DisplayName("过期 token 抛出 ExpiredJwtException")
    void shouldThrowExceptionForExpiredToken() {
        jwtUtil.setExpiration(-1L); // 立即过期
        String token = jwtUtil.generateToken(1L, 0);

        assertThrows(ExpiredJwtException.class, () -> jwtUtil.parseToken(token));
    }

    @Test
    @DisplayName("获取 userId 和 role")
    void shouldExtractUserIdAndRole() {
        String token = jwtUtil.generateToken(42L, 1);
        assertEquals(42L, jwtUtil.getUserId(token));
        assertEquals(1, jwtUtil.getRole(token));
    }

    @Test
    @DisplayName("无效 token 抛出异常")
    void shouldThrowExceptionForInvalidToken() {
        assertThrows(Exception.class, () -> jwtUtil.parseToken("invalid.token.here"));
    }
}
```

- [ ] **步骤 3：运行测试**

```bash
mvn test -pl common/common-security -am
```

预期：4 个测试全部通过。

- [ ] **步骤 4：Commit**

```bash
git add common/common-security/src/test/
git commit -m "test: 新增 JwtUtil 单元测试"
```

---

### 任务 14：创建 user-service 测试 — UserServiceImplTest

**文件：**
- 创建：`user-service/src/test/java/com/learning/user/service/impl/UserServiceImplTest.java`

- [ ] **步骤 1：编写 UserServiceImplTest**

```java
package com.learning.user.service.impl;

import com.learning.common.core.exception.BizException;
import com.learning.common.security.util.JwtUtil;
import com.learning.user.dto.req.LoginReq;
import com.learning.user.dto.req.RegisterReq;
import com.learning.user.dto.req.UpdateUserInfoReq;
import com.learning.user.dto.resp.LoginResp;
import com.learning.user.dto.resp.UserInfoResp;
import com.learning.user.entity.User;
import com.learning.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl 单元测试")
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() throws Exception {
        // BCryptPasswordEncoder 是 static final，需要通过反射注入
        Field encoderField = UserServiceImpl.class.getDeclaredField("PASSWORD_ENCODER");
        encoderField.setAccessible(true);
        // 使用反射修改 static final 字段的值
        // 实际上 BCryptPasswordEncoder 已经在类初始化时创建，无需额外设置
    }

    @Test
    @DisplayName("注册 — 手机号已存在应抛异常")
    void shouldThrowExceptionWhenPhoneExists() {
        RegisterReq req = new RegisterReq();
        req.setPhone("13800138000");
        req.setPassword("123456");

        when(userMapper.selectOne(any())).thenReturn(new User());

        assertThrows(BizException.class, () -> userService.register(req));
        verify(userMapper, never()).insert(any());
    }

    @Test
    @DisplayName("注册 — 新用户成功注册")
    void shouldRegisterSuccessfully() {
        RegisterReq req = new RegisterReq();
        req.setPhone("13800138000");
        req.setPassword("123456");
        req.setUsername("testuser");

        when(userMapper.selectOne(any())).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        assertDoesNotThrow(() -> userService.register(req));
        verify(userMapper).insert(any(User.class));
    }

    @Test
    @DisplayName("登录 — 手机号不存在抛异常")
    void shouldThrowExceptionWhenPhoneNotFound() {
        LoginReq req = new LoginReq();
        req.setPhone("13800138000");
        req.setPassword("123456");

        when(userMapper.selectOne(any())).thenReturn(null);

        assertThrows(BizException.class, () -> userService.login(req));
    }

    @Test
    @DisplayName("登录 — 密码错误抛异常")
    void shouldThrowExceptionWhenPasswordWrong() {
        LoginReq req = new LoginReq();
        req.setPhone("13800138000");
        req.setPassword("wrongpassword");

        User user = new User();
        user.setId(1L);
        user.setPhone("13800138000");
        user.setPassword(new BCryptPasswordEncoder().encode("correctpassword"));

        when(userMapper.selectOne(any())).thenReturn(user);

        assertThrows(BizException.class, () -> userService.login(req));
    }

    @Test
    @DisplayName("登录 — 成功后返回 token")
    void shouldReturnTokenOnSuccess() {
        LoginReq req = new LoginReq();
        req.setPhone("13800138000");
        req.setPassword("123456");

        User user = new User();
        user.setId(1L);
        user.setPhone("13800138000");
        user.setPassword(new BCryptPasswordEncoder().encode("123456"));
        user.setRole(0);
        user.setStatus(1);

        when(userMapper.selectOne(any())).thenReturn(user);
        when(jwtUtil.generateToken(1L, 0)).thenReturn("mock-jwt-token");

        LoginResp resp = userService.login(req);
        assertNotNull(resp);
        assertEquals("mock-jwt-token", resp.getToken());
    }

    @Test
    @DisplayName("获取用户信息 — 用户不存在抛异常")
    void shouldThrowExceptionWhenUserNotFound() {
        when(userMapper.selectById(999L)).thenReturn(null);
        assertThrows(BizException.class, () -> userService.getUserInfo(999L));
    }

    @Test
    @DisplayName("获取用户信息 — 正常返回")
    void shouldReturnUserInfo() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPhone("13800138000");
        user.setAvatar("http://avatar.png");

        when(userMapper.selectById(1L)).thenReturn(user);

        UserInfoResp resp = userService.getUserInfo(1L);
        assertNotNull(resp);
        assertEquals("testuser", resp.getUsername());
    }

    @Test
    @DisplayName("修改密码 — 旧密码错误抛异常")
    void shouldThrowExceptionWhenOldPasswordWrong() {
        User user = new User();
        user.setId(1L);
        user.setPassword(new BCryptPasswordEncoder().encode("correct"));

        when(userMapper.selectById(1L)).thenReturn(user);

        assertThrows(BizException.class,
                () -> userService.changePassword(1L, "wrong", "newpass"));
    }
}
```

- [ ] **步骤 2：运行测试**

```bash
mvn test -pl user-service -am
```

- [ ] **步骤 3：Commit**

```bash
git add user-service/src/test/
git commit -m "test: 新增 UserServiceImpl 单元测试"
```

---

### 任务 15：创建 course-service 测试 — CourseCacheServiceTest

**文件：**
- 创建：`course-service/src/test/java/com/learning/course/cache/CourseCacheServiceTest.java`

- [ ] **步骤 1：编写 CourseCacheServiceTest**

```java
package com.learning.course.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.learning.course.dto.resp.CourseDetailVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.*;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseCacheService 单元测试")
class CourseCacheServiceTest {

    @Mock
    private Cache<Long, CourseDetailVO> caffeineCache;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private SetOperations<String, Object> setOperations;

    @InjectMocks
    private CourseCacheService courseCacheService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    @DisplayName("Caffeine 命中直接返回")
    void shouldReturnFromCaffeineWhenHit() {
        CourseDetailVO cached = new CourseDetailVO();
        cached.setId(1L);
        cached.setTitle("Cached Course");

        when(caffeineCache.getIfPresent(1L)).thenReturn(cached);

        Function<Long, CourseDetailVO> loader = id -> {
            throw new RuntimeException("should not be called");
        };

        CourseDetailVO result = courseCacheService.getCourseDetail(1L, loader);
        assertNotNull(result);
        assertEquals("Cached Course", result.getTitle());
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    @DisplayName("Caffeine miss → Redis hit")
    void shouldFallbackToRedisWhenCaffeineMiss() {
        CourseDetailVO redisCached = new CourseDetailVO();
        redisCached.setId(1L);
        redisCached.setTitle("Redis Course");

        when(caffeineCache.getIfPresent(1L)).thenReturn(null);
        when(valueOperations.get("course:detail:1")).thenReturn(redisCached);

        Function<Long, CourseDetailVO> loader = id -> {
            throw new RuntimeException("should not be called");
        };

        CourseDetailVO result = courseCacheService.getCourseDetail(1L, loader);
        assertNotNull(result);
        assertEquals("Redis Course", result.getTitle());
        verify(caffeineCache).put(eq(1L), any(CourseDetailVO.class));
    }

    @Test
    @DisplayName("Redis 空值标记防穿透")
    void shouldReturnNullForNullMarker() {
        when(caffeineCache.getIfPresent(1L)).thenReturn(null);
        when(valueOperations.get("course:detail:1")).thenReturn(CourseCacheService.NULL_MARKER);

        CourseDetailVO result = courseCacheService.getCourseDetail(1L, id -> null);
        assertNull(result);
    }

    @Test
    @DisplayName("驱逐单个课程缓存")
    void shouldEvictSingleCourse() {
        courseCacheService.evict(1L);

        verify(redisTemplate).delete("course:detail:1");
        verify(caffeineCache).invalidate(1L);
    }

    @Test
    @DisplayName("驱逐全部课程详情缓存")
    void shouldEvictAllCourseDetails() {
        when(redisTemplate.keys("course:detail:*")).thenReturn(
                java.util.Set.of("course:detail:1", "course:detail:2"));

        courseCacheService.evictAllCourseDetail();

        verify(redisTemplate).delete(anySet());
        verify(caffeineCache).invalidateAll();
    }

    @Test
    @DisplayName("驱逐分类树缓存")
    void shouldEvictCategoryTree() {
        courseCacheService.evictCategoryTree();

        verify(redisTemplate).delete("course:category:tree");
        verify(redisTemplate).delete("course:category:list");
    }

    @Test
    @DisplayName("驱逐热门课程")
    void shouldEvictHotTop10() {
        courseCacheService.evictHotTop10();

        verify(redisTemplate).delete("course:hot:top10");
    }
}
```

- [ ] **步骤 2：运行测试**

```bash
mvn test -pl course-service -am
```

- [ ] **步骤 3：Commit**

```bash
git add course-service/src/test/
git commit -m "test: 新增 CourseCacheService 单元测试"
```

---

### 任务 16：创建 cart-service 测试 — CartServiceImplTest

**文件：**
- 创建：`cart-service/src/test/java/com/learning/cart/service/impl/CartServiceImplTest.java`

- [ ] **步骤 1：编写 CartServiceImplTest**

```java
package com.learning.cart.service.impl;

import com.learning.cart.client.CourseClient;
import com.learning.cart.entity.Cart;
import com.learning.cart.mapper.CartMapper;
import com.learning.cart.cache.CartCacheService;
import com.learning.common.core.exception.BizException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartServiceImpl 单元测试")
class CartServiceImplTest {

    @Mock
    private CartMapper cartMapper;

    @Mock
    private CartCacheService cartCacheService;

    @Mock
    private CourseClient courseClient;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    @DisplayName("添加购物车 — 重复加入应增加数量")
    void shouldIncrementQuantityWhenAlreadyInCart() {
        Cart existing = new Cart();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setCourseId(100L);
        existing.setQuantity(1);

        when(cartMapper.selectOne(any())).thenReturn(existing);
        when(cartMapper.updateById(any(Cart.class))).thenReturn(1);

        cartService.add(1L, 100L);

        verify(cartMapper).updateById(argThat(c -> c.getQuantity() == 2));
        verify(cartMapper, never()).insert(any());
    }

    @Test
    @DisplayName("添加购物车 — 新课程直接插入")
    void shouldInsertWhenNotInCart() {
        when(cartMapper.selectOne(any())).thenReturn(null);
        when(cartMapper.insert(any(Cart.class))).thenReturn(1);

        cartService.add(1L, 100L);

        verify(cartMapper).insert(any(Cart.class));
    }

    @Test
    @DisplayName("移出购物车")
    void shouldRemoveFromCart() {
        when(cartMapper.delete(any())).thenReturn(1);

        cartService.remove(1L, 100L);

        verify(cartMapper).delete(any());
    }

    @Test
    @DisplayName("清空购物车")
    void shouldClearCart() {
        when(cartMapper.delete(any())).thenReturn(3);

        cartService.clear(1L);

        verify(cartMapper).delete(any());
    }
}
```

- [ ] **步骤 2：运行测试**

```bash
mvn test -pl cart-service -am
```

- [ ] **步骤 3：Commit**

```bash
git add cart-service/src/test/
git commit -m "test: 新增 CartServiceImpl 单元测试"
```

---

### 任务 17：创建 order-service 测试

**文件：**
- 创建：`order-service/src/test/java/com/learning/order/service/impl/OrderServiceImplTest.java`
- 创建：`order-service/src/test/java/com/learning/order/mq/producer/OrderEventProducerTest.java`

- [ ] **步骤 1：编写 OrderServiceImplTest**

```java
package com.learning.order.service.impl;

import com.learning.common.core.exception.BizException;
import com.learning.common.core.result.R;
import com.learning.common.core.result.ResultCode;
import com.learning.order.client.CourseClient;
import com.learning.order.dto.req.CreateOrderReq;
import com.learning.order.dto.resp.OrderDetailVO;
import com.learning.order.entity.Order;
import com.learning.order.mapper.OrderItemMapper;
import com.learning.order.mapper.OrderMapper;
import com.learning.order.mq.producer.OrderEventProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl 单元测试")
class OrderServiceImplTest {

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private OrderEventProducer orderEventProducer;
    @Mock
    private CourseClient courseClient;
    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("查询详情 — 非本人订单抛异常")
    void shouldThrowExceptionWhenNotOwner() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(2L); // 不属于 userId=1

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BizException.class, () -> orderService.getDetail(1L, 1L));
    }

    @Test
    @DisplayName("取消订单 — 非本人订单抛异常")
    void shouldThrowExceptionWhenCancelNotOwn() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(2L);

        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThrows(BizException.class, () -> orderService.cancel(1L, 1L));
    }

    @Test
    @DisplayName("获取订单归属用户 ID")
    void shouldReturnOwnerUserId() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(100L);

        when(orderMapper.selectById(1L)).thenReturn(order);

        Long ownerId = orderService.getOwnerUserId(1L);
        assertEquals(100L, ownerId);
    }

    @Test
    @DisplayName("获取订单归属 — 订单不存在抛异常")
    void shouldThrowExceptionWhenOrderNotFound() {
        when(orderMapper.selectById(999L)).thenReturn(null);
        assertThrows(BizException.class, () -> orderService.getOwnerUserId(999L));
    }
}
```

- [ ] **步骤 2：编写 OrderEventProducerTest**

```java
package com.learning.order.mq.producer;

import com.learning.common.mq.message.OrderPaidMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderEventProducer 单元测试")
class OrderEventProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderEventProducer producer;

    @Test
    @DisplayName("发送订单支付成功消息")
    void shouldSendOrderPaidMessage() {
        producer.sendOrderPaid(1L, "ORD001", 100L, 200L);

        verify(rabbitTemplate).convertAndSend(
                eq("order.topic"),
                eq("order.paid"),
                any(OrderPaidMessage.class),
                any());
    }

    @Test
    @DisplayName("发送订单创建消息")
    void shouldSendOrderCreatedMessage() {
        producer.sendOrderCreated(1L);

        verify(rabbitTemplate).convertAndSend(
                eq("order.topic"),
                eq("order.created"),
                any(),
                any());
    }
}
```

- [ ] **步骤 3：运行测试**

```bash
mvn test -pl order-service -am
```

- [ ] **步骤 4：Commit**

```bash
git add order-service/src/test/
git commit -m "test: 新增 OrderServiceImpl 和 OrderEventProducer 单元测试"
```

---

### 任务 18：创建 payment-service 测试

**文件：**
- 创建：`payment-service/src/test/java/com/learning/payment/service/impl/PaymentServiceImplTest.java`

- [ ] **步骤 1：编写 PaymentServiceImplTest**

```java
package com.learning.payment.service.impl;

import com.learning.common.core.exception.BizException;
import com.learning.common.core.result.R;
import com.learning.payment.client.OrderClient;
import com.learning.payment.dto.resp.OrderSummaryVO;
import com.learning.payment.entity.PaymentRecord;
import com.learning.payment.mapper.PaymentRecordMapper;
import com.learning.payment.mq.producer.PaymentEventProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.*;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentServiceImpl 单元测试")
class PaymentServiceImplTest {

    @Mock
    private PaymentRecordMapper paymentRecordMapper;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private OrderClient orderClient;
    @Mock
    private PaymentEventProducer paymentEventProducer;
    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    @DisplayName("支付 — 订单不属于当前用户抛异常")
    void shouldThrowExceptionWhenNotOwner() {
        OrderSummaryVO summary = new OrderSummaryVO();
        summary.setUserId(2L);
        summary.setStatus(0);
        summary.setTotalAmount(BigDecimal.TEN);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);
        when(orderClient.getSummary(1L)).thenReturn(R.ok(summary));

        assertThrows(BizException.class, () -> paymentService.pay(1L, 1L));
    }

    @Test
    @DisplayName("支付 — 订单已支付抛异常")
    void shouldThrowExceptionWhenAlreadyPaid() {
        OrderSummaryVO summary = new OrderSummaryVO();
        summary.setUserId(1L);
        summary.setStatus(1); // 已支付
        summary.setTotalAmount(BigDecimal.TEN);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);
        when(orderClient.getSummary(1L)).thenReturn(R.ok(summary));

        assertThrows(BizException.class, () -> paymentService.pay(1L, 1L));
    }

    @Test
    @DisplayName("支付 — 获取分布式锁失败（重复支付）抛异常")
    void shouldThrowExceptionWhenLockFailed() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(false);

        assertThrows(BizException.class, () -> paymentService.pay(1L, 1L));
    }
}
```

- [ ] **步骤 2：运行测试**

```bash
mvn test -pl payment-service -am
```

- [ ] **步骤 3：Commit**

```bash
git add payment-service/src/test/
git commit -m "test: 新增 PaymentServiceImpl 单元测试"
```

---

### 任务 19：创建 learning-service 测试

**文件：**
- 创建：`learning-service/src/test/java/com/learning/learning/mq/consumer/EnrollmentConsumerTest.java`
- 创建：`learning-service/src/test/java/com/learning/learning/service/impl/ProgressServiceImplTest.java`

- [ ] **步骤 1：编写 EnrollmentConsumerTest**

```java
package com.learning.learning.mq.consumer;

import com.learning.common.mq.message.OrderPaidMessage;
import com.learning.learning.service.EnrollmentService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.*;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnrollmentConsumer 单元测试")
class EnrollmentConsumerTest {

    @Mock
    private EnrollmentService enrollmentService;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;
    @Mock
    private Channel channel;

    @InjectMocks
    private EnrollmentConsumer consumer;

    @Test
    @DisplayName("幂等消费 — SETNX 成功则报名")
    void shouldEnrollWhenNotProcessed() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq("enroll:lock:1:100"), eq("1"),
                anyLong(), any())).thenReturn(true);

        OrderPaidMessage msg = new OrderPaidMessage(1L, "ORD001", 100L, 200L, BigDecimal.TEN);

        consumer.handleOrderPaid(msg, channel, 1L);

        verify(enrollmentService).enroll(100L, 200L);
        verify(channel).basicAck(1L, false);
    }

    @Test
    @DisplayName("幂等消费 — SETNX 失败跳过")
    void shouldSkipWhenAlreadyProcessed() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(),
                anyLong(), any())).thenReturn(false);

        OrderPaidMessage msg = new OrderPaidMessage(1L, "ORD001", 100L, 200L, BigDecimal.TEN);

        consumer.handleOrderPaid(msg, channel, 1L);

        verify(enrollmentService, never()).enroll(anyLong(), anyLong());
        verify(channel).basicAck(1L, false);
    }
}
```

- [ ] **步骤 2：编写 ProgressServiceImplTest（进度防刷）**

```java
package com.learning.learning.service.impl;

import com.learning.common.core.exception.BizException;
import com.learning.learning.dto.req.ProgressReportReq;
import com.learning.learning.entity.VideoProgress;
import com.learning.learning.mapper.VideoProgressMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProgressServiceImpl 进度防刷单元测试")
class ProgressServiceImplTest {

    @Mock
    private VideoProgressMapper videoProgressMapper;

    @InjectMocks
    private ProgressServiceImpl progressService;

    @Test
    @DisplayName("进度只进不退 — 提交更大进度成功")
    void shouldAcceptLargerProgress() {
        ProgressReportReq req = new ProgressReportReq();
        req.setCourseId(100L);
        req.setVideoId(1L);
        req.setCurrentTime(60.0);

        VideoProgress existing = new VideoProgress();
        existing.setId(1L);
        existing.setCurrentTime(30.0);

        when(videoProgressMapper.selectOne(any())).thenReturn(existing);

        assertDoesNotThrow(() -> progressService.reportProgress(1L, req));
        verify(videoProgressMapper).updateById(any(VideoProgress.class));
    }

    @Test
    @DisplayName("进度只进不退 — 提交更小进度抛异常")
    void shouldRejectSmallerProgress() {
        ProgressReportReq req = new ProgressReportReq();
        req.setCourseId(100L);
        req.setVideoId(1L);
        req.setCurrentTime(20.0);

        VideoProgress existing = new VideoProgress();
        existing.setId(1L);
        existing.setCurrentTime(50.0);

        when(videoProgressMapper.selectOne(any())).thenReturn(existing);

        assertThrows(BizException.class, () -> progressService.reportProgress(1L, req));
        verify(videoProgressMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("首次上报进度")
    void shouldAcceptFirstProgress() {
        ProgressReportReq req = new ProgressReportReq();
        req.setCourseId(100L);
        req.setVideoId(1L);
        req.setCurrentTime(30.0);

        when(videoProgressMapper.selectOne(any())).thenReturn(null);

        assertDoesNotThrow(() -> progressService.reportProgress(1L, req));
        verify(videoProgressMapper).insert(any(VideoProgress.class));
    }
}
```

- [ ] **步骤 3：运行测试**

```bash
mvn test -pl learning-service -am
```

- [ ] **步骤 4：Commit**

```bash
git add learning-service/src/test/
git commit -m "test: 新增 EnrollmentConsumer 和 ProgressServiceImpl 单元测试"
```

---

### 任务 20：创建 admin-service 测试

**文件：**
- 创建：`admin-service/src/test/java/com/learning/admin/service/impl/DashboardServiceImplTest.java`
- 创建：`admin-service/src/test/java/com/learning/admin/mq/producer/AdminEventProducerTest.java`

- [ ] **步骤 1：编写 DashboardServiceImplTest**

```java
package com.learning.admin.service.impl;

import com.learning.admin.client.CourseServiceClient;
import com.learning.admin.client.OrderServiceClient;
import com.learning.admin.client.UserServiceClient;
import com.learning.admin.dto.resp.DashboardStatsResp;
import com.learning.admin.service.AdminAuthService;
import com.learning.common.core.exception.BizException;
import com.learning.common.core.result.R;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardServiceImpl 单元测试")
class DashboardServiceImplTest {

    @Mock
    private AdminAuthService authService;
    @Mock
    private CourseServiceClient courseServiceClient;
    @Mock
    private UserServiceClient userServiceClient;
    @Mock
    private OrderServiceClient orderServiceClient;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    @DisplayName("正常聚合仪表盘数据")
    void shouldAggregateDashboardStats() {
        when(courseServiceClient.getCourseCount()).thenReturn(R.ok(50L));
        when(userServiceClient.getUserCount()).thenReturn(R.ok(200L));
        when(orderServiceClient.getOrderCount()).thenReturn(R.ok(100L));
        when(orderServiceClient.getTotalRevenue()).thenReturn(R.ok(BigDecimal.valueOf(9999)));

        DashboardStatsResp stats = dashboardService.getStats(1);

        assertEquals(50L, stats.getCourseCount());
        assertEquals(200L, stats.getUserCount());
        assertEquals(100L, stats.getOrderCount());
        assertEquals(BigDecimal.valueOf(9999), stats.getTotalRevenue());
    }

    @Test
    @DisplayName("非管理员调用抛异常")
    void shouldThrowExceptionWhenNotAdmin() {
        doThrow(new BizException(40015, "仅管理员可操作"))
                .when(authService).checkAdmin(0);

        assertThrows(BizException.class, () -> dashboardService.getStats(0));
        verify(courseServiceClient, never()).getCourseCount();
    }

    @Test
    @DisplayName("下游服务异常抛出")
    void shouldThrowWhenDownstreamFails() {
        when(courseServiceClient.getCourseCount())
                .thenReturn(R.fail(50001, "服务不可用"));

        assertThrows(BizException.class, () -> dashboardService.getStats(1));
    }
}
```

- [ ] **步骤 2：编写 AdminEventProducerTest**

```java
package com.learning.admin.mq.producer;

import com.learning.common.mq.message.CourseUpdatedMessage;
import com.learning.common.mq.message.CategoryUpdatedMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminEventProducer 单元测试")
class AdminEventProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AdminEventProducer producer;

    @Test
    @DisplayName("发送课程更新消息")
    void shouldSendCourseUpdated() {
        CourseUpdatedMessage msg = new CourseUpdatedMessage(1L, 1);

        producer.sendCourseUpdated(msg);

        verify(rabbitTemplate).convertAndSend(
                eq("course.topic"),
                eq("course.updated"),
                eq(msg),
                any());
    }

    @Test
    @DisplayName("发送分类更新消息")
    void shouldSendCategoryUpdated() {
        CategoryUpdatedMessage msg = new CategoryUpdatedMessage(1L, 1);

        producer.sendCategoryUpdated(msg);

        verify(rabbitTemplate).convertAndSend(
                eq("course.topic"),
                eq("course.updated"),
                eq(msg),
                any());
    }
}
```

- [ ] **步骤 3：运行测试**

```bash
mvn test -pl admin-service -am
```

- [ ] **步骤 4：Commit**

```bash
git add admin-service/src/test/
git commit -m "test: 新增 DashboardServiceImpl 和 AdminEventProducer 单元测试"
```

---

### 任务 21：创建 CacheRefreshConsumerTest

**文件：**
- 创建：`course-service/src/test/java/com/learning/course/mq/consumer/CacheRefreshConsumerTest.java`

- [ ] **步骤 1：编写测试**

```java
package com.learning.course.mq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.common.mq.message.CourseUpdatedMessage;
import com.learning.common.mq.message.CategoryUpdatedMessage;
import com.learning.course.cache.CourseCacheService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CacheRefreshConsumer 单元测试")
class CacheRefreshConsumerTest {

    @Mock
    private CourseCacheService courseCacheService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Channel channel;

    @InjectMocks
    private CacheRefreshConsumer consumer;

    @Test
    @DisplayName("course.updated 更新操作 — 精确驱逐 + 热门")
    void shouldEvictSingleCourseAndHotOnUpdate() throws Exception {
        CourseUpdatedMessage msg = new CourseUpdatedMessage(42L, 1);
        byte[] body = objectMapper.writeValueAsBytes(msg);
        Message amqpMsg = new Message(body, new MessageProperties());

        consumer.handleCourseUpdated(amqpMsg, channel, 1L);

        verify(courseCacheService).evict(42L);
        verify(courseCacheService).evictHotTop10();
        verify(courseCacheService, never()).evictCategoryTree();
        verify(channel).basicAck(1L, false);
    }

    @Test
    @DisplayName("course.updated 删除操作 — 额外清除分类树")
    void shouldEvictCategoryTreeOnDelete() throws Exception {
        CourseUpdatedMessage msg = new CourseUpdatedMessage(42L, 3);
        byte[] body = objectMapper.writeValueAsBytes(msg);
        Message amqpMsg = new Message(body, new MessageProperties());

        consumer.handleCourseUpdated(amqpMsg, channel, 1L);

        verify(courseCacheService).evict(42L);
        verify(courseCacheService).evictHotTop10();
        verify(courseCacheService).evictCategoryTree();
        verify(channel).basicAck(1L, false);
    }

    @Test
    @DisplayName("category.updated — 仅清除分类树")
    void shouldEvictCategoryTreeOnly() throws Exception {
        CategoryUpdatedMessage msg = new CategoryUpdatedMessage(5L, 1);
        byte[] body = objectMapper.writeValueAsBytes(msg);
        Message amqpMsg = new Message(body, new MessageProperties());

        consumer.handleCourseUpdated(amqpMsg, channel, 1L);

        verify(courseCacheService).evictCategoryTree();
        verify(courseCacheService, never()).evict(anyLong());
        verify(courseCacheService, never()).evictHotTop10();
        verify(channel).basicAck(1L, false);
    }

    @Test
    @DisplayName("未知事件类型 — 全量刷新兜底")
    void shouldFullRefreshOnUnknownEventType() throws Exception {
        String unknownBody = "{\"eventType\":\"unknown.event\",\"data\":\"test\"}";
        Message amqpMsg = new Message(unknownBody.getBytes(), new MessageProperties());

        consumer.handleCourseUpdated(amqpMsg, channel, 1L);

        verify(courseCacheService).refreshAllCaches();
        verify(channel).basicAck(1L, false);
    }

    @Test
    @DisplayName("反序列化失败 — 全量刷新兜底 + ACK")
    void shouldFullRefreshAndAckOnFailure() throws Exception {
        String badBody = "not valid json at all {{{";
        Message amqpMsg = new Message(badBody.getBytes(), new MessageProperties());

        consumer.handleCourseUpdated(amqpMsg, channel, 1L);

        verify(courseCacheService).refreshAllCaches();
        verify(channel).basicAck(1L, false);
    }
}
```

- [ ] **步骤 2：运行测试**

```bash
mvn test -pl course-service -am
```

- [ ] **步骤 3：Commit**

```bash
git add course-service/src/test/java/com/learning/course/mq/consumer/CacheRefreshConsumerTest.java
git commit -m "test: 新增 CacheRefreshConsumer 精确驱逐单元测试"
```

---

### 任务 22：运行全部测试并更新文档

- [ ] **步骤 1：运行全部测试**

```bash
mvn test -q
```

确认所有新测试通过，无回归。

- [ ] **步骤 2：更新 README.md — 移除已修复的局限**

删除 README.md 中"已知局限"章节的以下条目：
- ~~admin-service 多数操作仅打日志~~（已过时，已有完整 Feign 代理实现）
- ~~@CurrentUser 注解已定义但无参数解析器~~（已创建 UserContext + CurrentUserArgumentResolver）
- ~~OrderPaidMessage 在 3 个服务中各自复制~~（已抽取到 common-mq）
- ~~缓存刷新 consumer 未实现~~（已实现精确驱逐）

保留并更新：
- **零测试** → 改为"部分模块已补充核心链路单元测试，覆盖率仍待提升"
- 其余不变

- [ ] **步骤 3：更新 CLAUDE.md — 同步已知局限**

将 CLAUDE.md 中"已知局限"章节与 README.md 保持一致。

- [ ] **步骤 4：Commit**

```bash
git add README.md CLAUDE.md
git commit -m "docs: 更新已知局限，标记已修复项"
```

---

### 任务 23：最终验证

- [ ] **步骤 1：全量编译 + 测试**

```bash
mvn clean verify -DskipTests=false -q
```

- [ ] **步骤 2：确认无编译警告或测试失败**

检查输出，确保 BUILD SUCCESS。

- [ ] **步骤 3：最终 Commit（如有残留改动）**

```bash
git status
git add -A
git commit -m "chore: 最终清理"
```
