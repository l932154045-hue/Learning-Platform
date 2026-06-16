package com.learning.admin.service.impl;

import com.learning.admin.client.UserServiceClient;
import com.learning.admin.service.AdminAuthService;
import com.learning.admin.service.UserAdminService;
import com.learning.common.core.exception.BizException;
import com.learning.common.core.page.PageReq;
import com.learning.common.core.page.PageResp;
import com.learning.common.core.result.R;
import com.learning.common.core.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {
    private final AdminAuthService authService;
    private final UserServiceClient userServiceClient;

    @Override
    public void updateUserStatus(Long userId, Integer status, Integer role) {
        authService.checkAdmin(role);
        R<Void> result = userServiceClient.updateUserStatus(userId, status);
        if (result == null || result.getCode() != 200) {
            throw new BizException(ResultCode.REMOTE_CALL_ERROR);
        }
        log.info("管理员更新用户状态: userId={}, status={}", userId, status);
    }

    @Override
    public PageResp<Map<String, Object>> listUsers(PageReq req, Integer role) {
        authService.checkAdmin(role);
        R<PageResp<Map<String, Object>>> result = userServiceClient.listUsers(req);
        if (result == null || result.getCode() != 200) {
            throw new BizException(ResultCode.REMOTE_CALL_ERROR);
        }
        return result.getData();
    }
}
