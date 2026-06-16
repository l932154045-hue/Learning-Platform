package com.learning.order.controller;

import com.learning.common.core.exception.BizException;
import com.learning.common.core.page.PageReq;
import com.learning.common.core.page.PageResp;
import com.learning.common.core.result.R;
import com.learning.common.core.result.ResultCode;
import com.learning.order.dto.req.CreateOrderReq;
import com.learning.order.dto.resp.OrderDetailVO;
import com.learning.order.dto.resp.OrderListVO;
import com.learning.order.dto.resp.OrderSummaryVO;
import com.learning.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    public R<OrderDetailVO> detail(@PathVariable("id") Long id,
                                    @RequestAttribute("userId") Long userId) {
        return R.ok(orderService.getDetail(id, userId));
    }

    @GetMapping("/list")
    public R<List<OrderDetailVO>> list(@RequestAttribute("userId") Long userId) {
        return R.ok(orderService.list(userId));
    }

    @PutMapping("/cancel/{id}")
    public R<Void> cancel(@PathVariable("id") Long id,
                           @RequestAttribute("userId") Long userId) {
        orderService.cancel(id, userId);
        return R.ok();
    }

    @GetMapping("/internal/owner/{id}")
    public R<Long> getOwner(@PathVariable("id") Long id,
                             @RequestAttribute(value = "role", required = false) Integer role) {
        // 通过网关访问时校验管理员角色, 内部 Feign 调用时 role 为 null 放行
        if (role != null && role != 1) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        return R.ok(orderService.getOwnerUserId(id));
    }

    @GetMapping("/internal/courseId/{id}")
    public R<Long> getCourseId(@PathVariable("id") Long id,
                                @RequestAttribute(value = "role", required = false) Integer role) {
        if (role != null && role != 1) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        return R.ok(orderService.getCourseId(id));
    }

    @GetMapping("/internal/totalAmount/{id}")
    public R<java.math.BigDecimal> getTotalAmount(@PathVariable("id") Long id,
                                                   @RequestAttribute(value = "role", required = false) Integer role) {
        if (role != null && role != 1) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        return R.ok(orderService.getTotalAmount(id));
    }

    @GetMapping("/internal/summary/{id}")
    public R<OrderSummaryVO> getSummary(@PathVariable("id") Long id,
                                         @RequestAttribute(value = "role", required = false) Integer role) {
        if (role != null && role != 1) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        return R.ok(orderService.getOrderSummary(id));
    }

    @PutMapping("/internal/updateStatus/{id}")
    public R<Void> updateStatus(@PathVariable("id") Long id,
                                 @RequestParam("status") Integer status,
                                 @RequestAttribute(value = "role", required = false) Integer role) {
        // 通过网关访问时校验管理员角色, 内部 Feign 调用时 role 为 null 放行
        if (role != null && role != 1) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        orderService.updateStatus(id, status);
        return R.ok();
    }

    @GetMapping("/internal/list")
    public R<PageResp<OrderListVO>> listAll(PageReq req,
                                             @RequestAttribute(value = "role", required = false) Integer role) {
        if (role != null && role != 1) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        return R.ok(orderService.listAllOrders(req));
    }

    @GetMapping("/internal/count")
    public R<Long> getOrderCount(@RequestAttribute(value = "role", required = false) Integer role) {
        if (role != null && role != 1) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        return R.ok(orderService.getOrderCount());
    }

    @GetMapping("/internal/revenue")
    public R<BigDecimal> getTotalRevenue(@RequestAttribute(value = "role", required = false) Integer role) {
        if (role != null && role != 1) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        return R.ok(orderService.getTotalRevenue());
    }
}
