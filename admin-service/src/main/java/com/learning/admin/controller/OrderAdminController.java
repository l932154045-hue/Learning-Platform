package com.learning.admin.controller;

import com.learning.admin.service.OrderAdminService;
import com.learning.common.core.page.PageReq;
import com.learning.common.core.page.PageResp;
import com.learning.common.core.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
public class OrderAdminController {

    private final OrderAdminService orderAdminService;

    @GetMapping("/list")
    public R<PageResp<Map<String, Object>>> list(PageReq req,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) Integer status,
                                                  @RequestAttribute("role") Integer role) {
        return R.ok(orderAdminService.listOrders(req, keyword, status, role));
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable("id") Long id,
                                 @RequestParam Integer status,
                                 @RequestAttribute("role") Integer role) {
        orderAdminService.updateOrderStatus(id, status, role);
        return R.ok();
    }
}
