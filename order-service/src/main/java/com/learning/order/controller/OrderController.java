package com.learning.order.controller;

import com.learning.common.core.result.R;
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
    public R<OrderDetailVO> detail(@PathVariable Long id) {
        return R.ok(orderService.getDetail(id));
    }

    @GetMapping("/list")
    public R<List<OrderDetailVO>> list(@RequestAttribute("userId") Long userId) {
        return R.ok(orderService.list(userId));
    }

    @PutMapping("/cancel/{id}")
    public R<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return R.ok();
    }
}
