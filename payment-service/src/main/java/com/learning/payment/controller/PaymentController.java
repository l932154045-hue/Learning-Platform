package com.learning.payment.controller;

import com.learning.common.core.result.R;
import com.learning.payment.dto.resp.PayResultVO;
import com.learning.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/pay/{orderId}")
    public R<PayResultVO> pay(@RequestAttribute("userId") Long userId,
                               @PathVariable Long orderId) {
        return R.ok(paymentService.pay(userId, orderId));
    }

    @GetMapping("/result/{orderId}")
    public R<PayResultVO> result(@PathVariable Long orderId) {
        return R.ok(paymentService.queryResult(orderId));
    }
}
