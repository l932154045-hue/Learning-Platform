package com.learning.common.web.config;

import com.learning.common.core.constant.HeaderConstants;
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
        String userId = request.getHeader(HeaderConstants.X_USER_ID);
        String role = request.getHeader(HeaderConstants.X_USER_ROLE);
        if (userId != null) {
            template.header(HeaderConstants.X_USER_ID, userId);
        }
        if (role != null) {
            template.header(HeaderConstants.X_USER_ROLE, role);
        }
    }
}
