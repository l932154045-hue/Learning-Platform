package com.learning.admin.service;

import com.learning.common.core.exception.BizException;
import com.learning.common.core.result.ResultCode;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {
    public void checkAdmin(Integer role) {
        if (role == null || role != 1) {
            throw new BizException(ResultCode.TOKEN_INVALID.getCode(), "仅管理员可操作");
        }
    }
}
