package com.learning.admin.controller;

import com.learning.admin.dto.req.UserStatusReq;
import com.learning.admin.service.UserAdminService;
import com.learning.common.core.page.PageReq;
import com.learning.common.core.page.PageResp;
import com.learning.common.core.result.R;
import com.learning.common.security.annotation.CurrentUser;
import com.learning.common.security.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userAdminService;

    @GetMapping("/list")
    public R<PageResp<Map<String, Object>>> list(PageReq req,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) Integer roleFilter,
                                                  @RequestParam(required = false) Integer status,
                                                  @CurrentUser UserContext userContext) {
        return R.ok(userAdminService.listUsers(req, keyword, roleFilter, status, userContext.getRole()));
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable("id") Long id,
                                 @RequestBody UserStatusReq req,
                                 @CurrentUser UserContext userContext) {
        userAdminService.updateUserStatus(id, req.getStatus(), userContext.getRole());
        return R.ok();
    }
}
