package com.learning.admin.controller;

import com.learning.admin.dto.req.UserStatusReq;
import com.learning.admin.service.UserAdminService;
import com.learning.common.core.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userAdminService;

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable("id") Long id,
                                 @RequestBody UserStatusReq req,
                                 @RequestAttribute("role") Integer role) {
        userAdminService.updateUserStatus(id, req.getStatus(), role);
        return R.ok();
    }
}
