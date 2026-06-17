package com.learning.admin.controller;

import com.learning.admin.dto.req.UserStatusReq;
import com.learning.admin.service.UserAdminService;
import com.learning.common.core.page.PageReq;
import com.learning.common.core.page.PageResp;
import com.learning.common.core.result.R;
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
                                                  @RequestAttribute("role") Integer role) {
        return R.ok(userAdminService.listUsers(req, keyword, roleFilter, status, role));
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable("id") Long id,
                                 @RequestBody UserStatusReq req,
                                 @RequestAttribute("role") Integer role) {
        userAdminService.updateUserStatus(id, req.getStatus(), role);
        return R.ok();
    }
}
