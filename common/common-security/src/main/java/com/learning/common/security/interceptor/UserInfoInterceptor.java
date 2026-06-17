package com.learning.common.security.interceptor;

import com.learning.common.core.constant.HeaderConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserInfoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = request.getHeader(HeaderConstants.X_USER_ID);
        String role = request.getHeader(HeaderConstants.X_USER_ROLE);
        if (userId != null) {
            request.setAttribute("userId", Long.parseLong(userId));
        }
        if (role != null) {
            request.setAttribute("role", Integer.parseInt(role));
        }
        return true;
    }
}
