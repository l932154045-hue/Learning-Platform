package com.learning.admin.service.impl;

import com.learning.admin.service.AdminAuthService;
import com.learning.admin.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {
    private final AdminAuthService authService;

    @Override
    public void updateUserStatus(Long userId, Integer status, Integer role) {
        authService.checkAdmin(role);
        log.info("管理员更新用户状态: userId={}, status={}", userId, status);
    }
}
