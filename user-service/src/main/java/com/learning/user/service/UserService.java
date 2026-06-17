package com.learning.user.service;

import com.learning.common.core.page.PageReq;
import com.learning.common.core.page.PageResp;
import com.learning.user.dto.req.LoginReq;
import com.learning.user.dto.req.RegisterReq;
import com.learning.user.dto.resp.LoginResp;
import com.learning.user.dto.resp.UserInfoResp;
import com.learning.user.dto.resp.UserListResp;

public interface UserService {
    void register(RegisterReq req);
    LoginResp login(LoginReq req);
    UserInfoResp getUserInfo(Long userId);
    void updateUserInfo(Long userId, UserInfoResp req);
    void changePassword(Long userId, String oldPassword, String newPassword);
    PageResp<UserListResp> listUsers(PageReq req, String keyword, Integer role, Integer status);
    void updateUserStatus(Long userId, Integer status);
    Long getUserCount();
}
