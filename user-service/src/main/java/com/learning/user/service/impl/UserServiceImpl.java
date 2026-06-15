package com.learning.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.learning.common.core.exception.BizException;
import com.learning.common.core.result.ResultCode;
import com.learning.common.security.util.JwtUtil;
import com.learning.user.dto.req.LoginReq;
import com.learning.user.dto.req.RegisterReq;
import com.learning.user.dto.resp.LoginResp;
import com.learning.user.dto.resp.UserInfoResp;
import com.learning.user.entity.User;
import com.learning.user.mapper.UserMapper;
import com.learning.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
