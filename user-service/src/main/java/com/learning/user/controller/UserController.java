package com.learning.user.controller;

import com.learning.common.core.result.R;
import com.learning.common.security.annotation.CurrentUser;
import com.learning.common.security.context.UserContext;
import com.learning.user.dto.req.ChangePasswordReq;
import com.learning.user.dto.req.LoginReq;
import com.learning.user.dto.req.RegisterReq;
import com.learning.user.dto.req.UpdateUserInfoReq;
import com.learning.user.dto.resp.LoginResp;
import com.learning.user.dto.resp.UserInfoResp;
import com.learning.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody RegisterReq req) {
        userService.register(req);
        return R.ok();
    }

    @PostMapping("/login")
    public R<LoginResp> login(@Valid @RequestBody LoginReq req) {
        return R.ok(userService.login(req));
    }

    @GetMapping("/info")
    public R<UserInfoResp> getUserInfo(@CurrentUser UserContext userContext) {
        return R.ok(userService.getUserInfo(userContext.getUserId()));
    }

    @PutMapping("/info")
    public R<Void> updateInfo(@CurrentUser UserContext userContext,
                              @Valid @RequestBody UpdateUserInfoReq req) {
        userService.updateUserInfo(userContext.getUserId(), req);
        return R.ok();
    }

    @PutMapping("/password")
    public R<Void> changePassword(@CurrentUser UserContext userContext,
                                  @RequestBody ChangePasswordReq req) {
        userService.changePassword(userContext.getUserId(), req.getOldPassword(), req.getNewPassword());
        return R.ok();
    }
}
