package com.learning.common.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtUtil 单元测试")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "test-secret-key-for-unit-test-min-256-bits-long!!");
        ReflectionTestUtils.setField(jwtUtil, "ttl", 3600_000L);
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
        ReflectionTestUtils.setField(jwtUtil, "ttl", -1L);
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

    @Test
    @DisplayName("isTokenExpired 对过期 token 返回 true")
    void shouldReturnTrueForExpiredToken() {
        ReflectionTestUtils.setField(jwtUtil, "ttl", -1L);
        String token = jwtUtil.generateToken(1L, 0);
        assertTrue(jwtUtil.isTokenExpired(token));
    }

    @Test
    @DisplayName("isTokenExpired 对有效 token 返回 false")
    void shouldReturnFalseForValidToken() {
        String token = jwtUtil.generateToken(1L, 0);
        assertFalse(jwtUtil.isTokenExpired(token));
    }
}
