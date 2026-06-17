package com.learning.user.controller;

import com.learning.common.core.result.R;
import com.learning.user.dto.req.ChangePasswordReq;
import com.learning.user.dto.req.LoginReq;
import com.learning.user.dto.req.RegisterReq;
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
    public R<UserInfoResp> getUserInfo(@RequestAttribute("userId") Long userId) {
        return R.ok(userService.getUserInfo(userId));
    }

    @PutMapping("/info")
    public R<Void> updateInfo(@RequestAttribute("userId") Long userId,
                               @RequestBody UserInfoResp req) {
        userService.updateUserInfo(userId, req);
        return R.ok();
    }

    @PutMapping("/password")
    public R<Void> changePassword(@RequestAttribute("userId") Long userId,
                                   @RequestBody ChangePasswordReq req) {
        userService.changePassword(userId, req.getOldPassword(), req.getNewPassword());
        return R.ok();
    }
}
