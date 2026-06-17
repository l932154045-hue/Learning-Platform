package com.learning.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.learning.common.core.exception.BizException;
import com.learning.common.core.page.PageReq;
import com.learning.common.core.page.PageResp;
import com.learning.common.core.result.ResultCode;
import com.learning.common.security.util.JwtUtil;
import com.learning.user.dto.req.LoginReq;
import com.learning.user.dto.req.RegisterReq;
import com.learning.user.dto.resp.LoginResp;
import com.learning.user.dto.resp.UserInfoResp;
import com.learning.user.dto.resp.UserListResp;
import com.learning.user.entity.User;
import com.learning.user.mapper.UserMapper;
import com.learning.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public void register(RegisterReq req) {
        Long phoneCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getPhone, req.getPhone()));
        if (phoneCount > 0) {
            throw new BizException(ResultCode.PHONE_EXIST);
        }
        Long nameCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername()));
        if (nameCount > 0) {
            throw new BizException(ResultCode.USERNAME_EXIST);
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(PASSWORD_ENCODER.encode(req.getPassword()));
        user.setPhone(req.getPhone());
        user.setNickname(req.getUsername());
        user.setRole(0);
        user.setStatus(1);
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setUpdatedAt(java.time.LocalDateTime.now());
        userMapper.insert(user);
        log.info("用户注册成功: userId={}", user.getId());
    }

    @Override
    public LoginResp login(LoginReq req) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, req.getPhone()));
        if (user == null || user.getStatus() == 0) {
            throw new BizException(ResultCode.LOGIN_FAIL);
        }
        if (!PASSWORD_ENCODER.matches(req.getPassword(), user.getPassword())) {
            throw new BizException(ResultCode.LOGIN_FAIL);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        log.info("用户登录成功: userId={}", user.getId());
        return new LoginResp(token, user.getId(), user.getNickname(), user.getRole());
    }

    @Override
    public UserInfoResp getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.LOGIN_FAIL);
        }
        UserInfoResp resp = new UserInfoResp();
        BeanUtils.copyProperties(user, resp);
        return resp;
    }

    @Override
    @Transactional
    public void updateUserInfo(Long userId, UserInfoResp req) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.LOGIN_FAIL);
        }
        if (req.getNickname() != null) user.setNickname(req.getNickname());
        if (req.getEmail() != null) user.setEmail(req.getEmail());
        if (req.getAvatarUrl() != null) user.setAvatarUrl(req.getAvatarUrl());
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new BizException(ResultCode.PASSWORD_TOO_SHORT);
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.LOGIN_FAIL);
        }
        if (!PASSWORD_ENCODER.matches(oldPassword, user.getPassword())) {
            throw new BizException(ResultCode.PASSWORD_WRONG);
        }
        user.setPassword(PASSWORD_ENCODER.encode(newPassword));
        userMapper.updateById(user);
    }

    @Override
    public PageResp<UserListResp> listUsers(PageReq req, String keyword, Integer role, Integer status) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<User>()
                .orderByDesc(User::getCreatedAt);
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getPhone, keyword));
        }
        if (role != null) {
            qw.eq(User::getRole, role);
        }
        if (status != null) {
            qw.eq(User::getStatus, status);
        }
        Page<User> page = new Page<>(req.getPageNum(), req.getPageSize());
        IPage<User> iPage = userMapper.selectPage(page, qw);

        List<UserListResp> list = iPage.getRecords().stream().map(user -> {
            UserListResp resp = new UserListResp();
            BeanUtils.copyProperties(user, resp);
            return resp;
        }).collect(Collectors.toList());

        return PageResp.of(list, iPage.getTotal(), req.getPageNum(), req.getPageSize());
    }

    @Override
    @Transactional
    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.LOGIN_FAIL);
        }
        // Prevent disabling the last admin
        if (status != null && status == 0 && user.getRole() != null && user.getRole() == 1) {
            Long adminCount = userMapper.selectCount(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getRole, 1)
                            .eq(User::getStatus, 1));
            if (adminCount <= 1) {
                throw new BizException(40019, "无法禁用最后一个管理员账号");
            }
        }
        user.setStatus(status);
        userMapper.updateById(user);
        log.info("管理员更新用户状态: userId={}, status={}", userId, status);
    }

    @Override
    public Long getUserCount() {
        return userMapper.selectCount(null);
    }
}
