package com.learning.common.core.constant;

/**
 * 内部请求头常量 — 由 Gateway 写入，服务间通过 Feign 透传。
 * 外部请求中携带的这些头会被 Gateway 剥离，不可信任。
 */
public final class HeaderConstants {

    private HeaderConstants() {
    }

    /** 当前登录用户 ID */
    public static final String X_USER_ID = "X-User-Id";

    /** 当前登录用户角色（0=普通用户, 1=管理员） */
    public static final String X_USER_ROLE = "X-User-Role";
}
