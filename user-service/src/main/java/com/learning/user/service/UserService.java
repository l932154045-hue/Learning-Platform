package com.learning.user.service;

import com.learning.user.dto.req.LoginReq;
import com.learning.user.dto.req.RegisterReq;
import com.learning.user.dto.resp.LoginResp;
import com.learning.user.dto.resp.UserInfoResp;

public interface UserService {
    void register(RegisterReq req);
    LoginResp login(LoginReq req);
    UserInfoResp getUserInfo(Long userId);
    void updateUserInfo(Long userId, UserInfoResp req);
}
