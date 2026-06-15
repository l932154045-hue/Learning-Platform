package com.learning.order.controller;

import com.learning.common.core.exception.BizException;
import com.learning.common.core.result.R;
import com.learning.common.core.result.ResultCode;
import com.learning.order.dto.req.CreateOrderReq;
import com.learning.order.dto.resp.OrderDetailVO;
import com.learning.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public R<Long> create(@RequestAttribute("userId") Long userId,
                          @Valid @RequestBody CreateOrderReq req) {
        return R.ok(orderService.createOrder(userId, req));
    }

    @GetMapping("/detail/{id}")
    public R<OrderDetailVO> detail(@PathVariable Long id,
                                    @RequestAttribute("userId") Long userId) {
        return R.ok(orderService.getDetail(id, userId));
    }

    @GetMapping("/list")
    public R<List<OrderDetailVO>> list(@RequestAttribute("userId") Long userId) {
        return R.ok(orderService.list(userId));
    }

    @PutMapping("/cancel/{id}")
    public R<Void> cancel(@PathVariable Long id,
                           @RequestAttribute("userId") Long userId) {
        orderService.cancel(id, userId);
        return R.ok();
    }

    @GetMapping("/internal/owner/{id}")
    public R<Long> getOwner(@PathVariable Long id,
                             @RequestAttribute(value = "role", required = false) Integer role) {
        // 通过网关访问时校验管理员角色, 内部 Feign 调用时 role 为 null 放行
        if (role != null && role != 1) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        return R.ok(orderService.getOwnerUserId(id));
    }

    @PutMapping("/internal/updateStatus/{id}")
    public R<Void> updateStatus(@PathVariable Long id,
                                 @RequestParam("status") Integer status,
                                 @RequestAttribute(value = "role", required = false) Integer role) {
        // 通过网关访问时校验管理员角色, 内部 Feign 调用时 role 为 null 放行
        if (role != null && role != 1) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        orderService.updateStatus(id, status);
        return R.ok();
    }
}
