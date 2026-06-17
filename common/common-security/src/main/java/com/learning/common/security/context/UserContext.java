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
