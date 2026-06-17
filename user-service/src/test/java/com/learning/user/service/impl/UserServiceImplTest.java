package com.learning.user.service.impl;

import com.learning.common.core.exception.BizException;
import com.learning.common.security.util.JwtUtil;
import com.learning.user.dto.req.LoginReq;
import com.learning.user.dto.req.RegisterReq;
import com.learning.user.dto.resp.LoginResp;
import com.learning.user.dto.resp.UserInfoResp;
import com.learning.user.entity.User;
import com.learning.user.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    @Test
    @DisplayName("注册 — 手机号已存在应抛异常")
    void shouldThrowExceptionWhenPhoneExists() {
        RegisterReq req = new RegisterReq();
        req.setPhone("13800138000");
        req.setPassword("123456");
        req.setUsername("testuser");
        when(userMapper.selectCount(any())).thenReturn(1L);
        assertThrows(BizException.class, () -> userService.register(req));
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("注册 — 新用户成功注册")
    void shouldRegisterSuccessfully() {
        RegisterReq req = new RegisterReq();
        req.setPhone("13800138000");
        req.setPassword("123456");
        req.setUsername("testuser");
        when(userMapper.selectCount(any())).thenReturn(0L);
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
        user.setPassword(ENCODER.encode("correctpassword"));
        user.setStatus(1);
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
        user.setPassword(ENCODER.encode("123456"));
        user.setRole(0);
        user.setStatus(1);
        user.setNickname("testnick");
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
        user.setAvatarUrl("http://avatar.png");
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
        user.setPassword(ENCODER.encode("correct"));
        when(userMapper.selectById(1L)).thenReturn(user);
        assertThrows(BizException.class,
                () -> userService.changePassword(1L, "wrong", "newpass"));
    }

    @Test
    @DisplayName("修改密码 — 新密码太短抛异常")
    void shouldThrowExceptionWhenNewPasswordTooShort() {
        assertThrows(BizException.class,
                () -> userService.changePassword(1L, "old", "12345"));
    }
}
