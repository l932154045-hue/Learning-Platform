package com.learning.common.web.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign 调用拦截器 — 从当前 HTTP 请求中提取 X-User-Id/X-User-Role，
 * 自动填充到 Feign 请求头，确保服务间调用的用户上下文能正确传递。
 */
@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        String userId = request.getHeader("X-User-Id");
        String role = request.getHeader("X-User-Role");
        if (userId != null) {
            template.header("X-User-Id", userId);
        }
        if (role != null) {
            template.header("X-User-Role", role);
        }
    }
}
