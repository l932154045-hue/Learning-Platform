package com.learning.gateway.filter;

import com.learning.common.core.constant.HeaderConstants;
import com.learning.common.security.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/user/register",
            "/api/user/login",
            "/api/course/list",
            "/api/course/detail",
            "/api/course/category/tree",
            "/api/course/hot",
            "/api/learning/course",
            // Swagger / Knife4j 文档端点
            "/doc.html",
            "/v3/api-docs",
            "/swagger-ui",
            "/webjars"
    );

    public AuthGlobalFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 剥离外部传入的内部头，防止用户身份伪造
        ServerWebExchange cleanedExchange = exchange.mutate()
                .request(r -> r.headers(headers -> {
                    headers.remove(HeaderConstants.X_USER_ID);
                    headers.remove(HeaderConstants.X_USER_ROLE);
                }))
                .build();

        String path = cleanedExchange.getRequest().getURI().getPath();

        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                return chain.filter(cleanedExchange);
            }
        }

        String authHeader = cleanedExchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            cleanedExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return cleanedExchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        try {
            if (jwtUtil.isTokenExpired(token)) {
                cleanedExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return cleanedExchange.getResponse().setComplete();
            }
            Long userId = jwtUtil.getUserId(token);
            Integer role = jwtUtil.getRole(token);

            ServerWebExchange mutatedExchange = cleanedExchange.mutate()
                    .request(r -> r.header(HeaderConstants.X_USER_ID, String.valueOf(userId))
                            .header(HeaderConstants.X_USER_ROLE, String.valueOf(role)))
                    .build();
            return chain.filter(mutatedExchange);
        } catch (Exception e) {
            log.warn("Token校验失败: {}", e.getMessage());
            cleanedExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return cleanedExchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
