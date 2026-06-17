package com.learning.common.security.context;

import lombok.Getter;

/**
 * 用户上下文 — 从 JWT 解析的用户信息，通过 @CurrentUser 注解注入 Controller
 */
@Getter
public class UserContext {

    /** 管理员角色编码 */
    public static final Integer ROLE_ADMIN = 1;

    private final Long userId;
    private final Integer role;

    public UserContext(Long userId, Integer role) {
        this.userId = userId;
        this.role = role;
    }

    /** 是否为管理员 */
    public boolean isAdmin() {
        return role != null && role.equals(ROLE_ADMIN);
    }
}
