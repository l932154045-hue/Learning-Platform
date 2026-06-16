package com.learning.admin.service;

import com.learning.common.core.page.PageReq;
import com.learning.common.core.page.PageResp;

import java.util.Map;

public interface UserAdminService {
    void updateUserStatus(Long userId, Integer status, Integer role);
    PageResp<Map<String, Object>> listUsers(PageReq req, Integer role);
}
