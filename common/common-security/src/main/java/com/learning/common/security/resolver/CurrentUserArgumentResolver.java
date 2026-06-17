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
