package com.learning.admin.client;

import com.learning.common.core.page.PageReq;
import com.learning.common.core.page.PageResp;
import com.learning.common.core.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "user-service", path = "/api/user")
public interface UserServiceClient {

    @GetMapping("/internal/list")
    R<PageResp<Map<String, Object>>> listUsers(@SpringQueryMap PageReq req,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) Integer roleFilter,
                                                @RequestParam(required = false) Integer status);

    @PutMapping("/internal/{id}/status")
    R<Void> updateUserStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status);

    @GetMapping("/internal/count")
    R<Long> getUserCount();
}
