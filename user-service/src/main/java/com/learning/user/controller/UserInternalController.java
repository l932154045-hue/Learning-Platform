package com.learning.user.controller;

import com.learning.common.core.page.PageReq;
import com.learning.common.core.page.PageResp;
import com.learning.common.core.result.R;
import com.learning.user.dto.resp.UserListResp;
import com.learning.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/internal")
@RequiredArgsConstructor
public class UserInternalController {

    private final UserService userService;

    @GetMapping("/list")
    public R<PageResp<UserListResp>> list(PageReq req,
                                           @RequestAttribute(value = "role", required = false) Integer role) {
        // Only admin can list users (role=1) or internal Feign call (role=null)
        if (role != null && role != 1) {
            return R.fail(40015, "无权访问");
        }
        return R.ok(userService.listUsers(req));
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id,
                                 @RequestParam Integer status,
                                 @RequestAttribute(value = "role", required = false) Integer role) {
        if (role != null && role != 1) {
            return R.fail(40015, "无权访问");
        }
        userService.updateUserStatus(id, status);
        return R.ok();
    }

    @GetMapping("/count")
    public R<Long> getUserCount() {
        return R.ok(userService.getUserCount());
    }
}
